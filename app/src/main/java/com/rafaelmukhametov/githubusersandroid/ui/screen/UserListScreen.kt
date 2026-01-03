package com.rafaelmukhametov.githubusersandroid.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.rafaelmukhametov.githubusersandroid.R
import com.rafaelmukhametov.githubusersandroid.data.model.User
import com.rafaelmukhametov.githubusersandroid.ui.component.*
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.LoadingState
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.UserListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    onUserClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: UserListViewModel = hiltViewModel()
) {
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val loadingState by viewModel.loadingState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val showHistory by viewModel.showHistory.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val hasMorePages by viewModel.hasMorePages.collectAsStateWithLifecycle()
    
    val listState = rememberLazyListState()
    
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_users)) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.accessibility_settings_button)
                        )
                    }
                    IconButton(onClick = onFavoritesClick) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = stringResource(R.string.accessibility_favorites_button),
                            tint = MaterialTheme.colorScheme.error
                        )
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
            // Network Status
            NetworkStatusView()
            
            // Search Bar
            SearchBar(
                text = searchText,
                onTextChange = viewModel::updateSearchText,
                modifier = Modifier.padding(16.dp)
            )
            
            // Search History
            if (showHistory && searchHistory.isNotEmpty()) {
                SearchHistoryView(
                    history = searchHistory,
                    onSelect = viewModel::selectHistoryItem,
                    onRemove = viewModel::removeHistoryItem,
                    onClear = viewModel::clearHistory,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Content
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() }
            ) {
                when (val state = loadingState) {
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
                        if (state.users.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Default.Search,
                                title = stringResource(R.string.search_no_results),
                                subtitle = stringResource(R.string.search_try_different)
                            )
                        } else {
                            UsersList(
                                users = state.users,
                                onUserClick = onUserClick,
                                onLoadMore = {
                                    if (!isLoadingMore && hasMorePages) {
                                        viewModel.loadNextPage()
                                    }
                                },
                                isLoadingMore = isLoadingMore,
                                hasMorePages = hasMorePages,
                                listState = listState,
                                viewModel = viewModel
                            )
                        }
                    }
                    is LoadingState.Error -> {
                        ErrorView(
                            message = state.message,
                            onRetry = { viewModel.searchUsers(searchText) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UsersList(
    users: List<User>,
    onUserClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean,
    hasMorePages: Boolean,
    listState: LazyListState,
    viewModel: UserListViewModel
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users.size) { index ->
            val user = users[index]
            var isFavorite by remember { mutableStateOf(false) }
            
            LaunchedEffect(user.id) {
                isFavorite = viewModel.isFavorite(user)
            }
            
            UserRow(
                user = user,
                isFavorite = isFavorite,
                onClick = { onUserClick(user.login) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Load more when near the end
            if (index >= users.size - 3 && hasMorePages && !isLoadingMore) {
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
        
        if (!hasMorePages && users.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.state_no_more_users),
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

