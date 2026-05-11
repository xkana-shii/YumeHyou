package com.axiel7.anihyou.core.domain.repository

import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.axiel7.anihyou.core.model.media.AnimeSeason
import com.axiel7.anihyou.core.model.media.AnimeThemes
import com.axiel7.anihyou.core.model.media.AnimeThemes.Companion.toBo
import com.axiel7.anihyou.core.model.media.MangaBakaCollection
import com.axiel7.anihyou.core.model.media.MangaBakaExternalLink
import com.axiel7.anihyou.core.model.media.MangaBakaMetadata
import com.axiel7.anihyou.core.model.media.MangaBakaRelatedEntry
import com.axiel7.anihyou.core.model.media.MangaBakaTrackerMapping
import com.axiel7.anihyou.core.model.media.MangaBakaWork
import com.axiel7.anihyou.core.model.media.ChartType
import com.axiel7.anihyou.core.model.media.MediaCharactersAndStaff
import com.axiel7.anihyou.core.model.media.MediaRelationsAndRecommendations
import com.axiel7.anihyou.core.model.media.isActive
import com.axiel7.anihyou.core.network.MediaDetailsQuery
import com.axiel7.anihyou.core.network.AiringAnimesQuery
import com.axiel7.anihyou.core.network.api.MalApi
import com.axiel7.anihyou.core.network.api.MangaBakaApi
import com.axiel7.anihyou.core.network.api.MediaApi
import com.axiel7.anihyou.core.base.DataResult
import com.axiel7.anihyou.core.network.type.AiringSort
import com.axiel7.anihyou.core.network.type.MediaSort
import com.axiel7.anihyou.core.network.type.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MediaRepository (
    private val api: MediaApi,
    private val malApi: MalApi,
    private val mangaBakaApi: MangaBakaApi,
    defaultPreferencesRepository: DefaultPreferencesRepository,
) : BaseNetworkRepository(defaultPreferencesRepository) {

    fun getAiringAnimesPage(
        airingAtGreater: Long? = null,
        airingAtLesser: Long? = null,
        sort: List<AiringSort> = listOf(AiringSort.TIME),
        onMyList: Boolean? = null,
        isAdult: Boolean = false,
        page: Int,
        perPage: Int = 25,
    ) = api
        .airingAnimesQuery(
            airingAtGreater = airingAtGreater,
            airingAtLesser = airingAtLesser,
            sort = sort,
            page = page,
            perPage = perPage,
        )
        .toFlow()
        .asPagedResult(page = { it.Page?.pageInfo?.commonPage }) { data ->
            val list = data.Page?.airingSchedules?.filterNotNull().orEmpty()
            fun AiringAnimesQuery.AiringSchedule.adultFilter() =
                if (!isAdult) media?.isAdult == false else true
            when (onMyList) {
                true -> list.filter { it.media?.mediaListEntry != null && it.adultFilter() }
                false -> list.filter { it.media?.mediaListEntry == null && it.adultFilter() }
                null -> list.filter { it.adultFilter() }
            }
        }

    fun getAiringAnimeOnMyListPage(
        page: Int,
        perPage: Int = 25,
    ) = api
        .airingOnMyListQuery(page, perPage)
        .toFlow()
        .asPagedResult(page = { it.Page?.pageInfo?.commonPage }) { data ->
            data.Page?.media?.filterNotNull()
                ?.filter {
                    it.nextAiringEpisode != null
                            && it.mediaListEntry?.basicMediaListEntry?.status?.isActive() == true
                }
                ?.sortedBy { it.nextAiringEpisode?.timeUntilAiring }
                .orEmpty()
        }

    fun getSeasonalAnimePage(
        animeSeason: AnimeSeason,
        sort: List<MediaSort> = listOf(MediaSort.POPULARITY_DESC),
        isAdult: Boolean? = null,
        page: Int,
        perPage: Int = 25,
    ) = api
        .seasonalAnimeQuery(animeSeason.toDto(), sort, isAdult, page, perPage)
        .toFlow()
        .asPagedResult(page = { it.Page?.pageInfo?.commonPage }) {
            it.Page?.media?.filterNotNull().orEmpty()
        }

    fun getMediaSortedPage(
        mediaType: MediaType,
        sort: List<MediaSort>,
        isAdult: Boolean? = null,
        page: Int,
        perPage: Int = 25,
    ) = api
        .mediaSortedQuery(mediaType, sort, isAdult, page, perPage)
        .toFlow()
        .asPagedResult(page = { it.Page?.pageInfo?.commonPage }) {
            it.Page?.media?.filterNotNull().orEmpty()
        }

    fun getMediaChartPage(
        type: ChartType,
        isAdult: Boolean? = null,
        page: Int,
        perPage: Int = 25,
    ) = api
        .mediaChartQuery(
            type = type.mediaType,
            sort = listOf(type.mediaSort),
            status = type.mediaStatus,
            format = type.mediaFormat,
            isAdult = isAdult,
            page = page,
            perPage = perPage
        )
        .toFlow()
        .asPagedResult(page = { it.Page?.pageInfo?.commonPage }) {
            it.Page?.media?.filterNotNull().orEmpty()
        }

    fun getMediaDetails(mediaId: Int) = api
        .mediaDetailsQuery(mediaId)
        .toFlow()
        .asDataResult { it.Media }

    suspend fun updateMediaDetailsCache(media: MediaDetailsQuery.Media) {
        api.updateMediaDetailsCache(
            data = MediaDetailsQuery.Data(media)
        )
    }

    fun getMediaCharactersAndStaff(mediaId: Int) = api
        .mediaCharactersAndStaffQuery(mediaId)
        .toFlow()
        .asDataResult {
            MediaCharactersAndStaff(
                characters = it.Media?.characters?.edges?.filterNotNull().orEmpty(),
                staff = it.Media?.staff?.edges?.filterNotNull().orEmpty()
            )
        }

    fun getMediaRelationsAndRecommendations(mediaId: Int) = api
        .mediaRelationsAndRecommendationsQuery(mediaId)
        .toFlow()
        .asDataResult {
            MediaRelationsAndRecommendations(
                relations = it.Media?.relations?.edges?.filterNotNull().orEmpty(),
                recommendations = it.Media?.recommendations?.nodes?.filterNotNull().orEmpty()
            )
        }

    fun getMediaStats(mediaId: Int) = api
        .mediaStatsQuery(mediaId)
        .toFlow()
        .asDataResult { it.Media }

    fun getMediaFollowing(
        mediaId: Int,
        page: Int,
        perPage: Int = 25,
    ) = api
        .mediaFollowingQuery(mediaId, page, perPage)
        .toFlow()
        .asPagedResult(page = { it.Page?.pageInfo?.commonPage }) {
            it.Page?.mediaList?.filterNotNull().orEmpty()
        }

    fun getMediaReviewsPage(
        mediaId: Int,
        page: Int,
        perPage: Int = 25,
    ) = api
        .mediaReviewsQuery(mediaId, page, perPage)
        .toFlow()
        .asPagedResult(page = { it.Media?.reviews?.pageInfo?.commonPage }) {
            it.Media?.reviews?.nodes?.filterNotNull().orEmpty()
        }

    fun getMediaThreadsPage(
        mediaId: Int,
        page: Int,
        perPage: Int = 25,
    ) = api
        .mediaThreadsQuery(mediaId, page, perPage)
        .toFlow()
        .asPagedResult(page = { it.Page?.pageInfo?.commonPage }) {
            it.Page?.threads?.filterNotNull().orEmpty()
        }

    fun getMediaActivityPage(
        mediaId: Int,
        userId: Int? = null,
        page: Int,
        perPage: Int = 25,
    ) = api
        .mediaActivityQuery(mediaId, userId, page, perPage)
        .toFlow()
        .asPagedResult(page = { it.Page?.pageInfo?.commonPage }) { data ->
            data.Page?.activities?.mapNotNull { it?.listActivityFragment }.orEmpty()
        }

    fun getBasicMediaDetails(mediaId: Int) = api
        .basicMediaDetails(mediaId)
        .toFlow()
        .asDataResult { it.Media?.mediaListEntry?.commonMediaListEntry }

    // widget

    suspend fun getAiringWidgetData(
        page: Int,
        perPage: Int = 25,
    ) = api
        .airingWidgetQuery(page, perPage)
        .fetchPolicy(FetchPolicy.NetworkFirst)
        .execute()
        .asDataResult { data ->
            data.Page?.media?.filterNotNull()
                ?.filter {
                        it.nextAiringEpisode != null
                                && it.mediaListEntry?.status?.isActive() == true
                    }
                ?.sortedBy { it.nextAiringEpisode?.timeUntilAiring }
                .orEmpty()
        }

    // MyAnimeList endpoints

    suspend fun getAnimeThemes(idMal: Int) = withContext(Dispatchers.IO) {
        malApi.getAnimeThemes(idMal)?.let { result ->
            AnimeThemes(
                openingThemes = result.openingThemes?.map { it.toBo() },
                endingThemes = result.endingThemes?.map { it.toBo() },
            )
        }
    }

    fun getMangaBakaMetadata(details: MediaDetailsQuery.Media): Flow<DataResult<MangaBakaMetadata>> = flow {
        if (details.basicMediaDetails.type != MediaType.MANGA) {
            emit(DataResult.Error("MangaBaka metadata is only available for manga"))
            return@flow
        }

        val resolvedSeriesId = resolveMangaBakaSeriesId(details)
        if (resolvedSeriesId.isNullOrBlank()) {
            emit(DataResult.Error("Could not resolve MangaBaka series for this manga"))
            return@flow
        }

        val series = mangaBakaApi.getSeries(resolvedSeriesId)?.toJsonObject()
            ?.optJSONObject("data")
        if (series == null) {
            emit(DataResult.Error("Could not load MangaBaka series details"))
            return@flow
        }

        val links = mangaBakaApi.getSeriesLinks(resolvedSeriesId)
            .toDataArrayOrEmpty()
        val collections = mangaBakaApi.getSeriesCollections(resolvedSeriesId)
            .toDataArrayOrEmpty()
        val works = mangaBakaApi.getSeriesWorks(resolvedSeriesId)
            .toDataArrayOrEmpty()
        val related = mangaBakaApi.getSeriesRelated(resolvedSeriesId)
            .toDataArrayOrEmpty()

        emit(
            DataResult.Success(
                mapMangaBakaMetadata(
                    series = series,
                    links = links,
                    collections = collections,
                    works = works,
                    related = related,
                ),
            ),
        )
    }

    private fun resolveMangaBakaSeriesId(details: MediaDetailsQuery.Media): String? {
        val sourceTitles = listOf(
            details.title?.userPreferred,
            details.title?.english,
            details.title?.romaji,
            details.title?.native,
        ).filterNotNull().filter { it.isNotBlank() }
        val anilistId = details.id
        val malId = details.idMal

        val fromExternalLinks = details.externalLinks.orEmpty()
            .firstNotNullOfOrNull { link ->
                val site = link?.site?.lowercase().orEmpty()
                if ("mangabaka" in site) link?.url?.extractLastPathSegment() else null
            }
        if (!fromExternalLinks.isNullOrBlank()) {
            return fromExternalLinks
        }

        for (title in sourceTitles) {
            val search = mangaBakaApi.searchSeries(query = title, page = 1, perPage = 20)
                ?.toJsonObject()
                ?.optJSONArray("data")
                ?: continue
            val byLinkMatch = search.firstMatchingSeriesId { entry ->
                val sourceUrl = entry.optJSONObject("source")?.optString("url").orEmpty().lowercase()
                sourceUrl.contains("/manga/$anilistId")
                    || (malId != null && sourceUrl.contains("/manga/$malId"))
            }
            if (!byLinkMatch.isNullOrBlank()) return byLinkMatch

            val byTitleMatch = search.firstMatchingSeriesId { entry ->
                val preferred = entry.optString("title").trim()
                val romanized = entry.optString("romanized_title").trim()
                val native = entry.optString("native_title").trim()
                sourceTitles.any { candidate ->
                    candidate.equals(preferred, ignoreCase = true)
                        || candidate.equals(romanized, ignoreCase = true)
                        || candidate.equals(native, ignoreCase = true)
                }
            }
            if (!byTitleMatch.isNullOrBlank()) return byTitleMatch
        }
        return null
    }

    private fun mapMangaBakaMetadata(
        series: JSONObject,
        links: JSONArray,
        collections: JSONArray,
        works: JSONArray,
        related: JSONArray,
    ): MangaBakaMetadata {
        val seriesId = series.opt("id")?.toString().orEmpty()
        val secondaryTitles = series.optJSONObject("secondary_titles")
        val alternateTitles = buildList {
            secondaryTitles?.keys()?.forEach { key ->
                secondaryTitles.optString(key).takeIf(String::isNotBlank)?.let(::add)
            }
        }

        val trackerMappings = buildTrackerMappings(seriesId = seriesId, links = links, source = series.optJSONObject("source"))
        val externalLinks = buildExternalLinks(links)

        return MangaBakaMetadata(
            seriesId = seriesId,
            preferredTitle = series.optString("title"),
            nativeTitle = series.optString("native_title").takeIf { it.isNotBlank() },
            romanizedTitle = series.optString("romanized_title").takeIf { it.isNotBlank() },
            alternateTitles = alternateTitles,
            description = series.optString("description")
                .replace("<br>", "\n")
                .replace(Regex("<.*?>"), "")
                .takeIf { it.isNotBlank() },
            coverImageUrl = series.optString("cover_url").takeIf { it.isNotBlank() },
            type = series.optString("type").takeIf { it.isNotBlank() },
            status = series.optString("status").takeIf { it.isNotBlank() },
            contentRating = series.optString("content_rating").takeIf { it.isNotBlank() },
            tags = series.optJSONArray("tags").toStringList(),
            authors = series.optJSONArray("authors").toStringList(),
            artists = series.optJSONArray("artists").toStringList(),
            publishers = buildList {
                val publishers = series.optJSONArray("publishers") ?: JSONArray()
                for (index in 0 until publishers.length()) {
                    publishers.optJSONObject(index)
                        ?.optString("name")
                        ?.takeIf(String::isNotBlank)
                        ?.let(::add)
                }
            },
            collections = buildList {
                for (index in 0 until collections.length()) {
                    val item = collections.optJSONObject(index) ?: continue
                    add(
                        MangaBakaCollection(
                            id = item.opt("id")?.toString().orEmpty(),
                            title = item.optString("title"),
                        ),
                    )
                }
            },
            works = buildList {
                for (index in 0 until works.length()) {
                    val item = works.optJSONObject(index) ?: continue
                    add(
                        MangaBakaWork(
                            id = item.opt("id")?.toString().orEmpty(),
                            subTitle = item.optString("sub_title"),
                        ),
                    )
                }
            },
            relatedEntries = buildList {
                for (index in 0 until related.length()) {
                    val item = related.optJSONObject(index) ?: continue
                    add(
                        MangaBakaRelatedEntry(
                            id = item.opt("id")?.toString().orEmpty(),
                            title = item.optString("title"),
                            coverImageUrl = item.optString("cover_url").takeIf { it.isNotBlank() },
                        ),
                    )
                }
            },
            links = externalLinks,
            trackerMappings = trackerMappings,
        )
    }

    private fun buildExternalLinks(links: JSONArray): List<MangaBakaExternalLink> = buildList {
        for (index in 0 until links.length()) {
            val link = links.optJSONObject(index) ?: continue
            val url = link.optString("url")
            if (url.isBlank()) continue
            add(
                MangaBakaExternalLink(
                    site = link.optString("name_display").ifBlank { link.optString("name") },
                    url = url,
                    label = link.optString("type").takeIf { it.isNotBlank() },
                ),
            )
        }
    }

    private fun buildTrackerMappings(
        seriesId: String,
        links: JSONArray,
        source: JSONObject?,
    ): List<MangaBakaTrackerMapping> {
        val mappings = linkedSetOf(
            MangaBakaTrackerMapping(
                trackerName = "MangaBaka",
                trackerMediaId = seriesId,
                url = "https://mangabaka.org/series/$seriesId",
                isPrimary = true,
            ),
        )
        for (index in 0 until links.length()) {
            val link = links.optJSONObject(index) ?: continue
            val name = link.optString("name_display").ifBlank { link.optString("name") }
            val url = link.optString("url").takeIf { it.isNotBlank() }
            if (url != null) {
                mappings += MangaBakaTrackerMapping(
                    trackerName = name.ifBlank { "External" },
                    trackerMediaId = url.extractLastPathSegment() ?: url,
                    url = url,
                )
            }
        }
        source?.optString("url")?.takeIf { it.isNotBlank() }?.let { sourceUrl ->
            mappings += MangaBakaTrackerMapping(
                trackerName = source.optString("site").ifBlank { "Source" },
                trackerMediaId = sourceUrl.extractLastPathSegment() ?: sourceUrl,
                url = sourceUrl,
            )
        }
        return mappings.toList()
    }
}

private fun String?.toJsonObject(): JSONObject? {
    if (this.isNullOrBlank()) return null
    return runCatching { JSONObject(this) }.getOrNull()
}

private fun String?.toDataArrayOrEmpty(): JSONArray =
    this.toJsonObject()?.optJSONArray("data") ?: JSONArray()

private fun JSONArray?.toStringList(): List<String> = buildList {
    if (this@toStringList == null) return@buildList
    for (index in 0 until this@toStringList.length()) {
        this@toStringList.opt(index)?.toString()?.takeIf { it.isNotBlank() }?.let(::add)
    }
}

private fun JSONArray.firstMatchingSeriesId(predicate: (JSONObject) -> Boolean): String? {
    for (index in 0 until length()) {
        val entry = optJSONObject(index) ?: continue
        if (predicate(entry)) {
            return entry.opt("id")?.toString()
        }
    }
    return null
}

private fun String.extractLastPathSegment(): String? =
    trimEnd('/').substringAfterLast('/').takeIf { it.isNotBlank() }
