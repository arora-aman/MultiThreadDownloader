package com.aman_arora.multi_thread_downloader.downloader

import android.arch.persistence.room.Room
import android.content.Context
import com.aman_arora.multi_thread_downloader.db.Download
import com.aman_arora.multi_thread_downloader.db.DownloadsDatabase
import com.aman_arora.multi_thread_downloader.file_manager.FileManager
import com.aman_arora.multi_thread_downloader.url_info.UrlDetails
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object MultiThreadDownloader : IMultiThreadDownloader {

    private val NO_CONTENT_LENGTH_CONSTANT: Long = -1

    private val mBlockingQueue = LinkedBlockingQueue<Runnable>()
    private val mExecutor = ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Int.MAX_VALUE,
            1, TimeUnit.SECONDS, mBlockingQueue)
    private val downloadIdMap = HashMap<Long, DownloadInfo>()
    private val listenerMap = HashMap<Long, IMultiThreadDownloader.OnDownloadEventListener>()

    private var fileManager: FileManager? = null
    private var database: DownloadsDatabase? = null
    private var downloadFinalizer: DownloadFinalizer? = null
    private var initialised = false

    override fun init(context: Context, eventListener: IMultiThreadDownloader.OnDownloaderInitiatedEventListener) {
        if (!initialised) {
            initialised = true
            mExecutor.execute {
                this.fileManager = FileManager(context)
                this.database = Room.databaseBuilder(context, DownloadsDatabase::class.java, "downloads-database").build()
                this.downloadFinalizer = DownloadFinalizer(fileManager!!)
                
                initDownloadMap()
                eventListener.onDownloaderInitiated()
            }
        } else {
            throw IllegalStateException()
        }
    }

    override fun download(webAddress: String, maxThreadCount: Int, file: File?,
                          eventListener: IMultiThreadDownloader.OnDownloadEventListener): Long {
        if (!initialised) {
            throw IllegalStateException()
        }

        if (file != null && file.exists() && !file.canWrite()) {
            throw IOException()
        }

        val id = UUID.randomUUID().hashCode().toLong()
        
        mExecutor.execute {
            val urlDetails = UrlDetails(webAddress)
            val threads: Int = if (urlDetails.contentLength == NO_CONTENT_LENGTH_CONSTANT) 1 else maxThreadCount
            val info = DownloadInfo(id, urlDetails.fileName, threads, webAddress)
            info.downloadFile = file ?: fileManager!!.createFile(urlDetails.fileName)
            downloadIdMap.put(id, info)

            listenerMap[id] = eventListener

            setDownloadState(info, IMultiThreadDownloader.DownloadState.STARTING)

            val dao = database?.downloadDao()
            val download = Download(id, webAddress, info.downloadFile!!.path, maxThreadCount)
            dao?.addDownload(download)

            addDownloadTasks(info, urlDetails, fileManager!!, true)
        }

        return id
    }

    override fun getDownloadInfo(id: Long): DownloadInfo {
        if (!downloadIdMap.containsKey(id)) {
            throw IllegalArgumentException()
        }

        return downloadIdMap[id]!!
    }

    override fun pause(id: Long) {
        if (!downloadIdMap.containsKey(id)) {
            throw IMultiThreadDownloader.InvalidDownloadException(id)
        }

        val downloadInfo = downloadIdMap[id]!!

        val downloadTaskList = downloadInfo.downloadTaskList

        for (task in downloadTaskList) {
            task.stopDownload()
        }

        downloadTaskList.clear()

        val threadList = downloadInfo.threadList
        for (thread in threadList) {
            thread.join()
        }

        threadList.clear()
        setDownloadState(downloadInfo, IMultiThreadDownloader.DownloadState.PAUSED)
    }

    override fun resume(id: Long) {
        if (!downloadIdMap.containsKey(id)) {
            throw IMultiThreadDownloader.InvalidDownloadException(id)
        }
        val downloadInfo = downloadIdMap[id]!!
        setDownloadState(downloadInfo, IMultiThreadDownloader.DownloadState.RESTARTING)

        mExecutor.execute {
            val urlDetails = UrlDetails(downloadInfo.webAddress)
            addDownloadTasks(downloadInfo, urlDetails, fileManager!!, false)
        }
    }

    override fun getAllDownloads(): Map<Long, DownloadInfo> {
        if (!initialised) {
            throw IllegalStateException()
        }

        return downloadIdMap.toMap()
    }

    override fun isDownloadLocationChangeable(id: Long): Boolean {
        if (!downloadIdMap.containsKey(id)) {
            throw IllegalArgumentException()
        }

        val state = downloadIdMap[id]!!.state
        return state != IMultiThreadDownloader.DownloadState.FINALIZING ||
                state != IMultiThreadDownloader.DownloadState.DOWNLOADED
    }

    override fun changeDownloadLocation(id: Long, newFile: File) {
        if (!downloadIdMap.containsKey(id)) {
            throw IllegalArgumentException()
        }
        if (!isDownloadLocationChangeable(id)) {
            throw IllegalStateException()
        }

        downloadIdMap[id]!!.downloadFile = newFile
    }

    private fun addDownloadTasks(downloadInfo: DownloadInfo, urlDetails: UrlDetails,
                                 fileManager: FileManager, isNewDownload: Boolean) {
        val threadCount = downloadInfo.threads

        val partSize = urlDetails.contentLength / downloadInfo.threads
        val listener = OnPartsDownloadEventListener(downloadInfo)

        var startByte = 0L
        var endByte: Long? = null

        if (urlDetails.contentLength != NO_CONTENT_LENGTH_CONSTANT) {
            endByte = partSize - 1
        }

        for (i in 1..threadCount) {
            val part = if (!isNewDownload) downloadInfo.partFileList[i - 1] else fileManager.createPartFile(urlDetails.fileName, i)

            val task = DownloadTask(urlDetails.webUrl, i, startByte, endByte, part, listener)

            if (urlDetails.contentLength != NO_CONTENT_LENGTH_CONSTANT) {
                startByte = endByte!! + 1
                endByte += partSize
                if (i == threadCount - 1) {
                    endByte = urlDetails.contentLength - 1
                }
            }

            mExecutor.execute(task)
            downloadInfo.downloadTaskList.add(task)
            if (isNewDownload) {
                downloadInfo.partFileList.add(part)
                downloadInfo.threadProgressList.add(0f)
            }
        }
    }

    private fun setDownloadState(downloadInfo: DownloadInfo, state: IMultiThreadDownloader.DownloadState) {
        if (downloadInfo.state == state) {
            return
        }

        downloadInfo.state = state
        listenerMap[downloadInfo.id]!!
                .onDownloadStateChanged(downloadInfo.id, state)
    }

    private fun setProgress(downloadInfo: DownloadInfo, thread: Int, progress: Float) {
        downloadInfo.threadProgressList.add(thread, progress)
        listenerMap[downloadInfo.id]!!.onDownloadProgressChanged(downloadInfo.id, thread, progress)
    }

    private fun initDownloadMap() {
        val downloads = database?.downloadDao()?.getDownloads()
        if (downloads != null) {
            for (download in downloads) {
                val downloadFile = File(download.file)
                downloadIdMap[download.id] = DownloadInfo(download.id, downloadFile.name, download.threadCount, download.webUrl)
                downloadIdMap[download.id]?.state = IMultiThreadDownloader.DownloadState.PAUSED
            }
        }
    }

    private class OnPartsDownloadEventListener(val downloadInfo: DownloadInfo) : IDownloadTask.OnDownloadTaskEventListener {
        override fun onDownloadStarted(currentThread: Thread) {
            downloadInfo.threadList.add(currentThread)
        }

        override fun onProgressUpdated(thread: Int, progress: Float) {
            setProgress(downloadInfo, thread, progress)
            setDownloadState(downloadInfo, IMultiThreadDownloader.DownloadState.DOWNLOADING)
        }

        override fun onDownloadComplete() {
            downloadInfo.downloadTaskList
                    .filterNot { it.isDownloaded }
                    .forEach { return }

            setDownloadState(downloadInfo, IMultiThreadDownloader.DownloadState.FINALIZING)
            downloadFinalizer!!
                    .finalizeDownload(downloadInfo.partFileList, downloadInfo.downloadFile!!) {
                        setDownloadState(downloadInfo, IMultiThreadDownloader.DownloadState.DOWNLOADED)
                    }
        }
    }
}