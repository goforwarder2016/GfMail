package com.gf.mail.di

import android.content.Context
import com.gf.mail.data.local.database.GfmailDatabase
import com.gf.mail.data.repository.AccountRepositoryImpl
import com.gf.mail.data.repository.EmailRepositoryImpl
import com.gf.mail.data.repository.AttachmentRepositoryImpl
import com.gf.mail.data.repository.FolderRepositoryImpl
import com.gf.mail.data.repository.SettingsRepositoryImpl
import com.gf.mail.data.auth.AuthenticationManager
import com.gf.mail.data.auth.OAuth2Service
import com.gf.mail.data.auth.ImapAuthService
import com.gf.mail.data.auth.BiometricAuthManager
import com.gf.mail.data.provider.ProviderConfigService
import com.gf.mail.data.service.ConnectionTestService
import com.gf.mail.data.email.ImapClient
import com.gf.mail.data.email.SmtpClient
import com.gf.mail.data.security.CredentialEncryption
import com.gf.mail.data.cache.CacheManager
import com.gf.mail.data.performance.PerformanceMonitor
import com.gf.mail.data.threading.EmailThreadingService
import com.gf.mail.data.sync.SyncOptimizer
import com.gf.mail.data.offline.OfflineManager
import com.gf.mail.data.offline.OfflineSyncService
import com.gf.mail.data.notification.EmailNotificationService
import com.gf.mail.data.notification.PushNotificationManager
import com.gf.mail.data.email.EmailSyncService
import com.gf.mail.data.email.RealtimeEmailSyncService
import com.gf.mail.data.email.ImapIdleService
import com.gf.mail.data.sync.FolderSyncService
import com.gf.mail.data.handwriting.HandwritingRecognitionService
import com.gf.mail.data.handwriting.HandwritingStorageService
import com.gf.mail.data.integration.AdvancedIntegrationManager
import com.gf.mail.data.security.advanced.AdvancedSecurityManager
import com.gf.mail.domain.enterprise.EnterpriseFeaturesManager
import com.gf.mail.presentation.ux.UXEnhancementManager
import com.gf.mail.presentation.manager.AccountSwitchManager
import com.gf.mail.utils.CrashReportingManager
import com.gf.mail.utils.PerformanceUtils

/**
 * Manual dependency injection container
 * Manages all application dependencies and their lifecycle
 */
class DependencyContainer(private val context: Context) {
    
    // Database
    private val database by lazy { 
        GfmailDatabase.getInstance(context)
    }
    
    // DAOs
    private val accountDao by lazy { database.accountDao() }
    private val emailDao by lazy { database.emailDao() }
    private val attachmentDao by lazy { database.attachmentDao() }
    private val folderDao by lazy { database.folderDao() }
    private val userSettingsDao by lazy { database.userSettingsDao() }
    private val emailSignatureDao by lazy { database.emailSignatureDao() }
    private val serverConfigurationDao by lazy { database.serverConfigurationDao() }
    
    // Core Services
    private val credentialEncryption by lazy { CredentialEncryption(context) }
    private val cacheManager by lazy { CacheManager(context) }
    private val performanceMonitor by lazy { PerformanceMonitor(context) }
    private val providerConfigService by lazy { ProviderConfigService() }
    private val connectionTestService by lazy { ConnectionTestService(imapClient, smtpClient) }
    private val oAuth2Service by lazy { OAuth2Service() }
    private val imapAuthService by lazy { ImapAuthService() }
    private val biometricAuthManager by lazy { BiometricAuthManager(context) }
    private val imapClient by lazy { ImapClient() }
    private val smtpClient by lazy { SmtpClient() }
    
    // Repositories
    private val accountRepository by lazy { 
        AccountRepositoryImpl(
            accountDao = accountDao,
            credentialEncryption = credentialEncryption,
            connectionTestService = connectionTestService
        )
    }
    
    private val emailRepository by lazy { 
        EmailRepositoryImpl(
            emailDao = emailDao,
            attachmentDao = attachmentDao,
            folderDao = folderDao
        )
    }
    
    private val attachmentRepository by lazy { 
        AttachmentRepositoryImpl(
            attachmentDao = attachmentDao
        )
    }
    
    private val folderRepository by lazy { 
        FolderRepositoryImpl(
            folderDao = folderDao
        )
    }
    
    private val settingsRepository by lazy { 
        SettingsRepositoryImpl(
            userSettingsDao = userSettingsDao,
            emailSignatureDao = emailSignatureDao,
            serverConfigurationDao = serverConfigurationDao,
            context = context,
            sharedPreferences = context.getSharedPreferences("gfmail_settings", Context.MODE_PRIVATE)
        )
    }
    
    // Authentication Manager
    private val authenticationManager by lazy {
        AuthenticationManager(
            context = context,
            oAuth2Service = oAuth2Service,
            imapAuthService = imapAuthService,
            providerConfigService = providerConfigService,
            connectionTestService = connectionTestService,
            accountRepository = accountRepository,
            credentialEncryption = credentialEncryption
        )
    }
    
