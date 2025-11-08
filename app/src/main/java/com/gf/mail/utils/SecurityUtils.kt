package com.gf.mail.utils

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager as AndroidXBiometricManager
import androidx.biometric.BiometricPrompt as AndroidXBiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.net.ssl.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Comprehensive security utilities for the Gfmail application
 * Provides encryption, biometric authentication, and network security
 */
object SecurityUtils {

    private const val KEYSTORE_ALIAS = "gfmail_master_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_SIZE = 16

    /**
     * Data encryption utilities using Android Keystore
     */
    object DataEncryption {

        /**
         * Generate or retrieve master key from Android Keystore
         */
        private fun getOrCreateSecretKey(): SecretKey {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

            // Check if key already exists
            if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
                return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            }

            // Generate new key
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false) // Set to true for additional security
                .setRandomizedEncryptionRequired(true)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        }

        /**
         * Encrypt sensitive data
         */
        fun encryptData(plaintext: String): EncryptedData {
            try {
                val secretKey = getOrCreateSecretKey()
                val cipher = Cipher.getInstance(AES_MODE)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)

                val iv = cipher.iv
                val encryptedBytes = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))

                val encryptedData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Base64.getEncoder().encodeToString(encryptedBytes)
                } else {
                    android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT)
                }

                val ivString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Base64.getEncoder().encodeToString(iv)
                } else {
                    android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT)
                }

                return EncryptedData(encryptedData, ivString)
            } catch (e: Exception) {
                throw SecurityException("Failed to encrypt data", e)
            }
        }

        /**
         * Decrypt sensitive data
         */
        fun decryptData(encryptedData: EncryptedData): String {
            try {
                val secretKey = getOrCreateSecretKey()
                val cipher = Cipher.getInstance(AES_MODE)

                val iv = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Base64.getDecoder().decode(encryptedData.iv)
                } else {
                    android.util.Base64.decode(encryptedData.iv, android.util.Base64.DEFAULT)
                }

                val spec = GCMParameterSpec(TAG_SIZE * 8, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

                val encryptedBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Base64.getDecoder().decode(encryptedData.data)
                } else {
                    android.util.Base64.decode(encryptedData.data, android.util.Base64.DEFAULT)
                }

                val decryptedBytes = cipher.doFinal(encryptedBytes)
                return String(decryptedBytes, StandardCharsets.UTF_8)
            } catch (e: Exception) {
                throw SecurityException("Failed to decrypt data", e)
            }
        }

        /**
         * Securely clear sensitive data from memory
         */
        fun clearSensitiveData(data: CharArray) {
            data.fill('\u0000')
        }

        /**
         * Generate secure random salt
         */
        fun generateSalt(): ByteArray {
            val salt = ByteArray(32)
            java.security.SecureRandom().nextBytes(salt)
            return salt
        }
    }

    /**
     * Biometric authentication utilities
     */
    object BiometricAuthentication {

        /**
         * Check if biometric authentication is available
         */
        fun isBiometricAvailable(context: Context): BiometricAvailability {
            val biometricManager = AndroidXBiometricManager.from(context)

            return when (biometricManager.canAuthenticate(
                AndroidXBiometricManager.Authenticators.BIOMETRIC_STRONG
            )) {
                AndroidXBiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
                AndroidXBiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
                AndroidXBiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HARDWARE_UNAVAILABLE
                AndroidXBiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
                AndroidXBiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SECURITY_UPDATE_REQUIRED
                AndroidXBiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.UNSUPPORTED
                AndroidXBiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricAvailability.UNKNOWN
                else -> BiometricAvailability.UNKNOWN
            }
        }

        /**
         * Check if device has secure lock screen
         */
        fun hasSecureLockScreen(context: Context): Boolean {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            return keyguardManager.isKeyguardSecure
        }

        /**
         * Authenticate with biometrics
         */
        suspend fun authenticateWithBiometrics(
            activity: FragmentActivity,
            title: String,
            subtitle: String,
            description: String,
            negativeButtonText: String
        ): BiometricAuthResult = suspendCancellableCoroutine { continuation ->

            val biometricPrompt = AndroidXBiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                object : AndroidXBiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result: AndroidXBiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        if (continuation.isActive) {
                            continuation.resume(BiometricAuthResult.Success)
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (continuation.isActive) {
                            continuation.resume(
                                BiometricAuthResult.Error(errorCode, errString.toString())
                            )
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        if (continuation.isActive) {
                            continuation.resume(BiometricAuthResult.Failed)
                        }
                    }
                }
            )

            val promptInfo = AndroidXBiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(AndroidXBiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()

            try {
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }

            continuation.invokeOnCancellation {
                try {
                    biometricPrompt.cancelAuthentication()
                } catch (e: Exception) {
                    // Ignore cancellation errors
                }
            }
        }

        /**
         * Authenticate with device credentials (PIN, pattern, password)
         */
        suspend fun authenticateWithDeviceCredentials(
            activity: FragmentActivity,
            title: String,
            subtitle: String,
            description: String
        ): BiometricAuthResult = suspendCancellableCoroutine { continuation ->

            val biometricPrompt = AndroidXBiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                object : AndroidXBiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result: AndroidXBiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        if (continuation.isActive) {
                            continuation.resume(BiometricAuthResult.Success)
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (continuation.isActive) {
                            continuation.resume(
                                BiometricAuthResult.Error(errorCode, errString.toString())
                            )
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        if (continuation.isActive) {
                            continuation.resume(BiometricAuthResult.Failed)
                        }
                    }
                }
            )

            val promptInfo = AndroidXBiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setAllowedAuthenticators(
                    AndroidXBiometricManager.Authenticators.BIOMETRIC_STRONG or
                        AndroidXBiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            try {
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }

            continuation.invokeOnCancellation {
                try {
                    biometricPrompt.cancelAuthentication()
                } catch (e: Exception) {
                    // Ignore cancellation errors
                }
            }
        }
    }

    /**
     * Network security utilities
     */
    object NetworkSecurity {

        /**
         * Create SSL context with certificate pinning
         */
        fun createSSLContextWithCertificatePinning(
            certificates: List<String>
        ): SSLContext {
            try {
                // Create certificate factory
                val certificateFactory = CertificateFactory.getInstance("X.509")

                // Create trust store with pinned certificates
                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
                keyStore.load(null, null)

                certificates.forEachIndexed { index, cert ->
                    val certificate = certificateFactory.generateCertificate(
                        ByteArrayInputStream(cert.toByteArray())
                    )
                    keyStore.setCertificateEntry("cert_$index", certificate)
                }

                // Create trust manager factory
                val trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
                )
                trustManagerFactory.init(keyStore)

                // Create SSL context
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustManagerFactory.trustManagers, null)

                return sslContext
            } catch (e: Exception) {
                throw SecurityException("Failed to create SSL context with certificate pinning", e)
            }
        }

        /**
         * Create hostname verifier for additional security
         */
        fun createSecureHostnameVerifier(allowedHostnames: List<String>): HostnameVerifier {
            return HostnameVerifier { hostname, session ->
                allowedHostnames.contains(hostname)
            }
        }

        /**
         * Validate certificate chain
         */
        fun validateCertificateChain(certificates: Array<X509Certificate>): Boolean {
            return try {
                // Basic validation - in production, implement more thorough checks
                certificates.isNotEmpty() &&
                    certificates.all { cert ->
                        val now = java.util.Date()
                        cert.notBefore.before(now) && cert.notAfter.after(now)
                    }
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Generate certificate fingerprint (SHA-256)
         */
        fun getCertificateFingerprint(certificate: X509Certificate): String {
            return try {
                val digest = java.security.MessageDigest.getInstance("SHA-256")
                val hash = digest.digest(certificate.encoded)
                hash.joinToString("") { String.format("%02x", it) }
            } catch (e: Exception) {
                throw SecurityException("Failed to generate certificate fingerprint", e)
            }
        }
    }

    /**
     * Session security utilities
     */
    object SessionSecurity {

        /**
         * Generate secure session token
         */
        fun generateSessionToken(): String {
            val random = java.security.SecureRandom()
            val tokenBytes = ByteArray(32)
            random.nextBytes(tokenBytes)

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getEncoder().encodeToString(tokenBytes)
            } else {
                android.util.Base64.encodeToString(tokenBytes, android.util.Base64.NO_WRAP)
            }
        }

        /**
         * Validate session token format
         */
        fun isValidSessionToken(token: String?): Boolean {
            if (token.isNullOrBlank()) return false

            return try {
                val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Base64.getDecoder().decode(token)
                } else {
                    android.util.Base64.decode(token, android.util.Base64.NO_WRAP)
                }
                decoded.size == 32
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Calculate session expiry time
         */
        fun calculateSessionExpiry(durationMillis: Long): Long {
            return System.currentTimeMillis() + durationMillis
        }

        /**
         * Check if session is expired
         */
        fun isSessionExpired(expiryTime: Long): Boolean {
            return System.currentTimeMillis() > expiryTime
        }
    }
}

