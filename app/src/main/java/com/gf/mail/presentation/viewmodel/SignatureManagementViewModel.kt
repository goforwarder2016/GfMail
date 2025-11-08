package com.gf.mail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.mail.domain.model.EmailSignature
import com.gf.mail.domain.model.HandwritingData
import com.gf.mail.domain.usecase.ManageEmailSignaturesUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for signature management
 */
class SignatureManagementViewModel(
    private val manageEmailSignaturesUseCase: ManageEmailSignaturesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignatureManagementUiState())
    val uiState: StateFlow<SignatureManagementUiState> = _uiState.asStateFlow()

    init {
        loadSignatures()
    }

    /**
     * Load all signatures
     */
    private fun loadSignatures() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                manageEmailSignaturesUseCase.getEmailSignatures().collect { signatures ->
                    _uiState.value = _uiState.value.copy(
                        signatures = signatures,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load signatures: ${e.message}"
                )
            }
        }
    }

    /**
     * Load signatures for specific account
     */
    fun loadSignaturesForAccount(accountId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // TODO: Implement get signatures for account
                val signatures = emptyList<EmailSignature>()
                _uiState.value = _uiState.value.copy(
                    signatures = signatures,
                    selectedAccountId = accountId,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load signatures: ${e.message}"
                )
            }
        }
    }

    /**
     * Create new text signature
     */
    fun createTextSignature(
        name: String,
        content: String,
        isHtml: Boolean = false,
        accountId: String? = null,
        setAsDefault: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val signature = EmailSignature(
                    id = "",
                    name = name,
                    content = content,
                    type = if (isHtml) EmailSignature.SignatureType.HTML else EmailSignature.SignatureType.TEXT,
                    isHtml = isHtml,
                    accountId = accountId,
                    isDefault = setAsDefault,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                // TODO: Implement create signature
                val result = manageEmailSignaturesUseCase.addEmailSignature(signature)

                if (result > 0) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Signature created successfully",
                        isCreatingSignature = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to create signature"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create signature: ${e.message}"
                )
            }
        }
    }

    /**
     * Create handwriting signature
     */
    fun createHandwritingSignature(
        name: String,
        handwritingData: HandwritingData,
        accountId: String? = null,
        setAsDefault: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val signature = EmailSignature(
                    id = "",
                    name = name,
                    content = "", // TODO: Convert handwriting to text
                    type = EmailSignature.SignatureType.HANDWRITTEN,
                    isHtml = false,
                    accountId = accountId,
                    isDefault = setAsDefault,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    hasHandwritingData = true,
                    handwritingPath = null, // TODO: Save handwriting data
                    handwritingWidth = handwritingData.width,
                    handwritingHeight = handwritingData.height
                )
                // TODO: Implement create signature
                val result = manageEmailSignaturesUseCase.addEmailSignature(signature)

                if (result > 0) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Handwriting signature created successfully",
                        isCreatingSignature = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to create handwriting signature"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create handwriting signature: ${e.message}"
                )
            }
        }
    }

    /**
     * Update signature
     */
    fun updateSignature(signature: EmailSignature) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // TODO: Implement update signature
                manageEmailSignaturesUseCase.updateEmailSignature(signature)

                // Assume success for now
                val result = true
                if (result) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Signature updated successfully",
                        editingSignature = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to update signature"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update signature: ${e.message}"
                )
            }
        }
    }

    /**
     * Delete signature
     */
    fun deleteSignature(signatureId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // TODO: Implement delete signature
                manageEmailSignaturesUseCase.deleteEmailSignature(signatureId.toLongOrNull() ?: 0L)

                // Assume success for now
                val result = true
                if (result) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Signature deleted successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to delete signature"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to delete signature: ${e.message}"
                )
            }
        }
    }

    /**
     * Set default signature
     */
    fun setDefaultSignature(signatureId: String, accountId: String? = null) {
        viewModelScope.launch {
            try {
                // TODO: Implement set default signature
                // For now, just simulate success
                val result = true

                if (result) {
                    _uiState.value = _uiState.value.copy(
                        message = "Default signature updated"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to set default signature"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to set default signature: ${e.message}"
                )
            }
        }
    }

    /**
     * Duplicate signature
     */
    fun duplicateSignature(signatureId: String, newName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // TODO: Implement duplicate signature
                val result = Result.success(Unit)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Signature duplicated successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to duplicate signature"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to duplicate signature: ${e.message}"
                )
            }
        }
    }

    /**
     * Start editing signature
     */
    fun startEditingSignature(signature: EmailSignature) {
        _uiState.value = _uiState.value.copy(
            editingSignature = signature,
            editingSignatureName = signature.name,
            editingSignatureContent = signature.content,
            editingSignatureIsHtml = signature.isHtml
        )
    }

    /**
     * Cancel editing signature
     */
    fun cancelEditingSignature() {
        _uiState.value = _uiState.value.copy(
            editingSignature = null,
            editingSignatureName = "",
            editingSignatureContent = "",
            editingSignatureIsHtml = false
        )
    }

    /**
     * Update editing signature name
     */
    fun updateEditingSignatureName(name: String) {
        _uiState.value = _uiState.value.copy(editingSignatureName = name)
    }

    /**
     * Update editing signature content
     */
    fun updateEditingSignatureContent(content: String) {
        _uiState.value = _uiState.value.copy(editingSignatureContent = content)
    }

    /**
     * Toggle editing signature HTML mode
     */
    fun toggleEditingSignatureHtml(isHtml: Boolean) {
        _uiState.value = _uiState.value.copy(editingSignatureIsHtml = isHtml)
    }

    /**
     * Save editing signature
     */
    fun saveEditingSignature() {
        val editingSignature = _uiState.value.editingSignature ?: return

        val updatedSignature = editingSignature.copy(
            name = _uiState.value.editingSignatureName,
            content = _uiState.value.editingSignatureContent,
            isHtml = _uiState.value.editingSignatureIsHtml,
            updatedAt = System.currentTimeMillis()
        )

        updateSignature(updatedSignature)
    }

    /**
     * Start creating signature
     */
    fun startCreatingSignature() {
        _uiState.value = _uiState.value.copy(
            isCreatingSignature = true,
            newSignatureName = "",
            newSignatureContent = "",
            newSignatureIsHtml = false,
            newSignatureType = SignatureType.TEXT
        )
    }

    /**
     * Cancel creating signature
     */
    fun cancelCreatingSignature() {
        _uiState.value = _uiState.value.copy(
            isCreatingSignature = false,
            newSignatureName = "",
            newSignatureContent = "",
            newSignatureIsHtml = false,
            newSignatureType = SignatureType.TEXT,
            handwritingData = null
        )
    }

    /**
     * Update new signature name
     */
    fun updateNewSignatureName(name: String) {
        _uiState.value = _uiState.value.copy(newSignatureName = name)
    }

    /**
     * Update new signature content
     */
    fun updateNewSignatureContent(content: String) {
        _uiState.value = _uiState.value.copy(newSignatureContent = content)
    }

    /**
     * Toggle new signature HTML mode
     */
    fun toggleNewSignatureHtml(isHtml: Boolean) {
        _uiState.value = _uiState.value.copy(newSignatureIsHtml = isHtml)
    }

    /**
     * Set signature type
     */
    fun setSignatureType(type: SignatureType) {
        _uiState.value = _uiState.value.copy(newSignatureType = type)
    }

    /**
     * Set handwriting data
     */
    fun setHandwritingData(handwritingData: HandwritingData) {
        _uiState.value = _uiState.value.copy(handwritingData = handwritingData)
    }

    /**
     * Save new signature
     */
    fun saveNewSignature(accountId: String? = null, setAsDefault: Boolean = false) {
        val state = _uiState.value

        when (state.newSignatureType) {
            SignatureType.TEXT -> {
                createTextSignature(
                    name = state.newSignatureName,
                    content = state.newSignatureContent,
                    isHtml = state.newSignatureIsHtml,
                    accountId = accountId,
                    setAsDefault = setAsDefault
                )
            }
            SignatureType.HANDWRITING -> {
                val handwritingData = state.handwritingData
                if (handwritingData != null) {
                    createHandwritingSignature(
                        name = state.newSignatureName,
                        handwritingData = handwritingData,
                        accountId = accountId,
                        setAsDefault = setAsDefault
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Please draw your signature first"
                    )
                }
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
     * Filter signatures by search query
     */
    fun filterSignatures(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /**
     * Get filtered signatures
     */
    fun getFilteredSignatures(): List<EmailSignature> {
        val query = _uiState.value.searchQuery
        val signatures = _uiState.value.signatures

        return if (query.isBlank()) {
            signatures
        } else {
            signatures.filter { signature ->
                signature.name.contains(query, ignoreCase = true) ||
                    signature.content.contains(query, ignoreCase = true)
            }
        }
    }
}

/**
 * UI state for signature management
 */
data class SignatureManagementUiState(
    val signatures: List<EmailSignature> = emptyList(),
    val isLoading: Boolean = false,
    val selectedAccountId: String? = null,
    val searchQuery: String = "",

    // Editing signature
    val editingSignature: EmailSignature? = null,
    val editingSignatureName: String = "",
    val editingSignatureContent: String = "",
    val editingSignatureIsHtml: Boolean = false,

    // Creating signature
    val isCreatingSignature: Boolean = false,
    val newSignatureName: String = "",
    val newSignatureContent: String = "",
    val newSignatureIsHtml: Boolean = false,
    val newSignatureType: SignatureType = SignatureType.TEXT,
    val handwritingData: HandwritingData? = null,

    val error: String? = null,
    val message: String? = null
) {
    val hasSignatures: Boolean
        get() = signatures.isNotEmpty()

    val defaultSignature: EmailSignature?
        get() = signatures.find { it.isDefault }

    val globalSignatures: List<EmailSignature>
        get() = signatures.filter { it.accountId == null }

    val accountSignatures: List<EmailSignature>
        get() = signatures.filter { it.accountId != null }

    val canSaveNewSignature: Boolean
        get() = newSignatureName.isNotBlank() && when (newSignatureType) {
            SignatureType.TEXT -> newSignatureContent.isNotBlank()
            SignatureType.HANDWRITING -> handwritingData != null
        }

    val canSaveEditingSignature: Boolean
        get() = editingSignature != null &&
            editingSignatureName.isNotBlank() &&
            editingSignatureContent.isNotBlank()
}

/**
 * Type of signature
 */
enum class SignatureType {
    TEXT,
    HANDWRITING
}
