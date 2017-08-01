package com.aman_arora.multi_threaded_downloader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.aman_arora.multi_thread_downloader.downloader.IMultiThreadDownloader
import com.aman_arora.multi_thread_downloader.downloader.MultiThreadDownloader
import com.aman_arora.multi_threaded_downloader.model.Download
import com.aman_arora.multi_threaded_downloader.view.DownloadListAdapter
import com.aman_arora.multi_threaded_downloader.view.DownloaderRow
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val downloader = MultiThreadDownloader
    val downloads = ArrayList<Download>()

    var adapter: DownloadListAdapter? = null

    var downloadCount = 1

    val downloadUrlList = listOf(
            "https://farm5.staticflickr.com/4179/33980954480_03567cffb8_o_d.jpg",
            "https://farm5.staticflickr.com/4251/34596594454_0d3b1f13c0_o_d.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/f/ff/Pizigani_1367_Chart_10MB.jpg",
            "http://ipv4.download.thinkbroadband.com:8080/50MB.zip"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloader.init(this)

        adapter = DownloadListAdapter(this, downloader, downloads)
        adapter?.setHasStableIds(true)
        downloadList.layoutManager = LinearLayoutManager(this)
        downloadList.adapter = adapter

        addDownload.setOnClickListener { startDownload(downloadUrlList[downloadCount - 1], downloadCount) }
    }

    fun startDownload(url: String, threads: Int): Long {
        return downloader.download(url, threads, null, object : IMultiThreadDownloader.OnDownloadEventListener {
            override fun onDownloadStateChanged(id: Long, state: IMultiThreadDownloader.DownloadState) {
                Log.d("Tag", state.name)

                if (state == IMultiThreadDownloader.DownloadState.STARTING) {
                    runOnUiThread {
                        val download = Download(threads)
                        download.downloadId = id
                        download.downloadUrl = url
                        downloads.add(download)
                        downloadCount++
                        adapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onDownloadProgressChanged(id: Long, thread: Int, progress: Float) {
//                Log.d("TG", "$thread thread of id $id has ${progress * 100} progress")
//                if (downloadList.findViewHolderForItemId(id)  == null) Log.d("TAG", "123r12432")
                val row = downloadList.findViewHolderForItemId(id) as DownloaderRow?
                runOnUiThread { row?.setProgress(thread, (progress * 100).toInt()) }
            }
        })
    }
}
