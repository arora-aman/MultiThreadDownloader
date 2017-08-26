package com.aman_arora.multi_threaded_downloader

import android.arch.lifecycle.LifecycleActivity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.aman_arora.multi_threaded_downloader.model.Download
import com.aman_arora.multi_threaded_downloader.repository.DownloadsRepository
import com.aman_arora.multi_threaded_downloader.viewmodel.DownloadsViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : LifecycleActivity() {

    var downloadsViewModel: DownloadsViewModel? = null
    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)

        val inited = DownloadsRepository().init(this)

        inited.observe(this, Observer<Boolean> { value ->
            if (value as Boolean) {
                loading.visibility = View.GONE
            }
        })

        val downloadUrlList = listOf(
                "https://farm5.staticflickr.com/4179/33980954480_03567cffb8_o_d.jpg",
                "https://farm5.staticflickr.com/4251/34596594454_0d3b1f13c0_o_d.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/f/ff/Pizigani_1367_Chart_10MB.jpg",
                "http://ipv4.download.thinkbroadband.com:8080/50MB.zip"
        )

        val adapter = DownloadsAdapter(ArrayList())
        downloads_list.adapter = adapter

        add_download.setOnClickListener {
            if(count >3) return@setOnClickListener
            adapter.addDownload(downloadsViewModel!!.download(downloadUrlList[count]))
            count++
        }

    }

    inner class DownloadsAdapter(val mDownloads: ArrayList<Download>): BaseAdapter() {
        override fun getView(position: Int, v: View?, parent: ViewGroup?): View {
            val viewHolder: DownloadViewHolder?
            var view = v
            if (view == null) {
                view = LayoutInflater.from(parent?.context).inflate(R.layout.download_item, null)
                viewHolder = DownloadViewHolder(view)
                view.tag = viewHolder
            } else {
                viewHolder = view.tag as DownloadViewHolder?
            }

            viewHolder!!.downloadName.text = mDownloads[position].webAddress

            mDownloads[position].totalProgress.observe(this@MainActivity, Observer<Float> {progress ->
                if (progress != null) {
                    viewHolder.progressBar.progress = ((progress * 100).toInt())
                }
            })

            return view as View
        }

        override fun getItem(position: Int): Any {
            return mDownloads[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return mDownloads.size
        }

        fun addDownload(download: Download) {
            mDownloads.add(download)
            notifyDataSetChanged()
        }

        private inner class DownloadViewHolder(view: View) {
            val downloadName: TextView  = view.findViewById(R.id.download_name)
            val pause: ImageView = view.findViewById(R.id.pause_control)
            val progressBar: ProgressBar = view.findViewById(R.id.download_progress)
        }
    }

}
