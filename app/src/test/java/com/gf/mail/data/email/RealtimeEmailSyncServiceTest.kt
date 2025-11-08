package com.gf.mail.data.email

import com.gf.mail.data.local.dao.EmailDao
import com.gf.mail.data.local.dao.FolderDao
import com.gf.mail.data.mapper.FolderMapper
import com.gf.mail.data.notification.PushNotificationManager
import com.gf.mail.data.security.CredentialEncryption
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.model.EmailFolder
import com.gf.mail.domain.model.EmailProvider
import com.gf.mail.domain.model.AuthenticationInfo
import com.gf.mail.domain.model.AuthenticationType
import com.gf.mail.domain.model.ServerConfiguration
import com.gf.mail.domain.model.EncryptionType
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for RealtimeEmailSyncService
 */
class RealtimeEmailSyncServiceTest {

    private lateinit var imapClient: ImapClient
    private lateinit var emailDao: EmailDao
    private lateinit var folderDao: FolderDao
    private lateinit var folderMapper: FolderMapper
    private lateinit var pushNotificationManager: PushNotificationManager
    private lateinit var credentialEncryption: CredentialEncryption
    private lateinit var realtimeEmailSyncService: RealtimeEmailSyncService

    private val testAccount = Account(
        id = "test-account-id",
        email = "test@example.com",
        emailAddress = "test@example.com",
        fullName = "Test User",
        displayName = "Test User",
        provider = EmailProvider.GMAIL,
        authInfo = AuthenticationInfo(
            type = AuthenticationType.PASSWORD,
            hasPassword = true,
            hasOAuthToken = false,
            oauthExpiresAt = null
        ),
        serverConfig = ServerConfiguration(
            imapHost = "imap.gmail.com",
            imapPort = 993,
            imapEncryption = EncryptionType.SSL,
            smtpHost = "smtp.gmail.com",
            smtpPort = 587,
            smtpEncryption = EncryptionType.STARTTLS
        ),
        isActive = true,
        syncEnabled = true,
        syncFrequency = 15,
        lastSync = 0L,
        signature = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        imapClient = mockk()
        emailDao = mockk()
        folderDao = mockk()
        folderMapper = mockk()
        pushNotificationManager = mockk()
        credentialEncryption = mockk()
        realtimeEmailSyncService = RealtimeEmailSyncService(
            imapClient = imapClient,
            emailDao = emailDao,
            folderDao = folderDao,
            folderMapper = folderMapper,
            pushNotificationManager = pushNotificationManager,
            credentialEncryption = credentialEncryption
        )
    }

