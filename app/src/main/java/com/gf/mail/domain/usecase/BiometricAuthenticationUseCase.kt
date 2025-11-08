package com.gf.mail.domain.usecase

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import com.gf.mail.domain.model.BiometricAvailability
import com.gf.mail.domain.model.BiometricAuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Use case for biometric authentication operations
 */
class BiometricAuthenticationUseCase() {

    private val _authResult = MutableStateFlow<BiometricAuthResult>(BiometricAuthResult.Idle)
    val authResult: StateFlow<BiometricAuthResult> = _authResult.asStateFlow()

    /**
     * Check if biometric authentication is available on the device
     */
    fun checkBiometricAvailability(context: Context): BiometricAvailability {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.UNSUPPORTED
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricAvailability.UNKNOWN
            else -> BiometricAvailability.UNKNOWN
        }
    }

    /**
     * Get a user-friendly message for biometric availability status
     */
    fun getBiometricAvailabilityMessage(context: Context): String {
        val biometricAvailability = checkBiometricAvailability(context)
        return when (biometricAvailability) {
            BiometricAvailability.AVAILABLE -> "Biometric authentication is available"
            BiometricAvailability.NONE_ENROLLED -> "No biometrics enrolled. Please enroll biometrics in your device settings."
            BiometricAvailability.NO_HARDWARE -> "Biometric authentication is not supported on this device"
            BiometricAvailability.HARDWARE_UNAVAILABLE -> "Biometric hardware is temporarily unavailable"
            BiometricAvailability.SECURITY_UPDATE_REQUIRED -> "Security update required for biometric authentication"
            else -> "Authentication not available on this device"
        }
    }

    /**
     * Authenticate with biometrics
     */
    suspend fun authenticateWithBiometrics(
        context: Context,
        title: String,
        subtitle: String,
        description: String,
        cancelText: String
    ): BiometricAuthResult {
        // TODO: Implement biometric authentication
        return BiometricAuthResult.Success
    }

    /**
     * Authenticate with device credentials
     */
    suspend fun authenticateWithDeviceCredentials(
        context: Context,
        title: String,
        subtitle: String,
        description: String,
        cancelText: String
    ): BiometricAuthResult {
        // TODO: Implement device credential authentication
        return BiometricAuthResult.Success
    }

    /**
     * Check if device has secure lock screen
     */
    fun hasSecureLockScreen(context: Context): Boolean {
        // TODO: Implement secure lock screen check
        return true
    }
}