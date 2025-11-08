package com.gf.mail.data.email

import com.sun.mail.imap.IMAPStore
import com.sun.mail.imap.IMAPFolder
import javax.mail.Folder
import javax.mail.Message
import javax.mail.MessagingException
import java.util.*

/**
 * 网易邮箱专用调试工具
 * 用于诊断163/126/188邮箱连接和同步问题
 */
object NeteaseDebugHelper {
    
    /**
     * 执行完整的网易邮箱诊断
     */
    fun performFullDiagnosis(store: IMAPStore, email: String): DiagnosisResult {
        val result = DiagnosisResult()
        
        try {
            println("🔍 [NETEASE_DEBUG] Starting full diagnosis for: $email")
            
            // 1. 检查连接状态
            result.connectionStatus = checkConnectionStatus(store)
            
            // 2. 检查TLS安全
            result.tlsSecurity = checkTlsSecurity(store)
            
            // 3. 发送ID命令
            result.idCommandSent = sendIdCommand(store)
            
            // 4. 检查文件夹访问权限
            result.folderAccess = checkFolderAccess(store)
            
            // 5. 测试INBOX访问
            result.inboxAccess = testInboxAccess(store)
            
            // 6. 检查邮件获取能力
            result.emailRetrieval = testEmailRetrieval(store)
            
            result.overallStatus = determineOverallStatus(result)
            
            println("✅ [NETEASE_DEBUG] Diagnosis completed: ${result.overallStatus}")
            
        } catch (e: Exception) {
            result.overallStatus = "ERROR"
            result.errorMessage = e.message
            println("❌ [NETEASE_DEBUG] Diagnosis failed: ${e.message}")
        }
        
        return result
    }
    
