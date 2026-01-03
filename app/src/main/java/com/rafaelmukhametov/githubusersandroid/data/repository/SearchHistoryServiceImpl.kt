package com.rafaelmukhametov.githubusersandroid.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rafaelmukhametov.githubusersandroid.domain.repository.SearchHistoryService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "search_history")
private val HISTORY_KEY = stringSetPreferencesKey("history")
private const val MAX_HISTORY_COUNT = 20

@Singleton
class SearchHistoryServiceImpl @Inject constructor(
    private val context: Context
) : SearchHistoryService {
    
    override val history: Flow<List<String>> = context.dataStore.data.map { preferences ->
        preferences[HISTORY_KEY]?.toList() ?: emptyList()
    }
    
    override suspend fun addToHistory(query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return
        
        context.dataStore.edit { preferences ->
            val currentHistory = (preferences[HISTORY_KEY] ?: emptySet()).toMutableList()
            
            // Удаляем дубликаты (case-insensitive)
            currentHistory.removeAll { it.equals(cleanQuery, ignoreCase = true) }
            
            // Добавляем в начало
            currentHistory.add(0, cleanQuery)
            
            // Ограничиваем количество
            val limitedHistory = currentHistory.take(MAX_HISTORY_COUNT)
            
            preferences[HISTORY_KEY] = limitedHistory.toSet()
        }
    }
    
    override suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(HISTORY_KEY)
        }
    }
    
    override suspend fun removeFromHistory(query: String) {
        context.dataStore.edit { preferences ->
            val currentHistory = (preferences[HISTORY_KEY] ?: emptySet()).toMutableList()
            currentHistory.removeAll { it.equals(query, ignoreCase = true) }
            preferences[HISTORY_KEY] = currentHistory.toSet()
        }
    }
}

