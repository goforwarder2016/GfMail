package com.gf.mail.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = EmailEntity::class,
            parentColumns = ["id"],
            childColumns = ["emailId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["emailId"]),
        Index(value = ["mime_type"]),
        Index(value = ["is_downloaded"]),
        Index(value = ["size"])
    ]
)
data class AttachmentEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "emailId")
    val emailId: String,

    @ColumnInfo(name = "filename")
    val filename: String,

    @ColumnInfo(name = "display_name")
    val displayName: String? = null,

    @ColumnInfo(name = "mime_type")
    val mimeType: String,

    @ColumnInfo(name = "size")
    val size: Long,

    @ColumnInfo(name = "content_id")
    val contentId: String? = null, // For inline attachments

    @ColumnInfo(name = "is_inline")
    val isInline: Boolean = false,

    @ColumnInfo(name = "is_downloaded")
    val isDownloaded: Boolean = false,

    @ColumnInfo(name = "local_file_path")
    val localFilePath: String? = null,

    @ColumnInfo(name = "download_url")
    val downloadUrl: String? = null,

    @ColumnInfo(name = "checksum")
    val checksum: String? = null, // For integrity verification

    @ColumnInfo(name = "download_status")
    val downloadStatus: String = "PENDING", // PENDING, DOWNLOADING, COMPLETED, FAILED

    @ColumnInfo(name = "download_progress")
    val downloadProgress: Int = 0, // 0-100

    @ColumnInfo(name = "encoding")
    val encoding: String? = null, // base64, quoted-printable, etc.

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
