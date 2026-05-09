package com.axiel7.yumehyou.tracker

import com.axiel7.anihyou.core.domain.repository.ActivityRepository
import com.axiel7.anihyou.core.domain.repository.DefaultPreferencesRepository
import com.axiel7.anihyou.core.domain.repository.FavoriteRepository
import com.axiel7.anihyou.core.domain.repository.LoginRepository
import com.axiel7.anihyou.core.domain.repository.MediaListRepository
import com.axiel7.anihyou.core.domain.repository.MediaRepository
import com.axiel7.anihyou.core.domain.repository.SearchRepository
import com.axiel7.anihyou.core.domain.repository.UserRepository
import com.axiel7.yumehyou.core.model.TrackerType
import org.koin.dsl.module

interface TrackerGateway {
    val mediaListRepository: MediaListRepository
    val adapters: List<TrackerAdapter>

    fun getAdapter(trackerType: TrackerType) =
        adapters.firstOrNull { it.trackerType == trackerType }

    fun getCapabilities(trackerType: TrackerType) =
        getAdapter(trackerType)?.capabilities

    fun supports(
        trackerType: TrackerType,
        capability: TrackerCapability,
    ) = getCapabilities(trackerType)?.supports(capability) ?: false
}

class AniListTrackerGateway(
    override val mediaListRepository: MediaListRepository,
    loginRepository: LoginRepository,
    defaultPreferencesRepository: DefaultPreferencesRepository,
    mediaRepository: MediaRepository,
    userRepository: UserRepository,
    activityRepository: ActivityRepository,
    favoriteRepository: FavoriteRepository,
    searchRepository: SearchRepository,
    override val adapters: List<TrackerAdapter> = listOf(
        AniListTrackerAdapter(
            loginRepository = loginRepository,
            defaultPreferencesRepository = defaultPreferencesRepository,
            mediaListRepository = mediaListRepository,
            mediaRepository = mediaRepository,
            userRepository = userRepository,
            activityRepository = activityRepository,
            favoriteRepository = favoriteRepository,
            searchRepository = searchRepository,
        )
    ) + defaultTrackerAdapters,
) : TrackerGateway

val trackerModule = module {
    single<TrackerGateway> {
        AniListTrackerGateway(
            mediaListRepository = get(),
            loginRepository = get(),
            defaultPreferencesRepository = get(),
            mediaRepository = get(),
            userRepository = get(),
            activityRepository = get(),
            favoriteRepository = get(),
            searchRepository = get(),
        )
    }
}
