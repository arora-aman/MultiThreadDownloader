package com.aman_arora.multi_thread_downloader.file_manager

import java.io.File

internal interface IFileManager {
    /**
     * Creates a file in the download directory accessible by the context provided to the downloader.
     *
     * @param fileName Name of the file created.
     */
    fun createFile(fileName: String): File

    /**
     * Creates a temporary file with name <code>fileName</code>.part<code>partNumber</code> .
     */
    fun createPartFile(fileName: String, partNumber: Int): File

    /**
     * Stitches the part files together to the final downloaded file.
     * @return True if the task was successful.
     */
    fun stitchPartFiles(downloadFile: File, partFiles: List<File>): Boolean
}