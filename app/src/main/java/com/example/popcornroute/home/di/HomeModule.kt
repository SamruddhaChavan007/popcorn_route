package com.example.popcornroute.home.di

import android.content.Context
import com.example.popcornroute.BuildConfig
import com.example.popcornroute.home.data.connectivity.AndroidConnectivityChecker
import com.example.popcornroute.home.data.repository.TmdbHomeRepository
import com.example.popcornroute.home.domain.repository.ConnectivityChecker
import com.example.popcornroute.home.domain.repository.HomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {
    @Provides
    @Singleton
    fun provideConnectivityChecker(
        @ApplicationContext context: Context,
    ): ConnectivityChecker = AndroidConnectivityChecker(context)

    @Provides
    @Singleton
    fun provideHomeRepository(): HomeRepository = TmdbHomeRepository(
        client = OkHttpClient(),
        readAccessToken = BuildConfig.TMDB_READ_ACCESS_TOKEN,
        apiKey = BuildConfig.TMDB_API_KEY,
    )
}
