package com.example.popcornroute.userlibrary.domain.model

/** Display details for a title, taken from the TMDB cache. */
data class TitleSummary(
    val title: String,
    val posterPath: String?,
    val releaseYear: Int?,
    val rating: Double?,
    val cachedAt: Long,
)
