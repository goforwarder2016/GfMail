package com.gf.mail.domain.model

/**
 * Biometric availability status
 */
enum class BiometricAvailability {
    AVAILABLE,
    NO_HARDWARE,
    HARDWARE_UNAVAILABLE,
    NONE_ENROLLED,
    SECURITY_UPDATE_REQUIRED,
    UNSUPPORTED,
    UNKNOWN
}

/**
 * Biometric authentication result
 */
sealed class BiometricAuthResult {
    object Idle : BiometricAuthResult()
    object Success : BiometricAuthResult()
    data class Error(val message: String, val errorCode: Int) : BiometricAuthResult()
    object Cancelled : BiometricAuthResult()
    object Failed : BiometricAuthResult()
}