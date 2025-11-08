package com.gf.mail.data.provider

import com.gf.mail.domain.model.*

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Email provider configuration service
 * Contains provider-specific configurations, server settings, and authentication methods
 */
@Singleton
class ProviderConfigService @Inject constructor() {

    companion object {
        // Well-known email providers and their configurations
        private val PROVIDER_CONFIGS = mapOf(
            EmailProvider.GMAIL to GmailProviderConfig(),
            EmailProvider.EXCHANGE to ExchangeProviderConfig(),
            EmailProvider.QQ to QQProviderConfig(),
            EmailProvider.NETEASE to NetEaseProviderConfig(),
            EmailProvider.OUTLOOK to OutlookProviderConfig(),
            EmailProvider.YAHOO to YahooProviderConfig(),
            EmailProvider.APPLE to AppleProviderConfig(),
            EmailProvider.IMAP to GenericImapProviderConfig(),
            EmailProvider.POP3 to GenericPop3ProviderConfig()
        )
    }

    /**
     * Get provider configuration for the given email provider
     */
    fun getProviderConfig(provider: EmailProvider): ProviderConfig {
        return PROVIDER_CONFIGS[provider] ?: GenericImapProviderConfig()
    }

    /**
     * Auto-detect provider from email address
     */
    fun detectProvider(emailAddress: String): EmailProvider {
        val domain = emailAddress.substringAfter("@").lowercase()

        return when {
            // Gmail
            domain.contains("gmail.com") || domain.contains("googlemail.com") -> EmailProvider.GMAIL
            
            // Microsoft/Outlook
            domain.contains("outlook.com") || domain.contains("hotmail.com") ||
                domain.contains("live.com") || domain.contains("msn.com") -> EmailProvider.OUTLOOK
            
            // QQ邮箱
            domain.contains("qq.com") -> EmailProvider.QQ
            
            // 网易邮箱
            domain.contains("163.com") || domain.contains("126.com") || domain.contains("188.com") -> EmailProvider.NETEASE
            
            // Yahoo
            domain.contains("yahoo.com") || domain.contains("ymail.com") || domain.contains("rocketmail.com") -> EmailProvider.YAHOO
            
            // Apple iCloud
            domain.contains("icloud.com") || domain.contains("me.com") || domain.contains("mac.com") -> EmailProvider.APPLE
            
            // Exchange (企业邮箱)
            domain.contains("office365.com") || domain.contains("exchange") -> EmailProvider.EXCHANGE
            
            else -> EmailProvider.IMAP // Default to generic IMAP
        }
    }

    /**
     * Get server configuration for provider
     */
    fun getServerConfiguration(provider: EmailProvider, customConfig: ServerConfiguration? = null): ServerConfiguration {
        if (customConfig != null) {
            return customConfig
        }

        val config = getProviderConfig(provider)
        return config.getDefaultServerConfiguration()
    }

    /**
     * Get provider-specific authentication hint
     */
    fun getAuthHint(provider: EmailProvider): String {
        return when (provider) {
            EmailProvider.GMAIL -> "Gmail需要开启两步验证并生成应用专用密码"
            EmailProvider.QQ -> "QQ邮箱需要在网页端开启IMAP/SMTP服务并使用授权码"
            EmailProvider.NETEASE -> "网易邮箱需要在网页端开启IMAP/SMTP服务并使用授权码"
            EmailProvider.OUTLOOK -> "Outlook支持OAuth2或应用专用密码"
            EmailProvider.YAHOO -> "Yahoo邮箱需要生成应用专用密码"
            EmailProvider.APPLE -> "iCloud需要生成应用专用密码"
            EmailProvider.EXCHANGE -> "Exchange邮箱通常使用OAuth2或企业密码"
            else -> "请使用正确的邮箱密码或授权码"
        }
    }
    
