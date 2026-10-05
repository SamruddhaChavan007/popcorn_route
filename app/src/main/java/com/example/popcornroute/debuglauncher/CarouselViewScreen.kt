package com.example.popcornroute.debuglauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.popcornroute.ui.components.CarouselItem
import com.example.popcornroute.ui.components.CarouselView
import com.example.popcornroute.ui.components.FeaturedCarousel
import com.example.popcornroute.ui.components.FeaturedCarouselItem

private const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w780"

private val carouselPreviewItems = listOf(
    FeaturedCarouselItem(
        id = "969681",
        title = "Spider-Man: Brand New Day",
        imageUrl = "$TMDB_IMAGE_BASE_URL/bjiS5ipwxb9JFy3XRRN4OAilSeX.jpg",
        year = "2026",
        genre = "Action",
        certification = "PG-13",
    ),
    FeaturedCarouselItem(
        id = "1368337",
        title = "The Odyssey",
        imageUrl = "$TMDB_IMAGE_BASE_URL/5rhTDKUhPYvpdQIijFIs5VoWsON.jpg",
        year = "2026",
        genre = "Adventure",
        certification = "PG-13",
    ),
    FeaturedCarouselItem(
        id = "1084244",
        title = "Toy Story 5",
        imageUrl = "$TMDB_IMAGE_BASE_URL/sfQtVlIHljToOwYjhe21KPGzZWK.jpg",
        year = "2026",
        genre = "Animation",
        certification = "PG",
    ),
)

private val originalCarouselPreviewItems = listOf(
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
)

@Composable
fun CarouselViewScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Text(
                text = "Featured Carousel",
                modifier = Modifier.padding(horizontal = 24.dp),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            FeaturedCarousel(items = carouselPreviewItems)
        }

        item {
            Text(
                text = "Original Carousel",
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            CarouselView(items = originalCarouselPreviewItems)
        }
    }
}
