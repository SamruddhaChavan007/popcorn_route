package com.example.popcornroute.userlibrary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ListNameTest {

    @Test
    fun `valid name passes`() {
        assertNull(ListName.validate("Horror Night"))
    }

    @Test
    fun `empty and blank names are rejected`() {
        assertEquals(ListNameError.Empty, ListName.validate(""))
        assertEquals(ListNameError.Empty, ListName.validate("   "))
    }

    @Test
    fun `name is checked after trimming`() {
        assertNull(ListName.validate("  ${"a".repeat(ListName.MAX_LENGTH)}  "))
    }

    @Test
    fun `name longer than the limit is rejected`() {
        assertNull(ListName.validate("a".repeat(ListName.MAX_LENGTH)))
        assertEquals(ListNameError.TooLong, ListName.validate("a".repeat(ListName.MAX_LENGTH + 1)))
    }

    @Test
    fun `Watchlist is reserved in any case and with whitespace`() {
        assertEquals(ListNameError.Reserved, ListName.validate("Watchlist"))
        assertEquals(ListNameError.Reserved, ListName.validate("  WATCHLIST "))
        assertEquals(ListNameError.Reserved, ListName.validate("watchlist"))
    }

    @Test
    fun `names that only contain Watchlist are allowed`() {
        assertNull(ListName.validate("My Watchlist"))
    }

    @Test
    fun `key ignores case and surrounding whitespace`() {
        assertEquals(ListName.keyOf("Horror"), ListName.keyOf("horror "))
        assertEquals("horror", ListName.keyOf("  HORROR"))
    }
}
