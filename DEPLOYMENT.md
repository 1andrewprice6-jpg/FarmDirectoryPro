# Deployment Guide

## CI/CD Pipeline

### GitHub Actions
The project uses GitHub Actions for continuous integration and deployment.

#### Workflows

**1. Android CI (`android-ci.yml`)**
Runs on every push and pull request:
- Linting
- Unit tests
- Build debug APK
- Test coverage reporting

**2. Release (`release.yml`)**
Runs on version tags (v*):
- Build release APK
- Sign APK
- Create GitHub release

### Setup Required

#### 1. Add Secrets to GitHub
Go to Settings → Secrets and add:

- `SIGNING_KEY`: Base64 encoded keystore file
- `ALIAS`: Keystore alias
- `KEY_STORE_PASSWORD`: Keystore password
- `KEY_PASSWORD`: Key password

#### 2. Generate Signing Key
```bash
keytool -genkey -v -keystore release.keystore \
  -alias release -keyalg RSA -keysize 2048 -validity 10000
```

#### 3. Encode Keystore
```bash
base64 release.keystore | tr -d '\n' > keystore.txt
```

Copy content of keystore.txt to SIGNING_KEY secret.

## Manual Deployment

### Build Release APK
```bash
./gradlew assembleRelease
```

### Sign APK
```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore release.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk release
```

### Verify Signature
```bash
jarsigner -verify -verbose -certs \
  app/build/outputs/apk/release/app-release.apk
```

### Align APK
```bash
zipalign -v 4 app-release-unsigned.apk app-release.apk
```

## Versioning

### Update Version
Edit `app/build.gradle.kts`:
```kotlin
versionCode = 2
versionName = "1.1.0"
```

### Create Release Tag
```bash
git tag -a v1.1.0 -m "Release version 1.1.0"
git push origin v1.1.0
```

## Play Store Deployment

### 1. Generate App Bundle
```bash
./gradlew bundleRelease
```

### 2. Upload to Play Console
- Go to Google Play Console
- Select your app
- Release → Production → Create new release
- Upload AAB file
- Fill in release notes
- Review and roll out

### 3. Release Rollout
- Start with 5% rollout
- Monitor crash reports
- Gradually increase to 100%

## Monitoring

### Crashlytics Setup
```kotlin
// Add to build.gradle.kts
plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
}
```

### Analytics
Monitor:
- App crashes
- User sessions
- Feature usage
- Performance metrics

## Rollback Procedure

If issues occur:
1. Halt rollout in Play Console
2. Create hotfix branch
3. Fix issue
4. Test thoroughly
5. Deploy hotfix release
