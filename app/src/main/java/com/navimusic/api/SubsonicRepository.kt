package com.navimusic.api

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.navimusic.model.*

/**
 * Subsonic API 封装层，处理响应解析
 */
class SubsonicRepository(private val networkManager: NetworkManager) {

    private val gson = Gson()

    private fun parseMap(map: Map<String, Any>): Map<String, Any> {
        @Suppress("UNCHECKED_CAST")
        return map["subsonic-response"] as? Map<String, Any> ?: emptyMap()
    }

    private inline fun <reified T> Any?.cast(): T? {
        val json = gson.toJson(this)
        return try {
            gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun ping(): Boolean {
        return try {
            val (u, t, s) = networkManager.authParams()
            val api = networkManager.getApi()
            val response = api.ping(u, t, s)
            val root = response.body()?.let { parseMap(it) }
            root?.get("status") == "ok"
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getArtists(): List<Artist> {
        val (u, t, s) = networkManager.authParams()
        val api = networkManager.getApi()
        val response = api.getArtists(u, t, s)
        val root = response.body()?.let { parseMap(it) } ?: return emptyList()

        @Suppress("UNCHECKED_CAST")
        val artistsMap = root["artists"] as? Map<String, Any> ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        val indexes = artistsMap["index"] as? List<Map<String, Any>> ?: return emptyList()

        val result = mutableListOf<Artist>()
        indexes.forEach { index ->
            @Suppress("UNCHECKED_CAST")
            val artists = index["artist"] as? List<Map<String, Any>> ?: emptyList()
            artists.forEach { a ->
                result.add(
                    Artist(
                        id = a["id"].toString(),
                        name = a["name"].toString(),
                        albumCount = (a["albumCount"] as? Double)?.toInt() ?: 0,
                        coverArt = a["coverArt"] as? String
                    )
                )
            }
        }
        return result.sortedBy { it.name }
    }

    suspend fun getArtistAlbums(artistId: String): List<Album> {
        val (u, t, s) = networkManager.authParams()
        val api = networkManager.getApi()
        val response = api.getArtist(artistId, u, t, s)
        val root = response.body()?.let { parseMap(it) } ?: return emptyList()

        @Suppress("UNCHECKED_CAST")
        val artistMap = root["artist"] as? Map<String, Any> ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        val albums = artistMap["album"] as? List<Map<String, Any>> ?: return emptyList()

        return albums.map { a ->
            Album(
                id = a["id"].toString(),
                name = a["name"].toString(),
                artist = a["artist"]?.toString() ?: "",
                artistId = a["artistId"]?.toString() ?: "",
                coverArt = a["coverArt"] as? String,
                songCount = (a["songCount"] as? Double)?.toInt() ?: 0,
                year = (a["year"] as? Double)?.toInt() ?: 0,
                genre = a["genre"] as? String
            )
        }
    }

    suspend fun getAlbumSongs(albumId: String): List<Song> {
        val (u, t, s) = networkManager.authParams()
        val api = networkManager.getApi()
        val response = api.getAlbum(albumId, u, t, s)
        val root = response.body()?.let { parseMap(it) } ?: return emptyList()

        @Suppress("UNCHECKED_CAST")
        val albumMap = root["album"] as? Map<String, Any> ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        val songs = albumMap["song"] as? List<Map<String, Any>> ?: return emptyList()

        return songs.mapNotNull { parseSong(it) }
    }

    suspend fun getAlbums(type: String = "alphabeticalByName", size: Int = 50, offset: Int = 0): List<Album> {
        val (u, t, s) = networkManager.authParams()
        val api = networkManager.getApi()
        val response = api.getAlbumList2(type, size, offset, u, t, s)
        val root = response.body()?.let { parseMap(it) } ?: return emptyList()

        @Suppress("UNCHECKED_CAST")
        val albumListMap = root["albumList2"] as? Map<String, Any> ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        val albums = albumListMap["album"] as? List<Map<String, Any>> ?: return emptyList()

        return albums.map { a ->
            Album(
                id = a["id"].toString(),
                name = a["name"].toString(),
                artist = a["artist"]?.toString() ?: "",
                artistId = a["artistId"]?.toString() ?: "",
                coverArt = a["coverArt"] as? String,
                songCount = (a["songCount"] as? Double)?.toInt() ?: 0,
                year = (a["year"] as? Double)?.toInt() ?: 0,
                genre = a["genre"] as? String
            )
        }
    }

    suspend fun search(query: String): Triple<List<Song>, List<Album>, List<Artist>> {
        val (u, t, s) = networkManager.authParams()
        val api = networkManager.getApi()
        val response = api.search3(query, u, t, s)
        val root = response.body()?.let { parseMap(it) } ?: return Triple(emptyList(), emptyList(), emptyList())

        @Suppress("UNCHECKED_CAST")
        val resultMap = root["searchResult3"] as? Map<String, Any> ?: return Triple(emptyList(), emptyList(), emptyList())

        @Suppress("UNCHECKED_CAST")
        val songs = (resultMap["song"] as? List<Map<String, Any>>)?.mapNotNull { parseSong(it) } ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val albums = (resultMap["album"] as? List<Map<String, Any>>)?.map { a ->
            Album(a["id"].toString(), a["name"].toString(), a["artist"]?.toString() ?: "",
                a["artistId"]?.toString() ?: "", a["coverArt"] as? String,
                (a["songCount"] as? Double)?.toInt() ?: 0, (a["year"] as? Double)?.toInt() ?: 0, null)
        } ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val artists = (resultMap["artist"] as? List<Map<String, Any>>)?.map { a ->
            Artist(a["id"].toString(), a["name"].toString(), (a["albumCount"] as? Double)?.toInt() ?: 0, a["coverArt"] as? String)
        } ?: emptyList()

        return Triple(songs, albums, artists)
    }

    suspend fun getPlaylists(): List<Playlist> {
        val (u, t, s) = networkManager.authParams()
        val api = networkManager.getApi()
        val response = api.getPlaylists(u, t, s)
        val root = response.body()?.let { parseMap(it) } ?: return emptyList()

        @Suppress("UNCHECKED_CAST")
        val playlistsMap = root["playlists"] as? Map<String, Any> ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        val playlists = playlistsMap["playlist"] as? List<Map<String, Any>> ?: return emptyList()

        return playlists.map { p ->
            Playlist(
                id = p["id"].toString(),
                name = p["name"].toString(),
                songCount = (p["songCount"] as? Double)?.toInt() ?: 0,
                duration = (p["duration"] as? Double)?.toInt() ?: 0,
                coverArt = p["coverArt"] as? String,
                comment = p["comment"] as? String
            )
        }
    }

    suspend fun getPlaylistSongs(playlistId: String): List<Song> {
        val (u, t, s) = networkManager.authParams()
        val api = networkManager.getApi()
        val response = api.getPlaylist(playlistId, u, t, s)
        val root = response.body()?.let { parseMap(it) } ?: return emptyList()

        @Suppress("UNCHECKED_CAST")
        val playlistMap = root["playlist"] as? Map<String, Any> ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        val songs = playlistMap["entry"] as? List<Map<String, Any>> ?: return emptyList()

        return songs.mapNotNull { parseSong(it) }
    }

    suspend fun getLyrics(artist: String, title: String): String? {
        val (u, t, s) = networkManager.authParams()
        val api = networkManager.getApi()
        val response = api.getLyrics(artist, title, u, t, s)
        val root = response.body()?.let { parseMap(it) } ?: return null

        @Suppress("UNCHECKED_CAST")
        val lyricsMap = root["lyrics"] as? Map<String, Any> ?: return null
        return lyricsMap["value"] as? String
    }

    suspend fun getRandomSongs(size: Int = 20): List<Song> {
        val (u, t, s) = networkManager.authParams()
        val api = networkManager.getApi()
        val response = api.getRandomSongs(size, u, t, s)
        val root = response.body()?.let { parseMap(it) } ?: return emptyList()

        @Suppress("UNCHECKED_CAST")
        val randomSongsMap = root["randomSongs"] as? Map<String, Any> ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        val songs = randomSongsMap["song"] as? List<Map<String, Any>> ?: return emptyList()

        return songs.mapNotNull { parseSong(it) }
    }

    private fun parseSong(map: Map<String, Any>): Song? {
        val id = map["id"]?.toString() ?: return null
        return Song(
            id = id,
            title = map["title"]?.toString() ?: map["name"]?.toString() ?: "未知歌曲",
            artist = map["artist"]?.toString() ?: "未知歌手",
            album = map["album"]?.toString() ?: "未知专辑",
            albumId = map["albumId"]?.toString() ?: "",
            duration = (map["duration"] as? Double)?.toInt() ?: 0,
            track = (map["track"] as? Double)?.toInt() ?: 0,
            year = (map["year"] as? Double)?.toInt() ?: 0,
            coverArt = map["coverArt"] as? String,
            suffix = map["suffix"]?.toString() ?: "mp3",
            size = (map["size"] as? Double)?.toLong() ?: 0,
            contentType = map["contentType"]?.toString() ?: "audio/mpeg",
            path = map["path"]?.toString() ?: ""
        )
    }
}
