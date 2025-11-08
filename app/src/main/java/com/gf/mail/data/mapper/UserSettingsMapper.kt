package com.gf.mail.data.mapper

import com.gf.mail.data.local.entity.UserSettingsEntity
import com.gf.mail.domain.model.UserSettings

/**
 * Mapper for converting between UserSettings domain model and UserSettingsEntity database entity
 */

/**
 * Convert UserSettingsEntity (database) to UserSettings (domain)
 */
fun UserSettingsEntity.toDomain(): UserSettings {
    return UserSettings(
        id = id,
        theme = theme,
        language = language,
        isFirstRun = isFirstRun,
        defaultAccountId = defaultAccountId,
        autoMarkAsRead = autoMarkAsRead,
        autoMarkAsReadDelay = autoMarkAsReadDelay,
        showPreviewText = showPreviewText,
        previewLines = previewLines,
        showImages = showImages,
        autoDownloadAttachments = autoDownloadAttachments,
        maxAttachmentSize = maxAttachmentSize,
        emailsPerPage = emailsPerPage,
        showAvatars = showAvatars,
        showUnreadBadge = showUnreadBadge,
        groupByDate = groupByDate,
        useCompactView = useCompactView,
        showSenderInfo = showSenderInfo,
        globalNotificationsEnabled = globalNotificationsEnabled,
        notificationSound = notificationSound,
        vibrationEnabled = vibrationEnabled,
        ledEnabled = ledEnabled,
        quietHoursEnabled = quietHoursEnabled,
        quietHoursStart = quietHoursStart,
        quietHoursEnd = quietHoursEnd,
        backgroundSyncEnabled = backgroundSyncEnabled,
        syncInterval = syncInterval,
        wifiOnlySync = wifiOnlySync,
        batteryOptimization = batteryOptimization,
        idleSyncEnabled = idleSyncEnabled,
        biometricAuthEnabled = biometricAuthEnabled,
        autoLockEnabled = autoLockEnabled,
        autoLockDelay = autoLockDelay,
        hideContentInRecents = hideContentInRecents,
        blockExternalImages = blockExternalImages,
        warnUnsafeLinks = warnUnsafeLinks,
        defaultSignatureId = defaultSignatureId,
        replyQuoteOriginal = replyQuoteOriginal,
        forwardAsAttachment = forwardAsAttachment,
        autoSaveDrafts = autoSaveDrafts,
        autoSaveInterval = autoSaveInterval,
        requestReadReceipts = requestReadReceipts,
        useRichTextEditor = useRichTextEditor,
        cacheSize = cacheSize,
        autoClearCacheEnabled = autoClearCacheEnabled,
        autoClearCacheDays = autoClearCacheDays,
        keepDeletedEmailsDays = keepDeletedEmailsDays,
        developerMode = developerMode,
        debugLogging = debugLogging,
        crashReporting = crashReporting,
        analyticsEnabled = analyticsEnabled,
        settingsVersion = settingsVersion,
        lastUpdated = lastUpdated
    )
}

/**
 * Convert UserSettings (domain) to UserSettingsEntity (database)
 */
fun UserSettings.toEntity(): UserSettingsEntity {
    return UserSettingsEntity(
        id = id,
        theme = theme,
        language = language,
        isFirstRun = isFirstRun,
        defaultAccountId = defaultAccountId,
        autoMarkAsRead = autoMarkAsRead,
        autoMarkAsReadDelay = autoMarkAsReadDelay,
        showPreviewText = showPreviewText,
        previewLines = previewLines,
        showImages = showImages,
        autoDownloadAttachments = autoDownloadAttachments,
        maxAttachmentSize = maxAttachmentSize,
        emailsPerPage = emailsPerPage,
        showAvatars = showAvatars,
        showUnreadBadge = showUnreadBadge,
        groupByDate = groupByDate,
        useCompactView = useCompactView,
        showSenderInfo = showSenderInfo,
        globalNotificationsEnabled = globalNotificationsEnabled,
        notificationSound = notificationSound,
        vibrationEnabled = vibrationEnabled,
        ledEnabled = ledEnabled,
        quietHoursEnabled = quietHoursEnabled,
        quietHoursStart = quietHoursStart,
        quietHoursEnd = quietHoursEnd,
        backgroundSyncEnabled = backgroundSyncEnabled,
        syncInterval = syncInterval,
        wifiOnlySync = wifiOnlySync,
        batteryOptimization = batteryOptimization,
        idleSyncEnabled = idleSyncEnabled,
        biometricAuthEnabled = biometricAuthEnabled,
        autoLockEnabled = autoLockEnabled,
        autoLockDelay = autoLockDelay,
        hideContentInRecents = hideContentInRecents,
        blockExternalImages = blockExternalImages,
        warnUnsafeLinks = warnUnsafeLinks,
        defaultSignatureId = defaultSignatureId,
        replyQuoteOriginal = replyQuoteOriginal,
        forwardAsAttachment = forwardAsAttachment,
        autoSaveDrafts = autoSaveDrafts,
        autoSaveInterval = autoSaveInterval,
        requestReadReceipts = requestReadReceipts,
        useRichTextEditor = useRichTextEditor,
        cacheSize = cacheSize,
        autoClearCacheEnabled = autoClearCacheEnabled,
        autoClearCacheDays = autoClearCacheDays,
        keepDeletedEmailsDays = keepDeletedEmailsDays,
        developerMode = developerMode,
        debugLogging = debugLogging,
        crashReporting = crashReporting,
        analyticsEnabled = analyticsEnabled,
        settingsVersion = settingsVersion,
        lastUpdated = lastUpdated
    )
}