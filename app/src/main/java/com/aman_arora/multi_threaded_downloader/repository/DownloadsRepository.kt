package com.aman_arora.multi_threaded_downloader.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.aman_arora.multi_thread_downloader.downloader.IMultiThreadDownloader
import com.aman_arora.multi_thread_downloader.downloader.MultiThreadDownloader
import com.aman_arora.multi_threaded_downloader.model.Download

class DownloadsRepository: IDownloadsRepository {
    val TAG: String = "DownloadsRepository"

    val downloader = MultiThreadDownloader
    val downloadsMap = HashMap<Long, Download>()

    override fun init(context: Context): LiveData<Boolean> {
        val inited = MutableLiveData<Boolean>()
        inited.value = false
        downloader.init(context, object: IMultiThreadDownloader.OnDownloaderInitiatedEventListener {
            override fun onDownloaderInitiated() {
                for ((key, downloadInfo) in downloader.getAllDownloads()) {
                    val download = Download(downloadInfo.id, downloadInfo.webAddress, downloadInfo.threads)
                    download.progressList.addAll(downloadInfo.getThreadProgressMap())
                    download.state = downloadInfo.getState()!!
                    downloadsMap.put(key, download)
                }

                Log.d(TAG, downloadsMap.toList().toString())
                inited.postValue(true)
            }
        })

        return inited
    }

    override fun download(webAddress: String): Download {
        val id = downloader.download(webAddress, 4, null, DownloadEventListener())
        val download = Download(id, webAddress, 4)
        downloadsMap[id] = download

        return download
    }

    override fun getProgress(id: Long): LiveData<Float> {
        return downloadsMap[id]!!.totalProgress
    }

    override fun getProgressOverAllThreads(id: Long): ArrayList<Float> {
        return downloadsMap[id]!!.progressList
    }

    override fun pause(id: Long) {
        downloader.pause(id)
    }

    override fun resume(id: Long) {
        downloader.resume(id)
    }

    private inner class DownloadEventListener: IMultiThreadDownloader.OnDownloadEventListener {
        override fun onDownloadStateChanged(id: Long, state: IMultiThreadDownloader.DownloadState) {
            downloadsMap[id]?.state = state
            Log.d(TAG, "$id has state ${state.name}")
        }

        override fun onDownloadProgressChanged(id: Long, thread: Int, progress: Float) {
            val download = downloadsMap[id]
            download?.progressList!![thread-1] = progress
            val totalProgress = (1..download.threads)
                    .map { download.progressList[it] }
                    .sum()

            download.totalProgress.value = totalProgress
        }
    }
}