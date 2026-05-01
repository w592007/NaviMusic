package com.navimusic.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SubsonicApi {

    @GET("rest/getArtists")
    suspend fun getArtists(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "NaviMusic",
        @Query("f") format: String = "json"
    ): Response<Map<String, Any>>

    @GET("rest/getArtist")
    suspend fun getArtist(
        @Query("id") id: String,
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "NaviMusic",
        @Query("f") format: String = "json"
    ): Response<Map<String, Any>>

    @GET("rest/getAlbumList2")
    suspend fun getAlbumList2(
        @Query("type") type: String = "alphabeticalByName",
        @Query("size") size: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "NaviMusic",
        @Query("f") format: String = "json"
    ): Response<Map<String, Any>>

    @GET("rest/getAlbum")
    suspend fun getAlbum(
        @Query("id") id: String,
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "NaviMusic",
        @Query("f") format: String = "json"
    ): Response<Map<String, Any>>

    @GET("rest/search3")
    suspend fun search3(
        @Query("query") query: String,
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "NaviMusic",
        @Query("f") format: String = "json"
    ): Response<Map<String, Any>>

    @GET("rest/getPlaylists")
    suspend fun getPlaylists(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "NaviMusic",
        @Query("f") format: String = "json"
    ): Response<Map<String, Any>>

    @GET("rest/getPlaylist")
    suspend fun getPlaylist(
        @Query("id") id: String,
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "NaviMusic",
        @Query("f") format: String = "json"
    ): Response<Map<String, Any>>

    @GET("rest/getLyrics")
    suspend fun getLyrics(
        @Query("artist") artist: String? = null,
        @Query("title") title: String? = null,
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "NaviMusic",
        @Query("f") format: String = "json"
    ): Response<Map<String, Any>>

    @GET("rest/ping")
    suspend fun ping(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "NaviMusic",
        @Query("f") format: String = "json"
    ): Response<Map<String, Any>>

    @GET("rest/getRandomSongs")
    suspend fun getRandomSongs(
        @Query("size") size: Int = 20,
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "NaviMusic",
        @Query("f") format: String = "json"
    ): Response<Map<String, Any>>
}
