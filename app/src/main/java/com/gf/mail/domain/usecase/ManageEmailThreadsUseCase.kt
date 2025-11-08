package com.gf.mail.domain.usecase

import com.gf.mail.domain.model.Email
import com.gf.mail.domain.repository.EmailRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing email threads
 */
class ManageEmailThreadsUseCase(
    private val emailRepository: EmailRepository
) {
    /**
     * Get email threads for an account
     */
    suspend fun getEmailThreads(accountId: String): Flow<List<List<Email>>> {
        // TODO: Implement get email threads
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }

    /**
     * Get a specific email thread
     */
    suspend fun getEmailThread(threadId: String): Flow<List<Email>> {
        // TODO: Implement get email thread
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }

    /**
     * Mark entire thread as read/unread
     */
    suspend fun markThreadAsRead(threadId: String, isRead: Boolean) {
        // TODO: Implement mark thread as read
    }
}