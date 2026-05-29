package com.example.mamunbingoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.data.projects.ProjectsRepository
import com.example.mamunbingoapp.ui.projects.ProjectUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProjectsUiState {
    data object Loading : ProjectsUiState
    data class Success(
        val featuredProjects: List<ProjectUiModel>,
        val recentProjects: List<ProjectUiModel>,
    ) : ProjectsUiState
    data class Error(val message: String) : ProjectsUiState
    data object Empty : ProjectsUiState
}

class ProjectsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectsUiState>(ProjectsUiState.Loading)
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError.asStateFlow()

    private val _lastUpdatedAtMillis = MutableStateFlow<Long?>(null)
    val lastUpdatedAtMillis: StateFlow<Long?> = _lastUpdatedAtMillis.asStateFlow()

    private var hasCachedSnapshot = false

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            val cached = ProjectsRepository.loadCachedProjects()
            hasCachedSnapshot = cached != null
            if (cached != null) {
                applyProjects(cached.projects)
                _lastUpdatedAtMillis.value = cached.updatedAtMillis
            } else {
                _uiState.value = ProjectsUiState.Loading
            }
            fetchFromNetwork(showFullScreenLoading = cached == null)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            fetchFromNetwork(showFullScreenLoading = false, userInitiatedRefresh = true)
        }
    }

    private suspend fun fetchFromNetwork(
        showFullScreenLoading: Boolean,
        userInitiatedRefresh: Boolean = false,
    ) {
        val showingCachedContent = hasDisplayableCachedContent()
        if (userInitiatedRefresh || showingCachedContent) {
            _isRefreshing.value = true
            _refreshError.value = null
        } else if (showFullScreenLoading) {
            _uiState.value = ProjectsUiState.Loading
            _refreshError.value = null
        }

        ProjectsRepository.fetchProjects()
            .onSuccess { result ->
                applyProjects(result.projects)
                _lastUpdatedAtMillis.value = result.updatedAtMillis
                _refreshError.value = null
                hasCachedSnapshot = true
            }
            .onFailure { error ->
                val message = error.message ?: "Could not load projects."
                if (hasDisplayableCachedContent()) {
                    _refreshError.value = message
                } else {
                    _uiState.value = ProjectsUiState.Error(message)
                }
            }

        _isRefreshing.value = false
    }

    private fun hasDisplayableCachedContent(): Boolean = when (val state = _uiState.value) {
        is ProjectsUiState.Success -> true
        ProjectsUiState.Empty -> hasCachedSnapshot
        else -> false
    }

    private fun applyProjects(projects: List<ProjectUiModel>) {
        _uiState.value = if (projects.isEmpty()) {
            ProjectsUiState.Empty
        } else {
            ProjectsUiState.Success(
                featuredProjects = projects.filter { it.isFeatured },
                recentProjects = projects.filter { !it.isFeatured },
            )
        }
    }
}
