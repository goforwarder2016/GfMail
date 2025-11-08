package com.gf.mail.domain.usecase

import com.gf.mail.domain.model.EmailSignature
import com.gf.mail.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing email signatures
 */
class ManageEmailSignaturesUseCase(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Get all email signatures
     */
    suspend fun getEmailSignatures(): Flow<List<EmailSignature>> {
        return settingsRepository.getAllSignaturesFlow()
    }

    /**
     * Add a new email signature
     */
    suspend fun addEmailSignature(signature: EmailSignature): Long {
        val signatureId = settingsRepository.createSignature(signature)
        return signatureId.toLongOrNull() ?: 0L
    }

    /**
     * Update an existing email signature
     */
    suspend fun updateEmailSignature(signature: EmailSignature) {
        // TODO: Implement update email signature
    }

    /**
     * Delete an email signature
     */
    suspend fun deleteEmailSignature(signatureId: Long) {
        // TODO: Implement delete email signature
    }
}