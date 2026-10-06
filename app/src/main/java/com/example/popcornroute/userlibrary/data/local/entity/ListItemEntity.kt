package com.example.popcornroute.userlibrary.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "list_item",
    primaryKeys = ["userId", "listId", "mediaType", "tmdbId"],
    foreignKeys = [
        ForeignKey(
            entity = ListEntity::class,
            parentColumns = ["userId", "listId"],
            childColumns = ["userId", "listId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["userId", "mediaType", "tmdbId"])],
)
data class ListItemEntity(
    val userId: String,
    val listId: String,
    val mediaType: String,
    val tmdbId: Int,
    val addedAt: Long,
)
