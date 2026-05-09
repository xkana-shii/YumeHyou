package com.axiel7.yumehyou.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class UnifiedDomainModelsTest {
    @Test
    fun animeDefaultsToAniListCanonicalMetadata() {
        val media = UnifiedMedia(
            id = "1",
            mediaType = MediaType.ANIME,
            title = Title(preferred = "Cowboy Bebop"),
        )

        assertEquals(MetadataSource.ANILIST, media.canonicalSource)
    }

    @Test
    fun mangaDefaultsToMangaBakaCanonicalMetadata() {
        val media = UnifiedMedia(
            id = "mb-1",
            mediaType = MediaType.MANGA,
            title = Title(preferred = "Yotsuba&!"),
        )

        assertEquals(MetadataSource.MANGA_BAKA, media.canonicalSource)
    }

    @Test
    fun scoreExposesNormalizedValue() {
        val score = Score(
            value = 8.0,
            maxValue = 10.0,
            format = Score.Format.POINT_10,
        )

        assertEquals(0.8, score.normalized, 0.0)
    }

    @Test
    fun scoreRejectsValuesOutsideScale() {
        assertThrows(IllegalArgumentException::class.java) {
            Score(value = 11.0, maxValue = 10.0)
        }
    }

    @Test
    fun titleCollectsDistinctVariants() {
        val title = Title(
            preferred = "Monster",
            english = "Monster",
            native = "MONSTER",
            alternatives = listOf("Monster", "Naoki Urasawa's Monster"),
        )

        assertEquals(
            listOf("Monster", "MONSTER", "Naoki Urasawa's Monster"),
            title.all,
        )
    }

    @Test
    fun partialDateRejectsInvalidMonthDayCombination() {
        assertThrows(IllegalArgumentException::class.java) {
            PartialDate(year = 2024, month = 2, day = 30)
        }
    }
}
