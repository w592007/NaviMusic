package com.navimusic.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: String,
    val duration: Int,        // 秒
    val track: Int,
    val year: Int,
    val coverArt: String?,
    val suffix: String,
    val size: Long,
    val contentType: String,
    val path: String
)

data class Album(
    val id: String,
    val name: String,
    val artist: String,
    val artistId: String,
    val coverArt: String?,
    val songCount: Int,
    val year: Int,
    val genre: String?
)

data class Artist(
    val id: String,
    val name: String,
    val albumCount: Int,
    val coverArt: String?
)

data class Playlist(
    val id: String,
    val name: String,
    val songCount: Int,
    val duration: Int,
    val coverArt: String?,
    val comment: String?
)

data class LyricLine(
    val timeMs: Long,   // 毫秒时间戳
    val text: String
)
