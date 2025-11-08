package com.gf.mail.domain.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing an email attachment
 */
@Parcelize
data class EmailAttachment(
    val id: String,
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val uri: Uri? = null,
    val contentId: String? = null,
    val isInline: Boolean = false
) : Parcelable