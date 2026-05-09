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
        require(day == null || month != null) {
            "month must be provided when day is set"
        }
        require(day == null || day in 1..maxDayOfMonth(month, year)) {
            "day must be valid for the provided month and year"
        }
    }

    private fun maxDayOfMonth(
        month: Int?,
        year: Int?,
    ) = when (month) {
        2 -> if (year != null && isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    private fun isLeapYear(year: Int) =
        year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
}
