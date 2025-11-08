package com.gf.mail.domain.model

import com.gf.mail.domain.model.EncryptionType

/**
 * Result of testing email server connection
 */
data class ConnectionTestResult(
    val isSuccessful: Boolean,
    val imapResult: ConnectionResult? = null,
    val smtpResult: ConnectionResult? = null,
    val errorMessage: String? = null
) {
    fun isCompletelySuccessful(): Boolean {
        return isSuccessful &&
            (imapResult?.isSuccessful == true) &&
            (smtpResult?.isSuccessful == true)
    }
}

/**
 * Result for individual server connection (IMAP or SMTP)
 */
data class ConnectionResult(
    val isSuccessful: Boolean,
    val serverType: ServerType,
    val host: String,
    val port: Int,
    val encryption: EncryptionType,
    val responseMessage: String? = null,
    val errorMessage: String? = null,
    val testDurationMs: Long = 0
)

enum class ServerType {
    IMAP, SMTP
}
