package com.example.popcornroute.auth.di

import com.example.popcornroute.auth.data.repository.FirebaseAuthRepository
import com.example.popcornroute.auth.domain.repository.AuthRepository
import com.example.popcornroute.auth.domain.usecase.SignInUseCase
import com.example.popcornroute.auth.domain.usecase.SignUpUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository =
        FirebaseAuthRepository(firebaseAuth)

    @Provides
    fun provideSignUpUseCase(authRepository: AuthRepository): SignUpUseCase =
        SignUpUseCase(authRepository)

    @Provides
    fun provideSignInUseCase(authRepository: AuthRepository): SignInUseCase =
        SignInUseCase(authRepository)
}
