package com.example.popcornroute.auth.signin.presentation

data class SignInState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
