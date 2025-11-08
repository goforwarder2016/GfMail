package com.gf.mail.data.security.advanced

import android.content.Context
import android.util.Log
import com.gf.mail.data.security.CredentialEncryption
import com.gf.mail.domain.usecase.ManageSecuritySettingsUseCase
import com.gf.mail.utils.SecurityUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced security manager for enterprise-grade security features
 */
@Singleton
class AdvancedSecurityManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val credentialEncryption: CredentialEncryption,
    private val securitySettingsUseCase: ManageSecuritySettingsUseCase
) {
    
    companion object {
        private const val TAG = "AdvancedSecurityManager"
        
        // Security thresholds
        private const val MAX_LOGIN_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 15 * 60 * 1000L // 15 minutes
        private const val SESSION_TIMEOUT_MS = 30 * 60 * 1000L // 30 minutes
        private const val PASSWORD_MIN_LENGTH = 8
        private const val PASSWORD_REQUIRE_UPPERCASE = true
        private const val PASSWORD_REQUIRE_LOWERCASE = true
        private const val PASSWORD_REQUIRE_NUMBERS = true
        private const val PASSWORD_REQUIRE_SYMBOLS = true
    }
    
    private val securityScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Security state
    private val _securityState = MutableStateFlow(AdvancedSecurityState())
    val securityState: StateFlow<AdvancedSecurityState> = _securityState.asStateFlow()
    
    // Login attempt tracking
    private val loginAttempts = ConcurrentHashMap<String, MutableList<Long>>()
    private val lockedAccounts = ConcurrentHashMap<String, Long>()
    
    // Session management
    private val activeSessions = ConcurrentHashMap<String, SecuritySession>()
    private val sessionTokens = ConcurrentHashMap<String, String>()
    
    // Security events
    private val _securityEvents = MutableStateFlow<List<SecurityEvent>>(emptyList())
    val securityEvents: StateFlow<List<SecurityEvent>> = _securityEvents.asStateFlow()
    
    // Threat detection
    private val _threatLevel = MutableStateFlow(ThreatLevel.LOW)
    val threatLevel: StateFlow<ThreatLevel> = _threatLevel.asStateFlow()
    
    init {
        startSecurityMonitoring()
    }
    
    /**
     * Start security monitoring
     */
    private fun startSecurityMonitoring() {
        securityScope.launch {
            while (isActive) {
                try {
                    monitorSecurityThreats()
                    cleanupExpiredSessions()
                    cleanupExpiredLockouts()
                    delay(60000) // Check every minute
                } catch (e: Exception) {
                    Log.e(TAG, "Error in security monitoring", e)
                }
            }
        }
    }
    
    /**
     * Monitor security threats
     */
    private suspend fun monitorSecurityThreats() {
        val currentThreats = analyzeSecurityThreats()
        _threatLevel.value = currentThreats
        
        if (currentThreats != ThreatLevel.LOW) {
            val event = SecurityEvent.ThreatDetected(
                threatLevel = currentThreats,
                timestamp = System.currentTimeMillis(),
                description = "Security threat detected: $currentThreats"
            )
            addSecurityEvent(event)
        }
    }
    
    /**
     * Analyze current security threats
     */
    private fun analyzeSecurityThreats(): ThreatLevel {
        val lockedAccountCount = lockedAccounts.size
        val failedAttempts = loginAttempts.values.sumOf { it.size }
        val activeSessionCount = activeSessions.size
        
        return when {
            lockedAccountCount > 3 || failedAttempts > 20 -> ThreatLevel.CRITICAL
            lockedAccountCount > 1 || failedAttempts > 10 -> ThreatLevel.HIGH
            failedAttempts > 5 || activeSessionCount > 10 -> ThreatLevel.MEDIUM
            else -> ThreatLevel.LOW
        }
    }
    
    /**
     * Authenticate user with advanced security checks
     */
    suspend fun authenticateUser(
        accountId: String,
        password: String,
        biometricData: String? = null
    ): AuthenticationResult {
        return try {
            // Check if account is locked
            if (isAccountLocked(accountId)) {
                return AuthenticationResult.Locked(
                    lockoutTimeRemaining = getLockoutTimeRemaining(accountId)
                )
            }
            
            // Validate password strength
            val passwordValidation = validatePasswordStrength(password)
            if (!passwordValidation.isValid) {
                recordFailedLoginAttempt(accountId)
                return AuthenticationResult.InvalidPassword(passwordValidation.errors)
            }
            
            // Perform authentication
            val authResult = performAuthentication(accountId, password, biometricData)
            
            when (authResult) {
                is AuthenticationResult.Success -> {
                    // Clear failed attempts on successful login
                    clearFailedLoginAttempts(accountId)
                    
                    // Create security session
                    val session = createSecuritySession(accountId, authResult.sessionToken)
                    activeSessions[accountId] = session
                    
                    // Log successful authentication
                    addSecurityEvent(SecurityEvent.AuthenticationSuccess(
                        accountId = accountId,
                        method = authResult.authMethod,
                        timestamp = System.currentTimeMillis()
                    ))
                    
                    authResult
                }
                is AuthenticationResult.Failed -> {
                    recordFailedLoginAttempt(accountId)
                    
                    // Log failed authentication
                    addSecurityEvent(SecurityEvent.AuthenticationFailed(
                        accountId = accountId,
                        reason = authResult.reason,
                        timestamp = System.currentTimeMillis()
                    ))
                    
                    authResult
                }
                else -> authResult
            }
        } catch (e: Exception) {
            Log.e(TAG, "Authentication error for account: $accountId", e)
            addSecurityEvent(SecurityEvent.AuthenticationError(
                accountId = accountId,
                error = e.message ?: "Unknown error",
                timestamp = System.currentTimeMillis()
            ))
            AuthenticationResult.Error(e.message ?: "Authentication failed")
        }
    }
    
    /**
     * Perform actual authentication
     */
    private suspend fun performAuthentication(
        accountId: String,
        password: String,
        biometricData: String?
    ): AuthenticationResult {
        // Check stored credentials
        val storedPassword = credentialEncryption.getPasswordSecurely(accountId)
        if (storedPassword == null) {
            return AuthenticationResult.Failed("Account not found")
        }
        
        // Verify password
        if (storedPassword != password) {
            return AuthenticationResult.Failed("Invalid password")
        }
        
        // Verify biometric data if provided
        if (biometricData != null) {
            val biometricResult = verifyBiometricData(accountId, biometricData)
            if (!biometricResult) {
                return AuthenticationResult.Failed("Biometric verification failed")
            }
        }
        
        // Generate session token
        val sessionToken = generateSecureSessionToken()
        sessionTokens[sessionToken] = accountId
        
        return AuthenticationResult.Success(
            sessionToken = sessionToken,
            authMethod = if (biometricData != null) AuthMethod.BIOMETRIC else AuthMethod.PASSWORD
        )
    }
    
    /**
     * Verify biometric data
     */
    private suspend fun verifyBiometricData(accountId: String, biometricData: String): Boolean {
        // This would typically use biometric verification APIs
        // For now, return true as a placeholder
        return true
    }
    
    /**
     * Generate secure session token
     */
    private fun generateSecureSessionToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Create security session
     */
    private fun createSecuritySession(accountId: String, sessionToken: String): SecuritySession {
        return SecuritySession(
            accountId = accountId,
            sessionToken = sessionToken,
            createdAt = System.currentTimeMillis(),
            lastActivity = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + SESSION_TIMEOUT_MS,
            ipAddress = "unknown", // Would get from network
            userAgent = "GfmailApp", // Would get from request
            isActive = true
        )
    }
    
    /**
     * Validate password strength
     */
    private fun validatePasswordStrength(password: String): PasswordValidationResult {
        val errors = mutableListOf<String>()
        
        if (password.length < PASSWORD_MIN_LENGTH) {
            errors.add("Password must be at least $PASSWORD_MIN_LENGTH characters long")
        }
        
        if (PASSWORD_REQUIRE_UPPERCASE && !password.any { it.isUpperCase() }) {
            errors.add("Password must contain at least one uppercase letter")
        }
        
        if (PASSWORD_REQUIRE_LOWERCASE && !password.any { it.isLowerCase() }) {
            errors.add("Password must contain at least one lowercase letter")
        }
        
        if (PASSWORD_REQUIRE_NUMBERS && !password.any { it.isDigit() }) {
            errors.add("Password must contain at least one number")
        }
        
        if (PASSWORD_REQUIRE_SYMBOLS && !password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) {
            errors.add("Password must contain at least one special character")
        }
        
        // Check for common passwords
        if (isCommonPassword(password)) {
            errors.add("Password is too common, please choose a more unique password")
        }
        
        return PasswordValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            strength = calculatePasswordStrength(password)
        )
    }
    
    /**
     * Check if password is common
     */
    private fun isCommonPassword(password: String): Boolean {
        val commonPasswords = listOf(
            "password", "123456", "123456789", "qwerty", "abc123",
            "password123", "admin", "letmein", "welcome", "monkey"
        )
        return commonPasswords.contains(password.lowercase())
    }
    
    /**
     * Calculate password strength
     */
    private fun calculatePasswordStrength(password: String): PasswordStrength {
        var score = 0
        
        // Length score
        score += when {
            password.length >= 12 -> 3
            password.length >= 8 -> 2
            password.length >= 6 -> 1
            else -> 0
        }
        
        // Character variety score
        if (password.any { it.isUpperCase() }) score += 1
        if (password.any { it.isLowerCase() }) score += 1
        if (password.any { it.isDigit() }) score += 1
        if (password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) score += 1
        
        return when (score) {
            in 0..2 -> PasswordStrength.WEAK
            in 3..4 -> PasswordStrength.MEDIUM
            in 5..6 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }
    }
    
    /**
     * Record failed login attempt
     */
    private fun recordFailedLoginAttempt(accountId: String) {
        val currentTime = System.currentTimeMillis()
        val attempts = loginAttempts.getOrPut(accountId) { mutableListOf() }
        attempts.add(currentTime)
        
        // Remove attempts older than 1 hour
        attempts.removeAll { it < currentTime - 3600000 }
        
        // Check if account should be locked
        if (attempts.size >= MAX_LOGIN_ATTEMPTS) {
            lockedAccounts[accountId] = currentTime + LOCKOUT_DURATION_MS
            addSecurityEvent(SecurityEvent.AccountLocked(
                accountId = accountId,
                reason = "Too many failed login attempts",
                timestamp = currentTime
            ))
        }
    }
    
    /**
     * Clear failed login attempts
     */
    private fun clearFailedLoginAttempts(accountId: String) {
        loginAttempts.remove(accountId)
        lockedAccounts.remove(accountId)
    }
    
    /**
     * Check if account is locked
     */
    private fun isAccountLocked(accountId: String): Boolean {
        val lockoutTime = lockedAccounts[accountId] ?: return false
        return System.currentTimeMillis() < lockoutTime
    }
    
    /**
     * Get lockout time remaining
     */
    private fun getLockoutTimeRemaining(accountId: String): Long {
        val lockoutTime = lockedAccounts[accountId] ?: return 0
        return maxOf(0, lockoutTime - System.currentTimeMillis())
    }
    
    /**
     * Validate session
     */
    suspend fun validateSession(sessionToken: String): SessionValidationResult {
        val accountId = sessionTokens[sessionToken] ?: return SessionValidationResult.Invalid
        
        val session = activeSessions[accountId] ?: return SessionValidationResult.Invalid
        
        if (!session.isActive || System.currentTimeMillis() > session.expiresAt) {
            return SessionValidationResult.Expired
        }
        
        // Update last activity
        session.lastActivity = System.currentTimeMillis()
        
        return SessionValidationResult.Valid(accountId)
    }
    
    /**
     * Invalidate session
     */
    suspend fun invalidateSession(sessionToken: String) {
        val accountId = sessionTokens.remove(sessionToken)
        if (accountId != null) {
            activeSessions.remove(accountId)
            addSecurityEvent(SecurityEvent.SessionInvalidated(
                accountId = accountId,
                timestamp = System.currentTimeMillis()
            ))
        }
    }
    
    /**
     * Cleanup expired sessions
     */
    private fun cleanupExpiredSessions() {
        val currentTime = System.currentTimeMillis()
        val expiredSessions = activeSessions.filter { it.value.expiresAt < currentTime }
        
        expiredSessions.forEach { (accountId, session) ->
            activeSessions.remove(accountId)
            sessionTokens.remove(session.sessionToken)
            addSecurityEvent(SecurityEvent.SessionExpired(
                accountId = accountId,
                timestamp = currentTime
            ))
        }
    }
    
    /**
     * Cleanup expired lockouts
     */
    private fun cleanupExpiredLockouts() {
        val currentTime = System.currentTimeMillis()
        val expiredLockouts = lockedAccounts.filter { it.value < currentTime }
        
        expiredLockouts.forEach { (accountId, _) ->
            lockedAccounts.remove(accountId)
            loginAttempts.remove(accountId)
        }
    }
    
    /**
     * Add security event
     */
    private fun addSecurityEvent(event: SecurityEvent) {
        val currentEvents = _securityEvents.value.toMutableList()
        currentEvents.add(event)
        
        // Keep only last 100 events
        if (currentEvents.size > 100) {
            currentEvents.removeAt(0)
        }
        
        _securityEvents.value = currentEvents
    }
    
    /**
     * Get security audit log
     */
    fun getSecurityAuditLog(): List<SecurityEvent> {
        return _securityEvents.value
    }
    
    /**
     * Get security statistics
     */
    fun getSecurityStatistics(): SecurityStatistics {
        val currentTime = System.currentTimeMillis()
        val last24Hours = currentTime - 86400000L
        
        val recentEvents = _securityEvents.value.filter { it.timestamp > last24Hours }
        val failedLogins = recentEvents.count { it is SecurityEvent.AuthenticationFailed }
        val successfulLogins = recentEvents.count { it is SecurityEvent.AuthenticationSuccess }
        val lockedAccountsCount = recentEvents.count { it is SecurityEvent.AccountLocked }
        
        return SecurityStatistics(
            totalEvents = _securityEvents.value.size,
            eventsLast24Hours = recentEvents.size,
            failedLoginsLast24Hours = failedLogins,
            successfulLoginsLast24Hours = successfulLogins,
            lockedAccountsLast24Hours = lockedAccountsCount,
            activeSessions = activeSessions.size,
            lockedAccounts = lockedAccounts.size,
            threatLevel = _threatLevel.value
        )
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        securityScope.cancel()
    }
}

