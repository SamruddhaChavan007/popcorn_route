package com.example.popcornroute.userlibrary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.popcornroute.userlibrary.data.local.entity.WatchedEntity
import com.example.popcornroute.userlibrary.data.local.relation.LibraryTitleRow
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchedDao {

    /**
     * Returns the new row ID, or -1 if the title was already watched. Marking as watched with side
     * effects goes through [LibraryActionsDao.markAsWatched] instead.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(watched: WatchedEntity): Long

    @Query("DELETE FROM watched WHERE userId = :userId AND mediaType = :mediaType AND tmdbId = :tmdbId")
    suspend fun delete(userId: String, mediaType: String, tmdbId: Int): Int

    @Query(
        "SELECT EXISTS(SELECT 1 FROM watched " +
            "WHERE userId = :userId AND mediaType = :mediaType AND tmdbId = :tmdbId)",
    )
    fun observeIsWatched(userId: String, mediaType: String, tmdbId: Int): Flow<Boolean>

    @Query(
        """
        SELECT w.mediaType AS mediaType, w.tmdbId AS tmdbId, w.watchedAt AS savedAt,
               c.title AS title, c.posterPath AS posterPath, c.releaseYear AS releaseYear,
               c.rating AS rating, c.cachedAt AS cachedAt
        FROM watched w
        LEFT JOIN title_cache c ON c.mediaType = w.mediaType AND c.tmdbId = w.tmdbId
        WHERE w.userId = :userId
        ORDER BY w.watchedAt DESC
        """,
    )
    fun observeWatched(userId: String): Flow<List<LibraryTitleRow>>

    @Query("DELETE FROM watched WHERE userId = :userId")
    suspend fun deleteAll(userId: String)
}
