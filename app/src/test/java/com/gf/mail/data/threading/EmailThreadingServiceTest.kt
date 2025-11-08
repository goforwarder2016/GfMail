package com.gf.mail.data.threading

import com.gf.mail.domain.model.Email
import com.gf.mail.domain.model.EmailProvider
import com.gf.mail.domain.model.AuthenticationInfo
import com.gf.mail.domain.model.AuthenticationType
import com.gf.mail.domain.model.ServerConfiguration
import com.gf.mail.domain.model.EncryptionType
import com.gf.mail.domain.model.EmailPriority
import com.gf.mail.domain.model.SyncState
import com.gf.mail.domain.repository.EmailRepository
import com.gf.mail.data.threading.EmailThread
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for EmailThreadingService
 */
class EmailThreadingServiceTest {

    private lateinit var emailRepository: EmailRepository
    private lateinit var emailThreadingService: EmailThreadingService

    private val testAccountId = "test-account-id"
    private val testThreadId = "test-thread-id"

    @Before
    fun setup() {
        emailRepository = mockk()
        emailThreadingService = EmailThreadingService(emailRepository)
    }

    @Test
    fun `getEmailThreads should return email threads for account`() = runTest {
        // Arrange
        val threadIds = listOf(testThreadId, "other-thread")
        val emails1 = listOf(
            createTestEmail("email1", threadId = testThreadId, receivedDate = 1000L),
            createTestEmail("email2", threadId = testThreadId, receivedDate = 2000L)
        )
        val emails2 = listOf(
            createTestEmail("email3", threadId = "other-thread", receivedDate = 1500L)
        )
        
        coEvery { emailRepository.getEmailThreads(any()) } returns threadIds
        coEvery { emailRepository.getEmailsByThread(testThreadId) } returns emails1
        coEvery { emailRepository.getEmailsByThread("other-thread") } returns emails2

        // Act
        val result = emailThreadingService.getEmailThreads(testAccountId).first()

        // Assert
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == testThreadId })
        assertTrue(result.any { it.id == "other-thread" })
        
        val thread1 = result.find { it.id == testThreadId }!!
        assertEquals(2, thread1.emailCount)
        assertEquals("email2", thread1.lastEmailId) // Most recent email
    }

    @Test
    fun `getEmailThread should return specific thread`() = runTest {
        // Arrange
        val emails = listOf(
            createTestEmail("email1", threadId = testThreadId, receivedDate = 1000L),
            createTestEmail("email2", threadId = testThreadId, receivedDate = 2000L)
        )
        coEvery { emailRepository.getEmailsByThread(any()) } returns emails

        // Act
        val result = emailThreadingService.getEmailThread(testThreadId)

        // Assert
        assertNotNull(result)
        assertEquals(testThreadId, result!!.id)
        assertEquals(2, result.emailCount)
        assertEquals("email2", result.lastEmailId) // Most recent email
        coVerify { emailRepository.getEmailsByThread(testThreadId) }
    }

    @Test
    fun `getEmailThreads should return flow of threads`() = runTest {
        // Arrange
        val threadIds = listOf(testThreadId)
        val emails = listOf(
            createTestEmail("email1", threadId = testThreadId),
            createTestEmail("email2", threadId = testThreadId)
        )
        coEvery { emailRepository.getEmailThreads(any()) } returns threadIds
        coEvery { emailRepository.getEmailsByThread(any()) } returns emails

        // Act
        val result = emailThreadingService.getEmailThreads(testAccountId).first()

        // Assert
        assertEquals(1, result.size)
        assertEquals(testThreadId, result[0].id)
        assertEquals(2, result[0].emailCount)
    }

    @Test
    fun `markThreadAsRead should mark all emails in thread as read`() = runTest {
        // Arrange
        val emails = listOf(
            createTestEmail("email1", threadId = testThreadId),
            createTestEmail("email2", threadId = testThreadId)
        )
        coEvery { emailRepository.getEmailsByThread(any()) } returns emails
        coEvery { emailRepository.batchMarkAsRead(any(), any()) } returns mockk()

        // Act
        emailThreadingService.markThreadAsRead(testThreadId)

        // Assert
        coVerify { emailRepository.getEmailsByThread(testThreadId) }
        coVerify { emailRepository.batchMarkAsRead(listOf("email1", "email2"), true) }
    }



    @Test
    fun `starThread should star all emails in thread`() = runTest {
        // Arrange
        val emails = listOf(
            createTestEmail("email1", threadId = testThreadId),
            createTestEmail("email2", threadId = testThreadId)
        )
        coEvery { emailRepository.getEmailsByThread(any()) } returns emails
        coEvery { emailRepository.batchStarEmails(any(), any()) } returns mockk()

        // Act
        emailThreadingService.starThread(testThreadId, true)

        // Assert
        coVerify { emailRepository.getEmailsByThread(testThreadId) }
        coVerify { emailRepository.batchStarEmails(listOf("email1", "email2"), true) }
    }

    @Test
    fun `deleteThread should delete all emails in thread`() = runTest {
        // Arrange
        val emails = listOf(
            createTestEmail("email1", threadId = testThreadId),
            createTestEmail("email2", threadId = testThreadId)
        )
        coEvery { emailRepository.getEmailsByThread(any()) } returns emails
        coEvery { emailRepository.batchDeleteEmails(any()) } returns mockk()

        // Act
        emailThreadingService.deleteThread(testThreadId)

        // Assert
        coVerify { emailRepository.getEmailsByThread(testThreadId) }
        coVerify { emailRepository.batchDeleteEmails(listOf("email1", "email2")) }
    }







    private fun createTestEmail(
        id: String,
        threadId: String? = null,
        subject: String = "Test Email",
        fromAddress: String = "sender@example.com",
        toAddresses: List<String> = listOf("recipient@example.com"),
        receivedDate: Long = System.currentTimeMillis(),
        isRead: Boolean = false
    ): Email {
        return Email(
            id = id,
            accountId = testAccountId,
            folderId = "folder1",
            threadId = threadId,
            subject = subject,
            fromName = "Test Sender",
            fromAddress = fromAddress,
            replyToAddress = null,
            toAddresses = toAddresses,
            ccAddresses = emptyList(),
            bccAddresses = emptyList(),
            bodyText = "Test email body",
            bodyHtml = "<p>Test email body</p>",
            sentDate = receivedDate,
            receivedDate = receivedDate,
            messageId = "message-$id",
            inReplyTo = null,
            references = null,
            isRead = isRead,
            isStarred = false,
            isFlagged = false,
            isDraft = false,
            hasAttachments = false,
            priority = EmailPriority.NORMAL,
            size = 1024L,
            uid = 1L,
            messageNumber = 1,
            labels = emptyList(),
            flags = emptyList(),
            headers = emptyMap(),
            syncState = SyncState.SYNCED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}