/**
 * Data classes for security operations
 */
data class EncryptedData(
    val data: String,
    val iv: String
)

enum class BiometricAvailability {
    AVAILABLE,
    NO_HARDWARE,
    HARDWARE_UNAVAILABLE,
    NONE_ENROLLED,
    SECURITY_UPDATE_REQUIRED,
    UNSUPPORTED,
    UNKNOWN
}

sealed class BiometricAuthResult {
    object Success : BiometricAuthResult()
    object Failed : BiometricAuthResult()
    data class Error(val errorCode: Int, val errorMessage: String) : BiometricAuthResult()
}

/**
 * Security configuration data class
 */
data class SecurityConfiguration(
    val biometricAuthEnabled: Boolean = false,
    val autoLockEnabled: Boolean = true,
    val autoLockTimeoutMinutes: Int = 5,
    val certificatePinningEnabled: Boolean = true,
    val allowedHostnames: List<String> = emptyList(),
    val sessionTimeoutMinutes: Int = 30,
    val requireBiometricForSensitiveOperations: Boolean = false,
    val encryptSensitiveData: Boolean = true
)

/**
 * Security validation utilities
 */
object SecurityValidator {

    /**
     * Validate password strength
     */
    fun validatePasswordStrength(password: String): PasswordStrength {
        var score = 0
        val requirements = mutableListOf<String>()

        // Length check
        if (password.length >= 8) score++ else requirements.add("At least 8 characters")
        if (password.length >= 12) score++

        // Character variety checks
        if (password.any {
                it.isUpperCase()
            }) score++ else requirements.add("At least one uppercase letter")
        if (password.any {
                it.isLowerCase()
            }) score++ else requirements.add("At least one lowercase letter")
        if (password.any { it.isDigit() }) score++ else requirements.add("At least one number")
        if (password.any {
                !it.isLetterOrDigit()
            }) score++ else requirements.add("At least one special character")

        // Common patterns check
        val commonPatterns = listOf("123", "password", "admin", "qwerty")
        if (commonPatterns.none { password.lowercase().contains(it) }) score++

        return when {
            score >= 6 -> PasswordStrength.STRONG
            score >= 4 -> PasswordStrength.MEDIUM
            score >= 2 -> PasswordStrength.WEAK
            else -> PasswordStrength.VERY_WEAK
        }.copy(missingRequirements = requirements)
    }

    /**
     * Validate email address format for security
     */
    fun validateEmailSecurity(email: String): Boolean {
        // Basic email format validation
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailRegex.matches(email)) return false

        // Check for suspicious patterns
        val suspiciousPatterns = listOf(
            "javascript:",
            "<script",
            "eval(",
            "document.cookie"
        )

        return suspiciousPatterns.none { email.contains(it, ignoreCase = true) }
    }
}

/**
 * Password strength data class
 */
data class PasswordStrength(
    val level: PasswordStrengthLevel,
    val missingRequirements: List<String> = emptyList()
) {
    companion object {
        val VERY_WEAK = PasswordStrength(PasswordStrengthLevel.VERY_WEAK)
        val WEAK = PasswordStrength(PasswordStrengthLevel.WEAK)
        val MEDIUM = PasswordStrength(PasswordStrengthLevel.MEDIUM)
        val STRONG = PasswordStrength(PasswordStrengthLevel.STRONG)
    }
}

enum class PasswordStrengthLevel {
    VERY_WEAK, WEAK, MEDIUM, STRONG
}
