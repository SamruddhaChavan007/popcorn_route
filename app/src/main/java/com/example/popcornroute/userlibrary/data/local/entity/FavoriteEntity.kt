package com.example.popcornroute.userlibrary.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "favorite",
    primaryKeys = ["userId", "mediaType", "tmdbId"],
)
data class FavoriteEntity(
    val userId: String,
    val mediaType: String,
    val tmdbId: Int,
    val addedAt: Long,
)
