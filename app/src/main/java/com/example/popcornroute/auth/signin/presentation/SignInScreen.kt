package com.example.popcornroute.auth.signin.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.popcornroute.R
import com.example.popcornroute.ui.components.AuthErrorColor
import com.example.popcornroute.ui.components.FlowingPosterBackground
import com.example.popcornroute.ui.components.authTextFieldColors
import com.example.popcornroute.ui.theme.PopcornRouteTheme

const val SIGN_IN_SCREEN_TAG = "sign_in_screen"

@Composable
fun SignInScreen(
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SignInScreenContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignInClick = viewModel::onSignInClicked,
        onSignUpClick = onSignUpClick,
        modifier = modifier,
    )
}

@Composable
fun SignInScreenContent(
    state: SignInState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    emailError: String? = null,
    passwordError: String? = null,
    isSignInEnabled: Boolean = true,
) {
    val focusManager = LocalFocusManager.current
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val fieldsEnabled = !state.isLoading
    val buttonColor = Color.White
    val buttonContentColor = Color.Black
    val textFieldColors = authTextFieldColors()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(SIGN_IN_SCREEN_TAG)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.sign_up_eyebrow),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.4.sp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.sign_in_title),
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.sign_in_subtitle),
            color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(36.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = fieldsEnabled,
            label = { Text(stringResource(R.string.sign_up_email_label)) },
            placeholder = { Text(stringResource(R.string.sign_up_email_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            supportingText = emailError?.let { error -> { Text(error) } },
            isError = emailError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            shape = RoundedCornerShape(14.dp),
            colors = textFieldColors,
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = fieldsEnabled,
            label = { Text(stringResource(R.string.sign_up_password_label)) },
            placeholder = { Text(stringResource(R.string.sign_in_password_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible },
                    enabled = fieldsEnabled,
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = stringResource(
                            if (isPasswordVisible) {
                                R.string.sign_up_hide_password
                            } else {
                                R.string.sign_up_show_password
                            },
                        ),
                    )
                }
            },
            supportingText = passwordError?.let { error -> { Text(error) } },
            isError = passwordError != null,
            singleLine = true,
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (isSignInEnabled && !state.isLoading) onSignInClick()
                },
            ),
            shape = RoundedCornerShape(14.dp),
            colors = textFieldColors,
        )

        state.error?.takeIf { it.isNotBlank() }?.let { message ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { liveRegion = LiveRegionMode.Polite },
                color = AuthErrorColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                focusManager.clearFocus()
                onSignInClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = isSignInEnabled && !state.isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = buttonContentColor,
                disabledContainerColor = buttonColor.copy(alpha = 0.38f),
                disabledContentColor = buttonContentColor.copy(alpha = 0.7f),
            ),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(22.dp),
                    color = buttonContentColor,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = stringResource(R.string.sign_in_action),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.sign_in_no_account),
                color = Color.White.copy(alpha = 0.75f),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.sign_in_sign_up_action),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !state.isLoading,
                    onClick = onSignUpClick,
                ),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignInScreenPreview() {
    PopcornRouteTheme(dynamicColor = false) {
        FlowingPosterBackground {
            SignInScreenContent(
                state = SignInState(
                    email = "alex@example.com",
                    password = "password123",
                ),
                onEmailChange = {},
                onPasswordChange = {},
                onSignInClick = {},
                onSignUpClick = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenErrorPreview() {
    PopcornRouteTheme(dynamicColor = false) {
        FlowingPosterBackground {
            SignInScreenContent(
                state = SignInState(
                    email = "alex@example.com",
                    password = "password123",
                    error = "Incorrect email or password.",
                ),
                onEmailChange = {},
                onPasswordChange = {},
                onSignInClick = {},
                onSignUpClick = {},
            )
        }
    }
}
