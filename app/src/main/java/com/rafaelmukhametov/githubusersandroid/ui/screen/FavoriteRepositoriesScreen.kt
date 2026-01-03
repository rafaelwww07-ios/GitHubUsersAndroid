package com.rafaelmukhametov.githubusersandroid.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafaelmukhametov.githubusersandroid.R
import com.rafaelmukhametov.githubusersandroid.data.local.FavoriteRepository
import com.rafaelmukhametov.githubusersandroid.ui.component.EmptyStateView
import com.rafaelmukhametov.githubusersandroid.ui.component.RepositoryRow
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.FavoriteRepositoriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteRepositoriesScreen(
    onRepositoryClick: (String, String) -> Unit,
    viewModel: FavoriteRepositoriesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites_title)) }
            )
        }
    ) { paddingValues ->
        if (favorites.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Favorite,
                title = stringResource(R.string.favorites_empty),
                subtitle = stringResource(R.string.favorites_add_hint),
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites) { favorite ->
                    val repository = com.rafaelmukhametov.githubusersandroid.data.model.Repository(
                        id = favorite.repositoryId,
                        name = favorite.name,
                        fullName = favorite.fullName,
                        description = favorite.description,
                        language = favorite.language,
                        stars = favorite.stars,
                        forks = favorite.forks,
                        htmlURL = favorite.htmlURL,
                        updatedAt = ""
                    )
                    
                    RepositoryRow(
                        repository = repository,
                        onClick = {
                            val parts = favorite.fullName.split("/")
                            if (parts.size == 2) {
                                onRepositoryClick(parts[0], parts[1])
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

