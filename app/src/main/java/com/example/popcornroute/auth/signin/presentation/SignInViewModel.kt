package com.example.popcornroute.auth.signin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.popcornroute.auth.domain.error.AuthError
import com.example.popcornroute.auth.domain.model.AuthResult
import com.example.popcornroute.auth.domain.usecase.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val useCase: SignInUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignInState())
    val uiState: StateFlow<SignInState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onSignInClicked() {
        val current = _uiState.value
        if (current.isLoading) {
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = useCase(email = current.email, password = current.password)
                if (result is AuthResult.Failure) {
                    _uiState.update { it.copy(error = result.error.toMessage()) }
                }
                // On success the session flips to Authenticated and MainActivity swaps the
                // auth flow out, so there is nothing to navigate to here.
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = GENERIC_ERROR) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun AuthError.toMessage(): String = when (this) {
        AuthError.EmailRequired -> "Enter your email."
        AuthError.PasswordRequired -> "Enter your password."
        AuthError.InvalidEmail -> "Enter a valid email address."
        AuthError.InvalidCredentials -> "Incorrect email or password."
        AuthError.AccountDisabled -> "This account has been disabled."
        AuthError.NetworkUnavailable -> "Check your internet connection and try again."
        AuthError.TooManyRequests -> "Too many attempts. Please try again later."
        // Sign-up-only errors cannot come back from signing in.
        AuthError.NameRequired,
        is AuthError.WeakPassword,
        AuthError.EmailAlreadyInUse,
        AuthError.ProfileUpdateFailed,
        AuthError.Unknown -> GENERIC_ERROR
    }

    private companion object {
        const val GENERIC_ERROR = "Something went wrong. Please try again."
    }
}
