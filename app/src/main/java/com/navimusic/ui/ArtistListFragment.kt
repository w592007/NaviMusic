package com.navimusic.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.navimusic.R
import com.navimusic.databinding.FragmentListBinding
import com.navimusic.ui.adapter.MusicItem
import com.navimusic.ui.adapter.MusicListAdapter
import com.navimusic.viewmodel.PlayerViewModel

class ArtistListFragment : Fragment() {
    private var _b: FragmentListBinding? = null
    private val b get() = _b!!
    private val vm: PlayerViewModel by activityViewModels()
    private val adapter = MusicListAdapter { item, _ ->
        findNavController().navigate(
            R.id.action_library_to_albums,
            Bundle().apply {
                putString("artistId", item.id)
                putString("artistName", item.title)
            }
        )
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentListBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.recyclerView.layoutManager = LinearLayoutManager(context)
        b.recyclerView.adapter = adapter
        vm.loading.observe(viewLifecycleOwner) { b.progressBar.visibility = if (it) View.VISIBLE else View.GONE }
        vm.artists.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list.map {
                MusicItem(it.id, it.name, "${it.albumCount} 张专辑", coverUrl = vm.getCoverUrl(it.coverArt))
            })
            b.emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            b.emptyText.text = "暂无歌手"
        }
        if (vm.artists.value.isNullOrEmpty()) vm.loadArtists()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
