package com.gf.mail.data.sync

import android.util.Log
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.EmailFolder
import com.gf.mail.domain.repository.AccountRepository
import com.gf.mail.domain.repository.EmailRepository
import com.gf.mail.domain.repository.FolderRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optimizer for email synchronization performance
 */
@Singleton
class SyncOptimizer @Inject constructor(
    private val accountRepository: AccountRepository,
    private val emailRepository: EmailRepository,
    private val folderRepository: FolderRepository
) {
    
    companion object {
        private const val TAG = "SyncOptimizer"
    }
    
    private val optimizationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _optimizationStats = MutableStateFlow(SyncOptimizationStats())
    val optimizationStats: StateFlow<SyncOptimizationStats> = _optimizationStats.asStateFlow()
    
    private val syncHistory = ConcurrentHashMap<String, SyncHistoryEntry>()
    private val performanceMetrics = ConcurrentHashMap<String, Long>()
    
    /**
     * Optimize sync strategy for an account
     */
    suspend fun optimizeSyncStrategy(account: Account): SyncStrategy {
        return try {
            val history = syncHistory[account.id]
            val metrics = performanceMetrics[account.id]
            
            val strategy = when {
                history == null -> SyncStrategy.FULL_SYNC
                history.lastSyncFailed -> SyncStrategy.RETRY_FAILED
                metrics != null && metrics > 30000 -> SyncStrategy.INCREMENTAL_SYNC
                else -> SyncStrategy.INCREMENTAL_SYNC
            }
            
            Log.d(TAG, "Optimized sync strategy for ${account.email}: $strategy")
            strategy
        } catch (e: Exception) {
            Log.e(TAG, "Failed to optimize sync strategy", e)
            SyncStrategy.FULL_SYNC
        }
    }
    
    /**
     * Get optimal batch size for sync
     */
    suspend fun getOptimalBatchSize(account: Account): Int {
        return try {
            val history = syncHistory[account.id]
            val baseSize = 50
            
            when {
                history == null -> baseSize
                history.lastSyncDuration < 5000 -> baseSize * 2
                history.lastSyncDuration > 30000 -> baseSize / 2
                else -> baseSize
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get optimal batch size", e)
            50
        }
    }
    
    /**
     * Get folders to sync based on priority
     */
    suspend fun getFoldersToSync(account: Account): List<EmailFolder> {
        return try {
            val folders = folderRepository.getFoldersForAccount(account.id)
            
            // Sort by priority: Inbox > Sent > Drafts > Others
            folders.sortedWith(compareBy<EmailFolder> { folder ->
                when (folder.name.lowercase()) {
                    "inbox" -> 1
                    "sent" -> 2
                    "drafts" -> 3
                    else -> 4
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get folders to sync", e)
            emptyList()
        }
    }
    
    /**
     * Record sync performance
     */
    fun recordSyncPerformance(accountId: String, duration: Long, success: Boolean, emailCount: Int) {
        try {
            val entry = SyncHistoryEntry(
                accountId = accountId,
                lastSyncTime = System.currentTimeMillis(),
                lastSyncDuration = duration,
                lastSyncFailed = !success,
                lastEmailCount = emailCount
            )
            
            syncHistory[accountId] = entry
            performanceMetrics[accountId] = duration
            
            updateOptimizationStats()
            
            Log.d(TAG, "Recorded sync performance for $accountId: ${duration}ms, success: $success, emails: $emailCount")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record sync performance", e)
        }
    }
    
    /**
     * Update optimization statistics
     */
    private fun updateOptimizationStats() {
        try {
            val stats = SyncOptimizationStats(
                totalAccounts = syncHistory.size,
                averageSyncTime = if (performanceMetrics.isNotEmpty()) {
                    performanceMetrics.values.average().toLong()
                } else 0L,
                successfulSyncs = syncHistory.values.count { !it.lastSyncFailed },
                failedSyncs = syncHistory.values.count { it.lastSyncFailed },
                totalEmailsSynced = syncHistory.values.sumOf { it.lastEmailCount },
                lastUpdated = System.currentTimeMillis()
            )
            
            _optimizationStats.value = stats
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update optimization stats", e)
        }
    }
    
    /**
     * Get sync recommendations
     */
    suspend fun getSyncRecommendations(account: Account): List<SyncRecommendation> {
        return try {
            val history = syncHistory[account.id]
            val recommendations = mutableListOf<SyncRecommendation>()
            
            if (history == null) {
                recommendations.add(SyncRecommendation.FULL_SYNC_RECOMMENDED)
            } else {
                if (history.lastSyncFailed) {
                    recommendations.add(SyncRecommendation.RETRY_FAILED_SYNC)
                }
                
                if (history.lastSyncDuration > 30000) {
                    recommendations.add(SyncRecommendation.REDUCE_BATCH_SIZE)
                }
                
                if (System.currentTimeMillis() - history.lastSyncTime > 24 * 60 * 60 * 1000) {
                    recommendations.add(SyncRecommendation.SCHEDULE_REGULAR_SYNC)
                }
            }
            
            recommendations
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get sync recommendations", e)
            emptyList()
        }
    }
    
    /**
     * Cleanup old sync history
     */
    fun cleanupOldHistory() {
        try {
            val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000) // 7 days
            syncHistory.entries.removeAll { it.value.lastSyncTime < cutoffTime }
            Log.d(TAG, "Cleaned up old sync history")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup old history", e)
        }
    }
    
    /**
     * Get optimization report
     */
    fun getOptimizationReport(): String {
        val stats = _optimizationStats.value
        return buildString {
            appendLine("=== Sync Optimization Report ===")
            appendLine("Total Accounts: ${stats.totalAccounts}")
            appendLine("Average Sync Time: ${stats.averageSyncTime}ms")
            appendLine("Successful Syncs: ${stats.successfulSyncs}")
            appendLine("Failed Syncs: ${stats.failedSyncs}")
            appendLine("Total Emails Synced: ${stats.totalEmailsSynced}")
            appendLine("Last Updated: ${stats.lastUpdated}")
        }
    }

    /**
     * Get optimization statistics
     */
    fun getOptimizationStats(): SyncOptimizationStats {
        return _optimizationStats.value
    }

    /**
     * Get sync performance metrics
     */
    fun getSyncPerformanceMetrics(): com.gf.mail.domain.model.SyncPerformanceMetrics {
        val stats = getOptimizationStats()
        return com.gf.mail.domain.model.SyncPerformanceMetrics(
            syncDuration = stats.averageSyncTime,
            emailsProcessed = stats.totalEmailsSynced,
            bytesTransferred = 0L,
            networkLatency = 0L,
            retryCount = 0,
            errorCount = stats.failedSyncs,
            timestamp = System.currentTimeMillis(),
            syncTime = stats.averageSyncTime,
            errors = emptyList(),
            networkQuality = com.gf.mail.domain.model.NetworkQuality.GOOD // TODO: Implement actual network quality assessment
        )
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        // TODO: Implement cleanup if needed
    }
}

/**
 * Sync strategy enum
 */
enum class SyncStrategy {
    FULL_SYNC,
    INCREMENTAL_SYNC,
    RETRY_FAILED
}

/**
 * Sync recommendation enum
 */
enum class SyncRecommendation {
    FULL_SYNC_RECOMMENDED,
    RETRY_FAILED_SYNC,
    REDUCE_BATCH_SIZE,
    SCHEDULE_REGULAR_SYNC
}

/**
 * Sync history entry
 */
data class SyncHistoryEntry(
    val accountId: String,
    val lastSyncTime: Long,
    val lastSyncDuration: Long,
    val lastSyncFailed: Boolean,
    val lastEmailCount: Int
)

/**
 * Sync optimization statistics
 */
data class SyncOptimizationStats(
    val totalAccounts: Int = 0,
    val averageSyncTime: Long = 0L,
    val successfulSyncs: Int = 0,
    val failedSyncs: Int = 0,
    val totalEmailsSynced: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)