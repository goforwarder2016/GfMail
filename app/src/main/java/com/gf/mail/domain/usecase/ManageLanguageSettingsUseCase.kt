package com.gf.mail.domain.usecase

import com.gf.mail.domain.model.LanguageSetting
import com.gf.mail.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case for managing language settings
 */
class ManageLanguageSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Get current language setting
     */
    suspend fun getLanguageSetting(): Flow<LanguageSetting> {
        return settingsRepository.getUserSettingsFlow().map { userSettings ->
            when (userSettings.language) {
                "system" -> LanguageSetting.SYSTEM_DEFAULT
                "en" -> LanguageSetting.ENGLISH
                "zh-CN" -> LanguageSetting.CHINESE_SIMPLIFIED
                "zh-TW" -> LanguageSetting.CHINESE_TRADITIONAL
                "ja" -> LanguageSetting.JAPANESE
                "ko" -> LanguageSetting.KOREAN
                else -> LanguageSetting.ENGLISH
            }
        }
    }

    /**
     * Set language setting
     */
    suspend fun setLanguageSetting(language: LanguageSetting) {
        val languageCode = when (language) {
            LanguageSetting.SYSTEM_DEFAULT -> "system"
            LanguageSetting.ENGLISH -> "en"
            LanguageSetting.CHINESE_SIMPLIFIED -> "zh-CN"
            LanguageSetting.CHINESE_TRADITIONAL -> "zh-TW"
            LanguageSetting.JAPANESE -> "ja"
            LanguageSetting.KOREAN -> "ko"
        }
        settingsRepository.updateLanguage(languageCode)
    }
}