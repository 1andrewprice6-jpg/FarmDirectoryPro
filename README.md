# FarmDirectoryPro 🌾

A modern Android application for managing farm directories, built with Kotlin and Jetpack Compose.

## Features

- 📱 Modern Material Design 3 UI
- 🗃️ Local database with Room persistence
- 🎨 Jetpack Compose for reactive UI
- 📊 Farmer management and directory
- 🔄 MVVM architecture with ViewModel

## Screenshots

<!-- Add screenshots here -->

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Database:** Room
- **Architecture:** MVVM (Model-View-ViewModel)
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)

## Building the App

### Quick Start

To build and run the app, you need:
- JDK 17 or higher
- Android SDK
- Android Studio (recommended) or command-line tools

For detailed build instructions, see [BUILD_APK.md](BUILD_APK.md)

### Quick Build

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

The APK files will be in:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## Installation

1. Download the APK from the [Releases](../../releases) page or build it yourself
2. Enable "Install from Unknown Sources" on your Android device
3. Transfer the APK to your device and install it
4. Or use ADB: `adb install app-debug.apk`

## Continuous Integration

The app is automatically built on every push using GitHub Actions. You can download the latest APK from the [Actions](../../actions) tab.

## Development

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/1andrewprice6-jpg/FarmDirectoryPro.git
   cd FarmDirectoryPro
   ```

2. Open the project in Android Studio

3. Sync Gradle files

4. Run the app on an emulator or physical device

### Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/farmdirectoryupgraded/
│   │   │   ├── MainActivity.kt          # Main entry point
│   │   │   ├── data/                    # Data layer
│   │   │   │   ├── Farmer.kt           # Data models
│   │   │   │   ├── FarmerDao.kt        # Database access
│   │   │   │   └── FarmDatabase.kt     # Room database
│   │   │   ├── viewmodel/              # ViewModels
│   │   │   │   └── FarmerViewModel.kt
│   │   │   └── ui/                     # UI components
│   │   │       └── theme/              # Material theme
│   │   └── res/                        # Resources
│   └── androidTest/                    # Instrumented tests
└── build.gradle.kts                    # App-level build config
```

## Dependencies

- **AndroidX Core:** Core Kotlin extensions
- **Jetpack Compose:** Modern declarative UI
- **Material 3:** Material Design components
- **Room:** Local database
- **Lifecycle & ViewModel:** Architecture components

See [app/build.gradle.kts](app/build.gradle.kts) for complete dependency list.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is available under the MIT License.

## Contact

Project Link: [https://github.com/1andrewprice6-jpg/FarmDirectoryPro](https://github.com/1andrewprice6-jpg/FarmDirectoryPro)

## Acknowledgments

- Built with [Android Jetpack](https://developer.android.com/jetpack)
- UI built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Icons from [Material Icons](https://fonts.google.com/icons)