    // Services
    val emailSyncService by lazy { 
        EmailSyncService(
            accountRepository = accountRepository,
            emailRepository = emailRepository,
            folderRepository = folderRepository,
            imapClient = imapClient
        )
    }
    
    private val realtimeEmailSyncService by lazy { 
        RealtimeEmailSyncService(
            imapClient = imapClient,
            emailDao = emailDao,
            folderDao = folderDao,
            folderMapper = com.gf.mail.data.mapper.FolderMapper,
            pushNotificationManager = pushNotificationManager,
            credentialEncryption = credentialEncryption
        )
    }
    
    private val imapIdleService by lazy { 
        ImapIdleService(
            context = context,
            emailRepository = emailRepository,
            accountRepository = accountRepository,
            folderRepository = folderRepository,
            realtimeEmailSyncService = realtimeEmailSyncService,
            performanceMonitor = performanceMonitor,
            imapClient = imapClient,
            credentialEncryption = credentialEncryption,
            emailDao = emailDao,
            notificationService = emailNotificationService
        )
    }
    
    private val folderSyncService by lazy { 
        FolderSyncService(
            folderRepository = folderRepository,
            folderDao = folderDao,
            folderMapper = com.gf.mail.data.mapper.FolderMapper,
            imapClient = imapClient
        )
    }
    
    private val syncOptimizer by lazy { 
        SyncOptimizer(
            accountRepository = accountRepository,
            emailRepository = emailRepository,
            folderRepository = folderRepository
        )
    }
    
    private val offlineManager by lazy { 
        OfflineManager(
            context = context
        )
    }
    
    private val offlineSyncService by lazy { 
        OfflineSyncService(
            offlineManager = offlineManager,
            emailRepository = emailRepository,
            accountRepository = accountRepository
        )
    }
    
    private val emailNotificationService by lazy { 
        EmailNotificationService(
            context = context
        )
    }
    
    private val pushNotificationManager by lazy { 
        PushNotificationManager(
            context = context,
            emailNotificationService = emailNotificationService
        )
    }
    
    private val emailThreadingService by lazy { 
        EmailThreadingService(
            emailRepository = emailRepository
        )
    }
    
    private val handwritingRecognitionService by lazy { 
        HandwritingRecognitionService(
            context = context
        )
    }
    
    private val handwritingStorageService by lazy { 
        HandwritingStorageService(
            context = context,
            credentialEncryption = credentialEncryption
        )
    }
    
    private val advancedIntegrationManager by lazy { 
        AdvancedIntegrationManager(
            context = context,
            enterpriseFeaturesManager = enterpriseFeaturesManager
        )
    }
    
    private val securitySettingsUseCase by lazy {
        com.gf.mail.domain.usecase.ManageSecuritySettingsUseCase(
            settingsRepository = settingsRepository
        )
    }
    
    private val advancedSecurityManager by lazy { 
        AdvancedSecurityManager(
            context = context,
            credentialEncryption = credentialEncryption,
            securitySettingsUseCase = securitySettingsUseCase
        )
    }
    
    private val enterpriseFeaturesManager by lazy { 
        EnterpriseFeaturesManager(
            context = context,
            advancedSecurityManager = advancedSecurityManager
        )
    }
    
    private val uxEnhancementManager by lazy { 
        UXEnhancementManager(
            context = context
        )
    }
    
    private val accountSwitchManager by lazy { 
        AccountSwitchManager(
            getActiveAccountUseCase = com.gf.mail.domain.usecase.GetActiveAccountUseCase(accountRepository),
            switchActiveAccountUseCase = com.gf.mail.domain.usecase.SwitchActiveAccountUseCase(accountRepository),
            getAccountSummaryUseCase = com.gf.mail.domain.usecase.GetAccountSummaryUseCase(
                accountRepository = accountRepository,
                emailRepository = emailRepository,
                folderRepository = folderRepository
            )
        )
    }
    
    private val crashReportingManager by lazy { 
        CrashReportingManager()
    }
    
    private val performanceUtils by lazy { 
        PerformanceUtils()
    }
    
    // ViewModels
    fun getMainViewModel(): com.gf.mail.presentation.viewmodel.MainViewModel {
        return com.gf.mail.presentation.viewmodel.MainViewModel(
            accountSwitchManager = accountSwitchManager,
            getActiveAccountUseCase = com.gf.mail.domain.usecase.GetActiveAccountUseCase(accountRepository),
            getAccountSummaryUseCase = com.gf.mail.domain.usecase.GetAccountSummaryUseCase(accountRepository, emailRepository, folderRepository)
        )
    }
    
    fun getAccountManagementViewModel(): com.gf.mail.presentation.viewmodel.AccountManagementViewModel {
        return com.gf.mail.presentation.viewmodel.AccountManagementViewModel(
            manageAccountsUseCase = com.gf.mail.domain.usecase.ManageAccountsUseCase(accountRepository)
        )
    }
    
