package com.rafaelmukhametov.githubusersandroid.util

import android.net.Uri

sealed class DeepLinkType {
    data class User(val username: String) : DeepLinkType()
    data class Repository(val owner: String, val repo: String) : DeepLinkType()
    object Favorites : DeepLinkType()
    data class Search(val query: String) : DeepLinkType()
}

object DeepLinkManager {
    fun handleURL(url: Uri): DeepLinkType? {
        val scheme = url.scheme ?: return null
        
        // Custom scheme: githubusers://
        if (scheme == "githubusers") {
            return handleCustomScheme(url)
        }
        
        // Universal Links: https://github.com/
        if (url.host == "github.com") {
            return handleUniversalLink(url)
        }
        
        return null
    }
    
    private fun handleCustomScheme(url: Uri): DeepLinkType? {
        val pathSegments = url.pathSegments
        
        if (pathSegments.isEmpty()) return null
        
        return when (pathSegments[0]) {
            "user" -> {
                if (pathSegments.size > 1) {
                    DeepLinkType.User(pathSegments[1])
                } else null
            }
            "repo" -> {
                if (pathSegments.size > 2) {
                    DeepLinkType.Repository(pathSegments[1], pathSegments[2])
                } else null
            }
            "favorites" -> DeepLinkType.Favorites
            "search" -> {
                if (pathSegments.size > 1) {
                    DeepLinkType.Search(pathSegments[1])
                } else null
            }
            else -> null
        }
    }
    
    private fun handleUniversalLink(url: Uri): DeepLinkType? {
        val pathSegments = url.pathSegments.filter { it.isNotEmpty() }
        
        if (pathSegments.isEmpty()) return null
        
        // https://github.com/{username}
        if (pathSegments.size == 1) {
            return DeepLinkType.User(pathSegments[0])
        }
        
        // https://github.com/{owner}/{repo}
        if (pathSegments.size == 2) {
            return DeepLinkType.Repository(pathSegments[0], pathSegments[1])
        }
        
        return null
    }
}

