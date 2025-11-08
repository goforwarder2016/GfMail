package com.gf.mail.domain.repository

import com.gf.mail.domain.model.Email
import com.gf.mail.domain.model.EmailPriority
import kotlinx.coroutines.flow.Flow

interface EmailRepository {

    // Basic CRUD operations
    suspend fun getEmailById(id: String): Email?
    suspend fun getEmailByMessageId(messageId: String): Email?
    suspend fun getAllEmails(): List<Email>
    fun getAllEmailsFlow(): Flow<List<Email>>

    suspend fun insertEmail(email: Email)
    suspend fun insertEmails(emails: List<Email>)
    suspend fun updateEmail(email: Email)
    suspend fun deleteEmail(emailId: String)
    suspend fun deleteEmailByMessageId(messageId: String)

    // Folder-based operations
    suspend fun getEmailsInFolder(folderId: String, limit: Int = 50, offset: Int = 0): List<Email>
    fun getEmailsInFolderFlow(folderId: String): Flow<List<Email>>
    suspend fun getEmailCountInFolder(folderId: String): Int
    suspend fun getUnreadEmailCountInFolder(folderId: String): Int

    // Account-based operations
    suspend fun getEmailsByAccount(accountId: String): List<Email>
    fun getEmailsByAccountFlow(accountId: String): Flow<List<Email>>
    suspend fun getEmailCountByAccount(accountId: String): Int
    suspend fun getUnreadEmailCountByAccount(accountId: String): Int

    // Search operations
    suspend fun searchEmails(query: String, accountId: String? = null): List<Email>
    suspend fun searchEmailsInFolder(query: String, folderId: String): List<Email>
    fun searchEmailsFlow(query: String, accountId: String? = null): Flow<List<Email>>

    // Status operations
    suspend fun markEmailAsRead(emailId: String, read: Boolean)
    suspend fun markEmailAsStarred(emailId: String, starred: Boolean)
    suspend fun starEmail(emailId: String, starred: Boolean)
    suspend fun markEmailsAsRead(emailIds: List<String>, read: Boolean)
    suspend fun markAllEmailsInFolderAsRead(folderId: String)
    
    // Email actions
    suspend fun archiveEmail(emailId: String)
    suspend fun markEmailAsSpam(emailId: String)
    suspend fun moveEmailToFolder(emailId: String, targetFolderId: String)

    // Thread operations
    suspend fun getEmailsByThread(threadId: String): List<Email>
    fun getEmailsByThreadFlow(threadId: String): Flow<List<Email>>
    suspend fun getEmailThreads(accountId: String): List<String>

    // Filtering operations
    suspend fun getEmailsByPriority(priority: EmailPriority, accountId: String? = null): List<Email>
    suspend fun getUnreadEmails(accountId: String? = null): List<Email>
    fun getUnreadEmailsFlow(accountId: String): Flow<List<Email>>
    suspend fun getUnreadCount(accountId: String): Int
    suspend fun getStarredEmails(accountId: String? = null): List<Email>
    
    // Additional unread email operations
    suspend fun getAllUnreadEmails(): List<Email>
    suspend fun getTotalUnreadCount(): Int
    suspend fun markAsRead(emailId: String)
    suspend fun markAsUnread(emailId: String)
    suspend fun markMultipleAsRead(emailIds: List<String>)
    suspend fun markMultipleAsUnread(emailIds: List<String>)
    suspend fun getDraftEmails(accountId: String? = null): List<Email>
    suspend fun getEmailsWithAttachments(accountId: String? = null): List<Email>

    // Date-based operations
    suspend fun getEmailsFromDate(fromDate: Long, accountId: String? = null): List<Email>
    suspend fun getEmailsInDateRange(fromDate: Long, toDate: Long, accountId: String? = null): List<Email>
    suspend fun getRecentEmails(accountId: String, days: Int = 7): List<Email>

    // Sender-based operations
    suspend fun getEmailsFromSender(senderEmail: String, accountId: String? = null): List<Email>
    suspend fun getEmailsToRecipient(recipientEmail: String, accountId: String? = null): List<Email>

    // Bulk operations
    suspend fun deleteEmailsInFolder(folderId: String)
    suspend fun deleteEmailsOlderThan(timestamp: Long)
    suspend fun moveEmailsToFolder(emailIds: List<String>, targetFolderId: String)

    // Sync operations
    suspend fun getEmailsModifiedAfter(timestamp: Long, accountId: String): List<Email>
    suspend fun getLastSyncedEmail(folderId: String): Email?
    suspend fun updateEmailSyncStatus(emailId: String, synced: Boolean)

    // Statistics
    suspend fun getEmailStats(accountId: String): EmailStats
    suspend fun getFolderStats(folderId: String): FolderEmailStats
    
    // Batch operations
    suspend fun batchMarkAsRead(emailIds: List<String>, read: Boolean): BatchOperationResult
    suspend fun batchDeleteEmails(emailIds: List<String>): BatchOperationResult
    suspend fun batchStarEmails(emailIds: List<String>, starred: Boolean): BatchOperationResult
    suspend fun batchMoveEmails(emailIds: List<String>, targetFolderId: String): BatchOperationResult
    suspend fun batchArchiveEmails(emailIds: List<String>): BatchOperationResult
    suspend fun batchMarkAsSpam(emailIds: List<String>): BatchOperationResult
}

/**
 * Email statistics data class
 */
data class EmailStats(
    val totalEmails: Int,
    val unreadEmails: Int,
    val starredEmails: Int,
    val draftsCount: Int,
    val attachmentsCount: Int,
    val totalSize: Long,
    val oldestEmailDate: Long?,
    val newestEmailDate: Long?
)

/**
 * Folder email statistics data class
 */
data class FolderEmailStats(
    val folderId: String,
    val totalEmails: Int,
    val unreadEmails: Int,
    val totalSize: Long,
    val lastEmailDate: Long?
)

/**
 * Result of batch operation
 */
sealed class BatchOperationResult {
    object Loading : BatchOperationResult()
    
    data class Progress(
        val current: Int,
        val total: Int
    ) : BatchOperationResult()
    
    data class Success(
        val operation: String,
        val totalCount: Int,
        val successCount: Int
    ) : BatchOperationResult()
    
    data class PartialSuccess(
        val operation: String,
        val totalCount: Int,
        val successCount: Int,
        val errorCount: Int,
        val errors: List<String>
    ) : BatchOperationResult()
    
    data class Error(
        val operation: String,
        val message: String
    ) : BatchOperationResult()
}
