package com.gf.mail.domain.model

/**
 * Email signature domain model
 */
data class EmailSignature(
    val id: String,
    val name: String,
    val content: String,
    val type: SignatureType = SignatureType.TEXT,
    val isHtml: Boolean = false,
    val isDefault: Boolean = false,
    val accountId: String? = null, // null for global signatures
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Handwriting signature
    val hasHandwritingData: Boolean = false,
    val handwritingPath: String? = null, // Path to handwriting image file
    val handwritingWidth: Int = 0,
    val handwritingHeight: Int = 0
) {
    /**
     * Signature type enumeration
     */
    enum class SignatureType {
        TEXT,
        HTML,
        HANDWRITTEN
    }
}