package com.gf.mail.di

import android.content.Context
import androidx.room.Room
import com.gf.mail.data.auth.AuthenticationManager
import com.gf.mail.data.auth.ImapAuthService
import com.gf.mail.data.auth.OAuth2Service
import com.gf.mail.data.email.ImapClient
import com.gf.mail.data.email.SmtpClient
import com.gf.mail.data.local.database.GfmailDatabase
import com.gf.mail.data.provider.ProviderConfigService
import com.gf.mail.data.repository.AccountRepositoryImpl
import com.gf.mail.data.repository.FolderRepositoryImpl
import com.gf.mail.data.repository.SettingsRepositoryImpl
import com.gf.mail.data.security.CredentialEncryption
import com.gf.mail.data.service.ConnectionTestService
import com.gf.mail.domain.repository.AccountRepository
import com.gf.mail.domain.repository.FolderRepository
import com.gf.mail.domain.repository.SettingsRepository
import com.gf.mail.domain.usecase.account.AuthenticateAccountUseCase
import com.gf.mail.domain.usecase.GetAccountSummaryUseCase
import com.gf.mail.domain.usecase.GetActiveAccountUseCase

import com.gf.mail.domain.usecase.ManageUserSettingsUseCase
import com.gf.mail.domain.usecase.SwitchActiveAccountUseCase
import com.gf.mail.presentation.manager.AccountSwitchManager
import com.gf.mail.presentation.viewmodel.AccountManagementViewModel

import com.gf.mail.presentation.viewmodel.MainViewModel
import com.gf.mail.presentation.viewmodel.SettingsViewModel

/**
 * Temporary dependency container until Hilt is properly configured
 * This provides manual dependency injection for the app
 */
class AppInitializer(private val context: Context) {

    // Database
    private val database by lazy {
        Room.databaseBuilder(
            context,
            GfmailDatabase::class.java,
            "gfmail_database"
        ).build()
    }

    // SharedPreferences
    private val sharedPreferences by lazy {
        context.getSharedPreferences("gfmail_settings", Context.MODE_PRIVATE)
    }

    // Core services
    private val credentialEncryption by lazy { CredentialEncryption(context) }
    private val oauth2Service by lazy { OAuth2Service() }
    private val imapAuthService by lazy { ImapAuthService() }
    private val providerConfigService by lazy { ProviderConfigService() }
    private val imapClient by lazy { ImapClient() }
    private val smtpClient by lazy { SmtpClient() }
    private val connectionTestService by lazy { ConnectionTestService(imapClient, smtpClient) }

    // Repositories
    private val accountRepository: AccountRepository by lazy {
        AccountRepositoryImpl(
            accountDao = database.accountDao(),
            credentialEncryption = credentialEncryption,
            connectionTestService = connectionTestService
        )
    }

    private val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(
            context = context,
            userSettingsDao = database.userSettingsDao(),
            emailSignatureDao = database.emailSignatureDao(),
            serverConfigurationDao = database.serverConfigurationDao(),
            sharedPreferences = sharedPreferences
        )
    }

    private val folderRepository: FolderRepository by lazy {
        FolderRepositoryImpl(
            folderDao = database.folderDao()
        )
    }

    private val emailRepository: com.gf.mail.domain.repository.EmailRepository by lazy {
        com.gf.mail.data.repository.EmailRepositoryImpl(
            emailDao = database.emailDao(),
            attachmentDao = database.attachmentDao(),
            folderDao = database.folderDao()
        )
    }

    // Authentication
    private val authenticationManager by lazy {
        AuthenticationManager(
            context = context,
            oAuth2Service = oauth2Service,
            imapAuthService = imapAuthService,
            providerConfigService = providerConfigService,
            connectionTestService = connectionTestService,
            accountRepository = accountRepository as AccountRepositoryImpl,
            credentialEncryption = credentialEncryption
        )
    }

    // Use cases
    private val getActiveAccountUseCase by lazy { GetActiveAccountUseCase(accountRepository) }
    private val switchActiveAccountUseCase by lazy { SwitchActiveAccountUseCase(accountRepository) }
    private val getAccountSummaryUseCase by lazy { GetAccountSummaryUseCase(accountRepository, emailRepository, folderRepository) }
    private val authenticateAccountUseCase by lazy {
        AuthenticateAccountUseCase(authenticationManager)
    }
    private val manageUserSettingsUseCase by lazy {
        ManageUserSettingsUseCase(settingsRepository)
    }


    // Managers
    private val accountSwitchManager by lazy {
        AccountSwitchManager(
            getActiveAccountUseCase = getActiveAccountUseCase,
            switchActiveAccountUseCase = switchActiveAccountUseCase,
            getAccountSummaryUseCase = getAccountSummaryUseCase
        )
    }

    // ViewModels
    fun createMainViewModel(): MainViewModel {
        return MainViewModel(
            accountSwitchManager = accountSwitchManager,
            getActiveAccountUseCase = getActiveAccountUseCase,
            getAccountSummaryUseCase = getAccountSummaryUseCase
        )
    }

    fun createAccountManagementViewModel(): AccountManagementViewModel {
        return AccountManagementViewModel(
            manageAccountsUseCase = com.gf.mail.domain.usecase.ManageAccountsUseCase(accountRepository)
        )
    }

    fun createSettingsViewModel(): SettingsViewModel {
        return SettingsViewModel(
            manageUserSettingsUseCase = manageUserSettingsUseCase,
            manageEmailSignaturesUseCase = com.gf.mail.domain.usecase.ManageEmailSignaturesUseCase(settingsRepository),
            manageSecuritySettingsUseCase = com.gf.mail.domain.usecase.ManageSecuritySettingsUseCase(settingsRepository),
            managePerformanceOptimizationUseCase = com.gf.mail.domain.usecase.ManagePerformanceOptimizationUseCase(settingsRepository),
            managePushNotificationsUseCase = com.gf.mail.domain.usecase.ManagePushNotificationsUseCase(settingsRepository),
            manageLanguageSettingsUseCase = com.gf.mail.domain.usecase.ManageLanguageSettingsUseCase(settingsRepository)
        )
    }



    companion object {
        @Volatile
        private var INSTANCE: AppInitializer? = null

        fun getInstance(context: Context): AppInitializer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppInitializer(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}