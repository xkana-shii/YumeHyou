package com.axiel7.anihyou.architecture.sync

import com.axiel7.anihyou.core.domain.repository.MediaListRepository
import com.axiel7.anihyou.core.domain.repository.UserRepository
import org.koin.dsl.module

interface SyncGateway {
    val userRepository: UserRepository
    val mediaListRepository: MediaListRepository
}

class AniListSyncGateway(
    override val userRepository: UserRepository,
    override val mediaListRepository: MediaListRepository,
) : SyncGateway

val syncModule = module {
    single<SyncGateway> {
        AniListSyncGateway(
            userRepository = get(),
            mediaListRepository = get(),
        )
    }
}
