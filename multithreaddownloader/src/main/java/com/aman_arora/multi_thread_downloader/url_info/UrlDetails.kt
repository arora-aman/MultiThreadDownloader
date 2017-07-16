package com.aman_arora.multi_thread_downloader.url_info

import android.webkit.URLUtil
import java.net.URL

internal data class UrlDetails(val webAddress: String) {
    val webUrl: URL = URL(webAddress)
    val fileName: String
    val contentLength: Long

    init {
        val httpConnection = webUrl.openConnection()

        fileName = URLUtil.guessFileName(webAddress, httpConnection.getHeaderField("Content-Disposition"),
                httpConnection.contentType)
        contentLength = httpConnection.contentLength.toLong()
    }

    override fun toString(): String {
        return "URLDetails{" +
                "webUrl='" + webUrl + '\'' +
                ", suggestedFileName='" + fileName + '\'' +
                ", contentLength='" +contentLength + '\'' +
                '}';
    }
}
