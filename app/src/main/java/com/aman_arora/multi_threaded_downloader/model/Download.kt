package com.aman_arora.multi_threaded_downloader.model

import android.webkit.URLUtil

class Download(val threadCount: Int) {
    var downloadUrl: String = ""
        set(value) {
            field = URLUtil.guessUrl(value)
            if (!URLUtil.isValidUrl(field)) field = ""
        }

    var downloadId: Long = -1
        set(value) {
            if (downloadId == -1L) field = value
        }
}