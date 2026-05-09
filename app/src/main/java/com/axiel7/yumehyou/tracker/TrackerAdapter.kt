package com.axiel7.yumehyou.tracker

import android.net.Uri
import com.axiel7.anihyou.core.base.DataResult
import com.axiel7.anihyou.core.network.fragment.BasicMediaListEntry
import com.axiel7.anihyou.core.network.fragment.FuzzyDate
import com.axiel7.anihyou.core.network.type.ActivitySort
import com.axiel7.anihyou.core.network.type.ActivityType
import com.axiel7.anihyou.core.network.type.MediaListSort
import com.axiel7.anihyou.core.network.type.MediaListStatus
import com.axiel7.anihyou.core.network.type.MediaType
import com.axiel7.yumehyou.core.model.TrackerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface TrackerAdapter {
    val trackerType: TrackerType
    val capabilities: TrackerCapabilities
    val isLoggedIn: Flow<Boolean>

    suspend fun onAuthRedirect(uri: Uri)

    suspend fun onNewToken(token: String)

    suspend fun logOut()

    fun getLibraryCollection(
        userId: Int,
        mediaType: MediaType,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean = false,
        chunk: Int?,
        perChunk: Int?,
    ): Flow<*>

    fun getAnimeList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean = false,
        page: Int?,
        perPage: Int? = 25,
    ): Flow<*>

    fun getMangaList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean = false,
        page: Int?,
        perPage: Int? = 25,
    ): Flow<*>

    fun updateEntry(
        oldEntry: BasicMediaListEntry? = null,
        mediaId: Int,
        status: MediaListStatus? = null,
        score: Double? = null,
        advancedScores: Collection<Double>? = null,
        progress: Int? = null,
        progressVolumes: Int? = null,
        startedAt: FuzzyDate? = oldEntry?.startedAt?.fuzzyDate,
        completedAt: FuzzyDate? = oldEntry?.completedAt?.fuzzyDate,
        repeat: Int? = null,
        isPrivate: Boolean? = null,
        hiddenFromStatusLists: Boolean? = null,
        notes: String? = null,
    ): Flow<*>

    fun updateStatus(
        oldEntry: BasicMediaListEntry? = null,
        mediaId: Int,
        status: MediaListStatus,
    ): Flow<*>

    fun updateProgress(
        oldEntry: BasicMediaListEntry? = null,
        mediaId: Int,
        progress: Int? = null,
        progressVolumes: Int? = null,
    ): Flow<*>

    fun updateScore(
        oldEntry: BasicMediaListEntry? = null,
        mediaId: Int,
        score: Double? = null,
        advancedScores: Collection<Double>? = null,
    ): Flow<*>

    fun getMyProfile(fetchFromNetwork: Boolean = false): Flow<*>

    fun getProfile(
        userId: Int? = null,
        username: String? = null,
        fetchFromNetwork: Boolean = false,
    ): Flow<*>

    fun getActivityFeed(
        isFollowing: Boolean,
        typeIn: List<ActivityType>,
        fetchFromNetwork: Boolean = false,
        page: Int,
        perPage: Int = 25,
    ): Flow<*>

    fun getUserActivity(
        userId: Int,
        sort: List<ActivitySort> = listOf(ActivitySort.ID_DESC),
        fetchFromNetwork: Boolean = false,
        page: Int,
        perPage: Int = 25,
    ): Flow<*>

    fun getFavoriteAnime(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Flow<*>

    suspend fun toggleFavorite(
        animeId: Int? = null,
        mangaId: Int? = null,
        characterId: Int? = null,
        staffId: Int? = null,
        studioId: Int? = null,
    ): DataResult<*>

    fun getFavoriteManga(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Flow<*>

    fun getFavoriteCharacters(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Flow<*>

    fun getFavoriteStaff(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Flow<*>

    fun getFavoriteStudios(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Flow<*>

    fun getFollowers(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Flow<*>

    fun getFollowing(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Flow<*>

    fun searchMedia(
        mediaType: MediaType,
        query: String,
        page: Int,
        perPage: Int = 25,
    ): Flow<*>

    fun getMediaDetails(mediaId: Int): Flow<*>

    fun getMediaActivity(
        mediaId: Int,
        userId: Int? = null,
        page: Int,
        perPage: Int = 25,
    ): Flow<*>
}

abstract class BaseTrackerAdapter : TrackerAdapter {
    override val isLoggedIn: Flow<Boolean> = flowOf(false)

    private fun <T> unsupported(): T {
        val operation = Throwable().stackTrace
            .firstOrNull { it.className == this::class.qualifiedName && it.methodName != "unsupported" }
            ?.methodName ?: "unknown"
        throw UnsupportedOperationException(
            "Tracker operation '$operation' not supported by adapter ${
                this::class.qualifiedName ?: this::class.toString()
            }"
        )
    }

    override suspend fun onAuthRedirect(uri: Uri) = unsupported()

    override suspend fun onNewToken(token: String) = unsupported()

    override suspend fun logOut() = unsupported()

    override fun getLibraryCollection(
        userId: Int,
        mediaType: MediaType,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean,
        chunk: Int?,
        perChunk: Int?,
    ) = unsupported<Flow<*>>()

    override fun getAnimeList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean,
        page: Int?,
        perPage: Int?,
    ) = unsupported<Flow<*>>()

    override fun getMangaList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean,
        page: Int?,
        perPage: Int?,
    ) = unsupported<Flow<*>>()

    override fun updateEntry(
        oldEntry: BasicMediaListEntry?,
        mediaId: Int,
        status: MediaListStatus?,
        score: Double?,
        advancedScores: Collection<Double>?,
        progress: Int?,
        progressVolumes: Int?,
        startedAt: FuzzyDate?,
        completedAt: FuzzyDate?,
        repeat: Int?,
        isPrivate: Boolean?,
        hiddenFromStatusLists: Boolean?,
        notes: String?,
    ) = unsupported<Flow<*>>()

    override fun updateStatus(
        oldEntry: BasicMediaListEntry?,
        mediaId: Int,
        status: MediaListStatus,
    ) = unsupported<Flow<*>>()

    override fun updateProgress(
        oldEntry: BasicMediaListEntry?,
        mediaId: Int,
        progress: Int?,
        progressVolumes: Int?,
    ) = unsupported<Flow<*>>()

    override fun updateScore(
        oldEntry: BasicMediaListEntry?,
        mediaId: Int,
        score: Double?,
        advancedScores: Collection<Double>?,
    ) = unsupported<Flow<*>>()

    override fun getMyProfile(fetchFromNetwork: Boolean) = unsupported<Flow<*>>()

    override fun getProfile(
        userId: Int?,
        username: String?,
        fetchFromNetwork: Boolean,
    ) = unsupported<Flow<*>>()

    override fun getActivityFeed(
        isFollowing: Boolean,
        typeIn: List<ActivityType>,
        fetchFromNetwork: Boolean,
        page: Int,
        perPage: Int,
    ) = unsupported<Flow<*>>()

    override fun getUserActivity(
        userId: Int,
        sort: List<ActivitySort>,
        fetchFromNetwork: Boolean,
        page: Int,
        perPage: Int,
    ) = unsupported<Flow<*>>()

    override fun getFavoriteAnime(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Flow<*>>()

    override suspend fun toggleFavorite(
        animeId: Int?,
        mangaId: Int?,
        characterId: Int?,
        staffId: Int?,
        studioId: Int?,
    ) = unsupported<DataResult<*>>()

    override fun getFavoriteManga(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Flow<*>>()

    override fun getFavoriteCharacters(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Flow<*>>()

    override fun getFavoriteStaff(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Flow<*>>()

    override fun getFavoriteStudios(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Flow<*>>()

    override fun getFollowers(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Flow<*>>()

    override fun getFollowing(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Flow<*>>()

    override fun searchMedia(
        mediaType: MediaType,
        query: String,
        page: Int,
        perPage: Int,
    ) = unsupported<Flow<*>>()

    override fun getMediaDetails(mediaId: Int) = unsupported<Flow<*>>()

    override fun getMediaActivity(
        mediaId: Int,
        userId: Int?,
        page: Int,
        perPage: Int,
    ) = unsupported<Flow<*>>()
}
