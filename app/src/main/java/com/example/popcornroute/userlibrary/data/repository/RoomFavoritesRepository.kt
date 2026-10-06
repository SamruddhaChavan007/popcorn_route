package com.example.popcornroute.userlibrary.data.repository

import com.example.popcornroute.userlibrary.data.local.dao.FavoriteDao
import com.example.popcornroute.userlibrary.data.local.entity.FavoriteEntity
import com.example.popcornroute.userlibrary.data.mapper.toDomain
import com.example.popcornroute.userlibrary.domain.model.LibraryTitle
import com.example.popcornroute.userlibrary.domain.model.TitleKey
import com.example.popcornroute.userlibrary.domain.repository.CurrentUserProvider
import com.example.popcornroute.userlibrary.domain.repository.FavoritesRepository
import com.example.popcornroute.userlibrary.domain.repository.requireUserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock

class RoomFavoritesRepository(
    private val dao: FavoriteDao,
    private val currentUser: CurrentUserProvider,
    private val clock: Clock,
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<LibraryTitle>> =
        dao.observeFavorites(currentUser.requireUserId()).map { rows -> rows.map { it.toDomain() } }

    override fun observeIsFavorite(key: TitleKey): Flow<Boolean> =
        dao.observeIsFavorite(currentUser.requireUserId(), key.mediaType.value, key.tmdbId)

    override suspend fun setFavorite(key: TitleKey, favorite: Boolean) {
        val userId = currentUser.requireUserId()
        if (favorite) {
            dao.insertIgnore(FavoriteEntity(userId, key.mediaType.value, key.tmdbId, clock.millis()))
        } else {
            dao.delete(userId, key.mediaType.value, key.tmdbId)
        }
    }
}
