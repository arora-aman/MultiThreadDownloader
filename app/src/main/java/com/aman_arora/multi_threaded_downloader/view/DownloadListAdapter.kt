package com.aman_arora.multi_threaded_downloader.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aman_arora.multi_thread_downloader.downloader.IMultiThreadDownloader
import com.aman_arora.multi_thread_downloader.downloader.MultiThreadDownloader
import com.aman_arora.multi_threaded_downloader.R
import com.aman_arora.multi_threaded_downloader.model.Download

class DownloadListAdapter(val context: Context,
                          val downloader: MultiThreadDownloader, val downloads: ArrayList<Download>) : RecyclerView.Adapter<DownloaderRow>() {

    override fun onBindViewHolder(holder: DownloaderRow?, position: Int) {
        val state = getDownloadState(position)

        holder?.name?.text = downloads[position].downloadUrl

        if (state == IMultiThreadDownloader.DownloadState.STARTING || state == IMultiThreadDownloader.DownloadState.DOWNLOADING) {
            holder?.pause?.setImageDrawable(context.getDrawable(android.R.drawable.ic_media_pause))
            holder?.pause?.setOnClickListener { downloader.pause(downloads[position].downloadId) }
        } else if (state == IMultiThreadDownloader.DownloadState.PAUSED) {
            holder?.pause?.setImageDrawable(context.getDrawable(android.R.drawable.ic_media_play))
            holder?.pause?.setOnClickListener { downloader.resume(downloads[position].downloadId) }
        } else {
            holder?.pause?.visibility == View.GONE
        }

        holder?.setProgressBars(downloads[position].threadCount)

        if (holder?.itemId == null)Log.d("TAG", "'234")
        Log.d("TAG", "${holder?.itemId} ${downloads[position].downloadId}")
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DownloaderRow {
        return DownloaderRow(LayoutInflater.from(parent?.context).inflate(R.layout.download_item, null))
    }

    override fun getItemCount(): Int {
        return downloads.size
    }

    override fun getItemId(position: Int): Long {
        Log.d("TAG45", "${downloads[position].downloadId}")
        return downloads[position].downloadId
    }

    private fun getDownloadState(position: Int): IMultiThreadDownloader.DownloadState? {
        return downloader.getDownloadInfo(downloads[position].downloadId).getState()
    }
}