package com.gf.mail.data.email

import com.gf.mail.domain.model.*
import com.gf.mail.utils.HtmlParser
import com.gf.mail.utils.AdvancedHtmlParser
import java.util.*
import javax.mail.Folder
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Store
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeUtility
import javax.mail.search.SearchTerm
import javax.mail.search.SubjectTerm
import javax.mail.search.FromStringTerm
import javax.mail.search.BodyTerm
import javax.mail.search.OrTerm
import javax.mail.search.HeaderTerm
import javax.mail.Flags
import javax.mail.Part
import com.sun.mail.imap.IMAPStore
import com.sun.mail.imap.IMAPFolder
import com.sun.mail.imap.protocol.IMAPProtocol
import java.io.InputStream
import java.nio.charset.Charset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.Result
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

/**
 * IMAP client for email communication
 * Handles connection, authentication, and email operations
 */
class ImapClient @Inject constructor() {

    private var store: Store? = null
    private var currentFolder: Folder? = null
    private var idleJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // IDLE configuration
    private var idleTimeout = 1800000L // 30 minutes
    private var isIdleEnabled = false

    /**
     * Connect to IMAP server with account configuration
     */
    suspend fun connect(account: Account, password: String): Result<Unit> = withContext(
        Dispatchers.IO
    ) {
        try {
            println("🔧 [IMAP_CONNECT] Starting IMAP server connection")
            println("📧 [IMAP_CONNECT] Email: ${account.email}")
            println("🔑 [IMAP_CONNECT] Password length: ${password.length}")
            
            // Create configuration based on email type
            val profile = createProviderProfile(account.email)
            val props = MailProps.forImap(profile)
            
            // Apply special security configuration for QQ email
            MailProps.applyQQSecurityConfiguration(props, account.email)
            
            // Enable IMAP debug logging for troubleshooting
            props.setProperty("mail.debug", "true")
            props.setProperty("mail.imap.debug", "true")
            props.setProperty("mail.imaps.debug", "true")
            
            println("🔧 [IMAP_CONNECT] Creating Session and Store")
            val session = Session.getInstance(props)
            store = session.getStore("imap") as IMAPStore
            
            println("🔌 [IMAP_CONNECT] Connecting to IMAP server: ${profile.imapHost()}:${profile.imapPort()}")
            
            // Connect to server
            store?.connect(profile.imapHost(), account.email, password)
            
            println("✅ [IMAP_CONNECT] IMAP connection successful")
            
            // For NetEase email, use new fix solution
            val imapStore = store as? IMAPStore
            if (imapStore != null && isNeteaseEmail(account.email)) {
                println("🔧 [IMAP_CONNECT] NetEase email connected successfully, applying security fix")
                
                try {
                    val fixResult = NeteaseImapFix.ensureSecureAndOpenInbox(imapStore, true)
                    println("🔧 [IMAP_FIX] Fix result: $fixResult")
                    
                    if (fixResult.ok && fixResult.folder != null) {
                        // Use the already opened INBOX folder from the fix result
                        currentFolder = fixResult.folder
                        println("✅ [IMAP_FIX] Current folder set to opened INBOX from fix result")
                    } else {
                        println("⚠️ [IMAP_FIX] Security fix failed: ${fixResult.note}")
                    }
                    
                    // Execute NetEase email diagnosis
                    println("🔍 [NETEASE_DEBUG] Running NetEase email diagnosis...")
                    val diagnosisResult = NeteaseDebugHelper.performFullDiagnosis(imapStore, account.email)
                    val diagnosisReport = NeteaseDebugHelper.generateDiagnosisReport(diagnosisResult)
                    println("📊 [NETEASE_DEBUG] Diagnosis Report:")
                    println(diagnosisReport)
                    
                } catch (e: Exception) {
                    println("❌ [IMAP_FIX] Security fix exception: ${e.message}")
                    // Fix failure should not prevent connection, continue execution
                }
            }
            
            println("🔍 [IMAP_CONNECT] Post-connection status check:")
            println("  - Store connected: ${store?.isConnected}")
            println("  - Store type: ${store?.javaClass?.simpleName}")
            
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Unknown error"
            println("❌ [IMAP_CONNECT] Connection failed: $errorMessage")
            
            Result.failure(ImapException("Failed to connect to IMAP server: $errorMessage", e))
        }
    }
    
    /**
     * Create Provider configuration based on email address
     */
    private fun createProviderProfile(email: String): MailProps.ProviderProfile {
        return when {
            email.contains("@163.com") || email.contains("@126.com") || email.contains("@188.com") -> {
                MailProps.createNeteaseProfile(email)
            }
            email.contains("@gmail.com") -> {
                MailProps.createGmailProfile()
            }
            email.contains("@qq.com") -> {
                MailProps.createQQProfile()
            }
            else -> {
                // Default configuration
                object : MailProps.ProviderProfile {
                    override fun imapHost(): String = "imap.gmail.com"
                    override fun imapPort(): Int = 993
                    override fun imapSsl(): Boolean = true
                    override fun imapStarttls(): Boolean = false
                    override fun smtpHost(): String = "smtp.gmail.com"
                    override fun smtpPort(): Int = 587
                    override fun smtpSsl(): Boolean = false
                    override fun smtpStarttls(): Boolean = true
                    override fun preferSelectOverExamine(): Boolean = true
                    override fun requireTls12Plus(): Boolean = true
                }
            }
        }
    }
    
    /**
     * Check if it's NetEase email
     */
    private fun isNeteaseEmail(email: String): Boolean {
        return email.contains("@163.com") || 
               email.contains("@126.com") || 
               email.contains("@188.com")
    }

