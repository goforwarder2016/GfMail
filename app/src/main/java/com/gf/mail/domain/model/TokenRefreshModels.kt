package com.gf.mail.domain.model

/**
 * Token refresh result sealed class
 */
sealed class TokenRefreshResult {
    object Success : TokenRefreshResult()
    data class Error(val message: String) : TokenRefreshResult()
    object InProgress : TokenRefreshResult()
}