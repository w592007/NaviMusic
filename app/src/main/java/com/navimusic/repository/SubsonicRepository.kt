package com.navimusic.repository

import android.content.Context
import com.navimusic.api.NetworkManager
import com.navimusic.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST")
class SubsonicRepository(private val context: Context) {

    private val network = NetworkManager(context)

    // ─── 辅助：解包 subsonic-response ───────────────────────────────────────
    private fun root(map: Map<String, Any>): Map<String, Any>? =
        map["subsonic-response"] as? Map<String, Any>

    private fun auth() = network.authParams()

    // ─── Artist ─────────────────────────────────────────────────────────────

    suspend fun getArtists(): List<Artist> = withContext(Dispatchers.IO) {
        val api = network.getApi()
        val (u, t, s) = auth()
        val resp = api.getArtists(u, t, s).body() ?: return@withContext emptyList()
        val root = root(resp) ?: return@withContext emptyList()
        val artists = root["artists"] as? Map<String, Any> ?: return@withContext emptyList()
        val indices = artists["index"] as? List<Map<String, Any>> ?: return@withContext emptyList()
        val result = mutableListOf<Artist>()
        for (index in indices) {
            val list = index["artist"] as? List<Map<String, Any>> ?: continue
            for (a in list) {
                result.add(Artist(
                    id = a["id"] as? String ?: continue,
                    name = a["name"] as? String ?: "",
                    albumCount = (a["albumCount"] as? Double)?.toInt() ?: 0,
                    coverArt = a["coverArt"] as? String
                ))
            }
        }
        result
    }

    suspend fun getArtistAlbums(artistId: String): List<Album> = withContext(Dispatchers.IO) {
        val api = network.getApi()
        val (u, t, s) = auth()
        val resp = api.getArtist(artistId, u, t, s).body() ?: return@withContext emptyList()
        val root = root(resp) ?: return@withContext emptyList()
        val artist = root["artist"] as? Map<String, Any> ?: return@withContext emptyList()
        val albums = artist["album"] as? List<Map<String, Any>> ?: return@withContext emptyList()
        albums.mapNotNull { parseAlbum(it) }
    }

    // ─── Album ───────────────────────────────────────────────────────────────

    suspend fun getAlbums(type: String = "alphabeticalByName", size: Int = 50, offset: Int = 0): List<Album> =
        withContext(Dispatchers.IO) {
            val api = network.getApi()
            val (u, t, s) = auth()
            val resp = api.getAlbumList2(type, size, offset, u, t, s).body()
                ?: return@withContext emptyList()
            val root = root(resp) ?: return@withContext emptyList()
            val albumList = root["albumList2"] as? Map<String, Any> ?: return@withContext emptyList()
            val albums = albumList["album"] as? List<Map<String, Any>> ?: return@withContext emptyList()
            albums.mapNotNull { parseAlbum(it) }
        }

    suspend fun getAlbumSongs(albumId: String): List<Song> = withContext(Dispatchers.IO) {
        val api = network.getApi()
        val (u, t, s) = auth()
        val resp = api.getAlbum(albumId, u, t, s).body() ?: return@withContext emptyList()
        val root = root(resp) ?: return@withContext emptyList()
        val album = root["album"] as? Map<String, Any> ?: return@withContext emptyList()
        val songs = album["song"] as? List<Map<String, Any>> ?: return@withContext emptyList()
        songs.mapNotNull { parseSong(it) }
    }

    // ─── Playlist ────────────────────────────────────────────────────────────

    suspend fun getPlaylists(): List<Playlist> = withContext(Dispatchers.IO) {
        val api = network.getApi()
        val (u, t, s) = auth()
        val resp = api.getPlaylists(u, t, s).body() ?: return@withContext emptyList()
        val root = root(resp) ?: return@withContext emptyList()
        val playlists = root["playlists"] as? Map<String, Any> ?: return@withContext emptyList()
        val list = playlists["playlist"] as? List<Map<String, Any>> ?: return@withContext emptyList()
        list.mapNotNull { p ->
            Playlist(
                id = p["id"] as? String ?: return@mapNotNull null,
                name = p["name"] as? String ?: "",
                songCount = (p["songCount"] as? Double)?.toInt() ?: 0,
                duration = (p["duration"] as? Double)?.toInt() ?: 0,
                coverArt = p["coverArt"] as? String,
                comment = p["comment"] as? String
            )
        }
    }

    suspend fun getPlaylistSongs(playlistId: String): List<Song> = withContext(Dispatchers.IO) {
        val api = network.getApi()
        val (u, t, s) = auth()
        val resp = api.getPlaylist(playlistId, u, t, s).body() ?: return@withContext emptyList()
        val root = root(resp) ?: return@withContext emptyList()
        val playlist = root["playlist"] as? Map<String, Any> ?: return@withContext emptyList()
        val entries = playlist["entry"] as? List<Map<String, Any>> ?: return@withContext emptyList()
        entries.mapNotNull { parseSong(it) }
    }

