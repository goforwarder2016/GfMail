package com.gf.mail.data.offline

import android.util.Log
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.repository.EmailRepository
import com.gf.mail.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.cancel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling offline synchronization when network becomes available
 */
@Singleton
class OfflineSyncService @Inject constructor(
    private val offlineManager: OfflineManager,
    private val emailRepository: EmailRepository,
    private val accountRepository: AccountRepository
) {
    
    companion object {
        private const val TAG = "OfflineSyncService"
        private const val SYNC_DELAY_MS = 2000L // 2 seconds delay before sync
        private const val MAX_RETRY_COUNT = 3
    }
    
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()
    
    init {
        startNetworkMonitoring()
    }
    
    /**
     * Start monitoring network changes for automatic sync
     */
    private fun startNetworkMonitoring() {
        syncScope.launch {
            offlineManager.networkEvents.collect { event ->
                when (event) {
                    is NetworkEvent.Connected -> {
                        Log.d(TAG, "Network connected, starting sync")
                        delay(SYNC_DELAY_MS) // Wait a bit for stable connection
                        syncPendingOperations()
                    }
                    is NetworkEvent.Disconnected -> {
                        Log.d(TAG, "Network disconnected")
                        _syncState.value = SyncState.OFFLINE
                    }
                    is NetworkEvent.TypeChanged -> {
                        Log.d(TAG, "Network type changed to: ${event.networkType}")
                        // Continue sync if already in progress
                    }
                }
            }
        }
    }
    
    /**
     * Sync all pending offline operations
     */
    suspend fun syncPendingOperations() {
        if (!offlineManager.isConnectedToInternet()) {
            Log.d(TAG, "No internet connection, skipping sync")
            return
        }
        
        val pendingOps = offlineManager.pendingOperations.value
        if (pendingOps.isEmpty()) {
            Log.d(TAG, "No pending operations to sync")
            return
        }
        
        _syncState.value = SyncState.SYNCING
        _syncProgress.value = 0f
        
        try {
            var successCount = 0
            var failureCount = 0
            
            pendingOps.forEachIndexed { index, operation ->
                try {
                    val success = executeOperation(operation)
                    if (success) {
                        successCount++
                        offlineManager.removeOfflineOperation(operation.id)
                    } else {
                        failureCount++
                        // Increment retry count
                        val updatedOperation = operation.copy(retryCount = operation.retryCount + 1)
                        if (updatedOperation.retryCount < MAX_RETRY_COUNT) {
                            // Keep operation for retry
                        } else {
                            // Remove operation after max retries
                            offlineManager.removeOfflineOperation(operation.id)
                            Log.w(TAG, "Operation failed after max retries: ${operation.id}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error executing operation: ${operation.id}", e)
                    failureCount++
                }
                
                // Update progress
                _syncProgress.value = (index + 1).toFloat() / pendingOps.size
            }
            
            _syncState.value = if (failureCount == 0) SyncState.COMPLETED else SyncState.PARTIAL
            _lastSyncTime.value = System.currentTimeMillis()
            
            Log.d(TAG, "Sync completed: $successCount success, $failureCount failures")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync", e)
            _syncState.value = SyncState.FAILED
        }
    }
    
    /**
     * Execute a single offline operation
     */
    private suspend fun executeOperation(operation: OfflineOperation): Boolean {
        return try {
            when (operation.type) {
                OfflineOperationType.SEND_EMAIL -> {
                    // TODO: Implement email sending
                    Log.d(TAG, "Executing send email operation: ${operation.id}")
                    true
                }
                OfflineOperationType.DELETE_EMAIL -> {
                    val emailId = operation.data["emailId"] as? String
                    if (emailId != null) {
                        emailRepository.deleteEmail(emailId)
                        true
                    } else {
                        false
                    }
                }
                OfflineOperationType.MARK_AS_READ -> {
                    val emailId = operation.data["emailId"] as? String
                    val isRead = operation.data["isRead"] as? Boolean ?: true
                    if (emailId != null) {
                        emailRepository.markEmailAsRead(emailId, isRead)
                        true
                    } else {
                        false
                    }
                }
                OfflineOperationType.MARK_AS_UNREAD -> {
                    val emailId = operation.data["emailId"] as? String
                    if (emailId != null) {
                        emailRepository.markEmailAsRead(emailId, false)
                        true
                    } else {
                        false
                    }
                }
                OfflineOperationType.STAR_EMAIL -> {
                    val emailId = operation.data["emailId"] as? String
                    val isStarred = operation.data["isStarred"] as? Boolean ?: true
                    if (emailId != null) {
                        emailRepository.starEmail(emailId, isStarred)
                        true
                    } else {
                        false
                    }
                }
                OfflineOperationType.MOVE_EMAIL -> {
                    val emailId = operation.data["emailId"] as? String
                    val folderId = operation.data["folderId"] as? String
                    if (emailId != null && folderId != null) {
                        emailRepository.moveEmailToFolder(emailId, folderId)
                        true
                    } else {
                        false
                    }
                }
                OfflineOperationType.CREATE_FOLDER -> {
                    // TODO: Implement folder creation
                    Log.d(TAG, "Executing create folder operation: ${operation.id}")
                    true
                }
                OfflineOperationType.DELETE_FOLDER -> {
                    // TODO: Implement folder deletion
                    Log.d(TAG, "Executing delete folder operation: ${operation.id}")
                    true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing operation: ${operation.type}", e)
            false
        }
    }
    
    /**
     * Force sync pending operations
     */
    suspend fun forceSync() {
        if (offlineManager.isConnectedToInternet()) {
            syncPendingOperations()
        } else {
            Log.d(TAG, "No internet connection for force sync")
        }
    }
    
    /**
     * Get sync statistics
     */
    fun getSyncStats(): SyncStats {
        val pendingOps = offlineManager.pendingOperations.value
        return SyncStats(
            pendingOperationsCount = pendingOps.size,
            lastSyncTime = _lastSyncTime.value,
            syncState = _syncState.value,
            networkQuality = offlineManager.getNetworkQuality()
        )
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        // Cancel the coroutine scope
        syncScope.coroutineContext.cancel()
    }
}

/**
 * Sync state enumeration
 */
enum class SyncState {
    IDLE,
    SYNCING,
    COMPLETED,
    PARTIAL,
    FAILED,
    OFFLINE
}

/**
 * Sync statistics
 */
data class SyncStats(
    val pendingOperationsCount: Int,
    val lastSyncTime: Long,
    val syncState: SyncState,
    val networkQuality: NetworkQuality
)