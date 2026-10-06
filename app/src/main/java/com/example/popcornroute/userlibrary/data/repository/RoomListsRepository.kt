package com.example.popcornroute.userlibrary.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.popcornroute.userlibrary.data.local.dao.LibraryActionsDao
import com.example.popcornroute.userlibrary.data.local.dao.ListDao
import com.example.popcornroute.userlibrary.data.local.dao.ListItemDao
import com.example.popcornroute.userlibrary.data.local.entity.ListItemEntity
import com.example.popcornroute.userlibrary.data.mapper.newCustomListEntity
import com.example.popcornroute.userlibrary.data.mapper.newWatchlistEntity
import com.example.popcornroute.userlibrary.data.mapper.toDomain
import com.example.popcornroute.userlibrary.domain.model.CreateListResult
import com.example.popcornroute.userlibrary.domain.model.LibraryList
import com.example.popcornroute.userlibrary.domain.model.LibraryTitle
import com.example.popcornroute.userlibrary.domain.model.ListName
import com.example.popcornroute.userlibrary.domain.model.ListNameError
import com.example.popcornroute.userlibrary.domain.model.RenameListResult
import com.example.popcornroute.userlibrary.domain.model.TitleKey
import com.example.popcornroute.userlibrary.domain.repository.CurrentUserProvider
import com.example.popcornroute.userlibrary.domain.repository.ListsRepository
import com.example.popcornroute.userlibrary.domain.repository.requireUserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.util.UUID

class RoomListsRepository(
    private val listDao: ListDao,
    private val listItemDao: ListItemDao,
    private val actionsDao: LibraryActionsDao,
    private val currentUser: CurrentUserProvider,
    private val clock: Clock,
    private val newListId: () -> String = { UUID.randomUUID().toString() },
) : ListsRepository {

    override fun observeLists(): Flow<List<LibraryList>> =
        listDao.observeLists(currentUser.requireUserId()).map { rows -> rows.map { it.toDomain() } }

    override fun observeItems(listId: String): Flow<List<LibraryTitle>> =
        listItemDao.observeItems(currentUser.requireUserId(), listId).map { rows -> rows.map { it.toDomain() } }

    override fun observeListIdsContaining(key: TitleKey): Flow<Set<String>> =
        listItemDao.observeListIdsContaining(currentUser.requireUserId(), key.mediaType.value, key.tmdbId)
            .map { it.toSet() }

    override suspend fun ensureWatchlist() {
        val userId = currentUser.requireUserId()
        listDao.ensureWatchlist(newWatchlistEntity(userId, clock.millis()))
    }

    override suspend fun setInList(listId: String, key: TitleKey, inList: Boolean) {
        val userId = currentUser.requireUserId()
        if (!inList) {
            listItemDao.delete(userId, listId, key.mediaType.value, key.tmdbId)
            return
        }
        val now = clock.millis()
        val item = ListItemEntity(userId, listId, key.mediaType.value, key.tmdbId, now)
        if (listId == LibraryList.WATCHLIST_ID) {
            actionsDao.addToWatchlist(newWatchlistEntity(userId, now), item)
        } else {
            listItemDao.insertIgnore(item)
        }
    }

    override suspend fun createList(name: String): CreateListResult {
        ListName.validate(name)?.let { return CreateListResult.Failure(it) }
        val entity = newCustomListEntity(currentUser.requireUserId(), newListId(), name, clock.millis())
        return try {
            listDao.insert(entity)
            CreateListResult.Success(entity.toDomain())
        } catch (_: SQLiteConstraintException) {
            CreateListResult.Failure(ListNameError.Duplicate)
        }
    }

    override suspend fun renameList(listId: String, name: String): RenameListResult {
        ListName.validate(name)?.let { return RenameListResult.Failure(it) }
        val userId = currentUser.requireUserId()
        return try {
            val changed = listDao.rename(userId, listId, ListName.normalize(name), ListName.keyOf(name))
            if (changed == 0) RenameListResult.NotAllowed else RenameListResult.Success
        } catch (_: SQLiteConstraintException) {
            RenameListResult.Failure(ListNameError.Duplicate)
        }
    }

    override suspend fun deleteList(listId: String): Boolean =
        listDao.deleteCustom(currentUser.requireUserId(), listId) > 0
}
