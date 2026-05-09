package com.axiel7.yumehyou.tracker.mangabaka

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class MangaBakaSession(
    val accessToken: String?,
    val refreshToken: String?,
    val tokenType: String?,
    val expiresAtEpochSeconds: Long?,
)

class MangaBakaSessionStore(
    private val dataStore: DataStore<Preferences>,
) {
    val session: Flow<MangaBakaSession> = dataStore.data.map { preferences ->
        MangaBakaSession(
            accessToken = preferences[MANGA_BAKA_ACCESS_TOKEN_KEY],
            refreshToken = preferences[MANGA_BAKA_REFRESH_TOKEN_KEY],
            tokenType = preferences[MANGA_BAKA_TOKEN_TYPE_KEY],
            expiresAtEpochSeconds = preferences[MANGA_BAKA_EXPIRES_AT_EPOCH_SECONDS_KEY],
        )
    }

    suspend fun getSession(): MangaBakaSession = session.first()

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String?,
        tokenType: String?,
        expiresInSeconds: Long?,
    ) {
        dataStore.edit { preferences ->
            preferences[MANGA_BAKA_ACCESS_TOKEN_KEY] = accessToken
            if (refreshToken.isNullOrBlank()) preferences.remove(MANGA_BAKA_REFRESH_TOKEN_KEY)
            else preferences[MANGA_BAKA_REFRESH_TOKEN_KEY] = refreshToken
            if (tokenType.isNullOrBlank()) preferences.remove(MANGA_BAKA_TOKEN_TYPE_KEY)
            else preferences[MANGA_BAKA_TOKEN_TYPE_KEY] = tokenType
            val expiresAt = expiresInSeconds?.let { currentEpochSeconds() + it }
            if (expiresAt == null) preferences.remove(MANGA_BAKA_EXPIRES_AT_EPOCH_SECONDS_KEY)
            else preferences[MANGA_BAKA_EXPIRES_AT_EPOCH_SECONDS_KEY] = expiresAt
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(MANGA_BAKA_ACCESS_TOKEN_KEY)
            preferences.remove(MANGA_BAKA_REFRESH_TOKEN_KEY)
            preferences.remove(MANGA_BAKA_TOKEN_TYPE_KEY)
            preferences.remove(MANGA_BAKA_EXPIRES_AT_EPOCH_SECONDS_KEY)
        }
    }

    companion object {
        private val MANGA_BAKA_ACCESS_TOKEN_KEY = stringPreferencesKey("mangabaka_access_token")
        private val MANGA_BAKA_REFRESH_TOKEN_KEY = stringPreferencesKey("mangabaka_refresh_token")
        private val MANGA_BAKA_TOKEN_TYPE_KEY = stringPreferencesKey("mangabaka_token_type")
        private val MANGA_BAKA_EXPIRES_AT_EPOCH_SECONDS_KEY =
            longPreferencesKey("mangabaka_expires_at_epoch_seconds")

        private fun currentEpochSeconds() = System.currentTimeMillis() / 1_000
    }
}
