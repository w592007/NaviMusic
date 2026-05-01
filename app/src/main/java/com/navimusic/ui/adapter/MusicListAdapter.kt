package com.navimusic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.navimusic.databinding.ItemMusicBinding

data class MusicItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val extra: String = "",
    val coverUrl: String? = null
)

class MusicListAdapter(
    private val onClick: (MusicItem, Int) -> Unit
) : ListAdapter<MusicItem, MusicListAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<MusicItem>() {
            override fun areItemsTheSame(a: MusicItem, b: MusicItem) = a.id == b.id
            override fun areContentsTheSame(a: MusicItem, b: MusicItem) = a == b
        }
    }

    inner class VH(val binding: ItemMusicBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            itemTitle.text = item.title
            itemSubtitle.text = item.subtitle
            itemExtra.text = item.extra
            Glide.with(itemCover)
                .load(item.coverUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(itemCover)
            root.setOnClickListener { onClick(item, position) }
        }
    }
}
