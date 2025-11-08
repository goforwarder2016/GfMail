package com.gf.mail.domain.model

/**
 * Security settings domain model
 */
data class SecuritySetting(
    val biometricAuthEnabled: Boolean = false,
    val autoLockEnabled: Boolean = false,
    val autoLockTimeout: Int = 5, // minutes
    val advancedSecurityEnabled: Boolean = false,
    val trustedHosts: List<String> = emptyList(),
    val sessionTimeout: Int = 30, // minutes
    val passwordStrength: Int = 0, // 0-100
    val blockExternalImages: Boolean = true,
    val warnUnsafeLinks: Boolean = true
)

/**
 * Security recommendation domain model
 */
data class SecurityRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val type: SecurityRecommendationType,
    val priority: SecurityRecommendationPriority,
    val actionRequired: Boolean = false
)

/**
 * Security recommendation type enum
 */
enum class SecurityRecommendationType {
    BIOMETRIC_AUTH,
    AUTO_LOCK,
    CERTIFICATE_PINNING,
    DATA_ENCRYPTION,
    SESSION_MANAGEMENT,
    PASSWORD_STRENGTH
}

/**
 * Security recommendation priority enum
 */
enum class SecurityRecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}