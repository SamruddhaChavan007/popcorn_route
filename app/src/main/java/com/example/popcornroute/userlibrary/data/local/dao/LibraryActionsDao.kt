package com.example.popcornroute.userlibrary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.popcornroute.userlibrary.data.local.entity.ListEntity
import com.example.popcornroute.userlibrary.data.local.entity.ListItemEntity
import com.example.popcornroute.userlibrary.data.local.entity.WatchedEntity

/** Outcome of [LibraryActionsDao.markAsWatched]. */
data class MarkAsWatchedOutcome(
    val changed: Boolean,
    val removedWatchlistItem: ListItemEntity?,
)

/**
 * Library actions that change several tables and must be atomic. Each runs in one transaction and
 * ensures the Watchlist row exists first.
 */
@Dao
abstract class LibraryActionsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun ensureWatchlist(watchlist: ListEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertWatchedIgnore(watched: WatchedEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertListItemIgnore(item: ListItemEntity): Long

    @Query(
        "SELECT * FROM list_item WHERE userId = :userId AND listId = 'watchlist' " +
            "AND mediaType = :mediaType AND tmdbId = :tmdbId",
    )
    protected abstract suspend fun findWatchlistItem(
        userId: String,
        mediaType: String,
        tmdbId: Int,
    ): ListItemEntity?

    @Query(
        "DELETE FROM list_item WHERE userId = :userId AND listId = 'watchlist' " +
            "AND mediaType = :mediaType AND tmdbId = :tmdbId",
    )
    protected abstract suspend fun deleteWatchlistItem(userId: String, mediaType: String, tmdbId: Int): Int

    @Query("DELETE FROM watched WHERE userId = :userId AND mediaType = :mediaType AND tmdbId = :tmdbId")
    protected abstract suspend fun deleteWatched(userId: String, mediaType: String, tmdbId: Int): Int

    /** Adds the title to the Watchlist. Returns false if it was already there (nothing changes). */
    @Transaction
    open suspend fun addToWatchlist(watchlist: ListEntity, item: ListItemEntity): Boolean {
        ensureWatchlist(watchlist)
        return insertListItemIgnore(item) != -1L
    }

    /**
     * Marks the title as watched. Only if it was not watched before, also removes it from the
     * Watchlist and returns that membership for Undo. An already-watched title is left untouched,
     * including a Watchlist membership re-added for a rewatch.
     */
    @Transaction
    open suspend fun markAsWatched(watchlist: ListEntity, watched: WatchedEntity): MarkAsWatchedOutcome {
        ensureWatchlist(watchlist)
        if (insertWatchedIgnore(watched) == -1L) return MarkAsWatchedOutcome(false, null)
        val removed = findWatchlistItem(watched.userId, watched.mediaType, watched.tmdbId)
        if (removed != null) deleteWatchlistItem(watched.userId, watched.mediaType, watched.tmdbId)
        return MarkAsWatchedOutcome(true, removed)
    }

    /** Undoes [markAsWatched]: removes the watched state and restores the exact Watchlist row. */
    @Transaction
    open suspend fun undoMarkAsWatched(
        watchlist: ListEntity,
        userId: String,
        mediaType: String,
        tmdbId: Int,
        removedWatchlistItem: ListItemEntity?,
    ) {
        ensureWatchlist(watchlist)
        deleteWatched(userId, mediaType, tmdbId)
        if (removedWatchlistItem != null) insertListItemIgnore(removedWatchlistItem)
    }
}
