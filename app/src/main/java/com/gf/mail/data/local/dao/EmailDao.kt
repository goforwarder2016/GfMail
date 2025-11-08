package com.gf.mail.data.local.dao

import androidx.room.*
import com.gf.mail.data.local.entity.EmailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailDao {

    // Basic CRUD Operations
    @Query("SELECT * FROM emails WHERE id = :id")
    suspend fun getEmailById(id: String): EmailEntity?

    @Query("SELECT * FROM emails WHERE message_id = :messageId")
    suspend fun getEmailByMessageId(messageId: String): EmailEntity?

    @Query("SELECT * FROM emails ORDER BY received_date DESC")
    suspend fun getAllEmails(): List<EmailEntity>

    @Query("SELECT * FROM emails ORDER BY received_date DESC")
    fun getAllEmailsFlow(): Flow<List<EmailEntity>>

    @Query("SELECT * FROM emails WHERE account_id = :accountId ORDER BY received_date DESC")
    suspend fun getEmailsByAccount(accountId: String): List<EmailEntity>

    @Query("SELECT * FROM emails WHERE account_id = :accountId ORDER BY received_date DESC")
    fun getEmailsByAccountFlow(accountId: String): Flow<List<EmailEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmail(email: EmailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<EmailEntity>)

    @Update
    suspend fun updateEmail(email: EmailEntity)

    @Delete
    suspend fun deleteEmail(email: EmailEntity)

    @Query("DELETE FROM emails WHERE id = :id")
    suspend fun deleteEmailById(id: String)

    @Query("DELETE FROM emails WHERE message_id = :messageId")
    suspend fun deleteEmailByMessageId(messageId: String)

    @Query("DELETE FROM emails WHERE account_id = :accountId")
    suspend fun deleteEmailsByAccount(accountId: String)

    @Query("DELETE FROM emails WHERE id IN (:emailIds)")
    suspend fun deleteEmails(emailIds: List<String>)

    // Folder-based operations
    @Query(
        "SELECT * FROM emails WHERE folder_id = :folderId ORDER BY received_date DESC LIMIT :limit OFFSET :offset"
    )
    suspend fun getEmailsInFolder(folderId: String, limit: Int, offset: Int): List<EmailEntity>

    @Query("SELECT * FROM emails WHERE folder_id = :folderId ORDER BY received_date DESC")
    fun getEmailsInFolderFlow(folderId: String): Flow<List<EmailEntity>>

    @Query("SELECT COUNT(*) FROM emails WHERE folder_id = :folderId")
    suspend fun getEmailCountInFolder(folderId: String): Int

    @Query("SELECT COUNT(*) FROM emails WHERE folder_id = :folderId AND is_read = 0")
    suspend fun getUnreadEmailCountInFolder(folderId: String): Int

    @Query("SELECT COUNT(*) FROM emails WHERE account_id = :accountId")
    suspend fun getEmailCountByAccount(accountId: String): Int

    @Query("SELECT COUNT(*) FROM emails WHERE account_id = :accountId AND is_read = 0")
    suspend fun getUnreadEmailCountByAccount(accountId: String): Int

    // Search operations
    @Query(
        """
        SELECT * FROM emails 
        WHERE (subject LIKE '%' || :query || '%' 
             OR body_text LIKE '%' || :query || '%' 
             OR from_address LIKE '%' || :query || '%'
             OR from_name LIKE '%' || :query || '%')
        ORDER BY received_date DESC
    """
    )
    suspend fun searchEmails(query: String): List<EmailEntity>

    @Query(
        """
        SELECT * FROM emails 
        WHERE account_id = :accountId 
        AND (subject LIKE '%' || :query || '%' 
             OR body_text LIKE '%' || :query || '%' 
             OR from_address LIKE '%' || :query || '%'
             OR from_name LIKE '%' || :query || '%')
        ORDER BY received_date DESC
    """
    )
    suspend fun searchEmailsByAccount(accountId: String, query: String): List<EmailEntity>

    @Query(
        """
        SELECT * FROM emails 
        WHERE folder_id = :folderId 
        AND (subject LIKE '%' || :query || '%' 
             OR body_text LIKE '%' || :query || '%' 
             OR from_address LIKE '%' || :query || '%'
             OR from_name LIKE '%' || :query || '%')
        ORDER BY received_date DESC
    """
    )
    suspend fun searchEmailsInFolder(folderId: String, query: String): List<EmailEntity>

    @Query(
        """
        SELECT * FROM emails 
        WHERE (subject LIKE '%' || :query || '%' 
             OR body_text LIKE '%' || :query || '%' 
             OR from_address LIKE '%' || :query || '%'
             OR from_name LIKE '%' || :query || '%')
        ORDER BY received_date DESC
    """
    )
    fun searchEmailsFlow(query: String): Flow<List<EmailEntity>>

    @Query(
        """
        SELECT * FROM emails 
        WHERE account_id = :accountId 
        AND (subject LIKE '%' || :query || '%' 
             OR body_text LIKE '%' || :query || '%' 
             OR from_address LIKE '%' || :query || '%'
             OR from_name LIKE '%' || :query || '%')
        ORDER BY received_date DESC
    """
    )
    fun searchEmailsByAccountFlow(accountId: String, query: String): Flow<List<EmailEntity>>

    // Status operations
    @Query("UPDATE emails SET is_read = :read WHERE id = :emailId")
    suspend fun markEmailAsRead(emailId: String, read: Boolean)

    @Query("UPDATE emails SET is_starred = :starred WHERE id = :emailId")
    suspend fun markEmailAsStarred(emailId: String, starred: Boolean)

    @Query("UPDATE emails SET is_read = :read WHERE id IN (:emailIds)")
    suspend fun markEmailsAsRead(emailIds: List<String>, read: Boolean)

    @Query("UPDATE emails SET is_read = 1 WHERE folder_id = :folderId")
    suspend fun markAllEmailsInFolderAsRead(folderId: String)

    // Thread operations
    @Query("SELECT * FROM emails WHERE thread_id = :threadId ORDER BY received_date ASC")
    suspend fun getEmailsByThread(threadId: String): List<EmailEntity>

    @Query("SELECT * FROM emails WHERE thread_id = :threadId ORDER BY received_date ASC")
    fun getEmailsByThreadFlow(threadId: String): Flow<List<EmailEntity>>

    @Query(
        "SELECT DISTINCT thread_id FROM emails WHERE account_id = :accountId AND thread_id IS NOT NULL"
    )
    suspend fun getEmailThreads(accountId: String): List<String>

    // Filtering operations
    @Query("SELECT * FROM emails WHERE priority = :priority ORDER BY received_date DESC")
    suspend fun getEmailsByPriority(priority: String): List<EmailEntity>

    @Query(
        "SELECT * FROM emails WHERE priority = :priority AND account_id = :accountId ORDER BY received_date DESC"
    )
    suspend fun getEmailsByPriorityAndAccount(priority: String, accountId: String): List<EmailEntity>

    @Query("SELECT * FROM emails WHERE is_read = 0 ORDER BY received_date DESC")
    suspend fun getUnreadEmails(): List<EmailEntity>

    @Query(
        "SELECT * FROM emails WHERE account_id = :accountId AND is_read = 0 ORDER BY received_date DESC"
    )
    suspend fun getUnreadEmailsByAccount(accountId: String): List<EmailEntity>

    @Query("SELECT * FROM emails WHERE is_starred = 1 ORDER BY received_date DESC")
    suspend fun getStarredEmails(): List<EmailEntity>

    @Query(
        "SELECT * FROM emails WHERE account_id = :accountId AND is_starred = 1 ORDER BY received_date DESC"
    )
    suspend fun getStarredEmailsByAccount(accountId: String): List<EmailEntity>

    @Query("SELECT * FROM emails WHERE is_draft = 1 ORDER BY received_date DESC")
    suspend fun getDraftEmails(): List<EmailEntity>

    @Query(
        "SELECT * FROM emails WHERE account_id = :accountId AND is_draft = 1 ORDER BY received_date DESC"
    )
    suspend fun getDraftEmailsByAccount(accountId: String): List<EmailEntity>

    @Query("SELECT * FROM emails WHERE has_attachments = 1 ORDER BY received_date DESC")
    suspend fun getEmailsWithAttachments(): List<EmailEntity>

    @Query(
        "SELECT * FROM emails WHERE account_id = :accountId AND has_attachments = 1 ORDER BY received_date DESC"
    )
    suspend fun getEmailsWithAttachmentsByAccount(accountId: String): List<EmailEntity>

    // Date-based operations
    @Query("SELECT * FROM emails WHERE received_date >= :fromDate ORDER BY received_date DESC")
    suspend fun getEmailsFromDate(fromDate: Long): List<EmailEntity>

    @Query(
        "SELECT * FROM emails WHERE account_id = :accountId AND received_date >= :fromDate ORDER BY received_date DESC"
    )
    suspend fun getEmailsFromDateByAccount(fromDate: Long, accountId: String): List<EmailEntity>

    @Query(
        "SELECT * FROM emails WHERE received_date BETWEEN :fromDate AND :toDate ORDER BY received_date DESC"
    )
    suspend fun getEmailsInDateRange(fromDate: Long, toDate: Long): List<EmailEntity>

    @Query(
        "SELECT * FROM emails WHERE account_id = :accountId AND received_date BETWEEN :fromDate AND :toDate ORDER BY received_date DESC"
    )
    suspend fun getEmailsInDateRangeByAccount(fromDate: Long, toDate: Long, accountId: String): List<EmailEntity>

    // Sender-based operations
    @Query("SELECT * FROM emails WHERE from_address = :senderEmail ORDER BY received_date DESC")
    suspend fun getEmailsFromSender(senderEmail: String): List<EmailEntity>

    @Query(
        "SELECT * FROM emails WHERE account_id = :accountId AND from_address = :senderEmail ORDER BY received_date DESC"
    )
    suspend fun getEmailsFromSenderByAccount(senderEmail: String, accountId: String): List<EmailEntity>

    @Query(
        "SELECT * FROM emails WHERE to_addresses LIKE '%' || :recipientEmail || '%' ORDER BY received_date DESC"
    )
    suspend fun getEmailsToRecipient(recipientEmail: String): List<EmailEntity>

    @Query(
        "SELECT * FROM emails WHERE account_id = :accountId AND to_addresses LIKE '%' || :recipientEmail || '%' ORDER BY received_date DESC"
    )
    suspend fun getEmailsToRecipientByAccount(recipientEmail: String, accountId: String): List<EmailEntity>

    // Bulk operations
    @Query("DELETE FROM emails WHERE folder_id = :folderId")
    suspend fun deleteEmailsInFolder(folderId: String)

    @Query("DELETE FROM emails WHERE received_date < :timestamp")
    suspend fun deleteEmailsOlderThan(timestamp: Long)

    @Query("UPDATE emails SET folder_id = :targetFolderId WHERE id IN (:emailIds)")
    suspend fun moveEmailsToFolder(emailIds: List<String>, targetFolderId: String)

    // Sync operations
    @Query(
        "SELECT * FROM emails WHERE account_id = :accountId AND updated_at > :timestamp ORDER BY updated_at DESC"
    )
    suspend fun getEmailsModifiedAfter(timestamp: Long, accountId: String): List<EmailEntity>

    @Query("SELECT * FROM emails WHERE folder_id = :folderId ORDER BY received_date DESC LIMIT 1")
    suspend fun getLastSyncedEmail(folderId: String): EmailEntity?

    @Query("UPDATE emails SET updated_at = :timestamp WHERE id = :emailId")
    suspend fun updateEmailSyncStatus(emailId: String, timestamp: Long = System.currentTimeMillis())

    // Statistics helper methods
    @Query("SELECT COUNT(*) FROM emails WHERE account_id = :accountId AND is_starred = 1")
    suspend fun getStarredEmailCountByAccount(accountId: String): Int

    @Query("SELECT COUNT(*) FROM emails WHERE account_id = :accountId AND is_draft = 1")
    suspend fun getDraftEmailCountByAccount(accountId: String): Int

    @Query("SELECT COUNT(*) FROM emails WHERE account_id = :accountId AND has_attachments = 1")
    suspend fun getEmailsWithAttachmentsCountByAccount(accountId: String): Int

    @Query("SELECT COALESCE(SUM(size_bytes), 0) FROM emails WHERE account_id = :accountId")
    suspend fun getTotalEmailSizeByAccount(accountId: String): Long

    @Query("SELECT COALESCE(SUM(size_bytes), 0) FROM emails WHERE folder_id = :folderId")
    suspend fun getTotalEmailSizeInFolder(folderId: String): Long

    @Query("SELECT * FROM emails WHERE account_id = :accountId ORDER BY received_date ASC LIMIT 1")
    suspend fun getOldestEmailByAccount(accountId: String): EmailEntity?

    @Query("SELECT * FROM emails WHERE account_id = :accountId ORDER BY received_date DESC LIMIT 1")
    suspend fun getNewestEmailByAccount(accountId: String): EmailEntity?

    @Query("SELECT * FROM emails WHERE folder_id = :folderId ORDER BY received_date DESC LIMIT 1")
    suspend fun getNewestEmailInFolder(folderId: String): EmailEntity?

    // Legacy methods - maintained for compatibility
    @Query(
        "SELECT * FROM emails WHERE account_id = :accountId AND is_read = 0 ORDER BY received_date DESC"
    )
    fun getUnreadEmailsFlow(accountId: String): Flow<List<EmailEntity>>

    @Query(
        "SELECT * FROM emails WHERE account_id = :accountId AND is_read = 0 ORDER BY received_date DESC"
    )
    fun getUnreadEmailsByAccountFlow(accountId: String): Flow<List<EmailEntity>>

    @Query("SELECT COUNT(*) FROM emails WHERE account_id = :accountId AND is_read = 0")
    suspend fun getUnreadCount(accountId: String): Int

    @Query("SELECT COUNT(*) FROM emails WHERE account_id = :accountId AND is_read = 0")
    suspend fun getUnreadCountByAccount(accountId: String): Int

    @Query("SELECT COUNT(*) FROM emails WHERE account_id = :accountId")
    suspend fun getTotalEmailCount(accountId: String): Int

    @Query(
        "SELECT * FROM emails WHERE account_id = :accountId ORDER BY received_date DESC LIMIT :limit"
    )
    suspend fun getRecentEmails(accountId: String, limit: Int = 20): List<EmailEntity>
    
    // Batch Operations
    @Query("UPDATE emails SET is_read = :read WHERE id IN (:emailIds)")
    suspend fun batchMarkAsRead(emailIds: List<String>, read: Boolean): Int
    
    @Query("DELETE FROM emails WHERE id IN (:emailIds)")
    suspend fun batchDeleteEmails(emailIds: List<String>): Int
    
    @Query("UPDATE emails SET is_starred = :starred WHERE id IN (:emailIds)")
    suspend fun batchStarEmails(emailIds: List<String>, starred: Boolean): Int
    
    @Query("UPDATE emails SET folder_id = :folderId WHERE id IN (:emailIds)")
    suspend fun batchMoveEmails(emailIds: List<String>, folderId: String): Int
    
    @Query("UPDATE emails SET folder_id = 'ARCHIVED' WHERE id IN (:emailIds)")
    suspend fun batchArchiveEmails(emailIds: List<String>): Int
    
    @Query("UPDATE emails SET folder_id = 'SPAM' WHERE id IN (:emailIds)")
    suspend fun batchMarkAsSpam(emailIds: List<String>): Int
}
