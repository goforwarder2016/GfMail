# Changelog

All notable changes to Gfmail will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Push notifications for new emails
- Email templates and signatures
- Advanced search filters
- Email scheduling functionality
- Calendar integration

### Changed
- Improved email sync performance
- Enhanced UI animations
- Updated Material Design 3 components

### Fixed
- Memory leaks in WebView rendering
- Email threading issues
- Sync conflicts with multiple accounts

## [1.0.0] - 2024-01-11

### Added
- **Multi-Provider Email Support**
  - 163/126/yeah.net (NetEase) with full IMAP/SMTP support
  - Gmail with OAuth2 and App Password authentication
  - QQ Mail with authorization code support
  - Outlook/Hotmail with modern authentication
  - Yahoo Mail with IMAP/SMTP support
  - Apple iCloud with IMAP/SMTP support

- **Advanced Email Rendering**
  - Full HTML email support with WebView
  - Dynamic height adjustment for email content
  - Multi-encoding support (UTF-8, GBK, GB2312)
  - User-controlled remote image loading
  - Rich text formatting preservation

- **Modern UI/UX**
  - Jetpack Compose implementation
  - Material Design 3 compliance
  - Dark/Light theme support
  - Responsive design for all screen sizes
  - Smooth animations and transitions

- **Security Features**
  - End-to-end encryption support
  - Privacy-first design principles
  - Secure authentication protocols
  - Local encrypted data storage
  - Network security with TLS/SSL

- **Performance Optimizations**
  - Efficient background email synchronization
  - Offline email reading capability
  - Memory-optimized WebView rendering
  - Fast email list loading

- **Email Management**
  - Email threading and conversation view
  - Advanced search and filtering
  - Mark as read/unread functionality
  - Archive and delete operations
  - Pull-to-refresh synchronization

### Technical Implementation

- **Architecture**
  - MVVM pattern with clean architecture
  - Repository pattern for data management
  - Use case pattern for business logic
  - Dependency injection with manual DI container

- **Data Layer**
  - Room database for local storage
  - Retrofit for network communication
  - OkHttp for HTTP client functionality
  - Gson for JSON serialization

- **Email Protocols**
  - IMAP client implementation
  - SMTP client implementation
  - OAuth2 authentication flow
  - TLS/SSL secure connections

- **UI Components**
  - WebView with dynamic height calculation
  - Jsoup for HTML parsing
  - Advanced HTML to text conversion
  - Material Design 3 components

### Fixed Issues

- **Email Content Display**
  - Fixed HTML email rendering issues
  - Resolved content truncation problems
  - Improved charset detection and handling
  - Enhanced image loading control

- **Authentication**
  - Fixed Gmail login with app passwords
  - Resolved QQ Mail authorization issues
  - Improved error handling and user guidance
  - Enhanced connection testing

- **UI/UX Improvements**
  - Fixed navigation back button functionality
  - Improved email list performance
  - Enhanced loading states and error handling
  - Better responsive design implementation

- **Performance**
  - Optimized email synchronization
  - Reduced memory usage in WebView
  - Improved database query performance
  - Enhanced network request handling

### Known Issues

- Push notifications not yet implemented
- Email templates feature pending
- Advanced search filters in development
- Calendar integration planned for v2.0

## [0.9.0] - 2024-01-01

### Added
- Initial project setup
- Basic email client architecture
- 163 email provider support
- Basic UI implementation

### Changed
- Project structure optimization
- Code organization improvements

### Fixed
- Initial compilation issues
- Basic functionality implementation

## [0.8.0] - 2023-12-15

### Added
- Project initialization
- Basic Android project structure
- Initial dependency setup

---

## Version History

- **v1.0.0** - First stable release with full email client functionality
- **v0.9.0** - Beta release with basic features
- **v0.8.0** - Alpha release with project setup

## Migration Guide

### From v0.9.0 to v1.0.0

1. **Database Migration**
   - Automatic migration from version 1 to 2
   - New `originalHtmlBody` field added
   - Existing data preserved

2. **UI Changes**
   - Updated to Material Design 3
   - New email detail screen with WebView
   - Enhanced account management

3. **Configuration Updates**
   - New email provider configurations
   - Updated server settings
   - Enhanced authentication flows

## Support

For questions about version updates or migration issues:
- Create an issue on GitHub
- Check the documentation
- Contact the development team

---

*This changelog is maintained by the Gfmail development team.*
