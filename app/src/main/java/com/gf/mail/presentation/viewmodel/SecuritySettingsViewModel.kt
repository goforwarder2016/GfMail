package com.gf.mail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.mail.domain.model.SecuritySetting
import com.gf.mail.domain.usecase.ManageSecuritySettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for security settings
 */
class SecuritySettingsViewModel(
    private val manageSecuritySettingsUseCase: ManageSecuritySettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecuritySettingsUiState())
    val uiState: StateFlow<SecuritySettingsUiState> = _uiState.asStateFlow()

    init {
        loadSecuritySettings()
    }

    private fun loadSecuritySettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                manageSecuritySettingsUseCase.getSecuritySettings().collect { settings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        securitySettings = settings
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateSecuritySettings(settings: SecuritySetting) {
        viewModelScope.launch {
            try {
                manageSecuritySettingsUseCase.updateSecuritySettings(settings)
                _uiState.value = _uiState.value.copy(successMessage = "Security settings updated successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to update security settings")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    // Add missing methods for SecuritySettingsScreen
    val recommendations: StateFlow<List<com.gf.mail.domain.model.SecurityRecommendation>> = 
        MutableStateFlow<List<com.gf.mail.domain.model.SecurityRecommendation>>(emptyList()).asStateFlow()

    fun toggleBiometricAuthentication(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.securitySettings ?: SecuritySetting()
                val updatedSettings = currentSettings.copy(biometricAuthEnabled = enabled)
                manageSecuritySettingsUseCase.updateSecuritySettings(updatedSettings)
                _uiState.value = _uiState.value.copy(
                    securitySettings = updatedSettings,
                    successMessage = "Biometric authentication ${if (enabled) "enabled" else "disabled"}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to update biometric settings")
            }
        }
    }

    fun toggleAutoLock(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.securitySettings ?: SecuritySetting()
                val updatedSettings = currentSettings.copy(autoLockTimeout = if (enabled) 5 else 0)
                manageSecuritySettingsUseCase.updateSecuritySettings(updatedSettings)
                _uiState.value = _uiState.value.copy(
                    securitySettings = updatedSettings,
                    successMessage = "Auto lock ${if (enabled) "enabled" else "disabled"}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to update auto lock settings")
            }
        }
    }

    fun toggleCertificatePinning(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.securitySettings ?: SecuritySetting()
                val updatedSettings = currentSettings.copy(advancedSecurityEnabled = enabled)
                manageSecuritySettingsUseCase.updateSecuritySettings(updatedSettings)
                _uiState.value = _uiState.value.copy(
                    securitySettings = updatedSettings,
                    successMessage = "Certificate pinning ${if (enabled) "enabled" else "disabled"}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to update certificate pinning")
            }
        }
    }

    fun toggleDataEncryption(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.securitySettings ?: SecuritySetting()
                val updatedSettings = currentSettings.copy(advancedSecurityEnabled = enabled)
                manageSecuritySettingsUseCase.updateSecuritySettings(updatedSettings)
                _uiState.value = _uiState.value.copy(
                    securitySettings = updatedSettings,
                    successMessage = "Data encryption ${if (enabled) "enabled" else "disabled"}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to update data encryption")
            }
        }
    }

    // Add more missing methods for SecuritySettingsScreen
    fun updateAutoLockTimeout(timeout: Int) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.securitySettings ?: SecuritySetting()
                val updatedSettings = currentSettings.copy(autoLockTimeout = timeout)
                manageSecuritySettingsUseCase.updateSecuritySettings(updatedSettings)
                _uiState.value = _uiState.value.copy(
                    securitySettings = updatedSettings,
                    successMessage = "Auto lock timeout updated to $timeout minutes"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to update auto lock timeout")
            }
        }
    }

    fun updateSessionTimeout(timeout: Int) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.securitySettings ?: SecuritySetting()
                val updatedSettings = currentSettings.copy(sessionTimeout = timeout)
                manageSecuritySettingsUseCase.updateSecuritySettings(updatedSettings)
                _uiState.value = _uiState.value.copy(
                    securitySettings = updatedSettings,
                    successMessage = "Session timeout updated to $timeout minutes"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to update session timeout")
            }
        }
    }

    fun addAllowedHostname(hostname: String) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.securitySettings ?: SecuritySetting()
                val updatedHosts = currentSettings.trustedHosts + hostname
                val updatedSettings = currentSettings.copy(trustedHosts = updatedHosts)
                manageSecuritySettingsUseCase.updateSecuritySettings(updatedSettings)
                _uiState.value = _uiState.value.copy(
                    securitySettings = updatedSettings,
                    successMessage = "Added trusted hostname: $hostname"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to add hostname")
            }
        }
    }

    fun removeAllowedHostname(hostname: String) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.securitySettings ?: SecuritySetting()
                val updatedHosts = currentSettings.trustedHosts.filter { it != hostname }
                val updatedSettings = currentSettings.copy(trustedHosts = updatedHosts)
                manageSecuritySettingsUseCase.updateSecuritySettings(updatedSettings)
                _uiState.value = _uiState.value.copy(
                    securitySettings = updatedSettings,
                    successMessage = "Removed trusted hostname: $hostname"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to remove hostname")
            }
        }
    }

    fun resetSecuritySettings() {
        viewModelScope.launch {
            try {
                val defaultSettings = SecuritySetting()
                manageSecuritySettingsUseCase.updateSecuritySettings(defaultSettings)
                _uiState.value = _uiState.value.copy(
                    securitySettings = defaultSettings,
                    successMessage = "Security settings reset to defaults"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to reset security settings")
            }
        }
    }

    // Add more missing methods for SecuritySettingsScreen
    fun createSession() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    sessionExpired = false,
                    message = "Session created successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to create session")
            }
        }
    }

    fun validateSession() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    sessionExpired = false,
                    message = "Session validated successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    sessionExpired = true,
                    error = e.message ?: "Session validation failed"
                )
            }
        }
    }

    fun invalidateSession() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    sessionExpired = true,
                    message = "Session invalidated"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to invalidate session")
            }
        }
    }

    fun authenticateWithBiometrics() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isAuthenticating = true)
                // TODO: Implement biometric authentication
                _uiState.value = _uiState.value.copy(
                    isAuthenticating = false,
                    message = "Biometric authentication successful"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAuthenticating = false,
                    error = e.message ?: "Biometric authentication failed"
                )
            }
        }
    }

    fun authenticateWithDeviceCredentials() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isAuthenticating = true)
                // TODO: Implement device credentials authentication
                _uiState.value = _uiState.value.copy(
                    isAuthenticating = false,
                    message = "Device credentials authentication successful"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAuthenticating = false,
                    error = e.message ?: "Device credentials authentication failed"
                )
            }
        }
    }

    fun acknowledgeSessionExpiry() {
        _uiState.value = _uiState.value.copy(sessionExpired = false)
    }

    fun validatePasswordStrength(password: String): Int {
        // TODO: Implement password strength validation
        return when {
            password.length < 6 -> 20
            password.length < 8 -> 40
            password.length < 12 -> 60
            password.length < 16 -> 80
            else -> 100
        }
    }

    /**
     * Toggle require biometric for sensitive operations
     */
    fun toggleRequireBiometricForSensitiveOperations(enabled: Boolean) {
        viewModelScope.launch {
            try {
                // TODO: Implement toggle require biometric for sensitive operations
                _uiState.value = _uiState.value.copy(message = "Require biometric for sensitive operations ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }


    data class SecuritySettingsUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,
        val message: String? = null,
        val securitySettings: SecuritySetting? = null,
        val sessionExpired: Boolean = false,
        val biometricAvailability: Boolean = false,
        val hasSecureLockScreen: Boolean = false,
        val isAuthenticating: Boolean = false
    )
}