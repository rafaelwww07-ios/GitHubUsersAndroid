package com.rafaelmukhametov.githubusersandroid.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rafaelmukhametov.githubusersandroid.data.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?
    
    @Query("SELECT * FROM users WHERE login = :login")
    suspend fun getUserByLogin(login: String): User?
    
    @Query("DELETE FROM users")
    suspend fun clearAll()
}

