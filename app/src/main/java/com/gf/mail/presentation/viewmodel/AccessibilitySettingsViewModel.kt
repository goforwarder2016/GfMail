package com.gf.mail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.mail.domain.model.AccessibilityRecommendation
import com.gf.mail.domain.model.AccessibilityRecommendationType
import com.gf.mail.domain.usecase.ManageAccessibilitySettingsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for accessibility settings screen
 */
class AccessibilitySettingsViewModel(
    private val manageAccessibilitySettingsUseCase: ManageAccessibilitySettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccessibilitySettingsUiState())
    val uiState: StateFlow<AccessibilitySettingsUiState> = _uiState.asStateFlow()

    init {
        loadAccessibilitySettings()
        loadAccessibilityStatus()
        loadRecommendations()
    }

    /**
     * Load accessibility settings
     */
    private fun loadAccessibilitySettings() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                manageAccessibilitySettingsUseCase.getAccessibilitySettingsFlow().collect { settings ->
                    _uiState.value = _uiState.value.copy(
                        settings = settings,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load accessibility settings: ${e.message}"
                )
            }
        }
    }

    /**
     * Load accessibility status from system
     */
    private fun loadAccessibilityStatus() {
        viewModelScope.launch {
            try {
                val isAccessibilityEnabled = manageAccessibilitySettingsUseCase.isAccessibilityEnabled()
                val isTalkBackEnabled = manageAccessibilitySettingsUseCase.isTalkBackEnabled()

                _uiState.value = _uiState.value.copy(
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    isTalkBackEnabled = isTalkBackEnabled
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load accessibility status: ${e.message}"
                )
            }
        }
    }

    /**
     * Load accessibility recommendations
     */
    private fun loadRecommendations() {
        viewModelScope.launch {
            try {
                val recommendations = manageAccessibilitySettingsUseCase.getAccessibilityRecommendations()
                _uiState.value = _uiState.value.copy(
                    recommendations = recommendations
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load recommendations: ${e.message}"
                )
            }
        }
    }

    /**
     * Update high contrast mode
     */
    fun updateHighContrastMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val result = manageAccessibilitySettingsUseCase.updateHighContrastMode(enabled)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update high contrast mode: ${result.exceptionOrNull()?.message}"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        message = "High contrast mode ${if (enabled) "enabled" else "disabled"}"
                    )
                    loadRecommendations() // Refresh recommendations
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update high contrast mode: ${e.message}"
                )
            }
        }
    }

    /**
     * Update large text mode
     */
    fun updateLargeTextMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val result = manageAccessibilitySettingsUseCase.updateLargeTextMode(enabled)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update large text mode: ${result.exceptionOrNull()?.message}"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        message = "Large text mode ${if (enabled) "enabled" else "disabled"}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update large text mode: ${e.message}"
                )
            }
        }
    }

    /**
     * Update reduce motion
     */
    fun updateReduceMotion(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val result = manageAccessibilitySettingsUseCase.updateReduceMotion(enabled)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update reduce motion: ${result.exceptionOrNull()?.message}"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        message = "Reduce motion ${if (enabled) "enabled" else "disabled"}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update reduce motion: ${e.message}"
                )
            }
        }
    }

    /**
     * Update keyboard navigation
     */
    fun updateKeyboardNavigation(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val result = manageAccessibilitySettingsUseCase.updateKeyboardNavigation(enabled)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update keyboard navigation: ${result.exceptionOrNull()?.message}"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        message = "Keyboard navigation ${if (enabled) "enabled" else "disabled"}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update keyboard navigation: ${e.message}"
                )
            }
        }
    }

    /**
     * Update color blind friendly mode
     */
    fun updateColorBlindFriendly(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val result = manageAccessibilitySettingsUseCase.updateColorBlindFriendly(enabled)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update color blind friendly mode: ${result.exceptionOrNull()?.message}"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        message = "Color blind friendly mode ${if (enabled) "enabled" else "disabled"}"
                    )
                    loadRecommendations() // Refresh recommendations
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update color blind friendly mode: ${e.message}"
                )
            }
        }
    }

    /**
     * Update focus indicator
     */
    fun updateFocusIndicator(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val result = manageAccessibilitySettingsUseCase.updateFocusIndicator(enabled)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update focus indicator: ${result.exceptionOrNull()?.message}"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        message = "Focus indicator ${if (enabled) "enabled" else "disabled"}"
                    )
                    loadRecommendations() // Refresh recommendations
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update focus indicator: ${e.message}"
                )
            }
        }
    }

    /**
     * Update touch target size
     */
    fun updateTouchTargetSize(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val result = manageAccessibilitySettingsUseCase.updateTouchTargetSize(enabled)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update touch target size: ${result.exceptionOrNull()?.message}"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        message = "Touch target size ${if (enabled) "enabled" else "disabled"}"
                    )
                    loadRecommendations() // Refresh recommendations
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update touch target size: ${e.message}"
                )
            }
        }
    }

    /**
     * Apply automatic accessibility settings
     */
    fun applyAutoSettings() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val result = manageAccessibilitySettingsUseCase.applyAutoAccessibilitySettings()
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to apply auto settings: ${result.exceptionOrNull()?.message}"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Auto accessibility settings applied successfully"
                    )
                    loadRecommendations() // Refresh recommendations
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to apply auto settings: ${e.message}"
                )
            }
        }
    }

    /**
     * Reset accessibility settings
     */
    fun resetSettings() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val result = manageAccessibilitySettingsUseCase.resetAccessibilitySettings()
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to reset settings: ${result.exceptionOrNull()?.message}"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Accessibility settings reset to defaults"
                    )
                    loadRecommendations() // Refresh recommendations
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to reset settings: ${e.message}"
                )
            }
        }
    }

    /**
     * Apply specific recommendation
     */
    fun applyRecommendation(recommendation: AccessibilityRecommendation) {
        viewModelScope.launch {
            try {
                when (recommendation.type) {
                    AccessibilityRecommendationType.VISUAL -> updateHighContrastMode(true)
                    AccessibilityRecommendationType.MOTOR -> updateTouchTargetSize(true)
                    AccessibilityRecommendationType.COGNITIVE -> updateReduceMotion(true)
                    AccessibilityRecommendationType.AUDITORY -> updateFocusIndicator(true)
                    AccessibilityRecommendationType.SPEECH -> updateLargeTextMode(true)
                    AccessibilityRecommendationType.LANGUAGE -> updateColorBlindFriendly(true)
                }

                _uiState.value = _uiState.value.copy(
                    message = "Applied recommendation: ${recommendation.title}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to apply recommendation: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }


    /**
     * Clear success message
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    /**
     * Refresh accessibility status
     */
    fun refreshAccessibilityStatus() {
        loadAccessibilityStatus()
        loadRecommendations()
    }
}

/**
 * UI state for accessibility settings screen
 */
data class AccessibilitySettingsUiState(
    val settings: com.gf.mail.domain.model.AccessibilitySettings = com.gf.mail.domain.model.AccessibilitySettings(),
    val isAccessibilityEnabled: Boolean = false,
    val isTalkBackEnabled: Boolean = false,
    val recommendations: List<AccessibilityRecommendation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)