    /**
     * 检查连接状态
     */
    private fun checkConnectionStatus(store: IMAPStore): String {
        return try {
            if (store.isConnected) {
                "CONNECTED"
            } else {
                "DISCONNECTED"
            }
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }
    
    /**
     * 检查TLS安全
     */
    private fun checkTlsSecurity(store: IMAPStore): String {
        return try {
            // 尝试通过反射获取TLS信息
            val socket = try {
                val field = store.javaClass.getDeclaredField("socket")
                field.isAccessible = true
                field.get(store)
            } catch (e: Exception) {
                null
            }
            
            if (socket != null) {
                "TLS_DETECTED"
            } else {
                "TLS_UNKNOWN"
            }
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }
    
    /**
     * 发送ID命令
     * 根据网易邮箱官方要求，必须发送IMAP ID命令以声明客户端身份
     */
    private fun sendIdCommand(store: IMAPStore): String {
        return try {
            val idParams = mapOf(
                "name" to "GFMail",
                "version" to "1.0.0",
                "vendor" to "GoForwarder",
                "os" to "Android",
                "os-version" to "API 21+",
                "client" to "GFMail",
                "client-version" to "1.0.0"
            )
            store.id(idParams)
            println("✅ [NETEASE_DEBUG] IMAP ID command sent successfully: $idParams")
            "SUCCESS"
        } catch (e: Exception) {
            println("⚠️ [NETEASE_DEBUG] Failed to send IMAP ID command: ${e.message}")
            "FAILED: ${e.message}"
        }
    }
    
    /**
     * 检查文件夹访问权限
     */
    private fun checkFolderAccess(store: IMAPStore): Map<String, String> {
        val folderAccess = mutableMapOf<String, String>()
        
        val testFolders = listOf("INBOX", "Sent", "Drafts", "Trash", "Spam")
        
        for (folderName in testFolders) {
            try {
                val folder = store.getFolder(folderName) as IMAPFolder
                if (folder.exists()) {
                    folderAccess[folderName] = "EXISTS"
                    
                    // 使用新的访问策略检查文件夹访问
                    val accessResult = NeteaseFolderAccessStrategy.openNeteaseFolder(store, folderName, true)
                    
                    if (accessResult.success) {
                        folderAccess["${folderName}_OPEN"] = "SUCCESS (${accessResult.accessMethod})"
                        accessResult.folder?.close(false)
                    } else {
                        folderAccess["${folderName}_OPEN"] = "FAILED: ${accessResult.errorMessage}"
                        if (accessResult.accessMethod == "RESTRICTED") {
                            folderAccess["${folderName}_RESTRICTED"] = "YES"
                        }
                    }
                } else {
                    folderAccess[folderName] = "NOT_EXISTS"
                }
            } catch (e: Exception) {
                folderAccess[folderName] = "ERROR: ${e.message}"
            }
        }
        
        return folderAccess
    }
    
    /**
     * 测试INBOX访问
     */
    private fun testInboxAccess(store: IMAPStore): String {
        return try {
            val inbox = store.getFolder("INBOX") as IMAPFolder
            if (!inbox.exists()) {
                return "INBOX_NOT_EXISTS"
            }
            
            inbox.open(Folder.READ_ONLY)
            val messageCount = inbox.messageCount
            val unreadCount = inbox.unreadMessageCount
            inbox.close(false)
            
            "SUCCESS: $messageCount messages, $unreadCount unread"
        } catch (e: MessagingException) {
            "FAILED: ${e.message}"
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }
    
    /**
     * 测试邮件获取能力
     */
    private fun testEmailRetrieval(store: IMAPStore): String {
        return try {
            val inbox = store.getFolder("INBOX") as IMAPFolder
            if (!inbox.exists()) {
                return "INBOX_NOT_EXISTS"
            }
            
            inbox.open(Folder.READ_ONLY)
            
            if (inbox.messageCount == 0) {
                inbox.close(false)
                return "NO_MESSAGES"
            }
            
            // 尝试获取第一封邮件
            val firstMessage = inbox.getMessage(1)
            val subject = firstMessage.subject ?: "No Subject"
            val from = firstMessage.from?.firstOrNull()?.toString() ?: "Unknown"
            
            inbox.close(false)
            
            "SUCCESS: Retrieved message from $from with subject '$subject'"
        } catch (e: MessagingException) {
            "FAILED: ${e.message}"
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }
    
    /**
     * 确定整体状态
     */
    private fun determineOverallStatus(result: DiagnosisResult): String {
        val issues = mutableListOf<String>()
        
        if (result.connectionStatus != "CONNECTED") {
            issues.add("Connection issue: ${result.connectionStatus}")
        }
        
        if (result.tlsSecurity.contains("ERROR")) {
            issues.add("TLS issue: ${result.tlsSecurity}")
        }
        
        if (result.idCommandSent.contains("FAILED")) {
            issues.add("ID command failed: ${result.idCommandSent}")
        }
        
        if (result.inboxAccess.contains("FAILED") || result.inboxAccess.contains("ERROR")) {
            issues.add("INBOX access issue: ${result.inboxAccess}")
        }
        
        if (result.emailRetrieval.contains("FAILED") || result.emailRetrieval.contains("ERROR")) {
            issues.add("Email retrieval issue: ${result.emailRetrieval}")
        }
        
        return if (issues.isEmpty()) {
            "HEALTHY"
        } else {
            "ISSUES_FOUND: ${issues.joinToString("; ")}"
        }
    }
    
    /**
     * 生成诊断报告
     */
    fun generateDiagnosisReport(result: DiagnosisResult): String {
        val report = StringBuilder()
        report.appendLine("=== 网易邮箱诊断报告 ===")
        report.appendLine("整体状态: ${result.overallStatus}")
        report.appendLine()
        
        report.appendLine("1. 连接状态: ${result.connectionStatus}")
        report.appendLine("2. TLS安全: ${result.tlsSecurity}")
        report.appendLine("3. ID命令: ${result.idCommandSent}")
        report.appendLine("4. INBOX访问: ${result.inboxAccess}")
        report.appendLine("5. 邮件获取: ${result.emailRetrieval}")
        report.appendLine()
        
        if (result.folderAccess.isNotEmpty()) {
            report.appendLine("6. 文件夹访问权限:")
            result.folderAccess.forEach { (folder, status) ->
                report.appendLine("   - $folder: $status")
            }
            report.appendLine()
            
            // 检查是否有被限制的文件夹
            val restrictedFolders = result.folderAccess.filter { (_, status) ->
                status.contains("RESTRICTED") || status.contains("Unsafe Login")
            }
            
            if (restrictedFolders.isNotEmpty()) {
                report.appendLine("⚠️ 被限制的文件夹:")
                restrictedFolders.forEach { (folder, status) ->
                    report.appendLine("   - $folder: $status")
                }
                report.appendLine()
            }
        }
        
        if (result.errorMessage != null) {
            report.appendLine("错误信息: ${result.errorMessage}")
        }
        
        // 添加建议
        report.appendLine("=== 建议 ===")
        when {
            result.overallStatus == "HEALTHY" -> {
                report.appendLine("✅ 邮箱连接正常，可以正常同步邮件")
            }
            result.connectionStatus != "CONNECTED" -> {
                report.appendLine("❌ 连接问题：请检查网络连接和服务器设置")
            }
            result.inboxAccess.contains("Unsafe Login") -> {
                report.appendLine("❌ 安全策略限制：")
                report.appendLine("   1. 登录163/126/188邮箱网页版")
                report.appendLine("   2. 进入设置 → POP3/SMTP/IMAP")
                report.appendLine("   3. 启用客户端授权码功能")
                report.appendLine("   4. 使用生成的16位授权码作为密码")
                report.appendLine("   5. 确保使用993端口+SSL连接")
            }
            result.folderAccess.values.any { it.contains("RESTRICTED") || it.contains("Unsafe Login") } -> {
                report.appendLine("⚠️ 部分文件夹被限制：")
                report.appendLine("   1. INBOX文件夹通常可以正常访问")
                report.appendLine("   2. Sent、Drafts等文件夹可能被网易安全策略限制")
                report.appendLine("   3. 这是网易邮箱的正常安全策略，不影响基本功能")
                report.appendLine("   4. 如需完整功能，请联系网易客服: kefu@188.com")
                report.appendLine("   5. 建议优先使用INBOX文件夹进行邮件同步")
            }
            result.emailRetrieval.contains("FAILED") -> {
                report.appendLine("❌ 邮件获取失败：可能是文件夹权限或编码问题")
            }
            else -> {
                report.appendLine("⚠️ 部分功能异常，请检查具体错误信息")
            }
        }
        
        return report.toString()
    }
}

/**
 * 诊断结果数据类
 */
data class DiagnosisResult(
    var connectionStatus: String = "UNKNOWN",
    var tlsSecurity: String = "UNKNOWN",
    var idCommandSent: String = "UNKNOWN",
    var folderAccess: Map<String, String> = emptyMap(),
    var inboxAccess: String = "UNKNOWN",
    var emailRetrieval: String = "UNKNOWN",
    var overallStatus: String = "UNKNOWN",
    var errorMessage: String? = null
)
