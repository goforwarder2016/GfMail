# Gfmail Deployment Guide

## Overview

This guide covers the deployment process for the Gfmail Android email client, including building, testing, and releasing the application.

## Prerequisites

### Development Environment

- **Android Studio Hedgehog** (2023.1.1) or higher
- **JDK 17** or higher
- **Android SDK** with API 24+ and build tools
- **Git** for version control
- **Gradle 8.0+** (included in project)

### Required Tools

```bash
# Check Java version
java -version

# Check Android SDK
$ANDROID_HOME/tools/bin/sdkmanager --list

# Check Gradle version
./gradlew --version
```

## Build Configuration

### 1. Local Properties

Create or update `local.properties`:

```properties
sdk.dir=/path/to/android/sdk
ndk.dir=/path/to/android/ndk
```

### 2. Signing Configuration

For release builds, configure signing in `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/keystore.jks")
            storePassword = "your-store-password"
            keyAlias = "your-key-alias"
            keyPassword = "your-key-password"
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 3. Keystore Generation

Generate a keystore for release signing:

```bash
keytool -genkey -v -keystore gfmail-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias gfmail-key
```

## Build Process

### Debug Build

```bash
# Clean and build debug APK
./gradlew clean assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

### Release Build

```bash
# Clean and build release APK
./gradlew clean assembleRelease

# Build release bundle (for Play Store)
./gradlew clean bundleRelease

# Generate signed APK
./gradlew clean assembleRelease
```

### Build Variants

```bash
# Build specific variant
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew bundleRelease

# Build all variants
./gradlew assemble
```

## Testing

### Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run tests for specific module
./gradlew :app:test

# Run tests with coverage
./gradlew jacocoTestReport
```

### Instrumented Tests

```bash
# Run instrumented tests
./gradlew connectedAndroidTest

# Run on specific device
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.gf.mail.EmailSyncIntegrationTest
```

### UI Tests

```bash
# Run Compose UI tests
./gradlew connectedAndroidTest

# Run specific UI test
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.gf.mail.ui.EmailListScreenTest
```

### Code Quality Checks

```bash
# Run lint checks
./gradlew lint

# Run ktlint formatting check
./gradlew ktlintCheck

# Run ktlint formatting
./gradlew ktlintFormat

# Run detekt static analysis
./gradlew detekt
```

## Release Process

### 1. Version Management

Update version in `app/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

### 2. Release Notes

Update `CHANGELOG.md` with new features and fixes.

### 3. Tag Release

```bash
# Create and push tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

### 4. Build Release

```bash
# Build release bundle
./gradlew clean bundleRelease

# Verify APK/Bundle
./gradlew verifyReleaseResources
```

### 5. Upload to Play Store

1. **Prepare Release**
   - Build release bundle
   - Test on multiple devices
   - Verify all features work

2. **Play Console Upload**
   - Upload AAB file
   - Fill release notes
   - Set rollout percentage
   - Submit for review

## Continuous Integration

### GitHub Actions

Create `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Set up Android SDK
      uses: android-actions/setup-android@v2
    
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Run lint
      run: ./gradlew lint
    
    - name: Run detekt
      run: ./gradlew detekt
    
    - name: Build debug APK
      run: ./gradlew assembleDebug
```

### Build Matrix

```yaml
strategy:
  matrix:
    api-level: [24, 28, 33]
    target: [default, google_apis]
```

## Deployment Environments

### Development

- **Branch**: `develop`
- **Build**: Debug APK
- **Testing**: Unit tests + basic UI tests
- **Deployment**: Manual installation

### Staging

- **Branch**: `release/*`
- **Build**: Release APK (unsigned)
- **Testing**: Full test suite + integration tests
- **Deployment**: Internal testing track

### Production

- **Branch**: `main`
- **Build**: Signed release bundle
- **Testing**: Full test suite + manual testing
- **Deployment**: Play Store production track

## Monitoring and Analytics

### Crash Reporting

Configure Firebase Crashlytics in `app/build.gradle.kts`:

```kotlin
plugins {
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation("com.google.firebase:firebase-crashlytics-ktx")
}
```

### Performance Monitoring

```kotlin
dependencies {
    implementation("com.google.firebase:firebase-perf-ktx")
}
```

## Security Considerations

### Code Obfuscation

Ensure ProGuard rules in `proguard-rules.pro`:

```proguard
# Keep email models
-keep class com.gf.mail.domain.model.** { *; }

# Keep repository interfaces
-keep interface com.gf.mail.domain.repository.** { *; }

# Remove debug logs in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
```

### Network Security

Configure `network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">imap.gmail.com</domain>
        <domain includeSubdomains="true">smtp.gmail.com</domain>
    </domain-config>
</network-security-config>
```

## Troubleshooting

### Common Build Issues

1. **Gradle Sync Failed**
   ```bash
   ./gradlew clean
   ./gradlew --refresh-dependencies
   ```

2. **Out of Memory**
   ```properties
   # gradle.properties
   org.gradle.jvmargs=-Xmx4g -XX:MaxPermSize=512m
   ```

3. **NDK Issues**
   ```bash
   ./gradlew clean
   rm -rf .gradle
   ./gradlew build
   ```

### Testing Issues

1. **Test Failures**
   ```bash
   ./gradlew test --info
   ./gradlew connectedAndroidTest --info
   ```

2. **Device Connection**
   ```bash
   adb devices
   adb kill-server
   adb start-server
   ```

## Performance Optimization

### Build Performance

```properties
# gradle.properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
android.useAndroidX=true
android.enableJetifier=true
```

### APK Size Optimization

```kotlin
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}
```

## Rollback Procedures

### Play Store Rollback

1. **Immediate Rollback**
   - Stop rollout in Play Console
   - Revert to previous version

2. **Emergency Hotfix**
   - Create hotfix branch
   - Fix critical issue
   - Build and deploy patch

### Database Migration Rollback

```kotlin
// In GfmailDatabase.kt
@Database(
    entities = [EmailEntity::class, AccountEntity::class],
    version = 2,
    exportSchema = false
)
abstract class GfmailDatabase : RoomDatabase() {
    // Migration from version 1 to 2
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add originalHtmlBody column
                database.execSQL("ALTER TABLE emails ADD COLUMN originalHtmlBody TEXT")
            }
        }
    }
}
```

## Documentation Updates

After deployment:

1. **Update README.md** with new features
2. **Update CHANGELOG.md** with release notes
3. **Update API.md** with new interfaces
4. **Update ARCHITECTURE.md** with changes

---

*This deployment guide is maintained by the Gfmail development team and should be updated as the deployment process evolves.*
