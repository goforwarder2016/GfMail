package com.gf.mail.domain.model

/**
 * OAuth2 token data
 */
data class OAuth2Tokens(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Int, // seconds
    val tokenType: String = "Bearer",
    val scope: String? = null
) {
    fun getExpirationTime(): Long {
        return System.currentTimeMillis() + (expiresIn * 1000L)
    }

    fun isExpired(): Boolean {
        return System.currentTimeMillis() > getExpirationTime()
    }
}

/**
 * OAuth2 provider configuration
 */
data class OAuth2ProviderConfig(
    val clientId: String,
    val clientSecret: String,
    val scope: String,
    val authUrl: String,
    val tokenUrl: String,
    val isDemoMode: Boolean = false
)

/**
 * Authentication flow results
 */
sealed class AuthenticationFlowResult {
    data class OAuth2FlowStarted(val intent: android.content.Intent) : AuthenticationFlowResult()
    data class PasswordRequired(
        val emailAddress: String,
        val provider: EmailProvider,
        val authType: AuthenticationType
    ) : AuthenticationFlowResult()
    data class Error(val message: String) : AuthenticationFlowResult()
}

/**
 * Authentication results
 */
sealed class AuthenticationResult {
    data class Success(val account: Account) : AuthenticationResult()
    data class Error(val message: String) : AuthenticationResult()
    data class RequiresOAuth2(val flowResult: AuthenticationFlowResult) : AuthenticationResult()
}

