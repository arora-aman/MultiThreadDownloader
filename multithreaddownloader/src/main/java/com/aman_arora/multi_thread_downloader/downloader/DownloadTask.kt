package com.aman_arora.multi_thread_downloader.downloader

import com.aman_arora.multi_thread_downloader.downloader.IDownloadTask.OnDownloadTaskEventListener
import java.io.BufferedInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

internal class DownloadTask(val webUrl: URL, val thread: Int, val startByte: Long, val endByte: Long?,
                            val partFile: File, val listener: OnDownloadTaskEventListener) : IDownloadTask {

    internal var isDownloaded = false

    override fun run() {
        val httpConnection = getHttpConnection()

        httpConnection.connect()

        val isr = BufferedInputStream(httpConnection.inputStream)
        val outputStream = partFile.outputStream()

        val byteInputBuffer = ByteArray(1024)
        var bytesRead : Int
        var totalRead: Int = 0

        while (true) {
            bytesRead = isr.read(byteInputBuffer)
            if (bytesRead == -1) {
                break
            }

            outputStream.write(byteInputBuffer, 0, bytesRead)
            totalRead += bytesRead
            if (endByte != null) {
                listener.onProgressUpdated(thread, totalRead.toFloat() / (endByte - startByte + 1))
            }
        }

        isDownloaded = true
        listener.onDownloadComplete()
    }

    private fun getHttpConnection(): HttpURLConnection {
        val httpConnection = webUrl.openConnection() as HttpURLConnection
        httpConnection.setRequestProperty("Range", "bytes=$startByte-$endByte")
        return httpConnection
    }
}