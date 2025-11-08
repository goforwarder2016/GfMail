package com.gf.mail.domain.model

/**
 * Enum representing email status
 */
enum class EmailStatus {
    DRAFT,
    SENT,
    RECEIVED,
    OUTBOX,
    TRASH,
    SPAM
}

// SyncStatus is defined in SyncModels.kt

/**
 * Data class representing email sync result
 */
data class EmailSyncResult(
    val success: Boolean,
    val emailsProcessed: Int,
    val errors: List<String>,
    val syncTime: Long
)

/**
 * Data class representing email sync stats
 */
data class EmailSyncStats(
    val totalEmails: Int,
    val newEmails: Int,
    val updatedEmails: Int,
    val deletedEmails: Int,
    val syncDuration: Long
)