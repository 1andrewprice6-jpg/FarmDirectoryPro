# How to Build the Updated APK

## ⚠️ Important: Build Environment Setup

The code changes have been completed successfully, but building on **Termux has limitations**. Follow these instructions based on your setup:

---

## Option 1: Build on Your Computer (Recommended)

### Prerequisites
- Android Studio 4.2+
- JDK 11+
- Android SDK API 34+
- Gradle 8.x

### Steps

1. **Transfer the project to your computer**:
   ```bash
   # Copy from Termux to computer
   scp -r /data/data/com.termux/files/home/downloads/FarmDirectoryUpgraded/* \
       your-username@your-computer:/path/to/projects/FarmDirectoryUpgraded/
   ```

2. **Open in Android Studio**:
   - Launch Android Studio
   - File → Open → Select FarmDirectoryUpgraded folder
   - Wait for Gradle sync to complete

3. **Build the APK**:
   ```bash
   # Option A: Using Gradle command line
   cd /path/to/FarmDirectoryUpgraded
   ./gradlew clean assembleDebug

   # Option B: Using Android Studio
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```

4. **Output APK location**:
   ```
   FarmDirectoryUpgraded/app/build/outputs/apk/dev/debug/app-dev-debug.apk
   ```

5. **Transfer back to phone**:
   ```bash
   adb push app-dev-debug.apk /sdcard/Download/
   ```

---

## Option 2: Use Existing APK + Apply Code Changes

If you want to use the pre-built APK and add the code changes:

### For Development Testing

1. **The latest pre-built APK**:
   ```
   /data/data/com.termux/files/home/downloads/FarmDirectoryUpgraded/app-debug.apk
   ```

2. **Code changes are ready in**:
   - `app/src/main/java/com/example/farmdirectoryupgraded/viewmodel/FarmerViewModel.kt`
   - `app/src/main/java/com/example/farmdirectoryupgraded/utils/QRCodeScanner.kt`
   - `app/build.gradle.kts`

3. **To rebuild with changes**:
   - Option 2a: Transfer files to computer and build there (Option 1 above)
   - Option 2b: Use CI/CD pipeline (see Option 3 below)

---

## Option 3: Use GitHub CI/CD Pipeline

If your project is on GitHub:

1. **Push the code to GitHub**:
   ```bash
   git add .
   git commit -m "Fix GPS, attendance check-out, QR code scanning, and route optimization"
   git push origin main
   ```

2. **GitHub Actions will build automatically**:
   - Check `.github/workflows/` for build configuration
   - APK will be available in workflow artifacts
   - Download and install on your phone

3. **Workflow file location**:
   ```
   .github/workflows/android-build.yml
   ```

---

## Option 4: Use Cloud Build Services

Services like **Appetize.io** or **Codemagic** can build automatically:

1. Connect your GitHub repository
2. Configure build settings
3. APK is built in the cloud
4. Download directly to your phone

---

## What Changed in This Build

### Files Modified:
1. **viewmodel/FarmerViewModel.kt** (Core changes)
   - ✓ Added EmployeeDao parameter
   - ✓ Added employee state management
   - ✓ Implemented `recordAttendance()` with employee tracking
   - ✓ Implemented `checkOutAttendance()` with hours calculation
   - ✓ Implemented `checkInWithQRCode()` for QR scanning
   - ✓ Improved `getCurrentLocation()` for real GPS
   - ✓ Improved `optimizeRoute()` with better algorithm
   - ✓ Added employee management functions

2. **utils/QRCodeScanner.kt** (NEW file)
   - ✓ QR code scanning utility
   - ✓ Data parsing from QR codes
   - ✓ Employee validation

3. **build.gradle.kts** (Dependencies)
   - ✓ Added: `com.google.mlkit:barcode-scanning:17.1.0`

### Files NOT Changed:
- All UI files (AdvancedScreens.kt)
- All data models (Employee.kt, AttendanceRecord.kt, etc.)
- Database DAOs
- Manifest files

---

## Build Configuration

### Debug Build (for testing)
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/dev/debug/app-dev-debug.apk
```

### Release Build (for production)
```bash
./gradlew assembleRelease -Pandroid.injected.signing.store.file=keystore.jks \
  -Pandroid.injected.signing.store.password=password \
  -Pandroid.injected.signing.key.alias=alias \
  -Pandroid.injected.signing.key.password=password
# Output: app/build/outputs/apk/prod/release/app-prod-release.apk
```

### Flavor Builds
```bash
# Dev environment
./gradlew assembleDevDebug

# Prod environment
./gradlew assembleProdDebug
```

---

## Troubleshooting Build Issues

### Issue: AAPT2 Error on Termux
**Solution**: Build on your computer instead. Termux has environment limitations for Android builds.

### Issue: Gradle Sync Fails
**Solution**:
```bash
./gradlew clean
./gradlew --refresh-dependencies
```

### Issue: Java Version Mismatch
**Solution**: Ensure Java 11+ is installed
```bash
java -version
# Should show: java 11+ (not java 8)
```

### Issue: Out of Memory
**Solution**: Increase heap size
```bash
export GRADLE_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
./gradlew clean assembleDebug
```

### Issue: Missing Android SDK
**Solution**: Install via Android Studio
- Open Android Studio → SDK Manager
- Install API 34 (or your target API)
- Install Android SDK Build-tools

---

## Verification Checklist

After building, verify the APK contains the fixes:

✓ APK size: ~15-20 MB (includes ML Kit)
✓ Features present:
  - Employee attendance tracking
  - QR code scanning capability
  - Route optimization
  - GPS reconciliation

---

## Installation on Phone

### Option A: Via ADB
```bash
adb install -r app-debug.apk
```

### Option B: Via File Transfer
1. Copy APK to phone's Download folder
2. Open file manager on phone
3. Tap the APK file
4. Install

### Option C: Via Email/Sharing
1. Email the APK to yourself
2. Download on phone
3. Install from Downloads

---

## Testing the Fixes

After installing the APK:

### Test 1: Attendance Check-Out
1. Go to Attendance screen
2. Select an employee
3. Click "Check In"
4. Wait a few seconds
5. Click "Check Out"
6. Verify hours worked are calculated

### Test 2: QR Code Scanning
1. Go to Attendance screen
2. Generate QR code: `EMP:123|FARM:Farm Name|TASK:Task`
3. Scan QR code
4. Verify employee is checked in

### Test 3: Route Optimization
1. Go to Route Optimization
2. Select 3-5 farms
3. Click "Optimize"
4. Verify distances and times are calculated

### Test 4: GPS Reconciliation
1. Enable location on device
2. Go to GPS Reconciliation
3. Click "Get Current Location"
4. Verify nearest farm is identified

---

## Production Deployment

### Pre-Release Checklist
- [ ] All features tested on device
- [ ] Attendance records save correctly
- [ ] QR codes scan properly
- [ ] GPS location is accurate
- [ ] Route optimization calculates correctly
- [ ] No crashes or errors in logs

### Release Build Steps
1. Increment version in `build.gradle.kts`
   ```kotlin
   versionCode = 3
   versionName = "2.1"
   ```

2. Build release APK
3. Sign with production keystore
4. Upload to Google Play Store or distribute

---

## Support Resources

- **Android Studio Docs**: https://developer.android.com/studio
- **Gradle Build Tool Docs**: https://gradle.org/
- **ML Kit QR Scanning**: https://developers.google.com/ml-kit/vision/barcode-scanning
- **Kotlin Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html

---

**Last Updated**: 2025-12-26
**Status**: Ready for Build ✓
