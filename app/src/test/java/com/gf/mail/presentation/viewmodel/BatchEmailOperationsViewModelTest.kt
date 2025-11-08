package com.gf.mail.presentation.viewmodel

import com.gf.mail.domain.repository.BatchOperationResult
import com.gf.mail.domain.usecase.BatchEmailOperationsUseCase
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for BatchEmailOperationsViewModel
 */
class BatchEmailOperationsViewModelTest {

    private lateinit var batchEmailOperationsUseCase: BatchEmailOperationsUseCase
    private lateinit var viewModel: BatchEmailOperationsViewModel

    @Before
    fun setup() {
        batchEmailOperationsUseCase = mockk()
        viewModel = BatchEmailOperationsViewModel(batchEmailOperationsUseCase)
    }

    @Test
    fun `selectAllEmails should update selected emails`() = runTest {
        // Arrange
        val emailIds = listOf("email1", "email2", "email3")

        // Act
        viewModel.selectAllEmails(emailIds)

        // Assert
        val selectedEmails = viewModel.selectedEmails.value
        assertEquals(3, selectedEmails.size)
        assertTrue(selectedEmails.containsAll(emailIds))
        
        val uiState = viewModel.uiState.value
        assertTrue(uiState.hasSelection)
        assertEquals(3, uiState.selectedCount)
    }

    @Test
    fun `markSelectedAsRead should call use case with correct parameters`() = runTest {
        // Arrange
        val emailIds = listOf("1", "2", "3")
        val expectedResult = BatchOperationResult.Success("mark_read_unread", 3, 3)
        coEvery { batchEmailOperationsUseCase.markEmailsReadUnread(any(), any()) } returns flowOf(expectedResult)
        
        viewModel.selectAllEmails(emailIds)

        // Act
        viewModel.markSelectedAsRead()

        // Assert
        coVerify { batchEmailOperationsUseCase.markEmailsReadUnread(listOf(1L, 2L, 3L), true) }
    }

    @Test
    fun `markSelectedAsUnread should call use case with correct parameters`() = runTest {
        // Arrange
        val emailIds = listOf("1", "2", "3")
        val expectedResult = BatchOperationResult.Success("mark_read_unread", 3, 3)
        coEvery { batchEmailOperationsUseCase.markEmailsReadUnread(any(), any()) } returns flowOf(expectedResult)
        
        viewModel.selectAllEmails(emailIds)

        // Act
        viewModel.markSelectedAsUnread()

        // Assert
        coVerify { batchEmailOperationsUseCase.markEmailsReadUnread(listOf(1L, 2L, 3L), false) }
    }

    @Test
    fun `deleteSelectedEmails should call use case with correct parameters`() = runTest {
        // Arrange
        val emailIds = listOf("1", "2", "3")
        val expectedResult = BatchOperationResult.Success("delete_emails", 3, 3)
        coEvery { batchEmailOperationsUseCase.deleteEmails(any()) } returns flowOf(expectedResult)
        
        viewModel.selectAllEmails(emailIds)

        // Act
        viewModel.deleteSelectedEmails()

        // Assert
        coVerify { batchEmailOperationsUseCase.deleteEmails(listOf(1L, 2L, 3L)) }
    }

    @Test
    fun `starSelectedEmails should call use case with correct parameters`() = runTest {
        // Arrange
        val emailIds = listOf("1", "2", "3")
        val expectedResult = BatchOperationResult.Success("star_emails", 3, 3)
        coEvery { batchEmailOperationsUseCase.starEmails(any(), any()) } returns flowOf(expectedResult)
        
        viewModel.selectAllEmails(emailIds)

        // Act
        viewModel.starSelectedEmails()

        // Assert
        coVerify { batchEmailOperationsUseCase.starEmails(listOf(1L, 2L, 3L), true) }
    }

    @Test
    fun `archiveSelectedEmails should call use case with correct parameters`() = runTest {
        // Arrange
        val emailIds = listOf("1", "2", "3")
        val expectedResult = BatchOperationResult.Success("archive_emails", 3, 3)
        coEvery { batchEmailOperationsUseCase.archiveEmails(any()) } returns flowOf(expectedResult)
        
        viewModel.selectAllEmails(emailIds)

        // Act
        viewModel.archiveSelectedEmails()

        // Assert
        coVerify { batchEmailOperationsUseCase.archiveEmails(listOf(1L, 2L, 3L)) }
    }

