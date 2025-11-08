package com.gf.mail.domain.repository

import com.gf.mail.domain.model.EmailSignature
import com.gf.mail.domain.model.ServerConfiguration
import com.gf.mail.domain.model.ThemeSettings
import com.gf.mail.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user settings management
 */
interface SettingsRepository {

    // User Settings
    suspend fun getUserSettings(): UserSettings
    fun getUserSettingsFlow(): Flow<UserSettings>
    suspend fun updateUserSettings(settings: UserSettings)
    suspend fun resetToDefaults(): UserSettings

    // Quick setting updates
    suspend fun updateTheme(theme: String)
    suspend fun getCurrentTheme(): String
    suspend fun updateLanguage(language: String)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateBackgroundSync(enabled: Boolean)
    suspend fun updateBiometricAuth(enabled: Boolean)

    // Email Signatures
    suspend fun getAllSignatures(): List<EmailSignature>
    fun getAllSignaturesFlow(): Flow<List<EmailSignature>>
    suspend fun getSignatureById(id: String): EmailSignature?
    suspend fun getSignaturesForAccount(accountId: String): List<EmailSignature>
    fun getSignaturesForAccountFlow(accountId: String): Flow<List<EmailSignature>>
    suspend fun getDefaultSignature(accountId: String? = null): EmailSignature?
    suspend fun createSignature(signature: EmailSignature): String
    suspend fun updateSignature(signature: EmailSignature)
    suspend fun deleteSignature(signatureId: String)
    suspend fun setDefaultSignature(signatureId: String, accountId: String? = null)

    // Signature Templates
    suspend fun getSignatureTemplates(): List<EmailSignature>
    fun getSignatureTemplatesFlow(): Flow<List<EmailSignature>>
    suspend fun getSignature(templateId: String): EmailSignature?

    // Server Configurations
    suspend fun getServerConfiguration(accountId: String): ServerConfiguration?
    fun getServerConfigurationFlow(accountId: String): Flow<ServerConfiguration?>
    suspend fun saveServerConfiguration(configuration: ServerConfiguration)
    suspend fun deleteServerConfiguration(accountId: String)
    suspend fun createDefaultServerConfiguration(accountId: String): ServerConfiguration

    // Theme and Appearance
    suspend fun getThemeSettings(): ThemeSettings
    suspend fun updateThemeSettings(themeSettings: ThemeSettings)

    // Import/Export Settings
    suspend fun exportSettings(): String // JSON
    suspend fun importSettings(settingsJson: String): Boolean
    suspend fun backupSettings(): String
    suspend fun restoreSettings(backupData: String): Boolean

    // Cache and Storage
    suspend fun clearCache()
    suspend fun getCacheSize(): Long
    suspend fun optimizeStorage()

    // Privacy and Security
    suspend fun clearAllData()
    suspend fun anonymizeData()

    // Migration and Versioning
    suspend fun migrateSettings(fromVersion: Int, toVersion: Int)
    suspend fun getSettingsVersion(): Int
}
