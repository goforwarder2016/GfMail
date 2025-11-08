package com.gf.mail.data.email

import com.gf.mail.domain.model.*
import java.io.File
import java.net.URL
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.activation.URLDataSource
import javax.mail.*
import javax.mail.internet.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SMTP client for sending emails
 * Handles connection, authentication, and email sending
 */
class SmtpClient {

    private var session: Session? = null
    private var transport: Transport? = null

    /**
     * Connect to SMTP server with account configuration
     */
    suspend fun connect(account: Account, password: String): Result<Unit> = withContext(
        Dispatchers.IO
    ) {
        try {
            val props = Properties().apply {
                setProperty("mail.smtp.host", account.serverConfig.smtpHost)
                setProperty("mail.smtp.port", account.serverConfig.smtpPort.toString())
                setProperty("mail.smtp.auth", "true")

                when (account.serverConfig.smtpEncryption) {
                    EncryptionType.SSL -> {
                        setProperty("mail.smtp.ssl.enable", "true")
                        setProperty("mail.smtp.ssl.trust", "*")
                        setProperty(
                            "mail.smtp.socketFactory.port",
                            account.serverConfig.smtpPort.toString()
                        )
                        setProperty(
                            "mail.smtp.socketFactory.class",
                            "javax.net.ssl.SSLSocketFactory"
                        )
                    }
                    EncryptionType.STARTTLS -> {
                        setProperty("mail.smtp.starttls.enable", "true")
                        setProperty("mail.smtp.ssl.trust", "*")
                    }
                    EncryptionType.NONE -> {
                        // No encryption
                    }
                }

                // Additional SMTP settings with longer timeouts
                setProperty("mail.smtp.connectiontimeout", "30000") // 30 seconds
                setProperty("mail.smtp.timeout", "30000") // 30 seconds  
                setProperty("mail.smtp.writetimeout", "30000") // 30 seconds
                
                // Better SSL/TLS configuration for compatibility
                setProperty("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3")
                setProperty("mail.smtp.ssl.checkserveridentity", "false")
                setProperty("mail.smtp.ssl.trust", "*")
                setProperty("mail.smtp.ssl.trustall", "true")
                // Let Java Mail automatically select compatible cipher suites
                
                // Authentication settings
                setProperty("mail.smtp.auth.login.disable", "false")
                setProperty("mail.smtp.auth.plain.disable", "false")
                setProperty("mail.smtp.auth.ntlm.disable", "true")
                setProperty("mail.smtp.auth.digestmd5.disable", "true")
                setProperty("mail.smtp.auth.crammd5.disable", "true")
            }

            val authenticator = object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(account.email, password)
                }
            }