    @Test
    fun `markSelectedAsSpam should call use case with correct parameters`() = runTest {
        // Arrange
        val emailIds = listOf("1", "2", "3")
        val expectedResult = BatchOperationResult.Success("mark_as_spam", 3, 3)
        coEvery { batchEmailOperationsUseCase.markEmailsAsSpam(any()) } returns flowOf(expectedResult)
        
        viewModel.selectAllEmails(emailIds)

        // Act
        viewModel.markSelectedAsSpam()

        // Assert
        coVerify { batchEmailOperationsUseCase.markEmailsAsSpam(listOf(1L, 2L, 3L)) }
    }

    @Test
    fun `moveSelectedEmailsToFolder should call use case with correct parameters`() = runTest {
        // Arrange
        val emailIds = listOf("1", "2", "3")
        val folderId = 1L
        val expectedResult = BatchOperationResult.Success("move_to_folder", 3, 3)
        coEvery { batchEmailOperationsUseCase.moveEmailsToFolder(any(), any()) } returns flowOf(expectedResult)
        
        viewModel.selectAllEmails(emailIds)

        // Act
        viewModel.moveSelectedEmailsToFolder(folderId)

        // Assert
        coVerify { batchEmailOperationsUseCase.moveEmailsToFolder(listOf(1L, 2L, 3L), folderId) }
    }

    @Test
    fun `toggleEmailSelection should add email to selection`() = runTest {
        // Arrange
        val emailId = "email1"

        // Act
        viewModel.toggleEmailSelection(emailId)

        // Assert
        val selectedEmails = viewModel.selectedEmails.value
        assertTrue(selectedEmails.contains(emailId))
        
        val uiState = viewModel.uiState.value
        assertTrue(uiState.hasSelection)
        assertEquals(1, uiState.selectedCount)
    }

    @Test
    fun `toggleEmailSelection should remove email from selection when already selected`() = runTest {
        // Arrange
        val emailId = "email1"
        viewModel.toggleEmailSelection(emailId)

        // Act
        viewModel.toggleEmailSelection(emailId)

        // Assert
        val selectedEmails = viewModel.selectedEmails.value
        assertFalse(selectedEmails.contains(emailId))
        
        val uiState = viewModel.uiState.value
        assertFalse(uiState.hasSelection)
        assertEquals(0, uiState.selectedCount)
    }

    @Test
    fun `clearSelection should clear all selected emails`() = runTest {
        // Arrange
        val emailIds = listOf("email1", "email2", "email3")
        viewModel.selectAllEmails(emailIds)

        // Act
        viewModel.clearSelection()

        // Assert
        val selectedEmails = viewModel.selectedEmails.value
        assertTrue(selectedEmails.isEmpty())
        
        val uiState = viewModel.uiState.value
        assertFalse(uiState.hasSelection)
        assertEquals(0, uiState.selectedCount)
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Arrange
        val emailIds = listOf("1", "2", "3")
        val errorResult = BatchOperationResult.Error("test_operation", "Test error")
        coEvery { batchEmailOperationsUseCase.markEmailsReadUnread(any(), any()) } returns flowOf(errorResult)
        
        viewModel.selectAllEmails(emailIds)
        viewModel.markSelectedAsRead()

        // Wait for error to be set
        Thread.sleep(100)

        // Act
        viewModel.clearError()

        // Assert
        val uiState = viewModel.uiState.value
        assertNull(uiState.error)
    }

    @Test
    fun `clearSuccessMessage should clear success message`() = runTest {
        // Arrange
        val emailIds = listOf("1", "2", "3")
        val successResult = BatchOperationResult.Success("test_operation", 3, 3)
        coEvery { batchEmailOperationsUseCase.markEmailsReadUnread(any(), any()) } returns flowOf(successResult)
        
        viewModel.selectAllEmails(emailIds)
        viewModel.markSelectedAsRead()

        // Wait for success message to be set
        Thread.sleep(100)

        // Act
        viewModel.clearSuccessMessage()

        // Assert
        val uiState = viewModel.uiState.value
        assertNull(uiState.successMessage)
    }
}