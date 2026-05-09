package com.axiel7.yumehyou.metadata

import com.axiel7.anihyou.core.base.DataResult
import com.axiel7.yumehyou.core.model.CollectionEntry
import com.axiel7.yumehyou.core.model.ContentRating
import com.axiel7.yumehyou.core.model.ExternalLink
import com.axiel7.yumehyou.core.model.LibraryEntryStatus
import com.axiel7.yumehyou.core.model.MediaStatus
import com.axiel7.yumehyou.core.model.MediaType
import com.axiel7.yumehyou.core.model.NewsEntry
import com.axiel7.yumehyou.core.model.Relation
import com.axiel7.yumehyou.core.model.RelationType
import com.axiel7.yumehyou.core.model.Staff
import com.axiel7.yumehyou.core.model.Tag
import com.axiel7.yumehyou.core.model.Title
import com.axiel7.yumehyou.core.model.TrackerMapping
import com.axiel7.yumehyou.core.model.TrackerType
import com.axiel7.yumehyou.core.model.UnifiedLibraryEntry
import com.axiel7.yumehyou.core.model.UnifiedMedia
import com.axiel7.yumehyou.core.model.WorkEntry
import com.axiel7.yumehyou.metadata.mangabaka.MangaBakaMetadataClient
import org.json.JSONArray
import org.json.JSONObject

class MangaBakaMetadataProvider(
    private val metadataClient: MangaBakaMetadataClient,
) : MangaMetadataProvider {
    override suspend fun getMangaDetails(mangaId: String): DataResult<UnifiedMedia> {
        val seriesPayload = metadataClient.getSeriesDetails(mangaId)
        if (seriesPayload !is DataResult.Success) {
            return seriesPayload.castError()
        }

        val linksPayload = metadataClient.getSeriesLinks(mangaId)
        val collectionsPayload = metadataClient.getSeriesCollections(mangaId)
        val worksPayload = metadataClient.getSeriesWorks(mangaId)
        val relatedPayload = metadataClient.getSeriesRelated(mangaId)
        val newsPayload = metadataClient.getSeriesNews(mangaId)

        val mapper = MangaBakaMetadataMapper(
            linksPayload = linksPayload.successDataOrEmptyArray(),
            collectionsPayload = collectionsPayload.successDataOrEmptyArray(),
            worksPayload = worksPayload.successDataOrEmptyArray(),
            relatedPayload = relatedPayload.successDataOrEmptyArray(),
            newsPayload = newsPayload.successDataOrEmptyArray(),
        )

        return runCatching {
            val series = JSONObject(seriesPayload.data).optJSONObject("data")
                ?: return DataResult.Error("MangaBaka series response missing data payload")
            DataResult.Success(mapper.mapSeries(series))
        }.getOrElse {
            DataResult.Error(it.message ?: "Failed to parse MangaBaka series details")
        }
    }

    override suspend fun searchManga(
        query: String,
        page: Int,
        perPage: Int,
        contentRatings: Collection<String>,
        blockedTags: Collection<String>,
    ): DataResult<List<UnifiedMedia>> {
        val payload = metadataClient.searchSeries(
            query = query,
            page = page,
            perPage = perPage,
            contentRatings = contentRatings,
        )
        if (payload !is DataResult.Success) {
            return payload.castError()
        }

        return runCatching {
            val data = JSONObject(payload.data).optJSONArray("data") ?: JSONArray()
            val blockedTagSet = blockedTags.map { it.trim().lowercase() }.toSet()
            val mapper = MangaBakaMetadataMapper()
            val results = buildList {
                for (index in 0 until data.length()) {
                    val entry = data.optJSONObject(index) ?: continue
                    val mapped = mapper.mapSeries(entry)
                    val tagNames = mapped.tags.map { it.name.lowercase() }.toSet()
                    if (blockedTagSet.isEmpty() || tagNames.intersect(blockedTagSet).isEmpty()) {
                        add(mapped)
                    }
                }
            }
            DataResult.Success(results)
        }.getOrElse {
            DataResult.Error(it.message ?: "Failed to parse MangaBaka search response")
        }
    }

    override suspend fun getAvailableGenres(): DataResult<List<String>> {
        val payload = metadataClient.getGenres()
        if (payload !is DataResult.Success) {
            return payload.castError()
        }

        return runCatching {
            val data = JSONObject(payload.data).optJSONArray("data") ?: JSONArray()
            val genres = buildList {
                for (index in 0 until data.length()) {
                    val genre = data.optJSONObject(index) ?: continue
                    val label = genre.optString("label").ifBlank { genre.optString("value") }
                    if (label.isNotBlank()) add(label)
                }
            }
            DataResult.Success(genres)
        }.getOrElse {
            DataResult.Error(it.message ?: "Failed to parse MangaBaka genres")
        }
    }

    override suspend fun getAvailableTags(): DataResult<List<Tag>> {
        val payload = metadataClient.getTags()
        if (payload !is DataResult.Success) {
            return payload.castError()
        }

        return runCatching {
            val data = JSONObject(payload.data).optJSONArray("data") ?: JSONArray()
            val tags = buildList {
                for (index in 0 until data.length()) {
                    val tag = data.optJSONObject(index) ?: continue
                    val name = tag.optString("name")
                    if (name.isBlank()) continue
                    add(
                        Tag(
                            name = name,
                            description = tag.optString("description").takeIf { it.isNotBlank() },
                        ),
                    )
                }
            }
            DataResult.Success(tags)
        }.getOrElse {
            DataResult.Error(it.message ?: "Failed to parse MangaBaka tags")
        }
    }
}

