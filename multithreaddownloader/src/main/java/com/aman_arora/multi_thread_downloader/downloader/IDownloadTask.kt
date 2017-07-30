package com.aman_arora.multi_thread_downloader.downloader

internal interface IDownloadTask: Runnable{

    /**
     * Stops the current DownloadTask
     *
     * @throws IllegalStateException If the download has already been stopped.
     */
    @Throws(IllegalStateException::class)
    fun stopDownload()

    interface OnDownloadTaskEventListener {
        /**
         * Called when the task has begun its execution.
         *
         * @param currentThread The thread on which the task is being executed on.
         */
        fun onDownloadStarted(currentThread: Thread)

        /**
         * Called when a part has been downloaded successfully.
         */
        fun onDownloadComplete()

        /**
         * Called when the progress of a part has updated.
         *
         * @param thread Thread or part number whose download progress has updated.
         * @param progress Decimal between 0 and 1 indicating the progress of the download on
         *                 the <code>thread</code>.
         */
        fun onProgressUpdated(thread: Int, progress: Float)
    }
}