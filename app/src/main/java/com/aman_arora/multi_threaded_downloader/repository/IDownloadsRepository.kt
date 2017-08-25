package com.aman_arora.multi_threaded_downloader.repository

import android.arch.lifecycle.LiveData
import android.content.Context
import com.aman_arora.multi_threaded_downloader.model.Download

interface IDownloadsRepository {
    /**
     * Initialises the repository.
     */
    fun init(context: Context)

    /**
     * Initiates download from the given <code>webAddress</code>.
     *
     * @return id of the download.
     */
    fun download(webAddress: String): Download

    /**
     * @return Total progress over all threads.
     */
    fun getProgress(id: Long): LiveData<Float>

    /**
     * @return An array list with progress of each thread.
     */
    fun getProgressOverAllThreads(id: Long): ArrayList<Float>

    /**
     * Pauses a specific download.
     */
    fun pause(id: Long)

    /**
     * Resumes a specific download.
     */
    fun resume(id: Long)
}