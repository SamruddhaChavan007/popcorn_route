package com.example.popcornroute.userlibrary.data.repository

import com.example.popcornroute.userlibrary.data.local.dao.LibraryActionsDao
import com.example.popcornroute.userlibrary.data.local.dao.WatchedDao
import com.example.popcornroute.userlibrary.data.local.entity.WatchedEntity
import com.example.popcornroute.userlibrary.data.mapper.newWatchlistEntity
import com.example.popcornroute.userlibrary.data.mapper.toDomain
import com.example.popcornroute.userlibrary.data.mapper.toEntity
import com.example.popcornroute.userlibrary.domain.model.LibraryTitle
import com.example.popcornroute.userlibrary.domain.model.MarkAsWatchedResult
import com.example.popcornroute.userlibrary.domain.model.TitleKey
import com.example.popcornroute.userlibrary.domain.repository.CurrentUserProvider
import com.example.popcornroute.userlibrary.domain.repository.WatchedRepository
import com.example.popcornroute.userlibrary.domain.repository.requireUserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock

class RoomWatchedRepository(
    private val watchedDao: WatchedDao,
    private val actionsDao: LibraryActionsDao,
    private val currentUser: CurrentUserProvider,
    private val clock: Clock,
) : WatchedRepository {

    override fun observeWatched(): Flow<List<LibraryTitle>> =
        watchedDao.observeWatched(currentUser.requireUserId()).map { rows -> rows.map { it.toDomain() } }

    override fun observeIsWatched(key: TitleKey): Flow<Boolean> =
        watchedDao.observeIsWatched(currentUser.requireUserId(), key.mediaType.value, key.tmdbId)

    override suspend fun markAsWatched(key: TitleKey): MarkAsWatchedResult {
        val userId = currentUser.requireUserId()
        val now = clock.millis()
        return actionsDao.markAsWatched(
            watchlist = newWatchlistEntity(userId, now),
            watched = WatchedEntity(userId, key.mediaType.value, key.tmdbId, now),
        ).toDomain()
    }

    override suspend fun undoMarkAsWatched(key: TitleKey, result: MarkAsWatchedResult) {
        // An unchanged result means nothing was marked, so there is nothing to undo.
        if (!result.changed) return
        val userId = currentUser.requireUserId()
        actionsDao.undoMarkAsWatched(
            watchlist = newWatchlistEntity(userId, clock.millis()),
            userId = userId,
            mediaType = key.mediaType.value,
            tmdbId = key.tmdbId,
            removedWatchlistItem = result.removedFromWatchlist?.toEntity(userId),
        )
    }

    override suspend fun markAsNotWatched(key: TitleKey) {
        watchedDao.delete(currentUser.requireUserId(), key.mediaType.value, key.tmdbId)
    }
}
