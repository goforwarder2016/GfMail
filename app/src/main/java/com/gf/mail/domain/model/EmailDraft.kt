package com.gf.mail.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing an email draft
 */
@Parcelize
data class EmailDraft(
    val id: String,
    val toAddresses: List<String>,
    val ccAddresses: List<String> = emptyList(),
    val bccAddresses: List<String> = emptyList(),
    val subject: String,
    val body: String,
    val attachments: List<EmailAttachment> = emptyList(),
    val priority: EmailPriority = EmailPriority.NORMAL,
    val isHtml: Boolean = false,
    val originalEmailId: String? = null,
    val originalSubject: String? = null,
    val originalFrom: String? = null,
    val originalBody: String? = null,
    val inReplyTo: String? = null,
    val references: String? = null
) : Parcelable