package com.gf.mail.data.repository

import com.gf.mail.data.local.dao.AttachmentDao
import com.gf.mail.data.local.entity.AttachmentEntity
import com.gf.mail.domain.model.EmailAttachment
import com.gf.mail.domain.repository.AttachmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AttachmentRepository
 */
@Singleton
class AttachmentRepositoryImpl @Inject constructor(
    private val attachmentDao: AttachmentDao
) : AttachmentRepository {

    override fun getAttachmentsForEmail(emailId: String): Flow<List<EmailAttachment>> {
        return attachmentDao.getAttachmentsByEmailFlow(emailId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override suspend fun getAttachmentsForEmailSync(emailId: String): List<EmailAttachment> {
        return attachmentDao.getAttachmentsByEmail(emailId).map { it.toDomainModel() }
    }

    override suspend fun getAttachmentById(attachmentId: String): EmailAttachment? {
        return attachmentDao.getAttachmentById(attachmentId)?.toDomainModel()
    }

    override suspend fun saveAttachment(attachment: EmailAttachment): EmailAttachment {
        // Use attachment ID as emailId for now - this should be improved
        val entity = attachment.toEntity(attachment.id)
        attachmentDao.insertAttachment(entity)
        return attachment
    }

    override suspend fun updateAttachment(attachment: EmailAttachment): EmailAttachment {
        // Use attachment ID as emailId for now - this should be improved
        val entity = attachment.toEntity(attachment.id)
        attachmentDao.updateAttachment(entity)
        return attachment
    }

    override suspend fun deleteAttachment(attachmentId: String) {
        attachmentDao.deleteAttachmentById(attachmentId)
    }

    override suspend fun deleteAttachmentsForEmail(emailId: String) {
        attachmentDao.deleteAttachmentsByEmail(emailId)
    }

    override suspend fun getTotalAttachmentSize(emailId: String): Long {
        return attachmentDao.getTotalAttachmentSize(emailId)
    }

    override suspend fun getAttachmentByFilePath(filePath: String): EmailAttachment? {
        return attachmentDao.getAttachmentByFilePath(filePath)?.toDomainModel()
    }

    override suspend fun markAsDownloaded(attachmentId: String) {
        attachmentDao.updateDownloadComplete(
            id = attachmentId,
            is_downloaded = true,
            local_file_path = null,
            download_status = "COMPLETED",
            progress = 100
        )
    }

    override fun getUndownloadedAttachments(): Flow<List<EmailAttachment>> {
        return flow {
            val entities = attachmentDao.getAttachmentsByDownloadStatus("PENDING")
            emit(entities.map { it.toDomainModel() })
        }
    }

    override suspend fun cleanupOrphanedAttachments(): Int {
        val beforeCount = attachmentDao.getDownloadedAttachmentCount()
        attachmentDao.deleteOrphanedAttachments()
        val afterCount = attachmentDao.getDownloadedAttachmentCount()
        return beforeCount - afterCount
    }

    override suspend fun searchAttachments(query: String): List<EmailAttachment> {
        return attachmentDao.searchAttachments(query).map { it.toDomainModel() }
    }

    override suspend fun getAttachmentsByMimeType(mimeType: String): List<EmailAttachment> {
        return attachmentDao.getAttachmentsByMimeType(mimeType).map { it.toDomainModel() }
    }

    override suspend fun getImageAttachments(): List<EmailAttachment> {
        return attachmentDao.getImageAttachments().map { it.toDomainModel() }
    }

    override suspend fun getDocumentAttachments(): List<EmailAttachment> {
        return attachmentDao.getDocumentAttachments().map { it.toDomainModel() }
    }

    /**
     * Convert domain model to entity
     */
    private fun EmailAttachment.toEntity(emailId: String): AttachmentEntity {
        return AttachmentEntity(
            id = this.id,
            emailId = emailId,
            filename = this.fileName,
            displayName = this.fileName,
            mimeType = this.mimeType,
            size = this.size,
            localFilePath = this.uri?.toString(),
            downloadUrl = null,
            contentId = this.contentId,
            isInline = this.isInline,
            isDownloaded = false,
            downloadStatus = "PENDING",
            downloadProgress = 0,
            checksum = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Convert entity to domain model
     */
    private fun AttachmentEntity.toDomainModel(): EmailAttachment {
        return EmailAttachment(
            id = this.id,
            fileName = this.filename,
            mimeType = this.mimeType,
            size = this.size,
            uri = this.localFilePath?.let { android.net.Uri.parse(it) },
            contentId = this.contentId,
            isInline = this.isInline
        )
    }
    
    // TODO: Implement getAttachmentsByIds when Attachment model is available
    // override suspend fun getAttachmentsByIds(attachmentIds: List<String>): List<com.gf.mail.domain.model.Attachment> {
    //     return emptyList()
    // }
}
