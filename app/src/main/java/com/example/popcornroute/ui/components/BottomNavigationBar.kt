package com.example.popcornroute.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.popcornroute.navigation.bottomnavigation.BottomNavigationItem
import com.example.popcornroute.navigation.mainapp.keys.BottomNavigationKey
import com.example.popcornroute.ui.theme.PopcornRouteTheme

/**
 * UI model for one destination in [PopcornRouteBottomNavigation].
 *
 * The navigation component intentionally does not know about routes or build flavors. The caller
 * owns the list, so destinations such as Debug Launcher can be included only when appropriate.
 */

@Composable
fun PopcornRouteBottomNavigation(
    items: List<BottomNavigationItem>,
    selectedKey: BottomNavigationKey,
    onKeySelected: (BottomNavigationItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                )
            ),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        items.forEach { item ->
            key(item.key) {
                val selected = item.key == selectedKey
                val iconScale = rememberSelectedIconScale(selected)

                NavigationBarItem(
                    selected = selected,
                    onClick = { onKeySelected(item) },
                    icon = {
                        Icon(
                            modifier = Modifier.graphicsLayer {
                                scaleX = iconScale.value
                                scaleY = iconScale.value
                            },
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                        )
                    },
                    label = { Text(text = item.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

private const val SELECTED_ICON_INITIAL_SCALE = 0.92f

/**
 * Scale for a tab icon that settles from 92% to 100% when the tab becomes selected. Tabs that are
 * selected on first composition (e.g. at app start) are not animated.
 */
@Composable
private fun rememberSelectedIconScale(selected: Boolean): Animatable<Float, AnimationVector1D> {
    val scale = remember { Animatable(1f) }
    var isFirstComposition by remember { mutableStateOf(true) }

    LaunchedEffect(selected) {
        if (selected && !isFirstComposition) {
            scale.snapTo(SELECTED_ICON_INITIAL_SCALE)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
        isFirstComposition = false
    }

    return scale
}

@Preview(
    name = "Light theme",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun PopcornRouteBottomNavigationLightPreview() {
    PopcornRouteBottomNavigationPreview(darkTheme = false)
}

@Preview(
    name = "Dark theme",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PopcornRouteBottomNavigationDarkPreview() {
    PopcornRouteBottomNavigationPreview(darkTheme = true)
}

@Composable
private fun PopcornRouteBottomNavigationPreview(darkTheme: Boolean) {
    PopcornRouteTheme(
        darkTheme = darkTheme,
        dynamicColor = false,
    ) {
        PopcornRouteBottomNavigation(
            items = listOf(
                BottomNavigationItem(
                    key = BottomNavigationKey.Home,
                    label = "Home",
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                ),
                BottomNavigationItem(
                    key = BottomNavigationKey.Collections,
                    label = "Collections",
                    selectedIcon = Icons.Filled.CollectionsBookmark,
                    unselectedIcon = Icons.Outlined.CollectionsBookmark,
                ),
                BottomNavigationItem(
                    key = BottomNavigationKey.Profile,
                    label = "Profile",
                    selectedIcon = Icons.Filled.Person,
                    unselectedIcon = Icons.Outlined.Person,
                ),
                BottomNavigationItem(
                    key = BottomNavigationKey.DebugLauncher,
                    label = "Debug",
                    selectedIcon = Icons.Filled.BugReport,
                    unselectedIcon = Icons.Outlined.BugReport,
                ),
            ),
            selectedKey = BottomNavigationKey.DebugLauncher,
            onKeySelected = {},
        )
    }
}