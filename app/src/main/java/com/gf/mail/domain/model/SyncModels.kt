package com.gf.mail.domain.model

/**
 * Sync-related models
 */

/**
 * Sync performance metrics
 */
data class SyncPerformanceMetrics(
    val syncDuration: Long,
    val emailsProcessed: Int,
    val bytesTransferred: Long,
    val networkLatency: Long,
    val retryCount: Int,
    val errorCount: Int,
    val timestamp: Long,
    val syncTime: Long = syncDuration,
    val errors: List<String> = emptyList(),
    val networkQuality: NetworkQuality = NetworkQuality.UNKNOWN
)

/**
 * Network quality enum
 */
enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    UNKNOWN
}

/**
 * Sync status enum
 */
enum class SyncStatus {
    IDLE,
    SYNCING,
    PAUSED,
    ERROR,
    COMPLETED
}

/**
 * Sync result
 */
sealed class SyncResult {
    data class Success(val emailsProcessed: Int) : SyncResult()
    data class PartialSuccess(val emailsProcessed: Int, val errors: List<String>) : SyncResult()
    data class Error(val message: String, val cause: Throwable? = null) : SyncResult()
    data class Progress(val current: Int, val total: Int) : SyncResult()
}