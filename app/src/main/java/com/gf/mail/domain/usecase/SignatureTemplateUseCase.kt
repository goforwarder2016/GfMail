package com.gf.mail.domain.usecase

import com.gf.mail.domain.model.EmailSignature
import com.gf.mail.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing signature templates
 */
class SignatureTemplateUseCase(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Get signature templates
     */
    suspend fun getSignatureTemplates(): List<EmailSignature> {
        return settingsRepository.getSignatureTemplates()
    }

    /**
     * Create signature template
     */
    suspend fun createSignatureTemplate(template: EmailSignature): String {
        return settingsRepository.createSignature(template)
    }

    /**
     * Update signature template
     */
    suspend fun updateSignatureTemplate(template: EmailSignature) {
        settingsRepository.updateSignature(template)
    }

    /**
     * Delete signature template
     */
    suspend fun deleteSignatureTemplate(templateId: String) {
        settingsRepository.deleteSignature(templateId)
    }
}