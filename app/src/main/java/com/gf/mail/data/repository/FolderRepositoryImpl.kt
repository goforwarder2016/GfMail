package com.gf.mail.data.repository

import com.gf.mail.data.local.dao.FolderDao
import com.gf.mail.data.mapper.FolderMapper
import com.gf.mail.domain.model.EmailFolder
import com.gf.mail.domain.model.FolderType
import com.gf.mail.domain.model.SyncState
import com.gf.mail.domain.repository.FolderHierarchy
import com.gf.mail.domain.repository.FolderRepository
import com.gf.mail.domain.repository.FolderStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FolderRepository
 */
@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao
) : FolderRepository {

    // Basic CRUD operations
    override suspend fun getFolderById(id: String): EmailFolder? {
        return folderDao.getFolderById(id)?.let { FolderMapper.toDomain(it) }
    }

    override suspend fun getFolderByName(name: String, accountId: String): EmailFolder? {
        return folderDao.getFolderByName(accountId, name)?.let { FolderMapper.toDomain(it) }
    }

    override suspend fun getFolderByFullName(fullName: String, accountId: String): EmailFolder? {
        // For now, treat fullName as name since we don't have separate fullName field
        return getFolderByName(fullName, accountId)
    }

    override suspend fun getAllFolders(): List<EmailFolder> {
        // This would need to be implemented with a query that gets all folders across accounts
        // For now, return empty list as this is not commonly used
        return emptyList()
    }

    override fun getAllFoldersFlow(): Flow<List<EmailFolder>> {
        // This would need to be implemented with a query that gets all folders across accounts
        // For now, return empty flow
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }

    override suspend fun insertFolder(folder: EmailFolder) {
        println("🔍 [FOLDER_REPO] insertFolder called:")
        println("  - folder.id: ${folder.id}")
        println("  - folder.accountId: ${folder.accountId}")
        println("  - folder.name: ${folder.name}")
        println("  - folder.fullName: ${folder.fullName}")
        println("  - folder.displayName: ${folder.displayName}")
        println("  - folder.type: ${folder.type}")
        println("  - folder.type.name: ${folder.type.name}")
        
        val entity = FolderMapper.toEntity(folder)
        println("🔍 [FOLDER_REPO] Mapped to entity:")
        println("  - entity.id: ${entity.id}")
        println("  - entity.accountId: ${entity.accountId}")
        println("  - entity.name: ${entity.name}")
        println("  - entity.displayName: ${entity.displayName}")
        println("  - entity.type: ${entity.type}")
        
        try {
            folderDao.insertFolder(entity)
            println("✅ [FOLDER_REPO] Successfully inserted folder: ${folder.name}")
        } catch (e: Exception) {
            println("❌ [FOLDER_REPO] Failed to insert folder: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun insertFolders(folders: List<EmailFolder>) {
        val entities = folders.map { FolderMapper.toEntity(it) }
        folderDao.insertFolders(entities)
    }

    override suspend fun updateFolder(folder: EmailFolder) {
        val entity = FolderMapper.toEntity(folder)
        folderDao.updateFolder(entity)
    }

    override suspend fun deleteFolder(folderId: String) {
        folderDao.deleteFolderById(folderId)
    }

    override suspend fun deleteFolderByFullName(fullName: String, accountId: String) {
        val folder = getFolderByFullName(fullName, accountId)
        folder?.let { deleteFolder(it.id) }
    }

    // Account-based operations
    override suspend fun getFoldersByAccount(accountId: String): List<EmailFolder> {
        return folderDao.getFoldersByAccount(accountId).map { FolderMapper.toDomain(it) }
    }

    override suspend fun getFoldersForAccount(accountId: String): List<EmailFolder> {
        return getFoldersByAccount(accountId)
    }

    override fun getFoldersByAccountFlow(accountId: String): Flow<List<EmailFolder>> {
        return folderDao.getFoldersByAccountFlow(accountId).map { entities ->
            entities.map { FolderMapper.toDomain(it) }
        }
    }

    override suspend fun getFolderCountByAccount(accountId: String): Int {
        return getFoldersByAccount(accountId).size
    }

    // Type-based operations
    override suspend fun getFoldersByType(type: FolderType, accountId: String?): List<EmailFolder> {
        return if (accountId != null) {
            folderDao.getSystemFolder(accountId, type.name)?.let { 
                listOf(FolderMapper.toDomain(it)) 
            } ?: emptyList()
        } else {
            // This would need a different query to get folders by type across all accounts
            emptyList()
        }
    }

    override suspend fun getFolderByType(type: FolderType, accountId: String): EmailFolder? {
        return folderDao.getSystemFolder(accountId, type.name)?.let { FolderMapper.toDomain(it) }
    }

    override suspend fun getSystemFolders(accountId: String): List<EmailFolder> {
        return folderDao.getSystemFolders(accountId).map { FolderMapper.toDomain(it) }
    }

    override suspend fun getCustomFolders(accountId: String): List<EmailFolder> {
        return folderDao.getCustomFolders(accountId).map { FolderMapper.toDomain(it) }
    }

    override suspend fun getSubscribedFolders(accountId: String): List<EmailFolder> {
        return folderDao.getSubscribedFolders(accountId).map { FolderMapper.toDomain(it) }
    }

    // Hierarchy operations
    override suspend fun getTopLevelFolders(accountId: String): List<EmailFolder> {
        return folderDao.getRootFolders(accountId).map { FolderMapper.toDomain(it) }
    }

    override suspend fun getChildFolders(parentFolderId: String): List<EmailFolder> {
        return folderDao.getChildFolders(parentFolderId).map { FolderMapper.toDomain(it) }
    }

    override suspend fun getFolderHierarchy(accountId: String): List<FolderHierarchy> {
        val allFolders = getFoldersByAccount(accountId)
        return buildFolderHierarchy(allFolders, null, 0)
    }

    // Special folder operations
    override suspend fun getInboxFolder(accountId: String): EmailFolder? {
        return folderDao.getInboxFolder(accountId)?.let { FolderMapper.toDomain(it) }
    }

    override suspend fun getSentFolder(accountId: String): EmailFolder? {
        return folderDao.getSentFolder(accountId)?.let { FolderMapper.toDomain(it) }
    }

    override suspend fun getDraftsFolder(accountId: String): EmailFolder? {
        return folderDao.getDraftsFolder(accountId)?.let { FolderMapper.toDomain(it) }
    }

    override suspend fun getTrashFolder(accountId: String): EmailFolder? {
        return folderDao.getTrashFolder(accountId)?.let { FolderMapper.toDomain(it) }
    }

    override suspend fun getSpamFolder(accountId: String): EmailFolder? {
        return folderDao.getJunkFolder(accountId)?.let { FolderMapper.toDomain(it) }
    }

    override suspend fun getArchiveFolder(accountId: String): EmailFolder? {
        // Archive folder is not implemented in FolderDao yet
        return null
    }

    // Search operations
    override suspend fun searchFolders(query: String, accountId: String): List<EmailFolder> {
        return folderDao.searchFolders(accountId, query).map { FolderMapper.toDomain(it) }
    }

    override fun searchFoldersFlow(query: String, accountId: String): Flow<List<EmailFolder>> {
        // This would need a different implementation in FolderDao
        return kotlinx.coroutines.flow.flow {
            emit(searchFolders(query, accountId))
        }
    }

    // Status operations
    override suspend fun updateFolderMessageCount(folderId: String, messageCount: Int, unreadCount: Int) {
        folderDao.updateAllCounts(folderId, messageCount, unreadCount, 0)
    }

    override suspend fun updateFolderCounts(folderId: String, messageCount: Int, unreadCount: Int) {
        folderDao.updateAllCounts(folderId, messageCount, unreadCount, 0)
    }

    override suspend fun updateFolderSyncTime(folderId: String, syncTime: Long) {
        folderDao.updateLastSyncTime(folderId, syncTime)
    }

    override suspend fun updateFolderSubscription(folderId: String, subscribed: Boolean) {
        folderDao.setFolderSubscribed(folderId, subscribed)
    }

    // Sync operations
    override suspend fun getFoldersModifiedAfter(timestamp: Long, accountId: String): List<EmailFolder> {
        // This would need a different query in FolderDao
        return emptyList()
    }

    override suspend fun getFoldersRequiringSync(accountId: String): List<EmailFolder> {
        return folderDao.getSyncEnabledFolders(accountId).map { FolderMapper.toDomain(it) }
    }

    override suspend fun markFolderAsSynced(folderId: String) {
        folderDao.updateLastSyncTime(folderId, System.currentTimeMillis())
    }

    // Bulk operations
    override suspend fun deleteFoldersForAccount(accountId: String) {
        folderDao.deleteFoldersByAccount(accountId)
    }

    override suspend fun updateFoldersForAccount(accountId: String, folders: List<EmailFolder>) {
        val entities = folders.map { FolderMapper.toEntity(it) }
        folderDao.insertFolders(entities) // Using insert with REPLACE strategy
    }

    override suspend fun syncFoldersForAccount(accountId: String, serverFolders: List<EmailFolder>) {
        updateFoldersForAccount(accountId, serverFolders)
    }

    // Statistics
    override suspend fun getFolderStats(accountId: String): FolderStats {
        val folders = getFoldersByAccount(accountId)
        val systemFolders = getSystemFolders(accountId)
        val customFolders = getCustomFolders(accountId)
        val subscribedFolders = getSubscribedFolders(accountId)
        val totalMessages = folderDao.getTotalEmailCount(accountId)
        val totalUnread = folderDao.getTotalUnreadCount(accountId)
        val lastSyncTime = folders.maxOfOrNull { it.lastSyncTime ?: 0L }

        return FolderStats(
            accountId = accountId,
            totalFolders = folders.size,
            systemFolders = systemFolders.size,
            customFolders = customFolders.size,
            subscribedFolders = subscribedFolders.size,
            totalMessages = totalMessages,
            totalUnreadMessages = totalUnread,
            lastSyncTime = lastSyncTime
        )
    }

    override suspend fun getTotalMessageCount(accountId: String): Int {
        return folderDao.getTotalEmailCount(accountId)
    }

    override suspend fun getTotalUnreadCount(accountId: String): Int {
        return folderDao.getTotalUnreadCount(accountId)
    }

    // Private helper methods
    private fun buildFolderHierarchy(
        folders: List<EmailFolder>,
        parentId: String?,
        depth: Int
    ): List<FolderHierarchy> {
        return folders
            .filter { it.parentId == parentId }
            .map { folder ->
                val children = buildFolderHierarchy(folders, folder.id, depth + 1)
                FolderHierarchy(folder, children, depth)
            }
    }
}