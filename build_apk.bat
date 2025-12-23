@echo off
REM Build script for FarmDirectoryPro Android App (Windows)
REM This script helps build both debug and release APKs

setlocal enabledelayedexpansion

echo ==================================
echo FarmDirectoryPro APK Build Script
echo ==================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install JDK 17 or higher
    pause
    exit /b 1
)

echo [OK] Java is installed
java -version 2>&1 | findstr "version"

REM Check if ANDROID_HOME is set
if "%ANDROID_HOME%"=="" (
    echo [WARNING] ANDROID_HOME is not set
    echo This might cause build issues. Please set ANDROID_HOME to your Android SDK path
)

echo.
echo Select build type:
echo 1) Debug APK (for testing)
echo 2) Release APK (optimized)
echo 3) Both
echo.
set /p choice="Enter choice [1-3]: "

if "%choice%"=="1" goto build_debug
if "%choice%"=="2" goto build_release
if "%choice%"=="3" goto build_both
echo [ERROR] Invalid choice
pause
exit /b 1

:build_debug
echo.
echo Building Debug APK...
call gradlew.bat assembleDebug
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo [SUCCESS] Debug APK built successfully!
    echo Location: app\build\outputs\apk\debug\app-debug.apk
    for %%A in ("app\build\outputs\apk\debug\app-debug.apk") do echo Size: %%~zA bytes
)
goto end

:build_release
echo.
echo Building Release APK...
if exist "keystore.properties" (
    echo Found keystore.properties - building signed release
) else (
    echo [WARNING] keystore.properties not found - building unsigned release
)
call gradlew.bat assembleRelease
if exist "app\build\outputs\apk\release\" (
    echo.
    echo [SUCCESS] Release APK built successfully!
    dir /b "app\build\outputs\apk\release\*.apk" 2>nul
    for /r "app\build\outputs\apk\release\" %%f in (*.apk) do (
        echo Location: %%f
        echo Size: %%~zf bytes
    )
)
goto end

:build_both
echo.
echo Building Debug and Release APKs...
call gradlew.bat assembleDebug assembleRelease
echo.
echo [SUCCESS] Build completed!
echo.
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo Debug APK: app\build\outputs\apk\debug\app-debug.apk
)
if exist "app\build\outputs\apk\release\" (
    for /r "app\build\outputs\apk\release\" %%f in (*.apk) do (
        echo Release APK: %%f
    )
)
goto end

:end
echo.
echo ==================================
echo Build process completed!
echo ==================================
echo.
echo To install on a connected device, run:
echo   adb install ^<path-to-apk^>
echo.
echo For more information, see BUILD_APK.md
pause
