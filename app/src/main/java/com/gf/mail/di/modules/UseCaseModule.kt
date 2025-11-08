package com.gf.mail.di.modules

import com.gf.mail.domain.repository.AccountRepository
import com.gf.mail.domain.repository.AttachmentRepository
import com.gf.mail.domain.repository.EmailRepository
import com.gf.mail.domain.repository.FolderRepository
import com.gf.mail.domain.repository.SettingsRepository
import com.gf.mail.domain.usecase.*
import com.gf.mail.domain.usecase.account.*
import com.gf.mail.data.auth.AuthenticationManager
import com.gf.mail.data.email.EmailSyncService
import com.gf.mail.data.email.SmtpClient
import dagger.Module
import dagger.Provides
import android.content.Context
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing use case dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

@Provides
@Singleton
fun provideGetEmailsUseCase(
    emailRepository: EmailRepository,
    folderRepository: FolderRepository,
    @ApplicationContext context: Context
): GetEmailsUseCase {
    return GetEmailsUseCase(emailRepository, folderRepository, context)
}

    @Provides
    @Singleton
    fun provideManageUserSettingsUseCase(settingsRepository: SettingsRepository): ManageUserSettingsUseCase {
        return ManageUserSettingsUseCase(settingsRepository)
    }

    @Provides
    @Singleton
    fun provideSendEmailUseCase(
        smtpClient: SmtpClient,
        emailRepository: EmailRepository
    ): SendEmailUseCase {
        return SendEmailUseCase(smtpClient, emailRepository)
    }

    @Provides
    @Singleton
    fun provideSyncAccountEmailsUseCase(
        emailSyncService: EmailSyncService
    ): SyncAccountEmailsUseCase {
        return SyncAccountEmailsUseCase(emailSyncService)
    }

    @Provides
    @Singleton
    fun provideGetActiveAccountUseCase(accountRepository: AccountRepository): GetActiveAccountUseCase {
        return GetActiveAccountUseCase(accountRepository)
    }

    @Provides
    @Singleton
    fun provideGetUnreadEmailsUseCase(emailRepository: EmailRepository): GetUnreadEmailsUseCase {
        return GetUnreadEmailsUseCase(emailRepository)
    }

    @Provides
    @Singleton
    fun provideManageAccountsUseCase(accountRepository: AccountRepository): ManageAccountsUseCase {
        return ManageAccountsUseCase(accountRepository)
    }

    @Provides
    @Singleton
    fun provideAuthenticateAccountUseCase(authenticationManager: AuthenticationManager): AuthenticateAccountUseCase {
        return AuthenticateAccountUseCase(authenticationManager)
    }

    @Provides
    @Singleton
    fun provideManageAccountTokensUseCase(
        authenticationManager: AuthenticationManager
    ): ManageAccountTokensUseCase {
        return ManageAccountTokensUseCase(authenticationManager)
    }

    @Provides
    @Singleton
    fun provideSearchEmailsUseCase(
        emailRepository: EmailRepository
    ): SearchEmailsUseCaseImpl {
        return SearchEmailsUseCaseImpl(emailRepository)
    }
    
    @Provides
    @Singleton
    fun provideBatchEmailOperationsUseCase(
        emailRepository: EmailRepository
    ): BatchEmailOperationsUseCase {
        return BatchEmailOperationsUseCase(emailRepository)
    }
}