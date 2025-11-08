package com.gf.mail.data.email

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.gf.mail.data.local.dao.EmailDao
import com.gf.mail.data.local.dao.FolderDao
import com.gf.mail.data.mapper.toEntity
import com.gf.mail.data.mapper.EmailMapper
import com.gf.mail.data.notification.EmailNotificationService
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.Email
// import com.gf.mail.domain.model.EmailUpdate
// import com.gf.mail.domain.model.ImapException
import com.gf.mail.domain.model.SyncState

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
/**
 * Background service for IMAP IDLE monitoring and push notifications
 */
class ImapIdleService(
    private val context: android.content.Context,
    private val emailRepository: com.gf.mail.domain.repository.EmailRepository,
    private val accountRepository: com.gf.mail.domain.repository.AccountRepository,
    private val folderRepository: com.gf.mail.domain.repository.FolderRepository,
    private val realtimeEmailSyncService: RealtimeEmailSyncService,
    private val performanceMonitor: com.gf.mail.data.performance.PerformanceMonitor,
    private val imapClient: ImapClient,
    private val credentialEncryption: com.gf.mail.data.security.CredentialEncryption,
    private val emailDao: EmailDao,
    private val notificationService: EmailNotificationService
) : Service() {
    
    companion object {
        private const val TAG = "ImapIdleService"
        const val ACTION_START_IDLE = "com.gf.mail.START_IDLE"
        const val ACTION_STOP_IDLE = "com.gf.mail.STOP_IDLE"
        const val EXTRA_ACCOUNT_ID = "account_id"
        private const val IDLE_CHECK_INTERVAL = 30000L // 30 seconds
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY = 5000L // 5 seconds
    }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeIdleJobs = mutableMapOf<String, kotlinx.coroutines.Job>()
    private val lastUidCache = mutableMapOf<String, Long>()
    private val retryCounters = mutableMapOf<String, Int>()
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ImapIdleService created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        
        when (intent?.action) {
            ACTION_START_IDLE -> {
                val accountId = intent.getStringExtra(EXTRA_ACCOUNT_ID)
                if (accountId != null) {
                    startIdleMonitoring(accountId)
                }
            }
            ACTION_STOP_IDLE -> {
                val accountId = intent.getStringExtra(EXTRA_ACCOUNT_ID)
                if (accountId != null) {
                    stopIdleMonitoring(accountId)
                } else {
                    stopAllIdleMonitoring()
                }
            }
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ImapIdleService destroyed")
        stopAllIdleMonitoring()
        serviceScope.cancel()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    /**
     * Start IDLE monitoring for an account
     */
    private fun startIdleMonitoring(accountId: String) {
        if (activeIdleJobs.containsKey(accountId)) {
            Log.d(TAG, "IDLE monitoring already active for account: $accountId")
            return
        }
        
        Log.d(TAG, "Starting IDLE monitoring for account: $accountId")
        
        val job = serviceScope.launch {
            try {
                // Get account information
                val account = getAccountById(accountId)
                if (account == null) {
                    Log.e(TAG, "Account not found: $accountId")
                    return@launch
                }
                
                // Connect to IMAP server
                val password = credentialEncryption.getPasswordSecurely(account.id) ?: ""
                val connectResult = imapClient.connect(account, password)
                if (connectResult.isFailure) {
                    Log.e(TAG, "Failed to connect to IMAP server for account: $accountId")
                    handleConnectionError(accountId, connectResult.exceptionOrNull())
                    return@launch
                }
                
                // Initialize last UID cache
                val lastUidResult = imapClient.getHighestUid("INBOX")
                if (lastUidResult.isSuccess) {
                    lastUidCache[accountId] = lastUidResult.getOrNull() ?: 0L
                }
                
                // Reset retry counter on successful connection
                retryCounters[accountId] = 0
                
                // Start IDLE monitoring (simplified for now)
                // TODO: Implement proper IDLE monitoring when EmailUpdate class is available
                Log.d(TAG, "IDLE monitoring started for account: $accountId")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in IDLE monitoring for account: $accountId", e)
                handleConnectionError(accountId, e)
            }
        }
        
        activeIdleJobs[accountId] = job
    }
    
    /**
     * Stop IDLE monitoring for an account
     */
    private fun stopIdleMonitoring(accountId: String) {
        Log.d(TAG, "Stopping IDLE monitoring for account: $accountId")
        
        activeIdleJobs[accountId]?.cancel()
        activeIdleJobs.remove(accountId)
        lastUidCache.remove(accountId)
        retryCounters.remove(accountId)
        
        imapClient.stopIdleMonitoring()
    }
    
    /**
     * Stop all IDLE monitoring
     */
    private fun stopAllIdleMonitoring() {
        Log.d(TAG, "Stopping all IDLE monitoring")
        
        activeIdleJobs.values.forEach { it.cancel() }
        activeIdleJobs.clear()
        lastUidCache.clear()
        retryCounters.clear()
        
        imapClient.stopIdleMonitoring()
    }
    
    /**
     * Handle IDLE update from IMAP server (simplified)
     */
    private suspend fun handleIdleUpdate(accountId: String, account: Account) {
        try {
            Log.d(TAG, "IDLE update for account $accountId")
            
            // TODO: Implement proper IDLE update handling when EmailUpdate class is available
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling IDLE update for account $accountId", e)
            handleConnectionError(accountId, e)
        }
    }
    
    /**
     * Save new messages to database
     */
    private suspend fun saveNewMessages(accountId: String, messages: List<Email>): Int {
        return withContext(Dispatchers.IO) {
            try {
                val emailEntities = messages.map { email ->
                    email.copy(accountId = accountId, folderId = "INBOX").toEntity()
                }
                
                emailDao.insertEmails(emailEntities)
                emailEntities.size
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save new messages", e)
                0
            }
        }
    }
    
    /**
     * Show notification for new emails
     */
    private fun showNewEmailNotification(account: Account, emails: List<Email>) {
        try {
            if (emails.size == 1) {
                notificationService.showNewEmailNotification(emails.first(), account.email)
            } else {
                notificationService.showMultipleEmailsNotification(emails, account.email)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification", e)
        }
    }
    
    /**
     * Handle connection errors with retry logic
     */
    private fun handleConnectionError(accountId: String, error: Throwable?) {
        val retryCount = retryCounters[accountId] ?: 0
        
        if (retryCount < MAX_RETRY_ATTEMPTS) {
            retryCounters[accountId] = retryCount + 1
            
            Log.d(TAG, "Retrying connection for account $accountId (attempt ${retryCount + 1}/$MAX_RETRY_ATTEMPTS)")
            
            serviceScope.launch {
                delay(RETRY_DELAY)
                startIdleMonitoring(accountId)
            }
        } else {
            Log.e(TAG, "Max retry attempts reached for account $accountId")
            retryCounters.remove(accountId)
            activeIdleJobs.remove(accountId)
            
            // Show error notification
            notificationService.showSyncStatusNotification(
                accountId, 
                false, 
                "Connection failed after $MAX_RETRY_ATTEMPTS attempts"
            )
        }
    }
    
    /**
     * Get account by ID from repository
     */
    private suspend fun getAccountById(accountId: String): Account? {
        return try {
            accountRepository.getAccountById(accountId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get account by ID: $accountId", e)
            null
        }
    }
}