            session = Session.getInstance(props, authenticator)
            transport = session?.getTransport("smtp")
            transport?.connect()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SmtpException("Failed to connect to SMTP server: ${e.message}", e))
        }
    }

    /**
     * Disconnect from SMTP server
     */
    suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            transport?.close()
            transport = null
            session = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SmtpException("Failed to disconnect: ${e.message}", e))
        }
    }

    /**
     * Send email message
     */
    suspend fun sendEmail(
        account: Account,
        draft: EmailDraft,
        attachments: List<EmailAttachment> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val session = this@SmtpClient.session
                ?: return@withContext Result.failure(SmtpException("Not connected to server"))

            val message = MimeMessage(session).apply {
                // Set headers
                setFrom(InternetAddress(account.email, account.displayName))

                // Recipients
                draft.toAddresses.forEach { address ->
                    addRecipient(Message.RecipientType.TO, InternetAddress(address))
                }
                draft.ccAddresses.forEach { address ->
                    addRecipient(Message.RecipientType.CC, InternetAddress(address))
                }
                draft.bccAddresses.forEach { address ->
                    addRecipient(Message.RecipientType.BCC, InternetAddress(address))
                }

                // Subject
                subject = draft.subject

                // Reply-To headers
                draft.inReplyTo?.let { inReplyTo ->
                    setHeader("In-Reply-To", inReplyTo)
                }
                draft.references?.let { references ->
                    setHeader("References", references)
                }

                // Priority
                when (draft.priority) {
                    EmailPriority.HIGH -> {
                        setHeader("X-Priority", "1")
                        setHeader("X-MSMail-Priority", "High")
                        setHeader("Importance", "High")
                    }
                    EmailPriority.LOW -> {
                        setHeader("X-Priority", "5")
                        setHeader("X-MSMail-Priority", "Low")
                        setHeader("Importance", "Low")
                    }
                    EmailPriority.NORMAL -> {
                        // Default priority
                    }
                }

                // Content
                if (attachments.isEmpty()) {
                    // Simple message without attachments
                    setEmailContent(this, draft)
                } else {
                    // Multipart message with attachments
                    setMultipartContent(this, draft, attachments)
                }

                // Set sent date
                sentDate = Date()
            }

            // Send the message
            Transport.send(message)

            // Get message ID for tracking
            val messageId = message.getHeader("Message-ID")?.firstOrNull()
                ?: "<${UUID.randomUUID()}@${account.email.substringAfter("@")}>"

            Result.success(messageId)
        } catch (e: Exception) {
            Result.failure(SmtpException("Failed to send email: ${e.message}", e))
        }
    }

    /**
     * Set simple email content (text/html)
     */
    private fun setEmailContent(message: MimeMessage, draft: EmailDraft) {
        when {
            !draft.body.isNullOrBlank() && draft.isHtml -> {
                // HTML content
                message.setContent(draft.body, "text/html; charset=UTF-8")
            }
            !draft.body.isNullOrBlank() -> {
                // Text content
                message.setText(draft.body, "UTF-8")
            }
            else -> {
                // Empty message
                message.setText("", "UTF-8")
            }
        }
    }

    /**
     * Set multipart content with attachments
     */
    private fun setMultipartContent(
        message: MimeMessage,
        draft: EmailDraft,
        attachments: List<EmailAttachment>
    ) {
        val multipart = MimeMultipart("mixed")

        // Add text/html content as first part
        if (!draft.body.isNullOrBlank()) {
            val contentPart = MimeBodyPart()

            if (draft.isHtml) {
                // HTML content
                contentPart.setContent(draft.body, "text/html; charset=UTF-8")
            } else {
                // Text content
                contentPart.setText(draft.body, "UTF-8")
            }

            multipart.addBodyPart(contentPart)
        }

        // Add attachments
        attachments.forEach { attachment ->
            val attachmentPart = MimeBodyPart().apply {
                // Use URI if available, otherwise create a temporary file
                val dataSource = if (attachment.uri != null) {
                    try {
                        URLDataSource(URL(attachment.uri.toString()))
                    } catch (e: Exception) {
                        // If URI is invalid, fall back to creating a temporary file
                        val tempFile = File.createTempFile("attachment_", "_${attachment.fileName}")
                        FileDataSource(tempFile)
                    }
                } else {
                    // Create a temporary file for the attachment
                    val tempFile = File.createTempFile("attachment_", "_${attachment.fileName}")
                    FileDataSource(tempFile)
                }
                dataHandler = DataHandler(dataSource)
                fileName = attachment.fileName

                // Set content disposition
                disposition = Part.ATTACHMENT

                // Set content ID for inline attachments
                if (attachment.isInline) {
                    disposition = Part.INLINE
                    attachment.contentId?.let { contentId ->
                        setHeader("Content-ID", "<$contentId>")
                    }
                }
            }
            multipart.addBodyPart(attachmentPart)
        }

        message.setContent(multipart)
    }

    /**
     * Check if connected
     */
    fun isConnected(): Boolean {
        return transport?.isConnected == true
    }

    /**
     * Validate email addresses
     */
    fun validateEmailAddresses(addresses: List<String>): List<String> {
        val invalidAddresses = mutableListOf<String>()

        addresses.forEach { address ->
            try {
                InternetAddress(address, true)
            } catch (e: AddressException) {
                invalidAddresses.add(address)
            }
        }

        return invalidAddresses
    }

    /**
     * Create reply message info
     */
    fun createReplyInfo(originalEmail: Email, replyAll: Boolean = false): ReplyInfo {
        val replyTo = originalEmail.replyToAddress ?: originalEmail.fromAddress
        val subject = if (originalEmail.subject.startsWith("Re: ")) {
            originalEmail.subject
        } else {
            "Re: ${originalEmail.subject}"
        }

        val toAddresses = listOf(replyTo)
        val ccAddresses = if (replyAll) {
            (originalEmail.toAddresses + originalEmail.ccAddresses)
                .filter { it != replyTo } // Don't include original sender in CC
                .distinct()
        } else {
            emptyList()
        }

        // Build references header
        val references = buildString {
            originalEmail.references?.let { refs ->
                append(refs)
                append(" ")
            }
            append(originalEmail.messageId)
        }

        return ReplyInfo(
            toAddresses = toAddresses,
            ccAddresses = ccAddresses,
            subject = subject,
            inReplyTo = originalEmail.messageId,
            references = references,
            originalEmail = originalEmail
        )
    }

    /**
     * Create forward message info
     */
    fun createForwardInfo(originalEmail: Email): ForwardInfo {
        val subject = if (originalEmail.subject.startsWith("Fwd: ")) {
            originalEmail.subject
        } else {
            "Fwd: ${originalEmail.subject}"
        }

        return ForwardInfo(
            subject = subject,
            originalEmail = originalEmail
        )
    }

    /**
     * Send email
     */
    suspend fun sendEmail(
        account: Account,
        password: String,
        draft: EmailDraft,
        attachments: List<EmailAttachment> = emptyList()
    ): SendResult = withContext(Dispatchers.IO) {
        try {
            // Connect if not connected
            val connectResult = connect(account, password)
            if (connectResult.isFailure) {
                return@withContext SendResult(
                    success = false,
                    error = "Failed to connect: ${connectResult.exceptionOrNull()?.message}"
                )
            }

            val currentSession = session ?: return@withContext SendResult(
                success = false,
                error = "No session available"
            )

            // Create message
            val message = MimeMessage(currentSession).apply {
                setFrom(InternetAddress(account.email, account.displayName))

                // Set recipients
                setRecipients(Message.RecipientType.TO, draft.toAddresses.joinToString(","))
                if (draft.ccAddresses.isNotEmpty()) {
                    setRecipients(Message.RecipientType.CC, draft.ccAddresses.joinToString(","))
                }
                if (draft.bccAddresses.isNotEmpty()) {
                    setRecipients(Message.RecipientType.BCC, draft.bccAddresses.joinToString(","))
                }

                subject = draft.subject
                sentDate = Date()

                // Set reply headers
                draft.inReplyTo?.let { setHeader("In-Reply-To", it) }
                draft.references?.let { setHeader("References", it) }

                // Set priority
                when (draft.priority) {
                    EmailPriority.HIGH -> setHeader("X-Priority", "1")
                    EmailPriority.LOW -> setHeader("X-Priority", "5")
                    else -> setHeader("X-Priority", "3")
                }
            }

            // Set content
            if (attachments.isNotEmpty()) {
                setMultipartContent(message, draft, attachments)
            } else {
                setEmailContent(message, draft)
            }

            // Send message
            Transport.send(message)

            SendResult(success = true)
        } catch (e: Exception) {
            SendResult(
                success = false,
                error = "Failed to send email: ${e.message}"
            )
        }
    }

    /**
     * Send result data class
     */
    data class SendResult(
        val success: Boolean,
        val error: String? = null
    )
}

/**
 * Custom exception for SMTP operations
 */
class SmtpException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Reply information data class
 */
data class ReplyInfo(
    val toAddresses: List<String>,
    val ccAddresses: List<String>,
    val subject: String,
    val inReplyTo: String,
    val references: String,
    val originalEmail: Email
)

/**
 * Forward information data class
 */
data class ForwardInfo(
    val subject: String,
    val originalEmail: Email
)
