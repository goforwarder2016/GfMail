package com.gf.mail.domain.usecase

import com.gf.mail.domain.model.Account
import com.gf.mail.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting the currently active account
 */
class GetActiveAccountUseCase(
    private val accountRepository: AccountRepository
) {
    /**
     * Get the currently active account as a Flow
     */
    operator fun invoke(): Flow<Account?> {
        return accountRepository.getActiveAccountFlow()
    }
}