package com.gf.mail.data.repository

import com.gf.mail.data.local.dao.AccountDao
import com.gf.mail.data.mapper.toDomain
import com.gf.mail.data.mapper.toEntity
import com.gf.mail.data.security.CredentialEncryption
import com.gf.mail.data.service.ConnectionTestService
import com.gf.mail.domain.model.*
import com.gf.mail.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class AccountRepositoryImpl(
    private val accountDao: AccountDao,
    private val credentialEncryption: CredentialEncryption,
    private val connectionTestService: ConnectionTestService
) : AccountRepository {

    // Basic CRUD operations
    override suspend fun getAccountById(id: String): Account? {
        return accountDao.getAccountById(id)?.toDomain()
    }

    override suspend fun getAccountByEmail(email: String): Account? {
        return accountDao.getAccountByEmail(email)?.toDomain()
    }

    override suspend fun getAllAccounts(): List<Account> {
        return accountDao.getAllAccounts().toDomain()
    }

    override suspend fun getAllAccountsSync(): List<Account> {
        return getAllAccounts() // Alias for consistency
    }

    override fun getAllAccountsFlow(): Flow<List<Account>> {
        return accountDao.getAllAccountsFlow().map { entities ->
            println("🔍 [AccountRepositoryImpl] Raw entities from database: ${entities.size} accounts")
            entities.forEach { entity ->
                println("  - ${entity.email} (${entity.provider}) - Active: ${entity.isActive}")
            }
            val accounts = entities.toDomain()
            println("📧 [AccountRepositoryImpl] Converted to domain: ${accounts.size} accounts")
            accounts
        }
    }

    override suspend fun insertAccount(account: Account, password: String?): Long {
        // Store credentials if password is provided
        password?.let { credentialEncryption.storePasswordSecurely(account.id, it) }
        accountDao.insertAccount(account.toEntity(encryptedPassword = password))
        return 1L // Return success indicator
    }

    override suspend fun updateAccount(account: Account) {
        // Get existing credentials to preserve them
        val existing = accountDao.getAccountById(account.id)
        accountDao.updateAccount(
            account.toEntity(
                encryptedPassword = existing?.encryptedPassword,
                oauthToken = existing?.oauthToken,
                oauthRefreshToken = existing?.oauthRefreshToken
            )
        )
    }

    override suspend fun deleteAccount(accountId: String) {
        // TODO: Add method to remove credentials from CredentialEncryption
        // credentialEncryption.removeCredentials(accountId)
        accountDao.deleteAccountById(accountId)
    }

    // Active Account Management
    override suspend fun getActiveAccount(): Account? {
        return accountDao.getActiveAccount()?.toDomain()
    }

    override fun getActiveAccountFlow(): Flow<Account?> {
        return accountDao.getActiveAccountFlow().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun setActiveAccount(accountId: String) {
        accountDao.clearAllActiveAccounts()
        accountDao.setActiveAccount(accountId)
    }

    override suspend fun clearAllActiveAccounts() {
        accountDao.clearAllActiveAccounts()
    }

    // Enhanced Account Management
    override suspend fun getEnabledAccounts(): List<Account> {
        return accountDao.getEnabledAccounts().toDomain()
    }

    override suspend fun getSyncEnabledAccounts(): List<Account> {
        return accountDao.getSyncEnabledAccounts().toDomain()
    }

    override suspend fun updateSyncEnabled(accountId: String, enabled: Boolean) {
        accountDao.updateSyncEnabled(accountId, enabled)
    }

    override suspend fun updateAccountEnabled(accountId: String, enabled: Boolean) {
        accountDao.updateAccountEnabled(accountId, enabled)
    }

    override suspend fun updateSyncStatus(accountId: String, timestamp: Long?, error: String?) {
        accountDao.updateSyncStatus(accountId, timestamp, error)
    }

    // Authentication & Credentials
    override suspend fun storePassword(accountId: String, password: String): Boolean {
        return try {
            // Get account to get email for storePasswordSecurely
            val account = accountDao.getAccountById(accountId)?.toDomain()
            if (account != null) {
                credentialEncryption.storePasswordSecurely(accountId, password)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getPassword(accountId: String): String? {
        return credentialEncryption.getPasswordSecurely(accountId)
    }

    override suspend fun storeOAuthTokens(
        accountId: String,
        accessToken: String,
        refreshToken: String?,
        expiresAt: Long?
    ): Boolean {
        return try {
            // Create OAuth2Tokens and store them
            val tokens = OAuth2Tokens(
                accessToken = accessToken,
                refreshToken = refreshToken ?: "",
                expiresIn = (expiresAt ?: 0L).toInt(),
                tokenType = "Bearer"
            )
            // TODO: Implement token storage
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getOAuthTokens(accountId: String): Pair<String?, String?> {
        // TODO: Implement OAuth token retrieval
        return Pair(null, null)
    }

    override suspend fun updateOAuthTokens(
        accountId: String,
        accessToken: String?,
        refreshToken: String?,
        expiresAt: Long?
    ) {
        try {
            // Create OAuth2Tokens and store them
            val tokens = OAuth2Tokens(
                accessToken = accessToken ?: "",
                refreshToken = refreshToken ?: "",
                expiresIn = (expiresAt ?: 0L).toInt(),
                tokenType = "Bearer"
            )
            // TODO: Implement token storage
        } catch (e: Exception) {
            // Log error or handle as needed
        }
    }



    override suspend fun getAccountsWithExpiredTokens(): List<Account> {
        return accountDao.getAccountsWithExpiredTokens(System.currentTimeMillis()).toDomain()
    }

    override suspend fun removeCredentials(accountId: String) {
        // TODO: Add method to remove credentials from CredentialEncryption
        // credentialEncryption.removeCredentials(accountId)
        accountDao.updateEncryptedPassword(accountId, null)
        accountDao.updateOAuthTokens(accountId, null, null, null)
    }

    // Server Configuration
    override suspend fun updateImapConfiguration(
        accountId: String,
        host: String?,
        port: Int,
        encryption: EncryptionType
    ) {
        accountDao.updateImapConfiguration(accountId, host, port, encryption.name)
    }

    override suspend fun updateSmtpConfiguration(
        accountId: String,
        host: String?,
        port: Int,
        encryption: EncryptionType
    ) {
        accountDao.updateSmtpConfiguration(accountId, host, port, encryption.name)
    }

    // Signature Management
    override suspend fun updateSignature(accountId: String, signature: String?) {
        accountDao.updateSignature(accountId, signature)
    }

    // Provider-based operations
    override suspend fun getAccountsByProvider(provider: EmailProvider): List<Account> {
        return accountDao.getAccountsByProvider(provider.name).toDomain()
    }

    override suspend fun getAccountCountByProvider(provider: EmailProvider): Int {
        return accountDao.getAccountCountByProvider(provider.name)
    }

    // Validation & Limits
    override suspend fun getTotalAccountCount(): Int {
        return accountDao.getTotalAccountCount()
    }

    override suspend fun getEnabledAccountCount(): Int {
        return accountDao.getEnabledAccountCount()
    }

    override suspend fun getEnabledAccountCountByProvider(provider: EmailProvider): Int {
        return accountDao.getEnabledAccountCountByProvider(provider.name)
    }

    override suspend fun canAddMoreAccounts(): Boolean {
        return getEnabledAccountCount() < 3 // Max 3 accounts per PRP requirement
    }

    override suspend fun isEmailAlreadyAdded(email: String): Boolean {
        return getAccountByEmail(email) != null
    }

    // Provider Configuration
    override suspend fun getProviderDefaults(provider: EmailProvider): ServerConfiguration {
        return when (provider) {
            EmailProvider.GMAIL -> ServerConfiguration(
                imapHost = "imap.gmail.com",
                imapPort = 993,
                imapEncryption = EncryptionType.SSL,
                smtpHost = "smtp.gmail.com",
                smtpPort = 587,
                smtpEncryption = EncryptionType.STARTTLS
            )
            EmailProvider.EXCHANGE -> ServerConfiguration(
                imapHost = "outlook.office365.com",
                imapPort = 993,
                imapEncryption = EncryptionType.SSL,
                smtpHost = "smtp-mail.outlook.com",
                smtpPort = 587,
                smtpEncryption = EncryptionType.STARTTLS
            )
            EmailProvider.IMAP -> ServerConfiguration(
                imapHost = null,
                imapPort = 993,
                imapEncryption = EncryptionType.SSL,
                smtpHost = null,
                smtpPort = 587,
                smtpEncryption = EncryptionType.STARTTLS
            )
            EmailProvider.QQ -> ServerConfiguration(
                imapHost = "imap.qq.com",
                imapPort = 993,
                imapEncryption = EncryptionType.SSL,
                smtpHost = "smtp.qq.com",
                smtpPort = 587,
                smtpEncryption = EncryptionType.STARTTLS
            )
            EmailProvider.NETEASE -> ServerConfiguration(
                imapHost = "imap.163.com",
                imapPort = 993,
                imapEncryption = EncryptionType.SSL,
                smtpHost = "smtp.163.com",
                smtpPort = 465,
                smtpEncryption = EncryptionType.SSL
            )
            EmailProvider.OUTLOOK -> ServerConfiguration(
                imapHost = "outlook.office365.com",
                imapPort = 993,
                imapEncryption = EncryptionType.SSL,
                smtpHost = "smtp-mail.outlook.com",
                smtpPort = 587,
                smtpEncryption = EncryptionType.STARTTLS
            )
            EmailProvider.YAHOO -> ServerConfiguration(
                imapHost = "imap.mail.yahoo.com",
                imapPort = 993,
                imapEncryption = EncryptionType.SSL,
                smtpHost = "smtp.mail.yahoo.com",
                smtpPort = 587,
                smtpEncryption = EncryptionType.STARTTLS
            )
            EmailProvider.APPLE -> ServerConfiguration(
                imapHost = "imap.mail.me.com",
                imapPort = 993,
                imapEncryption = EncryptionType.SSL,
                smtpHost = "smtp.mail.me.com",
                smtpPort = 587,
                smtpEncryption = EncryptionType.STARTTLS
            )
            EmailProvider.POP3 -> ServerConfiguration(
                imapHost = null,
                imapPort = 995,
                imapEncryption = EncryptionType.SSL,
                smtpHost = null,
                smtpPort = 587,
                smtpEncryption = EncryptionType.STARTTLS
            )
        }
    }

    override suspend fun testConnection(account: Account, password: String?): ConnectionTestResult {
        val isSuccess = connectionTestService.testConnection(account.serverConfig, account.email, password ?: "")
        return ConnectionTestResult(
            isSuccessful = isSuccess,
            errorMessage = if (!isSuccess) "Connection test failed" else null
        )
    }
}
