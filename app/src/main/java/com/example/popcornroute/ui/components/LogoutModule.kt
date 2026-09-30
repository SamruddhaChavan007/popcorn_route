package com.example.popcornroute.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun LogoutModule() {
    val authApi = Firebase.auth
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
    ) {
        Button(
            onClick = { authApi.signOut() }
        ) {
            Text(
                text = "Logout testing",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun PreviewLogoutModule() {
    LogoutModule()
}