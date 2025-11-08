package com.gf.mail.data.sync

import com.gf.mail.data.email.ImapClient
import com.gf.mail.data.local.dao.FolderDao
import com.gf.mail.data.mapper.FolderMapper
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.EmailFolder
import com.gf.mail.domain.model.FolderType
import com.gf.mail.domain.model.SyncState
import com.gf.mail.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced folder synchronization service
 * Provides comprehensive folder sync capabilities with progress tracking
 */
@Singleton
class FolderSyncService @Inject constructor(
    private val folderRepository: FolderRepository,
    private val folderDao: FolderDao,
    private val folderMapper: FolderMapper,
    private val imapClient: ImapClient
) {
    
    /**
     * Sync all folders for an account with progress tracking
     */
    suspend fun syncFoldersForAccount(
        account: Account,
        password: String
    ): Flow<FolderSyncResult> = flow {
        try {
            emit(FolderSyncResult.Progress(0, "Connecting to server..."))
            
            // Connect to IMAP server
            val connectResult = imapClient.connect(account, password)
            if (connectResult.isFailure) {
                emit(FolderSyncResult.Error("Failed to connect: ${connectResult.exceptionOrNull()?.message}"))
                return@flow
            }
            
            emit(FolderSyncResult.Progress(10, "Fetching folder list..."))
            
            // Get folders from server
            println("🚨🚨🚨 [FOLDERSYNC_DEBUG] About to call imapClient.getFolders() - FOLDERSYNC PATH")
            val serverFoldersResult = imapClient.getFolders(account)
            println("🚨🚨🚨 [FOLDERSYNC_DEBUG] imapClient.getFolders() completed - FOLDERSYNC PATH")
            if (serverFoldersResult.isFailure) {
                emit(FolderSyncResult.Error("Failed to fetch folders: ${serverFoldersResult.exceptionOrNull()?.message}"))
                return@flow
            }
            
            val serverFolders = serverFoldersResult.getOrThrow()
            emit(FolderSyncResult.Progress(30, "Processing ${serverFolders.size} folders..."))
            
            // Get local folders
            val localFolders = folderRepository.getFoldersByAccount(account.id)
            val localFolderMap = localFolders.associateBy { it.fullName }
            
            var processedCount = 0
            var addedCount = 0
            var updatedCount = 0
            var removedCount = 0
            
            // Process server folders
            for (serverFolder in serverFolders) {
                val existingFolder = localFolderMap[serverFolder.fullName]
                
                if (existingFolder != null) {
                    // Update existing folder
                    val updatedFolder = existingFolder.copy(
                        name = serverFolder.name,
                        displayName = serverFolder.displayName,
                        messageCount = serverFolder.messageCount,
                        unreadCount = serverFolder.unreadCount,
                        isSubscribed = serverFolder.isSubscribed,
                        lastSyncTime = System.currentTimeMillis(),
                        syncState = SyncState.SYNCED
                    )
                    folderRepository.updateFolder(updatedFolder)
                    updatedCount++
                } else {
                    // Add new folder
                    val newFolder = serverFolder.copy(
                        accountId = account.id,
                        lastSyncTime = System.currentTimeMillis(),
                        syncState = SyncState.SYNCED
                    )
                    folderRepository.insertFolder(newFolder)
                    addedCount++
                }
                
                processedCount++
                val progress = 30 + (processedCount * 50 / serverFolders.size)
                emit(FolderSyncResult.Progress(progress, "Processed $processedCount/${serverFolders.size} folders"))
            }
            
            emit(FolderSyncResult.Progress(80, "Cleaning up removed folders..."))
            
            // Remove folders that no longer exist on server
            val serverFolderNames = serverFolders.map { it.fullName }.toSet()
            for (localFolder in localFolders) {
                if (localFolder.fullName !in serverFolderNames) {
                    folderRepository.deleteFolder(localFolder.id)
                    removedCount++
                }
            }
            
            emit(FolderSyncResult.Progress(90, "Updating folder hierarchy..."))
            
            // Update folder hierarchy and relationships
            updateFolderHierarchy(account.id, serverFolders)
            
            emit(FolderSyncResult.Progress(100, "Sync completed"))
            
            emit(FolderSyncResult.Success(
                totalFolders = serverFolders.size,
                addedFolders = addedCount,
                updatedFolders = updatedCount,
                removedFolders = removedCount
            ))
            
        } catch (e: Exception) {
            emit(FolderSyncResult.Error("Sync failed: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Sync specific folder with detailed progress
     */
    suspend fun syncSpecificFolder(
        account: Account,
        password: String,
        folderName: String
    ): Flow<FolderSyncResult> = flow {
        try {
            emit(FolderSyncResult.Progress(0, "Connecting to server..."))
            
            val connectResult = imapClient.connect(account, password)
            if (connectResult.isFailure) {
                emit(FolderSyncResult.Error("Failed to connect: ${connectResult.exceptionOrNull()?.message}"))
                return@flow
            }
            
            emit(FolderSyncResult.Progress(20, "Accessing folder: $folderName"))
            
            // Get folder info from server
            val folderInfoResult = imapClient.getFolderInfo(folderName)
            if (folderInfoResult.isFailure) {
                emit(FolderSyncResult.Error("Failed to get folder info: ${folderInfoResult.exceptionOrNull()?.message}"))
                return@flow
            }
            
            val serverFolder = folderInfoResult.getOrThrow()
            emit(FolderSyncResult.Progress(50, "Updating folder information..."))
            
            // Update or create folder
            val existingFolder = folderRepository.getFolderByName(folderName, account.id)
            if (existingFolder != null) {
                val updatedFolder = existingFolder.copy(
                    messageCount = serverFolder.messageCount,
                    unreadCount = serverFolder.unreadCount,
                    lastSyncTime = System.currentTimeMillis(),
                    syncState = SyncState.SYNCED
                )
                folderRepository.updateFolder(updatedFolder)
            } else {
                val newFolder = serverFolder.copy(
                    accountId = account.id,
                    lastSyncTime = System.currentTimeMillis(),
                    syncState = SyncState.SYNCED
                )
                folderRepository.insertFolder(newFolder)
            }
            
            emit(FolderSyncResult.Progress(100, "Folder sync completed"))
            
            emit(FolderSyncResult.Success(
                totalFolders = 1,
                addedFolders = if (existingFolder == null) 1 else 0,
                updatedFolders = if (existingFolder != null) 1 else 0,
                removedFolders = 0
            ))
            
        } catch (e: Exception) {
            emit(FolderSyncResult.Error("Folder sync failed: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Update folder hierarchy and parent-child relationships
     */
    private suspend fun updateFolderHierarchy(accountId: String, folders: List<EmailFolder>) {
        val folderMap = folders.associateBy { it.fullName }
        
        for (folder in folders) {
            val parentName = getParentFolderName(folder.fullName)
            if (parentName != null && folderMap.containsKey(parentName)) {
                val parentFolder = folderMap[parentName]!!
                val updatedFolder = folder.copy(parentId = parentFolder.id)
                folderRepository.updateFolder(updatedFolder)
            }
        }
    }
    
    /**
     * Get parent folder name from full folder path
     */
    private fun getParentFolderName(fullName: String): String? {
        val parts = fullName.split("/")
        return if (parts.size > 1) {
            parts.dropLast(1).joinToString("/")
        } else null
    }
    
    /**
     * Get folders that need synchronization
     */
    suspend fun getFoldersRequiringSync(accountId: String): List<EmailFolder> {
        return folderRepository.getFoldersRequiringSync(accountId)
    }
    
    /**
     * Mark folder as synced
     */
    suspend fun markFolderAsSynced(folderId: String) {
        folderRepository.markFolderAsSynced(folderId)
    }
    
    /**
     * Get folder sync statistics
     */
    suspend fun getFolderSyncStats(accountId: String): FolderSyncStats {
        val allFolders = folderRepository.getFoldersByAccount(accountId)
        val syncedFolders = allFolders.count { it.syncState == SyncState.SYNCED }
        val pendingFolders = allFolders.count { it.syncState == SyncState.PENDING }
        val failedFolders = allFolders.count { it.syncState == SyncState.FAILED }
        val lastSyncTime = allFolders.maxOfOrNull { it.lastSyncTime ?: 0L }
        
        return FolderSyncStats(
            accountId = accountId,
            totalFolders = allFolders.size,
            syncedFolders = syncedFolders,
            pendingFolders = pendingFolders,
            failedFolders = failedFolders,
            lastSyncTime = lastSyncTime
        )
    }
}

/**
 * Result of folder synchronization operation
 */
sealed class FolderSyncResult {
    data class Progress(
        val percentage: Int,
        val message: String
    ) : FolderSyncResult()
    
    data class Success(
        val totalFolders: Int,
        val addedFolders: Int,
        val updatedFolders: Int,
        val removedFolders: Int
    ) : FolderSyncResult()
    
    data class Error(
        val message: String
    ) : FolderSyncResult()
}

/**
 * Folder synchronization statistics
 */
data class FolderSyncStats(
    val accountId: String,
    val totalFolders: Int,
    val syncedFolders: Int,
    val pendingFolders: Int,
    val failedFolders: Int,
    val lastSyncTime: Long?
)