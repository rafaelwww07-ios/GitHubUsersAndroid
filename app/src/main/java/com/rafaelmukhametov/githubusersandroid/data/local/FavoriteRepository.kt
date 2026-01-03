package com.rafaelmukhametov.githubusersandroid.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Избранный репозиторий
 */
@Entity(tableName = "favorite_repositories")
data class FavoriteRepository(
    @PrimaryKey
    val repositoryId: Int,
    val name: String,
    val fullName: String,
    val description: String?,
    val language: String?,
    val stars: Int,
    val forks: Int,
    val htmlURL: String,
    val createdAt: Long = System.currentTimeMillis()
)

