package com.navimusic.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.navimusic.databinding.FragmentListBinding
import com.navimusic.ui.adapter.MusicItem
import com.navimusic.ui.adapter.MusicListAdapter
import com.navimusic.viewmodel.PlayerViewModel
import java.util.concurrent.TimeUnit

class SongListFragment : Fragment() {
    private var _b: FragmentListBinding? = null
    private val b get() = _b!!
    private val vm: PlayerViewModel by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentListBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val albumId = arguments?.getString("albumId") ?: ""
        val playlistId = arguments?.getString("playlistId") ?: ""

        val adapter = MusicListAdapter { _, pos ->
            val songs = vm.songs.value ?: return@MusicListAdapter
            vm.playQueue(songs, pos)
        }

        b.recyclerView.layoutManager = LinearLayoutManager(context)
        b.recyclerView.adapter = adapter
        vm.loading.observe(viewLifecycleOwner) { b.progressBar.visibility = if (it) View.VISIBLE else View.GONE }
        vm.songs.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list.mapIndexed { idx, song ->
                MusicItem(
                    song.id,
                    song.title,
                    song.artist,
                    formatDuration(song.duration),
                    vm.getCoverUrl(song.coverArt)
                )
            })
            b.emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            b.emptyText.text = "暂无歌曲"
        }

        when {
            albumId.isNotBlank() -> vm.loadAlbumSongs(albumId)
            playlistId.isNotBlank() -> vm.loadPlaylistSongs(playlistId)
        }
    }

    private fun formatDuration(seconds: Int): String {
        val m = TimeUnit.SECONDS.toMinutes(seconds.toLong())
        val s = seconds - m * 60
        return "%d:%02d".format(m, s)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
