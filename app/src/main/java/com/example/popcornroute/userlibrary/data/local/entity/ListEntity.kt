package com.example.popcornroute.userlibrary.data.local.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * The built-in Watchlist (`listId = "watchlist"`) or a custom list. The rule that `isBuiltIn` is true
 * exactly for the Watchlist ID is enforced by triggers in `UserLibraryDatabase`.
 */
@Entity(
    tableName = "list",
    primaryKeys = ["userId", "listId"],
    indices = [Index(value = ["userId", "nameKey"], unique = true)],
)
data class ListEntity(
    val userId: String,
    val listId: String,
    val name: String,
    val nameKey: String,
    val isBuiltIn: Boolean,
    val createdAt: Long,
)
