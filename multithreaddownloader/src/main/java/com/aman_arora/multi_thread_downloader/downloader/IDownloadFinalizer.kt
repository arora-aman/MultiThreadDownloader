package com.aman_arora.multi_thread_downloader.downloader

import java.io.File

internal interface IDownloadFinalizer {

    /**
     * Stitches the partFiles to complete download process.
     *
     * @param partFiles List of parts downloaded.
     * @param finalFile File where the content needs to be saved after being stitched.
     * @param onDownloadCompleted Callback when the download completes.
     */
    fun finalizeDownload(partFiles: List<File>, finalFile: File, onDownloadCompleted: () -> Unit)
}