package com.example.popcornroute.navigation.debuglauncher

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Destinations owned by the debug launcher feature. */
@Serializable
sealed interface DebugLauncherKey : NavKey {
    @Serializable
    data object CarouselView : DebugLauncherKey

    @Serializable
    data object DiscoveryFilterChips : DebugLauncherKey

    @Serializable
    data object FlowingPosterBackground : DebugLauncherKey

    @Serializable
    data object PullToRefresh : DebugLauncherKey

    @Serializable
    data object TopTenMedia : DebugLauncherKey

    @Serializable
    data object UserLibrary : DebugLauncherKey
}
