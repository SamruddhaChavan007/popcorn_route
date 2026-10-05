package com.example.popcornroute.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.popcornroute.ui.theme.PopcornRouteTheme

/**
 * Reusable horizontally scrolling group of Material 3 single-selection filter chips.
 *
 * The parent owns the available options, current selection, and the behavior triggered by a new
 * selection, so the component can be used by any screen.
 */
@Composable
fun HorizontalFilterChips(
    chips: List<HorizontalFilterChip>,
    selectedChipId: String,
    onChipSelected: (HorizontalFilterChip) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (chips.isEmpty()) return

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = chips,
            key = HorizontalFilterChip::id,
        ) { chip ->
            val selected = chip.id == selectedChipId
            val chipIcon = chip.icon
            val leadingIcon: (@Composable () -> Unit)? = when {
                selected -> {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                chipIcon != null -> {
                    {
                        Icon(
                            imageVector = chipIcon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                else -> null
            }

            FilterChip(
                selected = selected,
                onClick = { onChipSelected(chip) },
                label = {
                    Text(
                        text = chip.label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                leadingIcon = leadingIcon,
            )
        }
    }
}

data class HorizontalFilterChip(
    val id: String,
    val label: String,
    val icon: ImageVector? = null,
)

@Preview(
    name = "Horizontal filters - light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Horizontal filters - dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HorizontalFilterChipsPreview() {
    PopcornRouteTheme(dynamicColor = false) {
        Surface {
            HorizontalFilterChips(
                chips = listOf(
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
                ),
                selectedChipId = "for_you",
                onChipSelected = {},
            )
        }
    }
}
