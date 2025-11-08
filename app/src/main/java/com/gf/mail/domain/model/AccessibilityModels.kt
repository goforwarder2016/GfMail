package com.gf.mail.domain.model

/**
 * Accessibility settings domain model
 */
data class AccessibilitySettings(
    val highContrastMode: Boolean = false,
    val largeTextMode: Boolean = false,
    val colorBlindFriendly: Boolean = false,
    val focusIndicator: Boolean = true,
    val touchTargetSize: Float = 1.0f,
    val keyboardNavigation: Boolean = true,
    val reduceMotion: Boolean = false,
    val screenReaderSupport: Boolean = true,
    val fontSize: Float = 1.0f
)

/**
 * Accessibility recommendation domain model
 */
data class AccessibilityRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val type: AccessibilityRecommendationType,
    val priority: AccessibilityRecommendationPriority,
    val actionRequired: Boolean = false
)

/**
 * Accessibility recommendation type enum
 */
enum class AccessibilityRecommendationType {
    VISUAL,
    MOTOR,
    COGNITIVE,
    AUDITORY,
    SPEECH,
    LANGUAGE
}

/**
 * Accessibility recommendation priority enum
 */
enum class AccessibilityRecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}