# Gfmail API Documentation

## Overview

This document describes the internal APIs and interfaces used within the Gfmail email client application.

## Core Interfaces

### EmailRepository

Central repository for email operations.

```kotlin
interface EmailRepository {
    suspend fun getEmails(accountId: String): List<Email>
    suspend fun getEmailById(id: String): Email?
    suspend fun markAsRead(emailId: String)
    suspend fun markAsUnread(emailId: String)
    suspend fun deleteEmail(emailId: String)
    suspend fun archiveEmail(emailId: String)
    suspend fun syncEmails(accountId: String): Result<Unit>
    suspend fun searchEmails(query: String, accountId: String): List<Email>
}
```

### AccountRepository

Repository for account management operations.

```kotlin
interface AccountRepository {
    suspend fun getAccounts(): List<Account>
    suspend fun getAccountById(id: String): Account?
    suspend fun addAccount(account: Account): Result<Unit>
    suspend fun updateAccount(account: Account): Result<Unit>
    suspend fun deleteAccount(accountId: String): Result<Unit>
    suspend fun testConnection(serverConfig: ServerConfiguration): Result<ConnectionTestResult>
}
```

## Data Models

### Email

Core email data model.

```kotlin
data class Email(
    val id: String,
    val accountId: String,
    val subject: String,
    val sender: String,
    val recipient: String,
    val date: Long,
    val isRead: Boolean,
    val isStarred: Boolean,
    val isArchived: Boolean,
    val bodyText: String?,
    val bodyHtml: String?,
    val originalHtmlBody: String?,
    val attachments: List<Attachment>?,
    val headers: Map<String, String>?
)
```

### Account

Email account information.

```kotlin
data class Account(
    val id: String,
    val email: String,
    val provider: EmailProvider,
    val displayName: String,
    val serverConfig: ServerConfiguration,
    val isActive: Boolean,
    val lastSyncTime: Long?
)
```

### EmailProvider

Supported email providers.

```kotlin
enum class EmailProvider {
    GMAIL,
    QQ,
    NETEASE,
    OUTLOOK,
    YAHOO,
    APPLE
}
```

### ServerConfiguration

Email server configuration.

```kotlin
data class ServerConfiguration(
    val imapHost: String,
    val imapPort: Int,
    val imapEncryption: EncryptionType,
    val smtpHost: String,
    val smtpPort: Int,
    val smtpEncryption: EncryptionType,
    val username: String,
    val password: String,
    val authType: AuthType
)
```

## Use Cases

### GetEmailsUseCase

Retrieves emails for a specific account.

```kotlin
class GetEmailsUseCase(
    private val repository: EmailRepository
) {
    suspend operator fun invoke(accountId: String): Result<List<Email>>
}
```

### SendEmailUseCase

Sends an email.

```kotlin
class SendEmailUseCase(
    private val repository: EmailRepository
) {
    suspend operator fun invoke(email: Email): Result<Unit>
}
```

### SyncEmailsUseCase

Synchronizes emails with the server.

```kotlin
class SyncEmailsUseCase(
    private val repository: EmailRepository
) {
    suspend operator fun invoke(accountId: String): Result<Unit>
}
```

### AddAccountUseCase

Adds a new email account.

```kotlin
class AddAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(account: Account): Result<Unit>
}
```

## Network Clients

### ImapClient

IMAP protocol client for email retrieval.

```kotlin
class ImapClient {
    suspend fun connect(serverConfig: ServerConfiguration): Boolean
    suspend fun disconnect()
    suspend fun getEmails(folder: String = "INBOX"): List<Email>
    suspend fun markAsRead(emailId: String)
    suspend fun markAsUnread(emailId: String)
    suspend fun deleteEmail(emailId: String)
    suspend fun getFolders(): List<String>
    suspend fun createFolder(folderName: String)
    suspend fun deleteFolder(folderName: String)
}
```

### SmtpClient

SMTP protocol client for sending emails.

```kotlin
class SmtpClient {
    suspend fun connect(serverConfig: ServerConfiguration): Boolean
    suspend fun disconnect()
    suspend fun sendEmail(email: Email): Boolean
    suspend fun sendEmailWithAttachments(email: Email, attachments: List<Attachment>): Boolean
}
```

## HTML Processing

### AdvancedHtmlParser

Advanced HTML to text conversion utility.

```kotlin
object AdvancedHtmlParser {
    fun htmlToText(html: String): String
    fun extractTextFromHtml(html: String, charset: String? = null): String
    fun detectCharsetFromHtml(html: String): String
    fun fallbackHtmlToText(html: String): String
}
```

### FormattingVisitor

Custom Jsoup NodeVisitor for HTML formatting.

```kotlin
private class FormattingVisitor : NodeVisitor {
    override fun head(node: Node, depth: Int)
    override fun tail(node: Node, depth: Int)
    private fun append(text: String)
    private fun startNewLine()
    private fun addEmptyLine()
    override fun toString(): String
}
```

## Database Entities

### EmailEntity

Room database entity for emails.

```kotlin
@Entity(tableName = "emails")
data class EmailEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val subject: String,
    val sender: String,
    val recipient: String,
    val date: Long,
    val isRead: Boolean,
    val isStarred: Boolean,
    val isArchived: Boolean,
    val bodyText: String?,
    val bodyHtml: String?,
    val originalHtmlBody: String?,
    val attachments: String?, // JSON
    val headers: String? // JSON
)
```

