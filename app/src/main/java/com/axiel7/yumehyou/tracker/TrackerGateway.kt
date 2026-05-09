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
import com.axiel7.yumehyou.tracker.mal.JikanApiClient
import com.axiel7.yumehyou.tracker.mal.MalApiClient
import com.axiel7.yumehyou.tracker.mal.MalAuthService
import com.axiel7.yumehyou.tracker.mal.MalMetadataProvider
import com.axiel7.yumehyou.tracker.mal.MalSessionStore
import org.koin.dsl.module

interface TrackerManager {
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

data class DefaultTrackerManager(
    override val adapters: List<TrackerAdapter>,
) : TrackerManager

interface TrackerGateway {
    val mediaListRepository: MediaListRepository
    val trackerManager: TrackerManager
    val adapters: List<TrackerAdapter>
        get() = trackerManager.adapters

    fun getAdapter(trackerType: TrackerType) = trackerManager.getAdapter(trackerType)

    fun getCapabilities(trackerType: TrackerType) = trackerManager.getCapabilities(trackerType)

    fun supports(trackerType: TrackerType, capability: TrackerCapability) =
        trackerManager.supports(trackerType, capability)
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
    malAuthService: MalAuthService,
    malApiClient: MalApiClient,
    malMetadataProvider: MalMetadataProvider,
    adapters: List<TrackerAdapter> = listOf(
        AniListTrackerAdapter(
            loginRepository = loginRepository,
            defaultPreferencesRepository = defaultPreferencesRepository,
            mediaListRepository = mediaListRepository,
            mediaRepository = mediaRepository,
            userRepository = userRepository,
            activityRepository = activityRepository,
            favoriteRepository = favoriteRepository,
            searchRepository = searchRepository,
        ),
        MalTrackerAdapter(
            malAuthService = malAuthService,
            malApiClient = malApiClient,
            malMetadataProvider = malMetadataProvider,
        ),
    ) + defaultTrackerAdapters,
    override val trackerManager: TrackerManager = DefaultTrackerManager(adapters),
) : TrackerGateway

val trackerModule = module {
    single { MalSessionStore(dataStore = get()) }
    single { MalAuthService(sessionStore = get(), client = get()) }
    single { MalApiClient(client = get(), authService = get()) }
    single { JikanApiClient(client = get()) }
    single { MalMetadataProvider(malApiClient = get(), jikanApiClient = get()) }

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
            malAuthService = get(),
            malApiClient = get(),
            malMetadataProvider = get(),
        )
    }
}
