package com.gf.mail.data.email

import com.gf.mail.domain.model.*
import com.gf.mail.domain.repository.AccountRepository
import com.gf.mail.domain.repository.EmailRepository
import com.gf.mail.domain.repository.FolderRepository
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for synchronizing emails with IMAP servers
 * Handles background sync, folder management, and email fetching
 */
@Singleton
class EmailSyncService @Inject constructor(
    private val accountRepository: AccountRepository,
    private val emailRepository: EmailRepository,
    private val folderRepository: FolderRepository,
    private val imapClient: ImapClient
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val imapClients = ConcurrentHashMap<String, ImapClient>()
    private val syncJobs = ConcurrentHashMap<String, Job>()

    // Sync status flows
    private val _syncStatus = MutableStateFlow<Map<String, SyncStatus>>(emptyMap())
    val syncStatus: StateFlow<Map<String, SyncStatus>> = _syncStatus.asStateFlow()

    /**
     * Sync account (public method for external use)
     */
    suspend fun syncAccount(accountId: String): Result<Unit> {
        return startSync(accountId)
    }

    /**
     * Start sync for an account
     */
    suspend fun startSync(accountId: String): Result<Unit> {
        return try {
            val account = accountRepository.getAccountById(accountId)
                ?: return Result.failure(Exception("Account not found"))

            if (!account.isEnabled || !account.syncEnabled) {
                return Result.failure(Exception("Account sync is disabled"))
            }

            // Cancel existing sync job
            syncJobs[accountId]?.cancel()

            // Start new sync job
            val job = scope.launch {
                performSync(account)
            }

            syncJobs[accountId] = job

            updateSyncStatus(accountId, SyncStatus.SYNCING)
            Result.success(Unit)
        } catch (e: Exception) {
            updateSyncStatus(accountId, SyncStatus.ERROR, e.message)
            Result.failure(e)
        }
    }

    /**
     * Stop sync for an account
     */
    suspend fun stopSync(accountId: String): Result<Unit> {
        return try {
            syncJobs[accountId]?.cancel()
            syncJobs.remove(accountId)

            // Disconnect IMAP client
            imapClients[accountId]?.let { client ->
                client.disconnect()
                imapClients.remove(accountId)
            }

            updateSyncStatus(accountId, SyncStatus.IDLE)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Perform full sync for an account
     */
    private suspend fun performSync(account: Account) {
        try {
            println("🚨🚨🚨 [EMAILSERVICE_CRITICAL] performSync started - version: 2025-01-08-v7")
            println("🚨🚨🚨 [EMAILSERVICE_CRITICAL] current time: ${System.currentTimeMillis()}")
            println("🚨🚨🚨 [EMAILSERVICE_CRITICAL] account: ${account.email}")
            println("🔄 StartingSyncAccount: ${account.email}")
            println("🔧 Account IMAP configuration:")
            println("  - IMAP Host: ${account.serverConfig.imapHost}")
            println("  - IMAP Port: ${account.serverConfig.imapPort}")
            println("  - IMAP Encryption: ${account.serverConfig.imapEncryption}")
            println("  - SMTP Host: ${account.serverConfig.smtpHost}")
            println("  - SMTP Port: ${account.serverConfig.smtpPort}")
            println("  - SMTP Encryption: ${account.serverConfig.smtpEncryption}")
            
            updateSyncStatus(account.id, SyncStatus.CONNECTING)

            // Get or create IMAP client
            val client = getOrCreateImapClient(account)

            // Connect to server (only if not already connected)
            if (!client.isConnected()) {
                val password = accountRepository.getPassword(account.id)
                if (password == null) {
                    println("❌ Cannot get account password: ${account.email}")
                    updateSyncStatus(account.id, SyncStatus.ERROR, "No password available")
                    return
                }

                println("🔑 Got password, length: ${password.length}")
                val connectResult = client.connect(account, password)
                if (connectResult.isFailure) {
                    val error = connectResult.exceptionOrNull()?.message
                    println("❌ IMAPConnection failed: $error")
                    updateSyncStatus(
                        account.id,
                        SyncStatus.ERROR,
                        error
                    )
                    return
                }
            } else {
                println("✅ IMAP client connected, skipping connection step")
            }

            updateSyncStatus(account.id, SyncStatus.SYNCING)

            // Sync folders
            println("🔍 [DEBUG] Preparing to call syncFolders...")
            syncFolders(account, client)
            println("🔍 [DEBUG] syncFolders call completed")

            // Sync emails for each folder
            val folders = folderRepository.getFoldersByAccount(account.id)
            println("🔍 [EMAIL_SYNC] Found ${folders.size} folders in database for account ${account.id}")
            for (folder in folders) {
                println("🔍 [EMAIL_SYNC] Processing folder: ${folder.name} (${folder.fullName}) - canHoldMessages: ${folder.canHoldMessages}")
                if (folder.canHoldMessages) {
                    println("🔍 [EMAIL_SYNC] Starting to sync emails for folder: ${folder.name}")
                    syncFolderEmails(account, client, folder)
                    println("🔍 [EMAIL_SYNC] Completed syncing emails for folder: ${folder.name}")
                } else {
                    println("🔍 [EMAIL_SYNC] Skipping folder ${folder.name} - cannot hold messages")
                }
            }

            // Update last sync time
            accountRepository.updateSyncStatus(account.id, System.currentTimeMillis(), null)
            updateSyncStatus(account.id, SyncStatus.COMPLETED)
        } catch (e: Exception) {
            updateSyncStatus(account.id, SyncStatus.ERROR, e.message)
            accountRepository.updateSyncStatus(account.id, null, e.message)
        }
    }

    /**
     * Sync folders for an account
     */
    private suspend fun syncFolders(account: Account, client: ImapClient) {
        println("🚨🚨🚨 [EMAILSERVICE_SYNC] Starting to sync folders: ${account.email} - version: 2025-01-08-v6")
        println("🚨🚨🚨 [CRITICAL_TEST] syncFolders method called")
        println("🚨🚨🚨 [CRITICAL_TEST] current time: ${System.currentTimeMillis()}")
        println("🚨🚨🚨 [CRITICAL_TEST] Calling client.getFolders()...")
        val foldersResult = client.getFolders(account)
        println("🔍 [DEBUG] client.getFolders() call completed, result: ${if (foldersResult.isSuccess) "success" else "failed"}")
        if (foldersResult.isFailure) {
            val error = foldersResult.exceptionOrNull()?.message
            println("❌ Failed to get folder: $error")
            throw Exception("Failed to get folders: $error")
        }

        val serverFolders = foldersResult.getOrThrow()
        println("📁 Remote folder count: ${serverFolders.size}")
        serverFolders.forEach { folder ->
            println("  - ${folder.fullName}: ${folder.messageCount} emails, ${folder.unreadCount}  unread")
        }
        
        val localFolders = folderRepository.getFoldersByAccount(account.id)
        println("📁 Local folder count: ${localFolders.size}")

        // Add or update folders
        for (serverFolder in serverFolders) {
            // 使用多种方式查找现有Folder，因为可能存在名称Mapping问题
            val existingFolder = localFolders.find { 
                it.fullName == serverFolder.fullName || 
                it.name == serverFolder.name ||
                it.id == serverFolder.id
            }

            if (existingFolder != null) {
                // Update existing folder
                val updatedFolder = existingFolder.copy(
                    name = serverFolder.name,
                    fullName = serverFolder.fullName,  // 确保 fullName 也被更新
                    messageCount = serverFolder.messageCount,
                    unreadCount = serverFolder.unreadCount,
                    isSubscribed = serverFolder.isSubscribed,
                    lastSyncTime = System.currentTimeMillis()
                )
                folderRepository.updateFolder(updatedFolder)
            } else {
                // Add new folder
                val newFolder = serverFolder.copy(
                    accountId = account.id,
                    lastSyncTime = System.currentTimeMillis()
                )
                folderRepository.insertFolder(newFolder)
            }
        }

        // Remove folders that no longer exist on server
        val serverFolderNames = serverFolders.map { it.fullName }.toSet()
        for (localFolder in localFolders) {
            if (localFolder.fullName !in serverFolderNames) {
                folderRepository.deleteFolder(localFolder.id)
            }
        }
    }

    /**
     * Sync emails for a specific folder
     */
    private suspend fun syncFolderEmails(account: Account, client: ImapClient, folder: EmailFolder) {
        try {
            println("📧 Starting to sync folder emails: ${folder.fullName}")
            println("🔍 Pre-sync status check:")
            println("  - Account: ${account.email}")
            println("  - Folder ID: ${folder.id}")
            println("  - Folder name: ${folder.name}")
            println("  - Folder full name: ${folder.fullName}")
            println("  - Client connected: ${client.isConnected()}")
            println("  - Folder email count: ${folder.messageCount}")
            println("  - Folder unread count: ${folder.unreadCount}")
            
            // Get latest emails (incremental sync)
            val existingCount = emailRepository.getEmailCountInFolder(folder.id)
            val newEmailsCount = maxOf(0, folder.messageCount - existingCount)
            
            println("📊 Folder ${folder.fullName} Statistics:")
            println("  - Total server emails: ${folder.messageCount}")
            println("  - Local existing emails: $existingCount")
            println("  - New emails to sync: $newEmailsCount")
            println("🔍 [DEBUG] Folder details:")
            println("  - folder.id: ${folder.id}")
            println("  - folder.name: ${folder.name}")
            println("  - folder.fullName: ${folder.fullName}")
            println("  - folder.displayName: ${folder.displayName}")
            println("  - folder.type: ${folder.type}")

            // 对于NetEase邮箱，如果folder.messageCount > 0但existingCount为0，强制尝试同步
            val shouldSync = if (folder.messageCount > 0 && existingCount == 0) {
                println("🔧 [NETEASE_SYNC] Forcing sync for folder with server emails but no local emails: ${folder.fullName}")
                true
            } else {
                newEmailsCount > 0
            }

            if (shouldSync) {
                println("📥 Starting to get new emails...")
                println("🔍 [EMAIL_SYNC_DEBUG] About to call client.getEmails() with:")
                println("  - folder.fullName: ${folder.fullName}")
                println("  - count: $newEmailsCount")
                println("  - offset: 0")
                println("  - account: ${account.email}")
                
                // 使用 getEmails 方法，它会内部Processing folder打开和邮件获取
                // 使用 folder.fullName 来获取完整的 IMAP Folder name（如 "&g0l6P3ux-"）
                val emailsResult = client.getEmails(folder.fullName, count = newEmailsCount, offset = 0, account)
                
                println("🔍 [EMAIL_SYNC_DEBUG] client.getEmails() call completed")
                println("  - Result success: ${emailsResult.isSuccess}")
                if (emailsResult.isFailure) {
                    println("  - Error: ${emailsResult.exceptionOrNull()?.message}")
                    println("  - Exception type: ${emailsResult.exceptionOrNull()?.javaClass?.simpleName}")
                    emailsResult.exceptionOrNull()?.printStackTrace()
                }
                if (emailsResult.isSuccess) {
                    val rawEmails = emailsResult.getOrThrow()
                    println("🔍 [EMAIL_SYNC_DEBUG] Successfully retrieved ${rawEmails.size} emails from client")
                    
                    // Log details of retrieved emails
                    rawEmails.forEachIndexed { index, email ->
                        println("📧 [EMAIL_SYNC_DEBUG] Email ${index + 1}:")
                        println("  - ID: ${email.id}")
                        println("  - Subject: ${email.subject}")
                        println("  - From: ${email.fromAddress}")
                        println("  - Date: ${email.sentDate}")
                        println("  - Body text length: ${email.bodyText?.length ?: 0}")
                        println("  - Body HTML length: ${email.bodyHtml?.length ?: 0}")
                        println("  - Is read: ${email.isRead}")
                    }
                    val emails = emailsResult.getOrThrow().map { email ->
                        email.copy(
                            accountId = account.id,
                            folderId = folder.id
                        )
                    }
                    
                    println("📥 Retrieved ${emails.size}  new emails")

                    // 输出第一emails的详细内容用于验证
                    if (emails.isNotEmpty()) {
                        val firstEmail = emails.first()
                        println("📧 [Email validation] First email details:")
                        println("  - Email ID: ${firstEmail.id}")
                        println("  - Message ID: ${firstEmail.messageId}")
                        println("  - Subject: ${firstEmail.subject}")
                        println("  - From: ${firstEmail.fromName} <${firstEmail.fromAddress}>")
                        println("  - To: ${firstEmail.toAddresses.joinToString(", ")}")
                        println("  - Sent time: ${firstEmail.sentDate}")
                        println("  - Received time: ${firstEmail.receivedDate}")
                        println("  - Is read: ${firstEmail.isRead}")
                        println("  - Email body length: ${firstEmail.bodyText?.length ?: 0}  characters")
                        println("  - Email body preview: ${firstEmail.bodyText?.take(100) ?: "No body"}")
                    }

                    // Insert new emails
                    var insertedCount = 0
                    for (email in emails) {
                        // Check if email already exists (by messageId)
                        val existing = emailRepository.getEmailByMessageId(email.messageId)
                        if (existing == null) {
                            emailRepository.insertEmail(email)
                            insertedCount++
                        }
                    }
                    println("✅ Successfully inserted $insertedCount  new emails")
                } else {
                    val error = emailsResult.exceptionOrNull()?.message
                    println("❌ Failed to get emails: $error")
                    
                    // 检查是否是网易Email的安全策略错误
        if (error?.contains("Unsafe Login") == true ||
            error?.contains("B64") == true ||
            error?.contains("Folder access restricted by Netease security policy") == true ||
            (error?.contains("NO EXAMINE") == true && error?.contains("PLEASE CONTACT") == true) ||
            (error?.contains("NO SELECT") == true && error?.contains("PLEASE CONTACT") == true) ||
            (error?.contains("NO EXAMINE") == true && error?.contains("Unsafe Login") == true) ||
            (error?.contains("NO EXAMINE") == true && error?.contains("kefu@188.com") == true)) {
                        println("⚠️ [NETEASE_SYNC] Detected NetEase email security policy restriction, skipping folder: ${folder.fullName}")
                        println("ℹ️ [NETEASE_SYNC] This is NetEase email security policy, does not affect INBOX sync")
                        return
                    }
                }
            } else {
                println("ℹ️ Folder ${folder.fullName} No new emails to sync")
                println("🔍 [EMAIL_SYNC_DEBUG] Skipping email sync because:")
                println("  - Total server emails: ${folder.messageCount}")
                println("  - Local existing emails: $existingCount")
                println("  - New emails to sync: $newEmailsCount (<= 0)")
            }

            // Update folder sync time
            folderRepository.updateFolder(
                folder.copy(lastSyncTime = System.currentTimeMillis())
            )
            println("✅ Folder ${folder.fullName} Sync completed")
        } catch (e: Exception) {
            println("❌ Folder ${folder.fullName} Sync error: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Sync specific folder
     */
    suspend fun syncFolder(accountId: String, folderId: String): Result<Unit> {
        return try {
            val account = accountRepository.getAccountById(accountId)
                ?: return Result.failure(Exception("Account not found"))

            val folder = folderRepository.getFolderById(folderId)
                ?: return Result.failure(Exception("Folder not found"))

            val client = getOrCreateImapClient(account)

            // Ensure connected
            if (!client.isConnected()) {
                val password = accountRepository.getPassword(account.id)
                    ?: return Result.failure(Exception("No password available"))

                val connectResult = client.connect(account, password)
                if (connectResult.isFailure) {
                    return Result.failure(connectResult.exceptionOrNull()!!)
                }
            }

            syncFolderEmails(account, client, folder)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mark email as read on server and locally
     */
    suspend fun markEmailAsRead(accountId: String, messageId: String, read: Boolean): Result<Unit> {
        return try {
            val account = accountRepository.getAccountById(accountId)
                ?: return Result.failure(Exception("Account not found"))

            val client = getOrCreateImapClient(account)

            // Ensure connected
            if (!client.isConnected()) {
                val password = accountRepository.getPassword(account.id)
                    ?: return Result.failure(Exception("No password available"))

                val connectResult = client.connect(account, password)
                if (connectResult.isFailure) {
                    return Result.failure(connectResult.exceptionOrNull()!!)
                }
            }

            // Update on server
            val serverResult = client.markAsRead(messageId, read)
            if (serverResult.isFailure) {
                return serverResult
            }

            // Update locally
            emailRepository.markEmailAsRead(messageId, read)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete email on server and locally
     */
    suspend fun deleteEmail(accountId: String, messageId: String): Result<Unit> {
        return try {
            val account = accountRepository.getAccountById(accountId)
                ?: return Result.failure(Exception("Account not found"))

            val client = getOrCreateImapClient(account)

            // Ensure connected
            if (!client.isConnected()) {
                val password = accountRepository.getPassword(account.id)
                    ?: return Result.failure(Exception("No password available"))

                val connectResult = client.connect(account, password)
                if (connectResult.isFailure) {
                    return Result.failure(connectResult.exceptionOrNull()!!)
                }
            }

            // Delete on server
            val serverResult = client.deleteMessage(messageId)
            if (serverResult.isFailure) {
                return serverResult
            }

            // Delete locally
            emailRepository.deleteEmail(messageId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Archive email
     */
    suspend fun archiveEmail(accountId: String, messageId: String): Result<Unit> {
        return try {
            val account = accountRepository.getAccountById(accountId)
                ?: return Result.failure(Exception("Account not found"))

            val client = getOrCreateImapClient(account)

            // Ensure connected
            if (!client.isConnected()) {
                val password = accountRepository.getPassword(account.id)
                    ?: return Result.failure(Exception("No password available"))

                val connectResult = client.connect(account, password)
                if (connectResult.isFailure) {
                    return Result.failure(connectResult.exceptionOrNull()!!)
                }
            }

            // Move email to archive folder
            val result = client.moveEmailToArchive(messageId)
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull()!!)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mark email as spam
     */
    suspend fun markAsSpam(accountId: String, messageId: String): Result<Unit> {
        return try {
            val account = accountRepository.getAccountById(accountId)
                ?: return Result.failure(Exception("Account not found"))

            val client = getOrCreateImapClient(account)

            // Ensure connected
            if (!client.isConnected()) {
                val password = accountRepository.getPassword(account.id)
                    ?: return Result.failure(Exception("No password available"))

                val connectResult = client.connect(account, password)
                if (connectResult.isFailure) {
                    return Result.failure(connectResult.exceptionOrNull()!!)
                }
            }

            // Move email to spam folder
            val result = client.moveEmailToSpam(messageId)
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull()!!)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search emails
     */
    suspend fun searchEmails(accountId: String, folderId: String, query: String): Result<List<Email>> {
        return try {
            val account = accountRepository.getAccountById(accountId)
                ?: return Result.failure(Exception("Account not found"))

            val folder = folderRepository.getFolderById(folderId)
                ?: return Result.failure(Exception("Folder not found"))

            val client = getOrCreateImapClient(account)

            // Ensure connected
            if (!client.isConnected()) {
                val password = accountRepository.getPassword(account.id)
                    ?: return Result.failure(Exception("No password available"))

                val connectResult = client.connect(account, password)
                if (connectResult.isFailure) {
                    return Result.failure(connectResult.exceptionOrNull()!!)
                }
            }

            // Open folder
            val openResult = client.openFolder(folder.fullName, readOnly = true, account)
            if (openResult.isFailure) {
                return Result.failure(openResult.exceptionOrNull()!!)
            }

            // Search
            val searchResult = client.searchMessages(query)
            if (searchResult.isFailure) {
                return searchResult
            }

            val emails = searchResult.getOrThrow().map { email ->
                email.copy(
                    accountId = account.id,
                    folderId = folder.id
                )
            }

            Result.success(emails)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get or create IMAP client for account
     */
    private fun getOrCreateImapClient(account: Account): ImapClient {
        return imapClients.getOrPut(account.id) {
            // 使用依赖注入的ImapClient实例
            imapClient
        }
    }

    /**
     * Update sync status
     */
    private fun updateSyncStatus(accountId: String, status: SyncStatus, error: String? = null) {
        val currentStatuses = _syncStatus.value.toMutableMap()
        currentStatuses[accountId] = status
        _syncStatus.value = currentStatuses

        // Update account sync status in repository
        scope.launch {
            try {
                val timestamp = if (status == SyncStatus.COMPLETED) System.currentTimeMillis() else null
                accountRepository.updateSyncStatus(accountId, timestamp, error)
            } catch (e: Exception) {
                // Ignore errors in status update
            }
        }
    }

    /**
     * Get sync status for account
     */
    fun getSyncStatus(accountId: String): SyncStatus {
        return _syncStatus.value[accountId] ?: SyncStatus.IDLE
    }

    /**
     * Stop all syncing
     */
    suspend fun stopAllSync() {
        syncJobs.values.forEach { it.cancel() }
        syncJobs.clear()

        imapClients.values.forEach { it.disconnect() }
        imapClients.clear()

        _syncStatus.value = emptyMap()
    }



    /**
     * Perform incremental sync for an account since last sync time
     */
    suspend fun syncAccountIncremental(accountId: String, lastSyncTime: Long): Result<Unit> {
        return try {
            val account = accountRepository.getAccountById(accountId)
                ?: return Result.failure(Exception("Account not found"))

            // For incremental sync, we would check lastSyncTime and only fetch newer emails
            // For now, delegate to full sync
            syncAccount(accountId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync only new emails for an account
     */
    suspend fun syncNewEmails(accountId: String): Result<Unit> {
        return try {
            val account = accountRepository.getAccountById(accountId)
                ?: return Result.failure(Exception("Account not found"))

            // For new emails only, we would only check INBOX
            // For now, delegate to full sync
            syncAccount(accountId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if account is currently syncing
     */
    fun isSyncing(accountId: String): Boolean {
        return getSyncStatus(accountId) in listOf(SyncStatus.CONNECTING, SyncStatus.SYNCING)
    }
}

/**
 * Sync status enumeration
 */
enum class SyncStatus {
    IDLE,
    CONNECTING,
    SYNCING,
    COMPLETED,
    ERROR
}
