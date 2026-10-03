package com.example.popcornroute.navigation.bottomnavigation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.popcornroute.navigation.mainapp.keys.BottomNavigationKey

@Immutable
data class BottomNavigationItem(
    val key: BottomNavigationKey,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector = selectedIcon,
)