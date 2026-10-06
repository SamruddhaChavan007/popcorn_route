package com.example.popcornroute.userlibrary.di

import android.content.Context
import androidx.room.Room
import com.example.popcornroute.userlibrary.data.local.UserLibraryDatabase
import com.example.popcornroute.userlibrary.data.local.dao.FavoriteDao
import com.example.popcornroute.userlibrary.data.local.dao.LibraryActionsDao
import com.example.popcornroute.userlibrary.data.local.dao.ListDao
import com.example.popcornroute.userlibrary.data.local.dao.ListItemDao
import com.example.popcornroute.userlibrary.data.local.dao.TitleCacheDao
import com.example.popcornroute.userlibrary.data.local.dao.WatchedDao
import com.example.popcornroute.userlibrary.data.repository.FirebaseCurrentUserProvider
import com.example.popcornroute.userlibrary.data.repository.RoomFavoritesRepository
import com.example.popcornroute.userlibrary.data.repository.RoomListsRepository
import com.example.popcornroute.userlibrary.data.repository.RoomWatchedRepository
import com.example.popcornroute.userlibrary.domain.repository.CurrentUserProvider
import com.example.popcornroute.userlibrary.domain.repository.FavoritesRepository
import com.example.popcornroute.userlibrary.domain.repository.ListsRepository
import com.example.popcornroute.userlibrary.domain.repository.WatchedRepository
import com.example.popcornroute.userlibrary.domain.usecase.ObserveTitleStateUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserLibraryModule {

    @Provides
    @Singleton
    fun provideUserLibraryDatabase(@ApplicationContext context: Context): UserLibraryDatabase =
        Room.databaseBuilder(context, UserLibraryDatabase::class.java, UserLibraryDatabase.NAME)
            .addCallback(UserLibraryDatabase.callback)
            .build()

    @Provides
    fun provideFavoriteDao(db: UserLibraryDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideWatchedDao(db: UserLibraryDatabase): WatchedDao = db.watchedDao()

    @Provides
    fun provideListDao(db: UserLibraryDatabase): ListDao = db.listDao()

    @Provides
    fun provideListItemDao(db: UserLibraryDatabase): ListItemDao = db.listItemDao()

    @Provides
    fun provideTitleCacheDao(db: UserLibraryDatabase): TitleCacheDao = db.titleCacheDao()

    @Provides
    fun provideLibraryActionsDao(db: UserLibraryDatabase): LibraryActionsDao = db.libraryActionsDao()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()

    // Reads FirebaseAuth directly, like AuthModule, so this module does not depend on AuthModule's bindings.
    @Provides
    @Singleton
    fun provideCurrentUserProvider(): CurrentUserProvider =
        FirebaseCurrentUserProvider(FirebaseAuth.getInstance())

    @Provides
    @Singleton
    fun provideFavoritesRepository(
        dao: FavoriteDao,
        currentUser: CurrentUserProvider,
        clock: Clock,
    ): FavoritesRepository = RoomFavoritesRepository(dao, currentUser, clock)

    @Provides
    @Singleton
    fun provideWatchedRepository(
        watchedDao: WatchedDao,
        actionsDao: LibraryActionsDao,
        currentUser: CurrentUserProvider,
        clock: Clock,
    ): WatchedRepository = RoomWatchedRepository(watchedDao, actionsDao, currentUser, clock)

    @Provides
    @Singleton
    fun provideListsRepository(
        listDao: ListDao,
        listItemDao: ListItemDao,
        actionsDao: LibraryActionsDao,
        currentUser: CurrentUserProvider,
        clock: Clock,
    ): ListsRepository = RoomListsRepository(listDao, listItemDao, actionsDao, currentUser, clock)

    @Provides
    fun provideObserveTitleStateUseCase(
        favorites: FavoritesRepository,
        watched: WatchedRepository,
        lists: ListsRepository,
    ): ObserveTitleStateUseCase = ObserveTitleStateUseCase(favorites, watched, lists)
}
