package com.example.popcornroute.auth.signup.presentation

data class SignUpState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String ? = null
)