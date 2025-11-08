package com.gf.mail.data.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.gf.mail.domain.model.EmailAttachment
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for handling file picking and attachment management
 */
class FilePickerService(private val context: Context) {

    companion object {
        const val MAX_ATTACHMENT_SIZE = 20 * 1024 * 1024L // 20MB per individual file
        const val MAX_TOTAL_ATTACHMENTS_SIZE = 20 * 1024 * 1024L // 20MB total per PRP requirement

        // Supported MIME types
        val SUPPORTED_MIME_TYPES = setOf(
            // Images
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
            // Documents
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            // Text files
            "text/plain", "text/csv", "text/html",
            // Archives
            "application/zip", "application/x-rar-compressed",
            // Audio
            "audio/mpeg", "audio/wav", "audio/ogg",
            // Video
            "video/mp4", "video/avi", "video/mov"
        )
    }

    /**
     * File picker result
     */
    data class FilePickerResult(
        val success: Boolean,
        val attachments: List<EmailAttachment> = emptyList(),
        val errors: List<String> = emptyList()
    )

    /**
     * Create intent for file picking
     */
    fun createFilePickerIntent(allowMultiple: Boolean = true): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
            putExtra(Intent.EXTRA_MIME_TYPES, SUPPORTED_MIME_TYPES.toTypedArray())
            addCategory(Intent.CATEGORY_OPENABLE)
        }
    }

    /**
     * Process selected files from file picker
     */
    suspend fun processSelectedFiles(
        uris: List<Uri>,
        emailId: String = "",
        accountId: String = ""
    ): FilePickerResult = withContext(Dispatchers.IO) {
        val attachments = mutableListOf<EmailAttachment>()
        val errors = mutableListOf<String>()
        var totalSize = 0L

        for (uri in uris) {
            try {
                val fileInfo = getFileInfo(uri)

                // Validate file
                val validationResult = validateFile(fileInfo, totalSize)
                if (!validationResult.isValid) {
                    errors.add("${fileInfo.fileName}: ${validationResult.error}")
                    continue
                }

                // Copy file to internal storage
                val localFile = copyToInternalStorage(uri, fileInfo.fileName)
                if (localFile == null) {
                    errors.add("${fileInfo.fileName}: Failed to save file")
                    continue
                }

                // Create attachment
                val attachment = EmailAttachment(
                    id = UUID.randomUUID().toString(),
                    fileName = fileInfo.fileName,
                    mimeType = fileInfo.mimeType,
                    size = fileInfo.size,
                    uri = android.net.Uri.fromFile(localFile),
                    contentId = null,
                    isInline = false
                )

                attachments.add(attachment)
                totalSize += fileInfo.size
            } catch (e: Exception) {
                errors.add("Error processing file: ${e.message}")
            }
        }

        FilePickerResult(
            success = attachments.isNotEmpty(),
            attachments = attachments,
            errors = errors
        )
    }

    /**
     * Get file information from URI
     */
    private fun getFileInfo(uri: Uri): FileInfo {
        var fileName = "unknown_file"
        var size = 0L
        var mimeType = "application/octet-stream"

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex) ?: fileName
                }
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }

        // Get MIME type
        mimeType = context.contentResolver.getType(uri) ?: run {
            // Fallback to extension-based detection
            val extension = fileName.substringAfterLast('.', "")
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
                ?: "application/octet-stream"
        }

        return FileInfo(fileName, size, mimeType)
    }

    /**
     * Validate file before processing
     */
    private fun validateFile(fileInfo: FileInfo, currentTotalSize: Long): ValidationResult {
        // Check file size
        if (fileInfo.size > MAX_ATTACHMENT_SIZE) {
            return ValidationResult(false, "File too large (max 20MB)")
        }

        // Check total size
        if (currentTotalSize + fileInfo.size > MAX_TOTAL_ATTACHMENTS_SIZE) {
            return ValidationResult(false, "Total attachments size exceeded (max 20MB)")
        }

        // Check MIME type
        if (fileInfo.mimeType !in SUPPORTED_MIME_TYPES) {
            return ValidationResult(false, "File type not supported")
        }

        // Check filename
        if (fileInfo.fileName.isBlank()) {
            return ValidationResult(false, "Invalid filename")
        }

        return ValidationResult(true, null)
    }

    /**
     * Copy file from URI to internal storage
     */
    private suspend fun copyToInternalStorage(uri: Uri, fileName: String): File? = withContext(
        Dispatchers.IO
    ) {
        try {
            val attachmentsDir = File(context.filesDir, "attachments").apply {
                if (!exists()) mkdirs()
            }

            // Generate unique filename to avoid conflicts
            val timestamp = System.currentTimeMillis()
            val cleanFileName = fileName.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val localFile = File(attachmentsDir, "${timestamp}_$cleanFileName")

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(localFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            localFile
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Delete attachment file from storage
     */
    suspend fun deleteAttachmentFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists() && file.isFile) {
                file.delete()
            } else {
                true // File doesn't exist, consider it deleted
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get attachment file size
     */
    fun getFileSize(filePath: String): Long {
        return try {
            File(filePath).length()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Check if file exists
     */
    fun fileExists(filePath: String): Boolean {
        return try {
            File(filePath).exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get MIME type from file extension
     */
    fun getMimeTypeFromFileName(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
    }

    /**
     * Format file size for display
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    /**
     * Get file icon based on MIME type
     */
    fun getFileIcon(mimeType: String): String {
        return when {
            mimeType.startsWith("image/") -> "🖼️"
            mimeType.startsWith("audio/") -> "🎵"
            mimeType.startsWith("video/") -> "🎬"
            mimeType == "application/pdf" -> "📄"
            mimeType.contains("word") -> "📝"
            mimeType.contains("excel") || mimeType.contains("spreadsheet") -> "📊"
            mimeType.contains("powerpoint") || mimeType.contains("presentation") -> "📋"
            mimeType.contains("zip") || mimeType.contains("rar") -> "🗜️"
            mimeType.startsWith("text/") -> "📃"
            else -> "📎"
        }
    }

    /**
     * Clean up old attachment files
     */
    suspend fun cleanupOldFiles(olderThanDays: Int = 30): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        try {
            val attachmentsDir = File(context.filesDir, "attachments")
            if (!attachmentsDir.exists()) return@withContext 0

            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)

            attachmentsDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but don't throw
        }

        deletedCount
    }

    // Helper data classes
    private data class FileInfo(
        val fileName: String,
        val size: Long,
        val mimeType: String
    )

    private data class ValidationResult(
        val isValid: Boolean,
        val error: String?
    )
}