/**
 * Advanced security state
 */
data class AdvancedSecurityState(
    val isMonitoring: Boolean = true,
    val threatLevel: ThreatLevel = ThreatLevel.LOW,
    val activeSessions: Int = 0,
    val lockedAccounts: Int = 0,
    val lastSecurityCheck: Long = System.currentTimeMillis()
)

/**
 * Threat level enumeration
 */
enum class ThreatLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Security session
 */
data class SecuritySession(
    val accountId: String,
    val sessionToken: String,
    val createdAt: Long,
    var lastActivity: Long,
    val expiresAt: Long,
    val ipAddress: String,
    val userAgent: String,
    var isActive: Boolean
)

/**
 * Authentication result
 */
sealed class AuthenticationResult {
    data class Success(
        val sessionToken: String,
        val authMethod: AuthMethod
    ) : AuthenticationResult()
    
    data class Failed(val reason: String) : AuthenticationResult()
    
    data class Locked(val lockoutTimeRemaining: Long) : AuthenticationResult()
    
    data class InvalidPassword(val errors: List<String>) : AuthenticationResult()
    
    data class Error(val message: String) : AuthenticationResult()
}

/**
 * Authentication method
 */
enum class AuthMethod {
    PASSWORD,
    BIOMETRIC,
    OAUTH2,
    DEVICE_CREDENTIAL
}

