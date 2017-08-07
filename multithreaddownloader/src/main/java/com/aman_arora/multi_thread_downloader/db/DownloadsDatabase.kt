package com.aman_arora.multi_thread_downloader.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = arrayOf(Download::class), version = 1)
abstract class DownloadsDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadsDao
}