package com.gf.mail.domain.model

/**
 * Account summary data model for UI display
 */
data class AccountSummary(
    val id: String,
    val email: String,
    val displayName: String,
    val provider: EmailProvider,
    val isActive: Boolean,
    val unreadCount: Int,
    val totalEmails: Int,
    val lastSyncTime: Long?,
    val syncStatus: SyncStatus,
    val hasError: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Get display text for the account
     */
    fun getDisplayText(): String {
        return if (displayName.isNotBlank()) {
            "$displayName ($email)"
        } else {
            email
        }
    }

    /**
     * Check if account can be added (limit check)
     */
    fun canAddAccount(): Boolean {
        // Simple limit check - can be enhanced with actual account count
        return true
    }
}

/**
 * Extension function to get status display text
 */
fun SyncStatus.getDisplayText(): String {
    return when (this) {
        SyncStatus.IDLE -> "Ready"
        SyncStatus.SYNCING -> "Syncing"
        SyncStatus.COMPLETED -> "Success"
        SyncStatus.ERROR -> "Failed"
        else -> "Unknown"
    }
}