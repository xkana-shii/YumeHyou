package com.axiel7.yumehyou.settings

import com.axiel7.anihyou.core.domain.repository.DefaultPreferencesRepository
import com.axiel7.anihyou.core.domain.repository.ListPreferencesRepository
import org.koin.dsl.module

interface SettingsGateway {
    val defaultPreferencesRepository: DefaultPreferencesRepository
    val listPreferencesRepository: ListPreferencesRepository
}

class AniListSettingsGateway(
    override val defaultPreferencesRepository: DefaultPreferencesRepository,
    override val listPreferencesRepository: ListPreferencesRepository,
) : SettingsGateway

val settingsModule = module {
    single<SettingsGateway> {
        AniListSettingsGateway(
            defaultPreferencesRepository = get(),
            listPreferencesRepository = get(),
        )
    }
}
