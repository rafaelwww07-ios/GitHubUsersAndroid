package com.rafaelmukhametov.githubusersandroid.domain.repository

import com.rafaelmukhametov.githubusersandroid.data.model.Repository
import com.rafaelmukhametov.githubusersandroid.data.model.RepositoryDetail
import com.rafaelmukhametov.githubusersandroid.data.model.RepositoryOrder
import com.rafaelmukhametov.githubusersandroid.data.model.RepositorySort

interface RepositoryRepository {
    suspend fun getRepositories(
        username: String,
        sort: RepositorySort?,
        order: RepositoryOrder?,
        page: Int
    ): List<Repository>
    
    suspend fun getRepository(owner: String, repo: String): RepositoryDetail
    
    suspend fun searchRepositories(
        query: String,
        sort: RepositorySort?,
        order: RepositoryOrder?,
        page: Int
    ): Triple<List<Repository>, Int, Boolean> // repositories, totalCount, hasMore
}

