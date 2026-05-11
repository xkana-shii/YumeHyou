package com.axiel7.yumehyou.core.model

import com.axiel7.anihyou.core.model.media.MangaBakaExternalLink
import com.axiel7.anihyou.core.model.media.MangaBakaMetadata
import com.axiel7.anihyou.core.model.media.MangaBakaTrackerMapping
import org.junit.Assert.assertEquals
import org.junit.Test

class MangaBakaMetadataModelTest {
    @Test
    fun primaryUrlPrefersPrimaryTrackerMapping() {
        val metadata = MangaBakaMetadata(
            seriesId = "321",
            preferredTitle = "Monster",
            links = listOf(
                MangaBakaExternalLink(
                    site = "External",
                    url = "https://example.org/alt",
                ),
            ),
            trackerMappings = listOf(
                MangaBakaTrackerMapping(
                    trackerName = "MangaBaka",
                    trackerMediaId = "321",
                    url = "https://mangabaka.org/series/321",
                    isPrimary = true,
                ),
            ),
        )

        assertEquals("https://mangabaka.org/series/321", metadata.primaryUrl)
    }

    @Test
    fun primaryUrlFallsBackToFirstExternalLink() {
        val metadata = MangaBakaMetadata(
            seriesId = "654",
            preferredTitle = "Yotsuba",
            links = listOf(
                MangaBakaExternalLink(
                    site = "MangaUpdates",
                    url = "https://www.mangaupdates.com/series/yotsuba",
                ),
            ),
            trackerMappings = emptyList(),
        )

        assertEquals("https://www.mangaupdates.com/series/yotsuba", metadata.primaryUrl)
    }
}
