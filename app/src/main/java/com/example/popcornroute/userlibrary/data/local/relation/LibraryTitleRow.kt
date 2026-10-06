package com.example.popcornroute.userlibrary.data.local.relation

/**
 * A saved title joined to its cached display details. The cache columns are null when the title is
 * not cached yet, because queries use a LEFT JOIN so saved titles never disappear.
 */
data class LibraryTitleRow(
    val mediaType: String,
    val tmdbId: Int,
    val savedAt: Long,
    val title: String?,
    val posterPath: String?,
    val releaseYear: Int?,
    val rating: Double?,
    val cachedAt: Long?,
)
