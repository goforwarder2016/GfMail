package com.gf.mail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.mail.domain.model.PushNotificationSettings
import com.gf.mail.domain.usecase.ManagePushNotificationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for push notification settings
 */
class PushNotificationViewModel(
    private val managePushNotificationsUseCase: ManagePushNotificationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PushNotificationUiState())
    val uiState: StateFlow<PushNotificationUiState> = _uiState.asStateFlow()

    init {
        loadPushNotificationSettings()
    }

    private fun loadPushNotificationSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                managePushNotificationsUseCase.getPushNotificationSettings().collect { settings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pushNotificationSettings = settings
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

    fun updatePushNotificationSettings(settings: PushNotificationSettings) {
        viewModelScope.launch {
            try {
                managePushNotificationsUseCase.updatePushNotificationSettings(settings)
                _uiState.value = _uiState.value.copy(successMessage = "Settings updated successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to update settings")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    data class PushNotificationUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,
        val pushNotificationSettings: PushNotificationSettings? = null
    )
}