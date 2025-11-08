package com.gf.mail.domain.usecase

import com.gf.mail.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for switching active account
 */
class SwitchActiveAccountUseCase(
    private val accountRepository: AccountRepository
) {
    /**
     * Switch to a different active account
     */
    suspend fun switchActiveAccount(accountId: String): Unit {
        accountRepository.setActiveAccount(accountId)
    }

    /**
     * Get current active account ID
     */
    suspend fun getCurrentActiveAccountId(): String? {
        return accountRepository.getActiveAccount()?.id
    }
}