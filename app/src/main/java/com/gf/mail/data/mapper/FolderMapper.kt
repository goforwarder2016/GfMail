package com.gf.mail.data.mapper

import com.gf.mail.data.local.entity.FolderEntity
import com.gf.mail.domain.model.EmailFolder
import com.gf.mail.domain.model.FolderType
import com.gf.mail.domain.model.SyncState
import java.util.UUID

/**
 * Mapper for converting between EmailFolder and FolderEntity
 */
object FolderMapper {

    /**
     * Convert EmailFolder to FolderEntity
     */
    fun toEntity(domain: EmailFolder): FolderEntity {
        return FolderEntity(
            id = domain.id,
            accountId = domain.accountId,
            name = domain.fullName, // Store the full IMAP name (e.g., "&XfJT0ZAB-") - like backup code
            displayName = domain.displayName ?: domain.name, // Store the display name (e.g., "已发送")
            type = domain.type.name,
            parentFolderId = domain.parentId,
            separator = domain.separator.toString(),
            isSelectable = domain.isSelectable,
            isSubscribed = domain.isSubscribed,
            totalCount = domain.messageCount, // Use messageCount as totalCount
            unreadCount = domain.unreadCount,
            recentCount = 0, // Not used in domain model
            uidValidity = null, // Not used in domain model
            uidNext = null, // Not used in domain model
            lastSyncTime = domain.lastSyncTime,
            syncEnabled = domain.syncState != SyncState.FAILED,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert FolderEntity to EmailFolder
     */
    fun toDomain(entity: FolderEntity): EmailFolder {
        return EmailFolder(
            id = entity.id,
            accountId = entity.accountId,
            name = entity.name, // Use the stored full IMAP name (e.g., "&XfJT0ZAB-")
            fullName = entity.name, // Use the stored full IMAP name (e.g., "&XfJT0ZAB-")
            displayName = entity.displayName, // Use displayName for display
            serverId = null, // Not stored in entity
            parentId = entity.parentFolderId,
            type = mapStringToFolderType(entity.type),
            messageCount = entity.totalCount,
            totalCount = entity.totalCount,
            unreadCount = entity.unreadCount,
            isSelectable = entity.isSelectable,
            isSubscribed = entity.isSubscribed,
            isSystem = entity.type != FolderType.CUSTOM.name,
            hasChildren = false, // Would need separate query to determine
            canHoldMessages = entity.isSelectable,
            canHoldFolders = true, // Assume all folders can hold subfolders
            parentFolder = entity.parentFolderId,
            separator = entity.separator.firstOrNull() ?: '/',
            attributes = emptyList(), // Not stored in entity
            syncState = if (entity.syncEnabled) SyncState.SYNCED else SyncState.FAILED,
            lastSyncTime = entity.lastSyncTime,
            sortOrder = 0, // Not stored in entity
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    /**
     * Map string to FolderType enum
     */
    private fun mapStringToFolderType(typeString: String): FolderType {
        return try {
            FolderType.valueOf(typeString)
        } catch (e: IllegalArgumentException) {
            FolderType.CUSTOM
        }
    }

    /**
     * Create a new EmailFolder with generated ID
     */
    fun createNewFolder(
        accountId: String,
        name: String,
        type: FolderType = FolderType.CUSTOM,
        parentId: String? = null
    ): EmailFolder {
        return EmailFolder(
            id = UUID.randomUUID().toString(),
            accountId = accountId,
            name = name,
            fullName = name,
            displayName = getDisplayNameForType(type),
            parentId = parentId,
            type = type,
            isSystem = type != FolderType.CUSTOM,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Get display name for folder type
     */
    private fun getDisplayNameForType(type: FolderType): String {
        return when (type) {
            FolderType.INBOX -> "Inbox"
            FolderType.SENT -> "Sent"
            FolderType.DRAFTS -> "Drafts"
            FolderType.TRASH -> "Trash"
            FolderType.SPAM -> "Spam"
            FolderType.ARCHIVE -> "Archive"
            FolderType.CUSTOM -> "Custom"
        }
    }
}