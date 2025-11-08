# Gfmail Quick Start Guide

## 🚀 Get Started in 5 Minutes

This guide will help you get Gfmail up and running quickly on your development machine.

## Prerequisites

- **Android Studio Hedgehog** (2023.1.1) or higher
- **JDK 17** or higher
- **Android SDK** with API 24+
- **Git** for version control

## Installation

### 1. Clone the Repository

```bash
   git clone https://github.com/[YOUR_USERNAME]/gfmail.git
cd gfmail
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select "Open an existing project"
3. Navigate to the cloned `gfmail` directory
4. Click "OK"

### 3. Sync Project

Android Studio will automatically:
- Download dependencies
- Sync Gradle files
- Index the project

Wait for the sync to complete (usually 2-3 minutes).

## Running the App

### Debug Build

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Using Android Studio

1. **Connect Device**: Connect your Android device via USB
2. **Enable USB Debugging**: In device settings
3. **Run**: Click the "Run" button (green play icon)
4. **Select Device**: Choose your connected device

## Adding Your First Email Account

### Gmail Setup

1. **Enable 2FA**: In your Google Account settings
2. **Generate App Password**: 
   - Go to Google Account → Security → App passwords
   - Generate password for "Mail"
3. **Add Account in App**:
   - Tap "Add Account"
   - Select "Gmail"
   - Enter your email and app password

### 163/126/yeah.net Setup

1. **Enable IMAP**: In your email settings
2. **Add Account in App**:
   - Tap "Add Account"
   - Select "163/126/yeah.net"
   - Enter your email and password

### QQ Mail Setup

1. **Enable IMAP**: In QQ Mail settings
2. **Generate Authorization Code**:
   - Go to QQ Mail → Settings → Account
   - Enable IMAP/SMTP service
   - Generate authorization code
3. **Add Account in App**:
   - Tap "Add Account"
   - Select "QQ Mail"
   - Enter your email and authorization code

## Testing Features

### Email Reading

1. **Open Email**: Tap any email in the list
2. **View Content**: Scroll through email content
3. **Load Images**: Tap "Load Remote Images" if needed
4. **Mark as Read**: Email automatically marked as read

### Email Management

1. **Mark as Unread**: Long-press email → "Mark as Unread"
2. **Archive**: Swipe left on email
3. **Delete**: Swipe right on email
4. **Search**: Use search bar at top

### Account Management

1. **Switch Accounts**: Use account switcher in sidebar
2. **Add More Accounts**: Tap "Add Account"
3. **Account Settings**: Long-press account → Settings

## Development Workflow

### Making Changes

1. **Create Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Changes**: Edit code in Android Studio

3. **Test Changes**:
   ```bash
   ./gradlew test
   ./gradlew assembleDebug
   ```

4. **Commit Changes**:
   ```bash
   git add .
   git commit -m "Add your feature"
   git push origin feature/your-feature-name
   ```

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# All tests
./gradlew check
```

### Code Quality

```bash
# Lint check
./gradlew lint

# Format code
./gradlew ktlintFormat

# Static analysis
./gradlew detekt
```

## Project Structure

```
app/src/main/java/com/gf/mail/
├── data/           # Data layer (database, network)
├── domain/         # Business logic (use cases, models)
├── presentation/   # UI layer (screens, viewmodels)
├── di/            # Dependency injection
└── utils/         # Utility classes
```

## Key Files

### Main Activity
- `MainActivity.kt` - App entry point

### Email Screens
- `EmailListScreen.kt` - Email list display
- `EmailDetailScreen.kt` - Email content view
- `AddAccountScreen.kt` - Account setup

### Data Layer
- `EmailRepositoryImpl.kt` - Email data operations
- `ImapClient.kt` - IMAP protocol client
- `GfmailDatabase.kt` - Room database

### Domain Layer
- `GetEmailsUseCase.kt` - Business logic
- `Email.kt` - Domain model
- `EmailRepository.kt` - Repository interface

## Common Tasks

### Adding a New Email Provider

1. **Add to Enum**:
   ```kotlin
   enum class EmailProvider {
       // ... existing providers
       NEW_PROVIDER
   }
   ```

2. **Add Configuration**:
   ```kotlin
   object NewProviderConfig {
       val IMAP_HOST = "imap.newprovider.com"
       val IMAP_PORT = 993
       // ... other settings
   }
   ```

3. **Update UI**: Add provider to account setup screens

### Adding a New Feature

1. **Create Use Case**:
   ```kotlin
   class NewFeatureUseCase(
       private val repository: EmailRepository
   ) {
       suspend operator fun invoke(): Result<Unit> {
           // Implementation
       }
   }
   ```

2. **Update Repository**:
   ```kotlin
   interface EmailRepository {
       // ... existing methods
       suspend fun newFeature(): Result<Unit>
   }
   ```

3. **Add UI**: Create Compose screen and ViewModel

### Debugging Issues

1. **Check Logs**:
   ```bash
   adb logcat | grep "Gfmail"
   ```

2. **Debug WebView**:
   ```bash
   adb logcat | grep "WebView"
   ```

3. **Database Issues**:
   ```bash
   adb shell
   run-as com.gf.mail
   ls databases/
   ```

## Troubleshooting

### Build Issues

```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug

# Clear Gradle cache
rm -rf .gradle
./gradlew build
```

### Runtime Issues

1. **App Crashes**: Check logcat for error messages
2. **Email Not Loading**: Verify account credentials
3. **WebView Issues**: Check HTML content and encoding

### Device Issues

```bash
# Check connected devices
adb devices

# Restart ADB
adb kill-server
adb start-server
```

## Getting Help

- **Documentation**: Check `docs/` folder
- **Issues**: Create GitHub issue
- **Discussions**: Use GitHub Discussions
- **Code Review**: Submit Pull Request

## Next Steps

1. **Explore Code**: Browse the codebase
2. **Run Tests**: Ensure everything works
3. **Make Changes**: Start developing
4. **Contribute**: Submit improvements

## Resources

- [Android Development Guide](https://developer.android.com/guide)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Material Design 3](https://m3.material.io/)

---

**Welcome to Gfmail Development!** 🎉

For more detailed information, check out the full documentation in the `docs/` folder.
