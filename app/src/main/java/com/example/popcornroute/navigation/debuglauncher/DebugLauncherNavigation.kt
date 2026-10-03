package com.example.popcornroute.navigation.debuglauncher

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.popcornroute.debuglauncher.CarouselViewScreen
import com.example.popcornroute.debuglauncher.DebugLauncherScreen
import com.example.popcornroute.debuglauncher.TopTenMediaScreen
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
            onTopTenMediaClick = {
                navigate(DebugLauncherKey.TopTenMedia)
            },
        )
    }

    entry<DebugLauncherKey.CarouselView> {
        CarouselViewScreen()
    }

    entry<DebugLauncherKey.TopTenMedia> {
        TopTenMediaScreen()
    }
}
