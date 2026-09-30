package com.example.popcornroute.auth.domain.repository

import com.example.popcornroute.auth.domain.model.AuthResult
import com.example.popcornroute.auth.domain.model.SessionState
import kotlinx.coroutines.flow.StateFlow

/** Shared authentication contract that can be extended by other auth features. */
interface AuthRepository {
    val sessionState: StateFlow<SessionState>

    suspend fun signUp(
        name: String,
        email: String,
        password: String,
    ): AuthResult

    suspend fun signIn(
        email: String,
        password: String,
    ): AuthResult
}