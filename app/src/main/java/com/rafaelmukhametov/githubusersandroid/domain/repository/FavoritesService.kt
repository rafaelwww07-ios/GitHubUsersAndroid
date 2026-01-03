package com.rafaelmukhametov.githubusersandroid.domain.repository

import com.rafaelmukhametov.githubusersandroid.data.local.FavoriteUser
import com.rafaelmukhametov.githubusersandroid.data.model.User
import kotlinx.coroutines.flow.Flow

interface FavoritesService {
    fun getFavoriteUsers(): Flow<List<FavoriteUser>>
    suspend fun addFavorite(user: User)
    suspend fun removeFavorite(userId: Int)
    suspend fun isFavorite(userId: Int): Boolean
}

