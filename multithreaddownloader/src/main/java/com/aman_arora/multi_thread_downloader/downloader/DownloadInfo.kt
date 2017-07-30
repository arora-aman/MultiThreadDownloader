package com.aman_arora.multi_thread_downloader.downloader

import java.io.File

data class DownloadInfo(val id: Long, val suggestedFileName: String, val threads: Int, val webAddress: String) {
    internal var state : IMultiThreadDownloader.DownloadState? = null
    internal var downloadFile: File? = null

    internal val partFileList = mutableListOf<File>()
    internal val threadProgressList = mutableListOf<Float>()
    internal val downloadTaskList = mutableListOf<DownloadTask>()
    internal val threadList = mutableListOf<Thread>()

    fun getThreadProgressMap() : Array<Float> {
        return threadProgressList.toTypedArray()
    }

    fun getState() : IMultiThreadDownloader.DownloadState? {
        return state
    }

    fun getFile() : File? {
        return downloadFile
    }
}