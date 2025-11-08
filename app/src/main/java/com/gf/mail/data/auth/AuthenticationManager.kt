package com.gf.mail.data.auth

import android.content.Context
import com.gf.mail.data.provider.ProviderConfigService
import com.gf.mail.data.repository.AccountRepositoryImpl
import com.gf.mail.data.security.CredentialEncryption
import com.gf.mail.data.service.ConnectionTestService
import com.gf.mail.domain.model.*
import com.gf.mail.domain.model.TokenRefreshStatus
import com.gf.mail.domain.model.TokenRefreshStatusFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central authentication manager that coordinates OAuth2 and IMAP authentication
 * Handles the complete authentication flow for different email providers
 */
@Singleton
class AuthenticationManager @Inject constructor(
    private val context: Context,
    private val oAuth2Service: OAuth2Service,
    private val imapAuthService: ImapAuthService,
    private val providerConfigService: ProviderConfigService,
    private val connectionTestService: ConnectionTestService,
    private val accountRepository: AccountRepositoryImpl,
    private val credentialEncryption: CredentialEncryption
) {

    private val _oauth2AuthState = MutableStateFlow(OAuth2AuthState.IDLE)
    
    /**
     * OAuth2 authentication state flow
     */
    val oauth2AuthState: StateFlow<OAuth2AuthState> = _oauth2AuthState.asStateFlow()

    /**
     * Start OAuth2 authentication flow
     */
    suspend fun startOAuth2Flow(provider: EmailProvider): AuthenticationFlowResult {
        return try {
            val authUrl = oAuth2Service.getAuthorizationUrl(provider)
            // Create an intent with the auth URL - this is a simplified approach
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(authUrl))
            AuthenticationFlowResult.OAuth2FlowStarted(intent)
        } catch (e: Exception) {
            AuthenticationFlowResult.Error(e.message ?: "Failed to start OAuth2 flow")
        }
    }

    /**
     * Handle OAuth2 callback
     */
    suspend fun handleOAuth2Callback(callbackUrl: String, provider: EmailProvider): AuthenticationResult {
        return try {
            val authCode = oAuth2Service.extractAuthCode(callbackUrl)
            if (authCode != null) {
                val result = oAuth2Service.exchangeCodeForTokens(provider, authCode)
                when (result) {
                    is OAuth2Result.Success -> {
                        // Create account from tokens
                        val account = createAccountFromTokens(result.tokens, provider)
                        accountRepository.insertAccount(account)
                        AuthenticationResult.Success(account)
                    }
                    is OAuth2Result.Failed -> {
                        AuthenticationResult.Error(result.errorMessage)
                    }
                    else -> {
                        AuthenticationResult.Error("Unknown OAuth2 result type")
                    }
                }
            } else {
                AuthenticationResult.Error("Failed to extract authorization code")
            }
        } catch (e: Exception) {
            AuthenticationResult.Error(e.message ?: "OAuth2 callback failed")
        }
    }

    /**
     * Authenticate with credentials
     */
    suspend fun authenticateWithCredentials(
        email: String,
        password: String,
        provider: EmailProvider,
        serverConfig: ServerConfiguration
    ): AuthenticationResult {
        return try {
            // Test connection
            val connectionResult = connectionTestService.testConnection(serverConfig, email, password)
            
            if (connectionResult) {
                // Create account
                val account = Account(
                    id = generateAccountId(),
                    email = email,
                    emailAddress = email,
                    displayName = email.substringBefore("@"),
                    fullName = email.substringBefore("@"),
                    provider = provider,
                    serverConfig = serverConfig,
                    isActive = true,
                    isEnabled = true,
                    lastSync = System.currentTimeMillis()
                )
                
                // Encrypt and store credentials
                imapAuthService.storeCredentials(account.id, email, password)
                accountRepository.insertAccount(account)
                
                AuthenticationResult.Success(account)
            } else {
                AuthenticationResult.Error("Connection test failed")
            }
        } catch (e: Exception) {
            AuthenticationResult.Error(e.message ?: "Authentication failed")
        }
    }

    /**
     * Test account connection
     */
    suspend fun testConnection(account: Account): AuthenticationResult {
        return try {
            val password = credentialEncryption.getPasswordSecurely(account.id)
            if (password != null) {
                val result = connectionTestService.testConnection(
                    account.serverConfig,
                    account.email,
                    password
                )
                if (result) {
                    AuthenticationResult.Success(account)
                } else {
                    AuthenticationResult.Error("Connection test failed")
                }
            } else {
                AuthenticationResult.Error("Credentials not found")
            }
        } catch (e: Exception) {
            AuthenticationResult.Error(e.message ?: "Connection test failed")
        }
    }

    /**
     * Refresh OAuth2 tokens
     */
    suspend fun refreshTokens(account: Account): TokenRefreshStatus {
        return try {
            val refreshToken = imapAuthService.getRefreshToken(account.id)
            if (refreshToken != null) {
                val result = oAuth2Service.refreshTokens(account.provider, refreshToken)
                when (result) {
                    is OAuth2Result.Success -> {
                        imapAuthService.storeTokens(account.id, result.tokens)
                        TokenRefreshStatusFactory.success()
                    }
                    is OAuth2Result.Failed -> {
                        TokenRefreshStatusFactory.failed(result.errorMessage)
                    }
                    else -> {
                        TokenRefreshStatusFactory.failed("Unknown OAuth2 result type")
                    }
                }
            } else {
                TokenRefreshStatusFactory.failed("No refresh token found")
            }
        } catch (e: Exception) {
            TokenRefreshStatusFactory.failed(e.message ?: "Token refresh failed")
        }
    }

    /**
     * Create account from OAuth2 tokens
     */
    private fun createAccountFromTokens(tokens: OAuth2Tokens, provider: EmailProvider): Account {
        val email = oAuth2Service.getEmailFromTokens(tokens) ?: "unknown@example.com"
        val serverConfig = oAuth2Service.getServerConfigForProvider(provider)
        
        return Account(
            id = generateAccountId(),
            email = email,
            emailAddress = email,
            displayName = email.substringBefore("@"),
            fullName = email.substringBefore("@"),
            provider = provider,
            serverConfig = serverConfig,
            isActive = true,
            isEnabled = true,
            lastSync = System.currentTimeMillis()
        )
    }

    /**
     * Generate unique account ID
     */
    private fun generateAccountId(): String {
        return "account_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}