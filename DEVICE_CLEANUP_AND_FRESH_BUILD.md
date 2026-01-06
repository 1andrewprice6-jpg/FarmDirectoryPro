# Device Cleanup and Fresh Build Guide

## 🧹 Clean Up Old Versions from Your Device

Since you want to get rid of all the old app versions on your phone, follow these steps:

### Method 1: Using Termux/ADB (If you have device connected)

```bash
# First, verify your device is connected
adb devices

# If connected, uninstall all old Farm Directory builds
adb uninstall com.example.farmdirectoryupgraded
adb shell pm clear com.example.farmdirectoryupgraded

# Clear app cache (optional but recommended)
adb shell pm clear com.example.farmdirectoryupgraded.cache
```

**Expected Output:**
```
List of devices attached
<device-id>    device

Success
Success
```

### Method 2: Manual Cleanup on Device

If ADB is not connected:

1. **On your phone's home screen:**
   - Go to **Settings**
   - Find **Apps** or **Application Manager**
   - Search for "Farm Directory"
   - Tap on the app
   - Select **Uninstall** or **Uninstall Updates**
   - Confirm

2. **Clear Cache (Optional but recommended):**
   - Go to **Settings** → **Storage** or **Storage Settings**
   - Find **App Cache** or **Cached Data**
   - Clear cache for "Farm Directory Upgraded"

---

## 🚀 Get Fresh Build from GitHub

The GitHub Actions CI/CD pipeline is already configured and will automatically build a new APK.

### Step 1: Trigger GitHub Actions Build

You have two options:

#### Option A: Automatic (Next Push)
Any push to the `master` branch will automatically trigger the build:
```bash
# From Termux, commit and push a small change
git add .
git commit -m "Trigger fresh build"
git push origin master
```

#### Option B: Manual Trigger (Immediate)
1. Go to your GitHub repository: https://github.com/1andrewprice6-jpg/FarmDirectoryPro
2. Click on the **Actions** tab
3. Find the **Enhanced Android CI** workflow
4. Click **Run workflow** button
5. Select the branch (master) and click **Run workflow**

### Step 2: Monitor Build Progress

1. Go to **Actions** tab in your GitHub repository
2. Watch the **Enhanced Android CI** workflow
3. It will show progress:
   - ✓ Lint Analysis (checking code quality)
   - ✓ Unit Tests (running tests)
   - ✓ Build APKs (compiling for all variants)
   - ✓ Security Analysis (checking dependencies)
   - ✓ Quality Gate (final verification)

**Expected build time: 5-10 minutes**

### Step 3: Download the Built APK

Once the build completes successfully:

1. Go to **Actions** tab
2. Click on the completed **Enhanced Android CI** workflow
3. Scroll down to **Artifacts** section
4. Download the APK you want:
   - `apk-devDebug` (development debug build) - Recommended for testing
   - `apk-prodDebug` (production debug build)
   - `apk-prodStaging` (production staging build)
   - `apk-devStaging` (development staging build)

### Step 4: Install Fresh APK

#### Using ADB (Recommended):
```bash
# Download the APK to your downloads folder, then:
adb install -r ~/Downloads/app-devDebug.apk

# Or install with force flag to replace existing
adb install -r -f ~/Downloads/app-devDebug.apk
```

#### Manual Installation:
1. Transfer the APK file to your phone via:
   - USB cable and file transfer
   - Email or messaging app
   - Cloud storage (Google Drive, Dropbox, etc.)
2. Open the APK file from your phone's file manager
3. Tap **Install**
4. Grant necessary permissions when prompted

### Step 5: Launch the App

```bash
# Using ADB
adb shell am start -n com.example.farmdirectoryupgraded/.MainActivity

# Or manually tap the app icon on your home screen
```

---

## 📋 What's in the Latest Build

The current GitHub Actions workflow builds 4 variants:

| Variant | Purpose | Size | Use Case |
|---------|---------|------|----------|
| **devDebug** | Development + Debugging | ~25MB | Best for testing new features |
| **devStaging** | Development + Staging API | ~25MB | Test against staging backend |
| **prodDebug** | Production + Debugging | ~24MB | Production features + debug info |
| **prodStaging** | Production + Staging API | ~24MB | Production features + staging backend |

**Recommended for you: `apk-devDebug`** - Has all features + useful debugging info

---

## 🔧 Troubleshooting

### Build Failed
- Check the **Actions** tab for detailed error logs
- Common issues:
  - Network interruption (auto-retries)
  - Gradle cache issues (cleared automatically)
  - AAPT2 issues (rare, workflow handles them)

### Installation Failed
```bash
# Clear previous installation completely
adb uninstall com.example.farmdirectoryupgraded
adb shell pm clear com.example.farmdirectoryupgraded

# Try installing again with force flag
adb install -r -f path/to/app-debug.apk
```

### App Crashes on Launch
1. Check device logs:
   ```bash
   adb logcat | grep FarmDirectory
   ```
2. Clear app data:
   ```bash
   adb shell pm clear com.example.farmdirectoryupgraded
   ```
3. Reinstall the APK

### Permissions Not Granted
The app will request permissions on first launch:
- **RECORD_AUDIO** (for voice features)
- **ACCESS_FINE_LOCATION** (for GPS/map features)
- **READ_EXTERNAL_STORAGE** (for data import)
- **WRITE_EXTERNAL_STORAGE** (for data export)

Tap **Allow** when prompted.

---

## 📞 Support

If you need help:

1. **Check build logs**: GitHub Actions → workflow output
2. **Check device logs**: `adb logcat` for app errors
3. **Verify ADB connection**: `adb devices`
4. **Clear Gradle cache** if build hangs:
   ```bash
   rm -rf ~/.gradle/caches
   ./gradlew clean
   ```

---

## ✅ Success Indicators

You'll know it's working when:
- ✓ Old app uninstalled from device
- ✓ GitHub Actions build completes (green checkmark)
- ✓ APK downloaded successfully
- ✓ APK installs without errors
- ✓ App launches and shows main screen
- ✓ All features are accessible

---

**Status**: Ready for clean install! 🎉

Next steps:
1. Clean old app from device (Method 1 or 2 above)
2. Trigger GitHub Actions build (Option A or B)
3. Download the APK when build completes
4. Install fresh APK
5. Enjoy your updated Farm Directory app!

