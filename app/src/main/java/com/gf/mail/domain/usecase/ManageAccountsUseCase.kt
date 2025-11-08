package com.gf.mail.domain.usecase

import com.gf.mail.domain.model.Account
import com.gf.mail.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing accounts
 */
class ManageAccountsUseCase(
    private val accountRepository: AccountRepository
) {
    /**
     * Get all accounts
     */
    fun getAllAccounts(): Flow<List<Account>> {
        return accountRepository.getAllAccountsFlow()
    }

    /**
     * Add a new account
     */
    suspend fun addAccount(account: Account): Long {
        return accountRepository.insertAccount(account, null)
    }

    /**
     * Update an existing account
     */
    suspend fun updateAccount(account: Account) {
        accountRepository.updateAccount(account)
    }

    /**
     * Delete an account
     */
    suspend fun deleteAccount(accountId: String) {
        accountRepository.deleteAccount(accountId)
    }

    /**
     * Set active account
     */
    suspend fun setActiveAccount(accountId: String) {
        accountRepository.setActiveAccount(accountId)
    }
}