    // ─── Search ──────────────────────────────────────────────────────────────

    suspend fun search(query: String): Triple<List<Artist>, List<Album>, List<Song>> =
        withContext(Dispatchers.IO) {
            val api = network.getApi()
            val (u, t, s) = auth()
            val resp = api.search3(query, u, t, s).body()
                ?: return@withContext Triple(emptyList(), emptyList(), emptyList())
            val root = root(resp)
                ?: return@withContext Triple(emptyList(), emptyList(), emptyList())
            val result = root["searchResult3"] as? Map<String, Any>
                ?: return@withContext Triple(emptyList(), emptyList(), emptyList())

            val artists = (result["artist"] as? List<Map<String, Any>>)?.mapNotNull { a ->
                Artist(
                    id = a["id"] as? String ?: return@mapNotNull null,
                    name = a["name"] as? String ?: "",
                    albumCount = (a["albumCount"] as? Double)?.toInt() ?: 0,
                    coverArt = a["coverArt"] as? String
                )
            } ?: emptyList()

            val albums = (result["album"] as? List<Map<String, Any>>)?.mapNotNull { parseAlbum(it) } ?: emptyList()
            val songs = (result["song"] as? List<Map<String, Any>>)?.mapNotNull { parseSong(it) } ?: emptyList()

            Triple(artists, albums, songs)
        }

    // ─── Lyrics ──────────────────────────────────────────────────────────────

    suspend fun getLyrics(artist: String, title: String): String? = withContext(Dispatchers.IO) {
        val api = network.getApi()
        val (u, t, s) = auth()
        val resp = api.getLyrics(artist, title, u, t, s).body() ?: return@withContext null
        val root = root(resp) ?: return@withContext null
        val lyrics = root["lyrics"] as? Map<String, Any> ?: return@withContext null
        lyrics["value"] as? String
    }

    // ─── Random Songs ────────────────────────────────────────────────────────

    suspend fun getRandomSongs(size: Int = 20): List<Song> = withContext(Dispatchers.IO) {
        val api = network.getApi()
        val (u, t, s) = auth()
        val resp = api.getRandomSongs(size, u, t, s).body() ?: return@withContext emptyList()
        val root = root(resp) ?: return@withContext emptyList()
        val container = root["randomSongs"] as? Map<String, Any> ?: return@withContext emptyList()
        val songs = container["song"] as? List<Map<String, Any>> ?: return@withContext emptyList()
        songs.mapNotNull { parseSong(it) }
    }

    // ─── Ping ────────────────────────────────────────────────────────────────

    suspend fun ping(): Boolean = withContext(Dispatchers.IO) {
        try {
            val api = network.getApi()
            val (u, t, s) = auth()
            val resp = api.ping(u, t, s).body() ?: return@withContext false
            val root = root(resp) ?: return@withContext false
            root["status"] == "ok"
        } catch (e: Exception) {
            false
        }
    }

    // ─── URL helpers ─────────────────────────────────────────────────────────

    fun getStreamUrl(songId: String) = network.getStreamUrl(songId)
    fun getCoverArtUrl(coverArt: String?) = network.getCoverArtUrl(coverArt)

    // ─── Parsers ─────────────────────────────────────────────────────────────

    private fun parseAlbum(a: Map<String, Any>): Album? = Album(
        id = a["id"] as? String ?: return null,
        name = a["name"] as? String ?: "",
        artist = a["artist"] as? String ?: "",
        artistId = a["artistId"] as? String ?: "",
        coverArt = a["coverArt"] as? String,
        songCount = (a["songCount"] as? Double)?.toInt() ?: 0,
        year = (a["year"] as? Double)?.toInt() ?: 0,
        genre = a["genre"] as? String
    )

    private fun parseSong(s: Map<String, Any>): Song? = Song(
        id = s["id"] as? String ?: return null,
        title = s["title"] as? String ?: "",
        artist = s["artist"] as? String ?: "",
        album = s["album"] as? String ?: "",
        albumId = s["albumId"] as? String ?: "",
        duration = (s["duration"] as? Double)?.toInt() ?: 0,
        track = (s["track"] as? Double)?.toInt() ?: 0,
        year = (s["year"] as? Double)?.toInt() ?: 0,
        coverArt = s["coverArt"] as? String,
        suffix = s["suffix"] as? String ?: "",
        size = (s["size"] as? Double)?.toLong() ?: 0L,
        contentType = s["contentType"] as? String ?: "",
        path = s["path"] as? String ?: ""
    )
}
