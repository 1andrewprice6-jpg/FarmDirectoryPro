#!/bin/bash
echo "====================================="
echo "Farm Directory Pro - Build & Deploy"
echo "====================================="
echo ""

cd ~/downloads/FarmDirectoryUpgraded

echo "📦 Building APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "APK Location:"
    echo "  app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "📱 To install:"
    echo "  adb install -r app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "Or copy to device:"
    echo "  cp app/build/outputs/apk/debug/app-debug.apk ~/storage/downloads/"
    echo ""
else
    echo ""
    echo "❌ Build failed. Check errors above."
    echo ""
fi
