package com.rafaelmukhametov.githubusersandroid.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelmukhametov.githubusersandroid.data.model.Repository
import com.rafaelmukhametov.githubusersandroid.data.model.RepositoryOrder
import com.rafaelmukhametov.githubusersandroid.data.model.RepositorySort
import com.rafaelmukhametov.githubusersandroid.domain.repository.RepositoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepositoryListViewModel @Inject constructor(
    private val repository: RepositoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val username: String = savedStateHandle.get<String>("username") ?: ""
    
    private val _repositories = MutableStateFlow<List<Repository>>(emptyList())
    val repositories: StateFlow<List<Repository>> = _repositories.asStateFlow()
    
    private val _filteredRepositories = MutableStateFlow<List<Repository>>(emptyList())
    val filteredRepositories: StateFlow<List<Repository>> = _filteredRepositories.asStateFlow()
    
    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Loading)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()
    
    private val _selectedSort = MutableStateFlow<RepositorySort?>(RepositorySort.UPDATED)
    val selectedSort: StateFlow<RepositorySort?> = _selectedSort.asStateFlow()
    
    private val _selectedOrder = MutableStateFlow(RepositoryOrder.DESC)
    val selectedOrder: StateFlow<RepositoryOrder> = _selectedOrder.asStateFlow()
    
    private val _selectedLanguage = MutableStateFlow<String?>(null)
    val selectedLanguage: StateFlow<String?> = _selectedLanguage.asStateFlow()
    
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()
    
    val availableLanguages: StateFlow<List<String>> = MutableStateFlow(emptyList())
    
    private var currentPage = 1
    
    init {
        loadRepositories()
    }
    
    fun loadRepositories() {
        if (username.isEmpty()) {
            _loadingState.value = LoadingState.Error("Username is required")
            return
        }
        
        currentPage = 1
        _hasMorePages.value = true
        _loadingState.value = LoadingState.Loading
        
        viewModelScope.launch {
            try {
                val repos = repository.getRepositories(
                    username = username,
                    sort = _selectedSort.value,
                    order = _selectedOrder.value,
                    page = 1
                )
                _repositories.value = repos
                applyFilters()
                _loadingState.value = LoadingState.Loaded(emptyList())
                _hasMorePages.value = repos.size >= 30
                
                // Update available languages
                val languages = repos.mapNotNull { it.language }.distinct().sorted()
                (availableLanguages as MutableStateFlow).value = languages
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun loadNextPage() {
        if (_isLoadingMore.value || !_hasMorePages.value) return
        
        _isLoadingMore.value = true
        currentPage++
        
        viewModelScope.launch {
            try {
                val newRepos = repository.getRepositories(
                    username = username,
                    sort = _selectedSort.value,
                    order = _selectedOrder.value,
                    page = currentPage
                )
                
                if (newRepos.isNotEmpty()) {
                    _repositories.value = _repositories.value + newRepos
                    applyFilters()
                    _hasMorePages.value = newRepos.size >= 30
                } else {
                    _hasMorePages.value = false
                }
            } catch (e: Exception) {
                currentPage--
            } finally {
                _isLoadingMore.value = false
            }
        }
    }
    
    fun refresh() {
        loadRepositories()
    }
    
    fun changeSort(sort: RepositorySort?) {
        _selectedSort.value = sort
        loadRepositories()
    }
    
    fun changeOrder(order: RepositoryOrder) {
        _selectedOrder.value = order
        loadRepositories()
    }
    
    fun filterByLanguage(language: String?) {
        _selectedLanguage.value = language
        applyFilters()
    }
    
    fun updateSearchText(text: String) {
        _searchText.value = text
        applyFilters()
    }
    
    private fun applyFilters() {
        var filtered = _repositories.value
        
        // Filter by search text
        if (_searchText.value.isNotEmpty()) {
            filtered = filtered.filter { repo ->
                repo.name.contains(_searchText.value, ignoreCase = true) ||
                repo.description?.contains(_searchText.value, ignoreCase = true) == true
            }
        }
        
        // Filter by language
        _selectedLanguage.value?.let { language ->
            filtered = filtered.filter { it.language == language }
        }
        
        // Sort locally
        filtered = sortRepositories(filtered, _selectedSort.value, _selectedOrder.value)
        
        _filteredRepositories.value = filtered
    }
    
    private fun sortRepositories(
        repos: List<Repository>,
        sort: RepositorySort?,
        order: RepositoryOrder
    ): List<Repository> {
        val sorted = repos.toMutableList()
        
        when (sort) {
            RepositorySort.STARS -> sorted.sortByDescending { it.stars }
            RepositorySort.FULL_NAME -> sorted.sortBy { it.fullName }
            RepositorySort.UPDATED -> sorted.sortByDescending { it.updatedAt }
            else -> {}
        }
        
        if (order == RepositoryOrder.ASC) {
            sorted.reverse()
        }
        
        return sorted
    }
}

