package com.rafaelmukhametov.githubusersandroid.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelmukhametov.githubusersandroid.data.model.RepositoryDetail
import com.rafaelmukhametov.githubusersandroid.domain.repository.RepositoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RepositoryDetailViewModel @Inject constructor(
    private val repositoryRepository: RepositoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val owner: String = savedStateHandle.get<String>("owner") ?: ""
    private val repo: String = savedStateHandle.get<String>("repo") ?: ""
    
    private val _repository = MutableStateFlow<RepositoryDetail?>(null)
    val repository: StateFlow<RepositoryDetail?> = _repository.asStateFlow()
    
    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Loading)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()
    
    init {
        loadRepository()
    }
    
    private fun loadRepository() {
        if (owner.isEmpty() || repo.isEmpty()) {
            _loadingState.value = LoadingState.Error("Owner and repo are required")
            return
        }
        
        _loadingState.value = LoadingState.Loading
        
        viewModelScope.launch {
            try {
                val loadedRepo = repositoryRepository.getRepository(owner, repo)
                _repository.value = loadedRepo
                _loadingState.value = LoadingState.Loaded(emptyList())
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun refresh() {
        loadRepository()
    }
    
    fun formatDate(dateString: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(dateString) ?: return dateString
            
            val now = Date()
            val diff = now.time - date.time
            val days = diff / (24 * 60 * 60 * 1000)
            
            when {
                days < 1 -> "Today"
                days < 2 -> "Yesterday"
                days < 365 -> {
                    val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
                    formatter.format(date)
                }
                else -> {
                    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    formatter.format(date)
                }
            }
        } catch (e: Exception) {
            dateString
        }
    }
    
    fun formatSize(size: Int): String {
        return when {
            size < 1024 -> "$size KB"
            size < 1024 * 1024 -> String.format("%.1f MB", size / 1024.0)
            else -> String.format("%.1f GB", size / (1024.0 * 1024.0))
        }
    }
}

