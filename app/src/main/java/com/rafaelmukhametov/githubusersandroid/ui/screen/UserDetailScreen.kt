package com.rafaelmukhametov.githubusersandroid.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.rafaelmukhametov.githubusersandroid.R
import com.rafaelmukhametov.githubusersandroid.ui.component.ErrorView
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.LoadingState
import com.rafaelmukhametov.githubusersandroid.ui.viewmodel.UserDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    onNavigateToRepositories: (String) -> Unit,
    viewModel: UserDetailViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val loadingState by viewModel.loadingState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.login ?: "") },
                actions = {
                    if (user != null) {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, user!!.htmlURL)
                            }
                            context.startActivity(Intent.createChooser(intent, null))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_share))
                        }
                        
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = stringResource(
                                    if (isFavorite) R.string.accessibility_remove_favorite_button
                                    else R.string.accessibility_favorite_button
                                ),
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
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is LoadingState.Loaded -> {
                if (user != null) {
                    UserContent(
                        user = user!!,
                        isFavorite = isFavorite,
                        onToggleFavorite = { viewModel.toggleFavorite() },
                        onOpenProfile = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(user!!.htmlURL))
                            context.startActivity(intent)
                        },
                        onNavigateToRepositories = { onNavigateToRepositories(user!!.login) },
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
private fun UserContent(
    user: com.rafaelmukhametov.githubusersandroid.data.model.User,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenProfile: () -> Unit,
    onNavigateToRepositories: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Avatar
        AsyncImage(
            model = user.avatarURL,
            contentDescription = user.login,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )
        
        // Name and login
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = user.login,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (user.name != null) {
                Text(
                    text = user.name!!,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                value = user.publicRepos.toString(),
                label = stringResource(R.string.user_repos),
                icon = Icons.Default.Info,
                onClick = onNavigateToRepositories
            )
            StatCard(
                value = user.followers.toString(),
                label = stringResource(R.string.user_followers),
                icon = Icons.Default.Person
            )
            StatCard(
                value = user.following.toString(),
                label = stringResource(R.string.user_following),
                icon = Icons.Default.Person
            )
        }
        
        Divider()
        
        // Chart
        if (user.publicRepos > 0 || user.followers > 0 || user.following > 0) {
            UserStatsChartView(user = user)
        }
        
        Divider()
        
        // Additional info
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (user.company != null) {
                InfoRow(icon = Icons.Default.Info, text = user.company!!)
            }
            if (user.location != null) {
                InfoRow(icon = Icons.Default.LocationOn, text = user.location!!)
            }
            if (user.bio != null) {
                InfoRow(icon = Icons.Default.Info, text = user.bio!!)
            }
            if (user.blog != null && user.blog!!.isNotEmpty()) {
                InfoRow(icon = Icons.Default.Share, text = user.blog!!)
            }
        }
        
        // Open profile button
        Button(
            onClick = onOpenProfile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.user_open_profile))
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null
) {
    val content = @Composable {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
    
    if (onClick != null) {
        Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            content()
        }
    } else {
        Card(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
private fun UserStatsChartView(
    user: com.rafaelmukhametov.githubusersandroid.data.model.User
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Simple bar chart using basic composables
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val maxValue = maxOf(user.publicRepos, user.followers, user.following, 1)
            
            BarItem(
                value = user.publicRepos,
                maxValue = maxValue,
                label = stringResource(R.string.user_repos),
                color = Color.Blue
            )
            BarItem(
                value = user.followers,
                maxValue = maxValue,
                label = stringResource(R.string.user_followers),
                color = Color.Green
            )
            BarItem(
                value = user.following,
                maxValue = maxValue,
                label = stringResource(R.string.user_following),
                color = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
private fun BarItem(
    value: Int,
    maxValue: Int,
    label: String,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        val heightRatio = if (maxValue > 0) value.toFloat() / maxValue.toFloat() else 0f
        
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .fillMaxHeight(heightRatio)
                .clip(MaterialTheme.shapes.medium)
                .background(color.copy(alpha = 0.7f))
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

