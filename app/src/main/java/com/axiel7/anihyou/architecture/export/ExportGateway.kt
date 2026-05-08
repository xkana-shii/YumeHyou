package com.axiel7.anihyou.architecture.export

import com.axiel7.anihyou.core.domain.repository.DefaultPreferencesRepository
import com.axiel7.anihyou.core.domain.repository.UserRepository
import org.koin.dsl.module

interface ExportGateway {
    val userRepository: UserRepository
    val defaultPreferencesRepository: DefaultPreferencesRepository
}

class AniListExportGateway(
    override val userRepository: UserRepository,
    override val defaultPreferencesRepository: DefaultPreferencesRepository,
) : ExportGateway

val exportModule = module {
    single<ExportGateway> {
        AniListExportGateway(
            userRepository = get(),
            defaultPreferencesRepository = get(),
        )
    }
}
