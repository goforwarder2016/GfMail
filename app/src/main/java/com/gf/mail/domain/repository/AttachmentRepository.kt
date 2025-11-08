package com.gf.mail.domain.repository

import com.gf.mail.domain.model.EmailAttachment
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for email attachments
 */
interface AttachmentRepository {

    /**
     * Get attachments for email as Flow
     */
    fun getAttachmentsForEmail(emailId: String): Flow<List<EmailAttachment>>

    /**
     * Get attachments for email synchronously
     */
    suspend fun getAttachmentsForEmailSync(emailId: String): List<EmailAttachment>

    /**
     * Get attachment by ID
     */
    suspend fun getAttachmentById(attachmentId: String): EmailAttachment?

    /**
     * Save attachment
     */
    suspend fun saveAttachment(attachment: EmailAttachment): EmailAttachment

    /**
     * Update attachment
     */
    suspend fun updateAttachment(attachment: EmailAttachment): EmailAttachment

    /**
     * Delete attachment by ID
     */
    suspend fun deleteAttachment(attachmentId: String)

    /**
     * Delete all attachments for email
     */
    suspend fun deleteAttachmentsForEmail(emailId: String)

    /**
     * Get total attachment size for email
     */
    suspend fun getTotalAttachmentSize(emailId: String): Long

    /**
     * Get attachment by file path
     */
    suspend fun getAttachmentByFilePath(filePath: String): EmailAttachment?

    /**
     * Mark attachment as downloaded
     */
    suspend fun markAsDownloaded(attachmentId: String)

    /**
     * Get undownloaded attachments
     */
    fun getUndownloadedAttachments(): Flow<List<EmailAttachment>>

    /**
     * Clean up orphaned attachments
     */
    suspend fun cleanupOrphanedAttachments(): Int

    /**
     * Search attachments
     */
    suspend fun searchAttachments(query: String): List<EmailAttachment>

    /**
     * Get attachments by MIME type
     */
    suspend fun getAttachmentsByMimeType(mimeType: String): List<EmailAttachment>

    /**
     * Get image attachments
     */
    suspend fun getImageAttachments(): List<EmailAttachment>

    /**
     * Get document attachments
     */
    suspend fun getDocumentAttachments(): List<EmailAttachment>
}

    
    /**
     * Get attachments by their IDs
     */
    suspend fun getAttachmentsByIds(attachmentIds: List<String>): List<com.gf.mail.domain.model.Attachment> {
        return emptyList() // TODO: Implement when Attachment model is available
    }
