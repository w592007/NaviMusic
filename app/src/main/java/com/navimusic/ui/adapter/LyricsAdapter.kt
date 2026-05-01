package com.navimusic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.navimusic.R
import com.navimusic.model.LyricLine

class LyricsAdapter : RecyclerView.Adapter<LyricsAdapter.VH>() {

    private var lines: List<LyricLine> = emptyList()
    private var highlightIndex: Int = -1

    fun setLyrics(newLines: List<LyricLine>) {
        lines = newLines
        notifyDataSetChanged()
    }

    fun setHighlight(index: Int) {
        if (index == highlightIndex) return
        val old = highlightIndex
        highlightIndex = index
        if (old >= 0) notifyItemChanged(old)
        if (index >= 0) notifyItemChanged(index)
    }

    inner class VH(val tv: TextView) : RecyclerView.ViewHolder(tv)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lyric, parent, false) as TextView
        return VH(tv)
    }

    override fun getItemCount() = lines.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tv.text = lines[position].text
        val colorRes = if (position == highlightIndex) R.color.colorLyricHighlight else R.color.colorLyricNormal
        holder.tv.setTextColor(ContextCompat.getColor(holder.tv.context, colorRes))
        val scale = if (position == highlightIndex) 1.08f else 1.0f
        holder.tv.scaleX = scale
        holder.tv.scaleY = scale
    }
}
