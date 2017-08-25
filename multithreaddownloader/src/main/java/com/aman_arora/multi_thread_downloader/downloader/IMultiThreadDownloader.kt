package com.aman_arora.multi_thread_downloader.downloader

import android.content.Context
import java.io.File
import java.io.IOException


interface IMultiThreadDownloader {

    enum class DownloadState {
        /**
         * Indicates that the download information is being fetched from the internet.
         */
        STARTING,

        /**
         * Indicates that the download is in progress.
         */
        DOWNLOADING,

        /**
         * Indicated that the downloaded parts are being stitched together.
         */
        FINALIZING,

        /**
         * Indicates that the download is complete.
         */
        DOWNLOADED,

        /**
         * Indicated that the download has been paused.
         */
        PAUSED,

        /**
         * Indicates that the previous states are being cleared.
         */
        RESTARTING,

        /**
         * Indicates that the download has been canceled.
         */
        CANCELED
    }

    /**
     * Initialise the downloader.
     *
     * @param context Application context.
     *
     * @throws IllegalStateException If the downloader has already been initialised.
     */
    @Throws(IllegalArgumentException::class)
    fun init(context: Context)

    /**
     * Download content from the internet on a number of concurrent threads.
     *
     * @param webAddress Web address to download content from.
     * @param maxThreadCount Maximum number of concurrent threads downloading the content.
     * @param file File where the content will be written to.
     * @param eventListener Handles state and progress changed events.
     *
     * @return Id of the download
     *
     * @throws IllegalStateException If the downloader has not been initialised.
     * @throws IOException If can't write to the <code>file</code>.
     */
    @Throws(IllegalArgumentException::class, IOException::class)
    fun download(webAddress: String, maxThreadCount: Int, file: File?, eventListener: OnDownloadEventListener): Long

    /**
     * @return Download Information for a download with the given <code>id</code>.
     *
     * @exception InvalidDownloadException If a download with <code>id</code> is not found.
     */
    @Throws(InvalidDownloadException:: class)
    fun getDownloadInfo(id: Long): DownloadInfo

    /**
     * Pauses the download with the given <code>id</code>.
     *
     * @throws InvalidDownloadException If a download with <code>id</code> is not found.
     */
    @Throws(IllegalArgumentException::class)
    fun pause(id: Long)

    /**
     * Resumes the download with the given <code>id</code>.
     *
     * @throws InvalidDownloadException If a download with <code>id</code> is not found.
     * @throws DownloadCantResumedException If the download can't be resumed and needs to be restarted.
     */
    @Throws(IllegalArgumentException::class, DownloadCantResumedException::class)
    fun resume(id: Long)

    /**
     * @return A map of a DownloadId -> {@link DownloadInfo} for all downloads.
     *
     * @throws IllegalStateException If the download manager hasn't been initiated.
     */
    @Throws(IllegalStateException::class)
    fun getAllDownloads() : Map<Long, DownloadInfo>

    /**
     * @param id Id of the download.
     *
     * @return True if the the location of the downloaded content can be changed i.e. when
     *         parts downloaded over different threads are not being stitched together.
     *
     * @throws IllegalArgumentException If a download with <code>id</code> is not found.
     */
    @Throws(InvalidDownloadException::class)
    fun isDownloadLocationChangeable(id: Long): Boolean

    /**
     * @param id Id of the download.
     * @param newFile New file where the content will be written to.
     *
     * @throws IllegalArgumentException If a download with <code>id</code> is not found.
     * @throws IllegalStateException If download location is not changeable.
     */
    @Throws(InvalidDownloadException::class, IllegalStateException::class)
    fun changeDownloadLocation(id: Long, newFile: File)

    interface OnDownloadEventListener {
        /**
         * Callback called when the download state has changed.
         *
         * @param id Id of the download.
         * @param state Current download state.
         */
        fun onDownloadStateChanged(id: Long, state: DownloadState)

        /**
         * Callback called when the download progress has changed.
         *
         * @param id Id of the download.
         * @param thread Thread or part number whose download progress has updated.
         * @param progress Decimal between 0 and 1 indicating the progress of the download on
         *                 the <code>thread</code>.
         */
        fun onDownloadProgressChanged(id: Long, thread: Int, progress: Float)
    }

    /**
     * Exception thrown when user tries to access information about download id that doesn't exist.
     */
    class InvalidDownloadException(id: Long) :
            IllegalArgumentException("Invalid download id: $id") {}

    /**
     * Exception thrown when the download can't be resumed.
     */
    class DownloadCantResumedException : RuntimeException() {}
}