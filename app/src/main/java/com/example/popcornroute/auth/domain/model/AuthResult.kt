package com.example.popcornroute.auth.domain.model

import com.example.popcornroute.auth.domain.error.AuthError

sealed interface AuthResult {
    data class Success(
        val user: AuthUser,
        val profileUpdateFailed: Boolean = false,
    ) : AuthResult

    data class Failure(val error: AuthError) : AuthResult
}
