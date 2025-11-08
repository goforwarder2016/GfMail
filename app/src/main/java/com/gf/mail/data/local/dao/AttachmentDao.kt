package com.gf.mail.data.local.dao

import androidx.room.*
import com.gf.mail.data.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    // Basic CRUD Operations
    @Query("SELECT * FROM attachments WHERE id = :id")
    suspend fun getAttachmentById(id: String): AttachmentEntity?

    
    @Query("SELECT * FROM attachments WHERE id IN (:ids)")
    suspend fun getAttachmentsByIds(ids: List<String>): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE emailId = :emailId ORDER BY filename ASC")
    suspend fun getAttachmentsByEmail(emailId: String): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE emailId = :emailId ORDER BY filename ASC")
    fun getAttachmentsByEmailFlow(emailId: String): Flow<List<AttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<AttachmentEntity>)

    @Update
    suspend fun updateAttachment(attachment: AttachmentEntity)

    @Delete
    suspend fun deleteAttachment(attachment: AttachmentEntity)

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteAttachmentById(id: String)

    @Query("DELETE FROM attachments WHERE emailId = :emailId")
    suspend fun deleteAttachmentsByEmail(emailId: String)

    // Download status management
    @Query("SELECT * FROM attachments WHERE download_status = :status")
    suspend fun getAttachmentsByDownloadStatus(status: String): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE is_downloaded = 1")
    suspend fun getDownloadedAttachments(): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE is_downloaded = 0")
    suspend fun getPendingDownloads(): List<AttachmentEntity>

    @Query("UPDATE attachments SET download_status = :status WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, status: String)

    @Query("UPDATE attachments SET download_progress = :progress WHERE id = :id")
    suspend fun updateDownloadProgress(id: String, progress: Int)

    @Query(
        """
        UPDATE attachments 
        SET is_downloaded = :is_downloaded,
            local_file_path = :local_file_path,
            download_status = :download_status,
            download_progress = :progress,
            updated_at = :updated_at
        WHERE id = :id
    """
    )
    suspend fun updateDownloadComplete(
        id: String,
        is_downloaded: Boolean,
        local_file_path: String?,
        download_status: String,
        progress: Int,
        updated_at: Long = System.currentTimeMillis()
    )

    // File management
    @Query("SELECT * FROM attachments WHERE local_file_path IS NOT NULL")
    suspend fun getAttachmentsWithLocalFiles(): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE local_file_path = :filePath LIMIT 1")
    suspend fun getAttachmentByFilePath(filePath: String): AttachmentEntity?

    @Query("UPDATE attachments SET local_file_path = :filePath WHERE id = :id")
    suspend fun updateLocalFilePath(id: String, filePath: String?)

    // Size and storage management
    @Query("SELECT SUM(size) FROM attachments WHERE is_downloaded = 1")
    suspend fun getTotalDownloadedSize(): Long

    @Query("SELECT SUM(size) FROM attachments WHERE emailId = :emailId")
    suspend fun getTotalAttachmentSize(emailId: String): Long

    @Query("SELECT * FROM attachments WHERE size > :sizeLimit ORDER BY size DESC")
    suspend fun getLargeAttachments(sizeLimit: Long): List<AttachmentEntity>

    // Inline attachments
    @Query("SELECT * FROM attachments WHERE emailId = :emailId AND is_inline = 1")
    suspend fun getInlineAttachments(emailId: String): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE emailId = :emailId AND is_inline = 0")
    suspend fun getRegularAttachments(emailId: String): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE content_id = :content_id LIMIT 1")
    suspend fun getAttachmentByContentId(content_id: String): AttachmentEntity?

    // MIME type queries
    @Query("SELECT * FROM attachments WHERE mime_type = :mime_type")
    suspend fun getAttachmentsByMimeType(mime_type: String): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE mime_type LIKE :mime_typePattern")
    suspend fun getAttachmentsByMimeTypePattern(mime_typePattern: String): List<AttachmentEntity>

    // Get all image attachments
    @Query("SELECT * FROM attachments WHERE mime_type LIKE 'image/%'")
    suspend fun getImageAttachments(): List<AttachmentEntity>

    // Get all document attachments
    @Query(
        "SELECT * FROM attachments WHERE mime_type IN ('application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document')"
    )
    suspend fun getDocumentAttachments(): List<AttachmentEntity>

    // Search functionality
    @Query(
        """
        SELECT * FROM attachments 
        WHERE filename LIKE '%' || :query || '%' 
           OR display_name LIKE '%' || :query || '%'
        ORDER BY filename ASC
    """
    )
    suspend fun searchAttachments(query: String): List<AttachmentEntity>

    @Query(
        """
        SELECT attachments.* FROM attachments
        INNER JOIN emails ON attachments.emailId = emails.id
        WHERE emails.account_id = :accountId
        AND (attachments.filename LIKE '%' || :query || '%' 
             OR attachments.display_name LIKE '%' || :query || '%')
        ORDER BY attachments.filename ASC
    """
    )
    suspend fun searchAttachmentsByAccount(accountId: String, query: String): List<AttachmentEntity>

    // Statistics and aggregation
    @Query("SELECT COUNT(*) FROM attachments WHERE emailId = :emailId")
    suspend fun getAttachmentCount(emailId: String): Int

    @Query("SELECT COUNT(*) FROM attachments WHERE is_downloaded = 1")
    suspend fun getDownloadedAttachmentCount(): Int

    @Query("SELECT COUNT(*) FROM attachments WHERE download_status = 'DOWNLOADING'")
    suspend fun getActiveDownloadCount(): Int

    @Query(
        """
        SELECT mime_type, COUNT(*) as count 
        FROM attachments 
        GROUP BY mime_type 
        ORDER BY count DESC
    """
    )
    suspend fun getAttachmentCountsByMimeType(): List<MimeTypeCount>

    // Cleanup operations
    @Query("DELETE FROM attachments WHERE emailId NOT IN (SELECT id FROM emails)")
    suspend fun deleteOrphanedAttachments()

    @Query(
        """
        DELETE FROM attachments 
        WHERE is_downloaded = 1 
        AND local_file_path IS NOT NULL 
        AND updated_at < :beforeTimestamp
    """
    )
    suspend fun deleteOldDownloadedAttachments(beforeTimestamp: Long)

    // Batch operations for performance
    @Query("UPDATE attachments SET download_status = :status WHERE id IN (:attachmentIds)")
    suspend fun updateDownloadStatusBatch(attachmentIds: List<String>, status: String)

    @Query("DELETE FROM attachments WHERE id IN (:attachmentIds)")
    suspend fun deleteAttachments(attachmentIds: List<String>)

    // Integrity and validation
    @Query("SELECT * FROM attachments WHERE checksum = :checksum LIMIT 1")
    suspend fun getAttachmentByChecksum(checksum: String): AttachmentEntity?

    @Query("UPDATE attachments SET checksum = :checksum WHERE id = :id")
    suspend fun updateChecksum(id: String, checksum: String)

    // Data transfer objects
    data class MimeTypeCount(
        val mime_type: String,
        val count: Int
    )

    data class AttachmentWithEmail(
        val id: String,
        val emailId: String,
        val filename: String,
        val mime_type: String,
        val size: Long,
        val is_downloaded: Boolean,
        val emailSubject: String,
        val emailFrom: String
    )
}
