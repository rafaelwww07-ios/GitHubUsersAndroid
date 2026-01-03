package com.rafaelmukhametov.githubusersandroid.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Модель пользователя GitHub
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: Int,
    val login: String,
    @SerializedName("avatar_url")
    val avatarURL: String,
    val name: String?,
    val company: String?,
    val location: String?,
    val bio: String?,
    @SerializedName("public_repos")
    val publicRepos: Int,
    val followers: Int,
    val following: Int,
    @SerializedName("html_url")
    val htmlURL: String,
    val blog: String?,
    @SerializedName("created_at")
    val createdAt: String
)

/**
 * Упрощённая модель пользователя из результатов поиска
 */
data class SearchUser(
    val id: Int,
    val login: String,
    @SerializedName("avatar_url")
    val avatarURL: String,
    @SerializedName("html_url")
    val htmlURL: String
)

/**
 * Модель для поиска пользователей
 */
data class UserSearchResponse(
    @SerializedName("total_count")
    val totalCount: Int,
    @SerializedName("incomplete_results")
    val incompleteResults: Boolean,
    val items: List<SearchUser>
)

