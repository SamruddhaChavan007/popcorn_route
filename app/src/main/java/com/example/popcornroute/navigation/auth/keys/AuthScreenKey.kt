package com.example.popcornroute.navigation.auth.keys

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthScreenKey : NavKey {
    @Serializable
    data object SignUp: AuthScreenKey

    @Serializable
    data object SignIn: AuthScreenKey
}