package com.gf.mail.data.mapper

import com.gf.mail.data.local.entity.AccountEntity
import com.gf.mail.domain.model.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AccountMapper
 */
class AccountMapperTest {

    @Test
    fun `toDomain should convert AccountEntity to Account correctly`() {
        // Given
        val entity = AccountEntity(
            id = "test-account-id",
            email = "test@example.com",
            displayName = "Test User",
            provider = "GMAIL",
            isActive = true,
            isEnabled = true,
            authType = "PASSWORD",
            encryptedPassword = "encrypted_password_data",
            oauthToken = "access_token_123",
            oauthRefreshToken = "refresh_token_456",
            oauthExpiresAt = 1640995200000L,
            imapHost = "imap.gmail.com",
            imapPort = 993,
            imapEncryption = "SSL",
            smtpHost = "smtp.gmail.com",
            smtpPort = 587,
            smtpEncryption = "STARTTLS",
            signature = "Best regards,\nTest User",
            syncEnabled = true,
            syncFrequency = 15,
            createdAt = 1640995200000L,
            updatedAt = 1640995200000L,
            lastSync = 1640995200000L
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals("test-account-id", domain.id)
        assertEquals("test@example.com", domain.email)
        assertEquals("Test User", domain.displayName)
        assertEquals(EmailProvider.GMAIL, domain.provider)
        assertTrue(domain.isActive)
        assertTrue(domain.isEnabled)
        assertEquals(AuthenticationType.PASSWORD, domain.authInfo.type)
        assertTrue(domain.authInfo.hasPassword)
        assertTrue(domain.authInfo.hasOAuthToken)
        assertEquals(1640995200000L, domain.authInfo.oauthExpiresAt)
        assertEquals("imap.gmail.com", domain.serverConfig.imapHost)
        assertEquals(993, domain.serverConfig.imapPort)
        assertEquals(EncryptionType.SSL, domain.serverConfig.imapEncryption)
        assertEquals("smtp.gmail.com", domain.serverConfig.smtpHost)
        assertEquals(587, domain.serverConfig.smtpPort)
        assertEquals(EncryptionType.STARTTLS, domain.serverConfig.smtpEncryption)
        assertEquals("Best regards,\nTest User", domain.signature)
        assertTrue(domain.syncEnabled)
        assertEquals(15, domain.syncFrequency)
        assertEquals(1640995200000L, domain.lastSync)
        assertEquals(1640995200000L, domain.createdAt)
        assertEquals(1640995200000L, domain.updatedAt)
    }

    @Test
    fun `toEntity should convert Account to AccountEntity correctly`() {
        // Given
        val account = Account(
            id = "test-account-id",
            email = "test@example.com",
            emailAddress = "test@example.com",
            fullName = "Test User",
            displayName = "Test User",
            provider = EmailProvider.GMAIL,
            isActive = true,
            isEnabled = true,
            authInfo = AuthenticationInfo(
                type = AuthenticationType.PASSWORD,
                hasPassword = true,
                hasOAuthToken = true,
                oauthExpiresAt = 1640995200000L
            ),
            serverConfig = ServerConfiguration(
                imapHost = "imap.gmail.com",
                imapPort = 993,
                imapEncryption = EncryptionType.SSL,
                smtpHost = "smtp.gmail.com",
                smtpPort = 587,
                smtpEncryption = EncryptionType.STARTTLS
            ),
            lastSync = 1640995200000L,
            syncFrequency = 15,
            syncEnabled = true,
            signature = "Best regards,\nTest User",
            createdAt = 1640995200000L,
            updatedAt = 1640995200000L
        )

        // When
        val entity = account.toEntity(
            encryptedPassword = "encrypted_password_data",
            oauthToken = "access_token_123",
            oauthRefreshToken = "refresh_token_456"
        )

        // Then
        assertEquals("test-account-id", entity.id)
        assertEquals("test@example.com", entity.email)
        assertEquals("Test User", entity.displayName)
        assertEquals("GMAIL", entity.provider)
        assertTrue(entity.isActive)
        assertTrue(entity.isEnabled)
        assertEquals("PASSWORD", entity.authType)
        assertEquals("encrypted_password_data", entity.encryptedPassword)
        assertEquals("access_token_123", entity.oauthToken)
        assertEquals("refresh_token_456", entity.oauthRefreshToken)
        assertEquals(1640995200000L, entity.oauthExpiresAt)
        assertEquals("imap.gmail.com", entity.imapHost)
        assertEquals(993, entity.imapPort)
        assertEquals("SSL", entity.imapEncryption)
        assertEquals("smtp.gmail.com", entity.smtpHost)
        assertEquals(587, entity.smtpPort)
        assertEquals("STARTTLS", entity.smtpEncryption)
        assertEquals("Best regards,\nTest User", entity.signature)
        assertTrue(entity.syncEnabled)
        assertEquals(15, entity.syncFrequency)
        assertEquals(1640995200000L, entity.lastSync)
        assertEquals(1640995200000L, entity.createdAt)
        assertEquals(1640995200000L, entity.updatedAt)
    }

    @Test
    fun `toEntity with password should set encrypted password`() {
        // Given
        val account = Account(
            id = "test-account-id",
            email = "test@example.com",
            emailAddress = "test@example.com",
            fullName = "Test User",
            displayName = "Test User",
            provider = EmailProvider.GMAIL,
            isActive = true,
            isEnabled = true,
            authInfo = AuthenticationInfo(
                type = AuthenticationType.PASSWORD,
                hasPassword = false,
                hasOAuthToken = false,
                oauthExpiresAt = null
            ),
            serverConfig = ServerConfiguration(
                imapHost = "imap.gmail.com",
                imapPort = 993,
                imapEncryption = EncryptionType.SSL,
                smtpHost = "smtp.gmail.com",
                smtpPort = 587,
                smtpEncryption = EncryptionType.STARTTLS
            ),
            lastSync = 0L,
            syncFrequency = 15,
            syncEnabled = true,
            signature = null,
            createdAt = 1640995200000L,
            updatedAt = 1640995200000L
        )
        val password = "test_password_123"

        // When
        val entity = account.toEntity(encryptedPassword = password)

        // Then
        assertEquals(password, entity.encryptedPassword)
    }

    @Test
    fun `toEntity without password should not set encrypted password`() {
        // Given
        val account = Account(
            id = "test-account-id",
            email = "test@example.com",
            emailAddress = "test@example.com",
            fullName = "Test User",
            displayName = "Test User",
            provider = EmailProvider.GMAIL,
            isActive = true,
            isEnabled = true,
            authInfo = AuthenticationInfo(
                type = AuthenticationType.PASSWORD,
                hasPassword = false,
                hasOAuthToken = false,
                oauthExpiresAt = null
            ),
            serverConfig = ServerConfiguration(
                imapHost = "imap.gmail.com",
                imapPort = 993,
                imapEncryption = EncryptionType.SSL,
                smtpHost = "smtp.gmail.com",
                smtpPort = 587,
                smtpEncryption = EncryptionType.STARTTLS
            ),
            lastSync = 0L,
            syncFrequency = 15,
            syncEnabled = true,
            signature = null,
            createdAt = 1640995200000L,
            updatedAt = 1640995200000L
        )

        // When
        val entity = account.toEntity()

        // Then
        assertNull(entity.encryptedPassword)
    }
}