package com.rafaelmukhametov.githubusersandroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelmukhametov.githubusersandroid.data.model.User
import com.rafaelmukhametov.githubusersandroid.domain.repository.FavoritesService
import com.rafaelmukhametov.githubusersandroid.domain.repository.SearchHistoryService
import com.rafaelmukhametov.githubusersandroid.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoadingState {
    object Idle : LoadingState()
    object Loading : LoadingState()
    data class Loaded(val users: List<User>) : LoadingState()
    data class Error(val message: String) : LoadingState()
}

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val favoritesService: FavoritesService,
    private val searchHistoryService: SearchHistoryService
) : ViewModel() {
    
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    
    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()
    
    private val _showHistory = MutableStateFlow(false)
    val showHistory: StateFlow<Boolean> = _showHistory.asStateFlow()
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()
    
    private var currentPage = 1
    private var currentQuery = ""
    private var searchJob: Job? = null
    
    init {
        viewModelScope.launch {
            searchHistoryService.history.collect { history ->
                _searchHistory.value = history
            }
        }
        viewModelScope.launch {
            _searchText.collect { query ->
                _showHistory.value = query.isEmpty()
                if (query.isNotEmpty()) {
                    debouncedSearch(query)
                } else {
                    _loadingState.value = LoadingState.Idle
                    _hasMorePages.value = true
                    currentPage = 1
                }
            }
        }
    }
    
    private fun debouncedSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            searchUsers(query)
        }
    }
    
    fun updateSearchText(text: String) {
        _searchText.value = text
    }
    
    fun searchUsers(query: String) {
        if (query.isEmpty()) {
            _loadingState.value = LoadingState.Idle
            return
        }
        
        currentPage = 1
        currentQuery = query
        _hasMorePages.value = true
        _loadingState.value = LoadingState.Loading
        
        viewModelScope.launch {
            try {
                val users = userRepository.searchUsers(query, 1)
                _loadingState.value = LoadingState.Loaded(users)
                _hasMorePages.value = users.size >= 30
                
                if (users.isNotEmpty()) {
                    searchHistoryService.addToHistory(query)
                }
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun loadNextPage() {
        if (_isLoadingMore.value || !_hasMorePages.value || currentQuery.isEmpty()) {
            return
        }
        
        _isLoadingMore.value = true
        currentPage++
        
        viewModelScope.launch {
            try {
                val newUsers = userRepository.searchUsers(currentQuery, currentPage)
                val currentUsers = (_loadingState.value as? LoadingState.Loaded)?.users ?: emptyList()
                _loadingState.value = LoadingState.Loaded(currentUsers + newUsers)
                _hasMorePages.value = newUsers.size >= 30
            } catch (e: Exception) {
                currentPage--
            } finally {
                _isLoadingMore.value = false
            }
        }
    }
    
    fun refresh() {
        if (currentQuery.isEmpty()) return
        
        _isRefreshing.value = true
        currentPage = 1
        _hasMorePages.value = true
        
        viewModelScope.launch {
            try {
                val users = userRepository.searchUsers(currentQuery, 1)
                _loadingState.value = LoadingState.Loaded(users)
                _hasMorePages.value = users.size >= 30
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    suspend fun isFavorite(user: User): Boolean {
        return favoritesService.isFavorite(user.id)
    }
    
    fun selectHistoryItem(query: String) {
        _searchText.value = query
        _showHistory.value = false
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            searchHistoryService.clearHistory()
        }
    }
    
    fun removeHistoryItem(query: String) {
        viewModelScope.launch {
            searchHistoryService.removeFromHistory(query)
        }
    }
}

