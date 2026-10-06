package com.example.popcornroute.userlibrary.domain.repository

import com.example.popcornroute.userlibrary.domain.model.LibraryTitle
import com.example.popcornroute.userlibrary.domain.model.TitleKey
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    /** Favorites, newest first. Titles whose details are not cached yet have a null summary. */
    fun observeFavorites(): Flow<List<LibraryTitle>>

    fun observeIsFavorite(key: TitleKey): Flow<Boolean>

    /**
     * Sets the title's Favorite state to [favorite]. Taking the desired state, not "flip", makes
     * repeated or rapid calls safe: favoriting an existing favorite keeps its original time.
     */
    suspend fun setFavorite(key: TitleKey, favorite: Boolean)
}
