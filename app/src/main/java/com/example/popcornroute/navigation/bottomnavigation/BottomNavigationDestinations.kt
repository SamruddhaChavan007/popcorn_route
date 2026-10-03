package com.example.popcornroute.navigation.bottomnavigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import com.example.popcornroute.BuildConfig
import com.example.popcornroute.navigation.mainapp.keys.BottomNavigationKey

val bottomDestinations = buildList {
    add(
        BottomNavigationItem(
            key = BottomNavigationKey.Home,
            label = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
        )
    )

    add(
        BottomNavigationItem(
            key = BottomNavigationKey.Collections,
            label = "Collections",
            selectedIcon = Icons.Filled.CollectionsBookmark,
            unselectedIcon = Icons.Outlined.CollectionsBookmark,
        )
    )

    add(
        BottomNavigationItem(
            key = BottomNavigationKey.Profile,
            label = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
        )
    )

    if (!BuildConfig.IS_PRODUCTION) {
        add(
            BottomNavigationItem(
                key = BottomNavigationKey.DebugLauncher,
                label = "Debug",
                selectedIcon = Icons.Filled.BugReport,
                unselectedIcon = Icons.Outlined.BugReport,
            )
        )
    }
}