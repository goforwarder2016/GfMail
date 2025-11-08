package com.gf.mail.data.sync

import com.gf.mail.data.email.ImapClient
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.EmailFolder
import com.gf.mail.domain.model.EmailProvider
import com.gf.mail.domain.model.AuthenticationInfo
import com.gf.mail.domain.model.AuthenticationType
import com.gf.mail.domain.model.ServerConfiguration
import com.gf.mail.domain.model.EncryptionType
import com.gf.mail.domain.model.FolderType
import com.gf.mail.domain.model.SyncState
import com.gf.mail.domain.repository.FolderRepository
import com.gf.mail.data.local.dao.FolderDao
import com.gf.mail.data.mapper.FolderMapper
import com.gf.mail.data.sync.FolderSyncResult
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for FolderSyncService
 */
class FolderSyncServiceTest {

    private lateinit var imapClient: ImapClient
    private lateinit var folderRepository: FolderRepository
    private lateinit var folderDao: FolderDao
    private lateinit var folderMapper: FolderMapper
    private lateinit var folderSyncService: FolderSyncService

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
        folderRepository = mockk()
        folderDao = mockk()
        folderMapper = mockk()
        folderSyncService = FolderSyncService(folderRepository, folderDao, folderMapper, imapClient)
    }

    @Test
    fun `initialSync should perform full folder synchronization successfully`() = runTest {
        // Arrange
        val testFolders = listOf(
            createTestFolder("INBOX", FolderType.INBOX),
            createTestFolder("SENT", FolderType.SENT),
            createTestFolder("DRAFTS", FolderType.DRAFTS)
        )

        coEvery { imapClient.connect(any(), any()) } returns Result.success(Unit)
        coEvery { imapClient.getFolders(any()) } returns Result.success(testFolders)
        coEvery { folderRepository.getFoldersByAccount(any()) } returns emptyList()
        coEvery { folderRepository.insertFolders(any()) } just Runs
        coEvery { folderRepository.updateFolder(any()) } just Runs
        coEvery { imapClient.disconnect() } returns Result.success(Unit)

        // Act
        val result = folderSyncService.syncFoldersForAccount(testAccount, "test-password").first()

        // Assert
        assertTrue(result is FolderSyncResult.Success)
        coVerify { imapClient.connect(testAccount, "test-password") }
        coVerify { imapClient.getFolders(any()) }
        coVerify { folderRepository.insertFolders(any()) }
        coVerify { imapClient.disconnect() }
    }

    @Test
    fun `initialSync should handle connection failure`() = runTest {
        // Arrange
        coEvery { imapClient.connect(any(), any()) } returns Result.failure(Exception("Connection failed"))
        coEvery { imapClient.disconnect() } returns Result.success(Unit)

        // Act
        val result = folderSyncService.syncFoldersForAccount(testAccount, "test-password").first()

        // Assert
        assertTrue(result is FolderSyncResult.Error)
        assertEquals("Connection failed", (result as FolderSyncResult.Error).message)
        coVerify { imapClient.connect(testAccount, "test-password") }
    }

    @Test
    fun `incrementalSync should update existing folders and add new ones`() = runTest {
        // Arrange
        val serverFolders = listOf(
            createTestFolder("INBOX", FolderType.INBOX, messageCount = 15, unreadCount = 5),
            createTestFolder("SENT", FolderType.SENT, messageCount = 10, unreadCount = 0),
            createTestFolder("NEW_FOLDER", FolderType.CUSTOM, messageCount = 3, unreadCount = 1)
        )

        val localFolders = listOf(
            createTestFolder("INBOX", FolderType.INBOX, messageCount = 10, unreadCount = 3),
            createTestFolder("SENT", FolderType.SENT, messageCount = 8, unreadCount = 0)
        )

        coEvery { imapClient.connect(any(), any()) } returns Result.success(Unit)
        coEvery { imapClient.getFolders(any()) } returns Result.success(serverFolders)
        coEvery { folderRepository.getFoldersByAccount(any()) } returns localFolders
        coEvery { folderRepository.insertFolders(any()) } just Runs
        coEvery { folderRepository.updateFolder(any()) } just Runs
        coEvery { imapClient.disconnect() } returns Result.success(Unit)

        // Act
        val result = folderSyncService.syncFoldersForAccount(testAccount, "test-password").first()

        // Assert
        assertTrue(result is FolderSyncResult.Success)
        coVerify { folderRepository.insertFolders(any()) } // Should insert NEW_FOLDER
        coVerify { folderRepository.updateFolder(any()) } // Should update INBOX and SENT
    }

    @Test
    fun `incrementalSync should remove deleted folders`() = runTest {
        // Arrange
        val serverFolders = listOf(
            createTestFolder("INBOX", FolderType.INBOX)
        )

        val localFolders = listOf(
            createTestFolder("INBOX", FolderType.INBOX),
            createTestFolder("DELETED_FOLDER", FolderType.CUSTOM)
        )

        coEvery { imapClient.connect(any(), any()) } returns Result.success(Unit)
        coEvery { imapClient.getFolders(any()) } returns Result.success(serverFolders)
        coEvery { folderRepository.getFoldersByAccount(any()) } returns localFolders
        coEvery { folderRepository.insertFolders(any()) } just Runs
        coEvery { folderRepository.updateFolder(any()) } just Runs
        coEvery { folderRepository.deleteFolder(any()) } just Runs
        coEvery { imapClient.disconnect() } returns Result.success(Unit)

        // Act
        val result = folderSyncService.syncFoldersForAccount(testAccount, "test-password").first()

        // Assert
        assertTrue(result is FolderSyncResult.Success)
        coVerify { folderRepository.deleteFolder(any()) } // Should delete DELETED_FOLDER
    }

    @Test
    fun `syncFoldersForAccount should return flow of sync results`() = runTest {
        // Arrange
        val testFolders = listOf(
            createTestFolder("INBOX", FolderType.INBOX)
        )
        
        coEvery { imapClient.connect(any(), any()) } returns Result.success(Unit)
        coEvery { imapClient.getFolders(any()) } returns Result.success(testFolders)
        coEvery { folderRepository.getFoldersByAccount(any()) } returns emptyList()
        coEvery { folderRepository.insertFolders(any()) } just Runs
        coEvery { imapClient.disconnect() } returns Result.success(Unit)

        // Act
        val results = folderSyncService.syncFoldersForAccount(testAccount, "test-password")
        val resultList = mutableListOf<FolderSyncResult>()
        results.collect { resultList.add(it) }

        // Assert
        assertTrue(resultList.isNotEmpty())
        assertTrue(resultList.any { it is FolderSyncResult.Success })
    }

    private fun createTestFolder(
        name: String,
        type: FolderType,
        messageCount: Int = 0,
        unreadCount: Int = 0
    ): EmailFolder {
        return EmailFolder(
            id = "folder-$name",
            accountId = testAccount.id,
            name = name,
            fullName = name,
            displayName = name,
            type = type,
            messageCount = messageCount,
            totalCount = messageCount,
            unreadCount = unreadCount,
            isSelectable = true,
            isSubscribed = true,
            isSystem = type != FolderType.CUSTOM,
            hasChildren = false,
            canHoldMessages = true,
            canHoldFolders = false,
            parentFolder = null,
            separator = '/',
            attributes = emptyList(),
            syncState = SyncState.SYNCED,
            lastSyncTime = System.currentTimeMillis(),
            sortOrder = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}