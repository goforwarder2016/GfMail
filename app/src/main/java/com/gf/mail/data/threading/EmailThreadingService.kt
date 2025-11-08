package com.gf.mail.data.threading

import com.gf.mail.domain.model.Email
import com.gf.mail.domain.repository.EmailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for email threading operations
 * Handles email conversation threading and grouping
 */
@Singleton
class EmailThreadingService @Inject constructor(
    private val emailRepository: EmailRepository
) {
    
    /**
     * Get all email threads for an account
     */
    suspend fun getEmailThreads(accountId: String): Flow<List<EmailThread>> = flow {
        try {
            val threadIds = emailRepository.getEmailThreads(accountId)
            val threads = mutableListOf<EmailThread>()
            
            for (threadId in threadIds) {
                val emails = emailRepository.getEmailsByThread(threadId)
                if (emails.isNotEmpty()) {
                    val thread = createEmailThread(threadId, emails)
                    threads.add(thread)
                }
            }
            
            // Sort threads by most recent email
            threads.sortByDescending { it.lastEmailDate }
            
            emit(threads)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get a specific email thread
     */
    suspend fun getEmailThread(threadId: String): EmailThread? {
        return try {
            val emails = emailRepository.getEmailsByThread(threadId)
            if (emails.isNotEmpty()) {
                createEmailThread(threadId, emails)
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get email thread as Flow
     */
    fun getEmailThreadFlow(threadId: String): Flow<EmailThread?> = flow {
        try {
            val emails = emailRepository.getEmailsByThread(threadId)
            if (emails.isNotEmpty()) {
                val thread = createEmailThread(threadId, emails)
                emit(thread)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Create an EmailThread from a list of emails
     */
    private fun createEmailThread(threadId: String, emails: List<Email>): EmailThread {
        // Sort emails by date (oldest first for conversation flow)
        val sortedEmails = emails.sortedBy { it.receivedDate }
        
        // Get thread metadata
        val firstEmail = sortedEmails.first()
        val lastEmail = sortedEmails.last()
        val subject = extractThreadSubject(firstEmail.subject)
        val participants = extractParticipants(sortedEmails)
        val unreadCount = sortedEmails.count { !it.isRead }
        val hasAttachments = sortedEmails.any { it.hasAttachments }
        val isStarred = sortedEmails.any { it.isStarred }
        
        // Build conversation structure
        val conversation = buildConversation(sortedEmails)
        
        return EmailThread(
            id = threadId,
            subject = subject,
            participants = participants,
            emailCount = sortedEmails.size,
            unreadCount = unreadCount,
            hasAttachments = hasAttachments,
            isStarred = isStarred,
            firstEmailDate = firstEmail.receivedDate,
            lastEmailDate = lastEmail.receivedDate,
            lastEmailId = lastEmail.id,
            emails = sortedEmails,
            conversation = conversation
        )
    }
    
    /**
     * Extract clean thread subject (remove Re:, Fwd:, etc.)
     */
    private fun extractThreadSubject(subject: String): String {
        return subject
            .replace(Regex("^(Re:|Fwd?:|AW:|WG:)\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    /**
     * Extract unique participants from thread emails
     */
    private fun extractParticipants(emails: List<Email>): List<ThreadParticipant> {
        val participantMap = mutableMapOf<String, ThreadParticipant>()
        
        emails.forEach { email ->
            // Add sender
            if (!participantMap.containsKey(email.fromAddress)) {
                participantMap[email.fromAddress] = ThreadParticipant(
                    email = email.fromAddress,
                    name = email.fromName,
                    isCurrentUser = false // This would need to be determined based on account
                )
            }
            
            // Add recipients
            email.toAddresses.forEach { address ->
                if (!participantMap.containsKey(address)) {
                    participantMap[address] = ThreadParticipant(
                        email = address,
                        name = address, // Would need to resolve name
                        isCurrentUser = false
                    )
                }
            }
        }
        
        return participantMap.values.toList()
    }
    
    /**
     * Build conversation structure with replies and forwards
     */
    private fun buildConversation(emails: List<Email>): List<ConversationItem> {
        val conversation = mutableListOf<ConversationItem>()
        
        emails.forEach { email ->
            val item = ConversationItem(
                emailId = email.id,
                sender = email.fromName,
                senderEmail = email.fromAddress,
                content = email.bodyText ?: email.bodyHtml ?: "",
                timestamp = email.receivedDate,
                isRead = email.isRead,
                hasAttachments = email.hasAttachments,
                isReply = email.inReplyTo != null,
                isForward = email.subject.contains("Fwd:", ignoreCase = true)
            )
            conversation.add(item)
        }
        
        return conversation
    }
    
    /**
     * Mark entire thread as read
     */
    suspend fun markThreadAsRead(threadId: String) {
        try {
            val emails = emailRepository.getEmailsByThread(threadId)
            val unreadEmails = emails.filter { !it.isRead }
            
            unreadEmails.forEach { email ->
                emailRepository.markEmailAsRead(email.id, true)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    /**
     * Mark entire thread as unread
     */
    suspend fun markThreadAsUnread(threadId: String) {
        try {
            val emails = emailRepository.getEmailsByThread(threadId)
            
            emails.forEach { email ->
                emailRepository.markEmailAsRead(email.id, false)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    /**
     * Star/unstar entire thread
     */
    suspend fun starThread(threadId: String, starred: Boolean) {
        try {
            val emails = emailRepository.getEmailsByThread(threadId)
            
            emails.forEach { email ->
                emailRepository.starEmail(email.id, starred)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    /**
     * Delete entire thread
     */
    suspend fun deleteThread(threadId: String) {
        try {
            val emails = emailRepository.getEmailsByThread(threadId)
            
            emails.forEach { email ->
                emailRepository.deleteEmail(email.id)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    /**
     * Get thread statistics
     */
    suspend fun getThreadStats(accountId: String): ThreadStats {
        val threadIds = emailRepository.getEmailThreads(accountId)
        var totalEmails = 0
        var totalUnread = 0
        var totalStarred = 0
        var totalWithAttachments = 0
        
        threadIds.forEach { threadId ->
            val emails = emailRepository.getEmailsByThread(threadId)
            totalEmails += emails.size
            totalUnread += emails.count { !it.isRead }
            totalStarred += emails.count { it.isStarred }
            totalWithAttachments += emails.count { it.hasAttachments }
        }
        
        return ThreadStats(
            totalThreads = threadIds.size,
            totalEmails = totalEmails,
            totalUnread = totalUnread,
            totalStarred = totalStarred,
            totalWithAttachments = totalWithAttachments
        )
    }
}

/**
 * Email thread data class
 */
data class EmailThread(
    val id: String,
    val subject: String,
    val participants: List<ThreadParticipant>,
    val emailCount: Int,
    val unreadCount: Int,
    val hasAttachments: Boolean,
    val isStarred: Boolean,
    val firstEmailDate: Long,
    val lastEmailDate: Long,
    val lastEmailId: String,
    val emails: List<Email>,
    val conversation: List<ConversationItem>
)

/**
 * Thread participant data class
 */
data class ThreadParticipant(
    val email: String,
    val name: String,
    val isCurrentUser: Boolean
)

/**
 * Conversation item for thread display
 */
data class ConversationItem(
    val emailId: String,
    val sender: String,
    val senderEmail: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean,
    val hasAttachments: Boolean,
    val isReply: Boolean,
    val isForward: Boolean
)

/**
 * Thread statistics
 */
data class ThreadStats(
    val totalThreads: Int,
    val totalEmails: Int,
    val totalUnread: Int,
    val totalStarred: Int,
    val totalWithAttachments: Int
)