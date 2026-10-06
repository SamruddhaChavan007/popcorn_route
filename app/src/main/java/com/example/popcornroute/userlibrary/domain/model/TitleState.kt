package com.example.popcornroute.userlibrary.domain.model

/** Everything the user has saved about one title; drives the toggles on cards and details. */
data class TitleState(
    val isFavorite: Boolean,
    val isWatched: Boolean,
    val listIds: Set<String>,
) {
    val isInWatchlist: Boolean get() = LibraryList.WATCHLIST_ID in listIds
}
