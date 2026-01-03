package com.rafaelmukhametov.githubusersandroid.data.model

/**
 * Типы ошибок приложения
 */
sealed class AppError(message: String) : Exception(message) {
    data class NetworkError(override val message: String) : AppError(message)
    data class DecodingError(override val message: String) : AppError(message)
    object NotFound : AppError("User not found")
    object Unauthorized : AppError("Unauthorized access")
    data class ServerError(val code: Int) : AppError("Server error: $code")
    object Unknown : AppError("Unknown error occurred")
}

