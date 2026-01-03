package com.rafaelmukhametov.githubusersandroid.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Модель репозитория GitHub
 */
@Entity(tableName = "repositories")
data class Repository(
    @PrimaryKey
    val id: Int,
    val name: String,
    @SerializedName("full_name")
    val fullName: String,
    val description: String?,
    val language: String?,
    @SerializedName("stargazers_count")
    val stars: Int,
    val forks: Int,
    @SerializedName("html_url")
    val htmlURL: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

/**
 * Детальная модель репозитория с полной информацией
 */
data class RepositoryDetail(
    val id: Int,
    val name: String,
    @SerializedName("full_name")
    val fullName: String,
    val description: String?,
    val language: String?,
    @SerializedName("stargazers_count")
    val stars: Int,
    val forks: Int,
    val watchers: Int,
    @SerializedName("html_url")
    val htmlURL: String,
    @SerializedName("clone_url")
    val cloneURL: String,
    @SerializedName("default_branch")
    val defaultBranch: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("pushed_at")
    val pushedAt: String?,
    val homepage: String?,
    val topics: List<String>,
    val license: License?,
    val owner: RepositoryOwner,
    @SerializedName("private")
    val isPrivate: Boolean,
    val archived: Boolean,
    val fork: Boolean,
    @SerializedName("open_issues_count")
    val openIssuesCount: Int,
    val size: Int
)

/**
 * Модель лицензии репозитория
 */
data class License(
    val key: String,
    val name: String,
    @SerializedName("spdx_id")
    val spdxId: String?,
    val url: String?
)

/**
 * Модель владельца репозитория
 */
data class RepositoryOwner(
    val login: String,
    @SerializedName("avatar_url")
    val avatarURL: String,
    @SerializedName("html_url")
    val htmlURL: String
)

/**
 * Модель ответа поиска репозиториев
 */
data class RepositorySearchResponse(
    @SerializedName("total_count")
    val totalCount: Int,
    @SerializedName("incomplete_results")
    val incompleteResults: Boolean,
    val items: List<Repository>
)

