package com.gf.mail.domain.repository

import com.gf.mail.domain.model.EmailFolder
import com.gf.mail.domain.model.FolderType
import kotlinx.coroutines.flow.Flow

interface FolderRepository {

    // Basic CRUD operations
    suspend fun getFolderById(id: String): EmailFolder?
    suspend fun getFolderByName(name: String, accountId: String): EmailFolder?
    suspend fun getFolderByFullName(fullName: String, accountId: String): EmailFolder?
    suspend fun getAllFolders(): List<EmailFolder>
    fun getAllFoldersFlow(): Flow<List<EmailFolder>>

    suspend fun insertFolder(folder: EmailFolder)
    suspend fun insertFolders(folders: List<EmailFolder>)
    suspend fun updateFolder(folder: EmailFolder)
    suspend fun deleteFolder(folderId: String)
    suspend fun deleteFolderByFullName(fullName: String, accountId: String)

    // Account-based operations
    suspend fun getFoldersByAccount(accountId: String): List<EmailFolder>
    suspend fun getFoldersForAccount(accountId: String): List<EmailFolder>
    fun getFoldersByAccountFlow(accountId: String): Flow<List<EmailFolder>>
    suspend fun getFolderCountByAccount(accountId: String): Int

    // Type-based operations
    suspend fun getFoldersByType(type: FolderType, accountId: String? = null): List<EmailFolder>
    suspend fun getFolderByType(type: FolderType, accountId: String): EmailFolder?
    suspend fun getSystemFolders(accountId: String): List<EmailFolder>
    suspend fun getCustomFolders(accountId: String): List<EmailFolder>
    suspend fun getSubscribedFolders(accountId: String): List<EmailFolder>

    // Hierarchy operations
    suspend fun getTopLevelFolders(accountId: String): List<EmailFolder>
    suspend fun getChildFolders(parentFolderId: String): List<EmailFolder>
    suspend fun getFolderHierarchy(accountId: String): List<FolderHierarchy>

    // Special folder operations
    suspend fun getInboxFolder(accountId: String): EmailFolder?
    suspend fun getSentFolder(accountId: String): EmailFolder?
    suspend fun getDraftsFolder(accountId: String): EmailFolder?
    suspend fun getTrashFolder(accountId: String): EmailFolder?
    suspend fun getSpamFolder(accountId: String): EmailFolder?
    suspend fun getArchiveFolder(accountId: String): EmailFolder?

    // Search operations
    suspend fun searchFolders(query: String, accountId: String): List<EmailFolder>
    fun searchFoldersFlow(query: String, accountId: String): Flow<List<EmailFolder>>

    // Status operations
    suspend fun updateFolderMessageCount(folderId: String, messageCount: Int, unreadCount: Int)
    suspend fun updateFolderCounts(folderId: String, messageCount: Int, unreadCount: Int)
    suspend fun updateFolderSyncTime(folderId: String, syncTime: Long)
    suspend fun updateFolderSubscription(folderId: String, subscribed: Boolean)

    // Sync operations
    suspend fun getFoldersModifiedAfter(timestamp: Long, accountId: String): List<EmailFolder>
    suspend fun getFoldersRequiringSync(accountId: String): List<EmailFolder>
    suspend fun markFolderAsSynced(folderId: String)

    // Bulk operations
    suspend fun deleteFoldersForAccount(accountId: String)
    suspend fun updateFoldersForAccount(accountId: String, folders: List<EmailFolder>)
    suspend fun syncFoldersForAccount(accountId: String, serverFolders: List<EmailFolder>)

    // Statistics
    suspend fun getFolderStats(accountId: String): FolderStats
    suspend fun getTotalMessageCount(accountId: String): Int
    suspend fun getTotalUnreadCount(accountId: String): Int
}

/**
 * Folder hierarchy data class for tree structure representation
 */
data class FolderHierarchy(
    val folder: EmailFolder,
    val children: List<FolderHierarchy> = emptyList(),
    val depth: Int = 0
)

/**
 * Folder statistics data class
 */
data class FolderStats(
    val accountId: String,
    val totalFolders: Int,
    val systemFolders: Int,
    val customFolders: Int,
    val subscribedFolders: Int,
    val totalMessages: Int,
    val totalUnreadMessages: Int,
    val lastSyncTime: Long?
)
