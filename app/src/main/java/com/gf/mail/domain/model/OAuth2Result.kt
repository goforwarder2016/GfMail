package com.gf.mail.domain.model

/**
 * Result of OAuth2 operations
 */
sealed class OAuth2Result {
    data class Success(val tokens: OAuth2Tokens) : OAuth2Result()
    data class Failed(val errorMessage: String) : OAuth2Result()
}

// Companion object for easier access
object OAuth2ResultFactory {
    fun success(tokens: OAuth2Tokens): OAuth2Result = OAuth2Result.Success(tokens)
    fun failed(errorMessage: String): OAuth2Result = OAuth2Result.Failed(errorMessage)
}