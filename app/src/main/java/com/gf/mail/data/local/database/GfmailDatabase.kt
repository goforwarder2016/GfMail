package com.gf.mail.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gf.mail.data.local.dao.AccountDao
import com.gf.mail.data.local.dao.AttachmentDao
import com.gf.mail.data.local.dao.EmailDao
import com.gf.mail.data.local.dao.EmailSignatureDao
import com.gf.mail.data.local.dao.FolderDao
import com.gf.mail.data.local.dao.ServerConfigurationDao
import com.gf.mail.data.local.dao.UserSettingsDao
import com.gf.mail.data.local.entity.AccountEntity
import com.gf.mail.data.local.entity.AttachmentEntity
import com.gf.mail.data.local.entity.EmailEntity
import com.gf.mail.data.local.entity.EmailSignatureEntity
import com.gf.mail.data.local.entity.FolderEntity
import com.gf.mail.data.local.entity.ServerConfigurationEntity
import com.gf.mail.data.local.entity.UserSettingsEntity

@Database(
    entities = [
        EmailEntity::class,
        AccountEntity::class,
        FolderEntity::class,
        AttachmentEntity::class,
        UserSettingsEntity::class,
        EmailSignatureEntity::class,
        ServerConfigurationEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GfmailDatabase : RoomDatabase() {

    abstract fun emailDao(): EmailDao
    abstract fun accountDao(): AccountDao
    abstract fun folderDao(): FolderDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun emailSignatureDao(): EmailSignatureDao
    abstract fun serverConfigurationDao(): ServerConfigurationDao

    companion object {
        const val DATABASE_NAME = "gfmail_database"

        @Volatile
        private var INSTANCE: GfmailDatabase? = null

        fun getInstance(context: Context): GfmailDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GfmailDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Clear the database instance (used for testing)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}