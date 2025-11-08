package com.gf.mail.domain.model

/**
 * Email folder data model
 */
data class EmailFolder(
    val id: String,
    val accountId: String,
    val name: String,
    val fullName: String,
    val displayName: String? = null,
    val serverId: String? = null,
    val parentId: String? = null,
    val type: FolderType = FolderType.CUSTOM,
    val messageCount: Int = 0,
    val totalCount: Int = 0,
    val unreadCount: Int = 0,
    val totalEmails: Int = 0, // Alias for totalCount
    val unreadEmails: Int = 0, // Alias for unreadCount
    val isSelectable: Boolean = true,
    val isSubscribed: Boolean = true,
    val isSystem: Boolean = false,
    val isSystemFolder: Boolean = false, // Alias for isSystem
    val isVisible: Boolean = true,
    val hasChildren: Boolean = false,
    val canHoldMessages: Boolean = true,
    val canHoldFolders: Boolean = true,
    val parentFolder: String? = null,
    val separator: Char = '/',
    val attributes: List<String> = emptyList(),
    val syncState: SyncState = SyncState.SYNCED,
    val lastSyncTime: Long? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Standard folder types
 */
enum class FolderType {
    INBOX,
    SENT,
    DRAFTS,
    TRASH,
    SPAM,
    ARCHIVE,
    CUSTOM
}

/**
 * Extension function to check if folder is a system folder
 */
fun EmailFolder.isSystemFolder(): Boolean {
    return isSystemFolder || type != FolderType.CUSTOM
}

/**
 * Extension function to get display name
 */
fun EmailFolder.getDisplayName(): String {
    return displayName ?: name
}

/**
 * Extension function to check if folder can be selected
 */
fun EmailFolder.canBeSelected(): Boolean {
    return isSelectable && isVisible
}