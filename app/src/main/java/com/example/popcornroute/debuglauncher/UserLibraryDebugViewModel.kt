package com.example.popcornroute.debuglauncher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.popcornroute.userlibrary.domain.model.CreateListResult
import com.example.popcornroute.userlibrary.domain.model.LibraryList
import com.example.popcornroute.userlibrary.domain.model.MarkAsWatchedResult
import com.example.popcornroute.userlibrary.domain.model.MediaType
import com.example.popcornroute.userlibrary.domain.model.TitleKey
import com.example.popcornroute.userlibrary.domain.model.TitleState
import com.example.popcornroute.userlibrary.domain.repository.FavoritesRepository
import com.example.popcornroute.userlibrary.domain.repository.ListsRepository
import com.example.popcornroute.userlibrary.domain.repository.WatchedRepository
import com.example.popcornroute.userlibrary.domain.usecase.ObserveTitleStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserLibraryDebugState(
    val key: TitleKey,
    val titleState: TitleState,
    val lists: List<LibraryList>,
    val favoritesCount: Int,
    val watchedCount: Int,
    val watchlistCount: Int,
    val message: String,
)

/** Debug-only: drives the user library repositories so the database can be checked in Database Inspector. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserLibraryDebugViewModel @Inject constructor(
    private val favorites: FavoritesRepository,
    private val watched: WatchedRepository,
    private val lists: ListsRepository,
    observeTitleState: ObserveTitleStateUseCase,
) : ViewModel() {

    private val sampleTitles = listOf(
        TitleKey(MediaType.MOVIE, 603),
        TitleKey(MediaType.TV, 1399),
    )
    private val selectedKey = MutableStateFlow(sampleTitles.first())
    private val message = MutableStateFlow("Ready")
    private var lastMarkResult: MarkAsWatchedResult? = null
    private var listCounter = 1

    val state: StateFlow<UserLibraryDebugState?> = combine(
        selectedKey,
        selectedKey.flatMapLatest { observeTitleState(it) },
        lists.observeLists(),
        favorites.observeFavorites(),
        watched.observeWatched(),
        lists.observeItems(LibraryList.WATCHLIST_ID),
        message,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        UserLibraryDebugState(
            key = values[0] as TitleKey,
            titleState = values[1] as TitleState,
            lists = values[2] as List<LibraryList>,
            favoritesCount = (values[3] as List<*>).size,
            watchedCount = (values[4] as List<*>).size,
            watchlistCount = (values[5] as List<*>).size,
            message = values[6] as String,
        )
    }.map<UserLibraryDebugState, UserLibraryDebugState?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun switchTitle() {
        selectedKey.update { current -> sampleTitles.first { it != current } }
    }

    fun ensureWatchlist() = run("Watchlist ensured") { lists.ensureWatchlist() }

    fun setFavorite(on: Boolean) = run("Favorite = $on") { favorites.setFavorite(selectedKey.value, on) }

    fun setInWatchlist(on: Boolean) =
        run("Watchlist = $on") { lists.setInList(LibraryList.WATCHLIST_ID, selectedKey.value, on) }

    fun markAsWatched() = run("") {
        val result = watched.markAsWatched(selectedKey.value)
        lastMarkResult = result
        message.value = when {
            !result.changed -> "Already watched: nothing changed"
            result.removedFromWatchlist != null -> "Marked as watched and removed from Watchlist"
            else -> "Marked as watched"
        }
    }

    fun undoMarkAsWatched() = run("") {
        val result = lastMarkResult
        if (result == null || !result.changed) {
            message.value = "Nothing to undo"
        } else {
            watched.undoMarkAsWatched(selectedKey.value, result)
            lastMarkResult = null
            message.value = "Undone"
        }
    }

    fun markAsNotWatched() = run("Marked as not watched") { watched.markAsNotWatched(selectedKey.value) }

    fun createList() = run("") {
        val result = lists.createList("Debug list ${listCounter++}")
        message.value = when (result) {
            is CreateListResult.Success -> "Created ${result.list.name}"
            is CreateListResult.Failure -> "Create failed: ${result.error}"
        }
    }

    fun addToLastCustomList() = run("") {
        val list = state.value?.lists?.lastOrNull { !it.isBuiltIn }
        if (list == null) {
            message.value = "No custom list yet"
        } else {
            lists.setInList(list.id, selectedKey.value, true)
            message.value = "Added to ${list.name}"
        }
    }

    fun deleteLastCustomList() = run("") {
        val list = state.value?.lists?.lastOrNull { !it.isBuiltIn }
        if (list == null) {
            message.value = "No custom list to delete"
        } else {
            lists.deleteList(list.id)
            message.value = "Deleted ${list.name}"
        }
    }

    private fun run(done: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                if (done.isNotEmpty()) message.value = done
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                message.value = "Error: ${e.message}"
            }
        }
    }
}