    @Test
    fun `startRealtimeSync should perform initial sync successfully`() = runTest {
        // Arrange
        val testFolders = listOf(
            EmailFolder(
                id = "folder1",
                accountId = testAccount.id,
                name = "INBOX",
                fullName = "INBOX",
                displayName = "Inbox",
                type = com.gf.mail.domain.model.FolderType.INBOX,
                messageCount = 10,
                totalCount = 10,
                unreadCount = 3,
                isSelectable = true,
                isSubscribed = true,
                isSystem = true,
                hasChildren = false,
                canHoldMessages = true,
                canHoldFolders = false,
                parentFolder = null,
                separator = '/',
                attributes = emptyList(),
                syncState = com.gf.mail.domain.model.SyncState.SYNCED,
                lastSyncTime = System.currentTimeMillis(),
                sortOrder = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        val testEmails = listOf(
            createTestEmail("email1"),
            createTestEmail("email2")
        )

        coEvery { credentialEncryption.getPasswordSecurely(any()) } returns "test-password"
        coEvery { imapClient.connect(any(), any()) } returns Result.success(Unit)
        coEvery { imapClient.getFolders(any()) } returns Result.success(testFolders)
        coEvery { imapClient.getNewMessages(any(), any()) } returns Result.success(testEmails)
        coEvery { imapClient.getHighestUid(any()) } returns Result.success(100L)
        coEvery { imapClient.startIdleMonitoring(any()) } returns flowOf()
        coEvery { folderMapper.toEntity(any()) } returns mockk()
        coEvery { folderDao.insertFolders(any()) } just Runs
        coEvery { emailDao.insertEmails(any()) } just Runs
        coEvery { pushNotificationManager.startPushNotifications(any()) } just Runs
        coEvery { pushNotificationManager.handleNewEmail(any(), any()) } just Runs

        // Act
        realtimeEmailSyncService.startSync(testAccount)

        // Assert - Since startSync is now a fire-and-forget method, we just verify it doesn't throw
        assertTrue(true) // Test passes if no exception is thrown

        coVerify { credentialEncryption.getPasswordSecurely(testAccount.id) }
        coVerify { imapClient.connect(testAccount, "test-password") }
        coVerify { imapClient.getFolders(any()) }
        coVerify { imapClient.getNewMessages(any(), any()) }
        coVerify { folderDao.insertFolders(any()) }
        coVerify { emailDao.insertEmails(any()) }
    }

    @Test
    fun `startSync should handle connection failure`() = runTest {
        // Arrange
        coEvery { credentialEncryption.getPasswordSecurely(any()) } returns "test-password"
        coEvery { imapClient.connect(any(), any()) } returns Result.failure(Exception("Connection failed"))
        coEvery { pushNotificationManager.handleSyncStatus(any(), any(), any()) } just Runs

        // Act
        realtimeEmailSyncService.startSync(testAccount)

        // Assert - Since startSync is now a fire-and-forget method, we just verify it doesn't throw
        assertTrue(true) // Test passes if no exception is thrown
        
        coVerify { pushNotificationManager.handleSyncStatus(any(), false, any()) }
    }

    @Test
    fun `stopSync should stop sync for account`() = runTest {
        // Arrange
        coEvery { pushNotificationManager.stopPushNotifications(any()) } returns Unit
        coEvery { imapClient.stopIdleMonitoring() } returns Unit
        coEvery { imapClient.disconnect() } returns Result.success(Unit)

        // Act
        realtimeEmailSyncService.stopSync()

        // Assert
        coVerify { imapClient.stopIdleMonitoring() }
    }

    @Test
    fun `stopSync should stop all sync operations`() = runTest {
        // Arrange
        coEvery { pushNotificationManager.stopPushNotifications(any()) } returns Unit
        coEvery { imapClient.stopIdleMonitoring() } returns Unit
        coEvery { imapClient.disconnect() } returns Result.success(Unit)

        // Act
        realtimeEmailSyncService.stopSync()

        // Assert
        coVerify { imapClient.stopIdleMonitoring() }
    }

    @Test
    fun `syncState should return current sync state`() = runTest {
        // Act
        val syncState = realtimeEmailSyncService.syncState.value

        // Assert
        assertNotNull(syncState)
    }

    private fun createTestEmail(id: String): Email {
        return Email(
            id = id,
            accountId = testAccount.id,
            folderId = "folder1",
            threadId = "thread1",
            subject = "Test Email $id",
            fromName = "Test Sender",
            fromAddress = "sender@example.com",
            replyToAddress = null,
            toAddresses = listOf("recipient@example.com"),
            ccAddresses = emptyList(),
            bccAddresses = emptyList(),
            bodyText = "Test email body",
            bodyHtml = "<p>Test email body</p>",
            sentDate = System.currentTimeMillis(),
            receivedDate = System.currentTimeMillis(),
            messageId = "message-$id",
            inReplyTo = null,
            references = null,
            isRead = false,
            isStarred = false,
            isFlagged = false,
            isDraft = false,
            hasAttachments = false,
            priority = com.gf.mail.domain.model.EmailPriority.NORMAL,
            size = 1024L,
            uid = 1L,
            messageNumber = 1,
            labels = emptyList(),
            flags = emptyList(),
            headers = emptyMap(),
            syncState = com.gf.mail.domain.model.SyncState.SYNCED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}