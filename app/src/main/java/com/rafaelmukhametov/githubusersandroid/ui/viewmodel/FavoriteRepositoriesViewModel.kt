package com.rafaelmukhametov.githubusersandroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelmukhametov.githubusersandroid.data.local.FavoriteRepository
import com.rafaelmukhametov.githubusersandroid.data.model.Repository
import com.rafaelmukhametov.githubusersandroid.domain.repository.FavoriteRepositoriesService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteRepositoriesViewModel @Inject constructor(
    private val favoriteRepositoriesService: FavoriteRepositoriesService
) : ViewModel() {
    
    private val _favorites = MutableStateFlow<List<FavoriteRepository>>(emptyList())
    val favorites: StateFlow<List<FavoriteRepository>> = _favorites.asStateFlow()
    
    init {
        loadFavorites()
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            favoriteRepositoriesService.getFavoriteRepositories().collect { favorites ->
                _favorites.value = favorites
            }
        }
    }
    
    fun removeFavorite(repository: FavoriteRepository) {
        viewModelScope.launch {
            favoriteRepositoriesService.removeFavorite(repository.repositoryId)
        }
    }
    
    suspend fun isFavorite(repositoryId: Int): Boolean {
        return favoriteRepositoriesService.isFavorite(repositoryId)
    }
    
    suspend fun addFavorite(repository: Repository) {
        favoriteRepositoriesService.addFavorite(repository)
    }
}

