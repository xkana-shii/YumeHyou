package com.axiel7.anihyou.core.network.api

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

private const val MANGA_BAKA_API_URL = "https://api.mangabaka.dev/v1/"

class MangaBakaApi(
    private val client: OkHttpClient,
) {
    fun searchSeries(
        query: String,
        page: Int = 1,
        perPage: Int = 20,
    ): String? = get(
        path = "series/search",
        queryParams = mapOf(
            "q" to query,
            "page" to page.toString(),
            "limit" to perPage.toString(),
        ),
    )

    fun getSeries(seriesId: String): String? =
        get(path = "series/$seriesId")

    fun getSeriesLinks(seriesId: String): String? =
        get(path = "series/$seriesId/links")

    fun getSeriesCollections(seriesId: String): String? =
        get(path = "series/$seriesId/collections")

    fun getSeriesWorks(seriesId: String): String? =
        get(path = "series/$seriesId/works")

    fun getSeriesRelated(seriesId: String): String? =
        get(path = "series/$seriesId/related")

    private fun get(
        path: String,
        queryParams: Map<String, String> = emptyMap(),
    ): String? {
        val baseUrl = "$MANGA_BAKA_API_URL$path".toHttpUrl()
        val url = baseUrl.newBuilder().apply {
            queryParams.forEach { (key, value) ->
                if (value.isNotBlank()) addQueryParameter(key, value)
            }
        }.build()

        return runCatching {
            client.newCall(
                Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "YumeHyou")
                    .get()
                    .build(),
            ).execute().use { response ->
                if (!response.isSuccessful) null else response.body?.string()
            }
        }.getOrNull()
    }
}
