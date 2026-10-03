package com.example.popcornroute.debuglauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun DebugLauncherScreen(
    onCarouselViewClick: () -> Unit,
    onTopTenMediaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val authApi = Firebase.auth

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hey this is Debug screen!",
            fontWeight = FontWeight.Bold
        )

        DebugLauncherButton(
            text = "Carousel View",
            onClick = onCarouselViewClick,
        )

        DebugLauncherButton(
            text = "Top 10 Movies & Shows",
            onClick = onTopTenMediaClick,
        )

        DebugLauncherButton(
            text = "Logout",
            onClick = { authApi.signOut() },
        )
    }
}

@Composable
private fun DebugLauncherButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
        )
    }
}
