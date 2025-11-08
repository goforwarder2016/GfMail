package com.gf.mail.di.modules

import android.content.Context
import android.content.SharedPreferences
import com.gf.mail.data.local.dao.AccountDao
import com.gf.mail.data.local.dao.AttachmentDao
import com.gf.mail.data.local.dao.EmailDao
import com.gf.mail.data.local.dao.EmailSignatureDao
import com.gf.mail.data.local.dao.FolderDao
import com.gf.mail.data.local.dao.ServerConfigurationDao
import com.gf.mail.data.local.dao.UserSettingsDao
import com.gf.mail.data.repository.AccountRepositoryImpl
import com.gf.mail.data.repository.AttachmentRepositoryImpl
import com.gf.mail.data.repository.EmailRepositoryImpl
import com.gf.mail.data.repository.FolderRepositoryImpl
import com.gf.mail.data.repository.SettingsRepositoryImpl
import com.gf.mail.data.auth.AuthenticationManager
import com.gf.mail.data.auth.ImapAuthService
import com.gf.mail.data.auth.OAuth2Service
import com.gf.mail.data.email.EmailSyncService
import com.gf.mail.data.email.ImapClient
import com.gf.mail.data.email.SmtpClient
import javax.inject.Named
import com.gf.mail.data.provider.ProviderConfigService
import com.gf.mail.data.security.CredentialEncryption
import com.gf.mail.data.service.ConnectionTestService
import com.gf.mail.domain.repository.AccountRepository
import com.gf.mail.domain.repository.AttachmentRepository
import com.gf.mail.domain.repository.EmailRepository
import com.gf.mail.domain.repository.FolderRepository
import com.gf.mail.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("gfmail_settings", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideEmailRepository(
        emailDao: EmailDao,
        attachmentDao: AttachmentDao,
        folderDao: FolderDao
    ): EmailRepository {
        return EmailRepositoryImpl(emailDao, attachmentDao, folderDao)
    }

    @Provides
    @Singleton
    fun provideCredentialEncryption(@ApplicationContext context: Context): CredentialEncryption {
        return CredentialEncryption(context)
    }

    @Provides
    @Singleton
    fun provideImapAuthService(): ImapAuthService {
        return ImapAuthService()
    }

    @Provides
    @Singleton
    fun provideProviderConfigService(): ProviderConfigService {
        return ProviderConfigService()
    }

    @Provides
    @Singleton
    fun provideConnectionTestService(
        @Named("imapClient") imapClient: ImapClient,
        @Named("smtpClient") smtpClient: SmtpClient
    ): ConnectionTestService {
        return ConnectionTestService(imapClient, smtpClient)
    }

    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao,
        credentialEncryption: CredentialEncryption,
        connectionTestService: ConnectionTestService
    ): AccountRepository {
        return AccountRepositoryImpl(accountDao, credentialEncryption, connectionTestService)
    }

    @Provides
    @Singleton
    fun provideFolderRepository(folderDao: FolderDao): FolderRepository {
        return FolderRepositoryImpl(folderDao)
    }

    @Provides
    @Singleton
    fun provideAttachmentRepository(attachmentDao: AttachmentDao): AttachmentRepository {
        return AttachmentRepositoryImpl(attachmentDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context,
        userSettingsDao: UserSettingsDao,
        emailSignatureDao: EmailSignatureDao,
        serverConfigurationDao: ServerConfigurationDao,
        sharedPreferences: SharedPreferences
    ): SettingsRepository {
        return SettingsRepositoryImpl(
            context = context,
            userSettingsDao = userSettingsDao,
            emailSignatureDao = emailSignatureDao,
            serverConfigurationDao = serverConfigurationDao,
            sharedPreferences = sharedPreferences
        )
    }

    @Provides
    @Singleton
    fun provideEmailSyncService(
        accountRepository: AccountRepository,
        emailRepository: EmailRepository,
        folderRepository: FolderRepository,
        imapClient: ImapClient
    ): EmailSyncService {
        return EmailSyncService(accountRepository, emailRepository, folderRepository, imapClient)
    }

    @Provides
    @Singleton
    fun provideSmtpClient(): SmtpClient {
        return SmtpClient()
    }

    @Provides
    @Singleton
    fun provideOAuth2Service(): OAuth2Service {
        return OAuth2Service()
    }

    @Provides
    @Singleton
    fun provideAuthenticationManager(
        @ApplicationContext context: Context,
        oAuth2Service: OAuth2Service,
        imapAuthService: ImapAuthService,
        providerConfigService: ProviderConfigService,
        connectionTestService: ConnectionTestService,
        accountRepository: AccountRepositoryImpl,
        credentialEncryption: CredentialEncryption
    ): AuthenticationManager {
        return AuthenticationManager(
            context,
            oAuth2Service,
            imapAuthService,
            providerConfigService,
            connectionTestService,
            accountRepository,
            credentialEncryption
        )
    }
}