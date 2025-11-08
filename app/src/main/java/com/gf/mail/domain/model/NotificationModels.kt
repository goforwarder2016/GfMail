package com.gf.mail.domain.model

/**
 * Push notification settings domain model
 */
data class PushNotificationSettings(
    val isEnabled: Boolean = true,
    val sound: String = "default",
    val vibration: Boolean = true,
    val showContentOnLockScreen: Boolean = false
)