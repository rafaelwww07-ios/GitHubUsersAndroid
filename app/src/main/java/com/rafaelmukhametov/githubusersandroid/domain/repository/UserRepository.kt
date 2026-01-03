package com.rafaelmukhametov.githubusersandroid.domain.repository

import com.rafaelmukhametov.githubusersandroid.data.model.User

interface UserRepository {
    suspend fun searchUsers(query: String, page: Int): List<User>
    suspend fun getUser(username: String): User
}

