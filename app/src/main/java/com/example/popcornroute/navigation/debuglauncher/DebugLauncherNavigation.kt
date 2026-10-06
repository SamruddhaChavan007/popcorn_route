package com.example.popcornroute.navigation.debuglauncher

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.popcornroute.debuglauncher.CarouselViewScreen
import com.example.popcornroute.debuglauncher.DebugLauncherScreen
import com.example.popcornroute.debuglauncher.DiscoveryFilterChipsScreen
import com.example.popcornroute.debuglauncher.FlowingPosterBackgroundScreen
import com.example.popcornroute.debuglauncher.PullToRefreshScreen
import com.example.popcornroute.debuglauncher.TopTenMediaScreen
import com.example.popcornroute.debuglauncher.UserLibraryDebugScreen
import com.example.popcornroute.navigation.mainapp.keys.BottomNavigationKey
import com.example.popcornroute.navigation.transitions.TabRootMetadata

/** Registers every destination owned by the debug launcher feature. */
fun EntryProviderScope<NavKey>.debugLauncherEntries(
    navigate: (NavKey) -> Unit,
) {
    entry<BottomNavigationKey.DebugLauncher>(metadata = TabRootMetadata) {
        DebugLauncherScreen(
            onCarouselViewClick = {
                navigate(DebugLauncherKey.CarouselView)
            },
            onDiscoveryFilterChipsClick = {
                navigate(DebugLauncherKey.DiscoveryFilterChips)
            },
            onFlowingPosterBackgroundClick = {
                navigate(DebugLauncherKey.FlowingPosterBackground)
            },
            onPullToRefreshClick = {
                navigate(DebugLauncherKey.PullToRefresh)
            },
            onTopTenMediaClick = {
                navigate(DebugLauncherKey.TopTenMedia)
            },
            onUserLibraryClick = {
                navigate(DebugLauncherKey.UserLibrary)
            },
        )
    }

    entry<DebugLauncherKey.CarouselView> {
        CarouselViewScreen()
    }

    entry<DebugLauncherKey.DiscoveryFilterChips> {
        DiscoveryFilterChipsScreen()
    }

    entry<DebugLauncherKey.FlowingPosterBackground> {
        FlowingPosterBackgroundScreen()
    }

    entry<DebugLauncherKey.PullToRefresh> {
        PullToRefreshScreen()
    }

    entry<DebugLauncherKey.TopTenMedia> {
        TopTenMediaScreen()
    }

    entry<DebugLauncherKey.UserLibrary> {
        UserLibraryDebugScreen()
    }
}
