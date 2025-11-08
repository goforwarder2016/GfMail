package com.gf.mail.integration

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gf.mail.data.local.dao.EmailDao
import com.gf.mail.data.local.dao.FolderDao
import com.gf.mail.data.local.database.GfmailDatabase
import com.gf.mail.data.local.entity.EmailEntity
import com.gf.mail.data.local.entity.FolderEntity
import com.gf.mail.domain.model.FolderType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Integration tests for email synchronization
 */
@RunWith(AndroidJUnit4::class)
class EmailSyncIntegrationTest {

    private lateinit var database: GfmailDatabase
    private lateinit var emailDao: EmailDao
    private lateinit var folderDao: FolderDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            GfmailDatabase::class.java
        ).allowMainThreadQueries().build()

        emailDao = database.emailDao()
        folderDao = database.folderDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveEmailsShouldWorkCorrectly() = runTest {
        // Arrange
        val testEmails = listOf(
            createTestEmailEntity("email1", "account1", "folder1"),
            createTestEmailEntity("email2", "account1", "folder1"),
            createTestEmailEntity("email3", "account2", "folder2")
        )

        // Act
        emailDao.insertEmails(testEmails)
        val retrievedEmails = emailDao.getAllEmails()

        // Assert
        assertEquals(3, retrievedEmails.size)
        assertTrue(retrievedEmails.any { it.id == "email1" })
        assertTrue(retrievedEmails.any { it.id == "email2" })
        assertTrue(retrievedEmails.any { it.id == "email3" })
    }

    @Test
    fun getEmailsByAccountShouldReturnCorrectEmails() = runTest {
        // Arrange
        val account1Emails = listOf(
            createTestEmailEntity("email1", "account1", "folder1"),
            createTestEmailEntity("email2", "account1", "folder1")
        )
        val account2Emails = listOf(
            createTestEmailEntity("email3", "account2", "folder2")
        )
        emailDao.insertEmails(account1Emails + account2Emails)

        // Act
        val account1RetrievedEmails = emailDao.getEmailsByAccount("account1")
        val account2RetrievedEmails = emailDao.getEmailsByAccount("account2")

        // Assert
        assertEquals(2, account1RetrievedEmails.size)
        assertEquals(1, account2RetrievedEmails.size)
        assertTrue(account1RetrievedEmails.all { it.accountId == "account1" })
        assertTrue(account2RetrievedEmails.all { it.accountId == "account2" })
    }

    @Test
    fun getEmailsByFolderShouldReturnCorrectEmails() = runTest {
        // Arrange
        val folder1Emails = listOf(
            createTestEmailEntity("email1", "account1", "folder1"),
            createTestEmailEntity("email2", "account1", "folder1")
        )
        val folder2Emails = listOf(
            createTestEmailEntity("email3", "account1", "folder2")
        )
        emailDao.insertEmails(folder1Emails + folder2Emails)

        // Act
        val folder1RetrievedEmails = emailDao.getEmailsInFolder("folder1", 50, 0)
        val folder2RetrievedEmails = emailDao.getEmailsInFolder("folder2", 50, 0)

        // Assert
        assertEquals(2, folder1RetrievedEmails.size)
        assertEquals(1, folder2RetrievedEmails.size)
        assertTrue(folder1RetrievedEmails.all { it.folderId == "folder1" })
        assertTrue(folder2RetrievedEmails.all { it.folderId == "folder2" })
    }

    @Test
    fun markEmailsAsReadShouldUpdateReadStatus() = runTest {
        // Arrange
        val testEmails = listOf(
            createTestEmailEntity("email1", "account1", "folder1", isRead = false),
            createTestEmailEntity("email2", "account1", "folder1", isRead = false)
        )
        emailDao.insertEmails(testEmails)

        // Act
        emailDao.markEmailsAsRead(listOf("email1", "email2"), true)
        val updatedEmails = emailDao.getAllEmails()

        // Assert
        assertEquals(2, updatedEmails.size)
        assertTrue(updatedEmails.all { it.isRead })
    }

    @Test
    fun insertAndRetrieveFoldersShouldWorkCorrectly() = runTest {
        // Arrange
        val testFolders = listOf(
            createTestFolderEntity("folder1", "account1", FolderType.INBOX),
            createTestFolderEntity("folder2", "account1", FolderType.SENT),
            createTestFolderEntity("folder3", "account2", FolderType.INBOX)
        )

        // Act
        folderDao.insertFolders(testFolders)
        val account1Folders = folderDao.getFoldersByAccount("account1")
        val account2Folders = folderDao.getFoldersByAccount("account2")
        val retrievedFolders = account1Folders + account2Folders

        // Assert
        assertEquals(3, retrievedFolders.size)
        assertTrue(retrievedFolders.any { folder -> folder.id == "folder1" })
        assertTrue(retrievedFolders.any { folder -> folder.id == "folder2" })
        assertTrue(retrievedFolders.any { folder -> folder.id == "folder3" })
    }

    @Test
    fun getFoldersByAccountShouldReturnCorrectFolders() = runTest {
        // Arrange
        val account1Folders = listOf(
            createTestFolderEntity("folder1", "account1", FolderType.INBOX),
            createTestFolderEntity("folder2", "account1", FolderType.SENT)
        )
        val account2Folders = listOf(
            createTestFolderEntity("folder3", "account2", FolderType.INBOX)
        )
        folderDao.insertFolders(account1Folders + account2Folders)

        // Act
        val account1RetrievedFolders = folderDao.getFoldersByAccount("account1")
        val account2RetrievedFolders = folderDao.getFoldersByAccount("account2")

        // Assert
        assertEquals(2, account1RetrievedFolders.size)
        assertEquals(1, account2RetrievedFolders.size)
        assertTrue(account1RetrievedFolders.all { it.accountId == "account1" })
        assertTrue(account2RetrievedFolders.all { it.accountId == "account2" })
    }

    @Test
    fun searchEmailsShouldReturnMatchingResults() = runTest {
        // Arrange
        val testEmails = listOf(
            createTestEmailEntity("email1", "account1", "folder1", subject = "Important meeting"),
            createTestEmailEntity("email2", "account1", "folder1", subject = "Lunch plans"),
            createTestEmailEntity("email3", "account1", "folder1", subject = "Important document")
        )
        emailDao.insertEmails(testEmails)

        // Act
        val searchResults = emailDao.searchEmails("important")

        // Assert
        assertEquals(2, searchResults.size)
        assertTrue(searchResults.any { it.id == "email1" })
        assertTrue(searchResults.any { it.id == "email3" })
    }

    @Test
    fun getUnreadCountShouldReturnCorrectCount() = runTest {
        // Arrange
        val testEmails = listOf(
            createTestEmailEntity("email1", "account1", "folder1", isRead = false),
            createTestEmailEntity("email2", "account1", "folder1", isRead = true),
            createTestEmailEntity("email3", "account1", "folder1", isRead = false)
        )
        emailDao.insertEmails(testEmails)

        // Act
        val unreadCount = emailDao.getUnreadCountByAccount("account1")

        // Assert
        assertEquals(2, unreadCount)
    }

    @Test
    fun getStarredEmailsShouldReturnOnlyStarredEmails() = runTest {
        // Arrange
        val testEmails = listOf(
            createTestEmailEntity("email1", "account1", "folder1", isStarred = true),
            createTestEmailEntity("email2", "account1", "folder1", isStarred = false),
            createTestEmailEntity("email3", "account1", "folder1", isStarred = true)
        )
        emailDao.insertEmails(testEmails)

        // Act
        val starredEmails = emailDao.getStarredEmailsByAccount("account1")

        // Assert
        assertEquals(2, starredEmails.size)
        assertTrue(starredEmails.all { it.isStarred })
    }

    private fun createTestEmailEntity(
        id: String,
        accountId: String,
        folderId: String,
        subject: String = "Test Email $id",
        isRead: Boolean = false,
        isStarred: Boolean = false
    ): EmailEntity {
        return EmailEntity(
            id = id,
            accountId = accountId,
            folderId = folderId,
            threadId = "thread1",
            subject = subject,
            fromName = "Test Sender",
            fromAddress = "sender@example.com",
            replyToAddress = null,
            toAddresses = "[\"recipient@example.com\"]",
            ccAddresses = "[]",
            bccAddresses = "[]",
            bodyText = "Test email body",
            bodyHtml = "<p>Test email body</p>",
            sentDate = System.currentTimeMillis(),
            receivedDate = System.currentTimeMillis(),
            messageId = "message-$id",
            inReplyTo = null,
            references = null,
            isRead = isRead,
            isStarred = isStarred,
            isFlagged = false,
            isDraft = false,
            hasAttachments = false,
            priority = "NORMAL",
            sizeBytes = 1024L,
            uid = 1L,
            messageNumber = 1,
            labels = "[]",
            flags = "[]",
            headers = "{}",
            syncState = "SYNCED",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun createTestFolderEntity(
        id: String,
        accountId: String,
        type: FolderType
    ): FolderEntity {
        return FolderEntity(
            id = id,
            accountId = accountId,
            name = type.name,
            displayName = type.name,
            type = type.name,
            parentFolderId = null,
            separator = "/",
            isSelectable = true,
            isSubscribed = true,
            totalCount = 0,
            unreadCount = 0,
            recentCount = 0,
            uidValidity = null,
            uidNext = null,
            lastSyncTime = System.currentTimeMillis(),
            syncEnabled = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}