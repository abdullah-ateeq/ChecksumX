# ChecksumX 🛡️

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat&logo=android)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![MinSDK](https://img.shields.io/badge/MinSDK-26%20%28Android%208.0%29-brightgreen)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline-success.svg)](#100-offline-privacy-guarantee)

**ChecksumX** is a developer-grade Android utility for calculating, generating, and verifying cyclic redundancy checks (CRC) and cryptographic message digests. Built with modern Jetpack Compose and designed around pure offline computation, ChecksumX offers fast, memory-safe bitwise integrity verification for files and raw text.

---

## 🌟 Key Features

- **⚡ Dual Input Modes**:
  - **Text Mode**: Real-time hash calculation on plain text strings with UTF-8 byte count tracking.
  - **File Mode**: High-performance streaming hashing for files of any size using Android's Storage Access Framework (SAF).
- **🔍 Dual Operation Modes**:
  - **Generate Mode**: Compute precise checksums with options for single algorithm or multi-algorithm batch calculations.
  - **Verify Mode**: Compare text or files against expected checksum strings with automatic format cleaning and match feedback.
- **📊 Streaming Hash Engine**: Memory-efficient chunked file reading prevents OutOfMemory (OOM) errors even on giant gigabyte disk images or APK files.
- **🔒 Security & Category Badging**: Clear technical separation and warnings between error-detection codes (CRC), legacy cryptographic hashes (MD5, SHA-1), and modern cryptographic standards (SHA-256, SHA-384, SHA-512).
- **⚙️ Customization & Preferences**: Persist preferred algorithms, default operation modes, hash output casing (uppercase/lowercase), and theme preferences via Jetpack DataStore.
- **🛡️ Zero Network Access**: 100% offline app with **zero network permissions** (`android.permission.INTERNET` is completely omitted).

---

## 🧮 Supported Algorithms

ChecksumX includes 9 standard algorithms categorized according to their mathematical properties and security standing:

| Algorithm | Category | Bit Length | Hex Length | Technical Parameters / Standards | Primary Use Case |
| :--- | :--- | :---: | :---: | :--- | :--- |
| **CRC-8** | CRC | 8-bit | 2 chars | Poly: `0x07` ($x^8 + x^2 + x + 1$), Init: `0x00` | SMBus, ATM networks |
| **CRC-16 / CCITT-FALSE** | CRC | 16-bit | 4 chars | Poly: `0x1021`, Init: `0xFFFF` | Telecommunications, Autosar |
| **CRC-32 (IEEE 802.3)** | CRC | 32-bit | 8 chars | Poly: `0x04C11DB7`, Init: `0xFFFFFFFF` | Ethernet, PNG, ZIP files |
| **CRC-32C (Castagnoli)** | CRC | 32-bit | 8 chars | Poly: `0x1EDC6F41`, Init: `0xFFFFFFFF` | ext4, Btrfs, iSCSI, SCTP |
| **MD5** | Legacy Crypto | 128-bit | 32 chars | RFC 1321 *(Collision Vulnerable)* | Legacy verification |
| **SHA-1** | Legacy Crypto | 160-bit | 40 chars | FIPS PUB 180-4 *(Collision Vulnerable)* | Legacy verification, Git IDs |
| **SHA-256** | Modern Crypto | 256-bit | 64 chars | FIPS PUB 180-4 (SHA-2 Family) | Software & ISO integrity verification |
| **SHA-384** | Modern Crypto | 384-bit | 96 chars | FIPS PUB 180-4 (SHA-2 Family) | High security margin verification |
| **SHA-512** | Modern Crypto | 512-bit | 128 chars | FIPS PUB 180-4 (SHA-2 Family) | Maximum strength digest |

---

## 🛠️ Architecture & Tech Stack

ChecksumX follows Android recommended best practices with Unidirectional Data Flow (UDF) and clean MVVM architecture:

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Architecture**: MVVM with `StateFlow` and `ViewModel`
- **Asynchronous Execution**: Kotlin Coroutines & `Dispatchers.IO`
- **Data Persistence**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Navigation**: Jetpack Navigation Compose
- **File Access**: Android Storage Access Framework (SAF)

```
app/src/main/java/com/example/checksumx/
├── crypto/              # Core bitwise checksum engines & streaming stream algorithms
├── data/
│   ├── model/           # Data models (ChecksumAlgorithm, FileMetadata, ChecksumResult)
│   └── preferences/     # DataStore repository for app preferences
├── ui/
│   ├── components/      # Reusable Compose UI components (Cards, Selectors, Badges)
│   ├── navigation/      # NavHost & Screen routes
│   ├── screens/         # HomeScreen, SettingsScreen, AboutScreen, SplashScreen
│   ├── theme/           # Material 3 Color palette, Typography, Themes
│   └── viewmodel/       # HomeViewModel & SettingsViewModel
└── util/                # File utilities & formatting helpers
```

---

## 🛡️ 100% Offline Privacy Guarantee

- **Zero Network Permissions**: The app does not declare or request the `INTERNET` permission in its Android Manifest.
- **Pure Local Execution**: All computations run entirely on the local device hardware using streaming input streams.
- **Zero Telemetry**: No tracking SDKs, analytics, remote loggers, or third-party ad networks.

---

## 📱 Screenshots & User Experience

| Main Home Screen | Settings & Preferences | Technical About Screen |
| :---: | :---: | :---: |
| *Generate & Verify Hashes with File or Text input* | *Default algorithm, operation mode, and formatting preferences* | *Algorithmic standards, parameters, and security warnings* |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: 11 or higher
- **Android Device / Emulator**: Running Android 8.0 (API level 26) or higher

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/ChecksumX.git
   cd ChecksumX
   ```

2. **Build the APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Install on connected device**:
   ```bash
   ./gradlew installDebug
   ```

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Abdullah Ateeq**
- GitHub: [@abdullah-ateeq](https://github.com/abdullah-ateeq)
