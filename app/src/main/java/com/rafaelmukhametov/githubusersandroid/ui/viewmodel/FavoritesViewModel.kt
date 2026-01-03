package com.rafaelmukhametov.githubusersandroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelmukhametov.githubusersandroid.data.local.FavoriteUser
import com.rafaelmukhametov.githubusersandroid.data.model.User
import com.rafaelmukhametov.githubusersandroid.domain.repository.FavoritesService
import com.rafaelmukhametov.githubusersandroid.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesService: FavoritesService,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _favoriteUsers = MutableStateFlow<List<FavoriteUser>>(emptyList())
    val favoriteUsers: StateFlow<List<FavoriteUser>> = _favoriteUsers.asStateFlow()
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadFavorites()
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            favoritesService.getFavoriteUsers().collect { favorites ->
                _favoriteUsers.value = favorites
                // Загружаем полную информацию о пользователях
                _isLoading.value = true
                val loadedUsers = favorites.mapNotNull { favorite ->
                    try {
                        userRepository.getUser(favorite.login)
                    } catch (e: Exception) {
                        null
                    }
                }
                _users.value = loadedUsers
                _isLoading.value = false
            }
        }
    }
    
    fun removeFavorite(user: User) {
        viewModelScope.launch {
            favoritesService.removeFavorite(user.id)
        }
    }
}

