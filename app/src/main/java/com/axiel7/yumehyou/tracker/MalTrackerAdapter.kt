package com.axiel7.yumehyou.tracker

import android.net.Uri
import com.axiel7.anihyou.core.base.DataResult
import com.axiel7.anihyou.core.network.fragment.BasicMediaListEntry
import com.axiel7.anihyou.core.network.fragment.FuzzyDate
import com.axiel7.anihyou.core.network.type.MediaListSort
import com.axiel7.anihyou.core.network.type.MediaListStatus
import com.axiel7.anihyou.core.network.type.MediaType
import com.axiel7.yumehyou.core.model.TrackerType
import com.axiel7.yumehyou.tracker.mal.MalApiClient
import com.axiel7.yumehyou.tracker.mal.MalAuthService
import com.axiel7.yumehyou.tracker.mal.MalMetadataProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MalTrackerAdapter(
    private val malAuthService: MalAuthService,
    private val malApiClient: MalApiClient,
    private val malMetadataProvider: MalMetadataProvider,
) : BaseTrackerAdapter() {
    override val trackerType: TrackerType = TrackerType.MY_ANIME_LIST
    override val capabilities: TrackerCapabilities = malTrackerCapabilities

    override val isLoggedIn: Flow<Boolean> = malAuthService.isLoggedIn

    override suspend fun onAuthRedirect(uri: Uri) =
        malAuthService.onAuthRedirect(uri)

    override suspend fun onNewToken(token: String) =
        malAuthService.onNewToken(token)

    override suspend fun logOut() =
        malAuthService.logOut()

    override fun getLibraryCollection(
        userId: Int,
        mediaType: MediaType,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean,
        chunk: Int?,
        perChunk: Int?,
    ) = if (mediaType == MediaType.MANGA) {
        getMangaList(
            userId = userId,
            statusIn = null,
            sort = sort,
            fetchFromNetwork = fetchFromNetwork,
            page = chunk,
            perPage = perChunk,
        )
    } else {
        getAnimeList(
            userId = userId,
            statusIn = null,
            sort = sort,
            fetchFromNetwork = fetchFromNetwork,
            page = chunk,
            perPage = perChunk,
        )
    }

    override fun getAnimeList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean,
        page: Int?,
        perPage: Int?,
    ) = resultFlow {
        malApiClient.getMyAnimeList(
            status = statusIn?.firstOrNull(),
            offset = ((page ?: 1) - 1).coerceAtLeast(0) * (perPage ?: 25),
            limit = perPage ?: 25,
        )
    }

    override fun getMangaList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean,
        page: Int?,
        perPage: Int?,
    ) = resultFlow {
        malApiClient.getMyMangaList(
            status = statusIn?.firstOrNull(),
            offset = ((page ?: 1) - 1).coerceAtLeast(0) * (perPage ?: 25),
            limit = perPage ?: 25,
        )
    }

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
    ) = resultFlow {
        when (resolveMediaType(oldEntry = oldEntry, progressVolumes = progressVolumes)) {
            MediaType.MANGA -> malApiClient.updateMangaListEntry(
                mangaId = mediaId,
                status = status,
                score = score,
                progress = progress,
                progressVolumes = progressVolumes,
                repeat = repeat,
                notes = notes,
            )

            MediaType.ANIME -> malApiClient.updateAnimeListEntry(
                animeId = mediaId,
                status = status,
                score = score,
                progress = progress,
                repeat = repeat,
                notes = notes,
            )
        }
    }

    override fun updateStatus(
        oldEntry: BasicMediaListEntry?,
        mediaId: Int,
        status: MediaListStatus,
    ) = updateEntry(
        oldEntry = oldEntry,
        mediaId = mediaId,
        status = status,
        score = oldEntry?.score,
        advancedScores = oldEntry?.advancedScores,
        progress = oldEntry?.progress,
        progressVolumes = oldEntry?.progressVolumes,
        startedAt = oldEntry?.startedAt?.fuzzyDate,
        completedAt = oldEntry?.completedAt?.fuzzyDate,
        repeat = oldEntry?.repeat,
        isPrivate = oldEntry?.private,
        hiddenFromStatusLists = oldEntry?.hiddenFromStatusLists,
        notes = oldEntry?.notes,
    )

    override fun updateProgress(
        oldEntry: BasicMediaListEntry?,
        mediaId: Int,
        progress: Int?,
        progressVolumes: Int?,
    ) = updateEntry(
        oldEntry = oldEntry,
        mediaId = mediaId,
        status = oldEntry?.status,
        score = oldEntry?.score,
        advancedScores = oldEntry?.advancedScores,
        progress = progress,
        progressVolumes = progressVolumes,
        startedAt = oldEntry?.startedAt?.fuzzyDate,
        completedAt = oldEntry?.completedAt?.fuzzyDate,
        repeat = oldEntry?.repeat,
        isPrivate = oldEntry?.private,
        hiddenFromStatusLists = oldEntry?.hiddenFromStatusLists,
        notes = oldEntry?.notes,
    )

    override fun updateScore(
        oldEntry: BasicMediaListEntry?,
        mediaId: Int,
        score: Double?,
        advancedScores: Collection<Double>?,
    ) = updateEntry(
        oldEntry = oldEntry,
        mediaId = mediaId,
        status = oldEntry?.status,
        score = score,
        advancedScores = advancedScores,
        progress = oldEntry?.progress,
        progressVolumes = oldEntry?.progressVolumes,
        startedAt = oldEntry?.startedAt?.fuzzyDate,
        completedAt = oldEntry?.completedAt?.fuzzyDate,
        repeat = oldEntry?.repeat,
        isPrivate = oldEntry?.private,
        hiddenFromStatusLists = oldEntry?.hiddenFromStatusLists,
        notes = oldEntry?.notes,
    )

    override fun getMyProfile(fetchFromNetwork: Boolean) = resultFlow {
        malApiClient.getMyProfile()
    }

    override fun getProfile(
        userId: Int?,
        username: String?,
        fetchFromNetwork: Boolean,
    ) = resultFlow {
        if (username.isNullOrBlank()) malApiClient.getMyProfile()
        else malApiClient.getProfile(username)
    }

    override fun getFavoriteAnime(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = resultFlow {
        fetchFavoritesForCurrentUser()
    }

    override suspend fun toggleFavorite(
        animeId: Int?,
        mangaId: Int?,
        characterId: Int?,
        staffId: Int?,
        studioId: Int?,
    ): DataResult<*> = DataResult.Error("MyAnimeList favorite updates are not supported by official MAL API")

    override fun getFavoriteManga(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = resultFlow {
        fetchFavoritesForCurrentUser()
    }

    override fun getFavoriteCharacters(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = resultFlow {
        fetchFavoritesForCurrentUser()
    }

    override fun getFavoriteStaff(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = resultFlow {
        fetchFavoritesForCurrentUser()
    }

    override fun getFavoriteStudios(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = resultFlow {
        fetchFavoritesForCurrentUser()
    }

    override fun getFollowers(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = resultFlow {
        fetchSocialForCurrentUser()
    }

    override fun getFollowing(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = resultFlow {
        fetchSocialForCurrentUser()
    }

    override fun searchMedia(
        mediaType: MediaType,
        query: String,
        page: Int,
        perPage: Int,
    ) = resultFlow {
        malMetadataProvider.searchMedia(
            mediaType = mediaType,
            query = query,
            page = page,
            perPage = perPage,
        )
    }

    override fun getMediaDetails(mediaId: Int) = resultFlow {
        malMetadataProvider.getMediaDetails(mediaId)
    }

    private fun resolveMediaType(
        oldEntry: BasicMediaListEntry?,
        progressVolumes: Int?,
    ): MediaType {
        return oldEntry?.media?.type ?: if (progressVolumes != null) MediaType.MANGA else MediaType.ANIME
    }

    private suspend fun fetchFavoritesForCurrentUser(): DataResult<String> {
        val username = resolveCurrentUsername()
            ?: return DataResult.Error("Could not resolve MAL username for favorites")
        return malMetadataProvider.getUserFavorites(username)
    }

    private suspend fun fetchSocialForCurrentUser(): DataResult<String> {
        val username = resolveCurrentUsername()
            ?: return DataResult.Error("Could not resolve MAL username for social data")
        return malMetadataProvider.getUserSocial(username)
    }

    private suspend fun resolveCurrentUsername(): String? {
        val profile = malApiClient.getMyProfile()
        if (profile !is DataResult.Success<*>) return null
        val profileBody = profile.data as? String ?: return null
        val match = NAME_REGEX.find(profileBody)
        return match?.groupValues?.getOrNull(1)
    }

    private fun resultFlow(block: suspend () -> DataResult<String>) = flow {
        emit(block())
    }

    companion object {
        private val NAME_REGEX = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"")
    }
}