### AccountEntity

Room database entity for accounts.

```kotlin
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val email: String,
    val provider: String,
    val displayName: String,
    val serverConfig: String, // JSON
    val isActive: Boolean,
    val lastSyncTime: Long?
)
```

## ViewModels

### EmailListViewModel

ViewModel for email list screen.

```kotlin
class EmailListViewModel(
    private val getEmailsUseCase: GetEmailsUseCase,
    private val syncEmailsUseCase: SyncEmailsUseCase
) : ViewModel() {
    val uiState: StateFlow<EmailListUiState>
    fun loadEmails(accountId: String)
    fun refreshEmails(accountId: String)
    fun markAsRead(emailId: String)
    fun deleteEmail(emailId: String)
}
```

### EmailDetailViewModel

ViewModel for email detail screen.

```kotlin
class EmailDetailViewModel(
    private val getEmailUseCase: GetEmailUseCase,
    private val markAsReadUseCase: MarkAsReadUseCase
) : ViewModel() {
    val uiState: StateFlow<EmailDetailUiState>
    fun loadEmail(emailId: String)
    fun markAsRead(emailId: String)
    fun loadRemoteImages(emailId: String)
}
```

## UI States

### EmailListUiState

UI state for email list screen.

```kotlin
sealed class EmailListUiState {
    object Loading : EmailListUiState()
    data class Success(val emails: List<Email>) : EmailListUiState()
    data class Error(val message: String) : EmailListUiState()
}
```

### EmailDetailUiState

UI state for email detail screen.

```kotlin
sealed class EmailDetailUiState {
    object Loading : EmailDetailUiState()
    data class Success(val email: Email) : EmailDetailUiState()
    data class Error(val message: String) : EmailDetailUiState()
}
```

## Provider Configuration

### ProviderConfigService

Service for email provider configuration.

```kotlin
class ProviderConfigService {
    fun detectProvider(email: String): EmailProvider
    fun getServerConfig(provider: EmailProvider): ServerConfiguration
    fun getSetupInstructions(provider: EmailProvider): String
}
```

### NetEaseProviderConfig

Configuration for NetEase email providers.

```kotlin
object NetEaseProviderConfig {
    val IMAP_HOST = "imap.163.com"
    val IMAP_PORT = 993
    val SMTP_HOST = "smtp.163.com"
    val SMTP_PORT = 465
    val ENCRYPTION = EncryptionType.SSL_TLS
}
```

## Error Handling

### Result Types

Generic result wrapper for operations.

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}
```

### ConnectionTestResult

Result of email server connection test.

```kotlin
data class ConnectionTestResult(
    val isSuccess: Boolean,
    val message: String,
    val serverConfig: ServerConfiguration?
)
```

## Utility Classes

### EmailMapper

Maps between domain models and database entities.

```kotlin
object EmailMapper {
    fun toDomain(entity: EmailEntity): Email
    fun toEntity(email: Email): EmailEntity
    fun toDomainList(entities: List<EmailEntity>): List<Email>
    fun toEntityList(emails: List<Email>): List<EmailEntity>
}
```

### AccountMapper

Maps between account domain models and entities.

```kotlin
object AccountMapper {
    fun toDomain(entity: AccountEntity): Account
    fun toEntity(account: Account): AccountEntity
    fun toDomainList(entities: List<AccountEntity>): List<Account>
    fun toEntityList(accounts: List<Account>): List<AccountEntity>
}
```

## Constants

### Email Constants

```kotlin
object EmailConstants {
    const val MAX_EMAIL_SIZE = 25 * 1024 * 1024 // 25MB
    const val MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024 // 10MB
    const val SYNC_INTERVAL_MINUTES = 15
    const val MAX_EMAILS_PER_PAGE = 50
}
```

### Provider Constants

```kotlin
object ProviderConstants {
    const val GMAIL_IMAP_HOST = "imap.gmail.com"
    const val GMAIL_IMAP_PORT = 993
    const val GMAIL_SMTP_HOST = "smtp.gmail.com"
    const val GMAIL_SMTP_PORT = 587
    
    const val QQ_IMAP_HOST = "imap.qq.com"
    const val QQ_IMAP_PORT = 993
    const val QQ_SMTP_HOST = "smtp.qq.com"
    const val QQ_SMTP_PORT = 587
}
```

## Extension Functions

### String Extensions

```kotlin
fun String.isValidEmail(): Boolean
fun String.extractDomain(): String
fun String.sanitizeForDisplay(): String
```

### Email Extensions

```kotlin
fun Email.getDisplayDate(): String
fun Email.getSenderDisplayName(): String
fun Email.hasAttachments(): Boolean
fun Email.isHtml(): Boolean
```

## Testing Utilities

### Test Data Builders

```kotlin
object TestDataBuilder {
    fun createTestEmail(id: String = "test-id"): Email
    fun createTestAccount(id: String = "test-account"): Account
    fun createTestServerConfig(): ServerConfiguration
}
```

### Mock Repositories

```kotlin
class MockEmailRepository : EmailRepository {
    private val emails = mutableListOf<Email>()
    
    override suspend fun getEmails(accountId: String): List<Email> = emails
    override suspend fun getEmailById(id: String): Email? = emails.find { it.id == id }
    // ... other implementations
}
```

---

*This API documentation is maintained by the Gfmail development team and should be updated as the APIs evolve.*