    /**
     * Disconnect from IMAP server
     */
    suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            currentFolder?.close()
            store?.close()
            store = null
            currentFolder = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ImapException("Failed to disconnect: ${e.message}", e))
        }
    }

    /**
     * Get all folders from the server using standard IMAP commands
     */
    suspend fun getFolders(account: Account): Result<List<EmailFolder>> = withContext(Dispatchers.IO) {
        try {
            println("🔍 [GET_FOLDERS] Starting folder enumeration...")
            
            val store = this@ImapClient.store ?: return@withContext Result.failure(
                ImapException("Not connected to server")
            )
            
            val root = store.defaultFolder
            
            // Use standard IMAP LIST command to get all folders
            val allFolders = try {
                root.list("*")
            } catch (e: Exception) {
                println("❌ [GET_FOLDERS] LIST failed: ${e.message}")
                return@withContext Result.failure(ImapException("Failed to list folders: ${e.message}", e))
            }
            
            // Use standard IMAP LSUB command to get subscribed folders
            val subscribedFolders = try {
                root.listSubscribed("*")
            } catch (e: Exception) {
                println("⚠️ [GET_FOLDERS] LSUB failed: ${e.message}")
                emptyArray()
            }
            
            println("🔍 [GET_FOLDERS] Found ${allFolders.size} total folders, ${subscribedFolders.size} subscribed")
            
            // Log all discovered folders for debugging
            println("📋 [GET_FOLDERS] All discovered folders:")
            allFolders.forEachIndexed { index, folder ->
                val imapFolder = folder as IMAPFolder
                println("  ${index + 1}. '${imapFolder.name}' (fullName: '${imapFolder.fullName}') - attributes: ${imapFolder.attributes.joinToString(", ")}")
            }
            
            // Process each folder using standard IMAP attributes
            val emailFolders = mutableListOf<EmailFolder>()
            
            for (folder in allFolders) {
                try {
                    val imapFolder = folder as IMAPFolder
                    val fullName = imapFolder.fullName
                    val displayName = imapFolder.name
                    val attrs = imapFolder.attributes
                    val type = imapFolder.type
                    val subscribed = imapFolder.isSubscribed
                    
                    println("🔍 [GET_FOLDERS] Processing folder: '$displayName' (fullName: '$fullName')")
                    
                    // Skip \Noselect folders (container folders that cannot be selected)
                    val isNoselect = attrs.any { it.equals("\\Noselect", ignoreCase = true) }
                    val holdsMessages = (type and Folder.HOLDS_MESSAGES) != 0
                    
                    if (isNoselect || (!holdsMessages && !fullName.equals("INBOX", ignoreCase = true))) {
                        println("  - ⏭️ Skipping container folder: $displayName")
                        continue
                    }
                    
                    // Determine folder type using standard IMAP attributes
                    val folderType = when {
                        attrs.any { it.equals("\\Sent", ignoreCase = true) } -> {
                            println("  - ✅ Detected SENT folder by \\Sent attribute")
                            FolderType.SENT
                        }
                        attrs.any { it.equals("\\Trash", ignoreCase = true) } -> {
                            println("  - ✅ Detected TRASH folder by \\Trash attribute")
                            FolderType.TRASH
                        }
                        attrs.any { it.equals("\\Junk", ignoreCase = true) } -> {
                            println("  - ✅ Detected SPAM folder by \\Junk attribute")
                            FolderType.SPAM
                        }
                        attrs.any { it.equals("\\Drafts", ignoreCase = true) } -> {
                            println("  - ✅ Detected DRAFTS folder by \\Drafts attribute")
                            FolderType.DRAFTS
                        }
                        attrs.any { it.equals("\\Archive", ignoreCase = true) } -> {
                            println("  - ✅ Detected ARCHIVE folder by \\Archive attribute")
                            FolderType.ARCHIVE
                        }
                        fullName.equals("INBOX", ignoreCase = true) -> {
                            println("  - ✅ Detected INBOX folder by name")
                            FolderType.INBOX
                        }
                        else -> {
                            // Try to detect by folder name patterns
                            val detectedType = when {
                                displayName.contains("sent", ignoreCase = true) || displayName.contains("已发送", ignoreCase = true) -> {
                                    println("  - ✅ Detected SENT folder by name pattern: $displayName")
                                    FolderType.SENT
                                }
                                displayName.contains("draft", ignoreCase = true) || displayName.contains("草稿", ignoreCase = true) -> {
                                    println("  - ✅ Detected DRAFTS folder by name pattern: $displayName")
                                    FolderType.DRAFTS
                                }
                                displayName.contains("trash", ignoreCase = true) || displayName.contains("删除", ignoreCase = true) -> {
                                    println("  - ✅ Detected TRASH folder by name pattern: $displayName")
                                    FolderType.TRASH
                                }
                                displayName.contains("spam", ignoreCase = true) || displayName.contains("垃圾", ignoreCase = true) -> {
                                    println("  - ✅ Detected SPAM folder by name pattern: $displayName")
                                    FolderType.SPAM
                                }
                                else -> {
                                    println("  - ⚠️ Unknown folder type, marking as CUSTOM: $displayName")
                                    FolderType.CUSTOM
                                }
                            }
                            detectedType
                        }
                    }
                    
                    // Get message counts
                    var messageCount = 0
                    var unreadCount = 0
                    
                    if (imapFolder.exists()) {
                        try {
                            messageCount = imapFolder.messageCount
                            unreadCount = imapFolder.unreadMessageCount
                        } catch (e: Exception) {
                            println("  - ⚠️ Could not get message count: ${e.message}")
                        }
                    }
                    
                    val emailFolder = EmailFolder(
                        id = fullName,
                        accountId = "", // Will be set by repository (like backup code)
                        name = displayName, // Use displayName for name (like backup code)
                        fullName = fullName,
                        displayName = displayName,
                        type = folderType,
                        messageCount = messageCount,
                        unreadCount = unreadCount,
                        canHoldMessages = holdsMessages,
                        canHoldFolders = (type and Folder.HOLDS_FOLDERS) != 0,
                        isSubscribed = subscribed,
                        parentFolder = imapFolder.parent?.fullName,
                        lastSyncTime = System.currentTimeMillis()
                    )
                    
                    println("  - 🔧 Created EmailFolder with messageCount: ${emailFolder.messageCount}")
                    
                    emailFolders.add(emailFolder)
                    println("✅ [GET_FOLDERS] Added folder: $displayName (type: $folderType, messages: $messageCount, unread: $unreadCount)")
                    println("🔍 [GET_FOLDERS] Folder details: id=${emailFolder.id}, accountId=${emailFolder.accountId}, fullName=${emailFolder.fullName}, type=${emailFolder.type}")
                    println("🔍 [GET_FOLDERS] Folder type details: type.name=${emailFolder.type.name}, type.ordinal=${emailFolder.type.ordinal}")
                    
                } catch (e: Exception) {
                    println("❌ [GET_FOLDERS] Failed to process folder: ${e.message}")
                }
            }
            
            println("🔍 [GET_FOLDERS] Enumeration complete. Found ${emailFolders.size} folders:")
            emailFolders.forEach { folder ->
                println("  - ${folder.fullName} (${folder.name}) - ${folder.type} - ${folder.messageCount} messages")
            }
            
            Result.success(emailFolders)
        } catch (e: Exception) {
            Result.failure(ImapException("Failed to get folders: ${e.message}", e))
        }
    }

    /**
     * Create folder name mapping from display names to IMAP encoded names
     */
    private suspend fun createFolderNameMapping(store: IMAPStore): Map<String, String> = withContext(Dispatchers.IO) {
        val mapping = mutableMapOf<String, String>()
        
        try {
            println("🔍 [FOLDER_MAPPING] Starting to create folder name mapping... - version: 2025-01-08-v3")
            println("🔍 [DEBUG] createFolderNameMapping method called")
            
            // Use JavaMail API to get Folder list
            val folders = try {
                store.defaultFolder.list("*")
            } catch (e: Exception) {
                println("❌ [FOLDER_MAPPING] Failed to get folder list: ${e.message}")
                return@withContext mapping
            }
            
            println("🔍 [FOLDER_MAPPING] Successfully retrieved ${folders.size} folders")
            
            for (folder in folders) {
                try {
                    val displayName = folder.name
                    val fullName = folder.fullName
                    
                    println("🔍 [FOLDER_MAPPING] Processing folder: '$displayName' -> '$fullName'")
                    
                    // If fullName contains encoded characters (like starting with &), it's an IMAP encoded name
                    if (fullName.startsWith("&") || fullName != displayName) {
                        mapping[displayName] = fullName
                        println("✅ [FOLDER_MAPPING] Mapped: '$displayName' -> '$fullName'")
                    } else {
                        // If fullName and displayName are the same, there's no encoding
                        mapping[displayName] = displayName
                        println("ℹ️ [FOLDER_MAPPING] No encoding: '$displayName' -> '$displayName'")
                    }
                } catch (e: Exception) {
                    println("⚠️ [FOLDER_MAPPING] Failed to process folder: ${e.message}")
                }
            }
            
            // Add folder type mappings
            val folderTypes = listOf("inbox", "sent", "drafts", "trash", "spam", "archive", "starred")
            
            for ((displayName, encodedName) in mapping) {
                // Use hardcoded folder type detection for standard folders
                val folderType = when {
                    displayName.equals("INBOX", ignoreCase = true) -> "inbox"
                    displayName.equals("Sent", ignoreCase = true) -> "sent"
                    displayName.equals("Drafts", ignoreCase = true) -> "drafts"
                    displayName.equals("Trash", ignoreCase = true) -> "trash"
                    displayName.equals("Spam", ignoreCase = true) -> "spam"
                    displayName.equals("Archive", ignoreCase = true) -> "archive"
                    else -> null
                }
                if (folderType != null) {
                    when (folderType) {
                        "inbox" -> {
                            mapping["INBOX"] = "INBOX"
                            mapping["Inbox"] = "INBOX"
                            mapping["Inbox"] = "INBOX"
                        }
                        "sent", "drafts", "trash", "spam", "archive" -> {
                            if (encodedName.startsWith("&")) {
                                // Use hardcoded English names for standard folders
                                val englishName = when (folderType) {
                                    "sent" -> "Sent"
                                    "drafts" -> "Drafts"
                                    "trash" -> "Trash"
                                    "spam" -> "Spam"
                                    "archive" -> "Archive"
                                    else -> folderType
                                }
                                mapping[englishName] = encodedName
                            }
                        }
                        "starred" -> {
                            // Starred is a virtual folder, no IMAP encoded name needed
                            mapping["Starred"] = "Starred"
                        }
                    }
                }
            }
            
            println("✅ [FOLDER_MAPPING] Folder name mapping completed, total ${mapping.size} mappings:")
            mapping.forEach { (displayName, encodedName) ->
                println("  - '$displayName' -> '$encodedName'")
            }
            
        } catch (e: Exception) {
            println("❌ [FOLDER_MAPPING] Failed to create folder name mapping: ${e.message}")
        }
        
        mapping
    }
    
    /**
     * Fallback method to create folder name mapping using JavaMail API
     */
    private suspend fun createFolderNameMappingFallback(store: IMAPStore): Map<String, String> = withContext(Dispatchers.IO) {
        val mapping = mutableMapOf<String, String>()
        
        try {
            println("🔍 [FOLDER_MAPPING_FALLBACK] Using JavaMail API fallback...")
            
            // Get all Folders
            val folders = try {
                store.defaultFolder.list("*")
            } catch (e: Exception) {
                println("❌ [FOLDER_MAPPING_FALLBACK] Failed to get folder list: ${e.message}")
                return@withContext mapping
            }
            
            println("🔍 [FOLDER_MAPPING_FALLBACK] Successfully retrieved ${folders.size} folders")
            
            for (folder in folders) {
                try {
                    val displayName = folder.name
                    val fullName = folder.fullName
                    
                    println("🔍 [FOLDER_MAPPING_FALLBACK] Processing folder: '$displayName' -> '$fullName'")
                    
                    // If fullName contains encoded characters (like starting with &), it's an IMAP encoded name
                    if (fullName.startsWith("&") || fullName != displayName) {
                        mapping[displayName] = fullName
                        println("✅ [FOLDER_MAPPING_FALLBACK] Mapped: '$displayName' -> '$fullName'")
                    } else {
                        // If fullName and displayName are the same, there's no encoding
                        mapping[displayName] = displayName
                        println("ℹ️ [FOLDER_MAPPING_FALLBACK] No encoding: '$displayName' -> '$displayName'")
                    }
                } catch (e: Exception) {
                    println("⚠️ [FOLDER_MAPPING_FALLBACK] Failed to process folder: ${e.message}")
                }
            }
            
            // Add standard folder mappings
            mapping["INBOX"] = "INBOX"
            mapping["Inbox"] = "INBOX"
            mapping["inbox"] = "INBOX"
            
            println("✅ [FOLDER_MAPPING_FALLBACK] Folder name mapping completed, total ${mapping.size}  mappings:")
            mapping.forEach { (displayName, encodedName) ->
                println("  - '$displayName' -> '$encodedName'")
            }
            
        } catch (e: Exception) {
            println("❌ [FOLDER_MAPPING_FALLBACK] Failed to create folder name mapping: ${e.message}")
        }
        
        mapping
    }

    /**
     * Get actual folder name from server based on display name
     */
    /**
     * Get actual folder name from server using proper IMAP encoding
     * Following the principle: use getFullName() as-is, no manual encoding/decoding
     */
    private suspend fun getActualFolderName(store: IMAPStore, displayName: String): String = withContext(Dispatchers.IO) {
        try {
            println("🔍 [FOLDER_MAPPING] Finding actual folder name: $displayName")

            // Special handling: INBOX directly returns "INBOX", no complex mapping needed
            if (displayName.equals("INBOX", ignoreCase = true)) {
                println("🔍 [FOLDER_MAPPING] INBOX detected, returning directly: INBOX")
                return@withContext "INBOX"
            }

            // If the input is already an IMAP encoded name (starting with &), return directly
            if (displayName.startsWith("&")) {
                println("🔍 [FOLDER_MAPPING] Already IMAP encoded name, returning as-is: $displayName")
                return@withContext displayName
            }

            // Use LIST command to get Folder information
            val rootFolder = store.defaultFolder
            val allFolders = try {
                rootFolder.list("*")
            } catch (e: Exception) {
                println("❌ [FOLDER_MAPPING] Failed to enumerate folders: ${e.message}")
                emptyArray()
            }

            // Folder mapping strategy: prioritize SPECIAL-USE attributes
            var foundBySpecialUse = false
            var foundByDisplayName = false
            var specialUseMatch = ""
            var displayNameMatch = ""
            
            for (folder in allFolders) {
                val imapFolder = folder as IMAPFolder
                val serverId = imapFolder.fullName  // Use serverId as unique identifier
                val folderName = imapFolder.name
                val attributes = imapFolder.attributes
                
                // 1. Prioritize SPECIAL-USE attribute matching
                val isSpecialMatch = when {
                    // Check for sent folder
                    displayName.equals("sent", ignoreCase = true) -> 
                        attributes.any { it.equals("\\Sent", ignoreCase = true) }
                    
                    // Check for drafts folder
                    displayName.equals("drafts", ignoreCase = true) -> 
                        attributes.any { it.equals("\\Drafts", ignoreCase = true) }
                    
                    // Check for trash folder
                    displayName.equals("trash", ignoreCase = true) -> 
                        attributes.any { it.equals("\\Trash", ignoreCase = true) }
                    
                    // Check for inbox folder
                    displayName.equals("inbox", ignoreCase = true) -> {
                        // INBOX special handling: prioritize INBOX name over SPECIAL-USE attribute
                        serverId.equals("INBOX", ignoreCase = true)
                    }
                    else -> false
                }
                
                if (isSpecialMatch) {
                    println("🔍 [FOLDER_MAPPING] Found match via SPECIAL-USE: $displayName -> $serverId (attributes: ${attributes.joinToString(", ")})")
                    foundBySpecialUse = true
                    specialUseMatch = serverId
                }
                
                // 2. Fallback to display name matching
                if (folderName.equals(displayName, ignoreCase = true)) {
                    println("🔍 [FOLDER_MAPPING] Found match via display name: $displayName -> $serverId")
                    foundByDisplayName = true
                    displayNameMatch = serverId
                }
            }
            
            // Corrected strategy: prioritize SPECIAL-USE match, fallback to display name match
            if (foundBySpecialUse) {
                // For SPECIAL-USE match, check if the returned name is Chinese
                // If it's a Chinese name, it means the server returned fullName in Chinese, use as-is
                if (specialUseMatch.matches(Regex(".*[\\u4e00-\\u9fff].*"))) {
                    println("🔍 [FOLDER_MAPPING] SPECIAL-USE match returned Chinese name, using as-is: $displayName -> $specialUseMatch")
                    return@withContext specialUseMatch
                } else {
                    println("🔍 [FOLDER_MAPPING] Using SPECIAL-USE match: $displayName -> $specialUseMatch")
                    return@withContext specialUseMatch
                }
            } else if (foundByDisplayName) {
                println("🔍 [FOLDER_MAPPING] Using display name match: $displayName -> $displayNameMatch")
                return@withContext displayNameMatch
            }
            
            // If not found, fallback to original name
            println("🔍 [FOLDER_MAPPING] No matching folder found, using original name: $displayName")
            
            
            displayName

        } catch (e: Exception) {
            println("❌ [FOLDER_MAPPING] Failed to get folder name: ${e.message}")
            displayName
        }
    }

    /**
     * Execute operation with retry mechanism for folder operations
     */
    private suspend fun <T> executeWithRetry(
        maxRetries: Int = 2,
        operation: suspend () -> Result<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        
        repeat(maxRetries + 1) { attempt ->
            try {
                val result = operation()
                if (result.isSuccess) {
                    return@withContext result
                } else {
                    val exception = result.exceptionOrNull()
                    if (exception is IllegalStateException && 
                        (exception.message?.contains("closed folder") == true || 
                         exception.message?.contains("not allowed on a closed folder") == true)) {
                        println("🔄 [RETRY] Detected folder close error, attempting retry (${attempt + 1}th time)")
                        lastException = exception
                        if (attempt < maxRetries) {
                            delay(500) // Wait 500ms before retry
                        }
                    } else {
                        return@withContext result
                    }
                }
            } catch (e: Exception) {
                if (e is IllegalStateException && 
                    (e.message?.contains("closed folder") == true || 
                     e.message?.contains("not allowed on a closed folder") == true)) {
                    println("🔄 [RETRY] Detected folder close exception, attempting retry (${attempt + 1}th time)")
                    lastException = e
                    if (attempt < maxRetries) {
                        delay(500) // Wait 500ms before retry
                    }
                } else {
                    return@withContext Result.failure(e)
                }
            }
        }
        
        Result.failure(lastException ?: Exception("Max retries exceeded"))
    }

    /**
     * Open a folder for reading/writing using standard IMAP commands
     */
    suspend fun openFolder(folderName: String, readOnly: Boolean = true, account: Account? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            println("🔍 [OPEN_FOLDER] Opening folder: $folderName (readOnly: $readOnly)")
            
            val store = this@ImapClient.store ?: return@withContext Result.failure(
                ImapException("Not connected to server")
            )

            // Close current folder if it's different from the target folder (like backup code)
            if (currentFolder != null && currentFolder?.isOpen == true && currentFolder?.fullName != folderName) {
                println("🔍 [OPEN_FOLDER] Closing current folder: ${currentFolder?.fullName}")
                try {
                    currentFolder?.close()
                } catch (e: Exception) {
                    println("⚠️ [OPEN_FOLDER] Exception while closing folder (ignored): ${e.message}")
                }
            }
            
            // Get the folder
            currentFolder = store.getFolder(folderName)
            if (currentFolder == null) {
                println("❌ [OPEN_FOLDER] Cannot get folder: $folderName")
                return@withContext Result.failure(ImapException("Folder not found: $folderName"))
            }
            
            // For NetEase email, use NeteaseImapFix.ensureSecureAndOpenFolder
            val imapStore = store as? IMAPStore
            println("🔍 [OPEN_FOLDER] Debug info:")
            println("  - imapStore: ${imapStore != null}")
            println("  - account: $account")
            println("  - account.email: ${account?.email}")
            println("  - isNeteaseEmail: ${isNeteaseEmail(account?.email ?: "")}")
            
            if (imapStore != null && isNeteaseEmail(account?.email ?: "")) {
                println("🔧 [OPEN_FOLDER] Using NeteaseImapFix for NetEase email folder: $folderName")
                try {
                    val fixResult = NeteaseImapFix.ensureSecureAndOpenFolder(imapStore, folderName, !readOnly)
                    if (fixResult.ok) {
                        // NeteaseImapFix.ensureSecureAndOpenFolder only returns Result, not ResultWithFolder
                        // So we need to get the folder separately
                        val folder = imapStore.getFolder(folderName) as IMAPFolder
                        currentFolder = folder
                        println("✅ [OPEN_FOLDER] NeteaseImapFix opened folder successfully: $folderName")
                        return@withContext Result.success(Unit)
                    } else {
                        println("❌ [OPEN_FOLDER] NeteaseImapFix failed: ${fixResult.note}")
                        return@withContext Result.failure(ImapException("NeteaseImapFix failed: ${fixResult.note}"))
                    }
                } catch (e: Exception) {
                    println("❌ [OPEN_FOLDER] NeteaseImapFix exception: ${e.message}")
                    return@withContext Result.failure(ImapException("NeteaseImapFix exception: ${e.message}", e))
                }
            }
            
            // Open the folder using standard IMAP commands (for non-NetEase emails)
            val openMode = if (readOnly) Folder.READ_ONLY else Folder.READ_WRITE
            println("🔍 [OPEN_FOLDER] Attempting to open folder: $folderName (mode: ${if (readOnly) "read-only" else "read-write"})")
            
            try {
                currentFolder?.open(openMode)
                println("✅ [OPEN_FOLDER] Folder opened successfully: $folderName")
                Result.success(Unit)
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                println("❌ [OPEN_FOLDER] Failed to open folder: $folderName - $errorMessage")
                Result.failure(ImapException("Failed to open folder: $errorMessage", e))
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Unknown error"
            println("❌ [OPEN_FOLDER] Folder opening failed: $folderName - $errorMessage")
            Result.failure(ImapException("Failed to open folder: $errorMessage", e))
        }
    }
    /**
     * Get emails from a specific folder
     */
    suspend fun getEmails(
        folderName: String,
        count: Int = 50,
        offset: Int = 0,
        account: Account? = null
    ): Result<List<Email>> = withContext(Dispatchers.IO) {
        try {
            println("🔍 [GET_EMAILS] Starting to get emails: folderName=$folderName, count=$count, offset=$offset")
            println("🔍 [GET_EMAILS] Current folder state:")
            println("  - currentFolder: ${currentFolder?.fullName}")
            println("  - currentFolder.isOpen: ${currentFolder?.isOpen}")
            println("  - target folderName: $folderName")
            println("  - folderName match: ${currentFolder?.fullName == folderName}")
            
            // Special handling: if we want to get INBOX emails and INBOX is already open, use directly (like backup code)
            if (folderName == "INBOX" && currentFolder != null && 
                currentFolder?.fullName == "INBOX" && currentFolder?.isOpen == true) {
                println("✅ [GET_EMAILS] INBOX is already open, using existing folder directly")
                // Call getMessages directly, skip openFolder
                return@withContext getMessages(count, offset, account)
            }
            
            
            // Use standard method to open folder (refer to backup program implementation)
            val openResult = openFolder(folderName, true, account)
            if (openResult.isFailure) {
                val error = openResult.exceptionOrNull()?.message
                println("❌ [GET_EMAILS] Failed to open folder: $error")


                return@withContext Result.failure(openResult.exceptionOrNull() ?: ImapException("Failed to open folder"))
            }
            
            println("✅ [GET_EMAILS] Folder opened successfully, starting to get messages...")
            
            // Check if current Folder is available
            if (currentFolder == null) {
                println("ℹ️ [GET_EMAILS] Folder restricted by security policy, cannot get emails")
                return@withContext Result.success(emptyList())
            }
            
            // Verify Folder status
            if (!currentFolder!!.isOpen) {
                println("⚠️ [GET_EMAILS] Folder not open, attempting to reopen...")
                
                // For NetEase email, use NeteaseImapFix for reopen as well
                val store = this@ImapClient.store as? IMAPStore
                println("🔍 [GET_EMAILS] Debug info for reopen:")
                println("  - store: ${store != null}")
                println("  - account: $account")
                println("  - account.email: ${account?.email}")
                println("  - isNeteaseEmail: ${isNeteaseEmail(account?.email ?: "")}")
                
                if (store != null && isNeteaseEmail(account?.email ?: "")) {
                    println("🔧 [GET_EMAILS] Using NeteaseImapFix for NetEase email folder reopen: ${currentFolder!!.fullName}")
                    try {
                        val fixResult = NeteaseImapFix.ensureSecureAndOpenFolder(store, currentFolder!!.fullName, false)
                        if (fixResult.ok) {
                            println("✅ [GET_EMAILS] NeteaseImapFix reopened folder successfully")
                        } else {
                            println("❌ [GET_EMAILS] NeteaseImapFix reopen failed: ${fixResult.note}")
                            return@withContext Result.failure(ImapException("NeteaseImapFix reopen failed: ${fixResult.note}"))
                        }
                    } catch (e: Exception) {
                        println("❌ [GET_EMAILS] NeteaseImapFix reopen exception: ${e.message}")
                        return@withContext Result.failure(ImapException("NeteaseImapFix reopen exception: ${e.message}", e))
                    }
                } else {
                    // Standard reopen for non-NetEase emails
                    try {
                        currentFolder!!.open(Folder.READ_ONLY)
                        println("✅ [GET_EMAILS] Folder reopened successfully")
                    } catch (e: Exception) {
                        println("❌ [GET_EMAILS] Failed to reopen folder: ${e.message}")
                        return@withContext Result.failure(ImapException("Failed to reopen folder: ${e.message}", e))
                    }
                }
            } else {
                println("✅ [GET_EMAILS] Folder already open, getting messages directly")
            }
            
            val messagesResult = getMessages(count, offset, account)
            
            if (messagesResult.isSuccess) {
                val emails = messagesResult.getOrNull() ?: emptyList()
                println("✅ [GET_EMAILS] Successfully retrieved ${emails.size}  emails")
            } else {
                val error = messagesResult.exceptionOrNull()?.message
                println("❌ [GET_EMAILS] Failed to get messages: $error")
                
                // If it's a Folder closed error, try to reopen and retry
                if (error?.contains("closed folder") == true || error?.contains("not allowed on a closed folder") == true) {
                    println("🔄 [GET_EMAILS] Detected folder close error, attempting to reopen and retry...")
                    try {
                        val retryOpenResult = openFolder(folderName, true, account)
                        if (retryOpenResult.isSuccess) {
                            val retryMessagesResult = getMessages(count, offset, account)
                            if (retryMessagesResult.isSuccess) {
                                val emails = retryMessagesResult.getOrNull() ?: emptyList()
                                println("✅ [GET_EMAILS] Retry successful, retrieved ${emails.size}  emails")
                                return@withContext retryMessagesResult
                            }
                        }
                    } catch (retryException: Exception) {
                        println("❌ [GET_EMAILS] Retry failed: ${retryException.message}")
                    }
                }
            }
            
            return@withContext messagesResult
        } catch (e: Exception) {
            println("❌ [GET_EMAILS] Exception while getting emails: ${e.message}")
            e.printStackTrace()
            Result.failure(ImapException("Failed to get emails from folder '$folderName': ${e.message}", e))
        }
    }

    /**
     * Get messages from current folder
     */
