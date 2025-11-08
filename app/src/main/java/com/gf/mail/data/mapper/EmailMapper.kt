package com.gf.mail.data.mapper

import com.gf.mail.data.local.entity.EmailEntity
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.model.EmailAttachment
import com.gf.mail.domain.model.EmailPriority
import com.gf.mail.domain.model.SyncState
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Mapper for converting between Email domain model and EmailEntity
 */
object EmailMapper {

    /**
     * Convert EmailEntity to Email domain model
     */
    fun toDomain(entity: EmailEntity): Email {
        // Helper function to parse JSON string list
        fun parseStringList(jsonString: String): List<String> {
            val jsonElement: JsonElement? = JsonMapper.parseJson(jsonString)
            return if (jsonElement != null) {
                val map: Map<String, Any> = JsonMapper.jsonToMap(jsonElement)
                map.keys.toList()
            } else {
                emptyList<String>()
            }
        }
        
        // Helper function to parse JSON string map
        fun parseStringMap(jsonString: String): Map<String, String> {
            val jsonElement: JsonElement? = JsonMapper.parseJson(jsonString)
            return if (jsonElement != null) {
                val map: Map<String, Any> = JsonMapper.jsonToMap(jsonElement)
                map.mapValues { (_, value) -> value.toString() }
            } else {
                emptyMap<String, String>()
            }
        }
        
        return Email(
            id = entity.id,
            accountId = entity.accountId,
            folderId = entity.folderId,
            threadId = entity.threadId,
            subject = entity.subject,
            fromName = entity.fromName,
            fromAddress = entity.fromAddress,
            replyToAddress = entity.replyToAddress,
            toAddresses = parseStringList(entity.toAddresses),
            ccAddresses = parseStringList(entity.ccAddresses),
            bccAddresses = parseStringList(entity.bccAddresses),
            bodyText = entity.bodyText,
            bodyHtml = entity.bodyHtml,
            originalHtmlBody = entity.originalHtmlBody,
            sentDate = entity.sentDate,
            receivedDate = entity.receivedDate,
            messageId = entity.messageId,
            inReplyTo = entity.inReplyTo,
            references = entity.references,
            isRead = entity.isRead,
            isStarred = entity.isStarred,
            isFlagged = entity.isFlagged,
            isDraft = entity.isDraft,
            hasAttachments = entity.hasAttachments,
            priority = try {
                EmailPriority.valueOf(entity.priority)
            } catch (e: Exception) {
                EmailPriority.NORMAL
            },
            size = entity.sizeBytes,
            uid = entity.uid,
            messageNumber = entity.messageNumber,
            labels = parseStringList(entity.labels),
            flags = parseStringList(entity.flags),
            headers = parseStringMap(entity.headers),
            attachments = emptyList<EmailAttachment>(), // TODO: Implement attachment mapping
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            syncState = try {
                SyncState.valueOf(entity.syncState)
            } catch (e: Exception) {
                SyncState.SYNCED
            }
        )
    }

    /**
     * Convert Email domain model to EmailEntity
     */
    fun toEntity(email: Email): EmailEntity {
        return EmailEntity(
            id = email.id,
            accountId = email.accountId,
            folderId = email.folderId,
            threadId = email.threadId,
            subject = email.subject,
            fromName = email.fromName,
            fromAddress = email.fromAddress,
            replyToAddress = email.replyToAddress,
            toAddresses = JsonMapper.toJsonString(email.toAddresses),
            ccAddresses = JsonMapper.toJsonString(email.ccAddresses),
            bccAddresses = JsonMapper.toJsonString(email.bccAddresses),
            bodyText = email.bodyText,
            bodyHtml = email.bodyHtml,
            originalHtmlBody = email.originalHtmlBody,
            sentDate = email.sentDate,
            receivedDate = email.receivedDate,
            messageId = email.messageId,
            inReplyTo = email.inReplyTo,
            references = email.references,
            isRead = email.isRead,
            isStarred = email.isStarred,
            isFlagged = email.isFlagged,
            isDraft = email.isDraft,
            hasAttachments = email.hasAttachments,
            priority = email.priority.name,
            sizeBytes = email.size,
            uid = email.uid,
            messageNumber = email.messageNumber,
            labels = JsonMapper.toJsonString(email.labels),
            flags = JsonMapper.toJsonString(email.flags),
            headers = JsonMapper.toJsonString(email.headers),
            createdAt = email.createdAt,
            updatedAt = email.updatedAt,
            syncState = email.syncState.name
        )
    }
}

/**
 * Extension function to convert EmailEntity to Email
 */
fun EmailEntity.toDomain(): Email = EmailMapper.toDomain(this)

/**
 * Extension function to convert List<EmailEntity> to List<Email>
 */
fun List<EmailEntity>.toDomain(): List<Email> = this.map { EmailMapper.toDomain(it) }

/**
 * Extension function to convert Email to EmailEntity
 */
fun Email.toEntity(): EmailEntity = EmailMapper.toEntity(this)