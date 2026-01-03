package com.rafaelmukhametov.githubusersandroid.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafaelmukhametov.githubusersandroid.R
import com.rafaelmukhametov.githubusersandroid.ui.component.ErrorView
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.FavoriteRepositoriesViewModel
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.LoadingState
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.RepositoryDetailViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryDetailScreen(
    viewModel: RepositoryDetailViewModel = hiltViewModel(),
    favoriteRepositoriesViewModel: FavoriteRepositoriesViewModel = hiltViewModel()
) {
    val repository by viewModel.repository.collectAsStateWithLifecycle()
    val loadingState by viewModel.loadingState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isFavorite by remember { mutableStateOf(false) }
    
    LaunchedEffect(repository?.id) {
        repository?.id?.let { id ->
            isFavorite = favoriteRepositoriesViewModel.isFavorite(id)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(repository?.name ?: "") },
                actions = {
                    if (repository != null) {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, repository!!.htmlURL)
                            }
                            context.startActivity(Intent.createChooser(intent, null))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_share))
                        }
                        
                        IconButton(onClick = {
                            repository?.let { repo ->
                                val repoModel = com.rafaelmukhametov.githubusersandroid.data.model.Repository(
                                    id = repo.id,
                                    name = repo.name,
                                    fullName = repo.fullName,
                                    description = repo.description,
                                    language = repo.language,
                                    stars = repo.stars,
                                    forks = repo.forks,
                                    htmlURL = repo.htmlURL,
                                    updatedAt = repo.updatedAt
                                )
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
                                        favoriteRepositoriesViewModel.addFavorite(repoModel)
                                    }
                                }
                                isFavorite = !isFavorite
                            }
                        }) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (loadingState) {
            is LoadingState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is LoadingState.Loaded -> {
                if (repository != null) {
                    RepositoryContent(
                        repository = repository!!,
                        viewModel = viewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            is LoadingState.Error -> {
                ErrorView(
                    message = (loadingState as LoadingState.Error).message,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is LoadingState.Idle -> {}
        }
    }
}

@Composable
private fun RepositoryContent(
    repository: com.rafaelmukhametov.githubusersandroid.data.model.RepositoryDetail,
    viewModel: RepositoryDetailViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repository.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = repository.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        // Description
        if (repository.description != null) {
            Text(
                text = repository.description!!,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = repository.stars.toString(),
                label = stringResource(R.string.repo_stars),
                icon = Icons.Default.Star
            )
            StatItem(
                value = repository.forks.toString(),
                label = stringResource(R.string.repo_forks),
                icon = Icons.Default.Share
            )
            StatItem(
                value = repository.watchers.toString(),
                label = stringResource(R.string.repo_watchers),
                icon = Icons.Default.Info
            )
            StatItem(
                value = repository.openIssuesCount.toString(),
                label = stringResource(R.string.repo_issues),
                icon = Icons.Default.Warning
            )
        }
        
        Divider()
        
        // Info
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (repository.language != null) {
                InfoRow(Icons.Default.Info, repository.language!!)
            }
            if (repository.license != null) {
                InfoRow(Icons.Default.Info, repository.license!!.name)
            }
            if (repository.homepage != null && repository.homepage!!.isNotEmpty()) {
                InfoRow(Icons.Default.Home, repository.homepage!!)
            }
            InfoRow(Icons.Default.Info, viewModel.formatSize(repository.size))
            InfoRow(Icons.Default.Share, repository.defaultBranch)
            
            if (repository.isPrivate) {
                InfoRow(Icons.Default.Lock, stringResource(R.string.repo_detail_private))
            }
            if (repository.archived) {
                InfoRow(Icons.Default.Info, stringResource(R.string.repo_detail_archived))
            }
            if (repository.fork) {
                InfoRow(Icons.Default.Share, stringResource(R.string.repo_detail_fork))
            }
        }
        
        // Dates
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.repo_detail_dates),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            DateRow(stringResource(R.string.repo_detail_created), viewModel.formatDate(repository.createdAt))
            DateRow(stringResource(R.string.repo_detail_updated), viewModel.formatDate(repository.updatedAt))
            if (repository.pushedAt != null) {
                DateRow(stringResource(R.string.repo_detail_pushed), viewModel.formatDate(repository.pushedAt!!))
            }
        }
        
        // Topics
        if (repository.topics.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.repo_detail_topics),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repository.topics.forEach { topic ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = topic,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
        
        // Actions
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repository.htmlURL))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.repo_open_safari))
            }
            
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, repository.cloneURL)
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.repo_share_clone))
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DateRow(label: String, date: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

