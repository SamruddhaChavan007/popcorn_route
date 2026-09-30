package com.example.popcornroute.auth.data.repository

import com.example.popcornroute.auth.domain.error.AuthError
import com.example.popcornroute.auth.domain.model.AuthResult
import com.example.popcornroute.auth.domain.model.AuthUser
import com.example.popcornroute.auth.domain.model.SessionState
import com.example.popcornroute.auth.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.atomic.AtomicBoolean

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
) : AuthRepository {

    // Suppresses AuthStateListener emissions while signUp() is finishing the
    // profile update, so collectors don't see a transient Authenticated user
    // that's missing its display name.
    private val isSigningUp = AtomicBoolean(false)

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            if (!isSigningUp.get()) {
                _sessionState.value = auth.currentUser.toSessionState()
            }
        }
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
    ): AuthResult {
        isSigningUp.set(true)
        try {
            val firebaseUser = try {
                firebaseAuth
                    .createUserWithEmailAndPassword(email, password)
                    .await()
                    .user
                    ?: return AuthResult.Failure(AuthError.Unknown)
            } catch (e: CancellationException) {
                throw e
            } catch (throwable: Throwable) {
                return AuthResult.Failure(throwable.toSignUpError())
            }

            val profile = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            try {
                firebaseUser.updateProfile(profile).await()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                return AuthResult.Success(
                    user = firebaseUser.toDomain(),
                    profileUpdateFailed = true,
                )
            }

            return AuthResult.Success(firebaseUser.toDomain())
        } finally {
            isSigningUp.set(false)
            _sessionState.value = firebaseAuth.currentUser.toSessionState()
        }
    }

    override suspend fun signIn(
        email: String,
        password: String,
    ): AuthResult {
        val firebaseUser = try {
            firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()
                .user
                ?: return AuthResult.Failure(AuthError.Unknown)
        } catch (e: CancellationException) {
            throw e
        } catch (throwable: Throwable) {
            return AuthResult.Failure(throwable.toSignInError())
        }

        // The auth state listener publishes Authenticated; nothing else to do here.
        return AuthResult.Success(firebaseUser.toDomain())
    }

    private fun FirebaseUser?.toSessionState(): SessionState =
        if (this != null) SessionState.Authenticated else SessionState.Unauthenticated

    private fun FirebaseUser.toDomain() = AuthUser(
        id = uid,
        name = displayName,
        email = email,
    )

    private fun Throwable.toSignUpError(): AuthError = when (this) {
        is FirebaseAuthWeakPasswordException -> AuthError.WeakPassword(reason)
        is FirebaseAuthUserCollisionException -> AuthError.EmailAlreadyInUse
        is FirebaseAuthInvalidCredentialsException -> AuthError.InvalidEmail
        is FirebaseNetworkException -> AuthError.NetworkUnavailable
        is FirebaseTooManyRequestsException -> AuthError.TooManyRequests
        else -> AuthError.Unknown
    }

    private fun Throwable.toSignInError(): AuthError = when (this) {
        is FirebaseAuthInvalidCredentialsException ->
            if (errorCode == ERROR_INVALID_EMAIL) AuthError.InvalidEmail else AuthError.InvalidCredentials
        is FirebaseAuthInvalidUserException ->
            if (errorCode == ERROR_USER_DISABLED) AuthError.AccountDisabled else AuthError.InvalidCredentials
        is FirebaseNetworkException -> AuthError.NetworkUnavailable
        is FirebaseTooManyRequestsException -> AuthError.TooManyRequests
        else -> AuthError.Unknown
    }

    private companion object {
        const val ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL"
        const val ERROR_USER_DISABLED = "ERROR_USER_DISABLED"
    }
}
