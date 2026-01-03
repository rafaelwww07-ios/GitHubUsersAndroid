package com.rafaelmukhametov.githubusersandroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelmukhametov.githubusersandroid.data.model.Repository
import com.rafaelmukhametov.githubusersandroid.data.model.RepositoryOrder
import com.rafaelmukhametov.githubusersandroid.data.model.RepositorySort
import com.rafaelmukhametov.githubusersandroid.domain.repository.RepositoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepositorySearchViewModel @Inject constructor(
    private val repository: RepositoryRepository
) : ViewModel() {
    
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    
    private val _repositories = MutableStateFlow<List<Repository>>(emptyList())
    val repositories: StateFlow<List<Repository>> = _repositories.asStateFlow()
    
    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()
    
    private val _selectedSort = MutableStateFlow<RepositorySort?>(RepositorySort.STARS)
    val selectedSort: StateFlow<RepositorySort?> = _selectedSort.asStateFlow()
    
    private val _selectedOrder = MutableStateFlow(RepositoryOrder.DESC)
    val selectedOrder: StateFlow<RepositoryOrder> = _selectedOrder.asStateFlow()
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    private val _hasMorePages = MutableStateFlow(false)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()
    
    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()
    
    private var currentPage = 1
    private var searchJob: Job? = null
    
    init {
        viewModelScope.launch {
            _searchText.collect { query ->
                if (query.isNotEmpty()) {
                    debouncedSearch(query)
                } else {
                    _repositories.value = emptyList()
                    _loadingState.value = LoadingState.Idle
                    currentPage = 1
                    _hasMorePages.value = false
                }
            }
        }
    }
    
    private fun debouncedSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            performSearch(query)
        }
    }
    
    fun updateSearchText(text: String) {
        _searchText.value = text
    }
    
    fun performSearch(query: String) {
        if (query.isEmpty()) {
            _repositories.value = emptyList()
            _loadingState.value = LoadingState.Idle
            return
        }
        
        currentPage = 1
        _repositories.value = emptyList()
        _loadingState.value = LoadingState.Loading
        
        viewModelScope.launch {
            try {
                val (repos, total, hasMore) = repository.searchRepositories(
                    query = query,
                    sort = _selectedSort.value,
                    order = _selectedOrder.value,
                    page = 1
                )
                _repositories.value = repos
                _totalCount.value = total
                _hasMorePages.value = hasMore
                _loadingState.value = LoadingState.Loaded(emptyList())
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun loadNextPage() {
        if (_isLoadingMore.value || !_hasMorePages.value || _searchText.value.isEmpty()) {
            return
        }
        
        _isLoadingMore.value = true
        currentPage++
        
        viewModelScope.launch {
            try {
                val (repos, _, hasMore) = repository.searchRepositories(
                    query = _searchText.value,
                    sort = _selectedSort.value,
                    order = _selectedOrder.value,
                    page = currentPage
                )
                _repositories.value = _repositories.value + repos
                _hasMorePages.value = hasMore
            } catch (e: Exception) {
                currentPage--
            } finally {
                _isLoadingMore.value = false
            }
        }
    }
    
    fun changeSort(sort: RepositorySort?) {
        _selectedSort.value = sort
        if (_searchText.value.isNotEmpty()) {
            performSearch(_searchText.value)
        }
    }
    
    fun changeOrder(order: RepositoryOrder) {
        _selectedOrder.value = order
        if (_searchText.value.isNotEmpty()) {
            performSearch(_searchText.value)
        }
    }
    
    fun refresh() {
        if (_searchText.value.isNotEmpty()) {
            performSearch(_searchText.value)
        }
    }
}

