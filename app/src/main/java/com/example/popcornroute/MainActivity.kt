package com.example.popcornroute

import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.popcornroute.auth.domain.model.SessionState
import com.example.popcornroute.auth.domain.repository.AuthRepository
import com.example.popcornroute.navigation.auth.AuthNavigation
import com.example.popcornroute.navigation.mainapp.MainAppNavigation
import com.example.popcornroute.ui.theme.PopcornRouteTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.hypot

private const val SPLASH_EXIT_ANIMATION_DURATION_MILLIS = 300L

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val iconView = splashScreenView.iconView
            val screenDiagonal = hypot(
                splashScreenView.view.width.toDouble(),
                splashScreenView.view.height.toDouble(),
            ).toFloat()
            val targetScale = (screenDiagonal / iconView.width.coerceAtLeast(1))
                .coerceAtLeast(1f)

            iconView.animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
                .setDuration(SPLASH_EXIT_ANIMATION_DURATION_MILLIS)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction { splashScreenView.remove() }
                .start()
        }

        enableEdgeToEdge()
        setContent {
            PopcornRouteTheme {
                val sessionState by authRepository.sessionState.collectAsStateWithLifecycle()

                when (sessionState) {
                    SessionState.Loading -> LoadingScreen()
                    SessionState.Authenticated -> MainAppNavigation()
                    SessionState.Unauthenticated -> AuthNavigation()
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}