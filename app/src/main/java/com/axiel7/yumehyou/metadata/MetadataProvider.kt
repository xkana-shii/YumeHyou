package com.axiel7.yumehyou.metadata

import com.axiel7.anihyou.core.base.DataResult
import com.axiel7.anihyou.core.domain.repository.MediaRepository
import com.axiel7.yumehyou.core.model.Tag
import com.axiel7.yumehyou.core.model.UnifiedMedia
import com.axiel7.yumehyou.metadata.mangabaka.MangaBakaMetadataClient
import org.koin.dsl.module

interface MetadataProvider {
    val mediaRepository: MediaRepository
    val preferredMangaProvider: MangaMetadataProvider?
        get() = null
}

interface MangaMetadataProvider {
    suspend fun getMangaDetails(mangaId: String): DataResult<UnifiedMedia>

    suspend fun searchManga(
        query: String,
        page: Int,
        perPage: Int = 20,
        contentRatings: Collection<String> = emptyList(),
        blockedTags: Collection<String> = emptyList(),
    ): DataResult<List<UnifiedMedia>>

    suspend fun getAvailableGenres(): DataResult<List<String>>

    suspend fun getAvailableTags(): DataResult<List<Tag>>
}

class AniListMetadataProvider(
    override val mediaRepository: MediaRepository,
    override val preferredMangaProvider: MangaMetadataProvider? = null,
) : MetadataProvider

val metadataModule = module {
    single { MangaBakaMetadataClient(client = get()) }
    single<MangaMetadataProvider> { MangaBakaMetadataProvider(metadataClient = get()) }
    single<MetadataProvider> {
        AniListMetadataProvider(
            mediaRepository = get(),
            preferredMangaProvider = get(),
        )
    }
}
