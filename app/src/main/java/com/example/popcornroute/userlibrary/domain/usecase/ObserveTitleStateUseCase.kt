package com.example.popcornroute.userlibrary.domain.usecase

import com.example.popcornroute.userlibrary.domain.model.TitleKey
import com.example.popcornroute.userlibrary.domain.model.TitleState
import com.example.popcornroute.userlibrary.domain.repository.FavoritesRepository
import com.example.popcornroute.userlibrary.domain.repository.ListsRepository
import com.example.popcornroute.userlibrary.domain.repository.WatchedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/** Everything the user saved about one title, kept live; drives the toggles on cards and details. */
class ObserveTitleStateUseCase(
    private val favorites: FavoritesRepository,
    private val watched: WatchedRepository,
    private val lists: ListsRepository,
) {
    operator fun invoke(key: TitleKey): Flow<TitleState> = combine(
        favorites.observeIsFavorite(key),
        watched.observeIsWatched(key),
        lists.observeListIdsContaining(key),
    ) { isFavorite, isWatched, listIds ->
        TitleState(isFavorite = isFavorite, isWatched = isWatched, listIds = listIds)
    }.distinctUntilChanged()
}
