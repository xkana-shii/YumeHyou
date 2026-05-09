package com.axiel7.yumehyou.tracker.mal

import com.axiel7.anihyou.core.base.DataResult
import com.axiel7.anihyou.core.network.type.MediaType

class MalMetadataProvider(
    private val malApiClient: MalApiClient,
    private val jikanApiClient: JikanApiClient,
) {
    suspend fun getMediaDetails(mediaId: Int): DataResult<String> {
        val officialAnime = malApiClient.getAnimeDetails(mediaId)
        if (officialAnime is DataResult.Success) return officialAnime

        val officialManga = malApiClient.getMangaDetails(mediaId)
        if (officialManga is DataResult.Success) return officialManga

        val jikanAnime = jikanApiClient.getAnimeDetails(mediaId)
        if (jikanAnime is DataResult.Success) return jikanAnime

        return jikanApiClient.getMangaDetails(mediaId)
    }

    suspend fun searchMedia(
        mediaType: MediaType,
        query: String,
        page: Int,
        perPage: Int,
    ): DataResult<String> {
        val offset = (page - 1).coerceAtLeast(0) * perPage
        val official = malApiClient.searchMedia(
            mediaType = mediaType,
            query = query,
            offset = offset,
            limit = perPage,
        )
        return if (official is DataResult.Success) official
        else {
            jikanApiClient.searchMedia(
                mediaType = mediaType,
                query = query,
                page = page,
                limit = perPage,
            )
        }
    }

    suspend fun getUserFavorites(username: String): DataResult<String> =
        jikanApiClient.getUserFavorites(username)

    suspend fun getUserSocial(username: String): DataResult<String> =
        jikanApiClient.getUserSocial(username)
}
