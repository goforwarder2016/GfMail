package com.gf.mail.utils

import android.content.Context
import com.gf.mail.R

/**
 * Utility class for managing multi-language folder name mappings for IMAP
 * Uses Android's built-in localization system
 */
object FolderNameMapper {
    
    /**
     * Get the localized folder name for a specific folder type
     * @param context Android context for accessing string resources
     * @param folderType The folder type (inbox, sent, drafts, trash, spam, archive, starred)
     * @return The localized folder name based on current system language
     */
    fun getFolderName(context: Context, folderType: String): String {
        return when (folderType.lowercase()) {
            "inbox" -> context.getString(R.string.folder_mapping_inbox)
            "sent" -> context.getString(R.string.folder_mapping_sent)
            "drafts" -> context.getString(R.string.folder_mapping_drafts)
            "trash" -> context.getString(R.string.folder_mapping_trash)
            "spam" -> context.getString(R.string.folder_mapping_spam)
            "archive" -> context.getString(R.string.folder_mapping_archive)
            "starred" -> context.getString(R.string.folder_mapping_starred)
            else -> folderType
        }
    }
    
    /**
     * Get all possible folder names for IMAP mapping
     * This includes all supported languages for folder name matching
     * @param context Android context for accessing string resources
     * @return List of all possible folder names in different languages
     */
    fun getAllFolderNames(context: Context): List<String> {
        val folderTypes = listOf("inbox", "sent", "drafts", "trash", "spam", "archive", "starred")
        return folderTypes.map { getFolderName(context, it) }
    }
    
    /**
     * Check if a folder name matches any of the known folder types
     * @param context Android context for accessing string resources
     * @param folderName The folder name to check
     * @return The folder type if found, null otherwise
     */
    fun getFolderType(context: Context, folderName: String): String? {
        val folderTypes = listOf("inbox", "sent", "drafts", "trash", "spam", "archive", "starred")
        
        for (folderType in folderTypes) {
            val localizedName = getFolderName(context, folderType)
            if (localizedName.equals(folderName, ignoreCase = true)) {
                return folderType
            }
        }
        return null
    }
    
    /**
     * Get all folder names for a specific folder type as a list
     * This method is kept for backward compatibility but now returns only the current language
     * @param context Android context for accessing string resources
     * @param folderType The folder type (inbox, sent, drafts, trash, spam, archive, starred)
     * @return List containing the localized folder name
     */
    fun getFolderNames(context: Context, folderType: String): List<String> {
        return listOf(getFolderName(context, folderType))
    }
}
