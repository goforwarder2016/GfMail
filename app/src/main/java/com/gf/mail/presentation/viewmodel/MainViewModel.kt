package com.gf.mail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.AccountSummary
import com.gf.mail.domain.usecase.GetAccountSummaryUseCase
import com.gf.mail.domain.usecase.GetActiveAccountUseCase
import com.gf.mail.presentation.manager.AccountSwitchManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Main ViewModel for the application
 * Handles overall app state including account switching
 */
class MainViewModel(
    private val accountSwitchManager: AccountSwitchManager,
    private val getActiveAccountUseCase: GetActiveAccountUseCase,
    private val getAccountSummaryUseCase: GetAccountSummaryUseCase
) : ViewModel() {

    // UI State data class
    data class MainUiState(
        val currentAccount: Account? = null,
        val availableAccounts: List<Account> = emptyList(),
        val accountSummary: AccountSummary? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load current account
                getActiveAccountUseCase().collect { currentAccount ->
                    // Load available accounts
                    val availableAccounts = accountSwitchManager.getAvailableAccountsForSwitching()
                    
                    // Load account summary if account exists
                    val accountSummary = currentAccount?.let { account ->
                        getAccountSummaryUseCase(account.id).first()
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        currentAccount = currentAccount,
                        availableAccounts = availableAccounts,
                        accountSummary = accountSummary,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun switchAccount(accountId: String) {
        viewModelScope.launch {
            try {
                accountSwitchManager.switchAccount(accountId)
                loadInitialData() // Reload data with new account
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to switch account"
                )
            }
        }
    }

    fun refreshData() {
        loadInitialData()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}