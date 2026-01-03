package com.rafaelmukhametov.githubusersandroid.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelmukhametov.githubusersandroid.data.model.User
import com.rafaelmukhametov.githubusersandroid.domain.repository.FavoritesService
import com.rafaelmukhametov.githubusersandroid.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val favoritesService: FavoritesService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val username: String = savedStateHandle.get<String>("username") ?: ""
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Loading)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()
    
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
    
    init {
        loadUser()
    }
    
    private fun loadUser() {
        if (username.isEmpty()) {
            _loadingState.value = LoadingState.Error("Username is required")
            return
        }
        
        _loadingState.value = LoadingState.Loading
        
        viewModelScope.launch {
            try {
                val loadedUser = userRepository.getUser(username)
                _user.value = loadedUser
                _isFavorite.value = favoritesService.isFavorite(loadedUser.id)
                _loadingState.value = LoadingState.Loaded(emptyList())
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun toggleFavorite() {
        val currentUser = _user.value ?: return
        
        viewModelScope.launch {
            if (_isFavorite.value) {
                favoritesService.removeFavorite(currentUser.id)
            } else {
                favoritesService.addFavorite(currentUser)
            }
            _isFavorite.value = !_isFavorite.value
        }
    }
    
    fun refresh() {
        loadUser()
    }
}

