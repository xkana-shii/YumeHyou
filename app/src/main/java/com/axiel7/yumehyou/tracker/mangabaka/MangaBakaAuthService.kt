package com.axiel7.yumehyou.tracker.mangabaka

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MangaBakaAuthService(
    private val sessionStore: MangaBakaSessionStore,
) {
    val isLoggedIn: Flow<Boolean> = sessionStore.session.map { !it.accessToken.isNullOrBlank() }

    suspend fun onAuthRedirect(uri: Uri) {
        val fragmentParams = uri.fragment
            ?.takeIf { it.contains("=") }
            ?.let { "https://dummy.local?$it".toUri() }
        val accessToken = uri.getQueryParameter("access_token")
            ?: uri.getQueryParameter("token")
            ?: fragmentParams?.getQueryParameter("access_token")
            ?: fragmentParams?.getQueryParameter("token")
        if (accessToken.isNullOrBlank()) return
        sessionStore.saveSession(
            accessToken = accessToken,
            refreshToken = uri.getQueryParameter("refresh_token")
                ?: fragmentParams?.getQueryParameter("refresh_token"),
            tokenType = uri.getQueryParameter("token_type")
                ?: fragmentParams?.getQueryParameter("token_type"),
            expiresInSeconds = (uri.getQueryParameter("expires_in")
                ?: fragmentParams?.getQueryParameter("expires_in"))?.toLongOrNull(),
        )
    }

    suspend fun onNewToken(token: String) {
        sessionStore.saveSession(
            accessToken = token,
            refreshToken = null,
            tokenType = "Bearer",
            expiresInSeconds = null,
        )
    }

    suspend fun logOut() {
        sessionStore.clearSession()
    }

    suspend fun getValidAccessToken(): String? {
        val session = sessionStore.getSession()
        return if (session.expiresAtEpochSeconds == null || session.expiresAtEpochSeconds > currentEpochSeconds()) {
            session.accessToken
        } else {
            null
        }
    }

    private fun currentEpochSeconds() = System.currentTimeMillis() / 1_000
}
