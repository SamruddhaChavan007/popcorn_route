package com.example.popcornroute.userlibrary.data.mapper

import com.example.popcornroute.userlibrary.data.local.relation.LibraryTitleRow
import com.example.popcornroute.userlibrary.domain.model.LibraryList
import com.example.popcornroute.userlibrary.domain.model.MediaType
import com.example.popcornroute.userlibrary.domain.model.TitleKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryMappersTest {

    @Test
    fun `row without cache details maps to a title with no summary`() {
        val row = LibraryTitleRow("movie", 603, 100L, null, null, null, null, null)

        val title = row.toDomain()

        assertEquals(TitleKey(MediaType.MOVIE, 603), title.key)
        assertEquals(100L, title.savedAt)
        assertNull(title.summary)
    }

    @Test
    fun `row with cache details maps to a summary`() {
        val row = LibraryTitleRow("tv", 1399, 200L, "Game of Thrones", "/p.jpg", 2011, 8.4, 5L)

        val summary = row.toDomain().summary

        assertNotNull(summary)
        assertEquals("Game of Thrones", summary!!.title)
        assertEquals("/p.jpg", summary.posterPath)
        assertEquals(2011, summary.releaseYear)
        assertEquals(8.4, summary.rating)
        assertEquals(5L, summary.cachedAt)
    }

    @Test
    fun `media type round trips and rejects unknown values`() {
        assertEquals(MediaType.MOVIE, MediaType.fromValue("movie"))
        assertEquals(MediaType.TV, MediaType.fromValue("tv"))
        assertThrows(IllegalArgumentException::class.java) { MediaType.fromValue("person") }
    }

    @Test
    fun `custom list entity is trimmed, keyed and not built in`() {
        val entity = newCustomListEntity("u1", "id-1", "  Horror ", now = 10L)

        assertEquals("Horror", entity.name)
        assertEquals("horror", entity.nameKey)
        assertFalse(entity.isBuiltIn)
        assertEquals(10L, entity.createdAt)
    }

    @Test
    fun `watchlist entity uses the fixed id and is built in`() {
        val entity = newWatchlistEntity("u1", now = 10L)

        assertEquals(LibraryList.WATCHLIST_ID, entity.listId)
        assertTrue(entity.isBuiltIn)
        assertEquals("watchlist", entity.nameKey)
    }
}
