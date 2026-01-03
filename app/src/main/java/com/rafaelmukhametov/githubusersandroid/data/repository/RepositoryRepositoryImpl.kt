package com.rafaelmukhametov.githubusersandroid.data.repository

import com.rafaelmukhametov.githubusersandroid.data.local.RepositoryDao
import com.rafaelmukhametov.githubusersandroid.data.model.Repository
import com.rafaelmukhametov.githubusersandroid.data.model.RepositoryDetail
import com.rafaelmukhametov.githubusersandroid.data.model.RepositoryOrder
import com.rafaelmukhametov.githubusersandroid.data.model.RepositorySort
import com.rafaelmukhametov.githubusersandroid.data.remote.GitHubApi
import com.rafaelmukhametov.githubusersandroid.domain.repository.RepositoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryRepositoryImpl @Inject constructor(
    private val api: GitHubApi,
    private val repositoryDao: RepositoryDao
) : RepositoryRepository {
    
    override suspend fun getRepositories(
        username: String,
        sort: RepositorySort?,
        order: RepositoryOrder?,
        page: Int
    ): List<Repository> {
        val repositories = api.getRepositories(
            username = username,
            sort = sort?.value,
            order = order?.value,
            perPage = 30,
            page = page
        )
        
        // Кэшируем только первую страницу
        if (page == 1) {
            repositoryDao.insertRepositories(repositories)
        }
        
        return repositories
    }
    
    override suspend fun getRepository(owner: String, repo: String): RepositoryDetail {
        return api.getRepository(owner, repo)
    }
    
    override suspend fun searchRepositories(
        query: String,
        sort: RepositorySort?,
        order: RepositoryOrder?,
        page: Int
    ): Triple<List<Repository>, Int, Boolean> {
        val response = api.searchRepositories(
            query = query,
            sort = sort?.value,
            order = order?.value,
            perPage = 30,
            page = page
        )
        
        val hasMore = (page * 30) < response.totalCount
        return Triple(response.items, response.totalCount, hasMore)
    }
}

