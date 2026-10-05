package com.example.popcornroute.debuglauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.popcornroute.ui.components.HorizontalFilterChip
import com.example.popcornroute.ui.components.HorizontalFilterChips

private val discoveryPreviewChips = listOf(
    HorizontalFilterChip(
        id = "for_you",
        label = "For you",
        icon = Icons.Outlined.AutoAwesome,
    ),
    HorizontalFilterChip(
        id = "movies",
        label = "Movies",
        icon = Icons.Outlined.Movie,
    ),
    HorizontalFilterChip(
        id = "tv_shows",
        label = "TV shows",
        icon = Icons.Outlined.Tv,
    ),
    HorizontalFilterChip(id = "netflix", label = "Netflix"),
    HorizontalFilterChip(id = "prime_video", label = "Prime Video"),
    HorizontalFilterChip(id = "disney_plus", label = "Disney+"),
)

@Composable
fun DiscoveryFilterChipsScreen(modifier: Modifier = Modifier) {
    var selectedChipId by remember { mutableStateOf(discoveryPreviewChips.first().id) }
    val selectedLabel = discoveryPreviewChips
        .first { it.id == selectedChipId }
        .label

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = "Discovery Filter Chips",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Selected feed: $selectedLabel",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )

        HorizontalFilterChips(
            chips = discoveryPreviewChips,
            selectedChipId = selectedChipId,
            onChipSelected = { selectedChipId = it.id },
        )
    }
}
