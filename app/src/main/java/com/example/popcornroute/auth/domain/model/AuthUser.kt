package com.example.popcornroute.auth.domain.model

/** Authenticated user data that is independent of the Firebase SDK. */
data class AuthUser(
    val id: String,
    val name: String?,
    val email: String?,
)
