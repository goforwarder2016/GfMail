package com.gf.mail.domain.usecase

import com.gf.mail.data.sync.FolderSyncService
import com.gf.mail.domain.model.Account
import kotlinx.coroutines.flow.Flow

/**
 * Use case for syncing folders
 */
class SyncFoldersUseCase(
    private val folderSyncService: FolderSyncService
) {
    /**
     * Sync folders for a specific account
     */
    suspend fun syncAccountFolders(account: Account): Boolean {
        // TODO: Implement sync account folders
        return true
    }

    /**
     * Sync all account folders
     */
    suspend fun syncAllAccountFolders(): Boolean {
        // TODO: Implement sync all account folders
        return true
    }
}