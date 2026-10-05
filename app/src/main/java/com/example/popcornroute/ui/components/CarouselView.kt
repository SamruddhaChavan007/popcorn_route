package com.example.popcornroute.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.popcornroute.ui.theme.PopcornRouteTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val DEFAULT_AUTO_SCROLL_INTERVAL_MILLIS = 5_000L

/**
 * A swipeable carousel for featured movies or collections.
 *
 * The component owns only presentation and paging state. Image loading and click events are kept
 * generic so the same carousel can be populated from any feature or data source. Auto-scrolling
 * pauses while the user drags and restarts its timer after the interaction ends.
 */
@Composable
fun CarouselView(
    items: List<CarouselItem>,
    modifier: Modifier = Modifier,
    onItemClick: (CarouselItem) -> Unit = {},
    autoScrollEnabled: Boolean = true,
    autoScrollIntervalMillis: Long = DEFAULT_AUTO_SCROLL_INTERVAL_MILLIS,
) {
    require(autoScrollIntervalMillis > 0) {
        "autoScrollIntervalMillis must be greater than zero"
    }

    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(
        autoScrollEnabled,
        autoScrollIntervalMillis,
        isDragged,
        items.size,
    ) {
        if (!autoScrollEnabled || isDragged || items.size < 2) return@LaunchedEffect

        while (true) {
            delay(autoScrollIntervalMillis.milliseconds)
            val nextPage = (pagerState.settledPage + 1) % items.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 12.dp,
            key = { page -> items[page].id },
        ) { page ->
            CarouselCard(
                item = items[page],
                onClick = { onItemClick(items[page]) },
            )
        }

        if (items.size > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            CarouselIndicator(
                pageCount = items.size,
                selectedPage = pagerState.currentPage,
            )
        }
    }
}

@Composable
private fun CarouselCard(
    item: CarouselItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(shape)
            .clickable(
                onClick = onClick,
                onClickLabel = "Open ${item.title}",
            ),
        shape = shape,
        tonalElevation = 3.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Icon(
                imageVector = Icons.Outlined.Movie,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            )

            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.3f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.9f),
                            )
                        )
                    ),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(18.dp),
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                item.subtitle?.takeIf(String::isNotBlank)?.let { subtitle ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
internal fun CarouselIndicator(
    pageCount: Int,
    selectedPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { page ->
            val selected = page == selectedPage
            Box(
                modifier = Modifier
                    .size(if (selected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        }
                    ),
            )
        }
    }
}

data class CarouselItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
)

@Preview(
    name = "Carousel - light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Carousel - dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CarouselViewPreview() {
    PopcornRouteTheme(dynamicColor = false) {
        Surface {
            CarouselView(
                items = listOf(
                    CarouselItem(
                        id = "1",
                        title = "Now Playing",
                        subtitle = "Find your next movie night pick",
                    ),
                    CarouselItem(
                        id = "2",
                        title = "Popular This Week",
                        subtitle = "The movies everyone is talking about",
                    ),
                    CarouselItem(
                        id = "3",
                        title = "Coming Soon",
                        subtitle = "Add upcoming releases to your watchlist",
                    ),
                ),
                modifier = Modifier.padding(vertical = 24.dp),
            )
        }
    }
}
