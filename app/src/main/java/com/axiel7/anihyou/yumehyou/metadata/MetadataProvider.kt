package com.axiel7.anihyou.yumehyou.metadata

import com.axiel7.anihyou.core.domain.repository.MediaRepository
import org.koin.dsl.module

interface MetadataProvider {
    val mediaRepository: MediaRepository
}

class AniListMetadataProvider(
    override val mediaRepository: MediaRepository,
) : MetadataProvider

val metadataModule = module {
    single<MetadataProvider> { AniListMetadataProvider(mediaRepository = get()) }
}
