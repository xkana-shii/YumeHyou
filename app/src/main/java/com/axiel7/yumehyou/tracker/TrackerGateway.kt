package com.axiel7.yumehyou.tracker

import com.axiel7.anihyou.core.domain.repository.MediaListRepository
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
    override val adapters: List<TrackerAdapter> = defaultTrackerAdapters,
) : TrackerGateway

val trackerModule = module {
    single<TrackerGateway> { AniListTrackerGateway(mediaListRepository = get()) }
}
