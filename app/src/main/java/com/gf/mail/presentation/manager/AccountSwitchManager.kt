package com.gf.mail.presentation.manager

import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.AccountSummary
import com.gf.mail.domain.usecase.GetAccountSummaryUseCase
import com.gf.mail.domain.usecase.GetActiveAccountUseCase
import com.gf.mail.domain.usecase.SwitchActiveAccountUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Manager for handling account switching and active account state
 * Provides a centralized way to manage account switching across the app
 */
class AccountSwitchManager(
    private val getActiveAccountUseCase: GetActiveAccountUseCase,
    private val switchActiveAccountUseCase: SwitchActiveAccountUseCase,
    private val getAccountSummaryUseCase: GetAccountSummaryUseCase
) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Private mutable state flows
    private val _currentAccount = MutableStateFlow<Account?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _switchError = MutableStateFlow<String?>(null)

    // Public read-only state flows
    val currentAccount: StateFlow<Account?> = _currentAccount.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val switchError: StateFlow<String?> = _switchError.asStateFlow()

    // Account summary flow - will be initialized when we have an account
    private val _accountSummary = MutableStateFlow<AccountSummary?>(null)
    val accountSummary: StateFlow<AccountSummary?> = _accountSummary.asStateFlow()

    init {
        // Observe active account changes
        scope.launch {
            getActiveAccountUseCase()
                .collect { account ->
                    _currentAccount.value = account
                }
        }
    }

    /**
     * Switch to a different account
     */
    suspend fun switchAccount(accountId: String): Boolean {
        return try {
            _isLoading.value = true
            _switchError.value = null

            switchActiveAccountUseCase.switchActiveAccount(accountId)
            true
        } catch (e: Exception) {
            _switchError.value = e.message ?: "Failed to switch account"
            false
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Clear switch error
     */
    fun clearSwitchError() {
        _switchError.value = null
    }

    /**
     * Get current account synchronously
     */
    suspend fun getCurrentAccountSync(): Account? {
        return getActiveAccountUseCase().first()
    }

    /**
     * Check if account switching is available
     */
    fun canSwitchAccounts(): Boolean {
        val summary = accountSummary.value
        return summary?.isActive ?: false
    }

    /**
     * Get available accounts for switching (enabled accounts that are not currently active)
     */
    fun getAvailableAccountsForSwitching(): List<Account> {
        // This would need to be implemented with a proper account list
        // TODO: Implement real account list retrieval
        return emptyList()
    }
}
