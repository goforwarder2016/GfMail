package com.gf.mail.data.email

import java.util.*

/**
 * 邮件服务器配置属性生成器
 * 专门为网易邮箱等提供标准化的Properties配置
 */
object MailProps {

    interface ProviderProfile {
        fun imapHost(): String
        fun imapPort(): Int
        fun imapSsl(): Boolean
        fun imapStarttls(): Boolean
        fun smtpHost(): String
        fun smtpPort(): Int
        fun smtpSsl(): Boolean
        fun smtpStarttls(): Boolean
        fun preferSelectOverExamine(): Boolean = true
        fun requireTls12Plus(): Boolean = true
    }

    /**
     * 为IMAP连接生成Properties
     */
    fun forImap(profile: ProviderProfile): Properties {
        val props = Properties()
        
        // 基本IMAP设置
        props.setProperty("mail.store.protocol", "imap")
        props.setProperty("mail.imap.host", profile.imapHost())
        props.setProperty("mail.imap.port", profile.imapPort().toString())
        
        // SSL/TLS设置
        if (profile.imapSsl()) {
            props.setProperty("mail.imap.ssl.enable", "true")
            props.setProperty("mail.imap.starttls.enable", "false")
            props.setProperty("mail.imap.ssl.trust", "*")
            props.setProperty("mail.imap.ssl.checkserveridentity", "false")
            props.setProperty("mail.imap.ssl.trustall", "true")
        } else if (profile.imapStarttls()) {
            props.setProperty("mail.imap.starttls.enable", "true")
            props.setProperty("mail.imap.ssl.trust", "*")
        }
        
        // 强制TLS1.2+
        if (profile.requireTls12Plus()) {
            props.setProperty("mail.imap.ssl.protocols", "TLSv1.2 TLSv1.3")
        }
        
        // 认证设置
        props.setProperty("mail.imap.auth.mechanisms", "LOGIN PLAIN")
        props.setProperty("mail.imap.auth.login.disable", "false")
        props.setProperty("mail.imap.auth.plain.disable", "false")
        props.setProperty("mail.imap.disableplainauth", "true")
        
        // 禁用不安全的认证方式
        props.setProperty("mail.imap.auth.ntlm.disable", "true")
        props.setProperty("mail.imap.auth.digestmd5.disable", "true")
        props.setProperty("mail.imap.auth.crammd5.disable", "true")
        
        // 超时设置
        props.setProperty("mail.imap.connectiontimeout", "15000")
        props.setProperty("mail.imap.timeout", "30000")
        props.setProperty("mail.imap.writetimeout", "30000")
        
        // 连接池设置
        props.setProperty("mail.imap.connectionpoolsize", "1")
        props.setProperty("mail.imap.connectionpooltimeout", "300000")
        
        // UTF-8支持（如果服务器支持）
        props.setProperty("mail.imap.enableutf8", "true")
        
        // 尝试绕过网易邮箱安全限制的额外配置
        props.setProperty("mail.imap.auth.login.disable", "false")
        props.setProperty("mail.imap.auth.plain.disable", "false")
        props.setProperty("mail.imap.disableplainauth", "false")
        props.setProperty("mail.imap.ssl.trust", "*")
        props.setProperty("mail.imap.ssl.checkserveridentity", "false")
        
        // 调试设置（仅用于网易邮箱）
        if (profile.imapHost().contains("163.com") || 
            profile.imapHost().contains("126.com") || 
            profile.imapHost().contains("188.com")) {
            props.setProperty("mail.debug", "true")
            props.setProperty("mail.debug.auth", "true")
        }
        
        return props
    }
    
    /**
     * 为SMTP连接生成Properties
     */
    fun forSmtp(profile: ProviderProfile): Properties {
        val props = Properties()
        
        // 基本SMTP设置
        props.setProperty("mail.transport.protocol", "smtp")
        props.setProperty("mail.smtp.host", profile.smtpHost())
        props.setProperty("mail.smtp.port", profile.smtpPort().toString())
        props.setProperty("mail.smtp.auth", "true")
        
        // SSL/TLS设置
        if (profile.smtpSsl()) {
            props.setProperty("mail.smtp.ssl.enable", "true")
            props.setProperty("mail.smtp.starttls.enable", "false")
            props.setProperty("mail.smtp.ssl.trust", "*")
            props.setProperty("mail.smtp.ssl.checkserveridentity", "false")
            props.setProperty("mail.smtp.ssl.trustall", "true")
        } else if (profile.smtpStarttls()) {
            props.setProperty("mail.smtp.starttls.enable", "true")
            props.setProperty("mail.smtp.ssl.trust", "*")
        }
        
        // 强制TLS1.2+
        if (profile.requireTls12Plus()) {
            props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3")
        }
        
        // 认证设置
        props.setProperty("mail.smtp.auth.mechanisms", "LOGIN PLAIN")
        props.setProperty("mail.smtp.auth.login.disable", "false")
        props.setProperty("mail.smtp.auth.plain.disable", "false")
        
        // 超时设置
        props.setProperty("mail.smtp.connectiontimeout", "30000")
        props.setProperty("mail.smtp.timeout", "30000")
        props.setProperty("mail.smtp.writetimeout", "30000")
        
        return props
    }
    
