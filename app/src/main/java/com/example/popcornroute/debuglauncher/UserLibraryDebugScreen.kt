package com.example.popcornroute.debuglauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Debug-only screen for exercising the user library; inspect `user_library.db` in Database Inspector. */
@Composable
fun UserLibraryDebugScreen(
    modifier: Modifier = Modifier,
    viewModel: UserLibraryDebugViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "User Library (debug)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        state?.let { s ->
            Text("Title: ${s.key.mediaType.value} ${s.key.tmdbId}", fontWeight = FontWeight.Bold)
            Text(
                "favorite=${s.titleState.isFavorite}  watched=${s.titleState.isWatched}  " +
                    "lists=${s.titleState.listIds}",
            )
            Text("Totals: favorites=${s.favoritesCount}  watched=${s.watchedCount}  watchlist=${s.watchlistCount}")
            Text("Lists: ${s.lists.joinToString { it.name }.ifEmpty { "none" }}")
            Text("Last: ${s.message}", color = MaterialTheme.colorScheme.primary)
        } ?: Text("Loading...")

        DebugButton("Switch sample title", viewModel::switchTitle)
        DebugButton("Ensure Watchlist row", viewModel::ensureWatchlist)

        ToggleRow("Favorite", viewModel::setFavorite)
        ToggleRow("In Watchlist", viewModel::setInWatchlist)

        DebugButton("Mark as watched", viewModel::markAsWatched)
        DebugButton("Undo mark as watched", viewModel::undoMarkAsWatched)
        DebugButton("Mark as not watched", viewModel::markAsNotWatched)

        DebugButton("Create custom list", viewModel::createList)
        DebugButton("Add title to last custom list", viewModel::addToLastCustomList)
        DebugButton("Delete last custom list", viewModel::deleteLastCustomList)
    }
}

@Composable
private fun ToggleRow(label: String, onSet: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(onClick = { onSet(true) }, modifier = Modifier.weight(1f)) { Text("$label ON") }
        Button(onClick = { onSet(false) }, modifier = Modifier.weight(1f)) { Text("$label OFF") }
    }
}

@Composable
private fun DebugButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
}
