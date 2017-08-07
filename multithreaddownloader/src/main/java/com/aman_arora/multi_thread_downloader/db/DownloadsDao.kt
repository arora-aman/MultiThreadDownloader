package com.aman_arora.multi_thread_downloader.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao interface DownloadsDao {
    @Query("SELECT * FROM download")
    fun getDownloads(): List<Download>

    @Query("SELECT * FROM download WHERE id = :arg0 LIMIT 1")
    fun getDownloadDetails(id: Int): Download

    @Insert
    fun addDownload(download: Download)
}