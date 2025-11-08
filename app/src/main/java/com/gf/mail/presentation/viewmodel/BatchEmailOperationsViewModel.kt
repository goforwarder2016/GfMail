package com.gf.mail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.mail.domain.repository.BatchOperationResult
import com.gf.mail.domain.usecase.BatchEmailOperationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for batch email operations
 */
class BatchEmailOperationsViewModel(
    private val batchEmailOperationsUseCase: BatchEmailOperationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchEmailOperationsUiState())
    val uiState: StateFlow<BatchEmailOperationsUiState> = _uiState.asStateFlow()

    private val _selectedEmails = MutableStateFlow<Set<String>>(emptySet())
    val selectedEmails: StateFlow<Set<String>> = _selectedEmails.asStateFlow()

    private val _operationResult = MutableStateFlow<BatchOperationResult?>(null)
    val operationResult: StateFlow<BatchOperationResult?> = _operationResult.asStateFlow()

    fun markEmailsAsRead(emailIds: List<Long>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                batchEmailOperationsUseCase.markEmailsReadUnread(emailIds, true).collect { result ->
                    when (result) {
                        is BatchOperationResult.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Marked ${result.successCount} emails as read"
                            )
                        }
                        is BatchOperationResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        is BatchOperationResult.Progress -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.PartialSuccess -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Marked ${result.successCount} emails as read"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun markEmailsAsUnread(emailIds: List<Long>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                batchEmailOperationsUseCase.markEmailsReadUnread(emailIds, false).collect { result ->
                    when (result) {
                        is BatchOperationResult.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Marked ${result.successCount} emails as unread"
                            )
                        }
                        is BatchOperationResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        is BatchOperationResult.Progress -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.PartialSuccess -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Marked ${result.successCount} emails as unread"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun deleteEmails(emailIds: List<Long>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                batchEmailOperationsUseCase.deleteEmails(emailIds).collect { result ->
                    when (result) {
                        is BatchOperationResult.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Deleted ${result.successCount} emails"
                            )
                        }
                        is BatchOperationResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        is BatchOperationResult.Progress -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.PartialSuccess -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Deleted ${result.successCount} emails"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun toggleEmailSelection(emailId: String) {
        val currentSelection = _selectedEmails.value.toMutableSet()
        if (currentSelection.contains(emailId)) {
            currentSelection.remove(emailId)
        } else {
            currentSelection.add(emailId)
        }
        _selectedEmails.value = currentSelection
        _uiState.value = _uiState.value.copy(
            hasSelection = currentSelection.isNotEmpty(),
            selectedCount = currentSelection.size
        )
    }

    fun clearSelection() {
        _selectedEmails.value = emptySet()
        _uiState.value = _uiState.value.copy(
            hasSelection = false,
            selectedCount = 0
        )
    }

    fun markSelectedAsRead() {
        val selectedIds = _selectedEmails.value.map { it.toLong() }
        markEmailsAsRead(selectedIds)
    }

    fun markSelectedAsUnread() {
        val selectedIds = _selectedEmails.value.map { it.toLong() }
        markEmailsAsUnread(selectedIds)
    }

    fun deleteSelectedEmails() {
        val selectedIds = _selectedEmails.value.map { it.toLong() }
        deleteEmails(selectedIds)
    }

    fun starSelectedEmails() {
        val selectedIds = _selectedEmails.value.map { it.toLong() }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                batchEmailOperationsUseCase.starEmails(selectedIds, true).collect { result ->
                    when (result) {
                        is BatchOperationResult.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Starred ${result.successCount} emails"
                            )
                        }
                        is BatchOperationResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        is BatchOperationResult.Progress -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.PartialSuccess -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Starred ${result.successCount} emails"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun archiveSelectedEmails() {
        val selectedIds = _selectedEmails.value.map { it.toLong() }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                batchEmailOperationsUseCase.archiveEmails(selectedIds).collect { result ->
                    when (result) {
                        is BatchOperationResult.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Archived ${result.successCount} emails"
                            )
                        }
                        is BatchOperationResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        is BatchOperationResult.Progress -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.PartialSuccess -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Archived ${result.successCount} emails"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun markSelectedAsSpam() {
        val selectedIds = _selectedEmails.value.map { it.toLong() }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                batchEmailOperationsUseCase.markEmailsAsSpam(selectedIds).collect { result ->
                    when (result) {
                        is BatchOperationResult.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Marked ${result.successCount} emails as spam"
                            )
                        }
                        is BatchOperationResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        is BatchOperationResult.Progress -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.PartialSuccess -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Marked ${result.successCount} emails as spam"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun selectAllEmails(emailIds: List<String>) {
        _selectedEmails.value = emailIds.toSet()
        _uiState.value = _uiState.value.copy(
            hasSelection = emailIds.isNotEmpty(),
            selectedCount = emailIds.size
        )
    }

    fun moveSelectedEmailsToFolder(folderId: Long) {
        val selectedIds = _selectedEmails.value.map { it.toLong() }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                batchEmailOperationsUseCase.moveEmailsToFolder(selectedIds, folderId).collect { result ->
                    when (result) {
                        is BatchOperationResult.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Moved ${result.successCount} emails"
                            )
                        }
                        is BatchOperationResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        is BatchOperationResult.Progress -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is BatchOperationResult.PartialSuccess -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Moved ${result.successCount} emails"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    data class BatchEmailOperationsUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,
        val hasSelection: Boolean = false,
        val selectedCount: Int = 0,
        val progress: Int = 0,
        val totalProgress: Int = 0
    )
}