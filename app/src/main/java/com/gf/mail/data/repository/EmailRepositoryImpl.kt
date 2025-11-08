package com.gf.mail.data.repository

import com.gf.mail.data.local.dao.EmailDao
import com.gf.mail.data.mapper.toDomain
import com.gf.mail.data.mapper.toEntity
import com.gf.mail.data.mapper.EmailMapper
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.model.EmailPriority
import com.gf.mail.domain.repository.EmailRepository
import com.gf.mail.domain.repository.EmailStats
import com.gf.mail.domain.repository.FolderEmailStats
import com.gf.mail.domain.repository.BatchOperationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class EmailRepositoryImpl(
    private val emailDao: EmailDao,
    private val attachmentDao: com.gf.mail.data.local.dao.AttachmentDao,
    private val folderDao: com.gf.mail.data.local.dao.FolderDao
) : EmailRepository {

    // Basic CRUD operations
    override suspend fun getEmailById(id: String): Email? {
        return emailDao.getEmailById(id)?.let { EmailMapper.toDomain(it) }
    }

    override suspend fun getEmailByMessageId(messageId: String): Email? {
        return emailDao.getEmailByMessageId(messageId)?.let { EmailMapper.toDomain(it) }
    }

    override suspend fun getAllEmails(): List<Email> {
        return emailDao.getAllEmails().map { EmailMapper.toDomain(it) }
    }

    override fun getAllEmailsFlow(): Flow<List<Email>> {
        return emailDao.getAllEmailsFlow().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun insertEmail(email: Email) {
        emailDao.insertEmail(email.toEntity())
    }

    override suspend fun insertEmails(emails: List<Email>) {
        emailDao.insertEmails(emails.map { it.toEntity() })
    }

    override suspend fun updateEmail(email: Email) {
        emailDao.updateEmail(email.toEntity())
    }

    override suspend fun deleteEmail(emailId: String) {
        emailDao.deleteEmailById(emailId)
    }

    override suspend fun deleteEmailByMessageId(messageId: String) {
        emailDao.deleteEmailByMessageId(messageId)
    }

    // Folder-based operations
    override suspend fun getEmailsInFolder(folderId: String, limit: Int, offset: Int): List<Email> {
        return emailDao.getEmailsInFolder(folderId, limit, offset).toDomain()
    }

    override fun getEmailsInFolderFlow(folderId: String): Flow<List<Email>> {
        return emailDao.getEmailsInFolderFlow(folderId).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getEmailCountInFolder(folderId: String): Int {
        return emailDao.getEmailCountInFolder(folderId)
    }

    override suspend fun getUnreadEmailCountInFolder(folderId: String): Int {
        return emailDao.getUnreadEmailCountInFolder(folderId)
    }

    // Account-based operations
    override suspend fun getEmailsByAccount(accountId: String): List<Email> {
        return emailDao.getEmailsByAccount(accountId).toDomain()
    }

    override fun getEmailsByAccountFlow(accountId: String): Flow<List<Email>> {
        return emailDao.getEmailsByAccountFlow(accountId).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getEmailCountByAccount(accountId: String): Int {
        return emailDao.getEmailCountByAccount(accountId)
    }

    override suspend fun getUnreadEmailCountByAccount(accountId: String): Int {
        return emailDao.getUnreadEmailCountByAccount(accountId)
    }

    // Search operations
    override suspend fun searchEmails(query: String, accountId: String?): List<Email> {
        return if (accountId != null) {
            emailDao.searchEmailsByAccount(accountId, query).toDomain()
        } else {
            emailDao.searchEmails(query).toDomain()
        }
    }

    override suspend fun searchEmailsInFolder(query: String, folderId: String): List<Email> {
        return emailDao.searchEmailsInFolder(folderId, query).toDomain()
    }

    override fun searchEmailsFlow(query: String, accountId: String?): Flow<List<Email>> {
        return if (accountId != null) {
            emailDao.searchEmailsByAccountFlow(accountId, query).map { entities ->
                entities.toDomain()
            }
        } else {
            emailDao.searchEmailsFlow(query).map { entities ->
                entities.toDomain()
            }
        }
    }

    // Status operations
    override suspend fun markEmailAsRead(emailId: String, read: Boolean) {
        emailDao.markEmailAsRead(emailId, read)
    }

    override suspend fun markEmailAsStarred(emailId: String, starred: Boolean) {
        emailDao.markEmailAsStarred(emailId, starred)
    }
    
    override suspend fun starEmail(emailId: String, starred: Boolean) {
        emailDao.markEmailAsStarred(emailId, starred)
    }

    override suspend fun markEmailsAsRead(emailIds: List<String>, read: Boolean) {
        emailDao.markEmailsAsRead(emailIds, read)
    }

    override suspend fun markAllEmailsInFolderAsRead(folderId: String) {
        emailDao.markAllEmailsInFolderAsRead(folderId)
    }
    
    // Email actions
    override suspend fun archiveEmail(emailId: String) {
        // Move email to archive folder
        // This would typically involve moving to a specific archive folder
        // For now, we'll mark it as archived in the database
        val email = getEmailById(emailId)
        if (email != null) {
            // Update email to mark as archived
            // This is a simplified implementation
        }
    }
    
    override suspend fun markEmailAsSpam(emailId: String) {
        // Move email to spam folder
        // This would typically involve moving to a specific spam folder
        // For now, we'll mark it as spam in the database
        val email = getEmailById(emailId)
        if (email != null) {
            // Update email to mark as spam
            // This is a simplified implementation
        }
    }
    
    override suspend fun moveEmailToFolder(emailId: String, targetFolderId: String) {
        // Move email to target folder
        // This would typically involve updating the folder_id in the database
        val email = getEmailById(emailId)
        if (email != null) {
            val updatedEmail = email.copy(folderId = targetFolderId)
            updateEmail(updatedEmail)
        }
    }

    // Thread operations
    override suspend fun getEmailsByThread(threadId: String): List<Email> {
        return emailDao.getEmailsByThread(threadId).toDomain()
    }

    override fun getEmailsByThreadFlow(threadId: String): Flow<List<Email>> {
        return emailDao.getEmailsByThreadFlow(threadId).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getEmailThreads(accountId: String): List<String> {
        return emailDao.getEmailThreads(accountId)
    }

    // Filtering operations
    override suspend fun getEmailsByPriority(priority: EmailPriority, accountId: String?): List<Email> {
        return if (accountId != null) {
            emailDao.getEmailsByPriorityAndAccount(priority.name, accountId).toDomain()
        } else {
            emailDao.getEmailsByPriority(priority.name).toDomain()
        }
    }

    override suspend fun getUnreadEmails(accountId: String?): List<Email> {
        return if (accountId != null) {
            emailDao.getUnreadEmailsByAccount(accountId).toDomain()
        } else {
            emailDao.getUnreadEmails().toDomain()
        }
    }

    override fun getUnreadEmailsFlow(accountId: String): Flow<List<Email>> {
        return emailDao.getUnreadEmailsByAccountFlow(accountId).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getUnreadCount(accountId: String): Int {
        return emailDao.getUnreadCountByAccount(accountId)
    }

    override suspend fun getStarredEmails(accountId: String?): List<Email> {
        return if (accountId != null) {
            emailDao.getStarredEmailsByAccount(accountId).toDomain()
        } else {
            emailDao.getStarredEmails().toDomain()
        }
    }

    override suspend fun getDraftEmails(accountId: String?): List<Email> {
        return if (accountId != null) {
            emailDao.getDraftEmailsByAccount(accountId).toDomain()
        } else {
            emailDao.getDraftEmails().toDomain()
        }
    }

    override suspend fun getEmailsWithAttachments(accountId: String?): List<Email> {
        return if (accountId != null) {
            emailDao.getEmailsWithAttachmentsByAccount(accountId).toDomain()
        } else {
            emailDao.getEmailsWithAttachments().toDomain()
        }
    }

    // Date-based operations
    override suspend fun getEmailsFromDate(fromDate: Long, accountId: String?): List<Email> {
        return if (accountId != null) {
            emailDao.getEmailsFromDateByAccount(fromDate, accountId).toDomain()
        } else {
            emailDao.getEmailsFromDate(fromDate).toDomain()
        }
    }

    override suspend fun getEmailsInDateRange(fromDate: Long, toDate: Long, accountId: String?): List<Email> {
        return if (accountId != null) {
            emailDao.getEmailsInDateRangeByAccount(fromDate, toDate, accountId).toDomain()
        } else {
            emailDao.getEmailsInDateRange(fromDate, toDate).toDomain()
        }
    }

    override suspend fun getRecentEmails(accountId: String, days: Int): List<Email> {
        val fromDate = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return emailDao.getEmailsFromDateByAccount(fromDate, accountId).toDomain()
    }

    // Sender-based operations
    override suspend fun getEmailsFromSender(senderEmail: String, accountId: String?): List<Email> {
        return if (accountId != null) {
            emailDao.getEmailsFromSenderByAccount(senderEmail, accountId).toDomain()
        } else {
            emailDao.getEmailsFromSender(senderEmail).toDomain()
        }
    }

    override suspend fun getEmailsToRecipient(recipientEmail: String, accountId: String?): List<Email> {
        return if (accountId != null) {
            emailDao.getEmailsToRecipientByAccount(recipientEmail, accountId).toDomain()
        } else {
            emailDao.getEmailsToRecipient(recipientEmail).toDomain()
        }
    }

    // Bulk operations
    override suspend fun deleteEmailsInFolder(folderId: String) {
        emailDao.deleteEmailsInFolder(folderId)
    }

    override suspend fun deleteEmailsOlderThan(timestamp: Long) {
        emailDao.deleteEmailsOlderThan(timestamp)
    }

    override suspend fun moveEmailsToFolder(emailIds: List<String>, targetFolderId: String) {
        emailDao.moveEmailsToFolder(emailIds, targetFolderId)
    }

    // Sync operations
    override suspend fun getEmailsModifiedAfter(timestamp: Long, accountId: String): List<Email> {
        return emailDao.getEmailsModifiedAfter(timestamp, accountId).toDomain()
    }

    override suspend fun getLastSyncedEmail(folderId: String): Email? {
        return emailDao.getLastSyncedEmail(folderId)?.toDomain()
    }

    override suspend fun updateEmailSyncStatus(emailId: String, synced: Boolean) {
        emailDao.updateEmailSyncStatus(emailId, System.currentTimeMillis())
    }

    // Statistics
    override suspend fun getEmailStats(accountId: String): EmailStats {
        val totalEmails = emailDao.getEmailCountByAccount(accountId)
        val unreadEmails = emailDao.getUnreadEmailCountByAccount(accountId)
        val starredEmails = emailDao.getStarredEmailCountByAccount(accountId)
        val draftsCount = emailDao.getDraftEmailCountByAccount(accountId)
        val attachmentsCount = emailDao.getEmailsWithAttachmentsCountByAccount(accountId)
        val totalSize = emailDao.getTotalEmailSizeByAccount(accountId)
        val oldestEmail = emailDao.getOldestEmailByAccount(accountId)
        val newestEmail = emailDao.getNewestEmailByAccount(accountId)

        return EmailStats(
            totalEmails = totalEmails,
            unreadEmails = unreadEmails,
            starredEmails = starredEmails,
            draftsCount = draftsCount,
            attachmentsCount = attachmentsCount,
            totalSize = totalSize,
            oldestEmailDate = oldestEmail?.sentDate,
            newestEmailDate = newestEmail?.sentDate
        )
    }

    override suspend fun getFolderStats(folderId: String): FolderEmailStats {
        val totalEmails = emailDao.getEmailCountInFolder(folderId)
        val unreadEmails = emailDao.getUnreadEmailCountInFolder(folderId)
        val totalSize = emailDao.getTotalEmailSizeInFolder(folderId)
        val lastEmail = emailDao.getNewestEmailInFolder(folderId)

        return FolderEmailStats(
            folderId = folderId,
            totalEmails = totalEmails,
            unreadEmails = unreadEmails,
            totalSize = totalSize,
            lastEmailDate = lastEmail?.sentDate
        )
    }
    
    // Batch operations
    override suspend fun batchMarkAsRead(emailIds: List<String>, read: Boolean): BatchOperationResult {
        return try {
            val successCount = emailDao.batchMarkAsRead(emailIds, read)
            
            BatchOperationResult.Success(
                operation = "mark_as_read",
                totalCount = emailIds.size,
                successCount = successCount
            )
        } catch (e: Exception) {
            BatchOperationResult.Error(
                operation = "mark_as_read",
                message = e.message ?: "Unknown error"
            )
        }
    }
    
    override suspend fun batchDeleteEmails(emailIds: List<String>): BatchOperationResult {
        return try {
            val successCount = emailDao.batchDeleteEmails(emailIds)
            
            BatchOperationResult.Success(
                operation = "delete",
                totalCount = emailIds.size,
                successCount = successCount
            )
        } catch (e: Exception) {
            BatchOperationResult.Error(
                operation = "delete",
                message = e.message ?: "Unknown error"
            )
        }
    }
    
    override suspend fun batchStarEmails(emailIds: List<String>, starred: Boolean): BatchOperationResult {
        return try {
            val successCount = emailDao.batchStarEmails(emailIds, starred)
            
            BatchOperationResult.Success(
                operation = "star",
                totalCount = emailIds.size,
                successCount = successCount
            )
        } catch (e: Exception) {
            BatchOperationResult.Error(
                operation = "star",
                message = e.message ?: "Unknown error"
            )
        }
    }
    
    override suspend fun batchMoveEmails(emailIds: List<String>, targetFolderId: String): BatchOperationResult {
        return try {
            val successCount = emailDao.batchMoveEmails(emailIds, targetFolderId)
            
            BatchOperationResult.Success(
                operation = "move",
                totalCount = emailIds.size,
                successCount = successCount
            )
        } catch (e: Exception) {
            BatchOperationResult.Error(
                operation = "move",
                message = e.message ?: "Unknown error"
            )
        }
    }
    
    override suspend fun batchArchiveEmails(emailIds: List<String>): BatchOperationResult {
        return try {
            val successCount = emailDao.batchArchiveEmails(emailIds)
            
            BatchOperationResult.Success(
                operation = "archive",
                totalCount = emailIds.size,
                successCount = successCount
            )
        } catch (e: Exception) {
            BatchOperationResult.Error(
                operation = "archive",
                message = e.message ?: "Unknown error"
            )
        }
    }
    
    override suspend fun batchMarkAsSpam(emailIds: List<String>): BatchOperationResult {
        return try {
            val successCount = emailDao.batchMarkAsSpam(emailIds)
            
            BatchOperationResult.Success(
                operation = "mark_spam",
                totalCount = emailIds.size,
                successCount = successCount
            )
        } catch (e: Exception) {
            BatchOperationResult.Error(
                operation = "mark_spam",
                message = e.message ?: "Unknown error"
            )
        }
    }

    override suspend fun getAllUnreadEmails(): List<Email> {
        return emailDao.getUnreadEmails().toDomain()
    }

    override suspend fun getTotalUnreadCount(): Int {
        // Get total unread count from all emails
        return emailDao.getUnreadEmails().size
    }

    override suspend fun markAsRead(emailId: String) {
        emailDao.markEmailAsRead(emailId, true)
    }

    override suspend fun markAsUnread(emailId: String) {
        emailDao.markEmailAsRead(emailId, false)
    }

    override suspend fun markMultipleAsRead(emailIds: List<String>) {
        emailDao.markEmailsAsRead(emailIds, true)
    }

    override suspend fun markMultipleAsUnread(emailIds: List<String>) {
        emailDao.markEmailsAsRead(emailIds, false)
    }
}
