package com.axiel7.yumehyou.core.model

enum class MetadataSource {
    ANILIST,
    MANGA_BAKA,
}

enum class TrackerType {
    ANILIST,
    MY_ANIME_LIST,
    MANGA_UPDATES,
    MANGA_BAKA,
}

enum class MediaType(
    val defaultMetadataSource: MetadataSource,
) {
    ANIME(MetadataSource.ANILIST),
    MANGA(MetadataSource.MANGA_BAKA),
}

enum class MediaStatus {
    FINISHED,
    RELEASING,
    NOT_YET_RELEASED,
    CANCELLED,
    HIATUS,
    UNKNOWN,
}

enum class ContentRating {
    GENERAL,
    TEEN,
    MATURE,
    ADULT,
    UNKNOWN,
}

enum class LibraryEntryStatus {
    CURRENT,
    PLANNING,
    COMPLETED,
    DROPPED,
    PAUSED,
    REPEATING,
    UNKNOWN,
}

data class PartialDate(
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
) {
    init {
        require(month == null || month in 1..12) {
            "month must be between 1 and 12"
        }
        require(day == null || day in 1..31) {
            "day must be between 1 and 31"
        }
    }
}
