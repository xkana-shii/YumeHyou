package com.axiel7.yumehyou.metadata

import com.axiel7.yumehyou.core.model.ContentRating
import com.axiel7.yumehyou.core.model.LibraryEntryStatus
import com.axiel7.yumehyou.core.model.MediaType
import com.axiel7.yumehyou.core.model.TrackerType
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MangaBakaMetadataProviderTest {
    @Test
    fun mapperBuildsUnifiedMediaWithTrackerMappings() {
        val mapper = MangaBakaMetadataMapper(
            linksPayload = JSONArray(
                listOf(
                    JSONObject(
                        mapOf(
                            "id" to "1",
                            "url" to "https://myanimelist.net/manga/12",
                            "name" to "myanimelist",
                            "name_display" to "MyAnimeList",
                            "type" to "tracker",
                        ),
                    ),
                    JSONObject(
                        mapOf(
                            "id" to "2",
                            "url" to "https://www.mangaupdates.com/series/example",
                            "name" to "mangaupdates",
                            "name_display" to "MangaUpdates",
                            "type" to "tracker",
                        ),
                    ),
                ),
            ),
            collectionsPayload = JSONArray(
                listOf(
                    JSONObject(
                        mapOf(
                            "id" to "c1",
                            "title" to "Deluxe Edition",
                            "format" to "print",
                            "publisher" to mapOf("name" to "Yen Press"),
                        ),
                    ),
                ),
            ),
            worksPayload = JSONArray(
                listOf(
                    JSONObject(
                        mapOf(
                            "id" to "w1",
                            "sub_title" to "Vol. 1",
                            "sequence_string" to "1",
                            "pages" to 200,
                        ),
                    ),
                ),
            ),
            relatedPayload = JSONArray(
                listOf(
                    JSONObject(
                        mapOf(
                            "id" to "44",
                            "title" to "Related Work",
                            "status" to "completed",
                        ),
                    ),
                ),
            ),
            newsPayload = JSONArray(
                listOf(
                    JSONObject(
                        mapOf(
                            "id" to "n1",
                            "title" to "Announcement",
                            "url" to "https://example.com/news",
                            "source_name" to "ann",
                        ),
                    ),
                ),
            ),
        )

        val media = mapper.mapSeries(
            JSONObject(
                mapOf(
                    "id" to "123",
                    "title" to "Yotsuba",
                    "native_title" to "よつばと！",
                    "romanized_title" to "Yotsuba to!",
                    "secondary_titles" to mapOf("1" to "Yotsubato!"),
                    "authors" to listOf("Kiyohiko Azuma"),
                    "artists" to listOf("Kiyohiko Azuma"),
                    "description" to "<br>Slice of life",
                    "status" to "completed",
                    "content_rating" to "safe",
                    "final_volume" to "16",
                    "total_chapters" to "112",
                    "cover_url" to "https://example.com/cover.jpg",
                    "publishers" to listOf(mapOf("name" to "ASCII Media Works")),
                    "genres" to listOf("Comedy"),
                    "tags" to listOf("Slice of Life"),
                ),
            ),
        )

        assertEquals(MediaType.MANGA, media.mediaType)
        assertEquals(ContentRating.GENERAL, media.contentRating)
        assertEquals(listOf("Kiyohiko Azuma"), media.authors)
        assertEquals(listOf("ASCII Media Works"), media.publishers)
        assertEquals(16, media.volumeCount)
        assertEquals(112, media.chapterCount)
        assertTrue(media.collections.isNotEmpty())
        assertTrue(media.works.isNotEmpty())
        assertTrue(media.news.isNotEmpty())
        assertTrue(media.trackerMappings.any { it.trackerType == TrackerType.MANGA_BAKA && it.isPrimary })
        assertTrue(media.trackerMappings.any { it.trackerType == TrackerType.MY_ANIME_LIST })
        assertTrue(media.trackerMappings.any { it.trackerType == TrackerType.MANGA_UPDATES })
    }

    @Test
    fun mapperBuildsUnifiedLibraryEntryWithRereadAndNoteFields() {
        val mapper = MangaBakaMetadataMapper()

        val entry = mapper.mapLibraryEntry(
            JSONObject(
                mapOf(
                    "id" to "entry-1",
                    "state" to "rereading",
                    "note" to "favorite arc",
                    "progress_chapter" to 50,
                    "progress_volume" to 8,
                    "number_of_rereads" to 2,
                    "Series" to mapOf(
                        "id" to "321",
                        "title" to "Monster",
                    ),
                ),
            ),
        )

        assertEquals("321", entry.mediaId)
        assertEquals(LibraryEntryStatus.REPEATING, entry.status)
        assertEquals(50, entry.progress)
        assertEquals(8, entry.progressVolumes)
        assertEquals(2, entry.repeatCount)
        assertEquals("favorite arc", entry.notes)
    }
}
