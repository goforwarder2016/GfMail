package com.gf.mail.domain.usecase

import com.gf.mail.domain.model.Email
import com.gf.mail.domain.repository.EmailRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting unread emails
 */
class GetUnreadEmailsUseCase(
    private val emailRepository: EmailRepository
) {
    /**
     * Get unread emails for a specific account
     */
    suspend fun getUnreadEmails(accountId: Long): Flow<List<Email>> {
        // TODO: Implement get unread emails for account
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }

    /**
     * Get unread email count for a specific account
     */
    suspend fun getUnreadEmailCount(accountId: Long): Flow<Int> {
        // TODO: Implement get unread email count for account
        return kotlinx.coroutines.flow.flowOf(0)
    }
}