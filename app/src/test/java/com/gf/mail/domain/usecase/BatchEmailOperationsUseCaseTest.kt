package com.gf.mail.domain.usecase

import com.gf.mail.domain.repository.EmailRepository
import com.gf.mail.domain.repository.BatchOperationResult
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for BatchEmailOperationsUseCase
 */
class BatchEmailOperationsUseCaseTest {

    private lateinit var emailRepository: EmailRepository
    private lateinit var batchEmailOperationsUseCase: BatchEmailOperationsUseCase

    @Before
    fun setup() {
        emailRepository = mockk()
        batchEmailOperationsUseCase = BatchEmailOperationsUseCase(emailRepository)
    }

    @Test
    fun `markEmailsReadUnread should return success when all operations succeed`() = runTest {
        // Arrange
        val emailIds = listOf(1L, 2L, 3L)
        coEvery { emailRepository.batchMarkAsRead(any<List<String>>(), any<Boolean>()) } returns BatchOperationResult.Success("mark_read_unread", 3, 3)

        // Act
        val result = batchEmailOperationsUseCase.markEmailsReadUnread(emailIds, true)

        // Assert
        result.collect { batchResult ->
            when (batchResult) {
                is BatchOperationResult.Success -> {
                    assertEquals("mark_read_unread", batchResult.operation)
                    assertEquals(3, batchResult.totalCount)
                    assertEquals(3, batchResult.successCount)
                }
                else -> fail("Expected Success result")
            }
        }

        coVerify { emailRepository.batchMarkAsRead(any<List<String>>(), true) }
    }

    @Test
    fun `markEmailsReadUnread should return partial success when some operations fail`() = runTest {
        // Arrange
        val emailIds = listOf(1L, 2L, 3L)
        coEvery { emailRepository.batchMarkAsRead(any<List<String>>(), any<Boolean>()) } returns BatchOperationResult.PartialSuccess("mark_read_unread", 3, 2, 1, listOf("Network error"))

        // Act
        val result = batchEmailOperationsUseCase.markEmailsReadUnread(emailIds, true)

        // Assert
        result.collect { batchResult ->
            when (batchResult) {
                is BatchOperationResult.PartialSuccess -> {
                    assertEquals("mark_read_unread", batchResult.operation)
                    assertEquals(3, batchResult.totalCount)
                    assertEquals(2, batchResult.successCount)
                    assertEquals(1, batchResult.errorCount)
                    assertTrue(batchResult.errors.isNotEmpty())
                }
                else -> fail("Expected PartialSuccess result")
            }
        }
    }

    @Test
    fun `deleteEmails should return success when all operations succeed`() = runTest {
        // Arrange
        val emailIds = listOf(1L, 2L)
        coEvery { emailRepository.batchDeleteEmails(any<List<String>>()) } returns BatchOperationResult.Success("delete_emails", 2, 2)

        // Act
        val result = batchEmailOperationsUseCase.deleteEmails(emailIds)

        // Assert
        result.collect { batchResult ->
            when (batchResult) {
                is BatchOperationResult.Success -> {
                    assertEquals("delete_emails", batchResult.operation)
                    assertEquals(2, batchResult.totalCount)
                    assertEquals(2, batchResult.successCount)
                }
                else -> fail("Expected Success result")
            }
        }

        coVerify { emailRepository.batchDeleteEmails(any<List<String>>()) }
    }

    @Test
    fun `starEmails should return success when all operations succeed`() = runTest {
        // Arrange
        val emailIds = listOf(1L, 2L)
        coEvery { emailRepository.batchStarEmails(any<List<String>>(), any<Boolean>()) } returns BatchOperationResult.Success("star_emails", 2, 2)

        // Act
        val result = batchEmailOperationsUseCase.starEmails(emailIds, true)

        // Assert
        result.collect { batchResult ->
            when (batchResult) {
                is BatchOperationResult.Success -> {
                    assertEquals("star_emails", batchResult.operation)
                    assertEquals(2, batchResult.totalCount)
                    assertEquals(2, batchResult.successCount)
                }
                else -> fail("Expected Success result")
            }
        }

        coVerify { emailRepository.batchStarEmails(any<List<String>>(), true) }
    }

    @Test
    fun `moveEmailsToFolder should return success when all operations succeed`() = runTest {
        // Arrange
        val emailIds = listOf(1L, 2L)
        val targetFolderId = 1L
        coEvery { emailRepository.batchMoveEmails(any<List<String>>(), any<String>()) } returns BatchOperationResult.Success("move_to_folder", 2, 2)

        // Act
        val result = batchEmailOperationsUseCase.moveEmailsToFolder(emailIds, targetFolderId)

        // Assert
        result.collect { batchResult ->
            when (batchResult) {
                is BatchOperationResult.Success -> {
                    assertEquals("move_to_folder", batchResult.operation)
                    assertEquals(2, batchResult.totalCount)
                    assertEquals(2, batchResult.successCount)
                }
                else -> fail("Expected Success result")
            }
        }

        coVerify { emailRepository.batchMoveEmails(any<List<String>>(), any<String>()) }
    }

    @Test
    fun `archiveEmails should return success when all operations succeed`() = runTest {
        // Arrange
        val emailIds = listOf(1L, 2L)
        coEvery { emailRepository.batchArchiveEmails(any<List<String>>()) } returns BatchOperationResult.Success("archive_emails", 2, 2)

        // Act
        val result = batchEmailOperationsUseCase.archiveEmails(emailIds)

        // Assert
        result.collect { batchResult ->
            when (batchResult) {
                is BatchOperationResult.Success -> {
                    assertEquals("archive_emails", batchResult.operation)
                    assertEquals(2, batchResult.totalCount)
                    assertEquals(2, batchResult.successCount)
                }
                else -> fail("Expected Success result")
            }
        }

        coVerify { emailRepository.batchArchiveEmails(any<List<String>>()) }
    }

    @Test
    fun `markEmailsAsSpam should return success when all operations succeed`() = runTest {
        // Arrange
        val emailIds = listOf(1L, 2L)
        coEvery { emailRepository.batchMarkAsSpam(any<List<String>>()) } returns BatchOperationResult.Success("mark_as_spam", 2, 2)

        // Act
        val result = batchEmailOperationsUseCase.markEmailsAsSpam(emailIds)

        // Assert
        result.collect { batchResult ->
            when (batchResult) {
                is BatchOperationResult.Success -> {
                    assertEquals("mark_as_spam", batchResult.operation)
                    assertEquals(2, batchResult.totalCount)
                    assertEquals(2, batchResult.successCount)
                }
                else -> fail("Expected Success result")
            }
        }

        coVerify { emailRepository.batchMarkAsSpam(any<List<String>>()) }
    }

    @Test
    fun `markEmailsReadUnread should return success result`() = runTest {
        // Arrange
        val emailIds = listOf(1L, 2L, 3L)
        coEvery { emailRepository.batchMarkAsRead(any<List<String>>(), any<Boolean>()) } returns BatchOperationResult.Success("mark_read_unread", 3, 3)

        // Act
        val result = batchEmailOperationsUseCase.markEmailsReadUnread(emailIds, true)

        // Assert
        result.collect { batchResult ->
            when (batchResult) {
                is BatchOperationResult.Success -> {
                    assertEquals("mark_read_unread", batchResult.operation)
                    assertEquals(3, batchResult.totalCount)
                    assertEquals(3, batchResult.successCount)
                }
                else -> fail("Expected Success result")
            }
        }
    }
}