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
