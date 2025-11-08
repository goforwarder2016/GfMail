package com.gf.mail.di.modules

import android.content.Context
import androidx.room.Room
import com.gf.mail.data.local.dao.AccountDao
import com.gf.mail.data.local.dao.AttachmentDao
import com.gf.mail.data.local.dao.EmailDao
import com.gf.mail.data.local.dao.EmailSignatureDao
import com.gf.mail.data.local.dao.FolderDao
import com.gf.mail.data.local.dao.ServerConfigurationDao
import com.gf.mail.data.local.dao.UserSettingsDao
import com.gf.mail.data.local.database.GfmailDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGfmailDatabase(@ApplicationContext context: Context): GfmailDatabase {
        return GfmailDatabase.getInstance(context)
    }

    @Provides
    fun provideEmailDao(database: GfmailDatabase): EmailDao {
        return database.emailDao()
    }

    @Provides
    fun provideAccountDao(database: GfmailDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    fun provideFolderDao(database: GfmailDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    fun provideAttachmentDao(database: GfmailDatabase): AttachmentDao {
        return database.attachmentDao()
    }

    @Provides
    fun provideUserSettingsDao(database: GfmailDatabase): UserSettingsDao {
        return database.userSettingsDao()
    }

    @Provides
    fun provideEmailSignatureDao(database: GfmailDatabase): EmailSignatureDao {
        return database.emailSignatureDao()
    }

    @Provides
    fun provideServerConfigurationDao(database: GfmailDatabase): ServerConfigurationDao {
        return database.serverConfigurationDao()
    }
}