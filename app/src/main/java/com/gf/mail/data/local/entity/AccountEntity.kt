package com.gf.mail.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["provider"]),
        Index(value = ["is_active"])
    ]
)
data class AccountEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "display_name")
    val displayName: String,

    @ColumnInfo(name = "provider")
    val provider: String, // "gmail", "exchange", "imap", "pop3"

    // Server Configuration
    @ColumnInfo(name = "imap_host")
    val imapHost: String? = null,

    @ColumnInfo(name = "imap_port")
    val imapPort: Int = 993,

    @ColumnInfo(name = "imap_encryption")
    val imapEncryption: String = "SSL", // "SSL", "STARTTLS", "NONE"

    @ColumnInfo(name = "smtp_host")
    val smtpHost: String? = null,

    @ColumnInfo(name = "smtp_port")
    val smtpPort: Int = 587,

    @ColumnInfo(name = "smtp_encryption")
    val smtpEncryption: String = "STARTTLS", // "SSL", "STARTTLS", "NONE"

    // Authentication
    @ColumnInfo(name = "auth_type")
    val authType: String = "PASSWORD", // "PASSWORD", "OAUTH2", "APP_PASSWORD"

    @ColumnInfo(name = "encrypted_password")
    val encryptedPassword: String? = null, // Encrypted password

    @ColumnInfo(name = "oauth_token")
    val oauthToken: String? = null, // Encrypted OAuth token

    @ColumnInfo(name = "oauth_refresh_token")
    val oauthRefreshToken: String? = null, // Encrypted refresh token

    @ColumnInfo(name = "oauth_expires_at")
    val oauthExpiresAt: Long? = null, // Token expiration timestamp

    // Account Settings
    @ColumnInfo(name = "signature")
    val signature: String? = null,

    @ColumnInfo(name = "sync_enabled")
    val syncEnabled: Boolean = true,

    @ColumnInfo(name = "sync_frequency")
    val syncFrequency: Int = 15, // minutes

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = false, // Only one account can be active

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    // Account Status
    @ColumnInfo(name = "last_sync")
    val lastSync: Long? = null,

    @ColumnInfo(name = "sync_error")
    val syncError: String? = null,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true
)
