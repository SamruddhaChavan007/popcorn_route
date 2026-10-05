package com.example.popcornroute.debuglauncher

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.popcornroute.ui.components.PullToRefreshContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PullToRefreshScreen(modifier: Modifier = Modifier) {
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshCount by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val demoItems = remember { List(20) { index -> "Movie recommendation ${index + 1}" } }

    PullToRefreshContainer(
        isRefreshing = isRefreshing,
        onRefresh = {
            if (!isRefreshing) {
                isRefreshing = true
                coroutineScope.launch {
                    delay(1_500)
                    refreshCount += 1
                    isRefreshing = false
                }
            }
        },
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            item {
                Text(
                    text = "Pull to Refresh",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            item {
                Text(
                    text = "Pull down to test the expressive loading indicator. Refreshes completed: $refreshCount",
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 12.dp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            items(
                items = demoItems,
                key = { it },
            ) { item ->
                ListItem(
                    supportingContent = { Text(text = "Swipe down when the list is at the top") },
                ) {
                    Text(text = item)
                }
            }
        }
    }
}
