package com.example.popcornroute.auth.signup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.popcornroute.auth.domain.error.AuthError
import com.example.popcornroute.auth.domain.model.AuthResult
import com.example.popcornroute.auth.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val useCase: SignUpUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpState())
    val uiState: StateFlow<SignUpState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { currentState ->
            currentState.copy(name = name)
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { currentState ->
            currentState.copy(email = email)
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { currentState ->
            currentState.copy(password = password)
        }
    }

    fun onSignUpClicked(email: String, name: String, password: String) {
        if (_uiState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                val result: AuthResult = useCase.invoke(
                    name = name,
                    email = email,
                    password = password
                )

                when (result) {
                    is AuthResult.Failure -> {
                        val message = when (val error = result.error) {
                            AuthError.NameRequired -> "Enter your name."
                            AuthError.EmailRequired -> "Enter your email."
                            AuthError.InvalidEmail -> "Enter a valid email address."
                            AuthError.PasswordRequired -> "Enter your password."
                            is AuthError.WeakPassword -> "Choose a stronger password."
                            AuthError.EmailAlreadyInUse -> "This email is already registered. Please sign in."
                            AuthError.NetworkUnavailable -> "Check your internet connection and try again."
                            AuthError.TooManyRequests -> "Too many attempts. Please try again later."
                            AuthError.ProfileUpdateFailed -> "We couldn’t save your name."
                            AuthError.InvalidCredentials, AuthError.AccountDisabled, AuthError.Unknown -> "Something went wrong. Please try again."
                        }

                        _uiState.update { it.copy(error = message) }
                    }

                    is AuthResult.Success -> Unit
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            } finally {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false
                    )
                }
            }
        }
    }
}