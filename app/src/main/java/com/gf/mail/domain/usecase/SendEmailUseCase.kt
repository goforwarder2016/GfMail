package com.gf.mail.domain.usecase

import com.gf.mail.data.email.SmtpClient
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.EmailAttachment
import com.gf.mail.domain.model.EmailDraft
import com.gf.mail.domain.repository.EmailRepository

/**
 * Use case for sending emails
 */
class SendEmailUseCase(
    private val smtpClient: SmtpClient,
    private val emailRepository: EmailRepository
) {
    /**
     * Send an email
     */
    suspend operator fun invoke(
        account: Account,
        draft: EmailDraft,
        attachments: List<EmailAttachment> = emptyList()
    ): Result<String> {
        return try {
            // TODO: Implement send email
            val result = smtpClient.sendEmail(account, draft, attachments)
            if (result.isSuccess) {
                // TODO: Save draft as sent
                // emailRepository.saveDraft(draft)
                Result.success("Email sent successfully")
            } else {
                Result.failure(Exception("Failed to send email"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save email as draft
     */
    suspend fun saveDraft(draft: EmailDraft): Long {
        // TODO: Implement save draft
        return 1L
    }
}