package com.rafaelmukhametov.githubusersandroid.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Избранный пользователь
 */
@Entity(tableName = "favorite_users")
data class FavoriteUser(
    @PrimaryKey
    val userId: Int,
    val login: String,
    val avatarURL: String,
    val name: String?,
    val createdAt: Long = System.currentTimeMillis()
)

