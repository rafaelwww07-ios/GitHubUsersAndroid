package com.rafaelmukhametov.githubusersandroid.widget

import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.rafaelmukhametov.githubusersandroid.R
import com.rafaelmukhametov.githubusersandroid.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class GitHubUsersWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return GitHubUsersRemoteViewsFactory(applicationContext)
    }
}

class GitHubUsersRemoteViewsFactory(private val context: android.content.Context) : RemoteViewsService.RemoteViewsFactory {
    private var favoriteUsers: List<com.rafaelmukhametov.githubusersandroid.data.local.FavoriteUser> = emptyList()
    
    override fun onCreate() {
        // Initialize
    }
    
    override fun onDataSetChanged() {
        runBlocking {
            val database = AppDatabase.getDatabase(context)
            favoriteUsers = database.favoriteUserDao().getAllFavorites().first()
        }
    }
    
    override fun onDestroy() {
        // Cleanup
    }
    
    override fun getCount(): Int = favoriteUsers.size
    
    override fun getViewAt(position: Int): RemoteViews {
        val user = favoriteUsers[position]
        val views = RemoteViews(context.packageName, R.layout.widget_user_item)
        
        views.setTextViewText(R.id.widget_user_login, user.login)
        views.setTextViewText(R.id.widget_user_name, user.name ?: "")
        
        // Load image with Glide or Coil (simplified here)
        // In production, use RemoteViews.setImageViewUri or load bitmap
        
        // Set click intent to open user detail
        val fillInIntent = Intent().apply {
            putExtra("username", user.login)
            putExtra("navigate_to", "user_detail")
        }
        views.setOnClickFillInIntent(R.id.widget_user_item, fillInIntent)
        
        return views
    }
    
    override fun getLoadingView(): RemoteViews? = null
    
    override fun getViewTypeCount(): Int = 1
    
    override fun getItemId(position: Int): Long = favoriteUsers[position].userId.toLong()
    
    override fun hasStableIds(): Boolean = true
}

