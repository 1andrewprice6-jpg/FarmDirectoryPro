#!/bin/bash
# Termux Android Build Hack
export ANDROID_AAPT2_EXECUTABLE=/data/data/com.termux/files/usr/bin/aapt2
export GRADLE_OPTS="-Dorg.gradle.daemon=false -Dandroid.aapt2FromMaven=false"

# Symlink hack for AGP bundled aapt2
find ~/.gradle/caches -name aapt2 -type f -exec ln -sf /data/data/com.termux/files/usr/bin/aapt2 {} \;

cd /data/data/com.termux/files/home/FarmDirectoryPro_repo
./gradlew assembleDevDebug
