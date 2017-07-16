package com.aman_arora.multi_thread_downloader.file_manager

import android.content.Context
import android.os.Environment
import java.io.File

internal class FileManager(val context: Context) : IFileManager {
    override fun createFile(fileName: String) : File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
    }

    override fun createPartFile(fileName: String, partNumber: Int): File {
        val file = File(context.filesDir, fileName + ".part" + partNumber)
        file.delete()
        file.createNewFile()
        return file
    }

    override fun stitchPartFiles(downloadFile: File, partFiles: List< File>): Boolean {
        if (downloadFile.exists()) {
            downloadFile.delete()
        }
        downloadFile.createNewFile()
        for (file in partFiles) {
            downloadFile.appendBytes(file.readBytes())
            file.delete()
        }

        return true
    }
}