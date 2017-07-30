package com.aman_arora.multi_threaded_downloader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ProgressBar
import com.aman_arora.multi_thread_downloader.downloader.IMultiThreadDownloader
import com.aman_arora.multi_thread_downloader.downloader.MultiThreadDownloader
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.download_item.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    val downloader = MultiThreadDownloader
    var id : Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloader.init(this)

        addDownload.setOnClickListener {
            val progressBarList = mutableListOf<ProgressBar>()
            val params = progressBars.layoutParams
            for(i in 1..5) {
                val bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
//                bar.layoutParams = LinearLayout.LayoutParams(params.width/5, params.height)
                progressBars.addView(bar)
                progressBarList.add(bar)
            }

//            setContentView(layout)
            id = startDownload("https://farm5.staticflickr.com/4179/33980954480_03567cffb8_o_d.jpg", progressBarList)
        }
        pause.setOnClickListener {
            Log.d("PAUSING" , "${getDownloadId()}")
            val state = downloader.getDownloadInfo(getDownloadId()).getState()
            if (state == IMultiThreadDownloader.DownloadState.PAUSED) {
                downloader.resume(getDownloadId())
                pause.setImageDrawable(getDrawable(android.R.drawable.ic_media_pause))
            } else if (state == IMultiThreadDownloader.DownloadState.DOWNLOADING) {
                downloader.pause(getDownloadId())
                pause.setImageDrawable(getDrawable(android.R.drawable.ic_media_play))
            }
        }
    }

    fun startDownload(url: String, progressBarList: List<ProgressBar>) : Long {
        var id: Long = -1
        Executors.newSingleThreadExecutor().execute {
            id = downloader.download(url, 5, null, object : IMultiThreadDownloader.OnDownloadEventListener {
                override fun onDownloadStateChanged(id: Long, state: IMultiThreadDownloader.DownloadState) {
                    Log.d("Tag", state.name)
                }

                override fun onDownloadProgressChanged(id: Long, thread: Int, progress: Float) {
                    progressBarList[thread - 1].progress = (progress * 100).toInt()
//                    Log.d("Tag", "thread: $thread " + (progress * 100))
                }
            })
        }

        return id
    }

    fun getDownloadId(): Long { return id }
}
