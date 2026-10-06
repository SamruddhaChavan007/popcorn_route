package com.example.popcornroute.userlibrary.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "watched",
    primaryKeys = ["userId", "mediaType", "tmdbId"],
)
data class WatchedEntity(
    val userId: String,
    val mediaType: String,
    val tmdbId: Int,
    val watchedAt: Long,
)
