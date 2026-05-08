package com.axiel7.yumehyou.core

import com.axiel7.yumehyou.activity.ActivityGateway
import com.axiel7.yumehyou.export.ExportGateway
import com.axiel7.yumehyou.metadata.MetadataProvider
import com.axiel7.yumehyou.search.SearchGateway
import com.axiel7.yumehyou.settings.SettingsGateway
import com.axiel7.yumehyou.sync.SyncGateway
import com.axiel7.yumehyou.tracker.TrackerGateway
import org.koin.dsl.module

data class YumeHyouLayerInfo(
    val name: String = "YumeHyou",
)

interface YumeHyouLayer {
    val metadataProvider: MetadataProvider
    val trackerGateway: TrackerGateway
    val syncGateway: SyncGateway
    val searchGateway: SearchGateway
    val activityGateway: ActivityGateway
    val exportGateway: ExportGateway
    val settingsGateway: SettingsGateway
}

class DefaultYumeHyouLayer(
    override val metadataProvider: MetadataProvider,
    override val trackerGateway: TrackerGateway,
    override val syncGateway: SyncGateway,
    override val searchGateway: SearchGateway,
    override val activityGateway: ActivityGateway,
    override val exportGateway: ExportGateway,
    override val settingsGateway: SettingsGateway,
) : YumeHyouLayer

val yumehyouCoreModule = module {
    single { YumeHyouLayerInfo() }
    single<YumeHyouLayer> {
        DefaultYumeHyouLayer(
            metadataProvider = get(),
            trackerGateway = get(),
            syncGateway = get(),
            searchGateway = get(),
            activityGateway = get(),
            exportGateway = get(),
            settingsGateway = get(),
        )
    }
}
