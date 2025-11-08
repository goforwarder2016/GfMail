package com.gf.mail.data.local.dao

import androidx.room.*
import com.gf.mail.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    // Basic CRUD Operations
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmail(email: String): AccountEntity?

    @Query("SELECT * FROM accounts ORDER BY display_name ASC")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Query("SELECT * FROM accounts ORDER BY display_name ASC")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: String)

    // Provider-based queries
    @Query("SELECT * FROM accounts WHERE provider = :provider")
    suspend fun getAccountsByProvider(provider: String): List<AccountEntity>

    @Query("SELECT COUNT(*) FROM accounts WHERE provider = :provider")
    suspend fun getAccountCountByProvider(provider: String): Int

    // Active Account Management
    @Query("SELECT * FROM accounts WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveAccount(): AccountEntity?

    @Query("SELECT * FROM accounts WHERE is_active = 1 LIMIT 1")
    fun getActiveAccountFlow(): Flow<AccountEntity?>

    @Query("UPDATE accounts SET is_active = 0")
    suspend fun clearAllActiveAccounts()

    @Query("UPDATE accounts SET is_active = 1 WHERE id = :accountId")
    suspend fun setActiveAccount(accountId: String)

    // Enhanced Account Management
    @Query("SELECT * FROM accounts WHERE is_enabled = 1 ORDER BY display_name ASC")
    suspend fun getEnabledAccounts(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE sync_enabled = 1")
    suspend fun getSyncEnabledAccounts(): List<AccountEntity>

    @Query("UPDATE accounts SET sync_enabled = :enabled WHERE id = :accountId")
    suspend fun updateSyncEnabled(accountId: String, enabled: Boolean)

    @Query("UPDATE accounts SET is_enabled = :enabled WHERE id = :accountId")
    suspend fun updateAccountEnabled(accountId: String, enabled: Boolean)

    @Query("UPDATE accounts SET last_sync = :timestamp, sync_error = :error WHERE id = :accountId")
    suspend fun updateSyncStatus(accountId: String, timestamp: Long?, error: String?)

    // OAuth Token Management
    @Query(
        "UPDATE accounts SET oauth_token = :token, oauth_refresh_token = :refreshToken, oauth_expires_at = :expiresAt WHERE id = :accountId"
    )
    suspend fun updateOAuthTokens(
        accountId: String,
        token: String?,
        refreshToken: String?,
        expiresAt: Long?
    )

    @Query(
        "SELECT * FROM accounts WHERE oauth_expires_at IS NOT NULL AND oauth_expires_at < :currentTime"
    )
    suspend fun getAccountsWithExpiredTokens(currentTime: Long): List<AccountEntity>

    // Signature Management
    @Query("UPDATE accounts SET signature = :signature WHERE id = :accountId")
    suspend fun updateSignature(accountId: String, signature: String?)

    // Server Configuration
    @Query(
        "UPDATE accounts SET imap_host = :host, imap_port = :port, imap_encryption = :encryption WHERE id = :accountId"
    )
    suspend fun updateImapConfiguration(
        accountId: String,
        host: String?,
        port: Int,
        encryption: String
    )

    @Query(
        "UPDATE accounts SET smtp_host = :host, smtp_port = :port, smtp_encryption = :encryption WHERE id = :accountId"
    )
    suspend fun updateSmtpConfiguration(
        accountId: String,
        host: String?,
        port: Int,
        encryption: String
    )

    // Password Management
    @Query("UPDATE accounts SET encrypted_password = :encryptedPassword WHERE id = :accountId")
    suspend fun updateEncryptedPassword(accountId: String, encryptedPassword: String?)

    // Statistics
    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getTotalAccountCount(): Int

    @Query("SELECT COUNT(*) FROM accounts WHERE is_enabled = 1")
    suspend fun getEnabledAccountCount(): Int

    @Query("SELECT COUNT(*) FROM accounts WHERE provider = :provider AND is_enabled = 1")
    suspend fun getEnabledAccountCountByProvider(provider: String): Int

    // Account Limits (Max 3 accounts as per PRP)
    @Query("SELECT COUNT(*) FROM accounts WHERE is_enabled = 1")
    suspend fun getActiveAccountsCount(): Int
}
