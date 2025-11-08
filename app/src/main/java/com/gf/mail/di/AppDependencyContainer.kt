package com.gf.mail.di

import android.content.Context
import com.gf.mail.data.local.database.GfmailDatabase
import com.gf.mail.data.local.dao.*
import com.gf.mail.data.repository.*
import com.gf.mail.domain.repository.*
import com.gf.mail.data.email.ImapClient
import com.gf.mail.data.email.SmtpClient
import com.gf.mail.data.email.EmailSyncService
import com.gf.mail.data.email.RealtimeEmailSyncService
import com.gf.mail.data.security.CredentialEncryption
import com.gf.mail.data.notification.PushNotificationManager
import com.gf.mail.data.notification.EmailNotificationService
import com.gf.mail.data.service.ConnectionTestService
import com.gf.mail.data.performance.PerformanceMonitor
import com.gf.mail.data.handwriting.HandwritingRecognitionService
import com.gf.mail.data.handwriting.HandwritingStorageService
import com.gf.mail.data.mapper.EmailMapper
import com.gf.mail.data.mapper.FolderMapper
import com.gf.mail.data.mapper.toDomain
import com.gf.mail.domain.usecase.*
import com.gf.mail.domain.usecase.SearchEmailsUseCaseImpl
import com.gf.mail.presentation.viewmodel.*

/**
 * 简单的依赖注入容器
 * 管理应用中的所有依赖关系
 */
class AppDependencyContainer(private val context: Context) {
    
    // ========== 数据库层 ==========
    private val database: GfmailDatabase by lazy {
        GfmailDatabase.getInstance(context)
    }
    
    private val emailDao: EmailDao by lazy { database.emailDao() }
    private val folderDao: FolderDao by lazy { database.folderDao() }
    private val accountDao: AccountDao by lazy { database.accountDao() }
    private val attachmentDao: AttachmentDao by lazy { database.attachmentDao() }
    private val userSettingsDao: UserSettingsDao by lazy { database.userSettingsDao() }
    private val emailSignatureDao: EmailSignatureDao by lazy { database.emailSignatureDao() }
    private val serverConfigurationDao: ServerConfigurationDao by lazy { database.serverConfigurationDao() }
    
    // ========== 数据映射器 ==========
    private val emailMapper: EmailMapper = EmailMapper
    private val folderMapper: FolderMapper = FolderMapper
    
    // ========== 安全组件 ==========
    private val _credentialEncryption: CredentialEncryption by lazy {
        CredentialEncryption(context)
    }
    
    // ========== 网络客户端 ==========
    private val _imapClient: ImapClient by lazy { ImapClient() }
    private val _smtpClient: SmtpClient by lazy { SmtpClient() }
    
    // ========== 服务 ==========
    private val connectionTestService: ConnectionTestService by lazy {
        ConnectionTestService(_imapClient, _smtpClient)
    }
    
    private val performanceMonitor: PerformanceMonitor by lazy {
        PerformanceMonitor(context)
    }
    
    // ========== 通知管理 ==========
    private val emailNotificationService: EmailNotificationService by lazy {
        EmailNotificationService(context)
    }
    
    private val _pushNotificationManager: PushNotificationManager by lazy {
        PushNotificationManager(context, emailNotificationService)
    }
    
    // ========== 数据仓库 ==========
    private val _emailRepository: EmailRepository by lazy {
        EmailRepositoryImpl(
            emailDao = emailDao,
            attachmentDao = attachmentDao,
            folderDao = folderDao
        )
    }
    
    private val _folderRepository: FolderRepository by lazy {
        FolderRepositoryImpl(
            folderDao = folderDao
        )
    }
    
    private val _accountRepository: AccountRepository by lazy {
        AccountRepositoryImpl(
            accountDao = accountDao,
            credentialEncryption = _credentialEncryption,
            connectionTestService = connectionTestService
        )
    }
    
    private val _attachmentRepository: AttachmentRepository by lazy {
        AttachmentRepositoryImpl(
            attachmentDao = attachmentDao
        )
    }
    
