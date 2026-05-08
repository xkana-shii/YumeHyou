package com.axiel7.anihyou.architecture.search

import com.axiel7.anihyou.core.domain.repository.SearchRepository
import org.koin.dsl.module

interface SearchGateway {
    val searchRepository: SearchRepository
}

class AniListSearchGateway(
    override val searchRepository: SearchRepository,
) : SearchGateway

val searchModule = module {
    single<SearchGateway> { AniListSearchGateway(searchRepository = get()) }
}
