package com.gf.mail.data.mapper

import com.gf.mail.data.local.entity.EmailSignatureEntity
import com.gf.mail.domain.model.EmailSignature

/**
 * Mapper for converting between EmailSignature domain model and EmailSignatureEntity database entity
 */

/**
 * Convert EmailSignatureEntity (database) to EmailSignature (domain)
 */
fun EmailSignatureEntity.toDomain(): EmailSignature {
    return EmailSignature(
        id = id,
        name = name,
        content = content,
        type = EmailSignature.SignatureType.valueOf(type),
        isHtml = isHtml,
        isDefault = isDefault,
        accountId = accountId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        hasHandwritingData = hasHandwritingData,
        handwritingPath = handwritingPath,
        handwritingWidth = handwritingWidth,
        handwritingHeight = handwritingHeight
    )
}

/**
 * Convert EmailSignature (domain) to EmailSignatureEntity (database)
 */
fun EmailSignature.toEntity(): EmailSignatureEntity {
    return EmailSignatureEntity(
        id = id,
        name = name,
        content = content,
        type = type.name,
        isHtml = isHtml,
        isDefault = isDefault,
        accountId = accountId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        hasHandwritingData = hasHandwritingData,
        handwritingPath = handwritingPath,
        handwritingWidth = handwritingWidth,
        handwritingHeight = handwritingHeight
    )
}