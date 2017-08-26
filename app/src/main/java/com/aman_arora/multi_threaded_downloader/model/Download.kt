package com.aman_arora.multi_threaded_downloader.model

import android.arch.lifecycle.MutableLiveData
import com.aman_arora.multi_thread_downloader.downloader.IMultiThreadDownloader

public class Download(val id: Long, val webAddress: String, val threads: Int) {
    var state: IMultiThreadDownloader.DownloadState? = null
    val progressList = ArrayList<Float>(threads)
    var totalProgress = MutableLiveData<Float>()

    init {
        for (i in 1..threads) {
            progressList.add(0f)
        }
    }
}