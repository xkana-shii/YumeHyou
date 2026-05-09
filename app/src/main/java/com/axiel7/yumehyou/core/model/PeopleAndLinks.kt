package com.axiel7.yumehyou.core.model

data class Staff(
    val id: String,
    val name: String,
    val role: String? = null,
    val language: String? = null,
    val imageUrl: String? = null,
)

data class Character(
    val id: String,
    val name: String,
    val role: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
)

enum class RelationType {
    ADAPTATION,
    PREQUEL,
    SEQUEL,
    PARENT,
    SIDE_STORY,
    CHARACTER,
    SUMMARY,
    ALTERNATIVE,
    SPIN_OFF,
    OTHER,
    SOURCE,
    COMPILATION,
    CONTAINS,
}

data class Relation(
    val type: RelationType,
    val mediaId: String,
    val mediaType: MediaType,
    val title: Title,
    val status: MediaStatus? = null,
    val coverImageUrl: String? = null,
)

data class Tag(
    val name: String,
    val description: String? = null,
    val rank: Int? = null,
    val isSpoiler: Boolean = false,
)

data class ExternalLink(
    val site: String,
    val url: String,
    val label: String? = null,
    val language: String? = null,
    val iconUrl: String? = null,
    val colorHex: String? = null,
)

data class CollectionEntry(
    val id: String,
    val title: String,
    val format: String? = null,
    val type: String? = null,
    val status: String? = null,
    val medium: String? = null,
    val publisherName: String? = null,
    val editionName: String? = null,
    val countMain: Int? = null,
)

data class WorkEntry(
    val id: String,
    val subTitle: String,
    val countType: String? = null,
    val releaseDate: String? = null,
    val sequence: String? = null,
    val pages: Int? = null,
    val imageUrl: String? = null,
    val price: String? = null,
)

data class NewsEntry(
    val id: String,
    val title: String,
    val url: String,
    val author: String? = null,
    val source: String? = null,
    val publishedAt: String? = null,
)
