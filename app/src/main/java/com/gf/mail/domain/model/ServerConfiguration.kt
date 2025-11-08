package com.gf.mail.domain.model

import com.gf.mail.domain.model.EncryptionType

/**
 * Server configuration domain model
 */
data class ServerConfiguration(
    val imapHost: String? = null,
    val imapPort: Int = 993,
    val imapEncryption: EncryptionType = EncryptionType.SSL,
    val imapUseSsl: Boolean = true, // Alias for SSL encryption
    val imapUseTls: Boolean = false, // Alias for TLS encryption
    val smtpHost: String? = null,
    val smtpPort: Int = 587,
    val smtpEncryption: EncryptionType = EncryptionType.STARTTLS,
    val smtpUseSsl: Boolean = false, // Alias for SSL encryption
    val smtpUseTls: Boolean = true, // Alias for TLS encryption
    val useSsl: Boolean = true, // General SSL flag
    val useTls: Boolean = false // General TLS flag
) {
    /**
     * Check if configuration is valid
     */
    fun isValid(): Boolean {
        return !imapHost.isNullOrBlank() && !smtpHost.isNullOrBlank()
    }
}