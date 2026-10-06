package com.example.popcornroute.userlibrary.domain.model

/** Identity of a title: `tmdbId` alone is not unique across movies and TV. */
data class TitleKey(
    val mediaType: MediaType,
    val tmdbId: Int,
)
