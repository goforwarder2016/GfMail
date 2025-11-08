package com.gf.mail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.mail.domain.model.EmailSignature
import com.gf.mail.domain.model.LanguageSetting
import com.gf.mail.domain.model.PerformanceMode
import com.gf.mail.domain.model.SecuritySetting
import com.gf.mail.domain.usecase.ManageEmailSignaturesUseCase
import com.gf.mail.domain.usecase.ManageLanguageSettingsUseCase
import com.gf.mail.domain.usecase.ManagePerformanceOptimizationUseCase
import com.gf.mail.domain.usecase.ManagePushNotificationsUseCase
import com.gf.mail.domain.usecase.ManageSecuritySettingsUseCase
import com.gf.mail.domain.usecase.ManageUserSettingsUseCase
import com.gf.mail.utils.LanguageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val manageUserSettingsUseCase: ManageUserSettingsUseCase,
    private val manageEmailSignaturesUseCase: ManageEmailSignaturesUseCase,
    private val manageSecuritySettingsUseCase: ManageSecuritySettingsUseCase,
    private val managePerformanceOptimizationUseCase: ManagePerformanceOptimizationUseCase,
    private val managePushNotificationsUseCase: ManagePushNotificationsUseCase,
    private val manageLanguageSettingsUseCase: ManageLanguageSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Load user settings
                manageUserSettingsUseCase.getUserSettings().collect { userSettings ->
                    _uiState.value = _uiState.value.copy(
                        settings = userSettings,
                        isDarkModeEnabled = userSettings.theme == "dark",
                        isReadReceiptsEnabled = userSettings.requestReadReceipts,
                        isSendUndoEnabled = false, // TODO: Add to UserSettings
                        defaultReplyAction = "reply" // TODO: Add to UserSettings
                    )
                }

                // Load email signatures
                manageEmailSignaturesUseCase.getEmailSignatures().collect { signatures ->
                    _uiState.value = _uiState.value.copy(emailSignatures = signatures)
                }

                // Load security settings
                manageSecuritySettingsUseCase.getSecuritySettings().collect { securitySettings ->
                    _uiState.value = _uiState.value.copy(
                        securitySettings = securitySettings
                    )
                }

                // Load performance settings
                managePerformanceOptimizationUseCase.getPerformanceMode().collect { performanceMode ->
                    _uiState.value = _uiState.value.copy(performanceMode = performanceMode)
                }

                // Load push notification settings
                managePushNotificationsUseCase.getPushNotificationSettings().collect { pushSettings ->
                    _uiState.value = _uiState.value.copy(
                        isPushNotificationsEnabled = pushSettings.isEnabled,
                        pushNotificationSound = pushSettings.sound,
                        pushNotificationVibration = pushSettings.vibration
                    )
                }

                // Load language settings
                manageLanguageSettingsUseCase.getLanguageSetting().collect { languageSetting ->
                    _uiState.value = _uiState.value.copy(languageSetting = languageSetting)
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // Functions to update settings
    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            // TODO: Implement toggle dark mode
            _uiState.value = _uiState.value.copy(isDarkModeEnabled = enabled)
        }
    }

    fun toggleReadReceipts(enabled: Boolean) {
        viewModelScope.launch {
            // TODO: Implement toggle read receipts
            _uiState.value = _uiState.value.copy(isReadReceiptsEnabled = enabled)
        }
    }

    fun toggleSendUndo(enabled: Boolean) {
        viewModelScope.launch {
            // TODO: Implement toggle send undo
            _uiState.value = _uiState.value.copy(isSendUndoEnabled = enabled)
        }
    }

    fun setDefaultReplyAction(action: String) {
        viewModelScope.launch {
            // TODO: Implement set default reply action
            _uiState.value = _uiState.value.copy(defaultReplyAction = action)
        }
    }

    fun addEmailSignature(signature: EmailSignature) {
        viewModelScope.launch {
            manageEmailSignaturesUseCase.addEmailSignature(signature)
        }
    }

    fun updateEmailSignature(signature: EmailSignature) {
        viewModelScope.launch {
            manageEmailSignaturesUseCase.updateEmailSignature(signature)
        }
    }

    fun deleteEmailSignature(signatureId: Long) {
        viewModelScope.launch {
            manageEmailSignaturesUseCase.deleteEmailSignature(signatureId)
        }
    }

    fun setPerformanceMode(mode: PerformanceMode) {
        viewModelScope.launch {
            managePerformanceOptimizationUseCase.setPerformanceMode(mode)
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            // TODO: Implement toggle push notifications
            _uiState.value = _uiState.value.copy(isPushNotificationsEnabled = enabled)
        }
    }

    fun setPushNotificationSound(sound: String) {
        viewModelScope.launch {
            // TODO: Implement set push notification sound
            _uiState.value = _uiState.value.copy(pushNotificationSound = sound)
        }
    }

    fun setPushNotificationVibration(vibration: Boolean) {
        viewModelScope.launch {
            // TODO: Implement set push notification vibration
            _uiState.value = _uiState.value.copy(pushNotificationVibration = vibration)
        }
    }

    fun setLanguageSetting(language: LanguageSetting) {
        viewModelScope.launch {
            manageLanguageSettingsUseCase.setLanguageSetting(language)
        }
    }

    // Add missing methods for MainSettingsScreen
    fun updateEmailSettings(settings: com.gf.mail.domain.model.UserSettings) {
        viewModelScope.launch {
            try {
                manageUserSettingsUseCase.updateUserSettings(settings)
                _uiState.value = _uiState.value.copy(message = "Email settings updated")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateDisplaySettings(settings: com.gf.mail.domain.model.UserSettings) {
        viewModelScope.launch {
            try {
                manageUserSettingsUseCase.updateUserSettings(settings)
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    message = "Display settings updated"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                // TODO: Implement set push notifications enabled
                _uiState.value = _uiState.value.copy(isPushNotificationsEnabled = enabled)
                _uiState.value = _uiState.value.copy(message = "Notifications ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleBackgroundSync(enabled: Boolean) {
        viewModelScope.launch {
            try {
                // TODO: Implement background sync toggle
                _uiState.value = _uiState.value.copy(message = "Background sync ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleBiometricAuth(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.securitySettings
                val updatedSettings = currentSettings.copy(biometricAuthEnabled = enabled)
                manageSecuritySettingsUseCase.updateSecuritySettings(updatedSettings)
                _uiState.value = _uiState.value.copy(
                    securitySettings = updatedSettings,
                    message = "Biometric authentication ${if (enabled) "enabled" else "disabled"}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateSecuritySettings(settings: SecuritySetting) {
        viewModelScope.launch {
            try {
                manageSecuritySettingsUseCase.updateSecuritySettings(settings)
                _uiState.value = _uiState.value.copy(
                    securitySettings = settings,
                    message = "Security settings updated"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun loadCacheSize() {
        viewModelScope.launch {
            try {
                // TODO: Implement cache size loading
                _uiState.value = _uiState.value.copy(cacheSizeFormatted = "0 MB")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                // TODO: Implement cache clearing
                _uiState.value = _uiState.value.copy(message = "Cache cleared")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun optimizeStorage() {
        viewModelScope.launch {
            try {
                // TODO: Implement storage optimization
                _uiState.value = _uiState.value.copy(message = "Storage optimized")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun exportSettings() {
        viewModelScope.launch {
            try {
                // TODO: Implement settings export
                _uiState.value = _uiState.value.copy(message = "Settings exported")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            try {
                // TODO: Implement reset to defaults
                _uiState.value = _uiState.value.copy(message = "Settings reset to defaults")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateLanguage(language: LanguageSetting) {
        viewModelScope.launch {
            try {
                manageLanguageSettingsUseCase.setLanguageSetting(language)
                _uiState.value = _uiState.value.copy(
                    languageSetting = language,
                    message = "Language updated. Please restart the app to see changes."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    /**
     * Update language and apply immediately
     */
    fun updateLanguageImmediate(language: LanguageSetting, context: android.content.Context) {
        viewModelScope.launch {
            try {
                // Save language setting
                manageLanguageSettingsUseCase.setLanguageSetting(language)
                
                // Apply language setting immediately
                val languageCode = when (language) {
                    LanguageSetting.SYSTEM_DEFAULT -> "system"
                    LanguageSetting.ENGLISH -> "en"
                    LanguageSetting.CHINESE_SIMPLIFIED -> "zh-CN"
                    LanguageSetting.CHINESE_TRADITIONAL -> "zh-TW"
                    LanguageSetting.JAPANESE -> "ja"
                    LanguageSetting.KOREAN -> "ko"
                }
                
                LanguageManager.saveLanguageCode(context, languageCode)
                LanguageManager.applyLanguageSetting(context, languageCode)
                
                _uiState.value = _uiState.value.copy(
                    languageSetting = language,
                    message = "Language updated successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateTheme(theme: String, context: android.content.Context) {
        viewModelScope.launch {
            try {
                // Save theme setting
                manageUserSettingsUseCase.updateTheme(theme)
                
                // Also save to ThemeManager for immediate effect
                com.gf.mail.utils.ThemeManager.saveTheme(context, theme)
                
                _uiState.value = _uiState.value.copy(
                    settings = _uiState.value.settings.copy(theme = theme),
                    message = "Theme updated successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    data class SettingsUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val message: String? = null,
        val isDarkModeEnabled: Boolean = false,
        val isReadReceiptsEnabled: Boolean = false,
        val isSendUndoEnabled: Boolean = false,
        val defaultReplyAction: String = "Reply", // TODO: Use string resource
        val emailSignatures: List<EmailSignature> = emptyList(),
        val securitySettings: SecuritySetting = SecuritySetting(),
        val performanceMode: PerformanceMode = PerformanceMode.BALANCED,
        val isPushNotificationsEnabled: Boolean = false,
        val pushNotificationSound: String = "Default", // TODO: Use string resource
        val pushNotificationVibration: Boolean = true,
        val languageSetting: LanguageSetting = LanguageSetting.ENGLISH,
        val cacheSizeFormatted: String = "0 MB",
        val syncIntervalDisplayName: String = "15 minutes",
        val autoLockDelayDisplayName: String = "5 minutes",
        val themeDisplayName: String = "System", // TODO: Use string resource
        val settings: com.gf.mail.domain.model.UserSettings = com.gf.mail.domain.model.UserSettings()
    )
}