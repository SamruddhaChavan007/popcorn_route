package com.example.popcornroute.userlibrary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.popcornroute.userlibrary.data.local.entity.TitleCacheEntity

/** The only DAO that overwrites whole rows: the cache just mirrors the latest TMDB data. */
@Dao
interface TitleCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(title: TitleCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(titles: List<TitleCacheEntity>)

    @Query("SELECT * FROM title_cache WHERE mediaType = :mediaType AND tmdbId = :tmdbId")
    suspend fun get(mediaType: String, tmdbId: Int): TitleCacheEntity?

    @Query("DELETE FROM title_cache WHERE cachedAt < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long): Int
}
