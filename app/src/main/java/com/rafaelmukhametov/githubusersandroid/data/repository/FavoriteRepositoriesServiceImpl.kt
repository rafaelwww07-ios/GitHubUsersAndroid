package com.rafaelmukhametov.githubusersandroid.data.repository

import com.rafaelmukhametov.githubusersandroid.data.local.FavoriteRepository
import com.rafaelmukhametov.githubusersandroid.data.local.FavoriteRepositoryDao
import com.rafaelmukhametov.githubusersandroid.data.model.Repository
import com.rafaelmukhametov.githubusersandroid.domain.repository.FavoriteRepositoriesService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoriesServiceImpl @Inject constructor(
    private val favoriteRepositoryDao: FavoriteRepositoryDao
) : FavoriteRepositoriesService {
    
    override fun getFavoriteRepositories(): Flow<List<FavoriteRepository>> {
        return favoriteRepositoryDao.getAllFavorites()
    }
    
    override suspend fun addFavorite(repository: Repository) {
        val favorite = FavoriteRepository(
            repositoryId = repository.id,
            name = repository.name,
            fullName = repository.fullName,
            description = repository.description,
            language = repository.language,
            stars = repository.stars,
            forks = repository.forks,
            htmlURL = repository.htmlURL
        )
        favoriteRepositoryDao.insertFavorite(favorite)
    }
    
    override suspend fun removeFavorite(repositoryId: Int) {
        favoriteRepositoryDao.deleteFavoriteById(repositoryId)
    }
    
    override suspend fun isFavorite(repositoryId: Int): Boolean {
        return favoriteRepositoryDao.isFavorite(repositoryId)
    }
}

