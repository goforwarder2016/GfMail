package com.gf.mail.data.local.dao

import androidx.room.*
import com.gf.mail.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for user settings operations
 */
@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM user_settings WHERE id = :id LIMIT 1")
    suspend fun getSettings(id: String = "default_user_settings"): UserSettingsEntity?

    @Query("SELECT * FROM user_settings WHERE id = :id LIMIT 1")
    fun getSettingsFlow(id: String = "default_user_settings"): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettingsEntity)

    @Update
    suspend fun updateSettings(settings: UserSettingsEntity)

    @Query("DELETE FROM user_settings WHERE id = :id")
    suspend fun deleteSettings(id: String = "default_user_settings")

    // Specific setting updates for performance
    @Query("UPDATE user_settings SET theme = :theme WHERE id = :id")
    suspend fun updateTheme(theme: String, id: String = "default_user_settings")

    @Query("UPDATE user_settings SET language = :language WHERE id = :id")
    suspend fun updateLanguage(language: String, id: String = "default_user_settings")

    @Query("UPDATE user_settings SET globalNotificationsEnabled = :enabled WHERE id = :id")
    suspend fun updateNotificationsEnabled(enabled: Boolean, id: String = "default_user_settings")

    @Query("UPDATE user_settings SET backgroundSyncEnabled = :enabled WHERE id = :id")
    suspend fun updateBackgroundSyncEnabled(enabled: Boolean, id: String = "default_user_settings")

    @Query("UPDATE user_settings SET biometricAuthEnabled = :enabled WHERE id = :id")
    suspend fun updateBiometricAuthEnabled(enabled: Boolean, id: String = "default_user_settings")

    @Query("UPDATE user_settings SET defaultSignatureId = :signatureId WHERE id = :id")
    suspend fun updateDefaultSignature(signatureId: String?, id: String = "default_user_settings")

    @Query("UPDATE user_settings SET lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateLastModified(timestamp: Long, id: String = "default_user_settings")
}