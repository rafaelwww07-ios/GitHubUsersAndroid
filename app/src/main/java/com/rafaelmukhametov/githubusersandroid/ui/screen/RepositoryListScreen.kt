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
import com.rafaelmukhametov.githubusersandroid.data.model.RepositoryOrder
import com.rafaelmukhametov.githubusersandroid.data.model.RepositorySort
import com.rafaelmukhametov.githubusersandroid.ui.component.*
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.FavoriteRepositoriesViewModel
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.LoadingState
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.RepositoryListViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryListScreen(
    onRepositoryClick: (String, String) -> Unit,
    viewModel: RepositoryListViewModel = hiltViewModel(),
    favoriteRepositoriesViewModel: FavoriteRepositoriesViewModel = hiltViewModel()
) {
    val repositories by viewModel.filteredRepositories.collectAsStateWithLifecycle()
    val loadingState by viewModel.loadingState.collectAsStateWithLifecycle()
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()
    val selectedOrder by viewModel.selectedOrder.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val availableLanguages by viewModel.availableLanguages.collectAsStateWithLifecycle()
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
            // Search and filters
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchBar(
                    text = searchText,
                    onTextChange = viewModel::updateSearchText
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (availableLanguages.isNotEmpty()) {
                        FilterChip(
                            selected = selectedLanguage != null,
                            onClick = { /* Show language menu */ },
                            label = { Text(selectedLanguage ?: stringResource(R.string.filter_all_languages)) }
                        )
                    }
                    
                    Text(
                        text = "${repositories.size} ${stringResource(R.string.repo_repositories)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            
            // Content
            PullToRefreshBox(
                isRefreshing = false,
                onRefresh = { viewModel.refresh() }
            ) {
                when (loadingState) {
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
                                subtitle = stringResource(R.string.repo_try_filters)
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
                            onRetry = { viewModel.loadRepositories() }
                        )
                    }
                    is LoadingState.Idle -> {}
                }
            }
        }
    }
    
    if (showSortDialog) {
        SortOptionsDialog(
            selectedSort = selectedSort,
            selectedOrder = selectedOrder,
            onDismiss = { showSortDialog = false },
            onSortSelected = { sort ->
                viewModel.changeSort(sort)
                showSortDialog = false
            },
            onOrderSelected = { order ->
                viewModel.changeOrder(order)
                showSortDialog = false
            }
        )
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

@Composable
private fun SortOptionsDialog(
    selectedSort: RepositorySort?,
    selectedOrder: RepositoryOrder,
    onDismiss: () -> Unit,
    onSortSelected: (RepositorySort?) -> Unit,
    onOrderSelected: (RepositoryOrder) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sort_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.sort_by),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                RepositorySort.values().forEach { sort ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(sort.value)
                        if (selectedSort == sort) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.sort_order),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                RepositoryOrder.values().forEach { order ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(order.value)
                        if (selectedOrder == order) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.nav_done))
            }
        }
    )
}

