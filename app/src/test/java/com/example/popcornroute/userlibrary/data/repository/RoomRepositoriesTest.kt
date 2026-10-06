package com.example.popcornroute.userlibrary.data.repository

import android.app.Application
import androidx.room.Room
import com.example.popcornroute.userlibrary.data.local.UserLibraryDatabase
import com.example.popcornroute.userlibrary.domain.model.CreateListResult
import com.example.popcornroute.userlibrary.domain.model.LibraryList
import com.example.popcornroute.userlibrary.domain.model.ListNameError
import com.example.popcornroute.userlibrary.domain.model.MediaType
import com.example.popcornroute.userlibrary.domain.model.RenameListResult
import com.example.popcornroute.userlibrary.domain.model.TitleKey
import com.example.popcornroute.userlibrary.domain.model.TitleState
import com.example.popcornroute.userlibrary.domain.repository.CurrentUserProvider
import com.example.popcornroute.userlibrary.domain.usecase.ObserveTitleStateUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class RoomRepositoriesTest {

    private class MutableClock(var now: Long = 1_000L) : Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?) = this
        override fun instant(): Instant = Instant.ofEpochMilli(now)
    }

    private class FakeUser(override var userId: String? = "u1") : CurrentUserProvider

    private lateinit var db: UserLibraryDatabase
    private lateinit var user: FakeUser
    private lateinit var clock: MutableClock
    private lateinit var favorites: RoomFavoritesRepository
    private lateinit var watched: RoomWatchedRepository
    private lateinit var lists: RoomListsRepository
    private var nextId = 0

    private val matrix = TitleKey(MediaType.MOVIE, 603)
    private val got = TitleKey(MediaType.TV, 1399)

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), UserLibraryDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(UserLibraryDatabase.callback)
            .build()
        user = FakeUser()
        clock = MutableClock()
        favorites = RoomFavoritesRepository(db.favoriteDao(), user, clock)
        watched = RoomWatchedRepository(db.watchedDao(), db.libraryActionsDao(), user, clock)
        lists = RoomListsRepository(
            db.listDao(), db.listItemDao(), db.libraryActionsDao(), user, clock,
            newListId = { "list-${nextId++}" },
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ---- favorites ----

    @Test
    fun `setting favorite on twice keeps the first time, off then on gets a new one`() = runTest {
        favorites.setFavorite(matrix, true)
        clock.now = 5_000
        favorites.setFavorite(matrix, true)
        assertEquals(1_000L, favorites.observeFavorites().first().single().savedAt)

        favorites.setFavorite(matrix, false)
        assertFalse(favorites.observeIsFavorite(matrix).first())
        clock.now = 9_000
        favorites.setFavorite(matrix, true)
        assertEquals(9_000L, favorites.observeFavorites().first().single().savedAt)
    }

    @Test
    fun `favorites are scoped to the signed in user`() = runTest {
        favorites.setFavorite(matrix, true)

        user.userId = "u2"

        assertTrue(favorites.observeFavorites().first().isEmpty())
    }

    @Test
    fun `using the library while signed out fails loudly`() {
        user.userId = null

        assertThrows(IllegalStateException::class.java) { favorites.observeFavorites() }
    }

    // ---- watched ----

    @Test
    fun `marking watched moves the title out of the watchlist and undo puts it back unchanged`() = runTest {
        clock.now = 100
        lists.setInList(LibraryList.WATCHLIST_ID, matrix, true)
        clock.now = 500

        val result = watched.markAsWatched(matrix)

        assertTrue(result.changed)
        assertTrue(watched.observeIsWatched(matrix).first())
        assertTrue(lists.observeItems(LibraryList.WATCHLIST_ID).first().isEmpty())

        clock.now = 900
        watched.undoMarkAsWatched(matrix, result)

        assertFalse(watched.observeIsWatched(matrix).first())
        assertEquals(100L, lists.observeItems(LibraryList.WATCHLIST_ID).first().single().savedAt)
    }

    @Test
    fun `marking an already watched title again changes nothing and undoing that result is a no-op`() = runTest {
        watched.markAsWatched(matrix)
        lists.setInList(LibraryList.WATCHLIST_ID, matrix, true)

        val again = watched.markAsWatched(matrix)
        watched.undoMarkAsWatched(matrix, again)

        assertFalse(again.changed)
        assertTrue(watched.observeIsWatched(matrix).first())
        assertEquals(1, lists.observeItems(LibraryList.WATCHLIST_ID).first().size)
    }

    @Test
    fun `marking not watched keeps favorites and lists`() = runTest {
        favorites.setFavorite(matrix, true)
        watched.markAsWatched(matrix)

        watched.markAsNotWatched(matrix)

        assertFalse(watched.observeIsWatched(matrix).first())
        assertTrue(favorites.observeIsFavorite(matrix).first())
    }

    // ---- lists ----

    @Test
    fun `create list returns the list and rejects invalid and duplicate names`() = runTest {
        val created = lists.createList("  Horror ") as CreateListResult.Success
        assertEquals("Horror", created.list.name)
        assertFalse(created.list.isBuiltIn)

        assertEquals(CreateListResult.Failure(ListNameError.Empty), lists.createList("  "))
        assertEquals(CreateListResult.Failure(ListNameError.TooLong), lists.createList("a".repeat(51)))
        assertEquals(CreateListResult.Failure(ListNameError.Reserved), lists.createList("watchlist"))
        assertEquals(CreateListResult.Failure(ListNameError.Duplicate), lists.createList("HORROR"))
    }

    @Test
    fun `lists include the watchlist once it is ensured`() = runTest {
        lists.ensureWatchlist()
        lists.ensureWatchlist()
        lists.createList("Horror")

        assertEquals(listOf("Watchlist", "Horror"), lists.observeLists().first().map { it.name })
    }

    @Test
    fun `rename covers success, duplicate, invalid and the watchlist`() = runTest {
        lists.ensureWatchlist()
        val horror = (lists.createList("Horror") as CreateListResult.Success).list
        lists.createList("Comedy")

        assertEquals(RenameListResult.Success, lists.renameList(horror.id, "Scary"))
        assertEquals(RenameListResult.Success, lists.renameList(horror.id, "scary"))
        assertEquals(RenameListResult.Failure(ListNameError.Duplicate), lists.renameList(horror.id, " comedy"))
        assertEquals(RenameListResult.Failure(ListNameError.Empty), lists.renameList(horror.id, ""))
        assertEquals(RenameListResult.NotAllowed, lists.renameList(LibraryList.WATCHLIST_ID, "Later"))
        assertEquals(RenameListResult.NotAllowed, lists.renameList("missing", "Whatever"))
    }

    @Test
    fun `delete list removes a custom list but not the watchlist`() = runTest {
        lists.ensureWatchlist()
        val horror = (lists.createList("Horror") as CreateListResult.Success).list
        lists.setInList(horror.id, matrix, true)
        favorites.setFavorite(matrix, true)

        assertTrue(lists.deleteList(horror.id))
        assertFalse(lists.deleteList(LibraryList.WATCHLIST_ID))
        assertFalse(lists.deleteList("missing"))

        assertEquals(listOf("Watchlist"), lists.observeLists().first().map { it.name })
        assertTrue(lists.observeListIdsContaining(matrix).first().isEmpty())
        assertTrue(favorites.observeIsFavorite(matrix).first())
    }

    @Test
    fun `set in list adds once, keeps the first time, and removes`() = runTest {
        val horror = (lists.createList("Horror") as CreateListResult.Success).list

        clock.now = 100
        lists.setInList(horror.id, matrix, true)
        clock.now = 200
        lists.setInList(horror.id, matrix, true)
        assertEquals(100L, lists.observeItems(horror.id).first().single().savedAt)

        lists.setInList(horror.id, matrix, false)
        assertTrue(lists.observeItems(horror.id).first().isEmpty())
    }

    @Test
    fun `adding to the watchlist works before it was ensured`() = runTest {
        lists.setInList(LibraryList.WATCHLIST_ID, matrix, true)

        assertEquals(setOf(LibraryList.WATCHLIST_ID), lists.observeListIdsContaining(matrix).first())
    }

    // ---- title state ----

    @Test
    fun `title state combines favorite, watched and list membership`() = runTest {
        val observe = ObserveTitleStateUseCase(favorites, watched, lists)
        val horror = (lists.createList("Horror") as CreateListResult.Success).list
        assertEquals(TitleState(false, false, emptySet()), observe(got).first())

        favorites.setFavorite(got, true)
        watched.markAsWatched(got)
        lists.setInList(horror.id, got, true)
        lists.setInList(LibraryList.WATCHLIST_ID, got, true)

        val state = observe(got).first()
        assertTrue(state.isFavorite)
        assertTrue(state.isWatched)
        assertTrue(state.isInWatchlist)
        assertEquals(setOf(horror.id, LibraryList.WATCHLIST_ID), state.listIds)
    }
}