suspend fun getMessages(
        count: Int = 50,
        offset: Int = 0,
        account: Account? = null
    ): Result<List<Email>> = executeWithRetry {
        getMessagesInternal(count, offset, account)
    }

    /**
     * Internal method to get messages from current folder
     */
    private suspend fun getMessagesInternal(
        count: Int = 50,
        offset: Int = 0,
        account: Account? = null
    ): Result<List<Email>> = withContext(Dispatchers.IO) {
        try {
            println("🔍 [GET_MESSAGES] Starting to get messages: count=$count, offset=$offset")
            
            var folder = currentFolder
            if (folder == null) {
                println("ℹ️ [GET_MESSAGES] No folder available, cannot get emails")
                return@withContext Result.success(emptyList())
            }

            println("🔍 [GET_MESSAGES] Current folder information:")
            println("  - Folder name: ${folder.name}")
            println("  - Full name: ${folder.fullName}")
            println("  - Is open: ${folder.isOpen}")
            println("  - Total messages: ${folder.messageCount}")

            // Check Folder status, if not open then try to reopen
            if (!folder.isOpen) {
                println("⚠️ [GET_MESSAGES] Folder not open, attempting to reopen...")
                
                // For NetEase email, use NeteaseImapFix for reopen as well
                val store = this@ImapClient.store as? IMAPStore
                // Always try NeteaseImapFix for NetEase stores (we can detect by the error pattern)
                if (store != null && isNeteaseEmail(account?.email ?: "")) {
                    println("🔧 [GET_MESSAGES] Using NeteaseImapFix for NetEase email folder reopen: ${folder.fullName}")
                    try {
                        val fixResult = NeteaseImapFix.ensureSecureAndOpenFolder(store, folder.fullName, false)
                        if (fixResult.ok && fixResult.folder != null) {
                            // Use the folder object returned by NeteaseImapFix
                            val newFolder = fixResult.folder!!
                            // Update the folder reference to use the newly opened folder
                            folder = newFolder
                            // Also update currentFolder to maintain consistency
                            currentFolder = newFolder
                            println("✅ [GET_MESSAGES] NeteaseImapFix reopened folder successfully")
                        } else {
                            println("❌ [GET_MESSAGES] NeteaseImapFix reopen failed: ${fixResult.note}")
                            return@withContext Result.failure(ImapException("NeteaseImapFix reopen failed: ${fixResult.note}"))
                        }
                    } catch (e: Exception) {
                        println("❌ [GET_MESSAGES] NeteaseImapFix reopen exception: ${e.message}")
                        return@withContext Result.failure(ImapException("NeteaseImapFix reopen exception: ${e.message}", e))
                    }
                } else {
                    // Standard reopen for non-NetEase emails
                    try {
                        folder.open(Folder.READ_ONLY)
                        println("✅ [GET_MESSAGES] Folder reopened successfully")
                    } catch (e: Exception) {
                        println("❌ [GET_MESSAGES] Failed to reopen folder: ${e.message}")
                        return@withContext Result.failure(ImapException("Failed to reopen folder: ${e.message}", e))
                    }
                }
            }

            // Check Folder status again
            if (!folder.isOpen) {
                println("❌ [GET_MESSAGES] Folder still not open, cannot get messages")
                return@withContext Result.failure(ImapException("Folder is not open"))
            }

            val messageCount = folder.messageCount
            if (messageCount == 0) {
                println("ℹ️ [GET_MESSAGES] No emails in folder")
                return@withContext Result.success(emptyList())
            }

            // Calculate range (IMAP uses 1-based indexing)
            val start = maxOf(1, messageCount - offset - count + 1)
            val end = maxOf(1, messageCount - offset)
            
            println("🔍 [GET_MESSAGES] Calculating message range:")
            println("  - Total messages: $messageCount")
            println("  - Requested count: $count")
            println("  - Offset: $offset")
            println("  - Start position: $start")
            println("  - End position: $end")

            // Check Folder status again before calling getMessages
            if (!folder.isOpen) {
                println("❌ [GET_MESSAGES] Folder closed before getting messages")
                return@withContext Result.failure(ImapException("Folder was closed before getting messages"))
            }

            val messages = if (start <= end) {
                println("🔍 [GET_MESSAGES] Calling folder.getMessages($start, $end)")
                try {
                    val retrievedMessages = folder.getMessages(start, end)
                    println("🔍 [GET_MESSAGES] Retrieved ${retrievedMessages.size} messages from server")
                    
                    // Prefetch message content to ensure we have the full message data
                    if (retrievedMessages.isNotEmpty()) {
                        try {
                            println("🔍 [GET_MESSAGES] Prefetching message content...")
                            val fetchProfile = javax.mail.FetchProfile().apply {
                                add(javax.mail.FetchProfile.Item.ENVELOPE)
                                add(javax.mail.FetchProfile.Item.CONTENT_INFO)
                                add(javax.mail.FetchProfile.Item.FLAGS)
                                // Add more specific content fetching
                                add(javax.mail.FetchProfile.Item.SIZE)
                            }
                            folder.fetch(retrievedMessages, fetchProfile)
                            println("✅ [GET_MESSAGES] Message content prefetch completed")
                            
                            // Log prefetch results for debugging
                            for (i in retrievedMessages.indices) {
                                val msg = retrievedMessages[i]
                                println("🔍 [GET_MESSAGES] Prefetched message ${i + 1}:")
                                println("  - Subject: ${msg.subject}")
                                println("  - Content type: ${msg.contentType}")
                                println("  - Size: ${msg.size}")
                                println("  - Content class: ${msg.content?.javaClass?.simpleName}")
                            }
                        } catch (e: Exception) {
                            println("⚠️ [GET_MESSAGES] Failed to prefetch message content: ${e.message}")
                            e.printStackTrace()
                            // Continue without prefetch
                        }
                    }
                    
                    retrievedMessages
                } catch (e: IllegalStateException) {
                    if (e.message?.contains("closed folder") == true) {
                        println("❌ [GET_MESSAGES] Folder closed during operation, attempting to reopen...")
                        try {
                            folder.open(Folder.READ_ONLY)
                            println("✅ [GET_MESSAGES] Folder reopened successfully, retrying to get messages...")
                            val retryMessages = folder.getMessages(start, end)
                            
                            // Prefetch content for retry messages too
                            if (retryMessages.isNotEmpty()) {
                                try {
                                    val fetchProfile = javax.mail.FetchProfile().apply {
                                        add(javax.mail.FetchProfile.Item.ENVELOPE)
                                        add(javax.mail.FetchProfile.Item.CONTENT_INFO)
                                        add(javax.mail.FetchProfile.Item.FLAGS)
                                        add(javax.mail.FetchProfile.Item.SIZE)
                                    }
                                    folder.fetch(retryMessages, fetchProfile)
                                    println("✅ [GET_MESSAGES] Retry message content prefetch completed")
                                } catch (e: Exception) {
                                    println("⚠️ [GET_MESSAGES] Failed to prefetch retry message content: ${e.message}")
                                    e.printStackTrace()
                                }
                            }
                            
                            retryMessages
                        } catch (retryException: Exception) {
                            println("❌ [GET_MESSAGES] Retry failed to get messages: ${retryException.message}")
                            throw retryException
                        }
                    } else {
                        throw e
                    }
                }
            } else {
                println("⚠️ [GET_MESSAGES] Start position greater than end position, returning empty array")
                emptyArray()
            }
            
            println("🔍 [GET_MESSAGES] Retrieved ${messages.size}  raw messages")

            val emails = messages.mapIndexed { index, message ->
                try {
                    println("📧 [GET_MESSAGES] Parsing message ${index + 1}...")
                    
                    // Log message details before parsing
                    println("📧 [GET_MESSAGES] Message ${index + 1} details:")
                    println("  - Message number: ${message.messageNumber}")
                    println("  - Subject: ${message.subject}")
                    println("  - From: ${message.from?.firstOrNull()}")
                    println("  - Content type: ${message.contentType}")
                    println("  - Size: ${message.size}")
                    println("  - Flags: ${message.flags}")
                    
                    val email = parseMessage(message)
                    println("📧 [GET_MESSAGES] Successfully parsed message ${index + 1}: ${email.subject}")
                    
                    // Output first email's detailed content for verification
                    if (index == 0) {
                        println("📧 [GET_MESSAGES] First email detailed content:")
                        println("  - Email ID: ${email.id}")
                        println("  - Message ID: ${email.messageId}")
                        println("  - Subject: ${email.subject}")
                        println("  - From: ${email.fromName} <${email.fromAddress}>")
                        println("  - To: ${email.toAddresses.joinToString(", ")}")
                        println("  - Sent time: ${email.sentDate}")
                        println("  - Received time: ${email.receivedDate}")
                        println("  - Is read: ${email.isRead}")
                        println("  - Email body length: ${email.bodyText?.length ?: 0}  characters")
                        println("  - Email body preview: ${email.bodyText?.take(100) ?: "No body"}")
                        println("  - HTML body length: ${email.bodyHtml?.length ?: 0}  characters")
                        println("  - HTML body preview: ${email.bodyHtml?.take(100) ?: "No HTML body"}")
                    }
                    
                    email
                } catch (e: Exception) {
                    println("❌ [GET_MESSAGES] Parsing message ${index + 1}  failed: ${e.message}")
                    e.printStackTrace()
                    // Create a basic email object
                    Email(
                        id = "error-${index}",
                        accountId = "",
                        folderId = "",
                        threadId = "error-${index}",
                        subject = "Parse Failed",
                        fromName = "Unknown",
                        fromAddress = "unknown@example.com",
                        toAddresses = emptyList(),
                        sentDate = System.currentTimeMillis(),
                        receivedDate = System.currentTimeMillis(),
                        messageId = "error-${index}"
                    )
                }
            }

            println("✅ [GET_MESSAGES] Successfully parsed ${emails.size}  emails")
            Result.success(emails.reversed()) // Most recent first
        } catch (e: Exception) {
            println("❌ [GET_MESSAGES] Exception while getting messages: ${e.message}")
            e.printStackTrace()
            Result.failure(ImapException("Failed to get messages: ${e.message}", e))
        }
    }

    /**
     * Search for messages
     */
    suspend fun searchMessages(query: String): Result<List<Email>> = withContext(Dispatchers.IO) {
        try {
            val folder = currentFolder ?: return@withContext Result.failure(
                ImapException("No folder opened")
            )

            // Create search terms
            val searchTerms = arrayOf(
                SubjectTerm(query),
                FromStringTerm(query),
                BodyTerm(query)
            )
            val orTerm = OrTerm(searchTerms)

            val messages = folder.search(orTerm)
            val emails = messages.map { message ->
                parseMessage(message)
            }

            Result.success(emails)
        } catch (e: Exception) {
            Result.failure(ImapException("Failed to search messages: ${e.message}", e))
        }
    }

    /**
     * Mark message as read/unread
     */
    suspend fun markAsRead(messageId: String, read: Boolean): Result<Unit> = withContext(
        Dispatchers.IO
    ) {
        try {
            val folder = currentFolder ?: return@withContext Result.failure(
                ImapException("No folder opened")
            )

            if (folder.mode == Folder.READ_ONLY) {
                folder.close()
                folder.open(Folder.READ_WRITE)
            }

            // Find message by Message-ID header
            val searchTerm = HeaderTerm("Message-ID", messageId)
            val messages = folder.search(searchTerm)

            messages.forEach { message ->
                message.setFlag(Flags.Flag.SEEN, read)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ImapException("Failed to mark message: ${e.message}", e))
        }
    }

    /**
     * Delete message
     */
    suspend fun deleteMessage(messageId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val folder = currentFolder ?: return@withContext Result.failure(
                ImapException("No folder opened")
            )

            if (folder.mode == Folder.READ_ONLY) {
                folder.close()
                folder.open(Folder.READ_WRITE)
            }

            // Find message by Message-ID header
            val searchTerm = HeaderTerm("Message-ID", messageId)
            val messages = folder.search(searchTerm)

            messages.forEach { message ->
                message.setFlag(Flags.Flag.DELETED, true)
            }

            folder.expunge()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ImapException("Failed to delete message: ${e.message}", e))
        }
    }

    /**
     * Get message by ID
     */
    suspend fun getMessageById(messageId: String): Result<Email?> = withContext(Dispatchers.IO) {
        try {
            val folder = currentFolder ?: return@withContext Result.failure(
                ImapException("No folder opened")
            )

            // Find message by Message-ID header
            val searchTerm = HeaderTerm("Message-ID", messageId)
            val messages = folder.search(searchTerm)

            val email = messages.firstOrNull()?.let { message ->
                parseMessage(message)
            }

            Result.success(email)
        } catch (e: Exception) {
            Result.failure(ImapException("Failed to get message: ${e.message}", e))
        }
    }

    /**
     * Parse a JavaMail Message into our Email domain model
     */
    private fun parseMessage(message: Message): Email {
        println("🔍 [PARSE_MESSAGE] Starting to parse message...")
        println("🔍 [PARSE_MESSAGE] Message basic info:")
        println("  - Message number: ${message.messageNumber}")
        println("  - Subject: ${message.subject}")
        println("  - From: ${message.from?.firstOrNull()}")
        println("  - To: ${message.getRecipients(Message.RecipientType.TO)?.firstOrNull()}")
        println("  - Date: ${message.sentDate}")
        println("  - Content type: ${message.contentType}")
        println("  - Size: ${message.size}")
        println("  - Flags: ${message.flags}")
        println("  - All flags: ${message.flags.systemFlags.toList()}")
        println("  - User flags: ${message.flags.userFlags.toList()}")
        println("  - Is FLAGGED: ${message.isSet(Flags.Flag.FLAGGED)}")
        println("  - Is SEEN: ${message.isSet(Flags.Flag.SEEN)}")
        println("  - Is DRAFT: ${message.isSet(Flags.Flag.DRAFT)}")
        
        val messageId = message.getHeader("Message-ID")?.firstOrNull() ?: UUID.randomUUID().toString()
        println("🔍 [PARSE_MESSAGE] Message ID: $messageId")

        // Extract content with better error handling
        val bodyText = try {
            extractTextContent(message)
        } catch (e: Exception) {
            println("⚠️ [PARSE_MESSAGE] Failed to extract text content: ${e.message}")
            null
        }

        val (originalHtmlBody, bodyHtml) = try {
            extractHtmlContentWithParsing(message)
        } catch (e: Exception) {
            println("⚠️ [PARSE_MESSAGE] Failed to extract HTML content: ${e.message}")
            Pair(null, null)
        }

        // Log content extraction results
        println("📧 [PARSE_MESSAGE] Content extraction results:")
        println("  - Text content length: ${bodyText?.length ?: 0}")
        println("  - HTML content length: ${bodyHtml?.length ?: 0}")
        println("  - Original HTML length: ${originalHtmlBody?.length ?: 0}")
        println("  - Text preview: ${bodyText?.take(100) ?: "No text content"}")
        println("  - HTML preview: ${bodyHtml?.take(100) ?: "No HTML content"}")
        println("  - Original HTML preview: ${originalHtmlBody?.take(100) ?: "No original HTML"}")

        return Email(
            id = messageId,
            accountId = "", // Will be set by repository
            folderId = "", // Will be set by repository
            messageId = messageId,
            subject = decodeMimeText(message.subject ?: ""),
            fromAddress = extractEmailAddress(message.from?.firstOrNull()?.toString() ?: ""),
            fromName = extractDisplayName(message.from?.firstOrNull()?.toString() ?: ""),
            toAddresses = message.getRecipients(Message.RecipientType.TO)
                ?.mapNotNull { extractEmailAddress(it.toString()) } ?: emptyList(),
            ccAddresses = message.getRecipients(Message.RecipientType.CC)
                ?.mapNotNull { extractEmailAddress(it.toString()) } ?: emptyList(),
            bccAddresses = message.getRecipients(Message.RecipientType.BCC)
                ?.mapNotNull { extractEmailAddress(it.toString()) } ?: emptyList(),
            replyToAddress = message.replyTo?.firstOrNull()?.let { extractEmailAddress(it.toString()) },
            receivedDate = message.receivedDate?.time ?: System.currentTimeMillis(),
            sentDate = message.sentDate?.time ?: System.currentTimeMillis(),
            bodyText = bodyText,
            bodyHtml = bodyHtml,
            originalHtmlBody = originalHtmlBody,
            isRead = message.isSet(Flags.Flag.SEEN),
            isStarred = message.isSet(Flags.Flag.FLAGGED),
            isDraft = message.isSet(Flags.Flag.DRAFT),
            priority = mapPriority(message),
            hasAttachments = hasAttachments(message),
            size = message.size.toLong(),
            messageNumber = message.messageNumber,
            uid = getUid(message)?.toLongOrNull() ?: 0L,
            inReplyTo = message.getHeader("In-Reply-To")?.firstOrNull(),
            references = message.getHeader("References")?.firstOrNull(),
            threadId = generateThreadId(message),
            labels = extractLabels(message),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Extract charset from content type string
     */
    private fun extractCharsetFromContentType(contentType: String?): String? {
        if (contentType == null) return null
        
        val charsetPattern = Regex("charset=([^;\\s]+)", RegexOption.IGNORE_CASE)
        val match = charsetPattern.find(contentType)
        return match?.groupValues?.get(1)?.trim('"', '\'')
    }

    /**
     * Extract text content from message
     */
    private fun extractTextContent(message: Message): String? {
        return try {
            println("🔍 [EXTRACT_TEXT] Starting text content extraction...")
            println("🔍 [EXTRACT_TEXT] Message content type: ${message.contentType}")
            println("🔍 [EXTRACT_TEXT] Message content class: ${message.content?.javaClass?.simpleName}")
            println("🔍 [EXTRACT_TEXT] Message is MimeMessage: ${message is javax.mail.internet.MimeMessage}")
            println("🔍 [EXTRACT_TEXT] Message size: ${message.size}")
            
            // Log all headers for debugging
            try {
                val headers = message.allHeaders
                var headerCount = 0
                val headerList = mutableListOf<javax.mail.Header>()
                while (headers.hasMoreElements()) {
                    val header = headers.nextElement()
                    headerList.add(header)
                    headerCount++
                }
                println("🔍 [EXTRACT_TEXT] Message headers count: $headerCount")
                headerList.take(5).forEach { header ->
                    println("🔍 [EXTRACT_TEXT] Header: ${header.name} = ${header.value}")
                }
            } catch (e: Exception) {
                println("⚠️ [EXTRACT_TEXT] Failed to get headers: ${e.message}")
            }
            
            when (val content = message.content) {
                is String -> {
                    println("🔍 [EXTRACT_TEXT] Content is String, length: ${content.length}")
                    try {
                        // Try to decode if it's encoded
                        if (content.startsWith("=?")) {
                            val decoded = MimeUtility.decodeText(content)
                            println("🔍 [EXTRACT_TEXT] Decoded content length: ${decoded.length}")
                            decoded
                        } else {
                            println("🔍 [EXTRACT_TEXT] Content not encoded, using as-is")
                            content
                        }
                    } catch (e: Exception) {
                        println("⚠️ [EXTRACT_TEXT] Failed to decode text content: ${e.message}")
                        content // Return as-is if decoding fails
                    }
                }
                is MimeMultipart -> {
                    println("🔍 [EXTRACT_TEXT] Content is MimeMultipart, parts: ${content.count}")
                    var textContent: String? = null
                    
                    // Prioritize finding text/plain part
                    for (i in 0 until content.count) {
                        val part = content.getBodyPart(i)
                        println("🔍 [EXTRACT_TEXT] Part $i: ${part.contentType}")
                        
                        if (part.isMimeType("text/plain")) {
                            println("🔍 [EXTRACT_TEXT] Found text/plain part")
                            val partContent = part.content
                            println("🔍 [EXTRACT_TEXT] Part content class: ${partContent?.javaClass?.simpleName}")
                            
                            textContent = when (partContent) {
                                is String -> {
                                    println("🔍 [EXTRACT_TEXT] Part content is String, length: ${partContent.length}")
                                    // Handle encoding for multipart text content
                                    try {
                                        if (partContent.startsWith("=?")) {
                                            val decoded = MimeUtility.decodeText(partContent)
                                            println("🔍 [EXTRACT_TEXT] Decoded multipart text length: ${decoded.length}")
                                            decoded
                                        } else {
                                            partContent
                                        }
                                    } catch (e: Exception) {
                                        println("⚠️ [EXTRACT_TEXT] Failed to decode multipart text: ${e.message}")
                                        partContent
                                    }
                                }
                                is InputStream -> {
                                    println("🔍 [EXTRACT_TEXT] Part content is InputStream")
                                    // Handle InputStream content with multiple charset attempts
                                    try {
                                        // Extract charset from content type
                                        val contentType = part.contentType
                                        val charset = extractCharsetFromContentType(contentType) ?: "UTF-8"
                                        val text = partContent.bufferedReader(java.nio.charset.Charset.forName(charset)).use { it.readText() }
                                        println("🔍 [EXTRACT_TEXT] Read InputStream content length: ${text.length}")
                                        text
                                    } catch (e: Exception) {
                                        println("⚠️ [EXTRACT_TEXT] Failed to read InputStream with charset: ${e.message}")
                                        // Try other common encodings
                                        try {
                                            val text = partContent.bufferedReader(java.nio.charset.Charset.forName("GBK")).use { it.readText() }
                                            println("🔍 [EXTRACT_TEXT] Read InputStream with GBK encoding, length: ${text.length}")
                                            text
                                        } catch (e2: Exception) {
                                            println("⚠️ [EXTRACT_TEXT] Failed to read InputStream with GBK: ${e2.message}")
                                            null
                                        }
                                    }
                                }
                                else -> {
                                    println("ℹ️ [EXTRACT_TEXT] Unknown part content type: ${partContent?.javaClass?.simpleName}")
                                    partContent?.toString()
                                }
                            }
                            break // Stop when text/plain is found
                        }
                    }
                    
                    // If text/plain not found, try text/html
                    if (textContent == null) {
                        for (i in 0 until content.count) {
                            val part = content.getBodyPart(i)
                            if (part.isMimeType("text/html")) {
                                println("🔍 [EXTRACT_TEXT] No text/plain found, using text/html part")
                                val partContent = part.content
                                textContent = when (partContent) {
                                    is String -> {
                                        // Use advanced HTML parser
                                        val charset = extractCharsetFromContentType(part.contentType)
                                        AdvancedHtmlParser.extractTextFromHtml(partContent, charset)
                                    }
                                    is InputStream -> {
                                        try {
                                            val charset = extractCharsetFromContentType(part.contentType) ?: "UTF-8"
                                            val html = partContent.bufferedReader(java.nio.charset.Charset.forName(charset)).use { it.readText() }
                                            AdvancedHtmlParser.extractTextFromHtml(html, charset)
                                        } catch (e: Exception) {
                                            println("⚠️ [EXTRACT_TEXT] Failed to read HTML InputStream: ${e.message}")
                                            null
                                        }
                                    }
                                    else -> partContent?.toString()?.let { 
                                        val charset = extractCharsetFromContentType(part.contentType)
                                        AdvancedHtmlParser.extractTextFromHtml(it, charset)
                                    }
                                }
                                break
                            }
                        }
                    }
                    
                    if (textContent == null) {
                        println("ℹ️ [EXTRACT_TEXT] No text/plain or text/html part found in multipart")
                    }
                    
                    textContent
                }
                else -> {
                    println("ℹ️ [EXTRACT_TEXT] Unknown message content type: ${content?.javaClass?.simpleName}")
                    null
                }
            }
        } catch (e: Exception) {
            println("❌ [EXTRACT_TEXT] Failed to extract text content: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Extract HTML content from message
     */
    private fun extractHtmlContent(message: Message): String? {
        return try {
            println("🔍 [EXTRACT_HTML] Starting HTML content extraction...")
            println("🔍 [EXTRACT_HTML] Message content type: ${message.contentType}")
            println("🔍 [EXTRACT_HTML] Message content class: ${message.content?.javaClass?.simpleName}")
            
            when (val content = message.content) {
                is String -> {
                    println("🔍 [EXTRACT_HTML] Content is String, length: ${content.length}")
                    if (message.isMimeType("text/html")) {
                        println("🔍 [EXTRACT_HTML] Message is text/html type")
                        // Handle encoding for HTML content
                        try {
                            val finalText = if (content.startsWith("=?")) {
                                val decoded = MimeUtility.decodeText(content)
                                println("🔍 [EXTRACT_HTML] Decoded HTML content length: ${decoded.length}")
                                decoded
                            } else {
                                content
                            }
                            
                            // Save original HTML content while generating parsed text
                            val charset = extractCharsetFromContentType(message.contentType)
                            val parsedText = AdvancedHtmlParser.extractTextFromHtml(finalText, charset)
                            // Return original HTML, parsed text will be handled at call site
                            finalText
                        } catch (e: Exception) {
                            println("⚠️ [EXTRACT_HTML] Failed to decode HTML content: ${e.message}")
                            val charset = extractCharsetFromContentType(message.contentType)
                            AdvancedHtmlParser.extractTextFromHtml(content, charset)
                        }
                    } else {
                        println("ℹ️ [EXTRACT_HTML] Message is not text/html type")
                        null
                    }
                }
                is MimeMultipart -> {
                    println("🔍 [EXTRACT_HTML] Content is MimeMultipart, parts: ${content.count}")
                    for (i in 0 until content.count) {
                        val part = content.getBodyPart(i)
                        println("🔍 [EXTRACT_HTML] Part $i: ${part.contentType}")
                        if (part.isMimeType("text/html")) {
                            println("🔍 [EXTRACT_HTML] Found text/html part")
                            val partContent = part.content
                            println("🔍 [EXTRACT_HTML] Part content class: ${partContent?.javaClass?.simpleName}")
                            return when (partContent) {
                                is String -> {
                                    println("🔍 [EXTRACT_HTML] Part content is String, length: ${partContent.length}")
                                    try {
                                        val finalText = if (partContent.startsWith("=?")) {
                                            val decoded = MimeUtility.decodeText(partContent)
                                            println("🔍 [EXTRACT_HTML] Decoded multipart HTML length: ${decoded.length}")
                                            decoded
                                        } else {
                                            partContent
                                        }
                                        
                                        // Save original HTML content while generating parsed text
                                        val charset = extractCharsetFromContentType(part.contentType)
                                        val parsedText = AdvancedHtmlParser.extractTextFromHtml(finalText, charset)
                                        // Return original HTML, parsed text will be handled at call site
                                        finalText
                                    } catch (e: Exception) {
                                        println("⚠️ [EXTRACT_HTML] Failed to decode multipart HTML: ${e.message}")
                                        val charset = extractCharsetFromContentType(part.contentType)
                                        AdvancedHtmlParser.extractTextFromHtml(partContent, charset)
                                    }
                                }
                                is InputStream -> {
                                    println("🔍 [EXTRACT_HTML] Part content is InputStream")
                                    try {
                                        // Try multiple encoding methods
                                        val contentType = part.contentType
                                        val charset = try {
                                            if (contentType != null && contentType.contains("charset=")) {
                                                val charsetMatch = Regex("charset=([^;\\s]+)").find(contentType)
                                                charsetMatch?.groupValues?.get(1) ?: "UTF-8"
                                            } else {
                                                "UTF-8"
                                            }
                                        } catch (e: Exception) {
                                            "UTF-8"
                                        }
                                        println("🔍 [EXTRACT_HTML] Using charset: $charset")
                                        
                                        val text = try {
                                            partContent.bufferedReader(java.nio.charset.Charset.forName(charset)).use { it.readText() }
                                        } catch (e: Exception) {
                                            println("⚠️ [EXTRACT_HTML] Failed with charset $charset, trying UTF-8: ${e.message}")
                                            partContent.bufferedReader(java.nio.charset.StandardCharsets.UTF_8).use { it.readText() }
                                        }
                                        
                                        println("🔍 [EXTRACT_HTML] Read HTML InputStream content length: ${text.length}")
                                        println("🔍 [EXTRACT_HTML] HTML content preview: ${text.take(200)}")
                                        
                                        // Check if MIME encoding needs to be decoded
                                        val finalText = if (text.startsWith("=?")) {
                                            val decoded = MimeUtility.decodeText(text)
                                            println("🔍 [EXTRACT_HTML] Decoded MIME HTML content length: ${decoded.length}")
                                            decoded
                                        } else {
                                            text
                                        }
                                        
                                        // Save original HTML content while generating parsed text
                                        val parsedText = AdvancedHtmlParser.extractTextFromHtml(finalText, charset)
                                        // Return original HTML, parsed text will be handled at call site
                                        finalText
                                    } catch (e: Exception) {
                                        println("⚠️ [EXTRACT_HTML] Failed to read HTML InputStream: ${e.message}")
                                        null
                                    }
                                }
                                else -> {
                                    println("ℹ️ [EXTRACT_HTML] Unknown HTML part content type: ${partContent?.javaClass?.simpleName}")
                                    partContent?.toString()?.let { 
                                        val charset = extractCharsetFromContentType(part.contentType)
                                        AdvancedHtmlParser.extractTextFromHtml(it, charset)
                                    }
                                }
                            }
                        }
                    }
                    println("ℹ️ [EXTRACT_HTML] No text/html part found in multipart")
                    null
                }
                else -> {
                    println("ℹ️ [EXTRACT_HTML] Unknown message content type: ${content?.javaClass?.simpleName}")
                    null
                }
            }
        } catch (e: Exception) {
            println("❌ [EXTRACT_HTML] Failed to extract HTML content: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Extract HTML content from message and return both original HTML and parsed text
     */
    private fun extractHtmlContentWithParsing(message: Message): Pair<String?, String?> {
        return try {
            println("🔍 [EXTRACT_HTML_WITH_PARSING] Starting HTML content extraction with parsing...")
            println("🔍 [EXTRACT_HTML_WITH_PARSING] Message content type: ${message.contentType}")
            println("🔍 [EXTRACT_HTML_WITH_PARSING] Message content class: ${message.content?.javaClass?.simpleName}")
            
            when (val content = message.content) {
                is String -> {
                    println("🔍 [EXTRACT_HTML_WITH_PARSING] Content is String, length: ${content.length}")
                    if (message.isMimeType("text/html")) {
                        println("🔍 [EXTRACT_HTML_WITH_PARSING] Message is text/html type")
                        // Handle encoding for HTML content
                        try {
                            val finalText = if (content.startsWith("=?")) {
                                val decoded = MimeUtility.decodeText(content)
                                println("🔍 [EXTRACT_HTML_WITH_PARSING] Decoded HTML content length: ${decoded.length}")
                                decoded
                            } else {
                                content
                            }
                            
                            // Save original HTML content while generating parsed text
                            val charset = extractCharsetFromContentType(message.contentType)
                            val parsedText = AdvancedHtmlParser.extractTextFromHtml(finalText, charset)
                            println("🔍 [EXTRACT_HTML_WITH_PARSING] Original HTML length: ${finalText.length}")
                            println("🔍 [EXTRACT_HTML_WITH_PARSING] Parsed text length: ${parsedText?.length ?: 0}")
                            Pair(finalText, parsedText)
                        } catch (e: Exception) {
                            println("⚠️ [EXTRACT_HTML_WITH_PARSING] Failed to decode HTML content: ${e.message}")
                            val charset = extractCharsetFromContentType(message.contentType)
                            val parsedText = AdvancedHtmlParser.extractTextFromHtml(content, charset)
                            Pair(content, parsedText)
                        }
                    } else {
                        println("ℹ️ [EXTRACT_HTML_WITH_PARSING] Message is not text/html type")
                        Pair(null, null)
                    }
                }
                is MimeMultipart -> {
                    println("🔍 [EXTRACT_HTML_WITH_PARSING] Content is MimeMultipart, parts: ${content.count}")
                    for (i in 0 until content.count) {
                        val part = content.getBodyPart(i)
                        println("🔍 [EXTRACT_HTML_WITH_PARSING] Part $i: ${part.contentType}")
                        if (part.isMimeType("text/html")) {
                            println("🔍 [EXTRACT_HTML_WITH_PARSING] Found text/html part")
                            val partContent = part.content
                            println("🔍 [EXTRACT_HTML_WITH_PARSING] Part content class: ${partContent?.javaClass?.simpleName}")
                            return when (partContent) {
                                is String -> {
                                    println("🔍 [EXTRACT_HTML_WITH_PARSING] Part content is String, length: ${partContent.length}")
                                    try {
                                        val finalText = if (partContent.startsWith("=?")) {
                                            val decoded = MimeUtility.decodeText(partContent)
                                            println("🔍 [EXTRACT_HTML_WITH_PARSING] Decoded multipart HTML length: ${decoded.length}")
                                            decoded
                                        } else {
                                            partContent
                                        }
                                        
                                        // Save original HTML content while generating parsed text
                                        val charset = extractCharsetFromContentType(part.contentType)
                                        val parsedText = AdvancedHtmlParser.extractTextFromHtml(finalText, charset)
                                        println("🔍 [EXTRACT_HTML_WITH_PARSING] Original HTML length: ${finalText.length}")
                                        println("🔍 [EXTRACT_HTML_WITH_PARSING] Parsed text length: ${parsedText?.length ?: 0}")
                                        Pair(finalText, parsedText)
                                    } catch (e: Exception) {
                                        println("⚠️ [EXTRACT_HTML_WITH_PARSING] Failed to decode multipart HTML: ${e.message}")
                                        val charset = extractCharsetFromContentType(part.contentType)
                                        val parsedText = AdvancedHtmlParser.extractTextFromHtml(partContent, charset)
                                        Pair(partContent, parsedText)
                                    }
                                }
                                is InputStream -> {
                                    try {
                                        val charset = extractCharsetFromContentType(part.contentType) ?: "UTF-8"
                                        println("🔍 [EXTRACT_HTML_WITH_PARSING] Using charset: $charset")
                                        
                                        val text = try {
                                            partContent.bufferedReader(java.nio.charset.Charset.forName(charset)).use { it.readText() }
                                        } catch (e: Exception) {
                                            println("⚠️ [EXTRACT_HTML_WITH_PARSING] Failed with charset $charset, trying UTF-8: ${e.message}")
                                            partContent.bufferedReader(java.nio.charset.StandardCharsets.UTF_8).use { it.readText() }
                                        }
                                        
                                        println("🔍 [EXTRACT_HTML_WITH_PARSING] Read HTML InputStream content length: ${text.length}")
                                        println("🔍 [EXTRACT_HTML_WITH_PARSING] HTML content preview: ${text.take(200)}")
                                        
                                        // Check if MIME encoding needs to be decoded
                                        val finalText = if (text.startsWith("=?")) {
                                            val decoded = MimeUtility.decodeText(text)
                                            println("🔍 [EXTRACT_HTML_WITH_PARSING] Decoded MIME HTML content length: ${decoded.length}")
                                            decoded
                                        } else {
                                            text
                                        }
                                        
                                        // Save original HTML content while generating parsed text
                                        val parsedText = AdvancedHtmlParser.extractTextFromHtml(finalText, charset)
                                        println("🔍 [EXTRACT_HTML_WITH_PARSING] Original HTML length: ${finalText.length}")
                                        println("🔍 [EXTRACT_HTML_WITH_PARSING] Parsed text length: ${parsedText?.length ?: 0}")
                                        Pair(finalText, parsedText)
                                    } catch (e: Exception) {
                                        println("⚠️ [EXTRACT_HTML_WITH_PARSING] Failed to read HTML InputStream: ${e.message}")
                                        Pair(null, null)
                                    }
                                }
                                else -> {
                                    println("ℹ️ [EXTRACT_HTML_WITH_PARSING] Unknown part content type: ${partContent?.javaClass?.simpleName}")
                                    partContent?.toString()?.let { 
                                        val charset = extractCharsetFromContentType(part.contentType)
                                        val parsedText = AdvancedHtmlParser.extractTextFromHtml(it, charset)
                                        Pair(it, parsedText)
                                    } ?: Pair(null, null)
                                }
                            }
                        }
                    }
                    println("ℹ️ [EXTRACT_HTML_WITH_PARSING] No text/html part found in multipart")
                    Pair(null, null)
                }
                else -> {
                    println("ℹ️ [EXTRACT_HTML_WITH_PARSING] Unknown message content type: ${content?.javaClass?.simpleName}")
                    Pair(null, null)
                }
            }
        } catch (e: Exception) {
            println("❌ [EXTRACT_HTML_WITH_PARSING] Failed to extract HTML content: ${e.message}")
            e.printStackTrace()
            Pair(null, null)
        }
    }

    /**
     * Check if message has attachments
     */
    private fun hasAttachments(message: Message): Boolean {
        return try {
            when (val content = message.content) {
                is MimeMultipart -> {
                    for (i in 0 until content.count) {
                        val part = content.getBodyPart(i)
                        if (Part.ATTACHMENT.equals(part.disposition, ignoreCase = true)) {
                            return true
                        }
                    }
                    false
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get UID from message
     */
    private fun getUid(message: Message): String? {
        return try {
            val folder = message.folder
            if (folder is com.sun.mail.imap.IMAPFolder) {
                folder.getUID(message).toString()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Map folder type from JavaMail folder
     */
    private fun mapFolderType(folder: Folder): FolderType {
        val folderName = folder.name.lowercase()
        return when {
            folderName.contains("inbox") -> FolderType.INBOX
            folderName.contains("sent") -> FolderType.SENT
            folderName.contains("draft") -> FolderType.DRAFTS
            folderName.contains("trash") || folderName.contains("delete") -> FolderType.TRASH
            folderName.contains("spam") || folderName.contains("junk") -> FolderType.SPAM
            folderName.contains("archive") -> FolderType.ARCHIVE
            else -> FolderType.CUSTOM
        }
    }

    /**
     * Map message priority
     */
    private fun mapPriority(message: Message): EmailPriority {
        return try {
            val priority = message.getHeader("X-Priority")?.firstOrNull()
                ?: message.getHeader("Priority")?.firstOrNull()
                ?: message.getHeader("Importance")?.firstOrNull()

            when (priority?.lowercase()) {
                "1", "high", "urgent" -> EmailPriority.HIGH
                "5", "low" -> EmailPriority.LOW
                else -> EmailPriority.NORMAL
            }
        } catch (e: Exception) {
            EmailPriority.NORMAL
        }
    }

    /**
     * Generate thread ID from message
     */
    private fun generateThreadId(message: Message): String? {
        return try {
            // Use Message-ID, In-Reply-To, or References to determine thread
            val messageId = message.getHeader("Message-ID")?.firstOrNull()
            val inReplyTo = message.getHeader("In-Reply-To")?.firstOrNull()
            val references = message.getHeader("References")?.firstOrNull()

            inReplyTo ?: references?.split("\\s+".toRegex())?.firstOrNull() ?: messageId
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract labels from message (Gmail-specific)
     */
    private fun extractLabels(message: Message): List<String> {
        return try {
            val labels = message.getHeader("X-Gmail-Labels")?.firstOrNull()
            labels?.split(",")?.map { it.trim() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Move email to archive folder
     */
    suspend fun moveEmailToArchive(messageId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val store = this@ImapClient.store ?: return@withContext Result.failure(
                ImapException("Not connected to server")
            )

            // Find archive folder
            val folders = store.defaultFolder.list("*")
            val archiveFolder = folders.find { folder ->
                folder.name.lowercase().contains("archive") ||
                    folder.name.lowercase().contains("all mail")
            } ?: return@withContext Result.failure(
                ImapException("Archive folder not found")
            )

            // Move email to archive
            moveEmailToFolder(messageId, archiveFolder.fullName)
        } catch (e: Exception) {
            Result.failure(ImapException("Failed to archive email: ${e.message}", e))
        }
    }

    /**
     * Move email to spam folder
     */
    suspend fun moveEmailToSpam(messageId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val store = this@ImapClient.store ?: return@withContext Result.failure(
                ImapException("Not connected to server")
            )

            // Find spam folder
            val folders = store.defaultFolder.list("*")
            val spamFolder = folders.find { folder ->
                folder.name.lowercase().contains("spam") ||
                    folder.name.lowercase().contains("junk")
            } ?: return@withContext Result.failure(
                ImapException("Spam folder not found")
            )

            // Move email to spam
            moveEmailToFolder(messageId, spamFolder.fullName)
        } catch (e: Exception) {
            Result.failure(ImapException("Failed to mark as spam: ${e.message}", e))
        }
    }

    /**
     * Move email to specific folder
     */
    private suspend fun moveEmailToFolder(messageId: String, targetFolderName: String): Result<Unit> = withContext(
        Dispatchers.IO
    ) {
        try {
            val store = this@ImapClient.store ?: return@withContext Result.failure(
                ImapException("Not connected to server")
            )

            // Open current folder (assuming INBOX for now)
            val sourceFolder = store.getFolder("INBOX")
            if (!sourceFolder.isOpen) {
                sourceFolder.open(Folder.READ_WRITE)
            }

            // Find message by Message-ID
            val messages = sourceFolder.search(
                HeaderTerm("Message-ID", messageId)
            )

            if (messages.isEmpty()) {
                return@withContext Result.failure(
                    ImapException("Email not found")
                )
            }

            // Open target folder
            val targetFolder = store.getFolder(targetFolderName)
            if (!targetFolder.exists()) {
                return@withContext Result.failure(
                    ImapException("Target folder does not exist")
                )
            }

            if (!targetFolder.isOpen) {
                targetFolder.open(Folder.READ_WRITE)
            }

            // Copy message to target folder
            sourceFolder.copyMessages(messages, targetFolder)

            // Mark original as deleted
            messages.forEach { message ->
                message.setFlag(Flags.Flag.DELETED, true)
            }

            // Expunge to permanently delete
            sourceFolder.expunge()

            // Close folders
            targetFolder.close(false)
            sourceFolder.close(false)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ImapException("Failed to move email: ${e.message}", e))
        }
    }

    /**
     * Check if connected
     */
    fun isConnected(): Boolean {
        return store?.isConnected == true
    }
    
    /**
     * Start IDLE monitoring for real-time email updates
     */
    fun startIdleMonitoring(folderName: String = "INBOX"): Flow<EmailUpdate> = flow {
        try {
            val imapStore = store as? IMAPStore ?: throw ImapException("Not connected to IMAP server")
            val folder = imapStore.getFolder(folderName) as? IMAPFolder 
                ?: throw ImapException("Folder $folderName not found or not IMAP folder")
            
            if (!folder.isOpen) {
                folder.open(Folder.READ_ONLY)
            }
            
            isIdleEnabled = true
            
            while (isIdleEnabled) {
                try {
                    // Start IDLE
                    folder.idle(true)
                    
                    // Check for new messages
                    val messageCount = folder.messageCount
                    val unreadCount = folder.unreadMessageCount
                    
                    // Emit update
                    emit(EmailUpdate(
                        folderName = folderName,
                        messageCount = messageCount,
                        unreadCount = unreadCount,
                        timestamp = System.currentTimeMillis()
                    ))
                    
                    // Small delay to prevent excessive updates
                    delay(1000)
                    
                } catch (e: Exception) {
                    if (isIdleEnabled) {
                        emit(EmailUpdate(
                            folderName = folderName,
                            error = e.message,
                            timestamp = System.currentTimeMillis()
                        ))
                    }
                    
                    // Wait before retrying
                    delay(5000)
                }
            }
            
        } catch (e: Exception) {
            emit(EmailUpdate(
                folderName = folderName,
                error = e.message,
                timestamp = System.currentTimeMillis()
            ))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Stop IDLE monitoring
     */
    fun stopIdleMonitoring() {
        isIdleEnabled = false
        idleJob?.cancel()
        idleJob = null
    }
    
    /**
     * Get new messages since last sync
     */
    suspend fun getNewMessages(sinceUid: Long, folderName: String = "INBOX"): Result<List<Email>> = withContext(Dispatchers.IO) {
        try {
            val imapStore = store as? IMAPStore ?: return@withContext Result.failure(
                ImapException("Not connected to IMAP server")
            )
            
            val folder = imapStore.getFolder(folderName) as? IMAPFolder 
                ?: return@withContext Result.failure(
                    ImapException("Folder $folderName not found")
                )
            
            if (!folder.isOpen) {
                folder.open(Folder.READ_ONLY)
            }
            
            // Search for messages with UID greater than sinceUid
            // Note: UIDTerm is not available in standard JavaMail, using alternative approach
            // For now, get all messages and filter by UID
            val allMessages = folder.messages
            val messages = allMessages.filter { message ->
                try {
                    val uid = folder.getUID(message)
                    uid > sinceUid
                } catch (e: Exception) {
                    false
                }
            }.toTypedArray()
            
            val emails = messages.map { message ->
                parseMessage(message)
            }
            
            Result.success(emails)
        } catch (e: Exception) {
            Result.failure(ImapException("Failed to get new messages: ${e.message}", e))
        }
    }
    
    /**
     * Get highest UID in folder
     */
    suspend fun getHighestUid(folderName: String = "INBOX"): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val imapStore = store as? IMAPStore ?: return@withContext Result.failure(
                ImapException("Not connected to IMAP server")
            )
            
            val folder = imapStore.getFolder(folderName) as? IMAPFolder 
                ?: return@withContext Result.failure(
                    ImapException("Folder $folderName not found")
                )
            
            if (!folder.isOpen) {
                folder.open(Folder.READ_ONLY)
            }
            
            val messageCount = folder.messageCount
            if (messageCount == 0) {
                return@withContext Result.success(0L)
            }
            
            val lastMessage = folder.getMessage(messageCount)
            val uid = folder.getUID(lastMessage)
            
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(ImapException("Failed to get highest UID: ${e.message}", e))
        }
    }
    
    /**
     * Set IDLE timeout
     */
    fun setIdleTimeout(timeoutMs: Long) {
        idleTimeout = timeoutMs
    }
    
    /**
     * Check if IDLE is enabled
     */
    fun isIdleEnabled(): Boolean = isIdleEnabled
    
    /**
     * Get detailed information about a specific folder
     */
    suspend fun getFolderInfo(folderName: String): Result<EmailFolder> = withContext(Dispatchers.IO) {
        try {
            val folder = store?.getFolder(folderName) ?: return@withContext Result.failure(
                ImapException("Store not connected")
            )
            
            if (!folder.exists()) {
                return@withContext Result.failure(
                    ImapException("Folder does not exist: $folderName")
                )
            }
            
            folder.open(Folder.READ_ONLY)
            
            val messageCount = folder.messageCount
            val unreadCount = folder.unreadMessageCount
            val isSubscribed = folder.isSubscribed
            
            folder.close(false)
            
            val emailFolder = EmailFolder(
                id = generateFolderId(folderName),
                accountId = "", // Will be set by caller
                name = folder.name,
                displayName = folder.name,
                fullName = folder.fullName,
                type = determineFolderType(folderName),
                messageCount = messageCount,
                unreadCount = unreadCount,
                isSubscribed = isSubscribed,
                parentId = null,
                syncState = SyncState.SYNCED,
                lastSyncTime = System.currentTimeMillis()
            )
            
            Result.success(emailFolder)
        } catch (e: Exception) {
            Result.failure(ImapException("Failed to get folder info: ${e.message}", e))
        }
    }
    
    /**
     * Generate a unique folder ID
     */
    private fun generateFolderId(folderName: String): String {
        return "folder_${folderName.hashCode()}_${System.currentTimeMillis()}"
    }
    
    /**
     * Determine folder type based on folder name
     */
    private fun determineFolderType(folderName: String): FolderType {
        return when (folderName.uppercase()) {
            "INBOX" -> FolderType.INBOX
            "SENT", "SENT ITEMS", "SENT MESSAGES" -> FolderType.SENT
            "DRAFTS", "DRAFT" -> FolderType.DRAFTS
            "TRASH", "DELETED ITEMS", "BIN" -> FolderType.TRASH
            "SPAM", "JUNK", "JUNK EMAIL" -> FolderType.SPAM
            "ARCHIVE", "ARCHIVED" -> FolderType.ARCHIVE
            else -> FolderType.CUSTOM
        }
    }
}

/**
 * Custom exception for IMAP operations
 */
class ImapException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Data class for email updates from IDLE monitoring
 */
data class EmailUpdate(
    val folderName: String,
    val messageCount: Int = 0,
    val unreadCount: Int = 0,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Decode MIME encoded text
 */
private fun decodeMimeText(encodedText: String): String {
    return try {
        if (encodedText.startsWith("=?")) {
            MimeUtility.decodeText(encodedText)
        } else {
            encodedText
        }
    } catch (e: Exception) {
        println("⚠️ Failed to decode MIME text: ${e.message}")
        encodedText
    }
}

/**
 * Extract email address from email address string
 * Handle formats like: "=?UTF-8?B?Z29mb3J3YXJkZXI=?= <goforwarder@yeah.net>"
 */
private fun extractEmailAddress(addressString: String): String {
    return try {
        // If contains < > brackets, extract email address inside brackets
        val emailPattern = Regex("<([^>]+)>")
        val matchResult = emailPattern.find(addressString)
        if (matchResult != null) {
            matchResult.groupValues[1].trim()
        } else {
            // If no brackets, check if it's directly an email address format
            val directEmailPattern = Regex("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})")
            val directMatch = directEmailPattern.find(addressString)
            directMatch?.groupValues[1] ?: addressString.trim()
        }
    } catch (e: Exception) {
        println("⚠️ Failed to extract email address from: $addressString, error: ${e.message}")
        addressString.trim()
    }
}

/**
 * Extract display name from email address string
 * Handle formats like: "=?UTF-8?B?Z29mb3J3YXJkZXI=?= <goforwarder@yeah.net>"
 */
private fun extractDisplayName(addressString: String): String {
    return try {
        // If contains < > brackets, extract display name before brackets
        val emailPattern = Regex("^(.+?)\\s*<[^>]+>$")
        val matchResult = emailPattern.find(addressString)
        if (matchResult != null) {
            val displayName = matchResult.groupValues[1].trim()
            // Decode MIME encoded display name
            decodeMimeText(displayName)
        } else {
            // If no brackets, check if it's directly an email address format
            val directEmailPattern = Regex("^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$")
            if (directEmailPattern.matches(addressString)) {
                // If it's a pure email address, return empty string
                ""
            } else {
                // Otherwise decode the entire string
                decodeMimeText(addressString)
            }
        }
    } catch (e: Exception) {
        println("⚠️ Failed to extract display name from: $addressString, error: ${e.message}")
        ""
    }
}

/**
 * Unified HTML content cleaning function
 * Used to thoroughly clean HTML content, remove all HTML code
 */