    fun getComposeEmailViewModel(): com.gf.mail.presentation.viewmodel.ComposeEmailViewModel {
        return com.gf.mail.presentation.viewmodel.ComposeEmailViewModel(
            sendEmailUseCase = com.gf.mail.domain.usecase.SendEmailUseCase(
                smtpClient = com.gf.mail.data.email.SmtpClient(),
                emailRepository = emailRepository
            )
        )
    }
    
    fun getSearchEmailViewModel(): com.gf.mail.presentation.viewmodel.SearchEmailViewModel {
        return com.gf.mail.presentation.viewmodel.SearchEmailViewModel(
            searchEmailsUseCase = com.gf.mail.domain.usecase.SearchEmailsUseCaseImpl(
                emailRepository = emailRepository
            )
        )
    }
    
    fun getSettingsViewModel(): com.gf.mail.presentation.viewmodel.SettingsViewModel {
        return com.gf.mail.presentation.viewmodel.SettingsViewModel(
            manageUserSettingsUseCase = com.gf.mail.domain.usecase.ManageUserSettingsUseCase(settingsRepository),
            manageEmailSignaturesUseCase = com.gf.mail.domain.usecase.ManageEmailSignaturesUseCase(settingsRepository),
            manageSecuritySettingsUseCase = com.gf.mail.domain.usecase.ManageSecuritySettingsUseCase(settingsRepository),
            managePerformanceOptimizationUseCase = com.gf.mail.domain.usecase.ManagePerformanceOptimizationUseCase(settingsRepository),
            managePushNotificationsUseCase = com.gf.mail.domain.usecase.ManagePushNotificationsUseCase(settingsRepository),
            manageLanguageSettingsUseCase = com.gf.mail.domain.usecase.ManageLanguageSettingsUseCase(settingsRepository)
        )
    }
    
    fun getHandwritingRecognitionViewModel(): com.gf.mail.presentation.viewmodel.HandwritingRecognitionViewModel {
        return com.gf.mail.presentation.viewmodel.HandwritingRecognitionViewModel(
            handwritingRecognitionUseCase = com.gf.mail.domain.usecase.HandwritingRecognitionUseCase(
                handwritingRecognitionService = handwritingRecognitionService,
                handwritingStorageService = handwritingStorageService
            )
        )
    }
    
    fun getBatchEmailOperationsViewModel(): com.gf.mail.presentation.viewmodel.BatchEmailOperationsViewModel {
        return com.gf.mail.presentation.viewmodel.BatchEmailOperationsViewModel(
            batchEmailOperationsUseCase = com.gf.mail.domain.usecase.BatchEmailOperationsUseCase(
                emailRepository = emailRepository
            )
        )
    }
    
    fun getPushNotificationViewModel(): com.gf.mail.presentation.viewmodel.PushNotificationViewModel {
        return com.gf.mail.presentation.viewmodel.PushNotificationViewModel(
            managePushNotificationsUseCase = com.gf.mail.domain.usecase.ManagePushNotificationsUseCase(
                settingsRepository = settingsRepository
            )
        )
    }
    
    fun getPerformanceOptimizationViewModel(): com.gf.mail.presentation.viewmodel.PerformanceOptimizationViewModel {
        return com.gf.mail.presentation.viewmodel.PerformanceOptimizationViewModel(
            performanceOptimizationUseCase = com.gf.mail.domain.usecase.PerformanceOptimizationUseCase(
                performanceMonitor = performanceMonitor
            )
        )
    }
    
    fun getSecuritySettingsViewModel(): com.gf.mail.presentation.viewmodel.SecuritySettingsViewModel {
        return com.gf.mail.presentation.viewmodel.SecuritySettingsViewModel(
            manageSecuritySettingsUseCase = com.gf.mail.domain.usecase.ManageSecuritySettingsUseCase(
                settingsRepository = settingsRepository
            )
        )
    }
    
    fun getEmailListViewModel(): com.gf.mail.presentation.viewmodel.EmailListViewModel {
        return com.gf.mail.presentation.viewmodel.EmailListViewModel(
            getEmailsUseCase = com.gf.mail.domain.usecase.GetEmailsUseCase(
                emailRepository = emailRepository,
                folderRepository = folderRepository,
                context = context
            )
        )
    }
    
    // Utility methods
    fun getAccountRepository(): com.gf.mail.domain.repository.AccountRepository = accountRepository
    fun getEmailRepository(): com.gf.mail.domain.repository.EmailRepository = emailRepository
    fun getAttachmentRepository(): com.gf.mail.domain.repository.AttachmentRepository = attachmentRepository
    fun getFolderRepository(): com.gf.mail.domain.repository.FolderRepository = folderRepository
    fun getSettingsRepository(): com.gf.mail.domain.repository.SettingsRepository = settingsRepository
}