package com.example.popcornroute.userlibrary.domain.model

/**
 * Outcome of "Mark as watched".
 *
 * [changed] is false when the title was already watched, in which case nothing was touched.
 * [removedFromWatchlist] is the exact Watchlist membership that was removed, so Undo can restore it.
 */
data class MarkAsWatchedResult(
    val changed: Boolean,
    val removedFromWatchlist: ListMembership?,
)
