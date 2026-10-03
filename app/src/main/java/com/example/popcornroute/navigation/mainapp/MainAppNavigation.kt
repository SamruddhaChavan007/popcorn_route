package com.example.popcornroute.navigation.mainapp

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.popcornroute.debuglauncher.TestCollectionScreen
import com.example.popcornroute.debuglauncher.TestProfileScreen
import com.example.popcornroute.home.presentation.HomeScreen
import com.example.popcornroute.navigation.bottomnavigation.bottomDestinations
import com.example.popcornroute.navigation.debuglauncher.debugLauncherEntries
import com.example.popcornroute.navigation.mainapp.keys.BottomNavigationKey
import com.example.popcornroute.navigation.transitions.TabRootMetadata
import com.example.popcornroute.navigation.transitions.isTabSwitch
import com.example.popcornroute.navigation.transitions.tabSwitchTransition
import com.example.popcornroute.ui.components.PopcornRouteBottomNavigation

/** Mirrors NavDisplay's built-in push transition, used for everything that is not a tab switch. */
private fun defaultForwardTransition(): ContentTransform =
    slideInHorizontally(initialOffsetX = { it }) togetherWith
        slideOutHorizontally(targetOffsetX = { -it })

@Composable
fun MainAppNavigation() {
    val backStack = rememberNavBackStack(BottomNavigationKey.Home)
    val selectedBottomKey = backStack.lastOrNull() as? BottomNavigationKey
    val density = LocalDensity.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (selectedBottomKey != null) {
                PopcornRouteBottomNavigation(
                    items = bottomDestinations,
                    selectedKey = selectedBottomKey,
                    onKeySelected = { item ->
                        if (item.key != selectedBottomKey) {
                            backStack.clear()
                            backStack.add(item.key)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            transitionSpec = {
                if (isTabSwitch(initialState, targetState)) {
                    tabSwitchTransition(density)
                } else {
                    defaultForwardTransition()
                }
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<BottomNavigationKey.Home>(metadata = TabRootMetadata) {
                    HomeScreen()
                }
                entry<BottomNavigationKey.Collections>(metadata = TabRootMetadata) {
                    TestCollectionScreen()
                }
                entry<BottomNavigationKey.Profile>(metadata = TabRootMetadata) {
                    TestProfileScreen()
                }
                debugLauncherEntries(navigate = backStack::add)
            },
        )
    }
}
