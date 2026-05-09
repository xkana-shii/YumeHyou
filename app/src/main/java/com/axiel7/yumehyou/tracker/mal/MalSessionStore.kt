package com.axiel7.yumehyou.tracker.mal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class MalSession(
    val accessToken: String?,
    val refreshToken: String?,
    val tokenType: String?,
    val expiresAtEpochSeconds: Long?,
)

class MalSessionStore(
    private val dataStore: DataStore<Preferences>,
) {
    val session: Flow<MalSession> = dataStore.data.map { preferences ->
        MalSession(
            accessToken = preferences[MAL_ACCESS_TOKEN_KEY],
            refreshToken = preferences[MAL_REFRESH_TOKEN_KEY],
            tokenType = preferences[MAL_TOKEN_TYPE_KEY],
            expiresAtEpochSeconds = preferences[MAL_EXPIRES_AT_EPOCH_SECONDS_KEY],
        )
    }

    suspend fun getSession(): MalSession = session.first()

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String?,
        tokenType: String?,
        expiresInSeconds: Long?,
    ) {
        dataStore.edit { preferences ->
            preferences[MAL_ACCESS_TOKEN_KEY] = accessToken
            if (refreshToken.isNullOrBlank()) preferences.remove(MAL_REFRESH_TOKEN_KEY)
            else preferences[MAL_REFRESH_TOKEN_KEY] = refreshToken
            if (tokenType.isNullOrBlank()) preferences.remove(MAL_TOKEN_TYPE_KEY)
            else preferences[MAL_TOKEN_TYPE_KEY] = tokenType
            val expiresAt = expiresInSeconds?.let { currentEpochSeconds() + it }
            if (expiresAt == null) preferences.remove(MAL_EXPIRES_AT_EPOCH_SECONDS_KEY)
            else preferences[MAL_EXPIRES_AT_EPOCH_SECONDS_KEY] = expiresAt
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(MAL_ACCESS_TOKEN_KEY)
            preferences.remove(MAL_REFRESH_TOKEN_KEY)
            preferences.remove(MAL_TOKEN_TYPE_KEY)
            preferences.remove(MAL_EXPIRES_AT_EPOCH_SECONDS_KEY)
        }
    }

    companion object {
        private val MAL_ACCESS_TOKEN_KEY = stringPreferencesKey("mal_access_token")
        private val MAL_REFRESH_TOKEN_KEY = stringPreferencesKey("mal_refresh_token")
        private val MAL_TOKEN_TYPE_KEY = stringPreferencesKey("mal_token_type")
        private val MAL_EXPIRES_AT_EPOCH_SECONDS_KEY = longPreferencesKey("mal_expires_at_epoch_seconds")

        private fun currentEpochSeconds() = System.currentTimeMillis() / 1_000
    }
}
