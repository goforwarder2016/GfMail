package com.gf.mail.domain.repository

import com.gf.mail.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    // Basic CRUD operations
    suspend fun getAccountById(id: String): Account?
    suspend fun getAccountByEmail(email: String): Account?
    suspend fun getAllAccounts(): List<Account>
    suspend fun getAllAccountsSync(): List<Account> // Alias for consistency
    fun getAllAccountsFlow(): Flow<List<Account>>

    suspend fun insertAccount(account: Account, password: String? = null): Long
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(accountId: String)

    // Active Account Management
    suspend fun getActiveAccount(): Account?
    fun getActiveAccountFlow(): Flow<Account?>
    suspend fun setActiveAccount(accountId: String)
    suspend fun clearAllActiveAccounts()

    // Enhanced Account Management
    suspend fun getEnabledAccounts(): List<Account>
    suspend fun getSyncEnabledAccounts(): List<Account>
    suspend fun updateSyncEnabled(accountId: String, enabled: Boolean)
    suspend fun updateAccountEnabled(accountId: String, enabled: Boolean)
    suspend fun updateSyncStatus(accountId: String, timestamp: Long?, error: String?)

    // Authentication & Credentials
    suspend fun storePassword(accountId: String, password: String): Boolean
    suspend fun getPassword(accountId: String): String?
    suspend fun storeOAuthTokens(
        accountId: String,
        accessToken: String,
        refreshToken: String?,
        expiresAt: Long?
    ): Boolean
    suspend fun getOAuthTokens(accountId: String): Pair<String?, String?>
    suspend fun updateOAuthTokens(
        accountId: String,
        accessToken: String?,
        refreshToken: String?,
        expiresAt: Long?
    )
    suspend fun getAccountsWithExpiredTokens(): List<Account>
    suspend fun removeCredentials(accountId: String)

    // Server Configuration
    suspend fun updateImapConfiguration(
        accountId: String,
        host: String?,
        port: Int,
        encryption: EncryptionType
    )
    suspend fun updateSmtpConfiguration(
        accountId: String,
        host: String?,
        port: Int,
        encryption: EncryptionType
    )

    // Signature Management
    suspend fun updateSignature(accountId: String, signature: String?)

    // Provider-based operations
    suspend fun getAccountsByProvider(provider: EmailProvider): List<Account>
    suspend fun getAccountCountByProvider(provider: EmailProvider): Int

    // Validation & Limits
    suspend fun getTotalAccountCount(): Int
    suspend fun getEnabledAccountCount(): Int
    suspend fun getEnabledAccountCountByProvider(provider: EmailProvider): Int
    suspend fun canAddMoreAccounts(): Boolean // Max 3 accounts per PRP requirement
    suspend fun isEmailAlreadyAdded(email: String): Boolean

    // Provider Configuration
    suspend fun getProviderDefaults(provider: EmailProvider): ServerConfiguration
    suspend fun testConnection(account: Account, password: String? = null): ConnectionTestResult
}
