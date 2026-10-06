package com.example.popcornroute.userlibrary.domain.repository

import com.example.popcornroute.userlibrary.domain.model.CreateListResult
import com.example.popcornroute.userlibrary.domain.model.LibraryList
import com.example.popcornroute.userlibrary.domain.model.LibraryTitle
import com.example.popcornroute.userlibrary.domain.model.RenameListResult
import com.example.popcornroute.userlibrary.domain.model.TitleKey
import kotlinx.coroutines.flow.Flow

interface ListsRepository {
    /** The Watchlist first, then custom lists oldest first. */
    fun observeLists(): Flow<List<LibraryList>>

    /** Titles in a list (use [LibraryList.WATCHLIST_ID] for the Watchlist), newest first. */
    fun observeItems(listId: String): Flow<List<LibraryTitle>>

    /** IDs of every list, including the Watchlist, that contains the title. */
    fun observeListIdsContaining(key: TitleKey): Flow<Set<String>>

    /** Creates the user's Watchlist row if it is missing. Safe to call any number of times. */
    suspend fun ensureWatchlist()

    /**
     * Puts the title in, or takes it out of, a list so that its membership equals [inList]. Works for
     * the Watchlist and custom lists. Adding to a list it is already in keeps its original time.
     */
    suspend fun setInList(listId: String, key: TitleKey, inList: Boolean)

    suspend fun createList(name: String): CreateListResult

    suspend fun renameList(listId: String, name: String): RenameListResult

    /** Deletes a custom list and its memberships. Returns false if nothing was deleted (Watchlist or unknown). */
    suspend fun deleteList(listId: String): Boolean
}
