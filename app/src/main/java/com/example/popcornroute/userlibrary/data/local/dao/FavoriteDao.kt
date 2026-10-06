package com.example.popcornroute.userlibrary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.popcornroute.userlibrary.data.local.entity.FavoriteEntity
import com.example.popcornroute.userlibrary.data.local.relation.LibraryTitleRow
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    /** Returns the new row ID, or -1 if the title was already a favorite (nothing changes). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorite WHERE userId = :userId AND mediaType = :mediaType AND tmdbId = :tmdbId")
    suspend fun delete(userId: String, mediaType: String, tmdbId: Int): Int

    @Query(
        "SELECT EXISTS(SELECT 1 FROM favorite " +
            "WHERE userId = :userId AND mediaType = :mediaType AND tmdbId = :tmdbId)",
    )
    fun observeIsFavorite(userId: String, mediaType: String, tmdbId: Int): Flow<Boolean>

    @Query(
        """
        SELECT f.mediaType AS mediaType, f.tmdbId AS tmdbId, f.addedAt AS savedAt,
               c.title AS title, c.posterPath AS posterPath, c.releaseYear AS releaseYear,
               c.rating AS rating, c.cachedAt AS cachedAt
        FROM favorite f
        LEFT JOIN title_cache c ON c.mediaType = f.mediaType AND c.tmdbId = f.tmdbId
        WHERE f.userId = :userId
        ORDER BY f.addedAt DESC
        """,
    )
    fun observeFavorites(userId: String): Flow<List<LibraryTitleRow>>

    @Query("DELETE FROM favorite WHERE userId = :userId")
    suspend fun deleteAll(userId: String)
}
