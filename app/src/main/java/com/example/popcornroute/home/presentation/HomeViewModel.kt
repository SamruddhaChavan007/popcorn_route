package com.example.popcornroute.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.popcornroute.home.data.repository.TmdbHttpException
import com.example.popcornroute.home.domain.repository.ConnectivityChecker
import com.example.popcornroute.home.domain.usecase.GetHomeContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeContent: GetHomeContentUseCase,
    private val connectivityChecker: ConnectivityChecker,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun onFilterSelected(filter: HomeFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onRefresh() {
        if (loadJob?.isActive == true) return
        load()
    }

    fun onRetry() {
        if (loadJob?.isActive == true) return
        load()
    }

    fun dismissRefreshError() {
        _uiState.update { it.copy(refreshError = null) }
    }

    private fun load() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            val hasContent = _uiState.value.content != null

            if (!connectivityChecker.isConnected()) {
                _uiState.update {
                    it.copy(
                        isCheckingConnectivity = false,
                        isLoading = false,
                        isRefreshing = false,
                        blockingError = if (hasContent) null else HomeError.Offline,
                        refreshError = if (hasContent) HomeError.Offline else null,
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isCheckingConnectivity = false,
                    isLoading = !hasContent,
                    isRefreshing = hasContent,
                    blockingError = null,
                    refreshError = null,
                )
            }

            try {
                val content = getHomeContent()
                _uiState.update {
                    it.copy(
                        content = content,
                        isLoading = false,
                        isRefreshing = false,
                        blockingError = null,
                        refreshError = null,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val homeError = error.toHomeError()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        blockingError = if (hasContent) null else homeError,
                        refreshError = if (hasContent) homeError else null,
                    )
                }
            }
        }
    }

    private fun Exception.toHomeError(): HomeError = when {
        this is IOException && !connectivityChecker.isConnected() -> HomeError.Offline
        this is TmdbHttpException && statusCode == 401 -> HomeError.RequestFailed(
            "TMDB authentication failed. Check the API credentials and try again.",
        )
        else -> HomeError.RequestFailed()
    }
}