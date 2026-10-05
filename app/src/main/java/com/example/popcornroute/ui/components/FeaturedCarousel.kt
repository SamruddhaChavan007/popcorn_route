package com.example.popcornroute.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

private const val DEFAULT_FEATURED_AUTO_SCROLL_INTERVAL_MILLIS = 5_000L

/**
 * Cinematic Home-screen carousel for featured movies and shows.
 *
 * Paging pauses while the user drags or holds either action. The component owns presentation and
 * pager state; navigation and collection mutations remain callbacks owned by the feature layer.
 */
@Composable
fun FeaturedCarousel(
    items: List<FeaturedCarouselItem>,
    modifier: Modifier = Modifier,
    onItemClick: (FeaturedCarouselItem) -> Unit = {},
    onViewDetailsClick: (FeaturedCarouselItem) -> Unit = onItemClick,
    onSaveClick: (FeaturedCarouselItem) -> Unit = {},
    isSaved: (FeaturedCarouselItem) -> Boolean = { false },
    autoScrollEnabled: Boolean = true,
    autoScrollIntervalMillis: Long = DEFAULT_FEATURED_AUTO_SCROLL_INTERVAL_MILLIS,
) {
    require(autoScrollIntervalMillis > 0) {
        "autoScrollIntervalMillis must be greater than zero"
    }

    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })
    val actionInteractionSource = remember { MutableInteractionSource() }
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    val isActionPressed by actionInteractionSource.collectIsPressedAsState()

    LaunchedEffect(
        autoScrollEnabled,
        autoScrollIntervalMillis,
        isDragged,
        isActionPressed,
        items.size,
    ) {
        if (!autoScrollEnabled || isDragged || isActionPressed || items.size < 2) {
            return@LaunchedEffect
        }

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
            contentPadding = PaddingValues(horizontal = 18.dp),
            pageSpacing = 12.dp,
            key = { page -> items[page].id },
        ) { page ->
            val item = items[page]
            FeaturedCarouselCard(
                item = item,
                saved = isSaved(item),
                actionInteractionSource = actionInteractionSource,
                onClick = { onItemClick(item) },
                onViewDetailsClick = { onViewDetailsClick(item) },
                onSaveClick = { onSaveClick(item) },
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
private fun FeaturedCarouselCard(
    item: FeaturedCarouselItem,
    saved: Boolean,
    actionInteractionSource: MutableInteractionSource,
    onClick: () -> Unit,
    onViewDetailsClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
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

            item.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0f to Color.Black.copy(alpha = 0.88f),
                            0.58f to Color.Black.copy(alpha = 0.42f),
                            1f to Color.Transparent,
                        )
                    ),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.25f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.72f),
                        )
                    ),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.82f)
                    .padding(18.dp),
            ) {
                item.eyebrow?.takeIf(String::isNotBlank)?.let { eyebrow ->
                    Text(
                        text = eyebrow.uppercase(),
                        color = Color.White.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = item.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                item.metadata.takeIf(String::isNotEmpty)?.let { metadata ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = metadata,
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onViewDetailsClick,
                        interactionSource = actionInteractionSource,
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 9.dp,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = "View details")
                    }

                    IconButton(
                        onClick = onSaveClick,
                        interactionSource = actionInteractionSource,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.16f),
                            contentColor = Color.White,
                        ),
                    ) {
                        Icon(
                            imageVector = if (saved) {
                                Icons.Filled.Bookmark
                            } else {
                                Icons.Outlined.BookmarkBorder
                            },
                            contentDescription = if (saved) {
                                "Remove ${item.title} from collections"
                            } else {
                                "Save ${item.title} to collections"
                            },
                        )
                    }
                }
            }
        }
    }
}

data class FeaturedCarouselItem(
    val id: String,
    val title: String,
    val imageUrl: String? = null,
    val eyebrow: String? = "Featured this week",
    val year: String? = null,
    val genre: String? = null,
    val certification: String? = null,
) {
    val metadata: String
        get() = listOfNotNull(
            year?.takeIf(String::isNotBlank),
            genre?.takeIf(String::isNotBlank),
            certification?.takeIf(String::isNotBlank),
        ).joinToString(separator = " • ")
}

@Preview(
    name = "Featured carousel - light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Featured carousel - dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FeaturedCarouselPreview() {
    PopcornRouteTheme(dynamicColor = false) {
        Surface {
            FeaturedCarousel(
                items = listOf(
                    FeaturedCarouselItem(
                        id = "1",
                        title = "The Odyssey",
                        year = "2026",
                        genre = "Adventure",
                        certification = "PG-13",
                    ),
                    FeaturedCarouselItem(
                        id = "2",
                        title = "Spider-Man: Brand New Day",
                        year = "2026",
                        genre = "Action",
                        certification = "PG-13",
                    ),
                ),
                modifier = Modifier.padding(vertical = 24.dp),
                autoScrollEnabled = false,
            )
        }
    }
}
