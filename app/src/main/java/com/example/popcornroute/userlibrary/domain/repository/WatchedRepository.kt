package com.example.popcornroute.userlibrary.domain.repository

import com.example.popcornroute.userlibrary.domain.model.LibraryTitle
import com.example.popcornroute.userlibrary.domain.model.MarkAsWatchedResult
import com.example.popcornroute.userlibrary.domain.model.TitleKey
import kotlinx.coroutines.flow.Flow

interface WatchedRepository {
    /** Watched titles, most recently watched first. */
    fun observeWatched(): Flow<List<LibraryTitle>>

    fun observeIsWatched(key: TitleKey): Flow<Boolean>

    /**
     * Marks the title as watched. If it was not watched, it is also removed from the Watchlist; the
     * result carries the removed membership for [undoMarkAsWatched]. An already-watched title is left
     * untouched and the result has `changed = false`.
     */
    suspend fun markAsWatched(key: TitleKey): MarkAsWatchedResult

    /** Undoes a [markAsWatched] that changed something, restoring the exact Watchlist membership. */
    suspend fun undoMarkAsWatched(key: TitleKey, result: MarkAsWatchedResult)

    /** Clears the Watched state only; Watchlist, other lists and Favorites are untouched. */
    suspend fun markAsNotWatched(key: TitleKey)
}
