package com.gf.mail.data.email

import com.sun.mail.imap.IMAPStore
import com.sun.mail.imap.IMAPFolder
import javax.mail.Folder
import javax.mail.MessagingException
import java.util.*

/**
 * 网易邮箱文件夹访问策略
 * 专门处理163/126/188邮箱的文件夹访问限制问题
 */
object NeteaseFolderAccessStrategy {
    
    /**
     * 网易邮箱文件夹访问结果
     */
    data class AccessResult(
        val success: Boolean,
        val folder: IMAPFolder?,
        val errorMessage: String? = null,
        val accessMethod: String? = null // "SELECT", "EXAMINE", "RESTRICTED"
    )
    
    /**
     * 尝试打开网易邮箱文件夹，使用多种策略
     */
    fun openNeteaseFolder(
        store: IMAPStore, 
        folderName: String, 
        readOnly: Boolean = true
    ): AccessResult {
        try {
            println("🔧 [NETEASE_STRATEGY] Attempting to open folder: $folderName")
            
            val folder = store.getFolder(folderName) as IMAPFolder
            
            if (!folder.exists()) {
                println("❌ [NETEASE_STRATEGY] Folder does not exist: $folderName")
                return AccessResult(false, null, "Folder does not exist")
            }
            
            // 策略1: 对于INBOX，使用标准方式
            if (folderName.equals("INBOX", ignoreCase = true)) {
                return openInboxFolder(folder, readOnly)
            }
            
            // 策略2: 对于其他文件夹，尝试多种访问方式
            return openNonInboxFolder(folder, readOnly)
            
        } catch (e: Exception) {
            println("❌ [NETEASE_STRATEGY] Failed to open folder $folderName: ${e.message}")
            return AccessResult(false, null, e.message)
        }
    }
    
    /**
     * 打开INBOX文件夹
     */
    private fun openInboxFolder(folder: IMAPFolder, readOnly: Boolean): AccessResult {
        try {
            val openMode = if (readOnly) Folder.READ_ONLY else Folder.READ_WRITE
            folder.open(openMode)
            println("✅ [NETEASE_STRATEGY] INBOX opened successfully with ${if (readOnly) "READ_ONLY" else "READ_WRITE"}")
            return AccessResult(true, folder, accessMethod = if (readOnly) "EXAMINE" else "SELECT")
        } catch (e: MessagingException) {
            val errorMessage = e.message ?: ""
            if (isUnsafeLoginError(errorMessage)) {
                println("🚫 [NETEASE_STRATEGY] INBOX access blocked by NetEase security policy")
                return AccessResult(false, null, "INBOX access restricted by NetEase security policy", "RESTRICTED")
            }
            throw e
        }
    }
    
    /**
     * 打开非INBOX文件夹，使用多种策略
     */
    private fun openNonInboxFolder(folder: IMAPFolder, readOnly: Boolean): AccessResult {
        val folderName = folder.fullName
        
        // 策略1: 尝试READ_WRITE模式（使用SELECT命令）
        if (!readOnly) {
            try {
                folder.open(Folder.READ_WRITE)
                println("✅ [NETEASE_STRATEGY] Folder $folderName opened with READ_WRITE (SELECT)")
                return AccessResult(true, folder, accessMethod = "SELECT")
            } catch (e: MessagingException) {
                val errorMessage = e.message ?: ""
                if (isUnsafeLoginError(errorMessage)) {
                    println("🚫 [NETEASE_STRATEGY] READ_WRITE access blocked for $folderName")
                } else {
                    println("⚠️ [NETEASE_STRATEGY] READ_WRITE failed for $folderName: $errorMessage")
                }
            }
        }
        
        // 策略2: 尝试READ_ONLY模式（使用EXAMINE命令）
        try {
            folder.open(Folder.READ_ONLY)
            println("✅ [NETEASE_STRATEGY] Folder $folderName opened with READ_ONLY (EXAMINE)")
            return AccessResult(true, folder, accessMethod = "EXAMINE")
        } catch (e: MessagingException) {
            val errorMessage = e.message ?: ""
            if (isUnsafeLoginError(errorMessage)) {
                println("🚫 [NETEASE_STRATEGY] READ_ONLY access blocked for $folderName")
                return AccessResult(false, null, "Folder access restricted by NetEase security policy", "RESTRICTED")
            } else {
                println("⚠️ [NETEASE_STRATEGY] READ_ONLY failed for $folderName: $errorMessage")
            }
        }
        
        // 策略3: 尝试使用SELECT命令（即使要求readOnly）
        try {
            folder.open(Folder.READ_WRITE)
            println("✅ [NETEASE_STRATEGY] Folder $folderName opened with SELECT (fallback)")
            return AccessResult(true, folder, accessMethod = "SELECT_FALLBACK")
        } catch (e: MessagingException) {
            val errorMessage = e.message ?: ""
            if (isUnsafeLoginError(errorMessage)) {
                println("🚫 [NETEASE_STRATEGY] All access methods blocked for $folderName")
                return AccessResult(false, null, "Folder access restricted by NetEase security policy", "RESTRICTED")
            } else {
                println("❌ [NETEASE_STRATEGY] All access methods failed for $folderName: $errorMessage")
                return AccessResult(false, null, errorMessage)
            }
        }
    }
    
