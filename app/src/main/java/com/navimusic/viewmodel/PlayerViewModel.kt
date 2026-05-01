package com.navimusic.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.navimusic.model.*
import com.navimusic.repository.SubsonicRepository
import com.navimusic.service.MusicPlayerService
import com.navimusic.util.LrcParser
import kotlinx.coroutines.launch
import android.content.ComponentName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SubsonicRepository(app)

    // ─── 音乐库数据 ─────────────────────────────────────────────────────────
    private val _artists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>> = _artists

    private val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> = _albums

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    // ─── 搜索 ───────────────────────────────────────────────────────────────
    private val _searchArtists = MutableLiveData<List<Artist>>()
    val searchArtists: LiveData<List<Artist>> = _searchArtists
    private val _searchAlbums = MutableLiveData<List<Album>>()
    val searchAlbums: LiveData<List<Album>> = _searchAlbums
    private val _searchSongs = MutableLiveData<List<Song>>()
    val searchSongs: LiveData<List<Song>> = _searchSongs

    // ─── 播放器状态 ─────────────────────────────────────────────────────────
    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _progress = MutableLiveData<Long>(0L)
    val progress: LiveData<Long> = _progress

    private val _queue = MutableLiveData<List<Song>>(emptyList())
    val queue: LiveData<List<Song>> = _queue

    // ─── 歌词 ───────────────────────────────────────────────────────────────
    private val _lyrics = MutableLiveData<List<LyricLine>>(emptyList())
    val lyrics: LiveData<List<LyricLine>> = _lyrics

    private val _currentLyricIndex = MutableLiveData<Int>(-1)
    val currentLyricIndex: LiveData<Int> = _currentLyricIndex

    // ─── MediaController ────────────────────────────────────────────────────
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun initController() {
        val token = SessionToken(
            getApplication(),
            ComponentName(getApplication(), MusicPlayerService::class.java)
        )
        controllerFuture = MediaController.Builder(getApplication(), token).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            startProgressPolling()
        }, { cmd -> cmd.run() })
    }

    /** 进度轮询（每500ms刷新一次） */
    private fun startProgressPolling() {
        viewModelScope.launch {
            while (true) {
                delay(500)
                val ctrl = controller ?: continue
                _isPlaying.postValue(ctrl.isPlaying)
                _progress.postValue(ctrl.currentPosition)
                val idx = LrcParser.getCurrentIndex(_lyrics.value ?: emptyList(), ctrl.currentPosition)
                _currentLyricIndex.postValue(idx)
            }
        }
    }

    // ─── 加载数据 ───────────────────────────────────────────────────────────

    fun loadArtists() = viewModelScope.launch {
        _loading.value = true
        try { _artists.value = repo.getArtists() }
        catch (e: Exception) { _error.value = e.message }
        finally { _loading.value = false }
    }

    fun loadAlbums() = viewModelScope.launch {
        _loading.value = true
        try { _albums.value = repo.getAlbums() }
        catch (e: Exception) { _error.value = e.message }
        finally { _loading.value = false }
    }

    fun loadArtistAlbums(artistId: String) = viewModelScope.launch {
        _loading.value = true
        try { _albums.value = repo.getArtistAlbums(artistId) }
        catch (e: Exception) { _error.value = e.message }
        finally { _loading.value = false }
    }

    fun loadAlbumSongs(albumId: String) = viewModelScope.launch {
        _loading.value = true
        try { _songs.value = repo.getAlbumSongs(albumId) }
        catch (e: Exception) { _error.value = e.message }
        finally { _loading.value = false }
    }

    fun loadPlaylists() = viewModelScope.launch {
        _loading.value = true
        try { _playlists.value = repo.getPlaylists() }
        catch (e: Exception) { _error.value = e.message }
        finally { _loading.value = false }
    }

    fun loadPlaylistSongs(playlistId: String) = viewModelScope.launch {
        _loading.value = true
        try { _songs.value = repo.getPlaylistSongs(playlistId) }
        catch (e: Exception) { _error.value = e.message }
        finally { _loading.value = false }
    }

    fun search(query: String) = viewModelScope.launch {
        _loading.value = true
        try {
            val (a, al, s) = repo.search(query)
            _searchArtists.value = a
            _searchAlbums.value = al
            _searchSongs.value = s
        } catch (e: Exception) { _error.value = e.message }
        finally { _loading.value = false }
    }

    // ─── 播放控制 ────────────────────────────────────────────────────────────

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        _queue.value = songs
        val ctrl = controller ?: return
        val items = songs.map { song ->
            MediaItem.Builder()
                .setUri(repo.getStreamUrl(song.id))
                .setMediaId(song.id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(
                            repo.getCoverArtUrl(song.coverArt)?.let {
                                android.net.Uri.parse(it)
                            }
                        )
                        .build()
                )
                .build()
        }
        ctrl.setMediaItems(items, startIndex, 0L)
        ctrl.prepare()
        ctrl.play()
        _currentSong.value = songs[startIndex]
        loadLyrics(songs[startIndex])
    }

    fun togglePlayPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }

    fun skipNext() {
        val ctrl = controller ?: return
        if (ctrl.hasNextMediaItem()) {
            ctrl.seekToNextMediaItem()
            updateCurrentSong()
        }
    }

    fun skipPrevious() {
        val ctrl = controller ?: return
        ctrl.seekToPreviousMediaItem()
        updateCurrentSong()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    private fun updateCurrentSong() {
        val ctrl = controller ?: return
        val idx = ctrl.currentMediaItemIndex
        val queue = _queue.value ?: return
        if (idx in queue.indices) {
            _currentSong.value = queue[idx]
            loadLyrics(queue[idx])
        }
    }

    private fun loadLyrics(song: Song) {
        viewModelScope.launch {
            val raw = repo.getLyrics(song.artist, song.title)
            if (!raw.isNullOrBlank()) {
                val parsed = LrcParser.parse(raw)
                _lyrics.postValue(parsed)
            } else {
                _lyrics.postValue(emptyList())
            }
        }
    }

    fun getCoverUrl(coverArt: String?) = repo.getCoverArtUrl(coverArt)

    override fun onCleared() {
        MediaController.releaseFuture(controllerFuture ?: return)
        super.onCleared()
    }
}
