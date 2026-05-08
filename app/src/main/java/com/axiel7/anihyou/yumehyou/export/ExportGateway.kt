package com.axiel7.anihyou.yumehyou.export

import com.axiel7.anihyou.core.domain.repository.MediaListRepository
import com.axiel7.anihyou.core.domain.repository.UserRepository
import org.koin.dsl.module

interface ExportGateway {
    val userRepository: UserRepository
    val mediaListRepository: MediaListRepository
}

class AniListExportGateway(
    override val userRepository: UserRepository,
    override val mediaListRepository: MediaListRepository,
) : ExportGateway

val exportModule = module {
    single<ExportGateway> {
        AniListExportGateway(
            userRepository = get(),
            mediaListRepository = get(),
        )
    }
}
