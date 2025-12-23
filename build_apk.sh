#!/bin/bash

# Build script for FarmDirectoryPro Android App
# This script helps build both debug and release APKs

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}==================================${NC}"
echo -e "${BLUE}FarmDirectoryPro APK Build Script${NC}"
echo -e "${BLUE}==================================${NC}"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed or not in PATH${NC}"
    echo "Please install JDK 17 or higher"
    exit 1
fi

echo -e "${GREEN}✓${NC} Java version: $(java -version 2>&1 | head -n 1)"

# Check if ANDROID_HOME is set
if [ -z "$ANDROID_HOME" ]; then
    echo -e "${YELLOW}Warning: ANDROID_HOME is not set${NC}"
    echo "This might cause build issues. Please set ANDROID_HOME to your Android SDK path"
fi

# Make gradlew executable
chmod +x gradlew

echo ""
echo "Select build type:"
echo "1) Debug APK (for testing)"
echo "2) Release APK (optimized)"
echo "3) Both"
echo ""
read -p "Enter choice [1-3]: " choice

case $choice in
    1)
        echo -e "\n${BLUE}Building Debug APK...${NC}"
        ./gradlew assembleDebug
        
        if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
            APK_SIZE=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
            echo -e "\n${GREEN}✓ Debug APK built successfully!${NC}"
            echo -e "Location: ${YELLOW}app/build/outputs/apk/debug/app-debug.apk${NC}"
            echo -e "Size: $APK_SIZE"
        fi
        ;;
    2)
        echo -e "\n${BLUE}Building Release APK...${NC}"
        
        if [ -f "keystore.properties" ]; then
            echo -e "${GREEN}Found keystore.properties - building signed release${NC}"
        else
            echo -e "${YELLOW}Warning: keystore.properties not found - building unsigned release${NC}"
        fi
        
        ./gradlew assembleRelease
        
        RELEASE_APK=$(find app/build/outputs/apk/release -name "*.apk" | head -1)
        if [ -n "$RELEASE_APK" ]; then
            APK_SIZE=$(du -h "$RELEASE_APK" | cut -f1)
            echo -e "\n${GREEN}✓ Release APK built successfully!${NC}"
            echo -e "Location: ${YELLOW}$RELEASE_APK${NC}"
            echo -e "Size: $APK_SIZE"
        fi
        ;;
    3)
        echo -e "\n${BLUE}Building Debug and Release APKs...${NC}"
        ./gradlew assembleDebug assembleRelease
        
        echo -e "\n${GREEN}✓ Build completed!${NC}"
        echo ""
        
        if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
            DEBUG_SIZE=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
            echo -e "Debug APK: ${YELLOW}app/build/outputs/apk/debug/app-debug.apk${NC} ($DEBUG_SIZE)"
        fi
        
        RELEASE_APK=$(find app/build/outputs/apk/release -name "*.apk" | head -1)
        if [ -n "$RELEASE_APK" ]; then
            RELEASE_SIZE=$(du -h "$RELEASE_APK" | cut -f1)
            echo -e "Release APK: ${YELLOW}$RELEASE_APK${NC} ($RELEASE_SIZE)"
        fi
        ;;
    *)
        echo -e "${RED}Invalid choice${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${BLUE}==================================${NC}"
echo -e "${GREEN}Build process completed!${NC}"
echo -e "${BLUE}==================================${NC}"
echo ""
echo "To install on a connected device, run:"
echo "  adb install <path-to-apk>"
echo ""
echo "For more information, see BUILD_APK.md"
