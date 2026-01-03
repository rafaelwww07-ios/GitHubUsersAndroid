package com.rafaelmukhametov.githubusersandroid.domain.repository

import kotlinx.coroutines.flow.Flow

interface SearchHistoryService {
    val history: Flow<List<String>>
    suspend fun addToHistory(query: String)
    suspend fun clearHistory()
    suspend fun removeFromHistory(query: String)
}

