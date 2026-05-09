package com.axiel7.yumehyou.tracker

import android.net.Uri
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
    ): Any

    fun getAnimeList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean = false,
        page: Int?,
        perPage: Int? = 25,
    ): Any

    fun getMangaList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean = false,
        page: Int?,
        perPage: Int? = 25,
    ): Any

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
        private: Boolean? = null,
        hiddenFromStatusLists: Boolean? = null,
        notes: String? = null,
    ): Any

    fun updateStatus(
        oldEntry: BasicMediaListEntry? = null,
        mediaId: Int,
        status: MediaListStatus,
    ): Any

    fun updateProgress(
        oldEntry: BasicMediaListEntry? = null,
        mediaId: Int,
        progress: Int? = null,
        progressVolumes: Int? = null,
    ): Any

    fun updateScore(
        oldEntry: BasicMediaListEntry? = null,
        mediaId: Int,
        score: Double? = null,
        advancedScores: Collection<Double>? = null,
    ): Any

    fun getMyProfile(fetchFromNetwork: Boolean = false): Any

    fun getProfile(
        userId: Int? = null,
        username: String? = null,
        fetchFromNetwork: Boolean = false,
    ): Any

    fun getActivityFeed(
        isFollowing: Boolean,
        typeIn: List<ActivityType>,
        fetchFromNetwork: Boolean = false,
        page: Int,
        perPage: Int = 25,
    ): Any

    fun getUserActivity(
        userId: Int,
        sort: List<ActivitySort> = listOf(ActivitySort.ID_DESC),
        fetchFromNetwork: Boolean = false,
        page: Int,
        perPage: Int = 25,
    ): Any

    fun getFavoriteAnime(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Any

    suspend fun toggleFavorite(
        animeId: Int? = null,
        mangaId: Int? = null,
        characterId: Int? = null,
        staffId: Int? = null,
        studioId: Int? = null,
    ): Any

    fun getFavoriteManga(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Any

    fun getFavoriteCharacters(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Any

    fun getFavoriteStaff(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Any

    fun getFavoriteStudios(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Any

    fun getFollowers(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Any

    fun getFollowing(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ): Any

    fun searchMedia(
        mediaType: MediaType,
        query: String,
        page: Int,
        perPage: Int = 25,
    ): Any

    fun getMediaDetails(mediaId: Int): Any

    fun getMediaActivity(
        mediaId: Int,
        userId: Int? = null,
        page: Int,
        perPage: Int = 25,
    ): Any
}

abstract class BaseTrackerAdapter : TrackerAdapter {
    override val isLoggedIn: Flow<Boolean> = flowOf(false)

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
    ) = unsupported<Any>()

    override fun getAnimeList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean,
        page: Int?,
        perPage: Int?,
    ) = unsupported<Any>()

    override fun getMangaList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean,
        page: Int?,
        perPage: Int?,
    ) = unsupported<Any>()

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
        private: Boolean?,
        hiddenFromStatusLists: Boolean?,
        notes: String?,
    ) = unsupported<Any>()

    override fun updateStatus(
        oldEntry: BasicMediaListEntry?,
        mediaId: Int,
        status: MediaListStatus,
    ) = unsupported<Any>()

    override fun updateProgress(
        oldEntry: BasicMediaListEntry?,
        mediaId: Int,
        progress: Int?,
        progressVolumes: Int?,
    ) = unsupported<Any>()

    override fun updateScore(
        oldEntry: BasicMediaListEntry?,
        mediaId: Int,
        score: Double?,
        advancedScores: Collection<Double>?,
    ) = unsupported<Any>()

    override fun getMyProfile(fetchFromNetwork: Boolean) = unsupported<Any>()

    override fun getProfile(
        userId: Int?,
        username: String?,
        fetchFromNetwork: Boolean,
    ) = unsupported<Any>()

    override fun getActivityFeed(
        isFollowing: Boolean,
        typeIn: List<ActivityType>,
        fetchFromNetwork: Boolean,
        page: Int,
        perPage: Int,
    ) = unsupported<Any>()

    override fun getUserActivity(
        userId: Int,
        sort: List<ActivitySort>,
        fetchFromNetwork: Boolean,
        page: Int,
        perPage: Int,
    ) = unsupported<Any>()

    override fun getFavoriteAnime(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Any>()

    override suspend fun toggleFavorite(
        animeId: Int?,
        mangaId: Int?,
        characterId: Int?,
        staffId: Int?,
        studioId: Int?,
    ) = unsupported<Any>()

    override fun getFavoriteManga(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Any>()

    override fun getFavoriteCharacters(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Any>()

    override fun getFavoriteStaff(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Any>()

    override fun getFavoriteStudios(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Any>()

    override fun getFollowers(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Any>()

    override fun getFollowing(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = unsupported<Any>()

    override fun searchMedia(
        mediaType: MediaType,
        query: String,
        page: Int,
        perPage: Int,
    ) = unsupported<Any>()

    override fun getMediaDetails(mediaId: Int) = unsupported<Any>()

    override fun getMediaActivity(
        mediaId: Int,
        userId: Int?,
        page: Int,
        perPage: Int,
    ) = unsupported<Any>()
}

private fun unsupported(): Nothing =
    throw UnsupportedOperationException("Tracker operation not supported by this adapter")
