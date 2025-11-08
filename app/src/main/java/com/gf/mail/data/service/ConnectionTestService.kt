package com.gf.mail.data.service

import com.gf.mail.data.email.ImapClient
import com.gf.mail.data.email.SmtpClient
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.AuthenticationInfo
import com.gf.mail.domain.model.AuthenticationType
import com.gf.mail.domain.model.EmailProvider
import com.gf.mail.domain.model.ServerConfiguration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for testing email server connections
 */
@Singleton
class ConnectionTestService @Inject constructor(
    private val imapClient: ImapClient,
    private val smtpClient: SmtpClient
) {
    
    /**
     * Test IMAP connection
     */
    suspend fun testImapConnection(serverConfig: ServerConfiguration, username: String, password: String): Boolean {
        return try {
            val account = Account(
                id = "test-account",
                email = username,
                emailAddress = username,
                displayName = username.substringBefore("@"),
                fullName = username.substringBefore("@"),
                provider = EmailProvider.IMAP,
                serverConfig = serverConfig,
                authInfo = AuthenticationInfo(
                    type = AuthenticationType.PASSWORD,
                    hasPassword = true
                )
            )
            
            val result = imapClient.connect(account, password)
            if (result.isSuccess) {
                imapClient.disconnect()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Test SMTP connection
     */
    suspend fun testSmtpConnection(serverConfig: ServerConfiguration, username: String, password: String): Boolean {
        return try {
            val account = Account(
                id = "test-account",
                email = username,
                emailAddress = username,
                displayName = username.substringBefore("@"),
                fullName = username.substringBefore("@"),
                provider = EmailProvider.IMAP,
                serverConfig = serverConfig,
                authInfo = AuthenticationInfo(
                    type = AuthenticationType.PASSWORD,
                    hasPassword = true
                )
            )
            
            val result = smtpClient.connect(account, password)
            if (result.isSuccess) {
                smtpClient.disconnect()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Test overall connection
     */
    suspend fun testConnection(serverConfig: ServerConfiguration, username: String, password: String): Boolean {
        val imapResult = testImapConnection(serverConfig, username, password)
        val smtpResult = testSmtpConnection(serverConfig, username, password)
        return imapResult && smtpResult
    }
}