    private val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(
            context = context,
            userSettingsDao = userSettingsDao,
            emailSignatureDao = emailSignatureDao,
            serverConfigurationDao = serverConfigurationDao,
            sharedPreferences = context.getSharedPreferences("gfmail_settings", Context.MODE_PRIVATE)
        )
    }
    
    // ========== 同步服务 ==========
    private val _emailSyncService: EmailSyncService by lazy {
        EmailSyncService(
            accountRepository = _accountRepository,
            emailRepository = _emailRepository,
            folderRepository = _folderRepository,
            imapClient = _imapClient
        )
    }
    
    private val _realtimeEmailSyncService: RealtimeEmailSyncService by lazy {
        RealtimeEmailSyncService(
            imapClient = _imapClient,
            emailDao = emailDao,
            folderDao = folderDao,
            folderMapper = folderMapper,
            pushNotificationManager = _pushNotificationManager,
            credentialEncryption = _credentialEncryption
        )
    }
    
    // ========== 手写识别服务 ==========
    private val handwritingRecognitionService: HandwritingRecognitionService by lazy {
        HandwritingRecognitionService(context)
    }
    
    private val handwritingStorageService: HandwritingStorageService by lazy {
        HandwritingStorageService(context, _credentialEncryption)
    }
    
    // ========== 用例层 ==========
    private val getEmailsUseCase: GetEmailsUseCase by lazy {
        GetEmailsUseCase(_emailRepository, _folderRepository, context)
    }
    
    private val getAccountSummaryUseCase: GetAccountSummaryUseCase by lazy {
        GetAccountSummaryUseCase(_accountRepository, _emailRepository, _folderRepository)
    }
    
    private val getActiveAccountUseCase: GetActiveAccountUseCase by lazy {
        GetActiveAccountUseCase(_accountRepository)
    }
    
    // Getter methods for UseCases
    fun getEmailsUseCase(): GetEmailsUseCase = getEmailsUseCase
    fun getAccountSummaryUseCase(): GetAccountSummaryUseCase = getAccountSummaryUseCase
    fun getActiveAccountUseCase(): GetActiveAccountUseCase = getActiveAccountUseCase
    
    private val switchActiveAccountUseCase: SwitchActiveAccountUseCase by lazy {
        SwitchActiveAccountUseCase(_accountRepository)
    }
    
    private val manageAccountsUseCase: ManageAccountsUseCase by lazy {
        ManageAccountsUseCase(_accountRepository)
    }
    
    private val sendEmailUseCase: SendEmailUseCase by lazy {
        SendEmailUseCase(_smtpClient, _emailRepository)
    }
    
    private val searchEmailsUseCase: SearchEmailsUseCaseImpl by lazy {
        SearchEmailsUseCaseImpl(_emailRepository)
    }
    
    private val manageUserSettingsUseCase: ManageUserSettingsUseCase by lazy {
        ManageUserSettingsUseCase(settingsRepository)
    }
    
    private val manageLanguageSettingsUseCase: ManageLanguageSettingsUseCase by lazy {
        ManageLanguageSettingsUseCase(settingsRepository)
    }
    
    private val manageEmailSignaturesUseCase: ManageEmailSignaturesUseCase by lazy {
        ManageEmailSignaturesUseCase(settingsRepository)
    }
    
    private val managePushNotificationsUseCase: ManagePushNotificationsUseCase by lazy {
        ManagePushNotificationsUseCase(settingsRepository)
    }
    
    private val biometricAuthenticationUseCase: BiometricAuthenticationUseCase by lazy {
        BiometricAuthenticationUseCase()
    }
    
    private val handwritingRecognitionUseCase: HandwritingRecognitionUseCase by lazy {
        HandwritingRecognitionUseCase(
            handwritingRecognitionService = handwritingRecognitionService,
            handwritingStorageService = handwritingStorageService
        )
    }
    
    private val batchEmailOperationsUseCase: BatchEmailOperationsUseCase by lazy {
        BatchEmailOperationsUseCase(_emailRepository)
    }
    
    private val manageAccessibilitySettingsUseCase: ManageAccessibilitySettingsUseCase by lazy {
        ManageAccessibilitySettingsUseCase(settingsRepository)
    }
    
    private val manageSecuritySettingsUseCase: ManageSecuritySettingsUseCase by lazy {
        ManageSecuritySettingsUseCase(settingsRepository)
    }
    
    private val performanceOptimizationUseCase: PerformanceOptimizationUseCase by lazy {
        PerformanceOptimizationUseCase(performanceMonitor)
    }
    
    private val managePerformanceOptimizationUseCase: ManagePerformanceOptimizationUseCase by lazy {
        ManagePerformanceOptimizationUseCase(settingsRepository)
    }
    
    
    // ========== ViewModel 工厂 ==========
    fun createEmailListViewModel(): EmailListViewModel {
        return EmailListViewModel(
            getEmailsUseCase = getEmailsUseCase
        )
    }
    
    fun createAccountManagementViewModel(): AccountManagementViewModel {
        return AccountManagementViewModel(
            manageAccountsUseCase = manageAccountsUseCase
        )
    }
    
    fun createComposeEmailViewModel(): ComposeEmailViewModel {
        return ComposeEmailViewModel(
            sendEmailUseCase = sendEmailUseCase
        )
    }
    
    fun createSearchEmailViewModel(): SearchEmailViewModel {
        return SearchEmailViewModel(
            searchEmailsUseCase = searchEmailsUseCase
        )
    }
    
    fun createSettingsViewModel(): SettingsViewModel {
        return SettingsViewModel(
            manageUserSettingsUseCase = manageUserSettingsUseCase,
            manageEmailSignaturesUseCase = manageEmailSignaturesUseCase,
            manageSecuritySettingsUseCase = manageSecuritySettingsUseCase,
            managePerformanceOptimizationUseCase = managePerformanceOptimizationUseCase,
            managePushNotificationsUseCase = managePushNotificationsUseCase,
            manageLanguageSettingsUseCase = manageLanguageSettingsUseCase
        )
    }
    
    fun createBatchEmailOperationsViewModel(): BatchEmailOperationsViewModel {
        return BatchEmailOperationsViewModel(
            batchEmailOperationsUseCase = batchEmailOperationsUseCase
        )
    }
    
    fun createAccessibilitySettingsViewModel(): AccessibilitySettingsViewModel {
        return AccessibilitySettingsViewModel(
            manageAccessibilitySettingsUseCase = manageAccessibilitySettingsUseCase
        )
    }
    
    fun createSecuritySettingsViewModel(): SecuritySettingsViewModel {
        return SecuritySettingsViewModel(
            manageSecuritySettingsUseCase = manageSecuritySettingsUseCase
        )
    }
    
    fun createPerformanceOptimizationViewModel(): PerformanceOptimizationViewModel {
        return PerformanceOptimizationViewModel(
            performanceOptimizationUseCase = performanceOptimizationUseCase
        )
    }
    
    fun createSignatureManagementViewModel(): SignatureManagementViewModel {
        return SignatureManagementViewModel(
            manageEmailSignaturesUseCase = manageEmailSignaturesUseCase
        )
    }
    
    fun createConnectionTestViewModel(): ConnectionTestViewModel {
        return ConnectionTestViewModel(
            connectionTestService = connectionTestService,
            accountRepository = _accountRepository
        )
    }
    
    fun createHandwritingRecognitionViewModel(): HandwritingRecognitionViewModel {
        return HandwritingRecognitionViewModel(
            handwritingRecognitionUseCase = handwritingRecognitionUseCase
        )
    }
    
    // ========== 服务访问器 ==========
    fun getEmailSyncService(): EmailSyncService = _emailSyncService
    fun getRealtimeEmailSyncService(): RealtimeEmailSyncService = _realtimeEmailSyncService
    fun getPushNotificationManager(): PushNotificationManager = _pushNotificationManager
    fun getImapClient(): ImapClient = _imapClient
    fun getSmtpClient(): SmtpClient = _smtpClient
    fun getCredentialEncryption(): CredentialEncryption = _credentialEncryption
    fun getEmailRepository(): EmailRepository = _emailRepository
    fun getAccountRepository(): AccountRepository = _accountRepository
    fun getFolderRepository(): FolderRepository = _folderRepository
    fun getAttachmentRepository(): AttachmentRepository = _attachmentRepository
}
