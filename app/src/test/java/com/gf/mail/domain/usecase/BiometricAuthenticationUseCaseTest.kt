package com.gf.mail.domain.usecase

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.gf.mail.domain.model.BiometricAvailability
import com.gf.mail.domain.model.BiometricAuthResult
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for BiometricAuthenticationUseCase
 */
class BiometricAuthenticationUseCaseTest {

    private lateinit var biometricAuthenticationUseCase: BiometricAuthenticationUseCase
    private lateinit var mockContext: Context
    private lateinit var mockActivity: FragmentActivity

    @Before
    fun setup() {
        biometricAuthenticationUseCase = BiometricAuthenticationUseCase()
        mockContext = mockk()
        mockActivity = mockk()
    }

    @Test
    fun `checkBiometricAvailability should return availability status`() {
        // Arrange
        every { mockContext } returns mockContext

        // Act
        val result = biometricAuthenticationUseCase.checkBiometricAvailability(mockContext)

        // Assert
        assertNotNull(result)
        assertTrue(result is BiometricAvailability)
    }

    @Test
    fun `getBiometricAvailabilityMessage should return appropriate message`() {
        // Arrange
        every { mockContext } returns mockContext

        // Act
        val message = biometricAuthenticationUseCase.getBiometricAvailabilityMessage(mockContext)

        // Assert
        assertNotNull(message)
        assertTrue(message.isNotEmpty())
    }

    @Test
    fun `authenticateWithBiometrics should return success result`() = runTest {
        // Arrange
        val expectedResult = BiometricAuthResult.Success
        every { mockContext } returns mockContext

        // Act
        val result = biometricAuthenticationUseCase.authenticateWithBiometrics(
            mockContext, "Title", "Subtitle", "Description", "Cancel"
        )

        // Assert
        assertEquals(expectedResult, result)
    }

    @Test
    fun `authenticateWithDeviceCredentials should return success result`() = runTest {
        // Arrange
        val expectedResult = BiometricAuthResult.Success
        every { mockContext } returns mockContext

        // Act
        val result = biometricAuthenticationUseCase.authenticateWithDeviceCredentials(
            mockContext, "Title", "Subtitle", "Description", "Cancel"
        )

        // Assert
        assertEquals(expectedResult, result)
    }

    @Test
    fun `hasSecureLockScreen should return true`() {
        // Arrange
        every { mockContext } returns mockContext

        // Act
        val result = biometricAuthenticationUseCase.hasSecureLockScreen(mockContext)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `authenticateWithBiometrics should handle authentication flow`() = runTest {
        // Arrange
        every { mockContext } returns mockContext

        // Act
        val result = biometricAuthenticationUseCase.authenticateWithBiometrics(
            mockContext, "Test Title", "Test Subtitle", "Test Description", "Test Cancel"
        )

        // Assert
        assertNotNull(result)
        assertTrue(result is BiometricAuthResult)
    }

    @Test
    fun `authenticateWithDeviceCredentials should handle authentication flow`() = runTest {
        // Arrange
        every { mockContext } returns mockContext

        // Act
        val result = biometricAuthenticationUseCase.authenticateWithDeviceCredentials(
            mockContext, "Test Title", "Test Subtitle", "Test Description", "Test Cancel"
        )

        // Assert
        assertNotNull(result)
        assertTrue(result is BiometricAuthResult)
    }
}