internal class MangaBakaMetadataMapper(
    private val linksPayload: JSONArray = JSONArray(),
    private val collectionsPayload: JSONArray = JSONArray(),
    private val worksPayload: JSONArray = JSONArray(),
    private val relatedPayload: JSONArray = JSONArray(),
    private val newsPayload: JSONArray = JSONArray(),
) {
    fun mapSeries(series: JSONObject): UnifiedMedia {
        val title = series.optString("title")
        val nativeTitle = series.optString("native_title")
        val romanizedTitle = series.optString("romanized_title")
        val alternateTitles = buildList {
            val secondaryTitles = series.optJSONObject("secondary_titles")
            secondaryTitles?.keys()?.forEach { key ->
                secondaryTitles.optString(key).takeIf(String::isNotBlank)?.let(::add)
            }
        }
        val tags = mapTagList(series.optJSONArray("tags"))
        val genres = mapStringList(series.optJSONArray("genres"))
        val authors = mapStringList(series.optJSONArray("authors"))
        val artists = mapStringList(series.optJSONArray("artists"))
        val publishers = buildList {
            val publisherArray = series.optJSONArray("publishers") ?: JSONArray()
            for (index in 0 until publisherArray.length()) {
                val publisher = publisherArray.optJSONObject(index) ?: continue
                publisher.optString("name").takeIf(String::isNotBlank)?.let(::add)
            }
        }

        val staff = authors.map { Staff(id = "author:$it", name = it, role = "Author") } +
            artists.map { Staff(id = "artist:$it", name = it, role = "Artist") }

        val mangaId = series.opt("id")?.toString().orEmpty()
        val mappings = buildTrackerMappings(mangaId = mangaId, links = linksPayload, source = series.optJSONObject("source"))
        val externalLinks = buildExternalLinks(linksPayload)

        return UnifiedMedia(
            id = mangaId,
            mediaType = MediaType.MANGA,
            title = Title(
                preferred = title,
                native = nativeTitle.takeIf { it.isNotBlank() },
                english = romanizedTitle.takeIf { it.isNotBlank() },
                alternatives = alternateTitles,
            ),
            status = mapStatus(series.optString("status")),
            description = series.optString("description")
                .replace("<br>", "\n")
                .replace(Regex("<.*?>"), "")
                .takeIf { it.isNotBlank() },
            coverImageUrl = series.optString("cover_url").takeIf { it.isNotBlank() },
            contentRating = mapContentRating(series.optString("content_rating")),
            synonyms = alternateTitles,
            genres = genres,
            tags = tags,
            authors = authors,
            artists = artists,
            publishers = publishers,
            staff = staff,
            relations = mapRelations(relatedPayload),
            collections = mapCollections(collectionsPayload),
            works = mapWorks(worksPayload),
            news = mapNews(newsPayload),
            externalLinks = externalLinks,
            trackerMappings = mappings,
            volumeCount = series.optString("final_volume").toIntOrNull(),
            chapterCount = series.optString("total_chapters").toIntOrNull(),
        )
    }

    fun mapLibraryEntry(entry: JSONObject): UnifiedLibraryEntry {
        val series = entry.optJSONObject("Series") ?: entry.optJSONObject("series") ?: JSONObject()
        val seriesId = series.opt("id")?.toString().orEmpty()
        val seriesLinks = series.optJSONArray("links") ?: JSONArray()
        return UnifiedLibraryEntry(
            mediaId = seriesId,
            mediaType = MediaType.MANGA,
            status = mapLibraryStatus(entry.optString("state")),
            progress = entry.optInt("progress_chapter").takeIf { it > 0 },
            progressVolumes = entry.optInt("progress_volume").takeIf { it > 0 },
            repeatCount = entry.optInt("number_of_rereads").coerceAtLeast(0),
            notes = entry.optString("note").takeIf { it.isNotBlank() },
            trackerMappings = buildTrackerMappings(
                mangaId = seriesId,
                entryId = entry.opt("id")?.toString(),
                links = seriesLinks,
                source = series.optJSONObject("source"),
            ),
        )
    }

    private fun buildExternalLinks(links: JSONArray): List<ExternalLink> = buildList {
        for (index in 0 until links.length()) {
            val link = links.optJSONObject(index) ?: continue
            val url = link.optString("url")
            if (url.isBlank()) continue
            add(
                ExternalLink(
                    site = link.optString("name_display").ifBlank { link.optString("name") },
                    url = url,
                    label = link.optString("type").takeIf { it.isNotBlank() },
                    language = link.optString("language").takeIf { it.isNotBlank() },
                ),
            )
        }
    }

    private fun buildTrackerMappings(
        mangaId: String,
        entryId: String? = null,
        links: JSONArray,
        source: JSONObject?,
    ): List<TrackerMapping> {
        val mappings = linkedSetOf(
            TrackerMapping(
                trackerType = TrackerType.MANGA_BAKA,
                trackerMediaId = mangaId,
                trackerEntryId = entryId,
                isPrimary = true,
                url = "https://mangabaka.org/series/$mangaId",
            ),
        )

        for (index in 0 until links.length()) {
            val link = links.optJSONObject(index) ?: continue
            val url = link.optString("url")
            val trackerType = trackerTypeFor(
                rawName = link.optString("name"),
                rawDisplayName = link.optString("name_display"),
                url = url,
            ) ?: continue
            mappings += TrackerMapping(
                trackerType = trackerType,
                trackerMediaId = extractTrackerId(url) ?: url,
                url = url.takeIf { it.isNotBlank() },
            )
        }

        val sourceUrl = source?.optString("url").takeIf { !it.isNullOrBlank() }
        val sourceType = sourceUrl?.let { trackerTypeFor(rawName = source?.optString("site"), rawDisplayName = null, url = it) }
        if (sourceType != null) {
            mappings += TrackerMapping(
                trackerType = sourceType,
                trackerMediaId = extractTrackerId(sourceUrl) ?: sourceUrl,
                url = sourceUrl,
            )
        }

        return mappings.toList()
    }

    private fun mapRelations(relatedPayload: JSONArray): List<Relation> = buildList {
        for (index in 0 until relatedPayload.length()) {
            val related = relatedPayload.optJSONObject(index) ?: continue
            val id = related.opt("id")?.toString().orEmpty()
            if (id.isBlank()) continue
            add(
                Relation(
                    type = RelationType.OTHER,
                    mediaId = id,
                    mediaType = MediaType.MANGA,
                    title = Title(preferred = related.optString("title")),
                    status = mapStatus(related.optString("status")),
                    coverImageUrl = related.optString("cover_url").takeIf { it.isNotBlank() },
                ),
            )
        }
    }

    private fun mapCollections(collectionsPayload: JSONArray): List<CollectionEntry> = buildList {
        for (index in 0 until collectionsPayload.length()) {
            val collection = collectionsPayload.optJSONObject(index) ?: continue
            add(
                CollectionEntry(
                    id = collection.opt("id")?.toString().orEmpty(),
                    title = collection.optString("title"),
                    format = collection.optString("format").takeIf { it.isNotBlank() },
                    type = collection.optString("type").takeIf { it.isNotBlank() },
                    status = collection.optString("status").takeIf { it.isNotBlank() },
                    medium = collection.optString("medium").takeIf { it.isNotBlank() },
                    publisherName = collection.optJSONObject("publisher")
                        ?.optString("name")
                        ?.takeIf { it.isNotBlank() },
                    editionName = collection.optJSONObject("edition")
                        ?.optString("name")
                        ?.takeIf { it.isNotBlank() },
                    countMain = collection.optInt("count_main").takeIf { it > 0 },
                ),
            )
        }
    }

    private fun mapWorks(worksPayload: JSONArray): List<WorkEntry> = buildList {
        for (index in 0 until worksPayload.length()) {
            val work = worksPayload.optJSONObject(index) ?: continue
            val firstImage = work.optJSONArray("images")
                ?.optJSONObject(0)
                ?.optJSONObject("image")
            val imageUrl = firstImage?.optJSONObject("x250")?.optString("x1")
                ?: firstImage?.optJSONObject("x150")?.optString("x1")
                ?: firstImage?.optJSONObject("raw")?.optString("url")
            val firstPrice = work.optJSONArray("price")?.optJSONObject(0)
            add(
                WorkEntry(
                    id = work.opt("id")?.toString().orEmpty(),
                    subTitle = work.optString("sub_title"),
                    countType = work.optString("count_type").takeIf { it.isNotBlank() },
                    releaseDate = work.optString("release_date").takeIf { it.isNotBlank() },
                    sequence = work.optString("sequence_string").takeIf { it.isNotBlank() },
                    pages = work.optInt("pages").takeIf { it > 0 },
                    imageUrl = imageUrl?.takeIf { it.isNotBlank() },
                    price = firstPrice?.let { priceObject ->
                        val value = priceObject.opt("value")?.toString().orEmpty()
                        val currencyCode = priceObject.optString("iso_code")
                        listOf(value, currencyCode.uppercase().takeIf { it.isNotBlank() })
                            .filterNotNull()
                            .joinToString(" ")
                            .takeIf { priceLabel -> priceLabel.isNotBlank() }
                    },
                ),
            )
        }
    }

    private fun mapNews(newsPayload: JSONArray): List<NewsEntry> = buildList {
        for (index in 0 until newsPayload.length()) {
            val news = newsPayload.optJSONObject(index) ?: continue
            val url = news.optString("url")
            if (url.isBlank()) continue
            add(
                NewsEntry(
                    id = news.opt("id")?.toString().orEmpty(),
                    title = news.optString("title"),
                    url = url,
                    author = news.optString("author").takeIf { it.isNotBlank() },
                    source = news.optString("source_name").takeIf { it.isNotBlank() },
                    publishedAt = news.optString("published_at").takeIf { it.isNotBlank() },
                ),
            )
        }
    }

    private fun mapTagList(tags: JSONArray?): List<Tag> = buildList {
        if (tags == null) return@buildList
        for (index in 0 until tags.length()) {
            val value = tags.opt(index)?.toString()?.takeIf { it.isNotBlank() } ?: continue
            add(Tag(name = value))
        }
    }

    private fun mapStringList(values: JSONArray?): List<String> = buildList {
        if (values == null) return@buildList
        for (index in 0 until values.length()) {
            values.opt(index)?.toString()?.takeIf { it.isNotBlank() }?.let(::add)
        }
    }
}

