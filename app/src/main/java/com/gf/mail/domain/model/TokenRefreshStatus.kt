package com.gf.mail.domain.model

/**
 * Token refresh status
 */
sealed class TokenRefreshStatus {
    object Success : TokenRefreshStatus()
    data class Failed(val message: String) : TokenRefreshStatus()
}

// Companion object for easier access
object TokenRefreshStatusFactory {
    fun success(): TokenRefreshStatus = TokenRefreshStatus.Success
    fun failed(message: String): TokenRefreshStatus = TokenRefreshStatus.Failed(message)
}