/**
 * Password validation result
 */
data class PasswordValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val strength: PasswordStrength
)

/**
 * Password strength
 */
enum class PasswordStrength {
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG
}

/**
 * Session validation result
 */
sealed class SessionValidationResult {
    object Invalid : SessionValidationResult()
    object Expired : SessionValidationResult()
    data class Valid(val accountId: String) : SessionValidationResult()
}

/**
 * Security event
 */
sealed class SecurityEvent {
    abstract val timestamp: Long
    
    data class AuthenticationSuccess(
        val accountId: String,
        val method: AuthMethod,
        override val timestamp: Long
    ) : SecurityEvent()
    
    data class AuthenticationFailed(
        val accountId: String,
        val reason: String,
        override val timestamp: Long
    ) : SecurityEvent()
    
    data class AuthenticationError(
        val accountId: String,
        val error: String,
        override val timestamp: Long
    ) : SecurityEvent()
    
    data class AccountLocked(
        val accountId: String,
        val reason: String,
        override val timestamp: Long
    ) : SecurityEvent()
    
    data class SessionExpired(
        val accountId: String,
        override val timestamp: Long
    ) : SecurityEvent()
    
    data class SessionInvalidated(
        val accountId: String,
        override val timestamp: Long
    ) : SecurityEvent()
    
    data class ThreatDetected(
        val threatLevel: ThreatLevel,
        override val timestamp: Long,
        val description: String
    ) : SecurityEvent()
}

/**
 * Security statistics
 */
data class SecurityStatistics(
    val totalEvents: Int,
    val eventsLast24Hours: Int,
    val failedLoginsLast24Hours: Int,
    val successfulLoginsLast24Hours: Int,
    val lockedAccountsLast24Hours: Int,
    val activeSessions: Int,
    val lockedAccounts: Int,
    val threatLevel: ThreatLevel
)