package com.rafaelmukhametov.githubusersandroid.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteRepositoryDao {
    @Query("SELECT * FROM favorite_repositories ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteRepository>>
    
    @Query("SELECT * FROM favorite_repositories WHERE repositoryId = :repositoryId")
    suspend fun getFavoriteById(repositoryId: Int): FavoriteRepository?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteRepository)
    
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteRepository)
    
    @Query("DELETE FROM favorite_repositories WHERE repositoryId = :repositoryId")
    suspend fun deleteFavoriteById(repositoryId: Int)
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_repositories WHERE repositoryId = :repositoryId)")
    suspend fun isFavorite(repositoryId: Int): Boolean
}

