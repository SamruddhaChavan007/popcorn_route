package com.example.popcornroute.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.popcornroute.ui.theme.PopcornRouteTheme

internal val DefaultPosterWidth = 132.dp
internal val DefaultPosterHeight = 210.dp
private val PosterShape = RoundedCornerShape(14.dp)

/** A reusable horizontal row for ordinary, unranked movie and show posters. */
@Composable
fun MediaPosterRow(
    title: String,
    items: List<MediaPosterItem>,
    modifier: Modifier = Modifier,
    onItemClick: (MediaPosterItem) -> Unit = {},
    imageModifier: @Composable (MediaPosterItem) -> Modifier = { Modifier },
) {
    if (items.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        MediaRowTitle(title = title)

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = items,
                key = MediaPosterItem::id,
            ) { item ->
                MediaPosterCard(
                    title = item.title,
                    imageUrl = item.imageUrl,
                    onClick = { onItemClick(item) },
                    clickLabel = "Open ${item.title}",
                    accessibilityDescription = item.title,
                    imageModifier = imageModifier(item),
                )
            }
        }
    }
}

@Composable
internal fun MediaRowTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier.padding(horizontal = 16.dp),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
}

/**
 * Shared poster-card foundation used by ordinary media rows and decorated rows such as Top 10.
 */
@Composable
internal fun MediaPosterCard(
    title: String,
    imageUrl: String?,
    onClick: () -> Unit,
    clickLabel: String,
    accessibilityDescription: String,
    imageModifier: Modifier,
    modifier: Modifier = Modifier,
    cardWidth: Dp = DefaultPosterWidth,
    posterWidth: Dp = DefaultPosterWidth,
    titleStartPadding: Dp = 0.dp,
    artworkOverlay: @Composable BoxScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .width(cardWidth)
            .clickable(
                onClick = onClick,
                onClickLabel = clickLabel,
            )
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityDescription
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(DefaultPosterHeight),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(posterWidth)
                    .fillMaxHeight()
                    .then(imageModifier)
                    .clip(PosterShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Movie,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                )

                imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            artworkOverlay()
        }

        Text(
            text = title,
            modifier = Modifier.padding(
                top = 10.dp,
                start = titleStartPadding,
                end = 4.dp,
                bottom = 4.dp,
            ),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

data class MediaPosterItem(
    val id: String,
    val title: String,
    val imageUrl: String? = null,
)

private val previewPosterItems = listOf(
    MediaPosterItem(id = "1", title = "Spider-Man: Brand New Day"),
    MediaPosterItem(id = "2", title = "The Odyssey"),
    MediaPosterItem(id = "3", title = "Toy Story 5"),
)

@Preview(
    name = "Media posters - light",
    showBackground = true,
    backgroundColor = 0xFFFFFBFE,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Media posters - dark",
    showBackground = true,
    backgroundColor = 0xFF1C1B1F,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun MediaPosterRowPreview() {
    PopcornRouteTheme(dynamicColor = false) {
        Surface {
            MediaPosterRow(
                title = "Now Playing",
                items = previewPosterItems,
                modifier = Modifier.padding(vertical = 24.dp),
            )
        }
    }
}
