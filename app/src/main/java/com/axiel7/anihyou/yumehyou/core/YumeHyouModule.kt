package com.axiel7.anihyou.yumehyou.core

import com.axiel7.anihyou.yumehyou.activity.activityModule
import com.axiel7.anihyou.yumehyou.export.exportModule
import com.axiel7.anihyou.yumehyou.metadata.metadataModule
import com.axiel7.anihyou.yumehyou.search.searchModule
import com.axiel7.anihyou.yumehyou.settings.settingsModule
import com.axiel7.anihyou.yumehyou.sync.syncModule
import com.axiel7.anihyou.yumehyou.tracker.trackerModule
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
