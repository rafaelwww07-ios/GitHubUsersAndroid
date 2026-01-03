package com.rafaelmukhametov.githubusersandroid.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.rafaelmukhametov.githubusersandroid.R
import com.rafaelmukhametov.githubusersandroid.ui.component.*
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.FavoriteRepositoriesViewModel
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.LoadingState
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.RepositorySearchViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositorySearchScreen(
    onRepositoryClick: (String, String) -> Unit,
    viewModel: RepositorySearchViewModel = hiltViewModel(),
    favoriteRepositoriesViewModel: FavoriteRepositoriesViewModel = hiltViewModel()
) {
    val repositories by viewModel.repositories.collectAsStateWithLifecycle()
    val loadingState by viewModel.loadingState.collectAsStateWithLifecycle()
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val hasMorePages by viewModel.hasMorePages.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    var showSortDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_repositories)) },
                actions = {
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Sort")
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
            SearchBar(
                text = searchText,
                onTextChange = viewModel::updateSearchText,
                modifier = Modifier.padding(16.dp)
            )
            
            PullToRefreshBox(
                isRefreshing = false,
                onRefresh = { viewModel.refresh() }
            ) {
                when (loadingState) {
                    is LoadingState.Idle -> {
                        EmptyStateView(
                            icon = Icons.Default.Search,
                            title = stringResource(R.string.search_start),
                            subtitle = stringResource(R.string.search_enter_username)
                        )
                    }
                    is LoadingState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is LoadingState.Loaded -> {
                        if (repositories.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Default.Info,
                                title = stringResource(R.string.repo_no_repositories),
                                subtitle = stringResource(R.string.search_try_different)
                            )
                        } else {
                            RepositoriesList(
                                repositories = repositories,
                                onRepositoryClick = { repo ->
                                    val parts = repo.fullName.split("/")
                                    if (parts.size == 2) {
                                        onRepositoryClick(parts[0], parts[1])
                                    }
                                },
                                onLoadMore = {
                                    if (!isLoadingMore && hasMorePages) {
                                        viewModel.loadNextPage()
                                    }
                                },
                                isLoadingMore = isLoadingMore,
                                hasMorePages = hasMorePages,
                                favoriteRepositoriesViewModel = favoriteRepositoriesViewModel
                            )
                        }
                    }
                    is LoadingState.Error -> {
                        ErrorView(
                            message = (loadingState as LoadingState.Error).message,
                            onRetry = { viewModel.performSearch(searchText) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RepositoriesList(
    repositories: List<com.rafaelmukhametov.githubusersandroid.data.model.Repository>,
    onRepositoryClick: (com.rafaelmukhametov.githubusersandroid.data.model.Repository) -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean,
    hasMorePages: Boolean,
    favoriteRepositoriesViewModel: FavoriteRepositoriesViewModel
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(repositories.size) { index ->
            val repo = repositories[index]
            var isFavorite by remember { mutableStateOf(false) }
            
            LaunchedEffect(repo.id) {
                isFavorite = favoriteRepositoriesViewModel.isFavorite(repo.id)
            }
            
            RepositoryRow(
                repository = repo,
                onClick = { onRepositoryClick(repo) },
                isFavorite = isFavorite,
                onFavoriteToggle = {
                    if (isFavorite) {
                        scope.launch {
                            favoriteRepositoriesViewModel.removeFavorite(
                                com.rafaelmukhametov.githubusersandroid.data.local.FavoriteRepository(
                                    repositoryId = repo.id,
                                    name = repo.name,
                                    fullName = repo.fullName,
                                    description = repo.description,
                                    language = repo.language,
                                    stars = repo.stars,
                                    forks = repo.forks,
                                    htmlURL = repo.htmlURL
                                )
                            )
                        }
                    } else {
                        scope.launch {
                            favoriteRepositoriesViewModel.addFavorite(repo)
                        }
                    }
                    isFavorite = !isFavorite
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            if (index >= repositories.size - 3 && hasMorePages && !isLoadingMore) {
                onLoadMore()
            }
        }
        
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        if (!hasMorePages && repositories.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.state_no_more_repos),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

