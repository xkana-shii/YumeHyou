package com.axiel7.yumehyou.tracker

import com.axiel7.anihyou.core.domain.repository.MediaListRepository
import org.koin.dsl.module

interface TrackerGateway {
    val mediaListRepository: MediaListRepository
}

class AniListTrackerGateway(
    override val mediaListRepository: MediaListRepository,
) : TrackerGateway

val trackerModule = module {
    single<TrackerGateway> { AniListTrackerGateway(mediaListRepository = get()) }
}
