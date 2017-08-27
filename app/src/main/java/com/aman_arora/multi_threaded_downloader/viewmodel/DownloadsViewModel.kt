package com.aman_arora.multi_threaded_downloader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.webkit.URLUtil
import com.aman_arora.multi_threaded_downloader.model.Download
import com.aman_arora.multi_threaded_downloader.repository.DownloadsRepository

class DownloadsViewModel(application: Application?) : AndroidViewModel(application) {
    val downloadsRepository: DownloadsRepository = DownloadsRepository()

    fun init(): LiveData<Boolean> {
        return downloadsRepository.init(getApplication())
    }

    fun download(webAddress: String): Download {
        return downloadsRepository.download(webAddress)
    }

    fun pause(id: Long) {
        downloadsRepository.pause(id)
    }

    fun resume(id: Long) {
        downloadsRepository.resume(id)
    }

    fun isValidUrl(webAddress: String): Boolean {
        return URLUtil.isValidUrl(webAddress)
    }
}