package com.navimusic.api

import retrofit2.http.GET
import retrofit2.http.Query

// Subsonic API 响应包装
data class SubsonicResponse<T>(
    val `subsonic-response`: SubsonicRoot<T>
)

data class SubsonicRoot<T>(
    val status: String,
    val version: String,
    val artists: ArtistsContainer? = null,
    val artist: ArtistDetail? = null,
    val albumList2: AlbumListContainer? = null,
    val album: AlbumDetail? = null,
    val searchResult3: SearchResult? = null,
    val playlists: PlaylistsContainer? = null,
    val playlist: PlaylistDetail? = null,
    val lyrics: LyricsResponse? = null,
    val randomSongs: RandomSongsContainer? = null
)

data class ArtistsContainer(val index: List<ArtistIndex>)
data class ArtistIndex(val name: String, val artist: List<ArtistItem>)
data class ArtistItem(val id: String, val name: String, val albumCount: Int, val coverArt: String?)
data class ArtistDetail(
    val id: String, val name: String, val coverArt: String?,
    val album: List<AlbumItem>?
)

data class AlbumListContainer(val album: List<AlbumItem>)
data class AlbumItem(
    val id: String, val name: String, val artist: String, val artistId: String,
    val coverArt: String?, val songCount: Int, val year: Int?, val genre: String?
)
data class AlbumDetail(
    val id: String, val name: String, val artist: String, val artistId: String,
    val coverArt: String?, val songCount: Int, val year: Int?,
    val song: List<SongItem>?
)

data class SongItem(
    val id: String, val title: String, val artist: String, val album: String,
    val albumId: String, val duration: Int?, val track: Int?, val year: Int?,
    val coverArt: String?, val suffix: String?, val size: Long?,
    val contentType: String?, val path: String?
)

data class SearchResult(
    val song: List<SongItem>?,
    val album: List<AlbumItem>?,
    val artist: List<ArtistItem>?
)

data class PlaylistsContainer(val playlist: List<PlaylistItem>)
data class PlaylistItem(
    val id: String, val name: String, val songCount: Int,
    val duration: Int, val coverArt: String?, val comment: String?
)
data class PlaylistDetail(
    val id: String, val name: String, val entry: List<SongItem>?
)

data class LyricsResponse(val artist: String?, val title: String?, val value: String?)
data class RandomSongsContainer(val song: List<SongItem>)
