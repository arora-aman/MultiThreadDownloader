package com.aman_arora.multi_thread_downloader.downloader

internal interface IDownloadTask: Runnable{

    interface OnDownloadTaskEventListener {
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