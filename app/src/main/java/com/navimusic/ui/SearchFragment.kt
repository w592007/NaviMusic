package com.navimusic.ui

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.navimusic.databinding.FragmentListBinding
import com.navimusic.ui.adapter.MusicItem
import com.navimusic.ui.adapter.MusicListAdapter
import com.navimusic.viewmodel.PlayerViewModel

class SearchFragment : Fragment() {
    private var _b: FragmentListBinding? = null
    private val b get() = _b!!
    private val vm: PlayerViewModel by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentListBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = MusicListAdapter { item, pos ->
            // 从搜索歌曲点播
            val songs = vm.searchSongs.value ?: return@MusicListAdapter
            val idx = songs.indexOfFirst { it.id == item.id }
            if (idx >= 0) vm.playQueue(songs, idx)
        }
        b.recyclerView.layoutManager = LinearLayoutManager(context)
        b.recyclerView.adapter = adapter

        // SearchView 放在 emptyText 位置上方（复用 fragment_list）
        val searchView = SearchView(requireContext()).apply {
            queryHint = "搜索歌曲、专辑、歌手…"
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(q: String?): Boolean {
                    q?.takeIf { it.isNotBlank() }?.let { vm.search(it) }
                    return true
                }
                override fun onQueryTextChange(q: String?) = false
            })
        }
        (b.root as ViewGroup).addView(searchView, 0, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        vm.loading.observe(viewLifecycleOwner) { b.progressBar.visibility = if (it) View.VISIBLE else View.GONE }
        vm.searchSongs.observe(viewLifecycleOwner) { songs ->
            val items = mutableListOf<MusicItem>()
            vm.searchArtists.value?.forEach { a ->
                items.add(MusicItem(a.id, a.name, "歌手 · ${a.albumCount}张专辑", coverUrl = vm.getCoverUrl(a.coverArt)))
            }
            vm.searchAlbums.value?.forEach { al ->
                items.add(MusicItem(al.id, al.name, "专辑 · ${al.artist}", coverUrl = vm.getCoverUrl(al.coverArt)))
            }
            songs.forEach { s ->
                items.add(MusicItem(s.id, s.title, "歌曲 · ${s.artist}", coverUrl = vm.getCoverUrl(s.coverArt)))
            }
            adapter.submitList(items)
            b.emptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            b.emptyText.text = "无搜索结果"
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
