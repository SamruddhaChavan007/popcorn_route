package com.example.popcornroute.home.presentation

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.popcornroute.home.domain.model.HomeContent
import com.example.popcornroute.home.domain.model.HomeMedia
import com.example.popcornroute.home.domain.model.HomeMediaType
import com.example.popcornroute.ui.components.FeaturedCarousel
import com.example.popcornroute.ui.components.FeaturedCarouselItem
import com.example.popcornroute.ui.components.HorizontalFilterChip
import com.example.popcornroute.ui.components.HorizontalFilterChips
import com.example.popcornroute.ui.components.MediaPosterItem
import com.example.popcornroute.ui.components.MediaPosterRow
import com.example.popcornroute.ui.components.PullToRefreshContainer
import com.example.popcornroute.ui.components.TopTenMediaItem
import com.example.popcornroute.ui.components.TopTenMediaRow
import com.example.popcornroute.ui.theme.PopcornRouteTheme

const val HOME_SHIMMER_TAG = "home_shimmer"
const val HOME_ERROR_TAG = "home_error"
const val HOME_CONTENT_TAG = "home_content"

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    onMediaClick: (HomeMedia) -> Unit = {},
    onWatchlistClick: (HomeMedia) -> Unit = {},
    isInWatchlist: (HomeMedia) -> Boolean = { false },
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onFilterSelected = viewModel::onFilterSelected,
        onDismissRefreshError = viewModel::dismissRefreshError,
        onMediaClick = onMediaClick,
        onWatchlistClick = onWatchlistClick,
        isInWatchlist = isInWatchlist,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onFilterSelected: (HomeFilter) -> Unit,
    onDismissRefreshError: () -> Unit,
    modifier: Modifier = Modifier,
    onMediaClick: (HomeMedia) -> Unit = {},
    onWatchlistClick: (HomeMedia) -> Unit = {},
    isInWatchlist: (HomeMedia) -> Boolean = { false },
) {
    PullToRefreshContainer(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        enabled = !state.isLoading && !state.isCheckingConnectivity,
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            state.isLoading -> HomeShimmer()
            state.content != null -> HomeFeed(
                content = state.content,
                selectedFilter = state.selectedFilter,
                refreshError = state.refreshError,
                onRetry = onRetry,
                onDismissRefreshError = onDismissRefreshError,
                onFilterSelected = onFilterSelected,
                onMediaClick = onMediaClick,
                onWatchlistClick = onWatchlistClick,
                isInWatchlist = isInWatchlist,
            )
            state.blockingError != null -> HomeErrorContent(
                error = state.blockingError,
                onRetry = onRetry,
            )
            else -> Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun HomeFeed(
    content: HomeContent,
    selectedFilter: HomeFilter,
    refreshError: HomeError?,
    onRetry: () -> Unit,
    onDismissRefreshError: () -> Unit,
    onFilterSelected: (HomeFilter) -> Unit,
    onMediaClick: (HomeMedia) -> Unit,
    onWatchlistClick: (HomeMedia) -> Unit,
    isInWatchlist: (HomeMedia) -> Boolean,
) {
    val featured = content.featured.filterFor(selectedFilter)
    val showMovies = selectedFilter != HomeFilter.TV_SHOWS
    val showTv = selectedFilter != HomeFilter.MOVIES
    val mediaByKey = (content.featured + content.topMovies + content.topShows +
        content.nowPlaying + content.comingSoon).associateBy(HomeMedia::key)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(HOME_CONTENT_TAG),
        contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        if (featured.isNotEmpty()) {
            item(key = "featured") {
                FeaturedCarousel(
                    items = featured.map(HomeMedia::toFeaturedItem),
                    onItemClick = { mediaByKey[it.id]?.let(onMediaClick) },
                    onViewDetailsClick = { mediaByKey[it.id]?.let(onMediaClick) },
                    onSaveClick = { mediaByKey[it.id]?.let(onWatchlistClick) },
                    isSaved = { item -> mediaByKey[item.id]?.let(isInWatchlist) == true },
                )
            }
        }

        item(key = "filters") {
            HorizontalFilterChips(
                chips = homeFilterChips,
                selectedChipId = selectedFilter.id,
                onChipSelected = { chip ->
                    HomeFilter.entries.firstOrNull { it.id == chip.id }?.let(onFilterSelected)
                },
            )
        }

        refreshError?.let { error ->
            item(key = "refresh_error") {
                RefreshErrorBanner(
                    error = error,
                    onRetry = onRetry,
                    onDismiss = onDismissRefreshError,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        if (showMovies && content.topMovies.isNotEmpty()) {
            item(key = "top_movies") {
                TopTenMediaRow(
                    title = "Top 10 Movies",
                    items = content.topMovies.mapIndexed { index, media -> media.toTopTenItem(index + 1) },
                    onItemClick = { mediaByKey[it.id]?.let(onMediaClick) },
                    onWatchlistClick = { mediaByKey[it.id]?.let(onWatchlistClick) },
                    isInWatchlist = { item -> mediaByKey[item.id]?.let(isInWatchlist) == true },
                )
            }
        }

        if (showTv && content.topShows.isNotEmpty()) {
            item(key = "top_shows") {
                TopTenMediaRow(
                    title = "Top 10 Shows",
                    items = content.topShows.mapIndexed { index, media -> media.toTopTenItem(index + 1) },
                    onItemClick = { mediaByKey[it.id]?.let(onMediaClick) },
                    onWatchlistClick = { mediaByKey[it.id]?.let(onWatchlistClick) },
                    isInWatchlist = { item -> mediaByKey[item.id]?.let(isInWatchlist) == true },
                )
            }
        }

        if (showMovies && content.nowPlaying.isNotEmpty()) {
            item(key = "now_playing") {
                MediaPosterRow(
                    title = "Now Playing",
                    items = content.nowPlaying.map(HomeMedia::toPosterItem),
                    onItemClick = { mediaByKey[it.id]?.let(onMediaClick) },
                    onWatchlistClick = { mediaByKey[it.id]?.let(onWatchlistClick) },
                    isInWatchlist = { item -> mediaByKey[item.id]?.let(isInWatchlist) == true },
                )
            }
        }

        if (showMovies && content.comingSoon.isNotEmpty()) {
            item(key = "coming_soon") {
                MediaPosterRow(
                    title = "Coming Soon",
                    items = content.comingSoon.map(HomeMedia::toPosterItem),
                    onItemClick = { mediaByKey[it.id]?.let(onMediaClick) },
                    onWatchlistClick = { mediaByKey[it.id]?.let(onWatchlistClick) },
                    isInWatchlist = { item -> mediaByKey[item.id]?.let(isInWatchlist) == true },
                )
            }
        }
    }
}

@Composable
private fun HomeErrorContent(
    error: HomeError,
    onRetry: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(HOME_ERROR_TAG),
        contentPadding = PaddingValues(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (error is HomeError.Offline) Icons.Outlined.WifiOff else Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (error is HomeError.Offline) "No internet connection" else "Unable to load Home",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error.message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onRetry) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun RefreshErrorBanner(
    error: HomeError,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = error.message,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Dismiss error")
                }
            }
            OutlinedButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun HomeShimmer() {
    val transition = rememberInfiniteTransition(label = "home-shimmer")
    val offset by transition.animateFloat(
        initialValue = -700f,
        targetValue = 1_700f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "home-shimmer-offset",
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surfaceContainerHighest
    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(offset - 500f, 0f),
        end = Offset(offset, 500f),
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(HOME_SHIMMER_TAG),
        contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item {
            ShimmerBlock(
                brush = brush,
                modifier = Modifier
                    .padding(horizontal = 18.dp)
                    .fillMaxWidth()
                    .aspectRatio(1.6f),
                shape = RoundedCornerShape(22.dp),
            )
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(3) { index ->
                    ShimmerBlock(
                        brush = brush,
                        modifier = Modifier
                            .width(if (index == 0) 96.dp else 112.dp)
                            .height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                    )
                }
            }
        }
        items(4) {
            ShimmerPosterRow(brush)
        }
    }
}

@Composable
private fun ShimmerPosterRow(brush: Brush) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ShimmerBlock(
            brush = brush,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(160.dp)
                .height(24.dp),
            shape = RoundedCornerShape(8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false,
        ) {
            items(4) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ShimmerBlock(
                        brush = brush,
                        modifier = Modifier
                            .width(132.dp)
                            .height(210.dp),
                        shape = RoundedCornerShape(14.dp),
                    )
                    ShimmerBlock(
                        brush = brush,
                        modifier = Modifier
                            .width(112.dp)
                            .height(16.dp),
                        shape = RoundedCornerShape(6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ShimmerBlock(
    brush: Brush,
    modifier: Modifier,
    shape: RoundedCornerShape,
) {
    Box(modifier = modifier.background(brush = brush, shape = shape))
}

private val homeFilterChips = listOf(
    HorizontalFilterChip(HomeFilter.FOR_YOU.id, "For you", Icons.Outlined.AutoAwesome),
    HorizontalFilterChip(HomeFilter.MOVIES.id, "Movies", Icons.Outlined.Movie),
    HorizontalFilterChip(HomeFilter.TV_SHOWS.id, "TV shows", Icons.Outlined.Tv),
)

private fun List<HomeMedia>.filterFor(filter: HomeFilter): List<HomeMedia> = when (filter) {
    HomeFilter.FOR_YOU -> this
    HomeFilter.MOVIES -> filter { it.mediaType == HomeMediaType.MOVIE }
    HomeFilter.TV_SHOWS -> filter { it.mediaType == HomeMediaType.TV }
}

private fun HomeMedia.toFeaturedItem() = FeaturedCarouselItem(
    id = key,
    title = title,
    imageUrl = backdropUrl ?: posterUrl,
    eyebrow = if (mediaType == HomeMediaType.MOVIE) "Featured movie" else "Featured show",
    year = year,
)

private fun HomeMedia.toTopTenItem(rank: Int) = TopTenMediaItem(
    id = key,
    rank = rank,
    title = title,
    imageUrl = posterUrl,
)

private fun HomeMedia.toPosterItem() = MediaPosterItem(
    id = key,
    title = title,
    imageUrl = posterUrl,
)

private val previewMedia = HomeMedia(
    tmdbId = 1,
    mediaType = HomeMediaType.MOVIE,
    title = "The Odyssey",
    posterUrl = null,
    backdropUrl = null,
    year = "2026",
)

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    PopcornRouteTheme(dynamicColor = false) {
        HomeScreen(
            state = HomeUiState(
                content = HomeContent(
                    featured = listOf(previewMedia),
                    topMovies = List(10) { previewMedia.copy(tmdbId = it + 1) },
                    topShows = emptyList(),
                    nowPlaying = listOf(previewMedia),
                    comingSoon = listOf(previewMedia),
                ),
                isCheckingConnectivity = false,
            ),
            onRefresh = {},
            onRetry = {},
            onFilterSelected = {},
            onDismissRefreshError = {},
        )
    }
}