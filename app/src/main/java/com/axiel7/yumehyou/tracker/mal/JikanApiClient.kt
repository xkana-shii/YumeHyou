package com.axiel7.yumehyou.tracker.mal

import com.axiel7.anihyou.core.base.DataResult
import com.axiel7.anihyou.core.network.type.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

private const val JIKAN_API_URL = "https://api.jikan.moe/v4/"

class JikanApiClient(
    private val client: OkHttpClient,
) {
    suspend fun getUserFavorites(username: String): DataResult<String> =
        get(path = "users/$username/favorites")

    suspend fun getUserSocial(username: String): DataResult<String> =
        get(path = "users/$username/friends")

    suspend fun searchMedia(
        mediaType: MediaType,
        query: String,
        page: Int,
        limit: Int,
    ): DataResult<String> {
        val endpoint = if (mediaType == MediaType.MANGA) "manga" else "anime"
        return get(
            path = endpoint,
            query = mapOf(
                "q" to query,
                "page" to page.toString(),
                "limit" to limit.toString(),
            ),
        )
    }

    suspend fun getAnimeDetails(animeId: Int): DataResult<String> =
        get(path = "anime/$animeId/full")

    suspend fun getMangaDetails(mangaId: Int): DataResult<String> =
        get(path = "manga/$mangaId/full")

    private suspend fun get(
        path: String,
        query: Map<String, String?> = emptyMap(),
    ): DataResult<String> = withContext(Dispatchers.IO) {
        val baseUrl = "${JIKAN_API_URL}$path".toHttpUrl()
        val url = baseUrl.newBuilder().apply {
            query.forEach { (key, value) ->
                if (!value.isNullOrBlank()) addQueryParameter(key, value)
            }
        }.build()

        runCatching {
            client.newCall(
                Request.Builder()
                    .url(url)
                    .get()
                    .build(),
            ).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    DataResult.Error(message = body.ifBlank { "Jikan request failed (${response.code})" })
                } else {
                    DataResult.Success(body)
                }
            }
        }.getOrElse {
            DataResult.Error(message = it.message ?: "Jikan request failed")
        }
    }
}
