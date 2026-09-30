package com.example.popcornroute.navigation.auth

import com.example.popcornroute.auth.di.AuthModule
import com.example.popcornroute.auth.domain.error.AuthError
import com.example.popcornroute.auth.domain.model.AuthResult
import com.example.popcornroute.auth.domain.model.SessionState
import com.example.popcornroute.auth.domain.repository.AuthRepository
import com.example.popcornroute.auth.domain.usecase.SignInUseCase
import com.example.popcornroute.auth.domain.usecase.SignUpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Singleton

/** Keeps navigation tests off Firebase; only screen-to-screen flow is under test. */
class FakeAuthRepository : AuthRepository {
    override val sessionState: StateFlow<SessionState> = MutableStateFlow(SessionState.Unauthenticated)

    override suspend fun signUp(name: String, email: String, password: String): AuthResult =
        AuthResult.Failure(AuthError.Unknown)

    override suspend fun signIn(email: String, password: String): AuthResult =
        AuthResult.Failure(AuthError.Unknown)
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [AuthModule::class])
object FakeAuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository = FakeAuthRepository()

    @Provides
    fun provideSignUpUseCase(authRepository: AuthRepository): SignUpUseCase =
        SignUpUseCase(authRepository)

    @Provides
    fun provideSignInUseCase(authRepository: AuthRepository): SignInUseCase =
        SignInUseCase(authRepository)
}
