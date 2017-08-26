package com.aman_arora.multi_threaded_downloader

import android.arch.lifecycle.LifecycleActivity
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.View
import com.aman_arora.multi_threaded_downloader.repository.DownloadsRepository
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : LifecycleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inited = DownloadsRepository().init(this)

        inited.observe(this, Observer<Boolean> { value ->
            if (value as Boolean) {
                loading.visibility = View.GONE
            }
        })
    }
}
