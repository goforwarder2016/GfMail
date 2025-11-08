package com.gf.mail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.usecase.GetEmailsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for email list screen
 */
class EmailListViewModel(
    private val getEmailsUseCase: GetEmailsUseCase
) : ViewModel() {

    data class EmailListUiState(
        val emails: List<Email> = emptyList(),
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMoreEmails: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(EmailListUiState())
    val uiState: StateFlow<EmailListUiState> = _uiState.asStateFlow()

    private var currentFolderId: String? = null

    /**
     * Load emails for a specific folder
     */
    fun loadEmails(folderId: String, refresh: Boolean = false) {
        currentFolderId = folderId
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = !refresh,
                    isRefreshing = refresh,
                    error = null
                )

                // Get emails from the use case
                getEmailsUseCase.getEmails("current-account", folderId).collect { emails ->
                    _uiState.value = _uiState.value.copy(
                        emails = emails,
                        isLoading = false,
                        isRefreshing = false,
                        hasMoreEmails = false // Simplified for now
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = e.message ?: "Failed to load emails"
                )
            }
        }
    }

    /**
     * Refresh emails
     */
    fun refreshEmails() {
        currentFolderId?.let { folderId ->
            loadEmails(folderId, refresh = true)
        }
    }

    /**
     * Load more emails (pagination)
     */
    fun loadMoreEmails() {
        // TODO: Implement pagination
    }

    /**
     * Clear error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}