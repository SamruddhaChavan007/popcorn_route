package com.example.popcornroute.navigation.auth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.popcornroute.auth.signin.presentation.SignInScreen
import com.example.popcornroute.auth.signup.presentation.SignUpScreen
import com.example.popcornroute.navigation.auth.keys.AuthScreenKey
import com.example.popcornroute.navigation.transitions.backTransition
import com.example.popcornroute.navigation.transitions.forwardTransition
import com.example.popcornroute.ui.components.FlowingPosterBackground

@Composable
fun AuthNavigation() {
    val authBackStack = rememberNavBackStack(AuthScreenKey.SignUp)

    // One backdrop for the whole auth flow: only the forms animate, so the posters keep flowing
    // across navigation. The screens draw no background of their own.
    FlowingPosterBackground(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = authBackStack,
            onBack = { authBackStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = { forwardTransition() },
            popTransitionSpec = { backTransition() },
            predictivePopTransitionSpec = { backTransition() },
            entryProvider = entryProvider {
                entry<AuthScreenKey.SignUp> {
                    SignUpScreen(onSignInClick = { authBackStack.add(AuthScreenKey.SignIn) })
                }
                entry<AuthScreenKey.SignIn> {
                    SignInScreen(onSignUpClick = { authBackStack.removeLastOrNull() })
                }
            }
        )
    }
}