package com.example.popcornroute.userlibrary.data.local

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import com.example.popcornroute.userlibrary.data.local.entity.FavoriteEntity
import com.example.popcornroute.userlibrary.data.local.entity.ListEntity
import com.example.popcornroute.userlibrary.data.local.entity.ListItemEntity
import com.example.popcornroute.userlibrary.data.local.entity.TitleCacheEntity
import com.example.popcornroute.userlibrary.data.local.entity.WatchedEntity
import com.example.popcornroute.userlibrary.data.mapper.newCustomListEntity
import com.example.popcornroute.userlibrary.data.mapper.newWatchlistEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Runs the real Room schema, queries and triggers on an in-memory database. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class UserLibraryDatabaseTest {

    private lateinit var db: UserLibraryDatabase

    private val user = "u1"
    private val otherUser = "u2"
    private val movie = 603
    private val show = 1399

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), UserLibraryDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(UserLibraryDatabase.callback)
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun favorite(at: Long, id: Int = movie, userId: String = user, type: String = "movie") =
        FavoriteEntity(userId, type, id, at)

    private fun watched(at: Long, id: Int = movie, userId: String = user) =
        WatchedEntity(userId, "movie", id, at)

    private fun item(listId: String, at: Long, id: Int = movie, userId: String = user) =
        ListItemEntity(userId, listId, "movie", id, at)

    private suspend fun ensureWatchlist(userId: String = user) =
        db.listDao().ensureWatchlist(newWatchlistEntity(userId, now = 1L))

    // ---- favorites ----

    @Test
    fun `adding a favorite twice keeps the original time`() = runTest {
        assertNotEquals(-1L, db.favoriteDao().insertIgnore(favorite(at = 100)))
        assertEquals(-1L, db.favoriteDao().insertIgnore(favorite(at = 999)))

        val rows = db.favoriteDao().observeFavorites(user).first()
        assertEquals(1, rows.size)
        assertEquals(100L, rows.single().savedAt)
    }

    @Test
    fun `unfavorite then favorite again records a new time`() = runTest {
        db.favoriteDao().insertIgnore(favorite(at = 100))
        assertEquals(1, db.favoriteDao().delete(user, "movie", movie))
        db.favoriteDao().insertIgnore(favorite(at = 500))

        assertEquals(500L, db.favoriteDao().observeFavorites(user).first().single().savedAt)
    }

    @Test
    fun `removing a missing favorite is a no-op`() = runTest {
        assertEquals(0, db.favoriteDao().delete(user, "movie", movie))
    }

    @Test
    fun `movie and tv with the same tmdb id are different titles`() = runTest {
        db.favoriteDao().insertIgnore(favorite(at = 1, id = 7, type = "movie"))
        db.favoriteDao().insertIgnore(favorite(at = 2, id = 7, type = "tv"))

        assertEquals(2, db.favoriteDao().observeFavorites(user).first().size)
        assertTrue(db.favoriteDao().observeIsFavorite(user, "tv", 7).first())
        db.favoriteDao().delete(user, "tv", 7)
        assertFalse(db.favoriteDao().observeIsFavorite(user, "tv", 7).first())
        assertTrue(db.favoriteDao().observeIsFavorite(user, "movie", 7).first())
    }

    @Test
    fun `favorites are ordered newest first and separated per user`() = runTest {
        db.favoriteDao().insertIgnore(favorite(at = 10, id = 1))
        db.favoriteDao().insertIgnore(favorite(at = 30, id = 3))
        db.favoriteDao().insertIgnore(favorite(at = 20, id = 2))
        db.favoriteDao().insertIgnore(favorite(at = 99, id = 9, userId = otherUser))

        assertEquals(listOf(3, 2, 1), db.favoriteDao().observeFavorites(user).first().map { it.tmdbId })
        assertEquals(listOf(9), db.favoriteDao().observeFavorites(otherUser).first().map { it.tmdbId })
    }

    @Test
    fun `favorite without a cache row still appears with no details`() = runTest {
        db.favoriteDao().insertIgnore(favorite(at = 1))

        val row = db.favoriteDao().observeFavorites(user).first().single()

        assertEquals(movie, row.tmdbId)
        assertNull(row.title)
        assertNull(row.cachedAt)
    }

    @Test
    fun `cache row is joined once it exists and can be refreshed`() = runTest {
        db.favoriteDao().insertIgnore(favorite(at = 1))
        db.titleCacheDao().upsert(TitleCacheEntity("movie", movie, "The Matrix", "/old.jpg", 1999, 8.1, 10L))
        db.titleCacheDao().upsert(TitleCacheEntity("movie", movie, "The Matrix", "/new.jpg", 1999, 8.2, 20L))

        val row = db.favoriteDao().observeFavorites(user).first().single()

        assertEquals("The Matrix", row.title)
        assertEquals("/new.jpg", row.posterPath)
        assertEquals(20L, row.cachedAt)
        assertEquals(1L, row.savedAt)
    }

    // ---- watched ----

    @Test
    fun `marking watched twice keeps the original time`() = runTest {
        assertNotEquals(-1L, db.watchedDao().insertIgnore(watched(at = 100)))
        assertEquals(-1L, db.watchedDao().insertIgnore(watched(at = 999)))

        assertEquals(100L, db.watchedDao().observeWatched(user).first().single().savedAt)
    }

    @Test
    fun `mark as not watched removes only the watched row`() = runTest {
        ensureWatchlist()
        db.favoriteDao().insertIgnore(favorite(at = 1))
        db.listItemDao().insertIgnore(item("watchlist", at = 2))
        db.watchedDao().insertIgnore(watched(at = 3))

        db.watchedDao().delete(user, "movie", movie)

        assertFalse(db.watchedDao().observeIsWatched(user, "movie", movie).first())
        assertTrue(db.favoriteDao().observeIsFavorite(user, "movie", movie).first())
        assertEquals(listOf("watchlist"), db.listItemDao().observeListIdsContaining(user, "movie", movie).first())
    }

    // ---- lists ----

    @Test
    fun `ensuring the watchlist is idempotent`() = runTest {
        assertNotEquals(-1L, ensureWatchlist())
        assertEquals(-1L, ensureWatchlist())

        val lists = db.listDao().observeLists(user).first()
        assertEquals(listOf("watchlist"), lists.map { it.listId })
        assertTrue(lists.single().isBuiltIn)
    }

    @Test
    fun `lists are returned with the watchlist first then custom lists oldest first`() = runTest {
        db.listDao().insert(newCustomListEntity(user, "b", "Second", now = 30L))
        db.listDao().insert(newCustomListEntity(user, "a", "First", now = 20L))
        db.listDao().ensureWatchlist(newWatchlistEntity(user, now = 50L))

        assertEquals(listOf("watchlist", "a", "b"), db.listDao().observeLists(user).first().map { it.listId })
    }

    @Test
    fun `duplicate list name ignoring case and whitespace is rejected`() = runTest {
        db.listDao().insert(newCustomListEntity(user, "a", "Horror", now = 1L))

        assertThrows(SQLiteConstraintException::class.java) {
            kotlinx.coroutines.runBlocking {
                db.listDao().insert(newCustomListEntity(user, "b", " horror ", now = 2L))
            }
        }
    }

    @Test
    fun `same list name is allowed for different users`() = runTest {
        db.listDao().insert(newCustomListEntity(user, "a", "Horror", now = 1L))
        db.listDao().insert(newCustomListEntity(otherUser, "b", "Horror", now = 1L))

        assertEquals(1, db.listDao().observeLists(otherUser).first().size)
    }

    @Test
    fun `a custom list cannot take the watchlist name`() = runTest {
        ensureWatchlist()

        assertThrows(SQLiteConstraintException::class.java) {
            kotlinx.coroutines.runBlocking {
                db.listDao().insert(newCustomListEntity(user, "a", "WATCHLIST", now = 2L))
            }
        }
    }

    @Test
    fun `renaming changes only that list and rejects a taken name`() = runTest {
        db.listDao().insert(newCustomListEntity(user, "a", "Horror", now = 1L))
        db.listDao().insert(newCustomListEntity(user, "b", "Comedy", now = 2L))

        assertEquals(1, db.listDao().rename(user, "a", "Scary", "scary"))
        assertEquals(
            listOf("Scary", "Comedy"),
            db.listDao().observeLists(user).first().map { it.name },
        )
        assertThrows(SQLiteConstraintException::class.java) {
            kotlinx.coroutines.runBlocking { db.listDao().rename(user, "a", "comedy", "comedy") }
        }
    }

    @Test
    fun `the watchlist cannot be renamed or deleted`() = runTest {
        ensureWatchlist()

        assertEquals(0, db.listDao().rename(user, "watchlist", "Later", "later"))
        assertEquals(0, db.listDao().deleteCustom(user, "watchlist"))
        assertEquals("Watchlist", db.listDao().observeLists(user).first().single().name)
    }

    @Test
    fun `built-in flag must match the watchlist id`() = runTest {
        val badWatchlist = ListEntity(user, "watchlist", "Watchlist", "watchlist", isBuiltIn = false, createdAt = 1L)
        val badCustom = ListEntity(user, "x", "Mine", "mine", isBuiltIn = true, createdAt = 1L)

        assertThrows(SQLiteConstraintException::class.java) {
            kotlinx.coroutines.runBlocking { db.listDao().insert(badWatchlist) }
        }
        assertThrows(SQLiteConstraintException::class.java) {
            kotlinx.coroutines.runBlocking { db.listDao().insert(badCustom) }
        }
    }

    @Test
    fun `built-in flag cannot be changed by an update`() = runTest {
        db.listDao().insert(newCustomListEntity(user, "a", "Horror", now = 1L))

        assertThrows(SQLiteConstraintException::class.java) {
            db.openHelper.writableDatabase.execSQL("UPDATE list SET isBuiltIn = 1 WHERE listId = 'a'")
        }
    }

    // ---- list items ----

    @Test
    fun `adding to a list twice keeps the original time`() = runTest {
        db.listDao().insert(newCustomListEntity(user, "a", "Horror", now = 1L))

        assertNotEquals(-1L, db.listItemDao().insertIgnore(item("a", at = 100)))
        assertEquals(-1L, db.listItemDao().insertIgnore(item("a", at = 999)))

        assertEquals(100L, db.listItemDao().observeItems(user, "a").first().single().savedAt)
    }

    @Test
    fun `a title can be in several lists`() = runTest {
        ensureWatchlist()
        db.listDao().insert(newCustomListEntity(user, "a", "Horror", now = 1L))
        db.listItemDao().insertIgnore(item("watchlist", at = 1))
        db.listItemDao().insertIgnore(item("a", at = 2))

        assertEquals(
            setOf("watchlist", "a"),
            db.listItemDao().observeListIdsContaining(user, "movie", movie).first().toSet(),
        )
    }

    @Test
    fun `items need an existing list`() = runTest {
        assertThrows(SQLiteConstraintException::class.java) {
            kotlinx.coroutines.runBlocking { db.listItemDao().insertIgnore(item("missing", at = 1)) }
        }
    }

    @Test
    fun `removing from a list leaves other lists alone`() = runTest {
        ensureWatchlist()
        db.listDao().insert(newCustomListEntity(user, "a", "Horror", now = 1L))
        db.listItemDao().insertIgnore(item("watchlist", at = 1))
        db.listItemDao().insertIgnore(item("a", at = 2))

        db.listItemDao().delete(user, "a", "movie", movie)

        assertEquals(listOf("watchlist"), db.listItemDao().observeListIdsContaining(user, "movie", movie).first())
    }

    @Test
    fun `deleting a custom list removes its items only`() = runTest {
        ensureWatchlist()
        db.listDao().insert(newCustomListEntity(user, "a", "Horror", now = 1L))
        db.listDao().insert(newCustomListEntity(user, "b", "Comedy", now = 2L))
        db.favoriteDao().insertIgnore(favorite(at = 1))
        db.watchedDao().insertIgnore(watched(at = 2))
        db.listItemDao().insertIgnore(item("watchlist", at = 3))
        db.listItemDao().insertIgnore(item("a", at = 4))
        db.listItemDao().insertIgnore(item("b", at = 5))

        assertEquals(1, db.listDao().deleteCustom(user, "a"))

        assertTrue(db.listItemDao().observeItems(user, "a").first().isEmpty())
        assertEquals(
            setOf("watchlist", "b"),
            db.listItemDao().observeListIdsContaining(user, "movie", movie).first().toSet(),
        )
        assertTrue(db.favoriteDao().observeIsFavorite(user, "movie", movie).first())
        assertTrue(db.watchedDao().observeIsWatched(user, "movie", movie).first())
    }

    @Test
    fun `a deleted list name can be reused`() = runTest {
        db.listDao().insert(newCustomListEntity(user, "a", "Horror", now = 1L))
        db.listDao().deleteCustom(user, "a")

        db.listDao().insert(newCustomListEntity(user, "b", "Horror", now = 2L))

        assertEquals(listOf("b"), db.listDao().observeLists(user).first().map { it.listId })
    }

    @Test
    fun `list items without a cache row still appear`() = runTest {
        ensureWatchlist()
        db.listItemDao().insertIgnore(item("watchlist", at = 1))

        val row = db.listItemDao().observeItems(user, "watchlist").first().single()

        assertNull(row.title)
    }

    // ---- title cache ----

    @Test
    fun `deleting old cache rows keeps fresh ones`() = runTest {
        db.titleCacheDao().upsertAll(
            listOf(
                TitleCacheEntity("movie", 1, "Old", null, null, null, cachedAt = 10L),
                TitleCacheEntity("movie", 2, "Fresh", null, null, null, cachedAt = 100L),
            ),
        )

        assertEquals(1, db.titleCacheDao().deleteOlderThan(50L))

        assertNull(db.titleCacheDao().get("movie", 1))
        assertNotNull(db.titleCacheDao().get("movie", 2))
    }

    // ---- watchlist add ----

    @Test
    fun `adding to the watchlist creates the row and ignores repeats`() = runTest {
        val actions = db.libraryActionsDao()

        assertTrue(actions.addToWatchlist(newWatchlistEntity(user, 1L), item("watchlist", at = 100)))
        assertFalse(actions.addToWatchlist(newWatchlistEntity(user, 2L), item("watchlist", at = 999)))

        assertEquals(100L, db.listItemDao().observeItems(user, "watchlist").first().single().savedAt)
        assertEquals(1, db.listDao().observeLists(user).first().size)
    }

    // ---- mark as watched ----

    @Test
    fun `marking watched removes the title from the watchlist and returns the removed row`() = runTest {
        ensureWatchlist()
        db.listItemDao().insertIgnore(item("watchlist", at = 42))

        val outcome = db.libraryActionsDao().markAsWatched(newWatchlistEntity(user, 1L), watched(at = 500))

        assertTrue(outcome.changed)
        assertEquals(item("watchlist", at = 42), outcome.removedWatchlistItem)
        assertEquals(500L, db.watchedDao().observeWatched(user).first().single().savedAt)
        assertTrue(db.listItemDao().observeItems(user, "watchlist").first().isEmpty())
    }

    @Test
    fun `marking watched when not in the watchlist removes nothing`() = runTest {
        val outcome = db.libraryActionsDao().markAsWatched(newWatchlistEntity(user, 1L), watched(at = 500))

        assertTrue(outcome.changed)
        assertNull(outcome.removedWatchlistItem)
    }

    @Test
    fun `marking watched creates the watchlist row if it is missing`() = runTest {
        db.libraryActionsDao().markAsWatched(newWatchlistEntity(user, 1L), watched(at = 500))

        assertEquals(listOf("watchlist"), db.listDao().observeLists(user).first().map { it.listId })
    }

    @Test
    fun `marking watched leaves favorites and custom lists untouched`() = runTest {
        ensureWatchlist()
        db.listDao().insert(newCustomListEntity(user, "a", "Horror", now = 1L))
        db.favoriteDao().insertIgnore(favorite(at = 1))
        db.listItemDao().insertIgnore(item("a", at = 2))
        db.listItemDao().insertIgnore(item("watchlist", at = 3))

        db.libraryActionsDao().markAsWatched(newWatchlistEntity(user, 1L), watched(at = 500))

        assertTrue(db.favoriteDao().observeIsFavorite(user, "movie", movie).first())
        assertEquals(listOf("a"), db.listItemDao().observeListIdsContaining(user, "movie", movie).first())
    }

    @Test
    fun `marking an already watched title changes nothing and keeps a rewatch in the watchlist`() = runTest {
        val actions = db.libraryActionsDao()
        actions.markAsWatched(newWatchlistEntity(user, 1L), watched(at = 100))
        actions.addToWatchlist(newWatchlistEntity(user, 1L), item("watchlist", at = 200))

        val outcome = actions.markAsWatched(newWatchlistEntity(user, 1L), watched(at = 999))

        assertFalse(outcome.changed)
        assertNull(outcome.removedWatchlistItem)
        assertEquals(100L, db.watchedDao().observeWatched(user).first().single().savedAt)
        assertEquals(200L, db.listItemDao().observeItems(user, "watchlist").first().single().savedAt)
    }

    @Test
    fun `marking one title watched does not affect another title or user`() = runTest {
        ensureWatchlist()
        ensureWatchlist(otherUser)
        db.listItemDao().insertIgnore(item("watchlist", at = 1, id = show))
        db.listItemDao().insertIgnore(item("watchlist", at = 2, userId = otherUser))

        db.libraryActionsDao().markAsWatched(newWatchlistEntity(user, 1L), watched(at = 500))

        assertEquals(1, db.listItemDao().observeItems(user, "watchlist").first().size)
        assertEquals(1, db.listItemDao().observeItems(otherUser, "watchlist").first().size)
    }

    // ---- undo ----

    @Test
    fun `undo restores the exact watchlist row with its original time`() = runTest {
        ensureWatchlist()
        db.listItemDao().insertIgnore(item("watchlist", at = 42))
        db.listItemDao().insertIgnore(item("watchlist", at = 77, id = show))
        val actions = db.libraryActionsDao()
        val outcome = actions.markAsWatched(newWatchlistEntity(user, 1L), watched(at = 500))

        actions.undoMarkAsWatched(newWatchlistEntity(user, 1L), user, "movie", movie, outcome.removedWatchlistItem)

        assertFalse(db.watchedDao().observeIsWatched(user, "movie", movie).first())
        val rows = db.listItemDao().observeItems(user, "watchlist").first()
        assertEquals(setOf(movie to 42L, show to 77L), rows.map { it.tmdbId to it.savedAt }.toSet())
    }

    @Test
    fun `undo when nothing was removed does not add to the watchlist`() = runTest {
        val actions = db.libraryActionsDao()
        val outcome = actions.markAsWatched(newWatchlistEntity(user, 1L), watched(at = 500))

        actions.undoMarkAsWatched(newWatchlistEntity(user, 1L), user, "movie", movie, outcome.removedWatchlistItem)

        assertFalse(db.watchedDao().observeIsWatched(user, "movie", movie).first())
        assertTrue(db.listItemDao().observeItems(user, "watchlist").first().isEmpty())
    }

    @Test
    fun `undo leaves favorites and custom lists untouched`() = runTest {
        ensureWatchlist()
        db.listDao().insert(newCustomListEntity(user, "a", "Horror", now = 1L))
        db.favoriteDao().insertIgnore(favorite(at = 1))
        db.listItemDao().insertIgnore(item("a", at = 2))
        db.listItemDao().insertIgnore(item("watchlist", at = 3))
        val actions = db.libraryActionsDao()
        val outcome = actions.markAsWatched(newWatchlistEntity(user, 1L), watched(at = 500))

        actions.undoMarkAsWatched(newWatchlistEntity(user, 1L), user, "movie", movie, outcome.removedWatchlistItem)

        assertTrue(db.favoriteDao().observeIsFavorite(user, "movie", movie).first())
        assertEquals(
            setOf("a", "watchlist"),
            db.listItemDao().observeListIdsContaining(user, "movie", movie).first().toSet(),
        )
    }

    // ---- sign-out cleanup ----

    @Test
    fun `clearing a user removes only that user's library`() = runTest {
        ensureWatchlist()
        ensureWatchlist(otherUser)
        db.favoriteDao().insertIgnore(favorite(at = 1))
        db.favoriteDao().insertIgnore(favorite(at = 1, userId = otherUser))
        db.watchedDao().insertIgnore(watched(at = 1))
        db.listItemDao().insertIgnore(item("watchlist", at = 1))

        db.favoriteDao().deleteAll(user)
        db.watchedDao().deleteAll(user)
        db.listDao().deleteAll(user)

        assertTrue(db.favoriteDao().observeFavorites(user).first().isEmpty())
        assertTrue(db.watchedDao().observeWatched(user).first().isEmpty())
        assertTrue(db.listDao().observeLists(user).first().isEmpty())
        assertTrue(db.listItemDao().observeListIdsContaining(user, "movie", movie).first().isEmpty())
        assertEquals(1, db.favoriteDao().observeFavorites(otherUser).first().size)
        assertEquals(1, db.listDao().observeLists(otherUser).first().size)
    }

    private fun assertNotEquals(unexpected: Long, actual: Long) {
        org.junit.Assert.assertNotEquals(unexpected, actual)
    }
}