    /**
     * Get provider-specific connection error guidance
     */
    fun getConnectionErrorGuidance(provider: EmailProvider, error: String): String {
        return when {
            error.contains("authentication") || error.contains("login") || error.contains("535") -> {
                when (provider) {
                    EmailProvider.GMAIL -> "Gmail认证失败。请检查：\n1. 是否开启了两步验证\n2. 是否生成了应用专用密码\n3. 是否使用了应用专用密码而不是登录密码"
                    EmailProvider.QQ -> "QQ邮箱认证失败。请检查：\n1. 是否在网页端开启了IMAP/SMTP服务\n2. 是否使用了授权码而不是登录密码\n3. 授权码是否有效"
                    EmailProvider.NETEASE -> "网易邮箱认证失败。请检查：\n1. 是否在网页端开启了IMAP/SMTP服务\n2. 是否使用了授权码而不是登录密码\n3. 授权码是否有效"
                    EmailProvider.OUTLOOK -> "Outlook认证失败。请检查：\n1. 是否使用了正确的密码\n2. 是否开启了应用专用密码\n3. 是否支持OAuth2登录"
                    else -> "认证失败，请检查邮箱地址和密码是否正确"
                }
            }
            error.contains("timeout") || error.contains("connect") -> {
                "连接超时。请检查：\n1. 网络连接是否正常\n2. 防火墙设置\n3. 服务器地址和端口是否正确"
            }
            else -> "连接失败，请检查网络连接和账号设置"
        }
    }

    /**
     * Check if provider supports OAuth2
     */
    fun supportsOAuth2(provider: EmailProvider): Boolean {
        return getProviderConfig(provider).supportsOAuth2
    }

    /**
     * Check if provider supports app-specific passwords
     */
    fun supportsAppPasswords(provider: EmailProvider): Boolean {
        return getProviderConfig(provider).supportsAppPasswords
    }

    /**
     * Get OAuth2 scopes for provider
     */
    fun getOAuth2Scopes(provider: EmailProvider): List<String> {
        return getProviderConfig(provider).oauth2Scopes
    }

    /**
     * Get setup instructions for provider
     */
    fun getSetupInstructions(provider: EmailProvider): ProviderSetupInstructions {
        return getProviderConfig(provider).getSetupInstructions()
    }
}

/**
 * Base provider configuration interface
 */
abstract class ProviderConfig {
    abstract val displayName: String
    abstract val supportsOAuth2: Boolean
    abstract val supportsAppPasswords: Boolean
    abstract val oauth2Scopes: List<String>
    abstract val requiresTwoFactorAuth: Boolean

    abstract fun getDefaultServerConfiguration(): ServerConfiguration
    abstract fun getSetupInstructions(): ProviderSetupInstructions
}

/**
 * Gmail provider configuration
 */
class GmailProviderConfig : ProviderConfig() {
    override val displayName = "Gmail"
    override val supportsOAuth2 = true
    override val supportsAppPasswords = true
    override val oauth2Scopes = listOf(
        "https://www.googleapis.com/auth/gmail.readonly",
        "https://www.googleapis.com/auth/gmail.send",
        "https://www.googleapis.com/auth/gmail.modify"
    )
    override val requiresTwoFactorAuth = false // OAuth2 recommended but not required

    override fun getDefaultServerConfiguration(): ServerConfiguration {
        return ServerConfiguration(
            imapHost = "imap.gmail.com",
            imapPort = 993,
            imapEncryption = EncryptionType.SSL,
            smtpHost = "smtp.gmail.com",
            smtpPort = 587,
            smtpEncryption = EncryptionType.STARTTLS
        )
    }

    override fun getSetupInstructions(): ProviderSetupInstructions {
        return ProviderSetupInstructions(
            provider = EmailProvider.GMAIL,
            title = "Gmail Setup Instructions",
            steps = listOf(
                "For best security, use OAuth2 authentication (recommended)",
                "Alternative: Enable 2-factor authentication in your Google account",
                "Generate an app-specific password in Google Account settings",
                "Use your Gmail address and the app-specific password",
                "IMAP must be enabled in Gmail settings"
            ),
            additionalInfo = "Gmail requires either OAuth2 or app-specific passwords for third-party email clients.",
            helpUrl = "https://support.google.com/accounts/answer/185833"
        )
    }
}

/**
 * Exchange/Outlook provider configuration
 */
