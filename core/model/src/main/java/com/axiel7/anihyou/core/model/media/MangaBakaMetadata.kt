package com.axiel7.anihyou.core.model.media

data class MangaBakaMetadata(
    val seriesId: String,
    val preferredTitle: String,
    val nativeTitle: String? = null,
    val romanizedTitle: String? = null,
    val alternateTitles: List<String> = emptyList(),
    val description: String? = null,
    val coverImageUrl: String? = null,
    val type: String? = null,
    val status: String? = null,
    val contentRating: String? = null,
    val tags: List<String> = emptyList(),
    val authors: List<String> = emptyList(),
    val artists: List<String> = emptyList(),
    val publishers: List<String> = emptyList(),
    val collections: List<MangaBakaCollection> = emptyList(),
    val works: List<MangaBakaWork> = emptyList(),
    val relatedEntries: List<MangaBakaRelatedEntry> = emptyList(),
    val links: List<MangaBakaExternalLink> = emptyList(),
    val trackerMappings: List<MangaBakaTrackerMapping> = emptyList(),
) {
    val primaryUrl: String?
        get() = trackerMappings.firstOrNull { it.isPrimary }?.url
            ?: links.firstOrNull()?.url
}

data class MangaBakaCollection(
    val id: String,
    val title: String,
)

data class MangaBakaWork(
    val id: String,
    val subTitle: String,
)

data class MangaBakaRelatedEntry(
    val id: String,
    val title: String,
    val coverImageUrl: String? = null,
)

data class MangaBakaExternalLink(
    val site: String,
    val url: String,
    val label: String? = null,
)

data class MangaBakaTrackerMapping(
    val trackerName: String,
    val trackerMediaId: String,
    val url: String? = null,
    val isPrimary: Boolean = false,
)
