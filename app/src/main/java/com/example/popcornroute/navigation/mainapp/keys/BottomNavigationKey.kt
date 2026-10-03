package com.example.popcornroute.navigation.mainapp.keys

import kotlinx.serialization.Serializable

@Serializable
sealed interface BottomNavigationKey : MainScreenKey {
    @Serializable
    data object Home : BottomNavigationKey

    @Serializable
    data object Collections : BottomNavigationKey

    @Serializable
    data object Profile : BottomNavigationKey

    @Serializable
    data object DebugLauncher : BottomNavigationKey
}