package com.aman_arora.multi_thread_downloader.downloader

import android.content.Context
import com.aman_arora.multi_thread_downloader.file_manager.FileManager
import com.aman_arora.multi_thread_downloader.url_info.UrlDetails
import java.io.File
import java.io.IOException
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object MultiThreadDownloader : IMultiThreadDownloader {

    private val NO_CONTENT_LENGTH_CONSTANT: Long = -1

    private val mBlockingQueue = LinkedBlockingDeque<Runnable>()
    private val mExecutor = ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Int.MAX_VALUE,
            1, TimeUnit.SECONDS, mBlockingQueue)
    private val downloadIdMap = HashMap<Long, DownloadInfo>()
    private val listenerMap = HashMap<Long, IMultiThreadDownloader.OnDownloadEventListener>()

    private var fileManager: FileManager? = null
    private var downloadFinalizer: DownloadFinalizer? = null

    override fun init(context: Context) {
        if (fileManager == null) {
            this.fileManager = FileManager(context)
            this.downloadFinalizer = DownloadFinalizer(fileManager!!)
        } else {
            throw IllegalStateException()
        }
    }

    override fun download(webAddress: String, maxThreadCount: Int, file: File?,
                          eventListener: IMultiThreadDownloader.OnDownloadEventListener): Long {
        if (fileManager == null) {
            throw IllegalStateException()
        }

        if (file != null && file.exists() && !file.canWrite()) {
            throw IOException()
        }

        val id = downloadIdMap.size.toLong()
        val urlDetails = UrlDetails(webAddress)
        val info = DownloadInfo(id, urlDetails.fileName, maxThreadCount, webAddress)
        info.downloadFile = file ?: fileManager!!.createFile(urlDetails.fileName)
        downloadIdMap.put(id, info)

        listenerMap[id] = eventListener

        setDownloadState(info, IMultiThreadDownloader.DownloadState.STARTING)
        if (urlDetails.contentLength == NO_CONTENT_LENGTH_CONSTANT) {
            // TODO
        } else {
            addDownloadTasks(info, urlDetails, fileManager!!)
        }

        return downloadIdMap.size.toLong()
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

    private fun addDownloadTasks(downloadInfo: DownloadInfo, urlDetails: UrlDetails, fileManager: FileManager) {
        val threadCount = downloadInfo.threads

        val partSize = urlDetails.contentLength / downloadInfo.threads
        val listener = OnPartsDownloadEventListener(downloadInfo)

        var startByte = 0L
        var endByte = partSize - 1
        for (i in 1..threadCount) {
            val part = fileManager.createPartFile(urlDetails.fileName, i)
            part.delete()
            part.createNewFile()

            val task = DownloadTask(urlDetails.webUrl, i, startByte, endByte, part, listener)

            startByte = endByte + 1
            endByte += partSize
            if (i == threadCount - 1) {
                endByte = urlDetails.contentLength - 1
            }

            mExecutor.execute(task)
            downloadInfo.partFileList.add(part)
            downloadInfo.downloadTaskList.add(task)
            downloadInfo.threadProgressList.add(0f)
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

    private class OnPartsDownloadEventListener(val downloadInfo: DownloadInfo) : IDownloadTask.OnDownloadTaskEventListener {
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