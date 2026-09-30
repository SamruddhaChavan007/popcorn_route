package com.example.popcornroute.auth.domain.usecase

import com.example.popcornroute.auth.domain.error.AuthError
import com.example.popcornroute.auth.domain.model.AuthResult
import com.example.popcornroute.auth.domain.repository.AuthRepository

class SignUpUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
    ): AuthResult {
        val normalizedName = name.trim()
        val normalizedEmail = email.trim()

        if (normalizedName.isEmpty()) {
            return AuthResult.Failure(AuthError.NameRequired)
        }
        if (normalizedEmail.isEmpty()) {
            return AuthResult.Failure(AuthError.EmailRequired)
        }
        if (password.isEmpty()) {
            return AuthResult.Failure(AuthError.PasswordRequired)
        }

        // Firebase remains the source of truth for email validity and the project's
        // configured password policy; duplicating those rules here can drift.
        return authRepository.signUp(
            name = normalizedName,
            email = normalizedEmail,
            password = password,
        )
    }
}
