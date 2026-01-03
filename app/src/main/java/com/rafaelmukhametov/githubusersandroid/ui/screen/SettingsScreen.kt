package com.rafaelmukhametov.githubusersandroid.ui.screen

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rafaelmukhametov.githubusersandroid.R
import com.rafaelmukhametov.githubusersandroid.data.local.AppDatabase
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafaelmukhametov.githubusersandroid.util.AppTheme
import com.rafaelmukhametov.githubusersandroid.util.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val currentTheme by themeManager.currentTheme.collectAsStateWithLifecycle(initialValue = AppTheme.SYSTEM)
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var cacheCleared by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                actions = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.nav_done))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // About
            SettingsSection(title = stringResource(R.string.settings_about)) {
                SettingsItem(
                    label = stringResource(R.string.app_version),
                    value = getAppVersion(context)
                )
                SettingsItem(
                    label = stringResource(R.string.app_build),
                    value = getAppBuild(context)
                )
            }
            
            // Storage
            SettingsSection(title = stringResource(R.string.settings_storage)) {
                SettingsItem(
                    label = stringResource(R.string.settings_clear_cache),
                    icon = Icons.Default.Delete,
                    onClick = { showClearCacheDialog = true },
                    isDestructive = true
                )
                if (cacheCleared) {
                    SettingsItem(
                        label = stringResource(R.string.settings_cache_cleared),
                        icon = Icons.Default.CheckCircle,
                        isSuccess = true
                    )
                }
            }
            
            // Theme
            SettingsSection(title = stringResource(R.string.settings_theme)) {
                AppTheme.values().forEach { theme ->
                    SettingsItem(
                        label = when (theme) {
                            AppTheme.SYSTEM -> stringResource(R.string.settings_theme_system)
                            AppTheme.LIGHT -> stringResource(R.string.settings_theme_light)
                            AppTheme.DARK -> stringResource(R.string.settings_theme_dark)
                        },
                        icon = if (currentTheme == theme) Icons.Default.CheckCircle else null,
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                themeManager.setTheme(theme)
                            }
                        }
                    )
                }
            }
            
            // Links
            SettingsSection(title = "Links") {
                SettingsItem(
                    label = "GitHub",
                    icon = Icons.Default.Info,
                    onClick = {
                        // Open GitHub
                    }
                )
            }
        }
    }
    
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text(stringResource(R.string.alert_clear_cache_title)) },
            text = { Text(stringResource(R.string.alert_clear_cache_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        clearCache(context)
                        cacheCleared = true
                        showClearCacheDialog = false
                    }
                ) {
                    Text(stringResource(R.string.alert_clear_cache_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text(stringResource(R.string.alert_clear_cache_cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsItem(
    label: String,
    value: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: (() -> Unit)? = null,
    isDestructive: Boolean = false,
    isSuccess: Boolean = false
) {
    val contentColor = when {
        isDestructive -> MaterialTheme.colorScheme.error
        isSuccess -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor
                    )
                }
                Text(
                    text = label,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )
                if (value != null) {
                    Text(
                        text = value,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor
                )
            }
            Text(
                text = label,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            if (value != null) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun getAppVersion(context: Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
    } catch (e: Exception) {
        "1.0"
    }
}

private fun getAppBuild(context: Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toString()
    } catch (e: Exception) {
        "1"
    }
}

private fun clearCache(context: Context) {
    // Clear Room database cache
    // This would typically be done through a service or repository
    // For now, we'll just mark it as cleared
}

