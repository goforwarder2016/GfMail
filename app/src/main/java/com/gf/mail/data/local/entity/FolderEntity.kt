package com.gf.mail.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "folders",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["name", "accountId"], unique = true),
        Index(value = ["type"]),
        Index(value = ["is_selectable"])
    ]
)
data class FolderEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "accountId")
    val accountId: String,

    @ColumnInfo(name = "name")
    val name: String, // IMAP folder name (e.g., "INBOX", "Sent", "Drafts")

    @ColumnInfo(name = "display_name")
    val displayName: String, // Localized display name

    @ColumnInfo(name = "type")
    val type: String, // INBOX, SENT, DRAFTS, JUNK, TRASH, CUSTOM

    @ColumnInfo(name = "parent_folder_id")
    val parentFolderId: String? = null, // For hierarchical folders

    @ColumnInfo(name = "separator")
    val separator: String = "/", // IMAP folder separator

    @ColumnInfo(name = "is_selectable")
    val isSelectable: Boolean = true, // Can contain messages

    @ColumnInfo(name = "is_subscribed")
    val isSubscribed: Boolean = true,

    @ColumnInfo(name = "total_count")
    val totalCount: Int = 0,

    @ColumnInfo(name = "unread_count")
    val unreadCount: Int = 0,

    @ColumnInfo(name = "recent_count")
    val recentCount: Int = 0,

    @ColumnInfo(name = "uid_validity")
    val uidValidity: Long? = null, // IMAP UID validity

    @ColumnInfo(name = "uid_next")
    val uidNext: Long? = null, // IMAP next UID

    @ColumnInfo(name = "last_sync_time")
    val lastSyncTime: Long? = null,

    @ColumnInfo(name = "sync_enabled")
    val syncEnabled: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
