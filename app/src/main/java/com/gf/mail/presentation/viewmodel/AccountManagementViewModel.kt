package com.gf.mail.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.AddAccountStep
import com.gf.mail.domain.model.EmailProvider
import com.gf.mail.domain.model.ServerConfiguration
import com.gf.mail.domain.usecase.ManageAccountsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for account management
 */
class AccountManagementViewModel(
    private val manageAccountsUseCase: ManageAccountsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountManagementUiState())
    val uiState: StateFlow<AccountManagementUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                println("🔍 [AccountManagementViewModel] Starting to load accounts...")
                manageAccountsUseCase.getAllAccounts().collect { accounts ->
                    println("📧 [AccountManagementViewModel] Loaded ${accounts.size} accounts: ${accounts.map { it.email }}")
                    _uiState.value = _uiState.value.copy(accounts = accounts)
                }
            } catch (e: Exception) {
                println("❌ [AccountManagementViewModel] Failed to load accounts: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun addAccount(account: Account) {
        viewModelScope.launch {
            try {
                manageAccountsUseCase.addAccount(account)
                loadAccounts() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            try {
                manageAccountsUseCase.updateAccount(account)
                loadAccounts() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            try {
                manageAccountsUseCase.deleteAccount(accountId)
                loadAccounts() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun setActiveAccount(accountId: String) {
        viewModelScope.launch {
            try {
                manageAccountsUseCase.setActiveAccount(accountId)
                loadAccounts() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearOAuth2Intent() {
        _uiState.value = _uiState.value.copy(oauth2Intent = null)
    }

    fun cancelAddAccount() {
        _uiState.value = _uiState.value.copy(
            addAccountStep = AddAccountStep.SELECT_PROVIDER,
            oauth2Intent = null,
            successMessage = null,
            emailAddress = "",
            selectedProvider = null
        )
    }

    fun selectProvider(provider: EmailProvider) {
        _uiState.value = _uiState.value.copy(
            selectedProvider = provider,
            addAccountStep = AddAccountStep.ENTER_CREDENTIALS
        )
    }

    fun startQRCodeDisplay() {
        _uiState.value = _uiState.value.copy(addAccountStep = AddAccountStep.OAUTH_AUTHENTICATION)
    }

    fun setEmailAddress(email: String) {
        _uiState.value = _uiState.value.copy(emailAddress = email)
    }

    fun startOAuth2Authentication(provider: EmailProvider) {
        _uiState.value = _uiState.value.copy(
            selectedProvider = provider,
            addAccountStep = AddAccountStep.OAUTH_AUTHENTICATION
        )
    }

    fun showPasswordInput() {
        _uiState.value = _uiState.value.copy(addAccountStep = AddAccountStep.PASSWORD_AUTHENTICATION)
    }

    fun isAppPassword(): Boolean {
        return _uiState.value.selectedProvider?.name?.contains("Gmail") == true // Gmail requires app password
    }

    fun setAppPassword(isAppPassword: Boolean) {
        // TODO: Implement app password setting
    }

    fun authenticateWithPassword() {
        _uiState.value = _uiState.value.copy(addAccountStep = AddAccountStep.TEST_CONNECTION)
    }

    fun showManualServerConfig() {
        _uiState.value = _uiState.value.copy(addAccountStep = AddAccountStep.MANUAL_SERVER_CONFIG)
    }

    fun customServerConfig(): ServerConfiguration? {
        return null // TODO: Implement custom server config
    }

    fun updateServerConfig(config: ServerConfiguration) {
        // TODO: Implement server config update
    }

    fun testConnection() {
        _uiState.value = _uiState.value.copy(addAccountStep = AddAccountStep.COMPLETED)
    }

    data class AccountManagementUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val accounts: List<Account> = emptyList(),
        val addAccountStep: AddAccountStep = AddAccountStep.SELECT_PROVIDER,
        val oauth2Intent: Intent? = null,
        val successMessage: String? = null,
        val emailAddress: String = "",
        val selectedProvider: EmailProvider? = null,
        val isAppPassword: Boolean = false,
        val customServerConfig: ServerConfiguration? = null
    )
}