package com.example.popcornroute.userlibrary.domain.model

/** TMDB title kind. TMDB movie and TV IDs are separate number spaces, so an ID needs its kind. */
enum class MediaType(val value: String) {
    MOVIE("movie"),
    TV("tv");

    companion object {
        fun fromValue(value: String): MediaType =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown media type: $value")
    }
}
