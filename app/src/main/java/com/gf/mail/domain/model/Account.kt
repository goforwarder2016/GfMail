package com.gf.mail.domain.model

import com.gf.mail.domain.model.ServerConfiguration

enum class EmailProvider(val displayName: String) {
    GMAIL("Gmail"),
    EXCHANGE("Exchange"),
    QQ("QQ邮箱"),
    NETEASE("网易邮箱"),
    OUTLOOK("Outlook"),
    YAHOO("Yahoo"),
    APPLE("iCloud"),
    IMAP("IMAP"),
    POP3("POP3")
}

enum class AuthenticationType {
    PASSWORD,
    OAUTH2,
    APP_PASSWORD
}

data class AuthenticationInfo(
    val type: AuthenticationType = AuthenticationType.PASSWORD,
    val hasPassword: Boolean = false,
    val hasOAuthToken: Boolean = false,
    val oauthExpiresAt: Long? = null
) {
    fun isTokenExpired(): Boolean {
        return oauthExpiresAt?.let { it < System.currentTimeMillis() } ?: false
    }
}

data class Account(
    val id: String,
    val email: String,
    val emailAddress: String, // Alias for email
    val displayName: String,
    val fullName: String, // Alias for displayName
    val provider: EmailProvider,
    val serverConfig: ServerConfiguration = ServerConfiguration(),
    val authInfo: AuthenticationInfo = AuthenticationInfo(),
    val signature: String? = null,
    val syncEnabled: Boolean = true,
    val syncFrequency: Int = 15, // minutes
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSync: Long? = null,
    val syncError: String? = null,
    val isEnabled: Boolean = true
) {
    // Removed getEmailAddress() and getDisplayName() methods to avoid platform declaration clash
    // Use emailAddress and displayName properties directly

    /**
     * Check if account is properly configured
     */
    fun isConfigured(): Boolean {
        return email.isNotBlank() && serverConfig.isValid()
    }

    /**
     * Check if account needs authentication
     */
    fun needsAuthentication(): Boolean {
        return when (authInfo.type) {
            AuthenticationType.PASSWORD -> !authInfo.hasPassword
            AuthenticationType.OAUTH2 -> !authInfo.hasOAuthToken || authInfo.isTokenExpired()
            AuthenticationType.APP_PASSWORD -> !authInfo.hasPassword
        }
    }
    
    /**
     * Check if account requires OAuth authentication
     */
    fun requiresOAuth(): Boolean {
        return authInfo.type == AuthenticationType.OAUTH2
    }

    /**
     * Get account summary for UI display
     */
    fun getSummary(): String {
        return "$displayName ($email)"
    }
}