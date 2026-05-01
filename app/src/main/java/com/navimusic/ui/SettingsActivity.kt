package com.navimusic.ui

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.navimusic.R
import com.navimusic.databinding.ActivitySettingsBinding
import com.navimusic.repository.SubsonicRepository
import com.navimusic.util.PrefsManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.apply {
            title = getString(R.string.settings)
            setDisplayHomeAsUpEnabled(true)
        }

        prefs = PrefsManager(this)

        // 回填已保存的配置
        binding.etLanUrl.setText(prefs.lanUrl)
        binding.etWanUrl.setText(prefs.wanUrl)
        binding.etUsername.setText(prefs.username)
        binding.etPassword.setText(prefs.password)

        // 显示当前网络类型
        updateNetworkStatus()

        binding.btnSave.setOnClickListener { save() }
        binding.btnTest.setOnClickListener { testConnection() }
    }

    private fun save() {
        val lan = binding.etLanUrl.text?.toString()?.trim() ?: ""
        val wan = binding.etWanUrl.text?.toString()?.trim() ?: ""
        val user = binding.etUsername.text?.toString()?.trim() ?: ""
        val pass = binding.etPassword.text?.toString() ?: ""

        lifecycleScope.launch {
            prefs.save(lan, wan, user, pass)
            binding.tvResult.visibility = View.VISIBLE
            binding.tvResult.text = "✓ 设置已保存"
            binding.tvResult.setTextColor(getColor(R.color.colorPrimary))
        }
    }

    private fun testConnection() {
        val lan = binding.etLanUrl.text?.toString()?.trim() ?: ""
        val wan = binding.etWanUrl.text?.toString()?.trim() ?: ""
        val user = binding.etUsername.text?.toString()?.trim() ?: ""
        val pass = binding.etPassword.text?.toString() ?: ""

        binding.tvResult.visibility = View.VISIBLE
        binding.tvResult.text = "连接测试中…"
        binding.tvResult.setTextColor(getColor(R.color.colorSecondaryText))

        lifecycleScope.launch {
            // 先保存再测试
            prefs.save(lan, wan, user, pass)
            val repo = SubsonicRepository(this@SettingsActivity)
            val ok = repo.ping()
            binding.tvResult.text = if (ok) getString(R.string.connection_ok) else getString(R.string.connection_fail)
            binding.tvResult.setTextColor(
                getColor(if (ok) R.color.colorPrimary else android.R.color.holo_red_light)
            )
            if (ok) updateNetworkStatus()
        }
    }

    private fun updateNetworkStatus() {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        binding.tvNetworkStatus.text = if (isWifi) "Wi-Fi（将尝试内网地址）" else "移动数据（使用外网地址）"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
