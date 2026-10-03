package com.example.popcornroute.navigation.mainapp.keys

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
@Serializable
sealed interface MainScreenKey : NavKey {
    @Serializable
    data class MovieDetails(
        val movieId: String
    ) : MainScreenKey
}
