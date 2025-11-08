package com.gf.mail.domain.model

/**
 * Data class representing signature validation result
 */
data class SignatureValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

/**
 * Data class representing signature template
 */
data class SignatureTemplate(
    val id: String,
    val name: String,
    val content: String,
    val isDefault: Boolean = false
)