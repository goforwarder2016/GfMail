package com.gf.mail.di.modules

import android.content.Context
import com.gf.mail.data.notification.EmailNotificationService
import com.gf.mail.data.notification.PushNotificationManager
import com.gf.mail.domain.usecase.ManagePushNotificationsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for notification-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    
    @Provides
    @Singleton
    fun provideEmailNotificationService(
        @ApplicationContext context: Context
    ): EmailNotificationService {
        return EmailNotificationService(context)
    }
    
    @Provides
    @Singleton
    fun providePushNotificationManager(
        @ApplicationContext context: Context,
        emailNotificationService: EmailNotificationService
    ): PushNotificationManager {
        return PushNotificationManager(context, emailNotificationService)
    }
    
    @Provides
    @Singleton
    fun provideManagePushNotificationsUseCase(
        settingsRepository: com.gf.mail.domain.repository.SettingsRepository
    ): ManagePushNotificationsUseCase {
        return ManagePushNotificationsUseCase(settingsRepository)
    }
}