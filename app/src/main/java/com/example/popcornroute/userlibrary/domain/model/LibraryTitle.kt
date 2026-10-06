package com.example.popcornroute.userlibrary.domain.model

/**
 * A saved title in Favorites, Watched, or a list.
 *
 * [savedAt] is when it was favorited, watched, or added to the list. [summary] is null while the
 * title's display details are not cached yet; the title must still be shown, as a placeholder.
 */
data class LibraryTitle(
    val key: TitleKey,
    val savedAt: Long,
    val summary: TitleSummary?,
)