class ExchangeProviderConfig : ProviderConfig() {
    override val displayName = "Outlook/Exchange"
    override val supportsOAuth2 = true
    override val supportsAppPasswords = false // Microsoft deprecated basic auth
    override val oauth2Scopes = listOf(
        "https://outlook.office.com/IMAP.AccessAsUser.All",
        "https://outlook.office.com/SMTP.Send",
        "offline_access"
    )
    override val requiresTwoFactorAuth = true // Microsoft requires OAuth2

    override fun getDefaultServerConfiguration(): ServerConfiguration {
        return ServerConfiguration(
            imapHost = "outlook.office365.com",
            imapPort = 993,
            imapEncryption = EncryptionType.SSL,
            smtpHost = "smtp-mail.outlook.com",
            smtpPort = 587,
            smtpEncryption = EncryptionType.STARTTLS
        )
    }

    override fun getSetupInstructions(): ProviderSetupInstructions {
        return ProviderSetupInstructions(
            provider = EmailProvider.EXCHANGE,
            title = "Outlook/Exchange Setup Instructions",
            steps = listOf(
                "OAuth2 authentication is required for Microsoft accounts",
                "Click 'Sign in with Microsoft' to authenticate",
                "Grant permission for email access when prompted",
                "Your account will be automatically configured"
            ),
            additionalInfo = "Microsoft requires OAuth2 authentication for all third-party email clients. Basic password authentication is no longer supported.",
            helpUrl = "https://support.microsoft.com/en-us/office/pop-imap-and-smtp-settings-8361e398-8af4-4e97-b147-6c6c4ac95353"
        )
    }
}

/**
 * Generic IMAP provider configuration
 */
class GenericImapProviderConfig : ProviderConfig() {
    override val displayName = "IMAP"
    override val supportsOAuth2 = false
    override val supportsAppPasswords = false
    override val oauth2Scopes = emptyList<String>()
    override val requiresTwoFactorAuth = false

    override fun getDefaultServerConfiguration(): ServerConfiguration {
        return ServerConfiguration(
            imapHost = null, // User must specify
            imapPort = 993,
            imapEncryption = EncryptionType.SSL,
            smtpHost = null, // User must specify
            smtpPort = 587,
            smtpEncryption = EncryptionType.STARTTLS
        )
    }

    override fun getSetupInstructions(): ProviderSetupInstructions {
        return ProviderSetupInstructions(
            provider = EmailProvider.IMAP,
            title = "IMAP Setup Instructions",
            steps = listOf(
                "Contact your email provider for IMAP server settings",
                "Enter the IMAP server hostname and port",
                "Choose the appropriate encryption method (SSL/STARTTLS)",
                "Enter SMTP server settings for sending emails",
                "Use your email address and password to authenticate"
            ),
            additionalInfo = "IMAP settings vary by provider. Contact your email provider or IT administrator for the correct server settings.",
            helpUrl = null
        )
    }
}

/**
 * Generic POP3 provider configuration
 */
class GenericPop3ProviderConfig : ProviderConfig() {
    override val displayName = "POP3"
    override val supportsOAuth2 = false
    override val supportsAppPasswords = false
    override val oauth2Scopes = emptyList<String>()
    override val requiresTwoFactorAuth = false

    override fun getDefaultServerConfiguration(): ServerConfiguration {
        return ServerConfiguration(
            imapHost = null, // POP3 doesn't use IMAP
            imapPort = 995,
            imapEncryption = EncryptionType.SSL,
            smtpHost = null,
            smtpPort = 587,
            smtpEncryption = EncryptionType.STARTTLS
        )
    }

    override fun getSetupInstructions(): ProviderSetupInstructions {
        return ProviderSetupInstructions(
            provider = EmailProvider.POP3,
            title = "POP3 Setup Instructions",
            steps = listOf(
                "Contact your email provider for POP3 server settings",
                "Enter the POP3 server hostname and port",
                "Choose the appropriate encryption method (SSL/STARTTLS)",
                "Enter SMTP server settings for sending emails",
                "Use your email address and password to authenticate"
            ),
            additionalInfo = "POP3 downloads emails to your device and removes them from the server by default. IMAP is recommended for multi-device access.",
            helpUrl = null
        )
    }
}

