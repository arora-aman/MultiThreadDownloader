package com.aman_arora.multi_threaded_downloader.view

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.aman_arora.multi_threaded_downloader.R

class DownloaderRow(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val name = itemView.findViewById(R.id.downloadName) as TextView
    val pause = itemView.findViewById(R.id.pause) as ImageButton

    private val bars = mutableListOf<ProgressBar>()
    private val progressBars = itemView.findViewById(R.id.progressBars) as LinearLayout

    fun setProgressBars(threads: Int) {
        progressBars.removeAllViews()
        for (i in 1..threads) {
            val bar = ProgressBar(itemView.context, null, android.R.attr.progressBarStyleHorizontal)
            progressBars.addView(bar)
            bars.add(bar)
        }
        progressBars.layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    fun setProgress(thread: Int, progress: Int?) {
        if (progress != null) bars[thread - 1].progress = progress
    }
}
