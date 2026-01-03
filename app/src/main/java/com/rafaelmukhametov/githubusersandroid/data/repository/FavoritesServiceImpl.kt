package com.rafaelmukhametov.githubusersandroid.data.repository

import android.content.Context
import androidx.core.content.ContextCompat
import com.rafaelmukhametov.githubusersandroid.data.local.FavoriteUser
import com.rafaelmukhametov.githubusersandroid.data.local.FavoriteUserDao
import com.rafaelmukhametov.githubusersandroid.data.model.User
import com.rafaelmukhametov.githubusersandroid.domain.repository.FavoritesService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesServiceImpl @Inject constructor(
    private val favoriteUserDao: FavoriteUserDao,
    private val context: Context
) : FavoritesService {
    
    override fun getFavoriteUsers(): Flow<List<FavoriteUser>> {
        return favoriteUserDao.getAllFavorites()
    }
    
    override suspend fun addFavorite(user: User) {
        val favorite = FavoriteUser(
            userId = user.id,
            login = user.login,
            avatarURL = user.avatarURL,
            name = user.name
        )
        favoriteUserDao.insertFavorite(favorite)
        // Update widget
        com.rafaelmukhametov.githubusersandroid.widget.GitHubUsersWidgetProvider.updateWidgets(context)
    }
    
    override suspend fun removeFavorite(userId: Int) {
        favoriteUserDao.deleteFavoriteById(userId)
        // Update widget
        com.rafaelmukhametov.githubusersandroid.widget.GitHubUsersWidgetProvider.updateWidgets(context)
    }
    
    override suspend fun isFavorite(userId: Int): Boolean {
        return favoriteUserDao.isFavorite(userId)
    }
}

