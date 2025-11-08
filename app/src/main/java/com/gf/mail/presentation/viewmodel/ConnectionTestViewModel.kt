package com.gf.mail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.mail.data.auth.AuthenticationManager
import com.gf.mail.data.service.ConnectionTestService
import com.gf.mail.domain.model.*
import com.gf.mail.domain.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for connection testing functionality
 * Manages account connection testing with both automatic and manual server configurations
 */
class ConnectionTestViewModel(
    private val accountRepository: AccountRepository? = null, // TODO: Inject via DI
    private val connectionTestService: ConnectionTestService? = null, // TODO: Inject via DI
    private val authenticationManager: AuthenticationManager? = null // TODO: Inject via DI
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionTestUiState())
    val uiState: StateFlow<ConnectionTestUiState> = _uiState.asStateFlow()

    /**
     * Load account information for testing
     */
    fun loadAccount(accountId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val account = if (accountRepository != null) {
                    // Get actual account from repository
                    accountRepository.getAccountById(accountId)
                        ?: throw IllegalArgumentException("Account not found: $accountId")
                } else {
                    throw IllegalStateException("AccountRepository not available. Please configure dependency injection.")
                }

                _uiState.value = _uiState.value.copy(
                    account = account,
                    manualServerConfig = account.serverConfig,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load account: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Test both IMAP and SMTP connections
     */
    fun testConnection() {
        val account = _uiState.value.account ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    testResult = null
                )

                // Get password from user input
                val password = if (account.authInfo.type == AuthenticationType.PASSWORD ||
                    account.authInfo.type == AuthenticationType.APP_PASSWORD
                ) {
                    _uiState.value.enteredPassword ?: ""
                } else {
                    null
                }

                // Test connection using ConnectionTestService
                val result = testAccountConnection(account, password)

                _uiState.value = _uiState.value.copy(
                    testResult = result,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Connection test failed: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Test IMAP connection only
     */
    fun testImapOnly() {
        val account = _uiState.value.account ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

                val password = _uiState.value.enteredPassword ?: ""
                val result = testImapConnection(account, password)

                _uiState.value = _uiState.value.copy(
                    testResult = ConnectionTestResult(
                        isSuccessful = result.isSuccessful,
                        imapResult = result,
                        smtpResult = ConnectionResult(
                            isSuccessful = false,
                            serverType = ServerType.SMTP,
                            host = account.serverConfig.smtpHost ?: "",
                            port = account.serverConfig.smtpPort,
                            encryption = account.serverConfig.smtpEncryption,
                            errorMessage = "SMTP test not performed"
                        ),
                        errorMessage = if (!result.isSuccessful) result.errorMessage else null
                    ),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "IMAP test failed: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Test SMTP connection only
     */
    fun testSmtpOnly() {
        val account = _uiState.value.account ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

                val password = _uiState.value.enteredPassword ?: ""
                val result = testSmtpConnection(account, password)

                _uiState.value = _uiState.value.copy(
                    testResult = ConnectionTestResult(
                        isSuccessful = result.isSuccessful,
                        imapResult = ConnectionResult(
                            isSuccessful = false,
                            serverType = ServerType.IMAP,
                            host = account.serverConfig.imapHost ?: "",
                            port = account.serverConfig.imapPort,
                            encryption = account.serverConfig.imapEncryption,
                            errorMessage = "IMAP test not performed"
                        ),
                        smtpResult = result,
                        errorMessage = if (!result.isSuccessful) result.errorMessage else null
                    ),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "SMTP test failed: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Toggle manual settings visibility
     */
    fun toggleManualSettings() {
        _uiState.value = _uiState.value.copy(
            showManualSettings = !_uiState.value.showManualSettings
        )
    }

    /**
     * Update entered password for testing
     */
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            enteredPassword = password,
            error = null, // Clear previous errors when password changes
            testResult = null // Clear previous test results
        )
    }

    /**
     * Update manual server configuration
     */
    fun updateManualServerConfig(config: ServerConfiguration) {
        _uiState.value = _uiState.value.copy(manualServerConfig = config)
    }

    /**
     * Test connection with manual settings
     */
    fun testManualSettings() {
        val account = _uiState.value.account ?: return
        val manualConfig = _uiState.value.manualServerConfig

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    testResult = null
                )

                // Create account with manual configuration
                val testAccount = account.copy(serverConfig = manualConfig)
                val password = _uiState.value.enteredPassword ?: ""

                val result = testAccountConnection(testAccount, password)

                _uiState.value = _uiState.value.copy(
                    testResult = result,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Manual settings test failed: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    // Private helper methods

    private suspend fun testAccountConnection(account: Account, password: String?): ConnectionTestResult {
        return if (connectionTestService != null) {
            val result = connectionTestService.testConnection(
                account.serverConfig, account.email, password ?: ""
            )
            ConnectionTestResult(
                isSuccessful = result,
                errorMessage = if (result) null else "Connection failed"
            )
        } else {
            // Service not available - return failure
            ConnectionTestResult(
                isSuccessful = false,
                errorMessage = "Connection test service not available"
            )
        }
    }

    private suspend fun testImapConnection(account: Account, password: String?): ConnectionResult {
        return if (connectionTestService != null) {
            val result = connectionTestService.testImapConnection(account.serverConfig, account.email, password ?: "")
            ConnectionResult(
                isSuccessful = result,
                serverType = ServerType.IMAP,
                host = account.serverConfig.imapHost ?: "unknown",
                port = account.serverConfig.imapPort,
                encryption = account.serverConfig.imapEncryption
            )
        } else {
            // Service not available - return failure
            ConnectionResult(
                isSuccessful = false,
                serverType = ServerType.IMAP,
                host = account.serverConfig.imapHost ?: "unknown",
                port = account.serverConfig.imapPort,
                encryption = account.serverConfig.imapEncryption,
                errorMessage = "Connection test service not available"
            )
        }
    }

    private suspend fun testSmtpConnection(account: Account, password: String?): ConnectionResult {
        return if (connectionTestService != null) {
            val result = connectionTestService.testSmtpConnection(account.serverConfig, account.email, password ?: "")
            ConnectionResult(
                isSuccessful = result,
                serverType = ServerType.SMTP,
                host = account.serverConfig.smtpHost ?: "unknown",
                port = account.serverConfig.smtpPort,
                encryption = account.serverConfig.smtpEncryption
            )
        } else {
            // Service not available - return failure
            ConnectionResult(
                isSuccessful = false,
                serverType = ServerType.SMTP,
                host = account.serverConfig.smtpHost ?: "unknown",
                port = account.serverConfig.smtpPort,
                encryption = account.serverConfig.smtpEncryption,
                errorMessage = "Connection test service not available"
            )
        }
    }


}

/**
 * UI state for connection testing screen
 */
data class ConnectionTestUiState(
    val account: Account? = null,
    val testResult: ConnectionTestResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showManualSettings: Boolean = false,
    val enteredPassword: String? = null,
    val showPasswordField: Boolean = true,
    val manualServerConfig: ServerConfiguration = ServerConfiguration(
        imapHost = "",
        imapPort = 993,
        imapEncryption = EncryptionType.SSL,
        smtpHost = "",
        smtpPort = 587,
        smtpEncryption = EncryptionType.STARTTLS
    )
)
