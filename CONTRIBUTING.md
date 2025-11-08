# Contributing to Gfmail

Thank you for your interest in contributing to Gfmail! This document provides guidelines and information for contributors.

## 🚀 Getting Started

### Prerequisites

- **Android Studio Hedgehog** or higher
- **JDK 17** or higher
- **Git** for version control
- **Android SDK** with API 24+

### Development Setup

1. **Fork and Clone**
   ```bash
   git clone https://github.com/[YOUR_USERNAME]/gfmail.git
   cd gfmail
   ```

2. **Open in Android Studio**
   - Import the project
   - Sync Gradle files
   - Wait for indexing to complete

3. **Run Tests**
   ```bash
   ./gradlew test
   ```

## 📋 How to Contribute

### Reporting Issues

Before creating an issue, please:

1. **Search existing issues** to avoid duplicates
2. **Use the issue template** provided
3. **Include relevant information**:
   - Android version
   - Device model
   - App version
   - Steps to reproduce
   - Expected vs actual behavior

### Suggesting Features

1. **Check existing feature requests**
2. **Use the feature request template**
3. **Provide detailed description**:
   - Use case
   - Benefits
   - Implementation ideas (optional)

### Code Contributions

#### 1. Choose an Issue

- Look for issues labeled `good first issue` for beginners
- Check `help wanted` for more complex tasks
- Comment on the issue to claim it

#### 2. Create a Branch

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/issue-number-description
```

#### 3. Make Changes

Follow our coding standards:

- **Kotlin Style**: Follow [official conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Compose Guidelines**: Use Material Design 3 principles
- **Architecture**: Follow MVVM pattern
- **Testing**: Write unit tests for new features

#### 4. Test Your Changes

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Check code style
./gradlew ktlintCheck

# Run static analysis
./gradlew detekt
```

#### 5. Commit Changes

Use conventional commit messages:

```
feat: add email search functionality
fix: resolve WebView height calculation issue
docs: update README with new features
test: add unit tests for EmailRepository
```

#### 6. Push and Create PR

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request with:
- Clear title and description
- Reference related issues
- Include screenshots (for UI changes)
- Ensure all checks pass

## 🎨 Code Style Guidelines

### Kotlin

```kotlin
// Use camelCase for variables and functions
val emailAddress = "user@example.com"
fun sendEmail() { }

// Use PascalCase for classes
class EmailRepository { }

// Use UPPER_SNAKE_CASE for constants
companion object {
    const val MAX_EMAIL_SIZE = 25 * 1024 * 1024
}

// Prefer immutable data
val emails: List<Email> = repository.getEmails()

// Use meaningful names
val unreadEmailCount = emails.count { !it.isRead }
```

### Compose UI

```kotlin
@Composable
fun EmailListItem(
    email: Email,
    onEmailClick: (Email) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEmailClick(email) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // Content
    }
}
```

### Architecture

```kotlin
// Repository pattern
interface EmailRepository {
    suspend fun getEmails(): List<Email>
    suspend fun markAsRead(emailId: String)
}

// Use case pattern
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

## 🧪 Testing Guidelines

### Unit Tests

```kotlin
@Test
fun `when getEmails is called, should return list of emails`() = runTest {
    // Given
    val expectedEmails = listOf(createTestEmail())
    coEvery { mockRepository.getEmails() } returns expectedEmails
    
    // When
    val result = useCase()
    
    // Then
    assertTrue(result.isSuccess)
    assertEquals(expectedEmails, result.getOrNull())
}
```

### UI Tests

```kotlin
@Test
fun emailList_displaysEmails() {
    composeTestRule.setContent {
        EmailListScreen(emails = testEmails)
    }
    
    composeTestRule
        .onNodeWithText("Test Subject")
        .assertIsDisplayed()
}
```

## 📁 Project Structure

```
app/src/main/java/com/gf/mail/
├── data/                    # Data layer
│   ├── local/              # Room database
│   ├── remote/             # Network services
│   └── repository/         # Repository implementations
├── domain/                 # Business logic
│   ├── model/              # Domain models
│   └── usecase/            # Use cases
├── presentation/           # UI layer
│   ├── ui/                 # Compose screens
│   ├── viewmodel/          # ViewModels
│   └── navigation/         # Navigation
├── di/                     # Dependency injection
└── utils/                  # Utility classes
```

## 🔍 Code Review Process

### For Contributors

1. **Self-review** your code before submitting
2. **Ensure tests pass** and coverage is maintained
3. **Update documentation** if needed
4. **Respond to feedback** promptly

### For Reviewers

1. **Check functionality** and edge cases
2. **Verify code style** and architecture
3. **Ensure tests** are adequate
4. **Provide constructive feedback**

## 🐛 Bug Reports

When reporting bugs, include:

```markdown
**Bug Description**
A clear description of the bug.

**Steps to Reproduce**
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected Behavior**
What you expected to happen.

**Actual Behavior**
What actually happened.

**Environment**
- Android Version: [e.g., 13]
- Device: [e.g., Pixel 7]
- App Version: [e.g., 1.0.0]

**Screenshots**
If applicable, add screenshots.

**Additional Context**
Any other context about the problem.
```

## ✨ Feature Requests

When suggesting features:

```markdown
**Feature Description**
A clear description of the feature.

**Use Case**
Why is this feature needed?

**Proposed Solution**
How should this feature work?

**Alternatives**
Any alternative solutions considered.

**Additional Context**
Any other context or screenshots.
```

## 📚 Resources

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Material Design 3](https://m3.material.io/)
- [Jetpack Compose Guidelines](https://developer.android.com/jetpack/compose)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)

## 🤝 Community Guidelines

- **Be respectful** and inclusive
- **Help others** learn and grow
- **Follow the code of conduct**
- **Provide constructive feedback**

## 📞 Getting Help

- **GitHub Discussions** - General questions
- **GitHub Issues** - Bug reports and feature requests
- **Discord** - Real-time chat (if available)

## 🏆 Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes
- Project documentation

Thank you for contributing to Gfmail! 🎉
