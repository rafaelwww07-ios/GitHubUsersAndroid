package com.rafaelmukhametov.githubusersandroid.data.repository

import com.rafaelmukhametov.githubusersandroid.data.local.UserDao
import com.rafaelmukhametov.githubusersandroid.data.model.SearchUser
import com.rafaelmukhametov.githubusersandroid.data.model.User
import com.rafaelmukhametov.githubusersandroid.data.remote.GitHubApi
import com.rafaelmukhametov.githubusersandroid.domain.repository.UserRepository
import com.rafaelmukhametov.githubusersandroid.util.PerformanceMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: GitHubApi,
    private val userDao: UserDao
) : UserRepository {
    
    override suspend fun searchUsers(query: String, page: Int): List<User> {
        return PerformanceMonitor.measure("SearchUsers: $query, page: $page") {
            val cleanQuery = query.trimming()
            if (cleanQuery.isEmpty()) {
                return@measure emptyList()
            }
            
            // Проверяем, является ли запрос валидным именем пользователя
            if (isValidGitHubUsername(cleanQuery) && page == 1) {
                try {
                    val directUser = getUser(cleanQuery)
                    return@measure listOf(directUser)
                } catch (e: Exception) {
                    // Fallback to search
                }
            }
            
            val response = api.searchUsers(
                query = "$cleanQuery type:user",
                perPage = 30,
                page = page
            )
            
            val users = response.items.map { searchUser ->
                // Пытаемся получить полную информацию из кэша
                val cachedUser = userDao.getUserById(searchUser.id)
                if (cachedUser != null) {
                    cachedUser
                } else {
                    // Создаём упрощённую версию
                    User(
                        id = searchUser.id,
                        login = searchUser.login,
                        avatarURL = searchUser.avatarURL,
                        name = null,
                        company = null,
                        location = null,
                        bio = null,
                        publicRepos = 0,
                        followers = 0,
                        following = 0,
                        htmlURL = searchUser.htmlURL,
                        blog = null,
                        createdAt = ""
                    )
                }
            }
            
            // Кэшируем пользователей
            if (page == 1) {
                userDao.insertUsers(users)
            }
            
            users
        }
    }
    
    override suspend fun getUser(username: String): User {
        return PerformanceMonitor.measure("GetUser: $username") {
            val cleanUsername = username.trimming()
            if (cleanUsername.isEmpty()) {
                throw IllegalArgumentException("Username cannot be empty")
            }
            
            // Проверяем кэш
            val cachedUser = userDao.getUserByLogin(cleanUsername)
            if (cachedUser != null) {
                // Обновляем в фоне
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val freshUser = api.getUser(cleanUsername)
                        userDao.insertUser(freshUser)
                    } catch (e: Exception) {
                        // Ignore background update errors
                    }
                }
                return@measure cachedUser
            }
            
            val user = api.getUser(cleanUsername)
            userDao.insertUser(user)
            user
        }
    }
    
    private fun isValidGitHubUsername(username: String): Boolean {
        if (username.length > 39 || username.isEmpty() || 
            username.startsWith("-") || username.endsWith("-")) {
            return false
        }
        return username.all { it.isLetterOrDigit() || it == '-' }
    }
    
    private fun String.trimming() = trim()
}
