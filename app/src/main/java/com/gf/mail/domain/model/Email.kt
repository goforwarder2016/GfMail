package com.gf.mail.domain.model

/**
 * Complete email data model
 */
data class Email(
    val id: String,
    val accountId: String,
    val folderId: String,
    val threadId: String? = null,
    val subject: String,
    val fromName: String,
    val fromAddress: String,
    val replyToAddress: String? = null,
    val toAddresses: List<String>,
    val ccAddresses: List<String> = emptyList(),
    val bccAddresses: List<String> = emptyList(),
    val bodyText: String? = null,
    val bodyHtml: String? = null,
    val originalHtmlBody: String? = null, // 原始HTML内容，用于WebView渲染
    val sentDate: Long,
    val receivedDate: Long,
    val messageId: String,
    val inReplyTo: String? = null,
    val references: String? = null,
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val isFlagged: Boolean = false,
    val isDraft: Boolean = false,
    val hasAttachments: Boolean = false,
    val priority: EmailPriority = EmailPriority.NORMAL,
    val size: Long = 0,
    val uid: Long = 0,
    val messageNumber: Int = 0,
    val labels: List<String> = emptyList(),
    val flags: List<String> = emptyList(),
    val headers: Map<String, String> = emptyMap(),
    val attachments: List<EmailAttachment> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncState: SyncState = SyncState.SYNCED
) {
    // Alias for receivedDate to maintain compatibility
    val receivedAt: Long get() = receivedDate
}

/**
 * Email priority levels
 */
enum class EmailPriority {
    HIGH,
    NORMAL,
    LOW
}

/**
 * Email sync state
 */
enum class SyncState {
    SYNCED,
    PENDING,
    SYNCING,
    ERROR,
    FAILED,
    DELETED,
    SENT,
    DRAFT
}

/**
 * Extension function to check if email is recent (less than 24 hours old)
 */
fun Email.isRecent(): Boolean {
    val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
    return receivedDate > twentyFourHoursAgo
}

/**
 * Get display name for from field
 */
fun Email.getFromDisplay(): String {
    return if (fromName.isNotBlank()) {
        "$fromName <$fromAddress>"
    } else {
        fromAddress
    }
}

/**
 * Get email body content (prefer HTML, fallback to text)
 */
fun Email.getBodyContent(): String {
    return bodyHtml?.takeIf { it.isNotBlank() } ?: bodyText ?: ""
}