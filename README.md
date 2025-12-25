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

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or newer
- JDK 11 or higher
- Android SDK 24+

### Building the Project
```bash
./gradlew assembleDebug
```

### Running Tests
```bash
./gradlew test
```

### Installing on Device
```bash
./gradlew installDebug
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
