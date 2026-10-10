package com.example.popcornroute.home.benchmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.example.popcornroute.R
import com.example.popcornroute.home.domain.model.HomeContent
import com.example.popcornroute.home.domain.model.HomeMedia
import com.example.popcornroute.home.domain.model.HomeMediaType
import com.example.popcornroute.home.presentation.HomeScreen
import com.example.popcornroute.home.presentation.HomeUiState
import com.example.popcornroute.ui.theme.PopcornRouteTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Deterministic, benchmark-only host for measuring the real HomeScreen composable. */
class HomeBenchmarkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val shimmerOnly = intent.getBooleanExtra(EXTRA_SHIMMER_ONLY, false)

        setContent {
            PopcornRouteTheme(dynamicColor = false) {
                var state by remember {
                    mutableStateOf(
                        if (shimmerOnly) {
                            HomeUiState(isCheckingConnectivity = false, isLoading = true)
                        } else {
                            HomeUiState(content = benchmarkContent, isCheckingConnectivity = false)
                        },
                    )
                }
                val scope = rememberCoroutineScope()

                LaunchedEffect(shimmerOnly) {
                    if (!shimmerOnly) return@LaunchedEffect
                    // Keep producing shimmer frames for the full measured interval.
                    state = state.copy(isLoading = true)
                }

                HomeScreen(
                    state = state,
                    onRefresh = {
                        if (!state.isRefreshing) {
                            state = state.copy(isRefreshing = true, refreshError = null)
                            scope.launch {
                                delay(REFRESH_DURATION_MILLIS)
                                state = state.copy(isRefreshing = false)
                            }
                        }
                    },
                    onRetry = {},
                    onFilterSelected = { filter -> state = state.copy(selectedFilter = filter) },
                    onDismissRefreshError = {},
                    modifier = Modifier.semantics { testTagsAsResourceId = true },
                )
            }
        }
    }

    companion object {
        const val EXTRA_SHIMMER_ONLY = "shimmer_only"
        private const val REFRESH_DURATION_MILLIS = 1_200L
    }
}

private const val BENCHMARK_POSTER =
    "android.resource://com.example.popcornroute/${R.mipmap.ic_launcher}"

private fun media(index: Int, type: HomeMediaType) = HomeMedia(
    tmdbId = index,
    mediaType = type,
    title = if (type == HomeMediaType.MOVIE) "Benchmark Movie $index" else "Benchmark Show $index",
    posterUrl = BENCHMARK_POSTER,
    backdropUrl = BENCHMARK_POSTER,
    year = "2026",
)

private val benchmarkContent = HomeContent(
    featured = List(6) { index ->
        media(index + 1, if (index % 2 == 0) HomeMediaType.MOVIE else HomeMediaType.TV)
    },
    topMovies = List(10) { index -> media(index + 100, HomeMediaType.MOVIE) },
    topShows = List(10) { index -> media(index + 200, HomeMediaType.TV) },
    nowPlaying = List(20) { index -> media(index + 300, HomeMediaType.MOVIE) },
    comingSoon = List(20) { index -> media(index + 400, HomeMediaType.MOVIE) },
)
