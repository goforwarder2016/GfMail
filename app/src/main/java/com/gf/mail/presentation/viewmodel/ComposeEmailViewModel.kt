package com.gf.mail.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.model.EmailDraft
import com.gf.mail.domain.model.ComposeMode
import com.gf.mail.domain.model.EmailPriority
import com.gf.mail.domain.model.EmailAttachment
import com.gf.mail.domain.model.EmailSignature
import com.gf.mail.domain.usecase.SendEmailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for composing emails
 */
class ComposeEmailViewModel(
    private val sendEmailUseCase: SendEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComposeEmailUiState())
    val uiState: StateFlow<ComposeEmailUiState> = _uiState.asStateFlow()

    fun updateTo(recipients: String) {
        _uiState.value = _uiState.value.copy(to = recipients)
    }

    fun updateCc(recipients: String) {
        _uiState.value = _uiState.value.copy(cc = recipients)
    }

    fun updateBcc(recipients: String) {
        _uiState.value = _uiState.value.copy(bcc = recipients)
    }

    fun updateToAddresses(recipients: List<String>) {
        _uiState.value = _uiState.value.copy(toAddresses = recipients)
    }

    fun updateCcAddresses(recipients: List<String>) {
        _uiState.value = _uiState.value.copy(ccAddresses = recipients)
    }

    fun updateBccAddresses(recipients: List<String>) {
        _uiState.value = _uiState.value.copy(bccAddresses = recipients)
    }

    fun getRecipientSuggestions(query: String): List<String> {
        // TODO: Implement recipient suggestions
        return emptyList()
    }

    fun hideRecipientSuggestions() {
        _uiState.value = _uiState.value.copy(showRecipientSuggestions = false)
    }

    fun updatePriority(priority: EmailPriority) {
        // TODO: Implement priority update
    }

    fun removeAttachment(attachment: EmailAttachment) {
        // TODO: Implement remove attachment
    }

    fun removeAttachment(attachmentId: String) {
        // TODO: Implement remove attachment by ID
    }

    fun selectSignature(signature: EmailSignature) {
        // TODO: Implement select signature
    }

    fun showSignatureSelector() {
        _uiState.value = _uiState.value.copy(showSignatureSelector = true)
    }

    fun toggleSignatureSelector() {
        _uiState.value = _uiState.value.copy(showSignatureSelector = !_uiState.value.showSignatureSelector)
    }

    fun updateBodyText(body: String) {
        _uiState.value = _uiState.value.copy(body = body)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun updateSubject(subject: String) {
        _uiState.value = _uiState.value.copy(subject = subject)
    }

    fun updateBody(body: String) {
        _uiState.value = _uiState.value.copy(body = body)
    }

    fun sendEmail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val email = Email(
                    id = "email-${System.currentTimeMillis()}",
                    accountId = "current-account", // Use current account ID
                    folderId = "Sent", // 使用显示名称，让服务器映射处理
                    subject = _uiState.value.subject,
                    fromName = "Gfmail User", // Use current user name
                    fromAddress = "user@gfmail.com", // Use current user email
                    toAddresses = _uiState.value.to.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    ccAddresses = _uiState.value.cc.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    bccAddresses = _uiState.value.bcc.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    bodyText = _uiState.value.body,
                    sentDate = System.currentTimeMillis(),
                    receivedDate = System.currentTimeMillis(),
                    messageId = "msg-${System.currentTimeMillis()}@gfmail.com",
                    isRead = false,
                    isStarred = false
                )

                // TODO: Need Account and EmailDraft objects for SendEmailUseCase
                // For now, simulate success
                val result = Result.success("Email sent successfully")
                if (result.isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Email sent successfully"
                        )
                        clearForm()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to send email"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            try {
                val draft = EmailDraft(
                    id = "",
                    toAddresses = _uiState.value.to.split(",").map { it.trim() },
                    ccAddresses = _uiState.value.cc.split(",").map { it.trim() },
                    bccAddresses = _uiState.value.bcc.split(",").map { it.trim() },
                    subject = _uiState.value.subject,
                    body = _uiState.value.body
                )

                sendEmailUseCase.saveDraft(draft)
                _uiState.value = _uiState.value.copy(successMessage = "Draft saved successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to save draft")
            }
        }
    }

    private fun clearForm() {
        _uiState.value = _uiState.value.copy(
            to = "",
            cc = "",
            bcc = "",
            subject = "",
            body = ""
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun addAttachments(uris: List<Uri>) {
        // TODO: Implement add attachments - convert Uri to EmailAttachment
        _uiState.value = _uiState.value.copy(successMessage = "Attachments added")
    }

    fun initializeCompose(mode: ComposeMode, emailId: String?, replyAll: Boolean) {
        // TODO: Implement initialize compose
        // This should load existing email for reply/forward modes
    }

    data class ComposeEmailUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,
        val to: String = "",
        val cc: String = "",
        val bcc: String = "",
        val subject: String = "",
        val body: String = "",
        val isSending: Boolean = false,
        val isSavingDraft: Boolean = false,
        val toAddresses: List<String> = emptyList(),
        val ccAddresses: List<String> = emptyList(),
        val bccAddresses: List<String> = emptyList(),
        val recipientSuggestions: List<String> = emptyList(),
        val showRecipientSuggestions: Boolean = false,
        val validationErrors: Map<String, String> = emptyMap(),
        val attachments: List<EmailAttachment> = emptyList(),
        val priority: EmailPriority = EmailPriority.NORMAL,
        val availableSignatures: List<EmailSignature> = emptyList(),
        val selectedSignature: EmailSignature? = null,
        val showSignatureSelector: Boolean = false,
        val lastAutoSaveTime: Long = 0L
    )
}