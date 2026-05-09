package com.axiel7.yumehyou.tracker

import android.net.Uri
import com.axiel7.anihyou.core.base.DataResult
import com.axiel7.anihyou.core.network.fragment.BasicMediaListEntry
import com.axiel7.anihyou.core.network.fragment.FuzzyDate
import com.axiel7.anihyou.core.network.type.MediaListSort
import com.axiel7.anihyou.core.network.type.MediaListStatus
import com.axiel7.anihyou.core.network.type.MediaType
import com.axiel7.yumehyou.core.model.TrackerType
import com.axiel7.yumehyou.metadata.MangaMetadataProvider
import com.axiel7.yumehyou.tracker.mangabaka.MangaBakaAuthService
import com.axiel7.yumehyou.tracker.mangabaka.MangaBakaTrackerClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MangaBakaTrackerAdapter(
    private val authService: MangaBakaAuthService,
    private val trackerClient: MangaBakaTrackerClient,
    private val metadataProvider: MangaMetadataProvider,
) : BaseTrackerAdapter() {
    override val trackerType: TrackerType = TrackerType.MANGA_BAKA
    override val capabilities: TrackerCapabilities = mangaBakaTrackerCapabilities
    override val isLoggedIn: Flow<Boolean> = authService.isLoggedIn

    override suspend fun onAuthRedirect(uri: Uri) {
        authService.onAuthRedirect(uri)
    }

    override suspend fun onNewToken(token: String) {
        authService.onNewToken(token)
    }

    override suspend fun logOut() {
        authService.logOut()
    }

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
        resultFlow { DataResult.Error("MangaBaka only supports manga libraries") }
    }

    override fun getMangaList(
        userId: Int,
        statusIn: List<MediaListStatus>?,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean,
        page: Int?,
        perPage: Int?,
    ) = resultFlow {
        trackerClient.getMyLibrary(
            state = statusIn?.firstOrNull()?.toMangaBakaState(),
            page = page ?: 1,
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
        val seriesId = mediaId.toString()
        val normalizedStatus = status?.toMangaBakaState()
        val updateResult = trackerClient.updateLibraryEntry(
            seriesId = seriesId,
            state = normalizedStatus,
            progressChapter = progress,
            progressVolume = progressVolumes,
            rating = score?.toInt(),
            note = notes,
            rereads = repeat,
        )
        if (updateResult is DataResult.Error && oldEntry == null && normalizedStatus != null) {
            val created = trackerClient.createLibraryEntry(seriesId = seriesId, state = normalizedStatus)
            if (created is DataResult.Error) return@resultFlow created
            return@resultFlow trackerClient.updateLibraryEntry(
                seriesId = seriesId,
                state = normalizedStatus,
                progressChapter = progress,
                progressVolume = progressVolumes,
                rating = score?.toInt(),
                note = notes,
                rereads = repeat,
            )
        }
        updateResult
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
        advancedScores = null,
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
        advancedScores = null,
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
        trackerClient.getMyProfile()
    }

    override fun getProfile(
        userId: Int?,
        username: String?,
        fetchFromNetwork: Boolean,
    ) = resultFlow {
        trackerClient.getMyProfile()
    }

    override fun searchMedia(
        mediaType: MediaType,
        query: String,
        page: Int,
        perPage: Int,
    ) = resultFlow {
        if (mediaType != MediaType.MANGA) {
            DataResult.Error("MangaBaka only supports manga search")
        } else {
            metadataProvider.searchManga(query = query, page = page, perPage = perPage)
        }
    }

    override fun getMediaDetails(mediaId: Int) = resultFlow {
        metadataProvider.getMangaDetails(mediaId.toString())
    }

    private fun <T> resultFlow(block: suspend () -> DataResult<T>) = flow {
        emit(block())
    }
}

private fun MediaListStatus.toMangaBakaState(): String {
    return when (this) {
        MediaListStatus.CURRENT -> "reading"
        MediaListStatus.COMPLETED -> "completed"
        MediaListStatus.PAUSED -> "paused"
        MediaListStatus.DROPPED -> "dropped"
        MediaListStatus.PLANNING -> "plan_to_read"
        MediaListStatus.REPEATING -> "rereading"
        else -> "plan_to_read"
    }
}
