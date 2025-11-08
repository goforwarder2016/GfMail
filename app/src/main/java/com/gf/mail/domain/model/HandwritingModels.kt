package com.gf.mail.domain.model

/**
 * Handwriting recognition result domain model
 */
data class HandwritingRecognitionResult(
    val text: String,
    val confidence: Float,
    val alternatives: List<String> = emptyList(),
    val isRecognizing: Boolean = false
)