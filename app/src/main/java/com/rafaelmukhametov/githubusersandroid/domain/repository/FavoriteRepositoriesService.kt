package com.rafaelmukhametov.githubusersandroid.domain.repository

import com.rafaelmukhametov.githubusersandroid.data.local.FavoriteRepository
import com.rafaelmukhametov.githubusersandroid.data.model.Repository
import kotlinx.coroutines.flow.Flow

interface FavoriteRepositoriesService {
    fun getFavoriteRepositories(): Flow<List<FavoriteRepository>>
    suspend fun addFavorite(repository: Repository)
    suspend fun removeFavorite(repositoryId: Int)
    suspend fun isFavorite(repositoryId: Int): Boolean
}

