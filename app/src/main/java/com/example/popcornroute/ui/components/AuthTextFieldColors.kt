package com.example.popcornroute.ui.components

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Error tint shared by the auth screens, which sit on the dark poster background. */
val AuthErrorColor = Color(0xFFFFB4AB)

/** Text field colours for the auth screens, which sit on the dark poster background. */
@Composable
fun authTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    disabledTextColor = Color.White.copy(alpha = 0.6f),
    errorTextColor = Color.White,
    focusedContainerColor = Color.Black.copy(alpha = 0.3f),
    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
    disabledContainerColor = Color.Black.copy(alpha = 0.2f),
    errorContainerColor = Color.Black.copy(alpha = 0.3f),
    cursorColor = Color.White,
    errorCursorColor = AuthErrorColor,
    focusedBorderColor = Color.White.copy(alpha = 0.8f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.45f),
    disabledBorderColor = Color.White.copy(alpha = 0.25f),
    errorBorderColor = AuthErrorColor,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
    disabledLabelColor = Color.White.copy(alpha = 0.45f),
    errorLabelColor = AuthErrorColor,
    focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
    disabledPlaceholderColor = Color.White.copy(alpha = 0.4f),
    errorPlaceholderColor = Color.White.copy(alpha = 0.6f),
    focusedLeadingIconColor = Color.White,
    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.75f),
    disabledLeadingIconColor = Color.White.copy(alpha = 0.45f),
    errorLeadingIconColor = AuthErrorColor,
    focusedTrailingIconColor = Color.White,
    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.75f),
    disabledTrailingIconColor = Color.White.copy(alpha = 0.45f),
    errorTrailingIconColor = AuthErrorColor,
    errorSupportingTextColor = AuthErrorColor,
)
