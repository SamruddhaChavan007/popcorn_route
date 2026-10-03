package com.example.popcornroute.debuglauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.popcornroute.ui.components.MediaPosterItem
import com.example.popcornroute.ui.components.MediaPosterRow
import com.example.popcornroute.ui.components.TopTenMediaItem
import com.example.popcornroute.ui.components.TopTenMediaRow

private const val TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"

private val topTenPreviewItems = listOf(
    TopTenMediaItem(
        id = "969681",
        rank = 1,
        title = "Spider-Man: Brand New Day",
        imageUrl = "$TMDB_POSTER_BASE_URL/bjiS5ipwxb9JFy3XRRN4OAilSeX.jpg",
    ),
    TopTenMediaItem(
        id = "1368337",
        rank = 2,
        title = "The Odyssey",
        imageUrl = "$TMDB_POSTER_BASE_URL/5rhTDKUhPYvpdQIijFIs5VoWsON.jpg",
    ),
    TopTenMediaItem(
        id = "1288445",
        rank = 3,
        title = "Mutiny",
        imageUrl = "$TMDB_POSTER_BASE_URL/aAnTt6KpmbbHbd6xH3FQFlppZjc.jpg",
    ),
    TopTenMediaItem(
        id = "1323244",
        rank = 4,
        title = "Rage of Stars",
        imageUrl = "$TMDB_POSTER_BASE_URL/oLld47ZT1I3iecM3OWhIphohQUJ.jpg",
    ),
    TopTenMediaItem(
        id = "1084244",
        rank = 5,
        title = "Toy Story 5",
        imageUrl = "$TMDB_POSTER_BASE_URL/sfQtVlIHljToOwYjhe21KPGzZWK.jpg",
    ),
    TopTenMediaItem(
        id = "1339713",
        rank = 6,
        title = "Obsession",
        imageUrl = "$TMDB_POSTER_BASE_URL/bRwnj8WEKBCvmfeUNOukJPwB43K.jpg",
    ),
    TopTenMediaItem(
        id = "1621552",
        rank = 7,
        title = "Facing El Chapo",
        imageUrl = "$TMDB_POSTER_BASE_URL/alpf5v4UqSFawPmG9RX03Or4BDk.jpg",
    ),
    TopTenMediaItem(
        id = "1108427",
        rank = 8,
        title = "Moana",
        imageUrl = "$TMDB_POSTER_BASE_URL/zKVgiv5qHCvCLT4A2ymJi5QeXDH.jpg",
    ),
    TopTenMediaItem(
        id = "1315772",
        rank = 9,
        title = "Minions & Monsters",
        imageUrl = "$TMDB_POSTER_BASE_URL/4LwvU9SZc8QQzW1X1FAPhNbXnEU.jpg",
    ),
    TopTenMediaItem(
        id = "1375646",
        rank = 10,
        title = "Colony",
        imageUrl = "$TMDB_POSTER_BASE_URL/tN799oUR0f1gUKDYdMNrDaY7I51.jpg",
    ),
)

private val nowPlayingPreviewItems = topTenPreviewItems
    .take(5)
    .map(TopTenMediaItem::toMediaPosterItem)

private val comingSoonPreviewItems = topTenPreviewItems
    .drop(5)
    .map(TopTenMediaItem::toMediaPosterItem)

private fun TopTenMediaItem.toMediaPosterItem() = MediaPosterItem(
    id = id,
    title = title,
    imageUrl = imageUrl,
)

@Composable
fun TopTenMediaScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item {
            TopTenMediaRow(
                title = "Top 10 Trending Movies",
                items = topTenPreviewItems,
            )
        }

        item {
            MediaPosterRow(
                title = "Now Playing",
                items = nowPlayingPreviewItems,
            )
        }

        item {
            MediaPosterRow(
                title = "Coming Soon",
                items = comingSoonPreviewItems,
            )
        }
    }
}
