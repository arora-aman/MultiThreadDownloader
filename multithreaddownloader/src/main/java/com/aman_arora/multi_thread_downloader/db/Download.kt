package com.aman_arora.multi_thread_downloader.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity data class Download constructor(
        @PrimaryKey var id: Long = -1, var webUrl: String = "", var file: String = "", var threadCount: Int = -1
) {
    override fun toString(): String {
        return "Download(id=$id, webUrl='$webUrl', file='$file', threadCount=$threadCount)"
    }
}