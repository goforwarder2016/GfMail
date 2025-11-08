package com.gf.mail.data.local.dao

import androidx.room.*
import com.gf.mail.data.local.entity.ServerConfigurationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for server configuration operations
 */
@Dao
interface ServerConfigurationDao {

    @Query("SELECT * FROM server_configurations ORDER BY updatedAt DESC")
    suspend fun getAllConfigurations(): List<ServerConfigurationEntity>

    @Query("SELECT * FROM server_configurations ORDER BY updatedAt DESC")
    fun getAllConfigurationsFlow(): Flow<List<ServerConfigurationEntity>>

    @Query("SELECT * FROM server_configurations WHERE id = :id LIMIT 1")
    suspend fun getConfigurationById(id: String): ServerConfigurationEntity?

    @Query("SELECT * FROM server_configurations WHERE accountId = :accountId LIMIT 1")
    suspend fun getConfigurationByAccountId(accountId: String): ServerConfigurationEntity?

    @Query("SELECT * FROM server_configurations WHERE accountId = :accountId LIMIT 1")
    fun getConfigurationByAccountIdFlow(accountId: String): Flow<ServerConfigurationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguration(configuration: ServerConfigurationEntity)

    @Update
    suspend fun updateConfiguration(configuration: ServerConfigurationEntity)

    @Delete
    suspend fun deleteConfiguration(configuration: ServerConfigurationEntity)

    @Query("DELETE FROM server_configurations WHERE id = :id")
    suspend fun deleteConfigurationById(id: String)

    @Query("DELETE FROM server_configurations WHERE accountId = :accountId")
    suspend fun deleteConfigurationsForAccount(accountId: String)

    // IMAP specific updates
    @Query(
        "UPDATE server_configurations SET imapHost = :host, imapPort = :port, imapSecurity = :security WHERE accountId = :accountId"
    )
    suspend fun updateImapSettings(accountId: String, host: String, port: Int, security: String)

    @Query(
        "UPDATE server_configurations SET imapConnectionTimeout = :timeout, imapReadTimeout = :readTimeout WHERE accountId = :accountId"
    )
    suspend fun updateImapTimeouts(accountId: String, timeout: Int, readTimeout: Int)

    // SMTP specific updates
    @Query(
        "UPDATE server_configurations SET smtpHost = :host, smtpPort = :port, smtpSecurity = :security WHERE accountId = :accountId"
    )
    suspend fun updateSmtpSettings(accountId: String, host: String, port: Int, security: String)

    @Query(
        "UPDATE server_configurations SET smtpConnectionTimeout = :timeout WHERE accountId = :accountId"
    )
    suspend fun updateSmtpTimeout(accountId: String, timeout: Int)

    // Security updates
    @Query(
        "UPDATE server_configurations SET certificatePinning = :enabled WHERE accountId = :accountId"
    )
    suspend fun updateCertificatePinning(accountId: String, enabled: Boolean)

    @Query(
        "UPDATE server_configurations SET useProxy = :enabled, proxyType = :type, proxyHost = :host, proxyPort = :port WHERE accountId = :accountId"
    )
    suspend fun updateProxySettings(
        accountId: String,
        enabled: Boolean,
        type: String,
        host: String?,
        port: Int
    )

    @Query("UPDATE server_configurations SET updatedAt = :timestamp WHERE accountId = :accountId")
    suspend fun updateLastModified(accountId: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM server_configurations")
    suspend fun getConfigurationCount(): Int

    @Query("SELECT COUNT(*) FROM server_configurations WHERE accountId = :accountId")
    suspend fun hasConfigurationForAccount(accountId: String): Int
}