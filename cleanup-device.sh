#!/bin/bash

# Farm Directory Device Cleanup Script
# This script removes all old Farm Directory app versions from your device

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  Farm Directory - Device Cleanup and Reset Script              ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Check if device is connected
echo "🔍 Checking for connected devices..."
DEVICE_COUNT=$(adb devices | grep -c "device$")

if [ $DEVICE_COUNT -eq 0 ]; then
    echo "❌ No devices connected!"
    echo ""
    echo "To connect your device:"
    echo "1. Enable Developer Options on your phone"
    echo "2. Enable USB Debugging"
    echo "3. Connect via USB cable"
    echo "4. Run: adb shell"
    echo "5. Grant USB debugging permission on your phone"
    echo ""
    exit 1
fi

echo "✅ Device found!"
echo ""

# Get device info
DEVICE=$(adb devices | grep "device$" | head -1 | awk '{print $1}')
echo "📱 Device: $DEVICE"
echo ""

# Check if app is installed
echo "🔍 Checking if Farm Directory is installed..."
if adb shell pm list packages | grep -q "farmdirectoryupgraded"; then
    echo "✅ Found: com.example.farmdirectoryupgraded"

    echo ""
    echo "🧹 Cleanup steps:"
    echo "  1. Uninstalling app..."
    adb uninstall com.example.farmdirectoryupgraded

    echo "  2. Clearing cache..."
    adb shell pm clear com.example.farmdirectoryupgraded 2>/dev/null || true

    echo "  3. Clearing app data..."
    adb shell rm -rf /data/local/tmp/farm* 2>/dev/null || true

    echo ""
    echo "✅ Cleanup complete!"
else
    echo "ℹ️  Farm Directory not installed (nothing to clean)"
fi

echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  ✅ Device ready for fresh installation                        ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo "Next steps:"
echo "1. Download fresh APK from GitHub Actions"
echo "2. Run: adb install -r ~/Downloads/app-devDebug.apk"
echo "3. Run: adb shell am start -n com.example.farmdirectoryupgraded/.MainActivity"
echo ""
