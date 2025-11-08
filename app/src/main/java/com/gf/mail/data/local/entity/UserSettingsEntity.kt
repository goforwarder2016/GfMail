package com.gf.mail.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User settings entity for Room database
 */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: String = "default_user_settings",

    // General Settings
    val theme: String = "system", // "light", "dark", "system"
    val language: String = "system", // "en", "zh-cn", "zh-tw", "ja", "ko", "system"
    val isFirstRun: Boolean = true,

    // Email Settings
    val defaultAccountId: String? = null,
    val autoMarkAsRead: Boolean = false,
    val autoMarkAsReadDelay: Int = 3, // seconds
    val showPreviewText: Boolean = true,
    val previewLines: Int = 2,
    val showImages: Boolean = false,
    val autoDownloadAttachments: Boolean = false,
    val maxAttachmentSize: Long = 20 * 1024 * 1024, // 20MB

    // Display Settings
    val emailsPerPage: Int = 50,
    val showAvatars: Boolean = true,
    val showUnreadBadge: Boolean = true,
    val groupByDate: Boolean = true,
    val useCompactView: Boolean = false,
    val showSenderInfo: Boolean = true,

    // Notification Settings
    val globalNotificationsEnabled: Boolean = true,
    val notificationSound: String = "default",
    val vibrationEnabled: Boolean = true,
    val ledEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: Int = 22, // 22:00
    val quietHoursEnd: Int = 8, // 08:00

    // Sync Settings
    val backgroundSyncEnabled: Boolean = true,
    val syncInterval: Int = 15, // minutes
    val wifiOnlySync: Boolean = false,
    val batteryOptimization: Boolean = true,
    val idleSyncEnabled: Boolean = true,

    // Security Settings
    val biometricAuthEnabled: Boolean = false,
    val autoLockEnabled: Boolean = false,
    val autoLockDelay: Int = 5, // minutes
    val hideContentInRecents: Boolean = false,
    val blockExternalImages: Boolean = true,
    val warnUnsafeLinks: Boolean = true,

    // Compose Settings
    val defaultSignatureId: String? = null,
    val replyQuoteOriginal: Boolean = true,
    val forwardAsAttachment: Boolean = false,
    val autoSaveDrafts: Boolean = true,
    val autoSaveInterval: Int = 30, // seconds
    val requestReadReceipts: Boolean = false,
    val useRichTextEditor: Boolean = true,

    // Storage Settings
    val cacheSize: Long = 100 * 1024 * 1024, // 100MB
    val autoClearCacheEnabled: Boolean = true,
    val autoClearCacheDays: Int = 30,
    val keepDeletedEmailsDays: Int = 7,

    // Advanced Settings
    val developerMode: Boolean = false,
    val debugLogging: Boolean = false,
    val crashReporting: Boolean = true,
    val analyticsEnabled: Boolean = true,

    // Version info
    val settingsVersion: Int = 1,
    val lastUpdated: Long = System.currentTimeMillis()
)