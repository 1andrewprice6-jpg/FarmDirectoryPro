# Building the APK

This document provides instructions for building the FarmDirectoryPro Android application as an APK.

## Prerequisites

Before building the APK, ensure you have the following installed:

1. **Java Development Kit (JDK) 17 or higher**
   - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)
   - Verify installation: `java -version`

2. **Android SDK**
   - Install [Android Studio](https://developer.android.com/studio) (recommended)
   - Or install command-line tools from [Android SDK Command-line Tools](https://developer.android.com/studio#command-tools)
   - Set `ANDROID_HOME` environment variable to your SDK location

3. **Git** (optional, for cloning the repository)

## Building the Debug APK

The debug APK is useful for testing and development. It's signed with a debug keystore.

### Option 1: Using Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the project directory and click "OK"
4. Wait for Gradle sync to complete
5. Go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**
6. Once complete, click "locate" to find the APK

The debug APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Option 2: Using Command Line

1. Open a terminal/command prompt
2. Navigate to the project directory:
   ```bash
   cd /path/to/FarmDirectoryPro
   ```

3. Make the Gradle wrapper executable (Linux/Mac only):
   ```bash
   chmod +x gradlew
   ```

4. Build the debug APK:
   ```bash
   # On Linux/Mac:
   ./gradlew assembleDebug
   
   # On Windows:
   gradlew.bat assembleDebug
   ```

5. Find the APK at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

## Building the Release APK

The release APK is optimized and signed for distribution. You'll need a signing key.

### Step 1: Create a Keystore (First Time Only)

If you don't have a keystore, create one:

```bash
keytool -genkey -v -keystore my-release-key.keystore \
  -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
```

Follow the prompts to set a password and enter your information.

**Important:** Keep your keystore file and passwords secure. You'll need them for all future releases.

### Step 2: Configure Signing

Create a file named `keystore.properties` in the project root (don't commit this file):

```properties
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=my-key-alias
storeFile=/path/to/my-release-key.keystore
```

### Step 3: Update app/build.gradle.kts

Add the signing configuration to `app/build.gradle.kts`:

```kotlin
android {
    // ... existing configuration ...
    
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = java.util.Properties()
                keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
                
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### Step 4: Build Release APK

```bash
# Linux/Mac:
./gradlew assembleRelease

# Windows:
gradlew.bat assembleRelease
```

The release APK will be at:
```
app/build/outputs/apk/release/app-release.apk
```

## Installing the APK

### On a Physical Device

1. Enable "Unknown Sources" or "Install from Unknown Sources" in device settings
2. Transfer the APK to your device
3. Open the APK file on your device
4. Follow the installation prompts

### Using ADB

```bash
# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Install release APK
adb install app/build/outputs/apk/release/app-release.apk
```

## Troubleshooting

### Gradle Sync Failed

- Ensure you have an internet connection
- Check that `ANDROID_HOME` is set correctly
- Try: `./gradlew --refresh-dependencies`

### SDK Not Found

- Install Android SDK Platform 34: `sdkmanager "platforms;android-34"`
- Install Build Tools: `sdkmanager "build-tools;34.0.0"`

### Out of Memory

Add to `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
```

### Signing Error

- Verify keystore path in `keystore.properties`
- Ensure passwords are correct
- Check that the keystore file exists

## APK Information

- **Package Name:** com.example.farmdirectoryupgraded
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Version:** 2.0 (versionCode: 2)

## Continuous Integration

For automated builds, see `.github/workflows/build-apk.yml`
