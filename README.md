# PolicyDesk

[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.11.00-blue.svg)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**PolicyDesk** is an offline-first, privacy-focused vehicle insurance policy manager and CRM built for Android. Designed specifically for Indian insurance advisors and fleet managers, it simplifies tracking motor insurance policies, managing renewals, automating customer communications, and parsing policy PDFs using Google Gemini AI.

---

## Key Features

- **Local-First Architecture:** Complete offline functionality powered by Room database with zero forced cloud accounts or telemetry.
- **RTO Registration Normalizer:** Automatically strips spaces/hyphens and standardizes registrations (`MH12AB1234`, `DL1A1234`, `22BH1234AA`).
- **PSU vs. Private Insurer Categorization:** Pre-configured catalog for Indian insurance companies with custom insurer addition.
- **Multi-Vehicle Customer View:** Automatically groups vehicles and policies owned by the same customer linked by mobile number.
- **Bulk Spreadsheet Import:** Import `.csv` and `.xlsx` files with column auto-mapping, data validation, and duplicate conflict resolution.
- **AI PDF Policy Intake:** Share an insurance policy document directly from WhatsApp or a file manager into PolicyDesk. Powered by Google Gemini for accurate field extraction.
- **Automated Expiry Reminders:** Configurable background notifications via WorkManager (30-day, 15-day, 7-day, 1-day, and same-day alerts) with 1-tap WhatsApp reminder messages and phone dialer shortcuts.
- **Biometric App Lock:** Safeguards client records using device fingerprint, face unlock, or device PIN (`BiometricPrompt`).
- **Encrypted Cloud Backup:** Optional Google Drive AppData folder backup snapshot export and restore.

---

## Tech Stack

| Layer | Technologies |
|---|---|
| **UI** | Jetpack Compose, Material 3, Compose Navigation |
| **Architecture** | MVVM, Clean Architecture, Unidirectional Data Flow (UDF) |
| **Dependency Injection** | Dagger Hilt |
| **Local Storage** | Room Database (SQLite), DataStore Preferences |
| **Background Work** | WorkManager |
| **Security** | AndroidX Security Crypto (`EncryptedSharedPreferences`), AndroidX Biometric |
| **AI Extraction** | Google Gemini API (Multimodal PDF extraction) |
| **Cloud Sync** | Google Play Services Auth, Google Drive REST API (AppData scope) |

---

## Getting Started

### Prerequisites

- **Android Studio:** Ladybug (2024.2) or later recommended
- **JDK:** Version 17
- **Android SDK:** Compile SDK 35, Min SDK 26

### Cloning & Building

```bash
# Clone the repository
git clone https://github.com/your-username/policydesk.git
cd policydesk

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```

---

## Configuration

### 1. Google Gemini AI API Key (Optional)
To use automated PDF policy document intake:
1. Obtain an API key from [Google AI Studio](https://aistudio.google.com/).
2. In the app, navigate to **Settings > AI Settings** and enter your API key. Keys are securely stored in hardware-backed `EncryptedSharedPreferences`.

### 2. Google Drive Backup (Optional)
To enable silent backups to Google Drive AppData folder:
1. Create an OAuth 2.0 Client ID in the [Google Cloud Console](https://console.cloud.google.com/).
2. Register your package name (`com.policydesk.app`) and SHA-1 signing fingerprint.
3. Enable the Google Drive API for your project.

### 3. Release Signing
For production release builds, configure the following environment variables or properties in `~/.gradle/gradle.properties`:
```properties
KEYSTORE_FILE=/path/to/keystore.jks
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```
Then run:
```bash
./gradlew assembleRelease
```

---

## GitHub Releases

Automated releases are published via GitHub Actions whenever a semantic version tag (e.g. `v1.0.0`) is pushed:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The workflow:
1. Executes the unit test suite (`testDebugUnitTest`).
2. Builds and signs the release APK (`PolicyDesk-v1.0.0.apk`) and Android App Bundle (`PolicyDesk-v1.0.0.aab`).
3. Automatically creates a tagged GitHub Release with auto-generated release notes and attached distribution assets.

You can also manually trigger the **Publish GitHub Release** workflow from the repository's **Actions** tab in GitHub.

---

## Project Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/policydesk/app/
│   ├── PolicyDeskApp.kt               # Application entry point
│   ├── MainActivity.kt                # Single activity host
│   ├── data/
│   │   ├── ai/                        # Gemini AI PDF extraction client
│   │   ├── drive/                     # Google Drive AppData sync
│   │   ├── importer/                  # CSV/XLSX spreadsheet parser
│   │   ├── local/                     # Room DB, DAOs, entities
│   │   ├── repository/                # Repository implementation
│   │   └── security/                  # AppLock and EncryptedSharedPreferences
│   ├── di/                            # Dagger Hilt dependency injection
│   ├── domain/
│   │   ├── model/                     # Business entities & normalizers
│   │   └── repository/                # Repository interface
│   ├── notification/                  # Policy expiry alerts & WorkManager
│   └── ui/
│       ├── addpolicy/                 # Manual policy creation screen
│       ├── components/                # Shared glassmorphic UI components
│       ├── customers/                 # Customer directory & vehicle groupings
│       ├── dashboard/                 # Expiry countdown & metrics dashboard
│       ├── detail/                    # Vehicle policy history & details
│       ├── importer/                  # Bulk spreadsheet import workflow
│       ├── monthview/                 # Monthly renewal calendar
│       ├── navigation/                # Compose NavHost routing
│       ├── pdfreview/                 # AI intake review & confirmation
│       ├── renew/                     # Policy renewal screen
│       ├── reports/                   # Performance & sales reporting
│       ├── settings/                  # Backup, AI, & security preferences
│       └── theme/                     # Typography, colors, and Theme
└── res/                               # App icons, vectors, and themes
```

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
