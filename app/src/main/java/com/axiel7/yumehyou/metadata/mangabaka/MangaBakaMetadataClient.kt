package com.axiel7.yumehyou.metadata.mangabaka

import com.axiel7.anihyou.core.base.DataResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

private const val MANGA_BAKA_API_URL = "https://api.mangabaka.dev/v1/"

class MangaBakaMetadataClient(
    private val client: OkHttpClient,
) {
    suspend fun getSeriesDetails(seriesId: String): DataResult<String> =
        get(path = "series/$seriesId")

    suspend fun getSeriesLinks(seriesId: String): DataResult<String> =
        get(path = "series/$seriesId/links")

    suspend fun getSeriesCollections(seriesId: String): DataResult<String> =
        get(path = "series/$seriesId/collections")

    suspend fun getSeriesWorks(seriesId: String): DataResult<String> =
        get(path = "series/$seriesId/works")

    suspend fun getSeriesRelated(seriesId: String): DataResult<String> =
        get(path = "series/$seriesId/related")

    suspend fun getSeriesNews(seriesId: String): DataResult<String> =
        get(path = "series/$seriesId/news")

    suspend fun getGenres(): DataResult<String> =
        get(path = "genres")

    suspend fun getTags(): DataResult<String> =
        get(path = "tags")

    suspend fun searchSeries(
        query: String,
        page: Int,
        perPage: Int,
        contentRatings: Collection<String> = emptyList(),
    ): DataResult<String> {
        val queryParameters = linkedMapOf(
            "page" to page.coerceAtLeast(1).toString(),
            "limit" to perPage.coerceAtLeast(1).toString(),
        )
        if (query.isNotBlank()) {
            queryParameters["q"] = query
        }
        return get(
            path = "series/search",
            query = queryParameters,
            repeatedQuery = mapOf(
                "content_rating" to contentRatings.filter { it.isNotBlank() },
            ),
        )
    }

    private suspend fun get(
        path: String,
        query: Map<String, String?> = emptyMap(),
        repeatedQuery: Map<String, Collection<String>> = emptyMap(),
    ): DataResult<String> = withContext(Dispatchers.IO) {
        val baseUrl = "${MANGA_BAKA_API_URL}$path".toHttpUrl()
        val url = baseUrl.newBuilder().apply {
            query.forEach { (key, value) ->
                if (!value.isNullOrBlank()) addQueryParameter(key, value)
            }
            repeatedQuery.forEach { (key, values) ->
                values.filter { it.isNotBlank() }.forEach { addQueryParameter(key, it) }
            }
        }.build()

        runCatching {
            client.newCall(
                Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("User-Agent", "YumeHyou")
                    .build(),
            ).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    DataResult.Error(message = body.ifBlank { "MangaBaka request failed (${response.code})" })
                } else {
                    DataResult.Success(body)
                }
            }
        }.getOrElse {
            DataResult.Error(message = it.message ?: "MangaBaka request failed")
        }
    }
}
