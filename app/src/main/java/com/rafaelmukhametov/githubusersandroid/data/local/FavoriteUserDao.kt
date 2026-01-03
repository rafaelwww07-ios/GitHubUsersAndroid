package com.rafaelmukhametov.githubusersandroid.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteUserDao {
    @Query("SELECT * FROM favorite_users ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteUser>>
    
    @Query("SELECT * FROM favorite_users WHERE userId = :userId")
    suspend fun getFavoriteById(userId: Int): FavoriteUser?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteUser)
    
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteUser)
    
    @Query("DELETE FROM favorite_users WHERE userId = :userId")
    suspend fun deleteFavoriteById(userId: Int)
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_users WHERE userId = :userId)")
    suspend fun isFavorite(userId: Int): Boolean
}

