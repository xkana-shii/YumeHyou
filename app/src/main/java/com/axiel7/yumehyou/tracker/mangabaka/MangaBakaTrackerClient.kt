package com.axiel7.yumehyou.tracker.mangabaka

import com.axiel7.anihyou.core.base.DataResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

private const val MANGA_BAKA_API_URL = "https://api.mangabaka.dev/v1/"

class MangaBakaTrackerClient(
    private val client: OkHttpClient,
    private val authService: MangaBakaAuthService,
) {
    suspend fun getMyProfile(): DataResult<String> =
        authenticatedRequest(method = "GET", path = "me")

    suspend fun getMyLibrary(
        state: String?,
        page: Int,
        limit: Int,
    ): DataResult<String> = authenticatedRequest(
        method = "GET",
        path = "my/library",
        query = mapOf(
            "page" to page.coerceAtLeast(1).toString(),
            "limit" to limit.coerceAtLeast(1).toString(),
            "state" to state,
            "type" to "manga",
        ),
    )

    suspend fun createLibraryEntry(
        seriesId: String,
        state: String,
        progressChapter: Int? = null,
        progressVolume: Int? = null,
        rating: Int? = null,
        note: String? = null,
        rereads: Int? = null,
    ): DataResult<String> = authenticatedRequest(
        method = "POST",
        path = "my/library/$seriesId",
        jsonBody = JSONObject().apply {
            put("state", state)
            progressChapter?.let { put("progress_chapter", it) }
            progressVolume?.let { put("progress_volume", it) }
            rating?.let { put("rating", it) }
            note?.takeIf { it.isNotBlank() }?.let { put("note", it) }
            rereads?.let { put("number_of_rereads", it) }
        },
    )

    suspend fun updateLibraryEntry(
        seriesId: String,
        state: String? = null,
        progressChapter: Int? = null,
        progressVolume: Int? = null,
        rating: Int? = null,
        note: String? = null,
        rereads: Int? = null,
    ): DataResult<String> {
        val payload = JSONObject().apply {
            state?.let { put("state", it) }
            progressChapter?.let { put("progress_chapter", it) }
            progressVolume?.let { put("progress_volume", it) }
            rating?.let { put("rating", it) }
            note?.takeIf { it.isNotBlank() }?.let { put("note", it) }
            rereads?.let { put("number_of_rereads", it) }
        }
        return authenticatedRequest(
            method = "PUT",
            path = "my/library/$seriesId",
            jsonBody = payload,
        )
    }

    private suspend fun authenticatedRequest(
        method: String,
        path: String,
        query: Map<String, String?> = emptyMap(),
        jsonBody: JSONObject? = null,
    ): DataResult<String> = withContext(Dispatchers.IO) {
        val accessToken = authService.getValidAccessToken()
            ?: return@withContext DataResult.Error("Missing MangaBaka access token")

        val baseUrl = "${MANGA_BAKA_API_URL}$path".toHttpUrl()
        val url = baseUrl.newBuilder().apply {
            query.forEach { (key, value) ->
                if (!value.isNullOrBlank()) addQueryParameter(key, value)
            }
        }.build()

        runCatching {
            val body = jsonBody
                ?.toString()
                ?.toRequestBody("application/json; charset=utf-8".toMediaType())
            val requestBuilder = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("User-Agent", "YumeHyou")
            when (method) {
                "POST" -> requestBuilder.post(body ?: EMPTY_JSON_BODY)
                "PUT" -> requestBuilder.put(body ?: EMPTY_JSON_BODY)
                else -> requestBuilder.get()
            }
            client.newCall(requestBuilder.build()).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    DataResult.Error(message = responseBody.ifBlank { "MangaBaka request failed (${response.code})" })
                } else {
                    DataResult.Success(responseBody)
                }
            }
        }.getOrElse {
            DataResult.Error(message = it.message ?: "MangaBaka request failed")
        }
    }

    companion object {
        private val EMPTY_JSON_BODY = "{}".toRequestBody("application/json; charset=utf-8".toMediaType())
    }
}
