package com.example.popcornroute.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.popcornroute.ui.theme.PopcornRouteTheme

private val RankedCardWidth = 176.dp
private val RankedPosterStart = RankedCardWidth - DefaultPosterWidth

/**
 * A horizontally scrolling collection of ranked movies or shows.
 *
 * [imageModifier] allows a parent screen to add presentation behavior such as a shared-element
 * transition without coupling this component to navigation.
 */
@Composable
fun TopTenMediaRow(
    title: String,
    items: List<TopTenMediaItem>,
    modifier: Modifier = Modifier,
    onItemClick: (TopTenMediaItem) -> Unit = {},
    onWatchlistClick: (TopTenMediaItem) -> Unit = {},
    isInWatchlist: (TopTenMediaItem) -> Boolean = { false },
    imageModifier: @Composable (TopTenMediaItem) -> Modifier = { Modifier },
) {
    if (items.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        MediaRowTitle(title = title)

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(
                items = items,
                key = TopTenMediaItem::id,
            ) { item ->
                TopTenMediaCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onWatchlistClick = { onWatchlistClick(item) },
                    isInWatchlist = isInWatchlist(item),
                    imageModifier = imageModifier(item),
                )
            }
        }
    }
}

@Composable
private fun TopTenMediaCard(
    item: TopTenMediaItem,
    onClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    isInWatchlist: Boolean,
    imageModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    MediaPosterCard(
        title = item.title,
        imageUrl = item.imageUrl,
        onClick = onClick,
        clickLabel = "Open ${item.title}",
        accessibilityDescription = "Number ${item.rank}, ${item.title}",
        imageModifier = imageModifier,
        isInWatchlist = isInWatchlist,
        onWatchlistClick = onWatchlistClick,
        modifier = modifier,
        cardWidth = RankedCardWidth,
        posterWidth = DefaultPosterWidth,
        titleStartPadding = RankedPosterStart,
        artworkOverlay = {
            RankedNumber(
                rank = item.rank,
                modifier = Modifier.align(Alignment.BottomStart),
            )
        },
    )
}

@Composable
private fun RankedNumber(
    rank: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Text(
            text = rank.toString(),
            modifier = Modifier.align(Alignment.BottomEnd),
            color = MaterialTheme.colorScheme.surface,
            fontSize = 112.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 90.sp,
            style = MaterialTheme.typography.displayLarge.copy(
                drawStyle = Stroke(width = 8f),
            ),
            maxLines = 1,
        )
        Text(
            text = rank.toString(),
            modifier = Modifier.align(Alignment.BottomEnd),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 112.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 90.sp,
            maxLines = 1,
        )
    }
}

data class TopTenMediaItem(
    val id: String,
    val rank: Int,
    val title: String,
    val imageUrl: String? = null,
) {
    init {
        require(rank in 1..10) { "rank must be between 1 and 10" }
    }
}

private val previewTopTenItems = listOf(
    TopTenMediaItem(id = "4", rank = 4, title = "Toy Story 5"),
    TopTenMediaItem(id = "10", rank = 10, title = "The Last House"),
)

@Preview(
    name = "Top 10 - light",
    showBackground = true,
    backgroundColor = 0xFFFFFBFE,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Top 10 - dark",
    showBackground = true,
    backgroundColor = 0xFF1C1B1F,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TopTenMediaRowPreview() {
    PopcornRouteTheme(dynamicColor = false) {
        Surface {
            TopTenMediaRow(
                title = "Top 10 Movies & Shows",
                items = previewTopTenItems,
                modifier = Modifier.padding(vertical = 24.dp),
            )
        }
    }
}
