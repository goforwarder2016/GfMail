package com.gf.mail.domain.usecase

import com.gf.mail.data.email.EmailSyncService
import com.gf.mail.domain.model.Account
import kotlinx.coroutines.flow.Flow

/**
 * Use case for syncing account emails
 */
class SyncAccountEmailsUseCase(
    private val emailSyncService: EmailSyncService
) {
    /**
     * Sync emails for a specific account
     */
    suspend fun syncAccountEmails(account: Account): Boolean {
        // TODO: Implement sync account emails
        return true
    }

    /**
     * Sync all accounts
     */
    suspend fun syncAllAccounts(): Boolean {
        // TODO: Implement sync all accounts
        return true
    }
}