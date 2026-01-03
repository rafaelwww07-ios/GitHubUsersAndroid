package com.rafaelmukhametov.githubusersandroid.di

import android.content.Context
import com.rafaelmukhametov.githubusersandroid.data.repository.FavoriteRepositoriesServiceImpl
import com.rafaelmukhametov.githubusersandroid.data.repository.FavoritesServiceImpl
import com.rafaelmukhametov.githubusersandroid.data.repository.RepositoryRepositoryImpl
import com.rafaelmukhametov.githubusersandroid.data.repository.SearchHistoryServiceImpl
import com.rafaelmukhametov.githubusersandroid.data.repository.UserRepositoryImpl
import com.rafaelmukhametov.githubusersandroid.domain.repository.FavoriteRepositoriesService
import com.rafaelmukhametov.githubusersandroid.domain.repository.FavoritesService
import com.rafaelmukhametov.githubusersandroid.domain.repository.RepositoryRepository
import com.rafaelmukhametov.githubusersandroid.domain.repository.SearchHistoryService
import com.rafaelmukhametov.githubusersandroid.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindRepositoryRepository(impl: RepositoryRepositoryImpl): RepositoryRepository
    
    // FavoritesService and SearchHistoryService need Context, so we provide them directly
    companion object {
        @Provides
        @Singleton
        fun provideFavoritesService(
            favoriteUserDao: com.rafaelmukhametov.githubusersandroid.data.local.FavoriteUserDao,
            @ApplicationContext context: Context
        ): FavoritesService {
            return FavoritesServiceImpl(favoriteUserDao, context)
        }
        
        @Provides
        @Singleton
        fun provideSearchHistoryService(
            @ApplicationContext context: Context
        ): SearchHistoryService {
            return SearchHistoryServiceImpl(context)
        }
    }
    
    @Binds
    @Singleton
    abstract fun bindFavoriteRepositoriesService(impl: FavoriteRepositoriesServiceImpl): FavoriteRepositoriesService
}

