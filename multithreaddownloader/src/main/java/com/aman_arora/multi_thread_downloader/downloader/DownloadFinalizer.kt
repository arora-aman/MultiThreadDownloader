package com.aman_arora.multi_thread_downloader.downloader

import com.aman_arora.multi_thread_downloader.file_manager.FileManager
import java.io.File

internal class DownloadFinalizer(val fileManager: FileManager) : IDownloadFinalizer {
    override fun finalizeDownload(partFiles: List<File>, finalFile: File, onDownloadCompleted: () -> Unit) {
        fileManager.stitchPartFiles(finalFile, partFiles)
        onDownloadCompleted()
    }
}