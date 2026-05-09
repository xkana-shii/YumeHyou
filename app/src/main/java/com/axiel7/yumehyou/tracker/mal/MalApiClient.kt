package com.axiel7.yumehyou.tracker.mal

import com.axiel7.anihyou.core.base.DataResult
import com.axiel7.anihyou.core.base.MAL_API_URL
import com.axiel7.anihyou.core.network.type.MediaType
import com.axiel7.anihyou.core.network.type.MediaListStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class MalApiClient(
    private val client: OkHttpClient,
    private val authService: MalAuthService,
) {
    suspend fun getMyProfile(): DataResult<String> =
        authenticatedGet(
            path = "users/@me",
            query = mapOf(
                "fields" to "id,name,picture,anime_statistics,manga_statistics",
            ),
        )

    suspend fun getProfile(username: String): DataResult<String> =
        authenticatedGet(
            path = "users/$username",
            query = mapOf(
                "fields" to "id,name,picture,anime_statistics,manga_statistics",
            ),
        )

    suspend fun getMyAnimeList(
        status: MediaListStatus?,
        offset: Int,
        limit: Int,
    ): DataResult<String> = authenticatedGet(
        path = "users/@me/animelist",
        query = mapOf(
            "status" to status?.toMalStatus(MediaType.ANIME),
            "offset" to offset.toString(),
            "limit" to limit.toString(),
            "fields" to "list_status,num_episodes,media_type,status,start_date,alternative_titles,synopsis,mean,genres,nsfw",
            "sort" to "list_updated_at",
        ),
    )

    suspend fun getMyMangaList(
        status: MediaListStatus?,
        offset: Int,
        limit: Int,
    ): DataResult<String> = authenticatedGet(
        path = "users/@me/mangalist",
        query = mapOf(
            "status" to status?.toMalStatus(MediaType.MANGA),
            "offset" to offset.toString(),
            "limit" to limit.toString(),
            "fields" to "list_status,num_chapters,num_volumes,media_type,status,start_date,alternative_titles,synopsis,mean,genres,nsfw",
            "sort" to "list_updated_at",
        ),
    )

    suspend fun updateAnimeListEntry(
        animeId: Int,
        status: MediaListStatus?,
        score: Double?,
        progress: Int?,
        repeat: Int?,
        notes: String?,
    ): DataResult<String> {
        val body = FormBody.Builder().apply {
            status?.toMalStatus(MediaType.ANIME)?.let { add("status", it) }
            score?.let { add("score", it.toString()) }
            progress?.let { add("num_watched_episodes", it.toString()) }
            repeat?.let { add("num_times_rewatched", it.toString()) }
            if (!notes.isNullOrBlank()) add("comments", notes)
        }.build()
        return authenticatedPut(path = "anime/$animeId/my_list_status", body = body)
    }

    suspend fun updateMangaListEntry(
        mangaId: Int,
        status: MediaListStatus?,
        score: Double?,
        progress: Int?,
        progressVolumes: Int?,
        repeat: Int?,
        notes: String?,
    ): DataResult<String> {
        val body = FormBody.Builder().apply {
            status?.toMalStatus(MediaType.MANGA)?.let { add("status", it) }
            score?.let { add("score", it.toString()) }
            progress?.let { add("num_chapters_read", it.toString()) }
            progressVolumes?.let { add("num_volumes_read", it.toString()) }
            repeat?.let { add("num_times_reread", it.toString()) }
            if (!notes.isNullOrBlank()) add("comments", notes)
        }.build()
        return authenticatedPut(path = "manga/$mangaId/my_list_status", body = body)
    }

    suspend fun searchMedia(
        mediaType: MediaType,
        query: String,
        offset: Int,
        limit: Int,
    ): DataResult<String> {
        val endpoint = if (mediaType == MediaType.MANGA) "manga" else "anime"
        return authenticatedGet(
            path = endpoint,
            query = mapOf(
                "q" to query,
                "offset" to offset.toString(),
                "limit" to limit.toString(),
                "fields" to "id,title,main_picture,media_type,num_episodes,num_chapters,status,synopsis,mean,genres,nsfw",
            ),
        )
    }

    suspend fun getAnimeDetails(animeId: Int): DataResult<String> =
        authenticatedGet(
            path = "anime/$animeId",
            query = mapOf(
                "fields" to "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity,media_type,status,genres,num_episodes,start_season,broadcast,pictures,background,related_anime,related_manga,recommendations,studios,statistics,average_episode_duration,rating",
            ),
        )

    suspend fun getMangaDetails(mangaId: Int): DataResult<String> =
        authenticatedGet(
            path = "manga/$mangaId",
            query = mapOf(
                "fields" to "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity,media_type,status,genres,num_chapters,num_volumes,pictures,background,related_anime,related_manga,recommendations,authors{first_name,last_name},serialization{name}",
            ),
        )

    private suspend fun authenticatedGet(
        path: String,
        query: Map<String, String?>,
    ): DataResult<String> = withContext(Dispatchers.IO) {
        val token = authService.getValidAccessToken()
            ?: return@withContext DataResult.Error("Missing MAL access token")

        val baseUrl = "${MAL_API_URL}$path".toHttpUrl()
        val url = baseUrl.newBuilder().apply {
            query.forEach { (key, value) ->
                if (!value.isNullOrBlank()) addQueryParameter(key, value)
            }
        }.build()

        executeRequest(
            request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer $token")
                .build(),
        )
    }

    private suspend fun authenticatedPut(
        path: String,
        body: FormBody,
    ): DataResult<String> = withContext(Dispatchers.IO) {
        val token = authService.getValidAccessToken()
            ?: return@withContext DataResult.Error("Missing MAL access token")

        executeRequest(
            request = Request.Builder()
                .url("${MAL_API_URL}$path")
                .put(body)
                .addHeader("Authorization", "Bearer $token")
                .build(),
        )
    }

    private fun executeRequest(request: Request): DataResult<String> {
        return runCatching {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    DataResult.Error(message = body.ifBlank { "MAL request failed (${response.code})" })
                } else {
                    DataResult.Success(body)
                }
            }
        }.getOrElse {
            DataResult.Error(message = it.message ?: "MAL request failed")
        }
    }
}

private fun MediaListStatus.toMalStatus(mediaType: MediaType): String {
    return when (this) {
        MediaListStatus.CURRENT -> if (mediaType == MediaType.MANGA) "reading" else "watching"
        MediaListStatus.COMPLETED -> "completed"
        MediaListStatus.PAUSED -> "on_hold"
        MediaListStatus.DROPPED -> "dropped"
        MediaListStatus.PLANNING -> if (mediaType == MediaType.MANGA) "plan_to_read" else "plan_to_watch"
        MediaListStatus.REPEATING -> if (mediaType == MediaType.MANGA) "reading" else "watching"
    }
}
