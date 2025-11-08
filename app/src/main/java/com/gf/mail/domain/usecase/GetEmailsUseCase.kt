package com.gf.mail.domain.usecase

import android.content.Context
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.model.FolderType
import com.gf.mail.domain.repository.EmailRepository
import com.gf.mail.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll

/**
 * Use case for getting emails
 */
class GetEmailsUseCase(
    private val emailRepository: EmailRepository,
    private val folderRepository: FolderRepository,
    private val context: Context
) {
    /**
     * Get emails for a specific account and folder
     */
    fun getEmails(accountId: String, folderId: String): Flow<List<Email>> {
        // 处理特殊Folder
        when (folderId.lowercase()) {
            "inbox" -> {
                return getInboxEmails(accountId)
            }
            "sent" -> {
                return getSentEmails(accountId)
            }
            "drafts" -> {
                return getDraftEmails(accountId)
            }
            "starred" -> {
                println("🔍 [GetEmailsUseCase] Querying starred emails: accountId=$accountId")
                // 对于星标邮件，我们需要使用不同的方法
                // 由于getStarredEmails返回的是suspend函数，我们需要创建一个Flow
                return flow {
                    val starredEmails = emailRepository.getStarredEmails(accountId)
                    emit(starredEmails)
                }
            }
            else -> {
                // 直接使用folderId作为数据库查询的ID
                // 这里假设folderId就是数据库中存储的文件夹UUID
                return emailRepository.getEmailsInFolderFlow(folderId)
            }
        }
    }

    /**
     * Get all emails for an account
     */
    fun getAllEmails(accountId: String): Flow<List<Email>> {
        return emailRepository.getEmailsByAccountFlow(accountId)
    }
    
    /**
     * Get emails in inbox folder
     */
    fun getInboxEmails(accountId: String): Flow<List<Email>> {
        return flow {
            val folder = folderRepository.getFolderByType(FolderType.INBOX, accountId)
            println("🔍 [GetEmailsUseCase] getInboxEmails: accountId=$accountId, folder=$folder")
            if (folder != null) {
                println("🔍 [GetEmailsUseCase] Found INBOX folder: id=${folder.id}, name=${folder.name}, type=${folder.type}")
                emitAll(emailRepository.getEmailsInFolderFlow(folder.id))
            } else {
                println("🔍 [GetEmailsUseCase] No INBOX folder found for accountId=$accountId")
                emit(emptyList())
            }
        }
    }
    
    /**
     * Get sent emails
     */
    fun getSentEmails(accountId: String): Flow<List<Email>> {
        return flow {
            val folder = folderRepository.getFolderByType(FolderType.SENT, accountId)
            println("🔍 [GetEmailsUseCase] getSentEmails: accountId=$accountId, folder=$folder")
            if (folder != null) {
                println("🔍 [GetEmailsUseCase] Found SENT folder: id=${folder.id}, name=${folder.name}, type=${folder.type}")
                emitAll(emailRepository.getEmailsInFolderFlow(folder.id))
            } else {
                println("🔍 [GetEmailsUseCase] No SENT folder found for accountId=$accountId")
                emit(emptyList())
            }
        }
    }
    
    /**
     * Get draft emails
     */
    fun getDraftEmails(accountId: String): Flow<List<Email>> {
        return flow {
            val folder = folderRepository.getFolderByType(FolderType.DRAFTS, accountId)
            println("🔍 [GetEmailsUseCase] getDraftEmails: accountId=$accountId, folder=$folder")
            if (folder != null) {
                println("🔍 [GetEmailsUseCase] Found DRAFTS folder: id=${folder.id}, name=${folder.name}, type=${folder.type}")
                emitAll(emailRepository.getEmailsInFolderFlow(folder.id))
            } else {
                println("🔍 [GetEmailsUseCase] No DRAFTS folder found for accountId=$accountId")
                emit(emptyList())
            }
        }
    }
    
    /**
     * Get starred emails
     */
    suspend fun getStarredEmails(accountId: String): List<Email> {
        return emailRepository.getStarredEmails(accountId)
    }
    
    /**
     * Get folder ID by type for a specific account
     */
    suspend fun getFolderIdByType(folderType: FolderType, accountId: String): String? {
        return folderRepository.getFolderByType(folderType, accountId)?.id
    }
    
    /**
     * Get folder ID by name for a specific account
     */
    suspend fun getFolderIdByName(folderName: String, accountId: String): String? {
        return folderRepository.getFolderByName(folderName, accountId)?.id
    }
}