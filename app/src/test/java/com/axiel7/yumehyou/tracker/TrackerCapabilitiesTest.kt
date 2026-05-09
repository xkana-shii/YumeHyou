package com.axiel7.yumehyou.tracker

import com.axiel7.anihyou.core.domain.repository.MediaListRepository
import com.axiel7.yumehyou.core.model.TrackerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackerCapabilitiesTest {
    @Test
    fun capabilityFactoryDeduplicatesEntries() {
        val capabilities = TrackerCapabilities(
            trackerType = TrackerType.ANILIST,
            supported = setOf(
                TrackerCapability.ANIME_TRACKING,
                TrackerCapability.ANIME_TRACKING,
            ),
        )

        assertEquals(setOf(TrackerCapability.ANIME_TRACKING), capabilities.supported)
    }

    @Test
    fun anilistCapabilitiesSupportAllDefinedCapabilities() {
        assertTrue(
            anilistTrackerCapabilities.supportsAll(
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
            )
        )
    }

    @Test
    fun malCapabilitiesMatchImplementedPhase7Surface() {
        assertTrue(malTrackerCapabilities.supports(TrackerCapability.ANIME_TRACKING))
        assertTrue(malTrackerCapabilities.supports(TrackerCapability.MANGA_TRACKING))
        assertTrue(malTrackerCapabilities.supports(TrackerCapability.NOTES))
        assertTrue(malTrackerCapabilities.supports(TrackerCapability.STATUS_UPDATES))
        assertTrue(malTrackerCapabilities.supports(TrackerCapability.PROGRESS_UPDATES))
        assertTrue(malTrackerCapabilities.supports(TrackerCapability.SCORE_UPDATES))
        assertTrue(malTrackerCapabilities.supports(TrackerCapability.REWATCH_REREAD))
        assertTrue(malTrackerCapabilities.supports(TrackerCapability.FAVORITES))
        assertTrue(malTrackerCapabilities.supports(TrackerCapability.FOLLOWERS_FOLLOWING))
        assertTrue(malTrackerCapabilities.supports(TrackerCapability.PROFILE_DATA))
        assertTrue(malTrackerCapabilities.supports(TrackerCapability.EXTERNAL_LINKS))
        assertFalse(malTrackerCapabilities.supports(TrackerCapability.ACTIVITY_DATA))
    }

    @Test
    fun mangaUpdatesAdapterIsMangaOnly() {
        val adapter = defaultTrackerAdapters.first { it.trackerType == TrackerType.MANGA_UPDATES }

        assertTrue(adapter.capabilities.mangaTracking)
        assertFalse(adapter.capabilities.animeTracking)
    }

    @Test
    fun mangaBakaCapabilitiesMatchPhase8Surface() {
        assertTrue(mangaBakaTrackerCapabilities.supports(TrackerCapability.MANGA_TRACKING))
        assertTrue(mangaBakaTrackerCapabilities.supports(TrackerCapability.NOTES))
        assertTrue(mangaBakaTrackerCapabilities.supports(TrackerCapability.CUSTOM_TAGS_LABELS))
        assertTrue(mangaBakaTrackerCapabilities.supports(TrackerCapability.SCORE_UPDATES))
        assertTrue(mangaBakaTrackerCapabilities.supports(TrackerCapability.PROGRESS_UPDATES))
        assertTrue(mangaBakaTrackerCapabilities.supports(TrackerCapability.STATUS_UPDATES))
        assertTrue(mangaBakaTrackerCapabilities.supports(TrackerCapability.REWATCH_REREAD))
        assertTrue(mangaBakaTrackerCapabilities.supports(TrackerCapability.PROFILE_DATA))
        assertTrue(mangaBakaTrackerCapabilities.supports(TrackerCapability.EXPORT))
        assertTrue(mangaBakaTrackerCapabilities.supports(TrackerCapability.ADULT_CONTENT_PREFERENCES))
        assertTrue(mangaBakaTrackerCapabilities.supports(TrackerCapability.EXTERNAL_LINKS))
        assertTrue(mangaBakaTrackerCapabilities.supports(TrackerCapability.TITLE_LANGUAGE_SETTINGS))
        assertFalse(mangaBakaTrackerCapabilities.supports(TrackerCapability.ANIME_TRACKING))
    }

    @Test
    fun gatewayCanQueryCapabilitiesByTrackerType() {
        val malAdapter = object : BaseTrackerAdapter() {
            override val trackerType: TrackerType = TrackerType.MY_ANIME_LIST
            override val capabilities: TrackerCapabilities = malTrackerCapabilities
        }
        val gateway = object : TrackerGateway {
            override val mediaListRepository: MediaListRepository
                get() = error("Not needed for capability lookups")
            override val trackerManager = DefaultTrackerManager(listOf(malAdapter) + defaultTrackerAdapters)
        }

        assertTrue(gateway.supports(TrackerType.MY_ANIME_LIST, TrackerCapability.SCORE_UPDATES))
        assertFalse(gateway.supports(TrackerType.MANGA_BAKA, TrackerCapability.FAVORITES))
        assertNotNull(gateway.getCapabilities(TrackerType.MANGA_UPDATES))
    }

    @Test
    fun trackerManagerResolvesAdaptersByTrackerType() {
        val manager = DefaultTrackerManager(defaultTrackerAdapters)

        assertNull(manager.getAdapter(TrackerType.MY_ANIME_LIST))
        assertNotNull(manager.getAdapter(TrackerType.MANGA_UPDATES))
    }
}
