package com.example.popcornroute.userlibrary.domain.model

/** The built-in Watchlist or a user-created custom list. */
data class LibraryList(
    val id: String,
    val name: String,
    val isBuiltIn: Boolean,
    val createdAt: Long,
) {
    companion object {
        /** Fixed ID of the built-in Watchlist. */
        const val WATCHLIST_ID = "watchlist"
        const val WATCHLIST_NAME = "Watchlist"
    }
}
