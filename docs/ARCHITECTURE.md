# Gfmail Architecture Documentation

## Overview

Gfmail follows the **MVVM (Model-View-ViewModel)** architecture pattern with clean separation of concerns. The architecture is designed to be scalable, maintainable, and testable.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   Compose UI    │  │   ViewModels    │  │ Navigation  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   Use Cases     │  │   Models        │  │ Interfaces  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   Repository    │  │   Local DB      │  │   Remote    │ │
│  │  Implementation │  │   (Room)        │  │   (IMAP)    │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Layer Details

### 1. Presentation Layer

**Location**: `app/src/main/java/com/gf/mail/presentation/`

**Components**:
- **Compose UI**: Modern declarative UI using Jetpack Compose
- **ViewModels**: State management and business logic coordination
- **Navigation**: Screen navigation and deep linking

**Key Classes**:
```kotlin
// UI Screens
EmailListScreen
EmailDetailScreen
AddAccountScreen
AccountListScreen

// ViewModels
EmailListViewModel
EmailDetailViewModel
AddAccountViewModel

// Navigation
GfmailNavGraph
```

### 2. Domain Layer

**Location**: `app/src/main/java/com/gf/mail/domain/`

**Components**:
- **Use Cases**: Business logic and application rules
- **Models**: Domain entities and data classes
- **Interfaces**: Repository contracts and abstractions

**Key Classes**:
```kotlin
// Use Cases
GetEmailsUseCase
SendEmailUseCase
SyncEmailsUseCase
AddAccountUseCase

// Models
Email
Account
EmailProvider
ServerConfiguration

// Interfaces
EmailRepository
AccountRepository
```

### 3. Data Layer

**Location**: `app/src/main/java/com/gf/mail/data/`

**Components**:
- **Repository Implementation**: Data source coordination
- **Local Database**: Room database for offline storage
- **Remote Services**: IMAP/SMTP clients for email operations

**Key Classes**:
```kotlin
// Repository Implementation
EmailRepositoryImpl
AccountRepositoryImpl

// Local Database
GfmailDatabase
EmailEntity
AccountEntity

// Remote Services
ImapClient
SmtpClient
ProviderConfigService
```

## Data Flow

### Email Loading Flow

```
1. UI (EmailListScreen)
   ↓
2. ViewModel (EmailListViewModel)
   ↓
3. Use Case (GetEmailsUseCase)
   ↓
4. Repository (EmailRepositoryImpl)
   ↓
5. Data Sources (Local DB + IMAP Client)
   ↓
6. Response back through layers
```

### Email Sync Flow

```
1. Background Service
   ↓
2. Sync Use Case
   ↓
3. IMAP Client
   ↓
4. Parse & Store
   ↓
5. Update UI via Repository
```

## Key Design Patterns

### 1. Repository Pattern

Centralizes data access logic and provides a clean API for data operations.

```kotlin
interface EmailRepository {
    suspend fun getEmails(): List<Email>
    suspend fun getEmailById(id: String): Email?
    suspend fun markAsRead(emailId: String)
    suspend fun syncEmails()
}
```

### 2. Use Case Pattern

Encapsulates business logic and application rules.

```kotlin
class GetEmailsUseCase(
    private val repository: EmailRepository
) {
    suspend operator fun invoke(): Result<List<Email>> {
        return try {
            val emails = repository.getEmails()
            Result.success(emails)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 3. MVVM Pattern

Separates UI logic from business logic.

```kotlin
@Composable
fun EmailListScreen(
    viewModel: EmailListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState) {
        is EmailListUiState.Loading -> LoadingScreen()
        is EmailListUiState.Success -> EmailListContent(uiState.emails)
        is EmailListUiState.Error -> ErrorScreen(uiState.message)
    }
}
```

## Dependency Injection

**Location**: `app/src/main/java/com/gf/mail/di/`

Gfmail uses manual dependency injection with a custom container:

```kotlin
class AppDependencyContainer {
    // Database
    private val database by lazy { createDatabase() }
    
    // Repositories
    val emailRepository by lazy { EmailRepositoryImpl(database, imapClient) }
    
    // Use Cases
    val getEmailsUseCase by lazy { GetEmailsUseCase(emailRepository) }
    
    // ViewModels
    fun createEmailListViewModel(): EmailListViewModel {
        return EmailListViewModel(getEmailsUseCase)
    }
}
```

## Database Schema

### EmailEntity
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
    val bodyText: String?,
    val bodyHtml: String?,
    val originalHtmlBody: String?,
    val attachments: String? // JSON
)
```

### AccountEntity
```kotlin
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val email: String,
    val provider: String,
    val displayName: String,
    val serverConfig: String, // JSON
    val isActive: Boolean
)
```

## Network Architecture

### IMAP Client
```kotlin
class ImapClient {
    suspend fun connect(serverConfig: ServerConfiguration): Boolean
    suspend fun getEmails(folder: String): List<Email>
    suspend fun markAsRead(emailId: String)
    suspend fun disconnect()
}
```

### SMTP Client
```kotlin
class SmtpClient {
    suspend fun connect(serverConfig: ServerConfiguration): Boolean
    suspend fun sendEmail(email: Email): Boolean
    suspend fun disconnect()
}
```

## Error Handling

### Result Pattern
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}
```

### UI Error States
```kotlin
sealed class EmailListUiState {
    object Loading : EmailListUiState()
    data class Success(val emails: List<Email>) : EmailListUiState()
    data class Error(val message: String) : EmailListUiState()
}
```

## Testing Strategy

### Unit Tests
- **Use Cases**: Test business logic
- **Repository**: Test data operations
- **ViewModels**: Test state management

### Integration Tests
- **Database**: Test Room operations
- **Network**: Test IMAP/SMTP clients
- **End-to-End**: Test complete flows

### UI Tests
- **Compose**: Test UI components
- **Navigation**: Test screen transitions
- **User Flows**: Test complete user journeys

## Performance Considerations

### Memory Management
- **WebView**: Proper lifecycle management
- **Images**: Efficient loading and caching
- **Database**: Query optimization

### Network Optimization
- **Connection Pooling**: Reuse IMAP/SMTP connections
- **Batch Operations**: Group multiple operations
- **Background Sync**: Efficient background processing

### UI Performance
- **Lazy Loading**: Load content on demand
- **State Management**: Minimize recomposition
- **Animation**: Smooth transitions

## Security Architecture

### Data Protection
- **Encryption**: Local database encryption
- **Authentication**: Secure credential storage
- **Network**: TLS/SSL for all connections

### Privacy
- **No Tracking**: No user data collection
- **Local Storage**: All data stored locally
- **Secure Transmission**: Encrypted email protocols

## Future Architecture Considerations

### Scalability
- **Modularization**: Split into feature modules
- **Plugin System**: Support for email providers
- **Microservices**: Backend service integration

### Performance
- **Caching**: Advanced caching strategies
- **Background Processing**: WorkManager integration
- **Database Optimization**: Advanced indexing

### Maintainability
- **Code Generation**: Reduce boilerplate
- **Testing**: Comprehensive test coverage
- **Documentation**: Keep architecture docs updated

---

*This architecture documentation is maintained by the Gfmail development team and should be updated as the architecture evolves.*