private fun mapStatus(rawStatus: String?): MediaStatus {
    return when (rawStatus?.trim()?.lowercase()) {
        "ongoing", "releasing" -> MediaStatus.RELEASING
        "completed", "complete", "finished" -> MediaStatus.FINISHED
        "cancelled", "canceled" -> MediaStatus.CANCELLED
        "hiatus", "on_hiatus" -> MediaStatus.HIATUS
        "upcoming", "not_yet_released" -> MediaStatus.NOT_YET_RELEASED
        else -> MediaStatus.UNKNOWN
    }
}

private fun mapLibraryStatus(rawStatus: String?): LibraryEntryStatus {
    return when (rawStatus?.trim()?.lowercase()?.replace("-", "_")?.replace(" ", "_")) {
        "reading", "current" -> LibraryEntryStatus.CURRENT
        "plan_to_read", "planning", "considering" -> LibraryEntryStatus.PLANNING
        "completed", "complete" -> LibraryEntryStatus.COMPLETED
        "dropped" -> LibraryEntryStatus.DROPPED
        "paused", "on_hold" -> LibraryEntryStatus.PAUSED
        "rereading", "repeating" -> LibraryEntryStatus.REPEATING
        else -> LibraryEntryStatus.UNKNOWN
    }
}

private fun mapContentRating(rawRating: String?): ContentRating {
    return when (rawRating?.trim()?.lowercase()) {
        "safe", "general", "everyone" -> ContentRating.GENERAL
        "suggestive", "teen" -> ContentRating.TEEN
        "mature" -> ContentRating.MATURE
        "adult", "nsfw" -> ContentRating.ADULT
        else -> ContentRating.UNKNOWN
    }
}

private fun trackerTypeFor(
    rawName: String?,
    rawDisplayName: String?,
    url: String?,
): TrackerType? {
    val normalized = listOf(rawName, rawDisplayName, url)
        .filterNotNull()
        .joinToString(" ")
        .lowercase()
    return when {
        "myanimelist" in normalized || "mal" in normalized -> TrackerType.MY_ANIME_LIST
        "anilist" in normalized -> TrackerType.ANILIST
        "mangaupdates" in normalized -> TrackerType.MANGA_UPDATES
        "mangabaka" in normalized -> TrackerType.MANGA_BAKA
        else -> null
    }
}

private fun extractTrackerId(url: String?): String? {
    if (url.isNullOrBlank()) return null
    return url.trimEnd('/').substringAfterLast('/').takeIf { it.isNotBlank() }
}

private fun DataResult<String>.successDataOrEmptyArray(): JSONArray {
    return (this as? DataResult.Success)?.data
        ?.let(::JSONObject)
        ?.optJSONArray("data")
        ?: JSONArray()
}

private fun DataResult<String>.castError(): DataResult.Error<Nothing> =
    DataResult.Error((this as? DataResult.Error)?.message ?: "MangaBaka request failed")
