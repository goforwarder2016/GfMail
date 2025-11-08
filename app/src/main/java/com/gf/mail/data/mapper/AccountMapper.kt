package com.gf.mail.data.mapper

import com.gf.mail.data.local.entity.AccountEntity
import com.gf.mail.domain.model.*

/**
 * Convert AccountEntity (database) to Account (domain)
 */
fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        email = email,
        emailAddress = email,
        displayName = displayName,
        fullName = displayName,
        provider = provider.toEmailProvider(),
        serverConfig = ServerConfiguration(
            imapHost = imapHost,
            imapPort = imapPort,
            imapEncryption = imapEncryption.toEncryptionType(),
            smtpHost = smtpHost,
            smtpPort = smtpPort,
            smtpEncryption = smtpEncryption.toEncryptionType()
        ),
        authInfo = AuthenticationInfo(
            type = authType.toAuthenticationType(),
            hasPassword = encryptedPassword != null,
            hasOAuthToken = oauthToken != null,
            oauthExpiresAt = oauthExpiresAt
        ),
        signature = signature,
        syncEnabled = syncEnabled,
        syncFrequency = syncFrequency,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSync = lastSync,
        syncError = syncError,
        isEnabled = isEnabled
    )
}

/**
 * Convert Account (domain) to AccountEntity (database)
 * Note: This doesn't include sensitive data like actual passwords/tokens
 */
fun Account.toEntity(
    encryptedPassword: String? = null,
    oauthToken: String? = null,
    oauthRefreshToken: String? = null
): AccountEntity {
    return AccountEntity(
        id = id,
        email = email,
        displayName = displayName,
        provider = provider.name,
        imapHost = serverConfig.imapHost,
        imapPort = serverConfig.imapPort,
        imapEncryption = serverConfig.imapEncryption.name,
        smtpHost = serverConfig.smtpHost,
        smtpPort = serverConfig.smtpPort,
        smtpEncryption = serverConfig.smtpEncryption.name,
        authType = authInfo.type.name,
        encryptedPassword = encryptedPassword,
        oauthToken = oauthToken,
        oauthRefreshToken = oauthRefreshToken,
        oauthExpiresAt = authInfo.oauthExpiresAt,
        signature = signature,
        syncEnabled = syncEnabled,
        syncFrequency = syncFrequency,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSync = lastSync,
        syncError = syncError,
        isEnabled = isEnabled
    )
}

/**
 * Convert list of AccountEntity to list of Account
 */
fun List<AccountEntity>.toDomain(): List<Account> {
    return this.map { it.toDomain() }
}

/**
 * Convert provider string to EmailProvider enum safely
 */
private fun String.toEmailProvider(): EmailProvider {
    return try {
        EmailProvider.valueOf(this.uppercase())
    } catch (e: IllegalArgumentException) {
        EmailProvider.IMAP // Default fallback
    }
}

/**
 * Convert encryption type string to enum safely
 */
private fun String.toEncryptionType(): EncryptionType {
    return try {
        EncryptionType.valueOf(this.uppercase())
    } catch (e: IllegalArgumentException) {
        EncryptionType.SSL // Default fallback
    }
}

/**
 * Convert auth type string to enum safely
 */
private fun String.toAuthenticationType(): AuthenticationType {
    return try {
        AuthenticationType.valueOf(this.uppercase())
    } catch (e: IllegalArgumentException) {
        AuthenticationType.PASSWORD // Default fallback
    }
}
