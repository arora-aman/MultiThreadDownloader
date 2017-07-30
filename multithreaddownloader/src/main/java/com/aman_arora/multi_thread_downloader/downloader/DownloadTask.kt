package com.aman_arora.multi_thread_downloader.downloader

import com.aman_arora.multi_thread_downloader.downloader.IDownloadTask.OnDownloadTaskEventListener
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

internal class DownloadTask(val webUrl: URL, val thread: Int, val startByte: Long, val endByte: Long?,
                            val partFile: File, val listener: OnDownloadTaskEventListener) : IDownloadTask {
    private val partSize = endByte?.minus(startByte)?.plus(1)

    private var isPaused = false

    internal var isDownloaded = false

    override fun run() {
        if (endByte != null && this.startByte + partFile.length() >= endByte) {
            return
        }

        val httpConnection = getHttpConnection(this.startByte + partFile.length(), endByte)
        httpConnection.connect()

        val isr = BufferedInputStream(httpConnection.inputStream)
        val outputStream = FileOutputStream(partFile, true)
        val byteInputBuffer = ByteArray(1024)

        var bytesRead: Int
        var progress: Float
        var totalRead = partFile.length()

        while (true) {
            synchronized(isPaused) {
                if (isPaused) {
                    isr.close()
                    return
                }

                bytesRead = isr.read(byteInputBuffer)
                if (bytesRead == -1) {
                    if (endByte == null || partFile.length() == partSize) {
                        isDownloaded = true
                        listener.onDownloadComplete()
                    }
                    return
                }
                outputStream.write(byteInputBuffer, 0, bytesRead)
                totalRead += bytesRead

                if (partSize != null) {
                    progress = totalRead.toFloat().div(partSize)
                    listener.onProgressUpdated(thread, progress)
                }
            }
        }
    }

    override fun stopDownload() = synchronized(isPaused) {
        if (isPaused) {
            throw IllegalStateException()
        }
        isPaused = true

        return@synchronized
    }

    private fun getHttpConnection(startByte: Long,endByte: Long?): HttpURLConnection {
        val httpConnection = webUrl.openConnection() as HttpURLConnection
        httpConnection.setRequestProperty("Range", "bytes=$startByte-$endByte")
        return httpConnection
    }
}