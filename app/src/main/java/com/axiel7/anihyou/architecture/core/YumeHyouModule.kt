package com.axiel7.anihyou.architecture.core

import com.axiel7.anihyou.architecture.activity.activityModule
import com.axiel7.anihyou.architecture.export.exportModule
import com.axiel7.anihyou.architecture.metadata.metadataModule
import com.axiel7.anihyou.architecture.search.searchModule
import com.axiel7.anihyou.architecture.settings.settingsModule
import com.axiel7.anihyou.architecture.sync.syncModule
import com.axiel7.anihyou.architecture.tracker.trackerModule
import org.koin.dsl.module

val yumehyouModule = module {
    includes(
        yumehyouCoreModule,
        metadataModule,
        trackerModule,
        syncModule,
        searchModule,
        activityModule,
        exportModule,
        settingsModule,
    )
}
