package com.rafaelmukhametov.githubusersandroid.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")
private val THEME_KEY = stringPreferencesKey("selected_theme")

enum class AppTheme(val value: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark")
}

class ThemeManager(private val context: Context) {
    val currentTheme: Flow<AppTheme> = context.themeDataStore.data.map { preferences ->
        val themeValue = preferences[THEME_KEY] ?: AppTheme.SYSTEM.value
        AppTheme.values().find { it.value == themeValue } ?: AppTheme.SYSTEM
    }
    
    suspend fun setTheme(theme: AppTheme) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.value
        }
    }
}

