package com.example.popcornroute.debuglauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.popcornroute.ui.components.CarouselItem
import com.example.popcornroute.ui.components.CarouselView

private const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w780"

private val carouselPreviewItems = listOf(
    CarouselItem(
        id = "969681",
        title = "Spider-Man: Brand New Day",
        subtitle = "Coming July 29, 2026",
        imageUrl = "$TMDB_IMAGE_BASE_URL/bjiS5ipwxb9JFy3XRRN4OAilSeX.jpg",
    ),
    CarouselItem(
        id = "1368337",
        title = "The Odyssey",
        subtitle = "Coming July 15, 2026",
        imageUrl = "$TMDB_IMAGE_BASE_URL/5rhTDKUhPYvpdQIijFIs5VoWsON.jpg",
    ),
    CarouselItem(
        id = "1084244",
        title = "Toy Story 5",
        subtitle = "Coming June 17, 2026",
        imageUrl = "$TMDB_IMAGE_BASE_URL/sfQtVlIHljToOwYjhe21KPGzZWK.jpg",
    ),
    CarouselItem(
        id = "969688",
        title = "Spider-Man: Brand New Day",
        subtitle = "Coming July 29, 2026",
        imageUrl = "$TMDB_IMAGE_BASE_URL/bjiS5ipwxb9JFy3XRRN4OAilSeX.jpg",
    ),
    CarouselItem(
        id = "1368335",
        title = "The Odyssey",
        subtitle = "Coming July 15, 2026",
        imageUrl = "$TMDB_IMAGE_BASE_URL/5rhTDKUhPYvpdQIijFIs5VoWsON.jpg",
    ),
    CarouselItem(
        id = "1084248",
        title = "Toy Story 5",
        subtitle = "Coming June 17, 2026",
        imageUrl = "$TMDB_IMAGE_BASE_URL/sfQtVlIHljToOwYjhe21KPGzZWK.jpg",
    ),
)

@Composable
fun CarouselViewScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(
            space = 20.dp,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Text(
            text = "Carousel View",
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        CarouselView(items = carouselPreviewItems)
    }
}