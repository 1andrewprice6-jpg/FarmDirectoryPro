# FarmDirectoryUpgraded

## Overview
FarmDirectoryUpgraded is an Android application built with Kotlin and Jetpack Compose.

## Features
- Modern Material Design 3 UI
- Room Database for local persistence
- MVVM Architecture pattern
- Jetpack Compose declarative UI
- Kotlin Coroutines for async operations

## Project Statistics
- Kotlin files: 8
- Data Entities: 8
- ViewModels: 0
- Database DAOs: 0
- UI Screens: 0

## Architecture
This app follows the MVVM (Model-View-ViewModel) architecture pattern:
- **Model**: Data entities and database DAOs
- **View**: Jetpack Compose UI screens
- **ViewModel**: Business logic and state management

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Database**: Room Database
- **Architecture**: MVVM with Repository pattern
- **Async**: Kotlin Coroutines & Flow
- **DI**: Manual dependency injection

## Install on Your Phone

The easiest way to install Farm Directory Pro on your Android device is to download the latest debug APK directly from GitHub Releases.

### Download & Install (No computer needed)

1. On your Android phone, go to the repository's **[Releases](../../releases)** page, open the **"Debug Build (Latest)"** pre-release, and download the latest `FarmDirectoryPro-debug-*.apk` file from the Assets section.
2. Open **Settings → Security** (or **Apps → Special app access → Install unknown apps**) and enable **Install unknown apps** for your browser or file manager.
3. Open the downloaded `.apk` file and tap **Install**.
4. Once installed, open **Farm Directory Pro** from your app drawer.

> **Tip:** You can also trigger a fresh build from the [Debug APK Distribution](../../actions/workflows/debug-distribution.yml) workflow by clicking **Run workflow**.

### Building & Installing via ADB (Developers)

#### Prerequisites
- Android Studio Arctic Fox or newer
- JDK 17 or higher
- Android SDK 26+

#### Build the Project
```bash
./gradlew assembleProdDebug
```

#### Run Tests
```bash
./gradlew test
```

#### Install Directly on a Connected Device
```bash
./gradlew installProdDebug
```

## Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   ├── data/          # Data models and entities
│   │   │   ├── db/            # Database and DAOs
│   │   │   ├── viewmodel/     # ViewModels
│   │   │   ├── ui/            # Compose UI screens
│   │   │   └── MainActivity.kt
│   │   ├── res/               # Resources
│   │   └── AndroidManifest.xml
│   └── test/                  # Unit tests
```

## Contributing
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License
This project is licensed under the MIT License.

## Contact
For questions or support, please open an issue on GitHub.
