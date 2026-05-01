package com.navimusic.ui

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.bumptech.glide.Glide
import com.navimusic.R
import com.navimusic.databinding.FragmentPlayerBinding
import com.navimusic.ui.adapter.LyricsAdapter
import com.navimusic.viewmodel.PlayerViewModel
import java.util.concurrent.TimeUnit

class PlayerFragment : Fragment() {
    private var _b: FragmentPlayerBinding? = null
    private val b get() = _b!!
    private val vm: PlayerViewModel by activityViewModels()
    private val lyricsAdapter = LyricsAdapter()
    private var dragging = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentPlayerBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 歌词 RecyclerView
        val layoutManager = LinearLayoutManager(context)
        b.lyricsRecycler.layoutManager = layoutManager
        b.lyricsRecycler.adapter = lyricsAdapter

        // 当前歌曲
        vm.currentSong.observe(viewLifecycleOwner) { song ->
            if (song == null) return@observe
            b.playerTitle.text = song.title
            b.playerArtist.text = song.artist
            b.playerTotalTime.text = formatDuration(song.duration)
            b.playerSeekbar.max = song.duration * 1000
            Glide.with(this)
                .load(vm.getCoverUrl(song.coverArt))
                .placeholder(R.drawable.ic_music_placeholder)
                .into(b.playerCover)
        }

        // 播放状态
        vm.isPlaying.observe(viewLifecycleOwner) { playing ->
            b.btnPlayPause.setImageResource(
                if (playing) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play
            )
        }

        // 进度
        vm.progress.observe(viewLifecycleOwner) { ms ->
            if (!dragging) {
                b.playerSeekbar.progress = ms.toInt()
                b.playerCurrentTime.text = formatDuration((ms / 1000).toInt())
            }
        }

        // 歌词
        vm.lyrics.observe(viewLifecycleOwner) { lines ->
            lyricsAdapter.setLyrics(lines)
            if (lines.isEmpty()) {
                b.lyricsLabel.text = "暂无歌词"
            } else {
                b.lyricsLabel.text = "歌词"
            }
        }

        // 高亮行自动滚动
        vm.currentLyricIndex.observe(viewLifecycleOwner) { index ->
            lyricsAdapter.setHighlight(index)
            if (index >= 0) smoothScrollTo(index)
        }

        // 控制按钮
        b.btnPlayPause.setOnClickListener { vm.togglePlayPause() }
        b.btnNext.setOnClickListener { vm.skipNext() }
        b.btnPrevious.setOnClickListener { vm.skipPrevious() }

        // SeekBar 拖动
        b.playerSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(sb: SeekBar) { dragging = true }
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) b.playerCurrentTime.text = formatDuration(progress / 1000)
            }
            override fun onStopTrackingTouch(sb: SeekBar) {
                dragging = false
                vm.seekTo(sb.progress.toLong())
            }
        })
    }

    private fun smoothScrollTo(index: Int) {
        val lm = b.lyricsRecycler.layoutManager as? LinearLayoutManager ?: return
        val scroller = object : LinearSmoothScroller(requireContext()) {
            override fun getVerticalSnapPreference() = SNAP_TO_ANY
        }
        scroller.targetPosition = index
        lm.startSmoothScroll(scroller)
    }

    private fun formatDuration(seconds: Int): String {
        val m = TimeUnit.SECONDS.toMinutes(seconds.toLong())
        val s = seconds - m * 60
        return "%d:%02d".format(m, s)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
