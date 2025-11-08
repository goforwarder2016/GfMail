package com.gf.mail.data.mapper

import com.gf.mail.data.local.entity.ServerConfigurationEntity
import com.gf.mail.domain.model.EncryptionType
import com.gf.mail.domain.model.ServerConfiguration
import java.util.UUID

/**
 * Mapper for converting between ServerConfiguration and ServerConfigurationEntity
 */
object ServerConfigurationMapper {

    /**
     * Convert ServerConfiguration to ServerConfigurationEntity
     */
    fun toEntity(configuration: ServerConfiguration, accountId: String): ServerConfigurationEntity {
        return ServerConfigurationEntity(
            id = UUID.randomUUID().toString(),
            accountId = accountId,
            imapHost = configuration.imapHost ?: "",
            imapPort = configuration.imapPort,
            imapSecurity = mapEncryptionTypeToString(configuration.imapEncryption),
            imapAuthMethod = "PLAIN",
            imapConnectionTimeout = 30000,
            imapReadTimeout = 60000,
            imapMaxConnections = 3,
            imapIdleTimeout = 1800000,
            smtpHost = configuration.smtpHost ?: "",
            smtpPort = configuration.smtpPort,
            smtpSecurity = mapEncryptionTypeToString(configuration.smtpEncryption),
            smtpAuthMethod = "PLAIN",
            smtpConnectionTimeout = 30000,
            smtpRequireAuth = true,
            usePushFolders = true,
            pushFolders = "[\"INBOX\"]",
            pollInterval = 300,
            maxMessageAge = 0,
            messageDownloadSize = 32768,
            autoDownloadSize = 8192,
            useSmtpPipelining = true,
            smtpLocalAddress = null,
            smtpLocalPort = 0,
            certificatePinning = false,
            trustedCertificates = "[]",
            allowUntrustedCertificates = false,
            clientCertificateAlias = null,
            useProxy = false,
            proxyType = "HTTP",
            proxyHost = null,
            proxyPort = 0,
            proxyUsername = null,
            proxyPassword = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Convert ServerConfigurationEntity to ServerConfiguration
     */
    fun toDomain(entity: ServerConfigurationEntity): ServerConfiguration {
        return ServerConfiguration(
            imapHost = entity.imapHost,
            imapPort = entity.imapPort,
            imapEncryption = mapStringToEncryptionType(entity.imapSecurity),
            smtpHost = entity.smtpHost,
            smtpPort = entity.smtpPort,
            smtpEncryption = mapStringToEncryptionType(entity.smtpSecurity)
        )
    }

    /**
     * Map EncryptionType to String
     */
    private fun mapEncryptionTypeToString(encryptionType: EncryptionType): String {
        return when (encryptionType) {
            EncryptionType.NONE -> "NONE"
            EncryptionType.STARTTLS -> "STARTTLS"
            EncryptionType.SSL -> "SSL_TLS"
        }
    }

    /**
     * Map String to EncryptionType
     */
    private fun mapStringToEncryptionType(securityType: String): EncryptionType {
        return when (securityType) {
            "NONE" -> EncryptionType.NONE
            "STARTTLS" -> EncryptionType.STARTTLS
            "SSL_TLS" -> EncryptionType.SSL
            else -> EncryptionType.SSL // Default to SSL
        }
    }
}