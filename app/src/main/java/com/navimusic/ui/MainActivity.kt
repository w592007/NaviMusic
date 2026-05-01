package com.navimusic.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.navimusic.R
import com.navimusic.databinding.ActivityMainBinding
import com.navimusic.util.PrefsManager
import com.navimusic.viewmodel.PlayerViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val vm: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // 导航配置
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        binding.bottomNav.setupWithNavController(navController)

        // 未配置则跳转设置
        if (!PrefsManager(this).isConfigured()) {
            startActivity(Intent(this, SettingsActivity::class.java))
        } else {
            vm.initController()
        }

        // 迷你播放条
        vm.currentSong.observe(this) { song ->
            if (song != null) {
                binding.miniPlayerBar.visibility = View.VISIBLE
                binding.miniTitle.text = song.title
                binding.miniArtist.text = song.artist
                Glide.with(this)
                    .load(vm.getCoverUrl(song.coverArt))
                    .placeholder(R.drawable.ic_music_placeholder)
                    .into(binding.miniCover)
            } else {
                binding.miniPlayerBar.visibility = View.GONE
            }
        }

        vm.isPlaying.observe(this) { playing ->
            binding.miniPlayPause.setImageResource(
                if (playing) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play
            )
        }

        binding.miniPlayPause.setOnClickListener { vm.togglePlayPause() }
        binding.miniNext.setOnClickListener { vm.skipNext() }

        // 点击迷你条打开播放器页
        binding.miniPlayerBar.setOnClickListener {
            navController.navigate(R.id.playerFragment)
        }

        // Toolbar 设置按钮
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_settings) {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            } else false
        }
    }
}
