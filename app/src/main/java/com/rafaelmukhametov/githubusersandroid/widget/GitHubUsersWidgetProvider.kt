package com.rafaelmukhametov.githubusersandroid.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.TaskStackBuilder
import com.rafaelmukhametov.githubusersandroid.MainActivity
import com.rafaelmukhametov.githubusersandroid.R
import com.rafaelmukhametov.githubusersandroid.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GitHubUsersWidgetProvider : AppWidgetProvider() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onEnabled(context: Context) {
        // Widget enabled
    }
    
    override fun onDisabled(context: Context) {
        // Widget disabled
    }
    
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        scope.launch {
            val database = AppDatabase.getDatabase(context)
            val favoriteUsers = database.favoriteUserDao().getAllFavorites().first()
            
            val views = RemoteViews(context.packageName, R.layout.widget_github_users)
            
            if (favoriteUsers.isEmpty()) {
                views.setTextViewText(R.id.widget_title, context.getString(R.string.favorites_title))
                views.setTextViewText(R.id.widget_empty_text, context.getString(R.string.favorites_empty))
                views.setViewVisibility(R.id.widget_empty_text, android.view.View.VISIBLE)
                views.setViewVisibility(R.id.widget_list, android.view.View.GONE)
            } else {
                views.setTextViewText(R.id.widget_title, context.getString(R.string.favorites_title))
                views.setViewVisibility(R.id.widget_empty_text, android.view.View.GONE)
                views.setViewVisibility(R.id.widget_list, android.view.View.VISIBLE)
                
                // Set up RemoteViewsService for list
                val serviceIntent = Intent(context, GitHubUsersWidgetService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                views.setRemoteAdapter(R.id.widget_list, serviceIntent)
                
                // Set click template
                val clickIntentTemplate = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                }
                val clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate)
                
                // Set empty view
                views.setEmptyView(R.id.widget_list, R.id.widget_empty_text)
            }
            
            // Set click intent for title
            val titleIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("navigate_to", "favorites")
            }
            val titlePendingIntent = PendingIntent.getActivity(
                context,
                0,
                titleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, titlePendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
    
    companion object {
        fun updateWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, GitHubUsersWidgetProvider::class.java)
            )
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list)
        }
    }
}

