package com.example.popcornroute.userlibrary.data.local.entity

import androidx.room.Entity

/** Display-only TMDB snapshot, shared across users. Not part of the user's library state. */
@Entity(
    tableName = "title_cache",
    primaryKeys = ["mediaType", "tmdbId"],
)
data class TitleCacheEntity(
    val mediaType: String,
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val releaseYear: Int?,
    val rating: Double?,
    val cachedAt: Long,
)
