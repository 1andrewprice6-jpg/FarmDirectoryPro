# Installing FarmDirectoryPro on Your Phone 📱

This guide provides step-by-step instructions to install the FarmDirectoryPro APK on your Android phone.

## Prerequisites

- Android phone running Android 8.0 (API 26) or higher
- APK file (either from GitHub Actions or built locally)

## Method 1: Direct Installation (Easiest)

### Step 1: Get the APK

**Option A: Download from GitHub Actions**
1. Go to [GitHub Actions](https://github.com/1andrewprice6-jpg/FarmDirectoryPro/actions)
2. Click on the latest successful workflow run
3. Scroll down to "Artifacts" section
4. Download `farm-directory-pro-debug-apk.zip`
5. Extract the ZIP file to get `app-debug.apk`

**Option B: Build Locally**
```bash
./gradlew assembleDebug
# APK will be at: app/build/outputs/apk/debug/app-debug.apk
```

### Step 2: Transfer APK to Your Phone

Choose one of these methods:

**Via USB Cable:**
1. Connect your phone to computer with USB cable
2. Copy the APK file to your phone's Download folder
3. Disconnect phone

**Via Email:**
1. Email the APK file to yourself
2. Open the email on your phone
3. Download the attachment

**Via Cloud Storage (Google Drive, Dropbox, etc.):**
1. Upload APK to your cloud storage
2. Open cloud storage app on your phone
3. Download the APK

**Via Direct Download:**
1. If you have the APK on a web server, open the link in your phone's browser
2. Download the file

### Step 3: Enable Installation from Unknown Sources

Since this app is not from the Google Play Store, you need to allow installations from unknown sources.

**For Android 8.0 - 12:**
1. Go to **Settings** → **Security** (or **Biometrics and security**)
2. Find **Install unknown apps** or **Unknown sources**
3. Select the app you'll use to install (e.g., Chrome, Files, Gmail)
4. Toggle **Allow from this source** ON

**For Android 13+:**
1. When you try to install, Android will prompt you
2. Tap **Settings**
3. Toggle **Allow from this source** ON
4. Go back and continue installation

**Alternative path (varies by manufacturer):**
- Samsung: Settings → Apps → Special access → Install unknown apps
- Xiaomi: Settings → Privacy → Special permissions → Install unknown apps
- OnePlus: Settings → Security & privacy → More privacy settings → Install apps from external sources

### Step 4: Install the APK

1. Open your **Files** app or **Downloads** app
2. Navigate to where you saved the APK file
3. Tap on `app-debug.apk` (or `app-release.apk`)
4. You may see a warning - tap **Install** anyway
5. Wait for installation to complete
6. Tap **Open** to launch the app, or **Done**

### Step 5: Launch the App

1. Find "FarmDirectoryPro" or "ChickenFarmApp" in your app drawer
2. Tap to open
3. Grant any permissions the app requests

## Method 2: Using ADB (For Developers)

If you have Android Debug Bridge (ADB) installed on your computer:

### Prerequisites
- ADB installed on your computer
- USB debugging enabled on your phone

### Enable USB Debugging on Phone
1. Go to **Settings** → **About phone**
2. Tap **Build number** 7 times to enable Developer options
3. Go back to **Settings** → **System** → **Developer options**
4. Enable **USB debugging**

### Install via ADB
1. Connect your phone to computer via USB
2. On your phone, authorize the computer when prompted
3. Open terminal/command prompt on computer
4. Run:
   ```bash
   # Verify device is connected
   adb devices
   
   # Install the APK
   adb install app/build/outputs/apk/debug/app-debug.apk
   
   # Or for release APK
   adb install app/build/outputs/apk/release/app-release.apk
   ```
5. The app will be installed automatically
6. Launch it from your phone's app drawer

## Method 3: Wireless Installation (Advanced)

If you prefer wireless transfer:

### Using WiFi Direct/Nearby Share (Android to Android)
1. Use Android's built-in file sharing (Nearby Share)
2. Share the APK from one device to another
3. Follow installation steps above

### Using Local Web Server
1. On computer, start a simple HTTP server in the directory containing the APK:
   ```bash
   # Python 3
   python3 -m http.server 8000
   
   # Python 2
   python -m SimpleHTTPServer 8000
   ```
2. Find your computer's local IP address
3. On your phone's browser, go to `http://YOUR_IP:8000`
4. Download the APK file
5. Follow installation steps above

## Troubleshooting

### Installation Blocked
**Problem:** "App not installed" or "Installation blocked"

**Solutions:**
- Make sure you enabled "Install unknown apps" for the correct app
- Try using a different file manager
- Restart your phone and try again
- Check if you have enough storage space (need at least 50MB free)

### App Crashes on Launch
**Problem:** App opens but immediately closes

**Solutions:**
- Your Android version might be too old (need Android 8.0+)
- Clear app data: Settings → Apps → FarmDirectoryPro → Storage → Clear data
- Uninstall and reinstall the app
- Make sure you downloaded the complete APK file (check file size is reasonable, not 0 bytes)

### "Parse Error" or "There was a problem parsing the package"
**Problem:** Installation fails with parse error

**Solutions:**
- APK file might be corrupted - try downloading again
- Make sure APK is compatible with your device architecture
- Check that APK wasn't modified or truncated during transfer

### Can't Find Downloaded APK
**Problem:** Downloaded APK but can't locate it

**Solutions:**
- Check **Downloads** folder in Files app
- Look in **Internal storage** → **Download**
- Use search in Files app to find "app-debug.apk"
- Re-download and pay attention to where it saves

### "App not installed as package appears to be invalid"
**Problem:** Installation fails with invalid package message

**Solutions:**
- You might have an incompatible version already installed
- Uninstall any existing version first
- Download a fresh copy of the APK
- Make sure it's the debug APK (app-debug.apk)

## Updating the App

To update to a newer version:

1. Download the new APK
2. Install it directly (no need to uninstall first)
3. Your data will be preserved

Note: If you're switching between debug and release versions, or if the app signature changed, you'll need to uninstall the old version first.

## Uninstalling the App

To remove the app from your phone:

1. Go to **Settings** → **Apps**
2. Find **FarmDirectoryPro** or **ChickenFarmApp**
3. Tap **Uninstall**

Or:
1. Long-press the app icon
2. Drag to **Uninstall** or tap the info icon and then **Uninstall**

## Security Note

⚠️ **Important:** Only install APKs from trusted sources. This APK is from the official repository at https://github.com/1andrewprice6-jpg/FarmDirectoryPro

After installing, you can disable "Install unknown apps" for added security:
1. Go to the same settings where you enabled it
2. Toggle it back OFF

## App Information

- **Package Name:** com.example.farmdirectoryupgraded
- **Debug Package:** com.example.farmdirectoryupgraded.debug
- **Minimum Android Version:** 8.0 (API 26)
- **Target Android Version:** 14 (API 34)
- **Version:** 2.0
- **Permissions:** (Check app info after installation)

## Need More Help?

- For build instructions, see [BUILD_APK.md](BUILD_APK.md)
- For project information, see [README.md](README.md)
- For issues, visit [GitHub Issues](https://github.com/1andrewprice6-jpg/FarmDirectoryPro/issues)
