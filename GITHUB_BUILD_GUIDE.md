# GitHub CI/CD Build Guide

## Quick Start: Build on GitHub

Your project already has GitHub Actions configured! Just push your code and GitHub will automatically build the APK.

---

## Step 1: Check Your Git Configuration

```bash
cd /data/data/com.termux/files/home/downloads/FarmDirectoryUpgraded

# Check if git is initialized
git status

# If not initialized:
git init
git remote add origin https://github.com/YOUR_USERNAME/FarmDirectoryUpgraded.git
```

---

## Step 2: Commit Your Changes

```bash
# Add all changes
git add .

# Commit with message
git commit -m "Fix GPS, attendance check-out, QR code scanning, and route optimization

- Implemented employee attendance tracking with check-in/check-out
- Integrated ML Kit for QR code scanning (format: EMP:ID|FARM:Name|TASK:Task)
- Fixed GPS location tracking to use WebSocket updates
- Improved route optimization with realistic time/fuel estimates
- Added comprehensive documentation"

# View commit history
git log --oneline | head -5
```

---

## Step 3: Push to GitHub

```bash
# Push to main branch (triggers CI/CD)
git push -u origin main

# OR push to develop branch
git push -u origin develop

# Check push status
git status
```

---

## Step 4: Monitor Build on GitHub

### View Build Status

1. Go to: `https://github.com/YOUR_USERNAME/FarmDirectoryUpgraded`
2. Click **Actions** tab
3. Watch the workflow run in real-time

### Workflow Steps

The CI/CD pipeline runs 4 jobs in sequence:

#### 1. Lint Analysis ✓
- Checks code quality
- Reports style issues
- Duration: ~5 minutes

#### 2. Unit Tests ✓
- Runs all unit tests
- Generates coverage reports
- Duration: ~10 minutes

#### 3. Build APKs ✓
- Builds 4 variants:
  - devDebug
  - devStaging
  - prodDebug
  - prodStaging
- Duration: ~15 minutes

#### 4. Quality Gate
- Checks if all jobs passed
- Shows summary

---

## Step 5: Download Your APK

### From GitHub Actions

1. Go to: **Actions** tab
2. Click the latest workflow run
3. Scroll to **Artifacts** section
4. Download `apk-devDebug.zip` (or your preferred variant)
5. Extract the APK file

### APK Variants Available

| Variant | Purpose | Size |
|---------|---------|------|
| apk-devDebug | Development testing | ~15 MB |
| apk-devStaging | Staging environment | ~15 MB |
| apk-prodDebug | Production debug | ~15 MB |
| apk-prodStaging | Production staging | ~15 MB |

---

## Step 6: Install on Phone

### Option A: Direct from Downloads
```bash
# Download APK from GitHub
# Transfer to phone's Downloads folder

# On phone:
1. Open Files app
2. Navigate to Downloads
3. Tap the APK
4. Install
```

### Option B: Via ADB
```bash
# Download APK from GitHub to your computer
cd ~/Downloads/

# Install via ADB
adb install -r app-dev-debug.apk
```

### Option C: Create Release

Create a GitHub Release with the APK:

1. Go to **Releases** tab
2. Click **Create a new release**
3. Tag version: `v2.0`
4. Title: `Farm Directory 2.0 - Attendance Fixes`
5. Upload APK file
6. Publish release
7. Share release link with users

---

## What the CI/CD Pipeline Does

### Automatic Checks
✓ Lint - Code quality analysis
✓ Unit Tests - Functionality verification
✓ Build - APK compilation for all variants
✓ Security - Dependency analysis

### Artifacts Generated
✓ APK files (4 variants)
✓ Lint reports
✓ Test reports
✓ Coverage reports
✓ Dependency list

---

## Build Status Indicators

### ✅ Success
All jobs passed - APK is ready to download

```
✅ lint: success
✅ unit-test: success
✅ build: success
✅ security: success
✅ quality-gate: success
```

### ⚠️ Warnings
Build succeeded but with warnings

```
⚠️ Lint warnings present
⚠️ Unit test warnings present
✅ Build artifact created
```

### ❌ Failure
Build failed - check error details

```
❌ Build failed
```

---

## Troubleshooting CI/CD Issues

### Issue: Build Fails

**Check the logs:**
1. Go to Actions → Failed workflow
2. Click on the failed job
3. Expand "Build APKs" or "Run Unit Tests"
4. Read error message

**Common causes:**
- Gradle dependency issue
- Java version mismatch
- Syntax error in code
- Missing file

### Issue: APK Not Generated

**Check:**
1. Did the "Build APKs" job complete successfully?
2. Go to Artifacts section
3. If missing, check build logs for errors

**Solution:**
- Fix reported errors
- Push corrected code
- CI/CD will automatically rebuild

### Issue: Very Slow Build

**Normal timings:**
- Lint: 5 min
- Tests: 10 min
- Build: 15 min
- Total: ~30 minutes

