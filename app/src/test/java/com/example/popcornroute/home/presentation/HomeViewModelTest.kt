package com.example.popcornroute.home.presentation

import com.example.popcornroute.home.domain.model.HomeContent
import com.example.popcornroute.home.domain.model.HomeMedia
import com.example.popcornroute.home.domain.model.HomeMediaType
import com.example.popcornroute.home.domain.repository.ConnectivityChecker
import com.example.popcornroute.home.domain.repository.HomeRepository
import com.example.popcornroute.home.domain.usecase.GetHomeContentUseCase
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeHomeRepository()
    private val connectivity = FakeConnectivityChecker()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `offline initial load shows error without starting repository request`() = runTest(dispatcher) {
        connectivity.connected = false

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertSame(HomeError.Offline, state.blockingError)
        assertFalse(state.isLoading)
        assertFalse(state.isCheckingConnectivity)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `online initial load shows loading until content arrives`() = runTest(dispatcher) {
        repository.gate = CompletableDeferred()

        val viewModel = createViewModel()
        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.blockingError)

        repository.gate?.complete(content)
        advanceUntilIdle()

        assertEquals(content, viewModel.uiState.value.content)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `retry while still offline never enters loading`() = runTest(dispatcher) {
        connectivity.connected = false
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRetry()
        advanceUntilIdle()

        assertSame(HomeError.Offline, viewModel.uiState.value.blockingError)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `retry after connectivity returns enters loading and succeeds`() = runTest(dispatcher) {
        connectivity.connected = false
        val viewModel = createViewModel()
        advanceUntilIdle()
        connectivity.connected = true
        repository.gate = CompletableDeferred()

        viewModel.onRetry()
        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.blockingError)

        repository.gate?.complete(content)
        advanceUntilIdle()
        assertEquals(content, viewModel.uiState.value.content)
    }

    @Test
    fun `refresh keeps existing content visible`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        repository.gate = CompletableDeferred()

        viewModel.onRefresh()
        runCurrent()

        val refreshing = viewModel.uiState.value
        assertEquals(content, refreshing.content)
        assertTrue(refreshing.isRefreshing)
        assertFalse(refreshing.isLoading)

        repository.gate?.complete(refreshedContent)
        advanceUntilIdle()
        assertEquals(refreshedContent, viewModel.uiState.value.content)
    }

    @Test
    fun `offline refresh keeps content and shows non blocking error`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        connectivity.connected = false

        viewModel.onRefresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(content, state.content)
        assertSame(HomeError.Offline, state.refreshError)
        assertNull(state.blockingError)
        assertFalse(state.isLoading)
    }

    @Test
    fun `failed refresh preserves content and can dismiss error`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        repository.failure = IOException("network failed")

        viewModel.onRefresh()
        advanceUntilIdle()

        assertEquals(content, viewModel.uiState.value.content)
        assertTrue(viewModel.uiState.value.refreshError is HomeError.RequestFailed)

        viewModel.dismissRefreshError()
        assertNull(viewModel.uiState.value.refreshError)
    }

    @Test
    fun `filter selection is owned by viewmodel`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onFilterSelected(HomeFilter.TV_SHOWS)

        assertEquals(HomeFilter.TV_SHOWS, viewModel.uiState.value.selectedFilter)
    }

    private fun createViewModel() = HomeViewModel(
        getHomeContent = GetHomeContentUseCase(repository),
        connectivityChecker = connectivity,
    )

    private class FakeConnectivityChecker : ConnectivityChecker {
        var connected = true
        override fun isConnected() = connected
    }

    private class FakeHomeRepository : HomeRepository {
        var calls = 0
        var result = content
        var gate: CompletableDeferred<HomeContent>? = null
        var failure: Exception? = null

        override suspend fun getHomeContent(): HomeContent {
            calls++
            failure?.let { throw it }
            return gate?.await() ?: result
        }
    }

    private companion object {
        val movie = HomeMedia(
            tmdbId = 1,
            mediaType = HomeMediaType.MOVIE,
            title = "Movie",
            posterUrl = null,
            backdropUrl = null,
            year = "2026",
        )
        val content = HomeContent(
            featured = listOf(movie),
            topMovies = listOf(movie),
            topShows = emptyList(),
            nowPlaying = listOf(movie),
            comingSoon = listOf(movie),
        )
        val refreshedContent = content.copy(comingSoon = emptyList())
    }
}
