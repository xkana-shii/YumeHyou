package com.axiel7.yumehyou.tracker.mal

import android.net.Uri
import com.axiel7.anihyou.core.base.MAL_CLIENT_ID
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

private const val MAL_OAUTH_TOKEN_URL = "https://myanimelist.net/v1/oauth2/token"

class MalAuthService(
    private val sessionStore: MalSessionStore,
    private val client: OkHttpClient,
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
        if (!accessToken.isNullOrBlank()) {
            sessionStore.saveSession(
                accessToken = accessToken,
                refreshToken = uri.getQueryParameter("refresh_token")
                    ?: fragmentParams?.getQueryParameter("refresh_token"),
                tokenType = uri.getQueryParameter("token_type")
                    ?: fragmentParams?.getQueryParameter("token_type"),
                expiresInSeconds = (uri.getQueryParameter("expires_in")
                    ?: fragmentParams?.getQueryParameter("expires_in"))?.toLongOrNull(),
            )
            return
        }

        val code = uri.getQueryParameter("code")
        val codeVerifier = uri.getQueryParameter("code_verifier")
        if (!code.isNullOrBlank() && !codeVerifier.isNullOrBlank()) {
            exchangeAuthorizationCode(code = code, codeVerifier = codeVerifier)
        }
    }

    suspend fun onNewToken(token: String) {
        sessionStore.saveSession(
            accessToken = token,
            refreshToken = null,
            tokenType = null,
            expiresInSeconds = null,
        )
    }

    suspend fun logOut() {
        sessionStore.clearSession()
    }

    suspend fun getValidAccessToken(): String? {
        val session = sessionStore.getSession()
        val accessToken = session.accessToken ?: return null
        val expiresAt = session.expiresAtEpochSeconds
        if (expiresAt == null || expiresAt > currentEpochSeconds()) {
            return accessToken
        }
        val refreshToken = session.refreshToken ?: return null
        return refreshAccessToken(refreshToken)
    }

    private suspend fun refreshAccessToken(refreshToken: String): String? {
        return requestToken(
            formBody = FormBody.Builder()
                .add("client_id", MAL_CLIENT_ID)
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build(),
        )
    }

    private suspend fun exchangeAuthorizationCode(
        code: String,
        codeVerifier: String,
    ): String? {
        return requestToken(
            formBody = FormBody.Builder()
                .add("client_id", MAL_CLIENT_ID)
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("code_verifier", codeVerifier)
                .build(),
        )
    }

    private suspend fun requestToken(formBody: FormBody): String? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(MAL_OAUTH_TOKEN_URL)
                .post(formBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@use null
                val json = JSONObject(body)
                val accessToken = json.optString("access_token").takeIf { it.isNotBlank() }
                    ?: return@use null
                sessionStore.saveSession(
                    accessToken = accessToken,
                    refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
                    tokenType = json.optString("token_type").takeIf { it.isNotBlank() },
                    expiresInSeconds = json.optLong("expires_in").takeIf { it > 0L },
                )
                accessToken
            }
        }.getOrNull()
    }

    private fun currentEpochSeconds() = System.currentTimeMillis() / 1_000
}
