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
) : TrackerAdapter {

    override val trackerType: TrackerType = TrackerType.ANILIST
    override val capabilities: TrackerCapabilities = anilistTrackerCapabilities

    val isLoggedIn = defaultPreferencesRepository.isLoggedIn

    suspend fun onAuthRedirect(uri: Uri) =
        loginRepository.parseRedirectUri(uri)

    suspend fun onNewToken(token: String) =
        loginRepository.onNewToken(token)

    suspend fun logOut() =
        loginRepository.logOut()

    fun getLibraryCollection(
        userId: Int,
        mediaType: MediaType,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean = false,
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

    fun getAnimeList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean = false,
        page: Int?,
        perPage: Int? = 25,
    ) = mediaListRepository.getUserMediaList(
        userId = userId,
        mediaType = MediaType.ANIME,
        statusIn = statusIn,
        sort = sort,
        fetchFromNetwork = fetchFromNetwork,
        page = page,
        perPage = perPage,
    )

    fun getMangaList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean = false,
        page: Int?,
        perPage: Int? = 25,
    ) = mediaListRepository.getUserMediaList(
        userId = userId,
        mediaType = MediaType.MANGA,
        statusIn = statusIn,
        sort = sort,
        fetchFromNetwork = fetchFromNetwork,
        page = page,
        perPage = perPage,
    )

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

    fun updateStatus(
        oldEntry: BasicMediaListEntry? = null,
        mediaId: Int,
        status: MediaListStatus,
    ) = updateEntry(
        oldEntry = oldEntry,
        mediaId = mediaId,
        status = status,
    )

    fun updateProgress(
        oldEntry: BasicMediaListEntry? = null,
        mediaId: Int,
        progress: Int? = null,
        progressVolumes: Int? = null,
    ) = updateEntry(
        oldEntry = oldEntry,
        mediaId = mediaId,
        progress = progress,
        progressVolumes = progressVolumes,
    )

    fun updateScore(
        oldEntry: BasicMediaListEntry? = null,
        mediaId: Int,
        score: Double? = null,
        advancedScores: Collection<Double>? = null,
    ) = updateEntry(
        oldEntry = oldEntry,
        mediaId = mediaId,
        score = score,
        advancedScores = advancedScores,
    )

    fun getMyProfile(fetchFromNetwork: Boolean = false) =
        userRepository.getMyUserInfo(fetchFromNetwork = fetchFromNetwork)

    fun getProfile(
        userId: Int? = null,
        username: String? = null,
        fetchFromNetwork: Boolean = false,
    ) = userRepository.getUserInfo(
        userId = userId,
        username = username,
        fetchFromNetwork = fetchFromNetwork,
    )

    fun getActivityFeed(
        isFollowing: Boolean,
        typeIn: List<ActivityType>,
        fetchFromNetwork: Boolean = false,
        page: Int,
        perPage: Int = 25,
    ) = activityRepository.getActivityFeed(
        isFollowing = isFollowing,
        typeIn = typeIn,
        fetchFromNetwork = fetchFromNetwork,
        page = page,
        perPage = perPage,
    )

    fun getUserActivity(
        userId: Int,
        sort: List<ActivitySort> = listOf(ActivitySort.ID_DESC),
        fetchFromNetwork: Boolean = false,
        page: Int,
        perPage: Int = 25,
    ) = userRepository.getUserActivity(
        userId = userId,
        sort = sort,
        fetchFromNetwork = fetchFromNetwork,
        page = page,
        perPage = perPage,
    )

    fun getFavoriteAnime(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ) = favoriteRepository.getFavoriteAnime(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    suspend fun toggleFavorite(
        animeId: Int? = null,
        mangaId: Int? = null,
        characterId: Int? = null,
        staffId: Int? = null,
        studioId: Int? = null,
    ) = favoriteRepository.toggleFavorite(
        animeId = animeId,
        mangaId = mangaId,
        characterId = characterId,
        staffId = staffId,
        studioId = studioId,
    )

    fun getFavoriteManga(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ) = favoriteRepository.getFavoriteManga(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    fun getFavoriteCharacters(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ) = favoriteRepository.getFavoriteCharacters(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    fun getFavoriteStaff(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ) = favoriteRepository.getFavoriteStaff(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    fun getFavoriteStudios(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ) = favoriteRepository.getFavoriteStudio(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    fun getFollowers(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ) = userRepository.getFollowers(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    fun getFollowing(
        userId: Int,
        page: Int,
        perPage: Int = 25,
        fetchFromNetwork: Boolean,
    ) = userRepository.getFollowing(
        userId = userId,
        page = page,
        perPage = perPage,
        fetchFromNetwork = fetchFromNetwork,
    )

    fun searchMedia(
        mediaType: MediaType,
        query: String,
        page: Int,
        perPage: Int = 25,
    ) = searchRepository.searchMedia(
        mediaType = mediaType,
        query = query,
        page = page,
        perPage = perPage,
    )

    fun getMediaDetails(mediaId: Int) =
        mediaRepository.getMediaDetails(mediaId = mediaId)

    fun getMediaActivity(
        mediaId: Int,
        userId: Int? = null,
        page: Int,
        perPage: Int = 25,
    ) = mediaRepository.getMediaActivityPage(
        mediaId = mediaId,
        userId = userId,
        page = page,
        perPage = perPage,
    )
}