/**
 * QQ邮箱 provider configuration
 */
class QQProviderConfig : ProviderConfig() {
    override val displayName = "QQ邮箱"
    override val supportsOAuth2 = false
    override val supportsAppPasswords = true
    override val oauth2Scopes = emptyList<String>()
    override val requiresTwoFactorAuth = false

    override fun getDefaultServerConfiguration(): ServerConfiguration {
        return ServerConfiguration(
            imapHost = "imap.qq.com",
            imapPort = 993,
            imapEncryption = EncryptionType.SSL,
            smtpHost = "smtp.qq.com",
            smtpPort = 587,
            smtpEncryption = EncryptionType.STARTTLS
        )
    }

    override fun getSetupInstructions(): ProviderSetupInstructions {
        return ProviderSetupInstructions(
            provider = EmailProvider.QQ,
            title = "QQ邮箱设置说明",
            steps = listOf(
                "登录QQ邮箱网页版",
                "设置 → 账户 → 开启IMAP/SMTP服务",
                "生成授权码并使用授权码作为密码",
                "确保使用正确的服务器地址和端口"
            ),
            additionalInfo = "QQ邮箱必须使用授权码，不能使用登录密码。授权码在网页端生成。",
            helpUrl = "https://service.mail.qq.com/cgi-bin/help?subtype=1&id=28&no=1001256"
        )
    }
}

/**
 * 网易邮箱 provider configuration
 */
class NetEaseProviderConfig : ProviderConfig() {
    override val displayName = "网易邮箱"
    override val supportsOAuth2 = false
    override val supportsAppPasswords = true
    override val oauth2Scopes = emptyList<String>()
    override val requiresTwoFactorAuth = false

    override fun getDefaultServerConfiguration(): ServerConfiguration {
        return ServerConfiguration(
            imapHost = "imap.163.com",
            imapPort = 993,
            imapEncryption = EncryptionType.SSL,
            smtpHost = "smtp.163.com",
            smtpPort = 465,
            smtpEncryption = EncryptionType.SSL
        )
    }

    override fun getSetupInstructions(): ProviderSetupInstructions {
        return ProviderSetupInstructions(
            provider = EmailProvider.NETEASE,
            title = "网易邮箱设置说明",
            steps = listOf(
                "登录网易邮箱网页版",
                "设置 → POP3/SMTP/IMAP → 开启IMAP/SMTP服务",
                "生成授权码并使用授权码作为密码",
                "确保使用正确的服务器地址和端口"
            ),
            additionalInfo = "网易邮箱建议使用授权码，更安全且稳定。",
            helpUrl = "https://help.mail.163.com/faqDetail.do?code=d7a5dc8471cd0c0e8b4b8f4f8e49998b374173cfe9171302fa1be630658755472"
        )
    }
}

/**
 * Outlook provider configuration
 */
class OutlookProviderConfig : ProviderConfig() {
    override val displayName = "Outlook"
    override val supportsOAuth2 = true
    override val supportsAppPasswords = true
    override val oauth2Scopes = listOf(
        "https://outlook.office.com/IMAP.AccessAsUser.All",
        "https://outlook.office.com/SMTP.Send",
        "offline_access"
    )
    override val requiresTwoFactorAuth = false

    override fun getDefaultServerConfiguration(): ServerConfiguration {
        return ServerConfiguration(
            imapHost = "outlook.office365.com",
            imapPort = 993,
            imapEncryption = EncryptionType.SSL,
            smtpHost = "smtp-mail.outlook.com",
            smtpPort = 587,
            smtpEncryption = EncryptionType.STARTTLS
        )
    }

    override fun getSetupInstructions(): ProviderSetupInstructions {
        return ProviderSetupInstructions(
            provider = EmailProvider.OUTLOOK,
            title = "Outlook设置说明",
            steps = listOf(
                "推荐使用OAuth2认证（最安全）",
                "或者开启两步验证并生成应用专用密码",
                "确保IMAP已启用",
                "使用正确的服务器地址和端口"
            ),
            additionalInfo = "Outlook支持OAuth2和应用专用密码两种认证方式。",
            helpUrl = "https://support.microsoft.com/en-us/office/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040"
        )
    }
}

