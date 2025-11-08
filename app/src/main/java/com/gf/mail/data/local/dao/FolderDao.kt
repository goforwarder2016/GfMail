package com.gf.mail.data.local.dao

import androidx.room.*
import com.gf.mail.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    // Basic CRUD Operations
    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE accountId = :accountId AND name = :name LIMIT 1")
    suspend fun getFolderByName(accountId: String, name: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE accountId = :accountId ORDER BY display_name ASC")
    suspend fun getFoldersByAccount(accountId: String): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE accountId = :accountId ORDER BY display_name ASC")
    fun getFoldersByAccountFlow(accountId: String): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolderById(id: String)

    @Query("DELETE FROM folders WHERE accountId = :accountId")
    suspend fun deleteFoldersByAccount(accountId: String)

    // System folder queries
    @Query("SELECT * FROM folders WHERE accountId = :accountId AND type = :type LIMIT 1")
    suspend fun getSystemFolder(accountId: String, type: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE accountId = :accountId AND type = 'INBOX' LIMIT 1")
    suspend fun getInboxFolder(accountId: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE accountId = :accountId AND type = 'SENT' LIMIT 1")
    suspend fun getSentFolder(accountId: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE accountId = :accountId AND type = 'DRAFTS' LIMIT 1")
    suspend fun getDraftsFolder(accountId: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE accountId = :accountId AND type = 'TRASH' LIMIT 1")
    suspend fun getTrashFolder(accountId: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE accountId = :accountId AND type = 'JUNK' LIMIT 1")
    suspend fun getJunkFolder(accountId: String): FolderEntity?

    @Query(
        "SELECT * FROM folders WHERE accountId = :accountId AND type IN ('INBOX', 'SENT', 'DRAFTS', 'JUNK', 'TRASH')"
    )
    suspend fun getSystemFolders(accountId: String): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE accountId = :accountId AND type = 'CUSTOM'")
    suspend fun getCustomFolders(accountId: String): List<FolderEntity>

    // Hierarchical folder queries
    @Query("SELECT * FROM folders WHERE parent_folder_id = :parentId ORDER BY display_name ASC")
    suspend fun getChildFolders(parentId: String): List<FolderEntity>

    @Query(
        "SELECT * FROM folders WHERE accountId = :accountId AND parent_folder_id IS NULL ORDER BY display_name ASC"
    )
    suspend fun getRootFolders(accountId: String): List<FolderEntity>

    // Selectable and subscribed folders
    @Query(
        "SELECT * FROM folders WHERE accountId = :accountId AND is_selectable = 1 ORDER BY display_name ASC"
    )
    suspend fun getSelectableFolders(accountId: String): List<FolderEntity>

    @Query(
        "SELECT * FROM folders WHERE accountId = :accountId AND is_subscribed = 1 ORDER BY display_name ASC"
    )
    suspend fun getSubscribedFolders(accountId: String): List<FolderEntity>

    @Query("UPDATE folders SET is_subscribed = :isSubscribed WHERE id = :id")
    suspend fun setFolderSubscribed(id: String, isSubscribed: Boolean)

    // Sync management
    @Query("SELECT * FROM folders WHERE accountId = :accountId AND sync_enabled = 1")
    suspend fun getSyncEnabledFolders(accountId: String): List<FolderEntity>

    @Query("UPDATE folders SET sync_enabled = :enabled WHERE id = :id")
    suspend fun setSyncEnabled(id: String, enabled: Boolean)

    @Query("UPDATE folders SET last_sync_time = :timestamp WHERE id = :id")
    suspend fun updateLastSyncTime(id: String, timestamp: Long)

    // Email count management
    @Query("UPDATE folders SET total_count = :totalCount WHERE id = :id")
    suspend fun updateTotalCount(id: String, totalCount: Int)

    @Query("UPDATE folders SET unread_count = :unreadCount WHERE id = :id")
    suspend fun updateUnreadCount(id: String, unreadCount: Int)

    @Query("UPDATE folders SET recent_count = :recentCount WHERE id = :id")
    suspend fun updateRecentCount(id: String, recentCount: Int)

    @Query(
        """
        UPDATE folders 
        SET total_count = :totalCount, 
            unread_count = :unreadCount, 
            recent_count = :recentCount,
            updated_at = :updatedAt
        WHERE id = :id
    """
    )
    suspend fun updateAllCounts(
        id: String,
        totalCount: Int,
        unreadCount: Int,
        recentCount: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    // IMAP-specific operations
    @Query("UPDATE folders SET uid_validity = :uidValidity WHERE id = :id")
    suspend fun updateUidValidity(id: String, uidValidity: Long)

    @Query("UPDATE folders SET uid_next = :uidNext WHERE id = :id")
    suspend fun updateUidNext(id: String, uidNext: Long)

    @Query(
        """
        UPDATE folders 
        SET uid_validity = :uidValidity, 
            uid_next = :uidNext,
            updated_at = :updatedAt
        WHERE id = :id
    """
    )
    suspend fun updateImapInfo(
        id: String,
        uidValidity: Long,
        uidNext: Long,
        updatedAt: Long = System.currentTimeMillis()
    )

    // Statistics and aggregation
    @Query("SELECT SUM(unread_count) FROM folders WHERE accountId = :accountId")
    suspend fun getTotalUnreadCount(accountId: String): Int

    @Query("SELECT SUM(total_count) FROM folders WHERE accountId = :accountId")
    suspend fun getTotalEmailCount(accountId: String): Int

    @Query(
        """
        SELECT folders.id, 
               folders.accountId, 
               folders.name, 
               folders.display_name AS displayName, 
               folders.type, 
               folders.total_count AS totalCount, 
               folders.unread_count AS unreadCount, 
               folders.is_selectable AS isSelectable,
               folders.is_subscribed AS isSubscribed,
               folders.sync_enabled AS syncEnabled,
               COUNT(emails.id) as actualEmailCount 
        FROM folders 
        LEFT JOIN emails ON folders.id = emails.folder_id 
        WHERE folders.accountId = :accountId
        GROUP BY folders.id
        ORDER BY folders.display_name ASC
    """
    )
    suspend fun getFoldersWithActualCounts(accountId: String): List<FolderWithEmailCount>

    // Search functionality
    @Query(
        """
        SELECT * FROM folders 
        WHERE accountId = :accountId 
        AND (name LIKE '%' || :query || '%' OR display_name LIKE '%' || :query || '%')
        ORDER BY display_name ASC
    """
    )
    suspend fun searchFolders(accountId: String, query: String): List<FolderEntity>

    // Cleanup operations
    @Query("DELETE FROM folders WHERE accountId NOT IN (SELECT id FROM accounts)")
    suspend fun deleteOrphanedFolders()

    // Data transfer objects
    data class FolderWithEmailCount(
        val id: String,
        val accountId: String,
        val name: String,
        @ColumnInfo(name = "displayName")
        val displayName: String,
        val type: String,
        @ColumnInfo(name = "totalCount")
        val totalCount: Int,
        @ColumnInfo(name = "unreadCount")
        val unreadCount: Int,
        @ColumnInfo(name = "actualEmailCount")
        val actualEmailCount: Int,
        @ColumnInfo(name = "isSelectable")
        val isSelectable: Boolean,
        @ColumnInfo(name = "isSubscribed")
        val isSubscribed: Boolean,
        @ColumnInfo(name = "syncEnabled")
        val syncEnabled: Boolean
    )
}