    /**
     * 检查是否是网易邮箱的"Unsafe Login"错误
     */
    private fun isUnsafeLoginError(errorMessage: String): Boolean {
        if (errorMessage.isEmpty()) return false
        val upperMsg = errorMessage.uppercase(Locale.getDefault())
        return upperMsg.contains("UNSAFE LOGIN") ||
               (upperMsg.contains("NO EXAMINE") && upperMsg.contains("UNSAFE")) ||
               (upperMsg.contains("NO SELECT") && upperMsg.contains("UNSAFE")) ||
               (upperMsg.contains("NO EXAMINE") && upperMsg.contains("PLEASE CONTACT")) ||
               (upperMsg.contains("NO SELECT") && upperMsg.contains("PLEASE CONTACT")) ||
               upperMsg.contains("KEFU@188.COM")
    }
    
    /**
     * 获取网易邮箱文件夹访问建议
     */
    fun getAccessRecommendation(folderName: String, accessResult: AccessResult): String {
        return when {
            accessResult.success -> {
                "✅ 文件夹 $folderName 访问成功，使用方式: ${accessResult.accessMethod}"
            }
            accessResult.accessMethod == "RESTRICTED" -> {
                "🚫 文件夹 $folderName 被网易邮箱安全策略限制\n\n" +
                "可能的原因:\n" +
                "1. 该文件夹需要特殊权限\n" +
                "2. 网易邮箱安全策略限制\n" +
                "3. 需要联系网易客服: kefu@188.com\n\n" +
                "建议:\n" +
                "• 优先使用INBOX文件夹\n" +
                "• 检查是否启用了客户端授权码\n" +
                "• 确认使用993端口+SSL连接"
            }
            else -> {
                "❌ 文件夹 $folderName 访问失败: ${accessResult.errorMessage}"
            }
        }
    }
    
    /**
     * 检查文件夹是否可能被网易邮箱限制
     */
    fun isLikelyRestrictedFolder(folderName: String): Boolean {
        val restrictedFolders = listOf(
            "Sent", "Drafts", "Trash", "Spam", "Junk",
            "病毒文件夹", "广告邮件", "订阅邮件", "邮箱", "网页素材", "我的文档", "MyInfors"
        )
        
        return restrictedFolders.any { restricted ->
            folderName.equals(restricted, ignoreCase = true) ||
            folderName.contains(restricted, ignoreCase = true)
        }
    }
    
    /**
     * 获取网易邮箱文件夹访问优先级
     */
    fun getFolderAccessPriority(folderName: String): Int {
        return when {
            folderName.equals("INBOX", ignoreCase = true) -> 1 // 最高优先级
            folderName.equals("Sent", ignoreCase = true) -> 2
            folderName.equals("Drafts", ignoreCase = true) -> 3
            folderName.equals("Trash", ignoreCase = true) -> 4
            folderName.equals("Spam", ignoreCase = true) -> 5
            else -> 10 // 其他文件夹优先级较低
        }
    }
}
