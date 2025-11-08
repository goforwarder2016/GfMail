package com.gf.mail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.mail.domain.model.HandwritingData
import com.gf.mail.domain.model.HandwritingRecognitionResult
import com.gf.mail.domain.usecase.HandwritingRecognitionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for handwriting recognition
 */
class HandwritingRecognitionViewModel(
    private val handwritingRecognitionUseCase: HandwritingRecognitionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HandwritingRecognitionUiState())
    val uiState: StateFlow<HandwritingRecognitionUiState> = _uiState.asStateFlow()

    fun recognizeHandwriting(handwritingData: HandwritingData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = handwritingRecognitionUseCase.recognizeHandwriting(handwritingData)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    recognitionResult = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Recognition failed"
                )
            }
        }
    }

    fun saveHandwriting(handwritingData: HandwritingData) {
        viewModelScope.launch {
            try {
                val success = handwritingRecognitionUseCase.saveHandwriting(handwritingData)
                if (success) {
                    _uiState.value = _uiState.value.copy(successMessage = "Handwriting saved successfully")
                } else {
                    _uiState.value = _uiState.value.copy(error = "Failed to save handwriting")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to save handwriting")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearRecognitionResult() {
        _uiState.value = _uiState.value.copy(recognitionResult = null)
    }

    fun clearRecognition() {
        _uiState.value = _uiState.value.copy(
            recognitionResult = null,
            error = null,
            successMessage = null
        )
    }

    data class HandwritingRecognitionUiState(
        val isLoading: Boolean = false,
        val isRecognizing: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,
        val recognitionResult: HandwritingRecognitionResult? = null
    )
}