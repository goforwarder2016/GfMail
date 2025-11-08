package com.gf.mail.data.email

import android.util.Log
import com.gf.mail.data.local.dao.EmailDao
import com.gf.mail.data.local.dao.FolderDao
import com.gf.mail.data.local.entity.EmailEntity
import com.gf.mail.data.local.entity.FolderEntity
import com.gf.mail.data.mapper.toEntity
import com.gf.mail.data.mapper.FolderMapper
import com.gf.mail.data.notification.PushNotificationManager
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.model.EmailFolder

import com.gf.mail.domain.model.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for real-time email synchronization using IMAP IDLE
 */
@Singleton
class RealtimeEmailSyncService @Inject constructor(
    private val imapClient: ImapClient,
    private val emailDao: EmailDao,
    private val folderDao: FolderDao,
    private val folderMapper: FolderMapper,
    private val pushNotificationManager: PushNotificationManager,
    private val credentialEncryption: com.gf.mail.data.security.CredentialEncryption
) {
    
    companion object {
        private const val TAG = "RealtimeEmailSyncService"
    }
    
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _syncState = MutableStateFlow<com.gf.mail.domain.model.SyncState>(com.gf.mail.domain.model.SyncState.SYNCED)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var currentAccount: Account? = null

    /**
     * Start real-time synchronization for a given account
     */
    fun startSync(account: Account) {
        currentAccount = account
        _syncState.value = com.gf.mail.domain.model.SyncState.SYNCING
        
        Log.d(TAG, "Starting real-time sync for account: ${account.email}")

        syncScope.launch {
            try {
                // Connect to IMAP server
                val password = credentialEncryption.getPasswordSecurely(account.id) ?: ""
                val connectResult = imapClient.connect(account, password)
                if (connectResult.isFailure) {
                    _syncState.value = com.gf.mail.domain.model.SyncState.FAILED
                    pushNotificationManager.handleSyncStatus(
                        account.email, 
                        false, 
                        "Failed to connect to IMAP server"
                    )
                    return@launch
                }

                // Start push notifications
                pushNotificationManager.startPushNotifications(account)
                
                // Start IDLE monitoring for INBOX
                imapClient.startIdleMonitoring("INBOX").collectLatest { update ->
                    if (update.error != null) {
                        _syncState.value = com.gf.mail.domain.model.SyncState.FAILED
                        pushNotificationManager.handleSyncStatus(
                            account.email, 
                            false, 
                            "IDLE monitoring error: ${update.error}"
                        )
                        return@collectLatest
                    }

                    // Fetch new messages based on UID
                    val lastUidResult = imapClient.getHighestUid(update.folderName)
                    val lastUid = if (lastUidResult.isSuccess) lastUidResult.getOrNull() ?: 0L else 0L
                    val newMessagesResult = imapClient.getNewMessages(lastUid, update.folderName)

                    if (newMessagesResult.isSuccess) {
                        val newMessages = newMessagesResult.getOrNull() ?: emptyList()
                        if (newMessages.isNotEmpty()) {
                            // Set folderId for INBOX emails
                            val emailsWithFolderId = newMessages.map { email ->
                                email.copy(folderId = "INBOX")
                            }
                            val syncedCount = syncEmails(account.id, emailsWithFolderId)
                            _syncState.value = com.gf.mail.domain.model.SyncState.SYNCED
                            
                            // Show notification for new emails
                            pushNotificationManager.handleNewEmail(account, newMessages)
                            
                            Log.d(TAG, "Synced $syncedCount new emails for account: ${account.email}")
                        }
                    } else {
                        _syncState.value = com.gf.mail.domain.model.SyncState.FAILED
                        pushNotificationManager.handleSyncStatus(
                            account.email, 
                            false, 
                            "Failed to fetch new messages"
                        )
                    }

                    // Also sync folder counts
                    println("🚨🚨🚨 [REALTIME_DEBUG] About to call imapClient.getFolders() - REALTIME PATH")
                    val foldersResult = imapClient.getFolders(account)
                    println("🚨🚨🚨 [REALTIME_DEBUG] imapClient.getFolders() completed - REALTIME PATH")
                    if (foldersResult.isSuccess) {
                        syncFolders(account.id, foldersResult.getOrNull() ?: emptyList())
                    }
                }
            } catch (e: Exception) {
                _syncState.value = SyncState.FAILED
                pushNotificationManager.handleSyncStatus(
                    account.email, 
                    false, 
                    "Sync error: ${e.message}"
                )
                Log.e(TAG, "Error in real-time sync for account: ${account.email}", e)
            }
        }
    }

    /**
     * Stop real-time synchronization
     */
    fun stopSync() {
        currentAccount?.let { account ->
            Log.d(TAG, "Stopping real-time sync for account: ${account.email}")
            pushNotificationManager.stopPushNotifications(account.id)
        }
        
        imapClient.stopIdleMonitoring()
        syncScope.launch {
            imapClient.disconnect()
        }
        _syncState.value = com.gf.mail.domain.model.SyncState.SYNCED
        currentAccount = null
    }

    /**
     * Sync emails to local database
     */
    private suspend fun syncEmails(accountId: String, emails: List<Email>): Int {
        val emailEntities = emails.map { email ->
            email.copy(accountId = accountId).toEntity()
        }
        emailDao.insertEmails(emailEntities)
        return emailEntities.size
    }

    /**
     * Sync folders to local database
     */
    private suspend fun syncFolders(accountId: String, folders: List<EmailFolder>): Int {
        val folderEntities = folders.map { folder ->
            folderMapper.toEntity(folder.copy(accountId = accountId))
        }
        folderDao.insertFolders(folderEntities)
        return folderEntities.size
    }
}