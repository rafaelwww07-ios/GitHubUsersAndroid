package com.rafaelmukhametov.githubusersandroid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rafaelmukhametov.githubusersandroid.ui.screen.*
import com.rafaelmukhametov.githubusersandroid.ui.theme.GitHubUsersAndroidTheme
import com.rafaelmukhametov.githubusersandroid.util.AppTheme
import com.rafaelmukhametov.githubusersandroid.util.DeepLinkManager
import com.rafaelmukhametov.githubusersandroid.util.DeepLinkType
import com.rafaelmukhametov.githubusersandroid.util.ThemeManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppContent()
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
private fun AppContent() {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val currentTheme by themeManager.currentTheme.collectAsState(initial = AppTheme.SYSTEM)
    val darkTheme = when (currentTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }
    
    GitHubUsersAndroidTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            
            // Handle deep links and widget clicks from intent
            val activity = LocalContext.current as? ComponentActivity
            LaunchedEffect(Unit) {
                activity?.intent?.let { intent ->
                    // Check for widget navigation
                    val navigateTo = intent.getStringExtra("navigate_to")
                    if (navigateTo != null) {
                        when (navigateTo) {
                            "favorites" -> navController.navigate("favorites")
                            "user_detail" -> {
                                val username = intent.getStringExtra("username")
                                if (username != null) {
                                    navController.navigate("user_detail/$username")
                                }
                            }
                        }
                    } else {
                        handleDeepLink(intent, navController)
                    }
                }
            }
            
            NavHost(
                navController = navController,
                startDestination = "user_list"
            ) {
                composable("user_list") {
                    UserListScreen(
                        onUserClick = { username ->
                            navController.navigate("user_detail/$username")
                        },
                        onFavoritesClick = {
                            navController.navigate("favorites")
                        },
                        onSettingsClick = {
                            navController.navigate("settings")
                        }
                    )
                }
                
                composable(
                    route = "user_detail/{username}",
                    arguments = listOf(
                        navArgument("username") {
                            type = androidx.navigation.NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val username = backStackEntry.arguments?.getString("username") ?: ""
                    UserDetailScreen(
                        onNavigateToRepositories = { user ->
                            navController.navigate("repositories/$user")
                        }
                    )
                }
                
                composable(
                    route = "repositories/{username}",
                    arguments = listOf(
                        navArgument("username") {
                            type = androidx.navigation.NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val username = backStackEntry.arguments?.getString("username") ?: ""
                    RepositoryListScreen(
                        onRepositoryClick = { owner, repo ->
                            navController.navigate("repository_detail/$owner/$repo")
                        }
                    )
                }
                
                composable(
                    route = "repository_detail/{owner}/{repo}",
                    arguments = listOf(
                        navArgument("owner") {
                            type = androidx.navigation.NavType.StringType
                        },
                        navArgument("repo") {
                            type = androidx.navigation.NavType.StringType
                        }
                    )
                ) {
                    RepositoryDetailScreen()
                }
                
                composable("repository_search") {
                    RepositorySearchScreen(
                        onRepositoryClick = { owner, repo ->
                            navController.navigate("repository_detail/$owner/$repo")
                        }
                    )
                }
                
                composable("favorites") {
                    FavoritesScreen(
                        onUserClick = { username ->
                            navController.navigate("user_detail/$username")
                        },
                        onRepositoriesClick = {
                            navController.navigate("favorite_repositories")
                        }
                    )
                }
                
                composable("favorite_repositories") {
                    FavoriteRepositoriesScreen(
                        onRepositoryClick = { owner, repo ->
                            navController.navigate("repository_detail/$owner/$repo")
                        }
                    )
                }
                
                composable("settings") {
                    SettingsScreen(
                        onDismiss = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

private fun handleDeepLink(intent: Intent, navController: NavController) {
    intent.data?.let { uri ->
        DeepLinkManager.handleURL(uri)?.let { link ->
            handleDeepLinkType(link, navController)
        }
    }
}

private fun handleDeepLinkType(link: DeepLinkType, navController: NavController) {
    when (link) {
        is DeepLinkType.User -> {
            navController.navigate("user_detail/${link.username}") {
                popUpTo("user_list") { inclusive = false }
            }
        }
        is DeepLinkType.Repository -> {
            navController.navigate("repository_detail/${link.owner}/${link.repo}") {
                popUpTo("user_list") { inclusive = false }
            }
        }
        is DeepLinkType.Favorites -> {
            navController.navigate("favorites") {
                popUpTo("user_list") { inclusive = false }
            }
        }
        is DeepLinkType.Search -> {
            navController.navigate("user_list") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}
