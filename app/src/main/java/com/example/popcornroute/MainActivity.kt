package com.example.popcornroute

import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.popcornroute.auth.domain.model.SessionState
import com.example.popcornroute.auth.domain.repository.AuthRepository
import com.example.popcornroute.navigation.auth.AuthNavigation
import com.example.popcornroute.ui.components.LogoutModule
import com.example.popcornroute.ui.theme.PopcornRouteTheme
import com.google.firebase.auth.FirebaseAuth
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
                    //SessionState.Authenticated -> HomeScreen()
                    SessionState.Authenticated -> LogoutTest()
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

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        val auth = FirebaseAuth.getInstance().currentUser
        Greeting(
            name = auth?.displayName.toString(),
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun LogoutTest(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        val auth = FirebaseAuth.getInstance().currentUser
        Column() {
            Greeting(
                name = auth?.displayName.toString(),
                modifier = Modifier.padding(innerPadding)
            )
            LogoutModule()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PopcornRouteTheme {
        Greeting("Android")
    }
}
