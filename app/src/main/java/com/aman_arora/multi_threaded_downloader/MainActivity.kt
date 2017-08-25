package com.aman_arora.multi_threaded_downloader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.aman_arora.multi_threaded_downloader.repository.DownloadsRepository

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DownloadsRepository().init(this)
    }
}