/**
 * Yahoo provider configuration
 */
class YahooProviderConfig : ProviderConfig() {
    override val displayName = "Yahoo"
    override val supportsOAuth2 = true
    override val supportsAppPasswords = true
    override val oauth2Scopes = listOf(
        "https://mail.yahooapis.com/",
        "offline_access"
    )
    override val requiresTwoFactorAuth = false

    override fun getDefaultServerConfiguration(): ServerConfiguration {
        return ServerConfiguration(
            imapHost = "imap.mail.yahoo.com",
            imapPort = 993,
            imapEncryption = EncryptionType.SSL,
            smtpHost = "smtp.mail.yahoo.com",
            smtpPort = 587,
            smtpEncryption = EncryptionType.STARTTLS
        )
    }

    override fun getSetupInstructions(): ProviderSetupInstructions {
        return ProviderSetupInstructions(
            provider = EmailProvider.YAHOO,
            title = "Yahoo邮箱设置说明",
            steps = listOf(
                "推荐使用OAuth2认证",
                "或者开启两步验证并生成应用专用密码",
                "确保IMAP已启用",
                "使用正确的服务器地址和端口"
            ),
            additionalInfo = "Yahoo邮箱支持OAuth2和应用专用密码。",
            helpUrl = "https://help.yahoo.com/kb/SLN4075.html"
        )
    }
}

/**
 * Apple iCloud provider configuration
 */
class AppleProviderConfig : ProviderConfig() {
    override val displayName = "iCloud"
    override val supportsOAuth2 = true
    override val supportsAppPasswords = true
    override val oauth2Scopes = listOf(
        "https://www.icloud.com/mail/",
        "offline_access"
    )
    override val requiresTwoFactorAuth = true

    override fun getDefaultServerConfiguration(): ServerConfiguration {
        return ServerConfiguration(
            imapHost = "imap.mail.me.com",
            imapPort = 993,
            imapEncryption = EncryptionType.SSL,
            smtpHost = "smtp.mail.me.com",
            smtpPort = 587,
            smtpEncryption = EncryptionType.STARTTLS
        )
    }

    override fun getSetupInstructions(): ProviderSetupInstructions {
        return ProviderSetupInstructions(
            provider = EmailProvider.APPLE,
            title = "iCloud邮箱设置说明",
            steps = listOf(
                "推荐使用OAuth2认证",
                "或者开启两步验证并生成应用专用密码",
                "确保IMAP已启用",
                "使用正确的服务器地址和端口"
            ),
            additionalInfo = "iCloud邮箱需要开启两步验证。",
            helpUrl = "https://support.apple.com/en-us/HT202304"
        )
    }
}

/**
 * Provider setup instructions
 */
data class ProviderSetupInstructions(
    val provider: EmailProvider,
    val title: String,
    val steps: List<String>,
    val additionalInfo: String,
    val helpUrl: String? = null
)

/**
 * Common email provider domains for auto-detection
 */
object CommonProviderDomains {
    val GMAIL_DOMAINS = setOf(
        "gmail.com",
        "googlemail.com"
    )

    val MICROSOFT_DOMAINS = setOf(
        "outlook.com",
        "hotmail.com",
        "live.com",
        "msn.com"
    )

    val YAHOO_DOMAINS = setOf(
        "yahoo.com",
        "ymail.com",
        "rocketmail.com"
    )

    val APPLE_DOMAINS = setOf(
        "icloud.com",
        "me.com",
        "mac.com"
    )

    fun detectProviderFromDomain(domain: String): EmailProvider? {
        val lowerDomain = domain.lowercase()

        return when {
            GMAIL_DOMAINS.any { lowerDomain.contains(it) } -> EmailProvider.GMAIL
            MICROSOFT_DOMAINS.any { lowerDomain.contains(it) } -> EmailProvider.EXCHANGE
            YAHOO_DOMAINS.any { lowerDomain.contains(it) } -> EmailProvider.IMAP
            APPLE_DOMAINS.any { lowerDomain.contains(it) } -> EmailProvider.IMAP
            else -> null // Unknown provider
        }
    }
}
