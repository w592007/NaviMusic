package com.navimusic.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.navimusic.util.PrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.codec.digest.DigestUtils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

/**
 * 内外网自动切换管理器
 * - 连接Wi-Fi且内网地址可达 → 使用内网地址
 * - 其他情况 → 使用外网地址
 */
class NetworkManager(private val context: Context) {

    private val prefs = PrefsManager(context)

    // 当前使用的 API 实例（懒加载，地址变化时重建）
    private var currentBaseUrl: String = ""
    private var _api: SubsonicApi? = null

    suspend fun getApi(): SubsonicApi {
        val targetUrl = resolveBaseUrl()
        if (targetUrl != currentBaseUrl || _api == null) {
            currentBaseUrl = targetUrl
            _api = buildApi(targetUrl)
        }
        return _api!!
    }

    /** 决定使用内网还是外网地址 */
    suspend fun resolveBaseUrl(): String = withContext(Dispatchers.IO) {
        val lanUrl = prefs.lanUrl
        val wanUrl = prefs.wanUrl

        if (lanUrl.isNotBlank() && isOnWifi() && isReachable(lanUrl)) {
            lanUrl.trimEnd('/') + "/"
        } else if (wanUrl.isNotBlank()) {
            wanUrl.trimEnd('/') + "/"
        } else if (lanUrl.isNotBlank()) {
            lanUrl.trimEnd('/') + "/"
        } else {
            ""
        }
    }

    /** 检查是否连接 Wi-Fi */
    private fun isOnWifi(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /** 尝试 TCP 连通性检测（500ms 超时） */
    private fun isReachable(url: String): Boolean {
        return try {
            val uri = java.net.URI(url)
            val host = uri.host ?: return false
            val port = if (uri.port > 0) uri.port else if (uri.scheme == "https") 443 else 80
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 500)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun buildApi(baseUrl: String): SubsonicApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl.ifBlank { "http://localhost/" })
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SubsonicApi::class.java)
    }

    /** 生成 Subsonic token 认证参数 */
    fun authParams(): Triple<String, String, String> {
        val username = prefs.username
        val password = prefs.password
        val salt = generateSalt()
        val token = DigestUtils.md5Hex(password + salt)
        return Triple(username, token, salt)
    }

    private fun generateSalt(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return (1..10).map { chars.random() }.joinToString("")
    }

    /** 构建封面图 URL */
    fun getCoverArtUrl(coverArt: String?): String? {
        if (coverArt.isNullOrBlank()) return null
        val (u, t, s) = authParams()
        return "${currentBaseUrl}rest/getCoverArt?id=$coverArt&u=$u&t=$t&s=$s&v=1.16.1&c=NaviMusic&size=300"
    }

    /** 构建流媒体 URL */
    fun getStreamUrl(songId: String): String {
        val (u, t, s) = authParams()
        return "${currentBaseUrl}rest/stream?id=$songId&u=$u&t=$t&s=$s&v=1.16.1&c=NaviMusic&format=raw"
    }
}
