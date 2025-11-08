package com.gf.mail.domain.model

/**
 * Encryption type enumeration
 */
enum class EncryptionType(val displayName: String) {
    NONE("None"),
    STARTTLS("STARTTLS"),
    SSL("SSL/TLS")
}