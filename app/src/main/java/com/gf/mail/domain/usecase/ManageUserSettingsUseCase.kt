package com.gf.mail.domain.usecase

import com.gf.mail.domain.model.UserSettings
import com.gf.mail.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing user settings
 */
class ManageUserSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Get user settings
     */
    suspend fun getUserSettings(): Flow<UserSettings> {
        return settingsRepository.getUserSettingsFlow()
    }

    /**
     * Update user settings
     */
    suspend fun updateUserSettings(settings: UserSettings) {
        settingsRepository.updateUserSettings(settings)
    }
    
    /**
     * Update theme setting
     */
    suspend fun updateTheme(theme: String) {
        settingsRepository.updateTheme(theme)
    }
    
    /**
     * Get current theme
     */
    suspend fun getCurrentTheme(): String {
        return settingsRepository.getCurrentTheme()
    }
}