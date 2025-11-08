package com.gf.mail.domain.model

/**
 * Represents an email attachment
 */
data class Attachment(
    val id: String,
    val emailId: String,
    val filename: String,
    val contentType: String,
    val size: Long,
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val downloadStatus: String = "pending", // "pending", "downloading", "completed", "failed"
    val createdAt: Long = System.currentTimeMillis()
)