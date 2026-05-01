package com.navimusic.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "navimusic_prefs")

class PrefsManager(private val context: Context) {

    companion object {
        val KEY_LAN_URL = stringPreferencesKey("lan_url")
        val KEY_WAN_URL = stringPreferencesKey("wan_url")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_PASSWORD = stringPreferencesKey("password")
    }

    val lanUrl: String get() = runBlocking {
        context.dataStore.data.first()[KEY_LAN_URL] ?: ""
    }

    val wanUrl: String get() = runBlocking {
        context.dataStore.data.first()[KEY_WAN_URL] ?: ""
    }

    val username: String get() = runBlocking {
        context.dataStore.data.first()[KEY_USERNAME] ?: ""
    }

    val password: String get() = runBlocking {
        context.dataStore.data.first()[KEY_PASSWORD] ?: ""
    }

    suspend fun save(lanUrl: String, wanUrl: String, username: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAN_URL] = lanUrl
            prefs[KEY_WAN_URL] = wanUrl
            prefs[KEY_USERNAME] = username
            prefs[KEY_PASSWORD] = password
        }
    }

    fun isConfigured(): Boolean = username.isNotBlank() && password.isNotBlank() &&
            (lanUrl.isNotBlank() || wanUrl.isNotBlank())
}
