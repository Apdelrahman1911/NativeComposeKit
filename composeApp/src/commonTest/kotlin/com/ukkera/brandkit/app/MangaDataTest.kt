package com.ukkera.brandkit.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Guards the sample data the manga flow renders, and the deterministic cover-gradient fallback. */
class MangaDataTest {

    @Test
    fun library_is_non_empty_with_unique_ids() {
        assertTrue(MangaLibrary.all.isNotEmpty())
        val ids = MangaLibrary.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "manga ids must be unique")
    }

    @Test
    fun every_manga_is_internally_consistent() {
        for (m in MangaLibrary.all) {
            assertTrue(m.chapters.isNotEmpty(), "${m.id} has no chapters")
            assertTrue(m.unread in 0..m.chapters.size, "${m.id} unread ${m.unread} out of range")
            assertTrue(m.rating in 0f..5f, "${m.id} rating ${m.rating} out of range")
            assertTrue(m.genres.isNotEmpty(), "${m.id} has no genres")
            // chapter ids unique within the manga
            val cids = m.chapters.map { it.id }
            assertEquals(cids.size, cids.toSet().size, "${m.id} chapter ids must be unique")
        }
    }

    @Test
    fun lookups_resolve_known_ids_and_reject_unknown() {
        val first = MangaLibrary.all.first()
        assertEquals(first, MangaLibrary.byId(first.id))
        assertNull(MangaLibrary.byId("no-such-manga"))

        val chapter = first.chapters.first()
        assertNotNull(MangaLibrary.chapter(first.id, chapter.id))
        assertNull(MangaLibrary.chapter(first.id, "no-such-chapter"))
        assertNull(MangaLibrary.chapter("no-such-manga", chapter.id))
    }

    @Test
    fun page_count_is_in_expected_range() {
        for (m in MangaLibrary.all) {
            for (c in m.chapters) {
                assertTrue(MangaLibrary.pageCount(c) in 5..8, "${c.id} page count out of range")
            }
        }
    }

    @Test
    fun gradient_colors_are_deterministic_per_seed() {
        assertEquals(gradientColors("crimson-vanguard"), gradientColors("crimson-vanguard"))
        // a two-stop gradient should not collapse to a single color
        val (top, bottom) = gradientColors("crimson-vanguard")
        assertTrue(top != bottom, "gradient stops should differ")
    }
}