**If slower:**
- Check server load
- GitHub Actions has rate limits
- Wait for previous builds to complete

---

## Advanced: Customize CI/CD

The workflow file is at:
```
.github/workflows/android-ci.yml
```

### To Modify Build Behavior

Edit `android-ci.yml`:

```yaml
# Change build variants
matrix:
  variant:
    - devDebug
    - prodRelease  # Add this

# Change Java version
java-version: '17'  # Change to 11 or 21

# Change artifact retention
retention-days: 30  # Change to 60
```

After editing, commit and push to apply changes.

---

## Manual Build Trigger

You can manually trigger the workflow without pushing:

1. Go to **Actions** tab
2. Click **Enhanced Android CI** workflow
3. Click **Run workflow**
4. Select branch: `main`
5. Click **Run workflow**

---

## Integration with Other Tools

### Slack Notifications
Add to your repo settings:
1. GitHub → Settings → Integrations
2. Add Slack workspace
3. Get build notifications in Slack

### Email Notifications
GitHub automatically emails if:
- Build fails
- Test fails
- You're watching the repo

Configure in **GitHub Settings → Notifications**

### Discord Webhook
Use GitHub's webhook to notify Discord channel:
1. Get Discord webhook URL
2. Go to repo Settings → Webhooks
3. Add Discord webhook
4. Get build notifications in Discord

---

## Commit Messages Best Practices

### Good Commit Message Format
```
[Feature/Fix/Docs] Brief description

Detailed explanation of what changed and why.

- Point 1
- Point 2
- Point 3
```

### Example
```
[Fix] Implement employee attendance check-out

- Added checkOutAttendance() function with hours calculation
- Integrated employee selection system
- Updated database records on check-out
- Added error handling and logging

Fixes: GPS not correct, attendance not changed, QR code not working
```

---

## Release Management

### Creating a Release

```bash
# Tag the commit
git tag -a v2.0 -m "Farm Directory 2.0 - Attendance Fixes"
git push origin v2.0

# GitHub will:
# 1. Create Release page
# 2. Download APK artifacts
# 3. Attach to release
# 4. Mark as "Latest Release"
```

### Semantic Versioning
- v2.0.0 - Major release (new features)
- v2.0.1 - Minor release (bug fixes)
- v2.0.0-rc1 - Release candidate

---

## Performance Tips

### Speed Up Build
1. Push only when ready (avoid multiple quick pushes)
2. Use tags for releases (cleaner history)
3. Keep branches clean (delete merged branches)

### Reduce Build Time
- Disable unnecessary lint checks
- Skip tests for documentation changes
- Use `paths-ignore` in workflow

Example:
```yaml
on:
  push:
    paths-ignore:
      - '**.md'      # Skip on docs changes
      - 'docs/**'
      - '.gitignore'
```

---

## Security Considerations

### Secrets Management
If you need to store secrets (API keys, credentials):

1. Go to **Settings → Secrets and variables → Actions**
2. Create new secret
3. Use in workflow: `${{ secrets.SECRET_NAME }}`

### Never Commit
- API keys
- Credentials
- Private tokens
- Passwords

---

## Monitoring & Alerts

### Check Build History
```
https://github.com/YOUR_USERNAME/FarmDirectoryUpgraded/actions
```

### Watch Repository
1. Click **Watch** (bell icon)
2. Select **All activity** or **Releases only**
3. Get notified of builds

### Badge for README
Add to README.md:
```markdown
![Build Status](https://github.com/YOUR_USERNAME/FarmDirectoryUpgraded/workflows/Enhanced%20Android%20CI/badge.svg)
```

---

## Next Steps

### 1. Push Code to GitHub ✓
```bash
git push -u origin main
```

### 2. Monitor Build
Watch Actions tab for workflow to complete

### 3. Download APK
Get APK from Artifacts section

### 4. Install on Phone
Use ADB or file manager

### 5. Test Features
Verify all fixes work on device

### 6. Celebrate! 🎉
You've successfully:
- Fixed all 4 issues
- Set up CI/CD
- Built the APK automatically
- Deployed to your phone

---

## Helpful Commands

```bash
# Check git status
git status

# View recent commits
git log --oneline | head -10

# View branches
git branch -a

# Switch branch
git checkout develop

# Delete local branch
git branch -d branch-name

# View remote info
git remote -v

# Update from remote
git pull origin main

# Stash uncommitted changes
git stash

# View commit details
git show HEAD

# Check for uncommitted changes
git diff
```

---

## References

- **GitHub Actions Docs**: https://docs.github.com/en/actions
- **Gradle Build Guide**: https://gradle.org/
- **Android CI/CD**: https://developer.android.com/studio/build/continuous-integration

---

## Support

If build fails:
1. Check the workflow logs
2. Review error messages
3. Fix code locally
4. Push corrected code
5. CI/CD will automatically rebuild

---

**Last Updated**: 2025-12-26
**Status**: Ready to Push & Build ✅
