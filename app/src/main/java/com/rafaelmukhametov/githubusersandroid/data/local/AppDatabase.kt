package com.rafaelmukhametov.githubusersandroid.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rafaelmukhametov.githubusersandroid.data.model.Repository
import com.rafaelmukhametov.githubusersandroid.data.model.User

@Database(
    entities = [
        User::class,
        Repository::class,
        FavoriteUser::class,
        FavoriteRepository::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun repositoryDao(): RepositoryDao
    abstract fun favoriteUserDao(): FavoriteUserDao
    abstract fun favoriteRepositoryDao(): FavoriteRepositoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "github_users_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

