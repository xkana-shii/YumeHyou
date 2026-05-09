package com.axiel7.yumehyou.tracker

import android.net.Uri
import com.axiel7.anihyou.core.domain.repository.ActivityRepository
import com.axiel7.anihyou.core.domain.repository.DefaultPreferencesRepository
import com.axiel7.anihyou.core.domain.repository.FavoriteRepository
import com.axiel7.anihyou.core.domain.repository.LoginRepository
import com.axiel7.anihyou.core.domain.repository.MediaListRepository
import com.axiel7.anihyou.core.domain.repository.MediaRepository
import com.axiel7.anihyou.core.domain.repository.SearchRepository
import com.axiel7.anihyou.core.domain.repository.UserRepository
import com.axiel7.anihyou.core.network.fragment.BasicMediaListEntry
import com.axiel7.anihyou.core.network.fragment.FuzzyDate
import com.axiel7.anihyou.core.network.type.ActivitySort
import com.axiel7.anihyou.core.network.type.ActivityType
import com.axiel7.anihyou.core.network.type.MediaListSort
import com.axiel7.anihyou.core.network.type.MediaListStatus
import com.axiel7.anihyou.core.network.type.MediaType
import com.axiel7.yumehyou.core.model.TrackerType

class AniListTrackerAdapter(
    private val loginRepository: LoginRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository,
    val mediaListRepository: MediaListRepository,
    private val mediaRepository: MediaRepository,
    private val userRepository: UserRepository,
    private val activityRepository: ActivityRepository,
    private val favoriteRepository: FavoriteRepository,
    private val searchRepository: SearchRepository,
) : BaseTrackerAdapter() {

    override val trackerType: TrackerType = TrackerType.ANILIST
    override val capabilities: TrackerCapabilities = anilistTrackerCapabilities

    override val isLoggedIn = defaultPreferencesRepository.isLoggedIn

    override suspend fun onAuthRedirect(uri: Uri) =
        loginRepository.parseRedirectUri(uri)

    override suspend fun onNewToken(token: String) =
        loginRepository.onNewToken(token)

    override suspend fun logOut() =
        loginRepository.logOut()

    override fun getLibraryCollection(
        userId: Int,
        mediaType: MediaType,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean,
        chunk: Int?,
        perChunk: Int?,
    ) = mediaListRepository.getMediaListCollection(
        userId = userId,
        mediaType = mediaType,
        sort = sort,
        fetchFromNetwork = fetchFromNetwork,
        chunk = chunk,
        perChunk = perChunk,
    )

    override fun getAnimeList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean,
        page: Int?,
        perPage: Int?,
    ) = mediaListRepository.getUserMediaList(
        userId = userId,
        mediaType = MediaType.ANIME,
        statusIn = statusIn,
        sort = sort,
        fetchFromNetwork = fetchFromNetwork,
        page = page,
        perPage = perPage,
    )

    override fun getMangaList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean,
        page: Int?,
        perPage: Int?,
    ) = mediaListRepository.getUserMediaList(
        userId = userId,
        mediaType = MediaType.MANGA,
        statusIn = statusIn,
        sort = sort,
        fetchFromNetwork = fetchFromNetwork,
        page = page,
        perPage = perPage,
    )

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
    ) = mediaListRepository.updateEntry(
        oldEntry = oldEntry,
        mediaId = mediaId,
        status = status,
        score = score,
        advancedScores = advancedScores,
        progress = progress,
        progressVolumes = progressVolumes,
        startedAt = startedAt,
        completedAt = completedAt,
        repeat = repeat,
        private = private,
        hiddenFromStatusLists = hiddenFromStatusLists,
        notes = notes,
    )

    override fun updateStatus(
        oldEntry: BasicMediaListEntry?,
        mediaId: Int,
        status: MediaListStatus,
    ) = updateEntry(
        oldEntry = oldEntry,
        mediaId = mediaId,
        status = status,
        score = null,
        advancedScores = null,
        progress = null,
        progressVolumes = null,
        startedAt = oldEntry?.startedAt?.fuzzyDate,
        completedAt = oldEntry?.completedAt?.fuzzyDate,
        repeat = null,
        private = null,
        hiddenFromStatusLists = null,
        notes = null,
    )

    override fun updateProgress(
        oldEntry: BasicMediaListEntry?,
        mediaId: Int,
        progress: Int?,
        progressVolumes: Int?,
    ) = updateEntry(
        oldEntry = oldEntry,
        mediaId = mediaId,
        status = null,
        score = null,
        advancedScores = null,
        progress = progress,
        progressVolumes = progressVolumes,
        startedAt = oldEntry?.startedAt?.fuzzyDate,
        completedAt = oldEntry?.completedAt?.fuzzyDate,
        repeat = null,
        private = null,
        hiddenFromStatusLists = null,
        notes = null,
    )

    override fun updateScore(
        oldEntry: BasicMediaListEntry?,
        mediaId: Int,
        score: Double?,
        advancedScores: Collection<Double>?,
    ) = updateEntry(
        oldEntry = oldEntry,
        mediaId = mediaId,
        status = null,
        score = score,
        advancedScores = advancedScores,
        progress = null,
        progressVolumes = null,
        startedAt = oldEntry?.startedAt?.fuzzyDate,
        completedAt = oldEntry?.completedAt?.fuzzyDate,
        repeat = null,
        private = null,
        hiddenFromStatusLists = null,
        notes = null,
    )

    override fun getMyProfile(fetchFromNetwork: Boolean) =
        userRepository.getMyUserInfo(fetchFromNetwork = fetchFromNetwork)

    override fun getProfile(
        userId: Int?,
        username: String?,
        fetchFromNetwork: Boolean,
    ) = userRepository.getUserInfo(
        userId = userId,
        username = username,
        fetchFromNetwork = fetchFromNetwork,
    )

    override fun getActivityFeed(
        isFollowing: Boolean,
        typeIn: List<ActivityType>,
        fetchFromNetwork: Boolean,
        page: Int,
        perPage: Int,
    ) = activityRepository.getActivityFeed(
        isFollowing = isFollowing,
        typeIn = typeIn,
        fetchFromNetwork = fetchFromNetwork,
        page = page,
        perPage = perPage,
    )

    override fun getUserActivity(
        userId: Int,
        sort: List<ActivitySort>,
        fetchFromNetwork: Boolean,
        page: Int,
        perPage: Int,
    ) = userRepository.getUserActivity(
        userId = userId,
        sort = sort,
        fetchFromNetwork = fetchFromNetwork,
        page = page,
        perPage = perPage,
    )

    override fun getFavoriteAnime(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = favoriteRepository.getFavoriteAnime(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    override suspend fun toggleFavorite(
        animeId: Int?,
        mangaId: Int?,
        characterId: Int?,
        staffId: Int?,
        studioId: Int?,
    ) = favoriteRepository.toggleFavorite(
        animeId = animeId,
        mangaId = mangaId,
        characterId = characterId,
        staffId = staffId,
        studioId = studioId,
    )

    override fun getFavoriteManga(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = favoriteRepository.getFavoriteManga(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    override fun getFavoriteCharacters(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = favoriteRepository.getFavoriteCharacters(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    override fun getFavoriteStaff(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = favoriteRepository.getFavoriteStaff(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    override fun getFavoriteStudios(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = favoriteRepository.getFavoriteStudio(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    override fun getFollowers(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = userRepository.getFollowers(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    override fun getFollowing(
        userId: Int,
        page: Int,
        perPage: Int,
        fetchFromNetwork: Boolean,
    ) = userRepository.getFollowing(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    override fun searchMedia(
        mediaType: MediaType,
        query: String,
        page: Int,
        perPage: Int,
    ) = searchRepository.searchMedia(
        mediaType = mediaType,
        query = query,
        page = page,
        perPage = perPage,
    )

    override fun getMediaDetails(mediaId: Int) =
        mediaRepository.getMediaDetails(mediaId = mediaId)

    override fun getMediaActivity(
        mediaId: Int,
        userId: Int?,
        page: Int,
        perPage: Int,
    ) = mediaRepository.getMediaActivityPage(
        mediaId = mediaId,
        userId = userId,
        page = page,
        perPage = perPage,
    )
}
