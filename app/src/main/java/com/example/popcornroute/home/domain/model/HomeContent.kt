package com.example.popcornroute.home.domain.model

enum class HomeMediaType(val value: String) {
    MOVIE("movie"),
    TV("tv"),
}

data class HomeMedia(
    val tmdbId: Int,
    val mediaType: HomeMediaType,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val year: String?,
) {
    val key: String = "${mediaType.value}:$tmdbId"
}

data class HomeContent(
    val featured: List<HomeMedia>,
    val topMovies: List<HomeMedia>,
    val topShows: List<HomeMedia>,
    val nowPlaying: List<HomeMedia>,
    val comingSoon: List<HomeMedia>,
)
