package com.axiel7.yumehyou.tracker

import com.axiel7.yumehyou.core.model.TrackerType

enum class TrackerCapability {
    ANIME_TRACKING,
    MANGA_TRACKING,
    NOTES,
    CUSTOM_TAGS_LABELS,
    SCORE_UPDATES,
    PROGRESS_UPDATES,
    STATUS_UPDATES,
    REWATCH_REREAD,
    FAVORITES,
    FOLLOWERS_FOLLOWING,
    PROFILE_DATA,
    ACTIVITY_DATA,
    EXPORT,
    ADULT_CONTENT_PREFERENCES,
    EXTERNAL_LINKS,
    TITLE_LANGUAGE_SETTINGS,
}

data class TrackerCapabilities(
    val trackerType: TrackerType,
    val supported: Set<TrackerCapability>,
) {
    fun supports(capability: TrackerCapability) =
        capability in supported

    fun supportsAll(vararg capabilities: TrackerCapability) =
        capabilities.all(::supports)

    fun supportsAny(vararg capabilities: TrackerCapability) =
        capabilities.any(::supports)

    val animeTracking: Boolean
        get() = supports(TrackerCapability.ANIME_TRACKING)

    val mangaTracking: Boolean
        get() = supports(TrackerCapability.MANGA_TRACKING)

    val notes: Boolean
        get() = supports(TrackerCapability.NOTES)

    val customTags: Boolean
        get() = supports(TrackerCapability.CUSTOM_TAGS_LABELS)

    val scoreUpdates: Boolean
        get() = supports(TrackerCapability.SCORE_UPDATES)

    val progressUpdates: Boolean
        get() = supports(TrackerCapability.PROGRESS_UPDATES)

    val statusUpdates: Boolean
        get() = supports(TrackerCapability.STATUS_UPDATES)

    val rewatchOrReread: Boolean
        get() = supports(TrackerCapability.REWATCH_REREAD)

    val favorites: Boolean
        get() = supports(TrackerCapability.FAVORITES)

    val followersFollowing: Boolean
        get() = supports(TrackerCapability.FOLLOWERS_FOLLOWING)

    val profileData: Boolean
        get() = supports(TrackerCapability.PROFILE_DATA)

    val activityData: Boolean
        get() = supports(TrackerCapability.ACTIVITY_DATA)

    val export: Boolean
        get() = supports(TrackerCapability.EXPORT)

    val adultContentPreferences: Boolean
        get() = supports(TrackerCapability.ADULT_CONTENT_PREFERENCES)

    val externalLinks: Boolean
        get() = supports(TrackerCapability.EXTERNAL_LINKS)

    val titleLanguageSettings: Boolean
        get() = supports(TrackerCapability.TITLE_LANGUAGE_SETTINGS)

}

val anilistTrackerCapabilities = TrackerCapabilities(
    trackerType = TrackerType.ANILIST,
    supported = setOf(
        TrackerCapability.ANIME_TRACKING,
        TrackerCapability.MANGA_TRACKING,
        TrackerCapability.NOTES,
        TrackerCapability.CUSTOM_TAGS_LABELS,
        TrackerCapability.SCORE_UPDATES,
        TrackerCapability.PROGRESS_UPDATES,
        TrackerCapability.STATUS_UPDATES,
        TrackerCapability.REWATCH_REREAD,
        TrackerCapability.FAVORITES,
        TrackerCapability.FOLLOWERS_FOLLOWING,
        TrackerCapability.PROFILE_DATA,
        TrackerCapability.ACTIVITY_DATA,
        TrackerCapability.EXPORT,
        TrackerCapability.ADULT_CONTENT_PREFERENCES,
        TrackerCapability.EXTERNAL_LINKS,
        TrackerCapability.TITLE_LANGUAGE_SETTINGS,
    ),
)

private data class StaticTrackerAdapter(
    override val trackerType: TrackerType,
    override val capabilities: TrackerCapabilities,
) : BaseTrackerAdapter()

private fun staticTrackerAdapter(
    trackerType: TrackerType,
    vararg supported: TrackerCapability,
) = StaticTrackerAdapter(
    trackerType = trackerType,
    capabilities = TrackerCapabilities(
        trackerType = trackerType,
        supported = supported.toSet(),
    ),
)

val defaultTrackerAdapters: List<TrackerAdapter> = listOf(
    staticTrackerAdapter(
        trackerType = TrackerType.MY_ANIME_LIST,
        TrackerCapability.ANIME_TRACKING,
        TrackerCapability.MANGA_TRACKING,
        TrackerCapability.NOTES,
        TrackerCapability.CUSTOM_TAGS_LABELS,
        TrackerCapability.SCORE_UPDATES,
        TrackerCapability.PROGRESS_UPDATES,
        TrackerCapability.STATUS_UPDATES,
        TrackerCapability.REWATCH_REREAD,
        TrackerCapability.FAVORITES,
        TrackerCapability.FOLLOWERS_FOLLOWING,
        TrackerCapability.PROFILE_DATA,
        TrackerCapability.EXTERNAL_LINKS,
    ),
    staticTrackerAdapter(
        trackerType = TrackerType.MANGA_UPDATES,
        TrackerCapability.MANGA_TRACKING,
        TrackerCapability.NOTES,
        TrackerCapability.CUSTOM_TAGS_LABELS,
        TrackerCapability.SCORE_UPDATES,
        TrackerCapability.PROGRESS_UPDATES,
        TrackerCapability.STATUS_UPDATES,
        TrackerCapability.REWATCH_REREAD,
        TrackerCapability.FAVORITES,
        TrackerCapability.FOLLOWERS_FOLLOWING,
        TrackerCapability.PROFILE_DATA,
        TrackerCapability.EXTERNAL_LINKS,
    ),
    staticTrackerAdapter(
        trackerType = TrackerType.MANGA_BAKA,
        TrackerCapability.MANGA_TRACKING,
        TrackerCapability.NOTES,
        TrackerCapability.SCORE_UPDATES,
        TrackerCapability.PROGRESS_UPDATES,
        TrackerCapability.STATUS_UPDATES,
        TrackerCapability.EXTERNAL_LINKS,
        TrackerCapability.TITLE_LANGUAGE_SETTINGS,
    ),
)
