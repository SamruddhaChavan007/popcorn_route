package com.example.popcornroute.auth.domain.usecase

import com.example.popcornroute.auth.domain.error.AuthError
import com.example.popcornroute.auth.domain.model.AuthResult
import com.example.popcornroute.auth.domain.repository.AuthRepository

class SignInUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): AuthResult {
        val normalizedEmail = email.trim()

        if (normalizedEmail.isEmpty()) {
            return AuthResult.Failure(AuthError.EmailRequired)
        }
        if (password.isEmpty()) {
            return AuthResult.Failure(AuthError.PasswordRequired)
        }

        // Firebase decides whether the credentials are valid; the password is passed through
        // untouched because leading or trailing spaces can be part of it.
        return authRepository.signIn(
            email = normalizedEmail,
            password = password,
        )
    }
}
