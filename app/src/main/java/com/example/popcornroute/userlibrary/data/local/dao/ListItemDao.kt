package com.example.popcornroute.userlibrary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.popcornroute.userlibrary.data.local.entity.ListItemEntity
import com.example.popcornroute.userlibrary.data.local.relation.LibraryTitleRow
import kotlinx.coroutines.flow.Flow

@Dao
interface ListItemDao {

    /**
     * Returns the new row ID, or -1 if the title was already in the list (nothing changes). Adding
     * to the Watchlist goes through [LibraryActionsDao.addToWatchlist] so its row is ensured first.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(item: ListItemEntity): Long

    @Query(
        "DELETE FROM list_item WHERE userId = :userId AND listId = :listId " +
            "AND mediaType = :mediaType AND tmdbId = :tmdbId",
    )
    suspend fun delete(userId: String, listId: String, mediaType: String, tmdbId: Int): Int

    @Query(
        """
        SELECT i.mediaType AS mediaType, i.tmdbId AS tmdbId, i.addedAt AS savedAt,
               c.title AS title, c.posterPath AS posterPath, c.releaseYear AS releaseYear,
               c.rating AS rating, c.cachedAt AS cachedAt
        FROM list_item i
        LEFT JOIN title_cache c ON c.mediaType = i.mediaType AND c.tmdbId = i.tmdbId
        WHERE i.userId = :userId AND i.listId = :listId
        ORDER BY i.addedAt DESC
        """,
    )
    fun observeItems(userId: String, listId: String): Flow<List<LibraryTitleRow>>

    /** IDs of every list (including the Watchlist) that contains the title. */
    @Query(
        "SELECT listId FROM list_item " +
            "WHERE userId = :userId AND mediaType = :mediaType AND tmdbId = :tmdbId",
    )
    fun observeListIdsContaining(userId: String, mediaType: String, tmdbId: Int): Flow<List<String>>
}
