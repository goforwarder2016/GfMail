package com.gf.mail.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Server configuration entity for advanced IMAP/SMTP settings
 */
@Entity(tableName = "server_configurations")
data class ServerConfigurationEntity(
    @PrimaryKey
    val id: String,
    val accountId: String,

    // IMAP Settings
    val imapHost: String,
    val imapPort: Int = 993,
    val imapSecurity: String = "SSL_TLS", // SSL_TLS, STARTTLS, NONE
    val imapAuthMethod: String = "PLAIN", // PLAIN, LOGIN, OAUTH2
    val imapConnectionTimeout: Int = 30000, // milliseconds
    val imapReadTimeout: Int = 60000, // milliseconds
    val imapMaxConnections: Int = 3,
    val imapIdleTimeout: Int = 1800000, // 30 minutes

    // SMTP Settings
    val smtpHost: String,
    val smtpPort: Int = 587,
    val smtpSecurity: String = "STARTTLS", // SSL_TLS, STARTTLS, NONE
    val smtpAuthMethod: String = "PLAIN", // PLAIN, LOGIN, OAUTH2
    val smtpConnectionTimeout: Int = 30000, // milliseconds
    val smtpRequireAuth: Boolean = true,

    // Advanced IMAP Settings
    val usePushFolders: Boolean = true,
    val pushFolders: String = "[\"INBOX\"]", // JSON string of folder names
    val pollInterval: Int = 300, // seconds
    val maxMessageAge: Int = 0, // 0 = unlimited, days
    val messageDownloadSize: Int = 32768, // bytes, 0 = unlimited
    val autoDownloadSize: Int = 8192, // bytes

    // Advanced SMTP Settings
    val useSmtpPipelining: Boolean = true,
    val smtpLocalAddress: String? = null,
    val smtpLocalPort: Int = 0, // 0 = auto

    // Advanced Security
    val certificatePinning: Boolean = false,
    val trustedCertificates: String = "[]", // JSON string of certificates
    val allowUntrustedCertificates: Boolean = false,
    val clientCertificateAlias: String? = null,

    // Proxy Settings
    val useProxy: Boolean = false,
    val proxyType: String = "HTTP", // HTTP, SOCKS4, SOCKS5
    val proxyHost: String? = null,
    val proxyPort: Int = 0,
    val proxyUsername: String? = null,
    val proxyPassword: String? = null, // Encrypted

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)