    /**
     * 创建网易邮箱配置
     */
    fun createNeteaseProfile(email: String): ProviderProfile {
        return object : ProviderProfile {
            override fun imapHost(): String = when {
                email.contains("@163.com") -> "imap.163.com"
                email.contains("@126.com") -> "imap.126.com"
                email.contains("@188.com") -> "imap.188.com"
                else -> "imap.163.com"
            }
            override fun imapPort(): Int = 993
            override fun imapSsl(): Boolean = true
            override fun imapStarttls(): Boolean = false
            override fun smtpHost(): String = when {
                email.contains("@163.com") -> "smtp.163.com"
                email.contains("@126.com") -> "smtp.126.com"
                email.contains("@188.com") -> "smtp.188.com"
                else -> "smtp.163.com"
            }
            override fun smtpPort(): Int = 465
            override fun smtpSsl(): Boolean = true
            override fun smtpStarttls(): Boolean = false
            override fun preferSelectOverExamine(): Boolean = true
            override fun requireTls12Plus(): Boolean = true
        }
    }
    
    /**
     * 创建Gmail配置
     */
    fun createGmailProfile(): ProviderProfile {
        return object : ProviderProfile {
            override fun imapHost(): String = "imap.gmail.com"
            override fun imapPort(): Int = 993
            override fun imapSsl(): Boolean = true
            override fun imapStarttls(): Boolean = false
            override fun smtpHost(): String = "smtp.gmail.com"
            override fun smtpPort(): Int = 587
            override fun smtpSsl(): Boolean = false
            override fun smtpStarttls(): Boolean = true
            override fun preferSelectOverExamine(): Boolean = true
            override fun requireTls12Plus(): Boolean = true
        }
    }
    
    /**
     * 创建QQ邮箱配置
     */
    fun createQQProfile(): ProviderProfile {
        return object : ProviderProfile {
            override fun imapHost(): String = "imap.qq.com"
            override fun imapPort(): Int = 993
            override fun imapSsl(): Boolean = true
            override fun imapStarttls(): Boolean = false
            override fun smtpHost(): String = "smtp.qq.com"
            override fun smtpPort(): Int = 587
            override fun smtpSsl(): Boolean = false
            override fun smtpStarttls(): Boolean = true
            override fun preferSelectOverExamine(): Boolean = true
            override fun requireTls12Plus(): Boolean = true
        }
    }
    
    /**
     * 为QQ邮箱应用特殊安全配置
     */
    fun applyQQSecurityConfiguration(properties: Properties, email: String) {
        if (email.contains("@qq.com")) {
            println("🔧 [QQ_SECURITY] Detected QQ email, applying security configuration")
            
            // 1. 强制安全通道
            properties.setProperty("mail.imap.port", "993")
            properties.setProperty("mail.imap.ssl.enable", "true")
            properties.setProperty("mail.imap.starttls.enable", "false")
            properties.setProperty("mail.imap.ssl.protocols", "TLSv1.2 TLSv1.3")
            
            // 2. 强制使用安全认证方式
            properties.setProperty("mail.imap.auth.mechanisms", "LOGIN PLAIN")
            properties.setProperty("mail.imap.auth.login.disable", "false")
            properties.setProperty("mail.imap.auth.plain.disable", "false")
            
            // 3. 禁用不安全的认证方式
            properties.setProperty("mail.imap.auth.ntlm.disable", "true")
            properties.setProperty("mail.imap.auth.digestmd5.disable", "true")
            properties.setProperty("mail.imap.auth.crammd5.disable", "true")
            
            // 4. SSL/TLS 安全配置
            properties.setProperty("mail.imap.ssl.trust", "*")
            properties.setProperty("mail.imap.ssl.checkserveridentity", "false")
            properties.setProperty("mail.imap.ssl.trustall", "true")
            
            // 5. Connection timeout配置
            properties.setProperty("mail.imap.connectiontimeout", "30000")
            properties.setProperty("mail.imap.timeout", "30000")
            properties.setProperty("mail.imap.readtimeout", "30000")
            
            // 6. 禁用PLAIN认证（QQ邮箱可能不支持）
            properties.setProperty("mail.imap.disableplainauth", "true")
            
            println("✅ [QQ_SECURITY] QQ email security configuration applied")
        }
    }
}