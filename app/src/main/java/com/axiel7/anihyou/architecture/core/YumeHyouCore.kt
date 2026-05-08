package com.axiel7.anihyou.architecture.core

import com.axiel7.anihyou.architecture.activity.ActivityGateway
import com.axiel7.anihyou.architecture.export.ExportGateway
import com.axiel7.anihyou.architecture.metadata.MetadataProvider
import com.axiel7.anihyou.architecture.search.SearchGateway
import com.axiel7.anihyou.architecture.settings.SettingsGateway
import com.axiel7.anihyou.architecture.sync.SyncGateway
import com.axiel7.anihyou.architecture.tracker.TrackerGateway
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
