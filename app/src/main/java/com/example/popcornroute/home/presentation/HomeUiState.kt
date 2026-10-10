package com.example.popcornroute.home.presentation

import com.example.popcornroute.home.domain.model.HomeContent

enum class HomeFilter(val id: String) {
    FOR_YOU("for_you"),
    MOVIES("movies"),
    TV_SHOWS("tv_shows"),
}

sealed interface HomeError {
    val message: String

    data object Offline : HomeError {
        override val message = "You're offline. Check your connection and try again."
    }

    data class RequestFailed(
        override val message: String = "We couldn't load Home. Please try again.",
    ) : HomeError
}

data class HomeUiState(
    val content: HomeContent? = null,
    val selectedFilter: HomeFilter = HomeFilter.FOR_YOU,
    val isCheckingConnectivity: Boolean = true,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val blockingError: HomeError? = null,
    val refreshError: HomeError? = null,
)