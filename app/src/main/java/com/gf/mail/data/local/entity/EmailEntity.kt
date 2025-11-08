package com.gf.mail.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "emails",
    indices = [
        Index(value = ["account_id"]),
        Index(value = ["folder_id"]),
        Index(value = ["is_read"]),
        Index(value = ["received_date"]),
        Index(value = ["sent_date"])
    ]
)
data class EmailEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "account_id")
    val accountId: String,

    @ColumnInfo(name = "folder_id")
    val folderId: String,

    @ColumnInfo(name = "thread_id")
    val threadId: String? = null,

    @ColumnInfo(name = "subject")
    val subject: String,

    @ColumnInfo(name = "from_name")
    val fromName: String,

    @ColumnInfo(name = "from_address")
    val fromAddress: String,

    @ColumnInfo(name = "reply_to_address")
    val replyToAddress: String? = null,

    @ColumnInfo(name = "to_addresses")
    val toAddresses: String, // JSON string of addresses

    @ColumnInfo(name = "cc_addresses")
    val ccAddresses: String = "", // JSON string of addresses

    @ColumnInfo(name = "bcc_addresses")
    val bccAddresses: String = "", // JSON string of addresses

    @ColumnInfo(name = "body_text")
    val bodyText: String? = null,

    @ColumnInfo(name = "body_html")
    val bodyHtml: String? = null,

    @ColumnInfo(name = "original_html_body")
    val originalHtmlBody: String? = null,

    @ColumnInfo(name = "sent_date")
    val sentDate: Long,

    @ColumnInfo(name = "received_date")
    val receivedDate: Long,

    @ColumnInfo(name = "message_id")
    val messageId: String,

    @ColumnInfo(name = "in_reply_to")
    val inReplyTo: String? = null,

    @ColumnInfo(name = "references")
    val references: String? = null,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "is_starred")
    val isStarred: Boolean = false,

    @ColumnInfo(name = "is_flagged")
    val isFlagged: Boolean = false,

    @ColumnInfo(name = "is_draft")
    val isDraft: Boolean = false,

    @ColumnInfo(name = "has_attachments")
    val hasAttachments: Boolean = false,

    @ColumnInfo(name = "priority")
    val priority: String = "NORMAL", // EmailPriority as string

    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long = 0L,

    @ColumnInfo(name = "uid")
    val uid: Long = 0L, // IMAP UID

    @ColumnInfo(name = "message_number")
    val messageNumber: Int = 0, // IMAP message number

    @ColumnInfo(name = "labels")
    val labels: String = "", // JSON string of labels

    @ColumnInfo(name = "flags")
    val flags: String = "", // JSON string of flags

    @ColumnInfo(name = "headers")
    val headers: String = "", // JSON string of headers

    @ColumnInfo(name = "sync_state")
    val syncState: String = "SYNCED", // SyncState as string

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
