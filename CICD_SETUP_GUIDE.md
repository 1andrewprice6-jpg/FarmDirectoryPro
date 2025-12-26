# CI/CD Pipeline Setup Guide

## Overview

This guide will help you set up and configure the complete CI/CD pipeline for the Farm Directory Android app.

## Prerequisites

- GitHub repository with admin access
- Android signing keystore (for releases)
- Google Play Console access (optional, for Play Store deployment)

## Step 1: Configure GitHub Secrets

Navigate to your GitHub repository → Settings → Secrets and variables → Actions

### Required Secrets for Signed Releases

1. **SIGNING_KEY**
   ```bash
   # Convert your keystore to base64
   base64 -i your-keystore.jks | pbcopy
   # Or on Linux:
   base64 -w 0 your-keystore.jks
   ```
   Paste the output as the secret value

2. **SIGNING_ALIAS**
   - Your keystore alias (e.g., "release")

3. **KEY_STORE_PASSWORD**
   - Your keystore password

4. **KEY_PASSWORD**
   - Your key password (may be same as keystore password)

### Optional Secrets

5. **PLAY_STORE_JSON_KEY** (for Play Store deployment)
   - Google Play service account JSON key
   - Create in Google Play Console → Setup → API access

6. **CODECOV_TOKEN** (for coverage reporting)
   - Sign up at codecov.io
   - Add your repository
   - Copy the upload token

## Step 2: Enable GitHub Actions

1. Go to repository Settings → Actions → General
2. Enable "Allow all actions and reusable workflows"
3. Set "Workflow permissions" to "Read and write permissions"
4. Check "Allow GitHub Actions to create and approve pull requests"

## Step 3: Configure Dependabot

The `.github/dependabot.yml` file is already configured. Dependabot will:
- Check for Gradle dependency updates weekly
- Check for GitHub Actions updates weekly
- Create PRs for updates automatically

To enable:
1. Go to Settings → Code security and analysis
2. Enable "Dependabot alerts"
3. Enable "Dependabot security updates"
4. Enable "Dependabot version updates"

## Step 4: Set Up Branch Protection

Recommended branch protection rules for `main` branch:

1. Go to Settings → Branches → Add rule
2. Branch name pattern: `main`
3. Enable:
   - Require a pull request before merging
   - Require approvals (at least 1)
   - Require status checks to pass before merging
     - Search and add: `Code Quality Checks`, `Unit Tests`, `Build Validation`
   - Require conversation resolution before merging
   - Do not allow bypassing the above settings

## Step 5: Configure Codecov (Optional)

1. Visit https://codecov.io
2. Sign in with GitHub
3. Add your repository
4. Copy the upload token
5. Add as `CODECOV_TOKEN` secret in GitHub

Benefits:
- Visual coverage reports
- PR comments with coverage changes
- Coverage trends over time
- Badge for README

## Step 6: Create Initial Release

### Option A: Using Version Bump Workflow

1. Go to Actions → Version Bump
2. Click "Run workflow"
3. Select branch: `main`
4. Bump type: `minor` or `major`
5. Check "create_release"
6. Click "Run workflow"

This will:
- Update version in build.gradle.kts
- Generate changelog
- Create release tag
- Trigger production release workflow

### Option B: Manual Tag

```bash
# Update version in app/build.gradle.kts manually
# Then create and push tag
git tag -a v1.0.0 -m "Initial release"
git push origin v1.0.0
```

## Step 7: Verify Workflows

### Test PR Workflow

1. Create a new branch:
   ```bash
   git checkout -b test/ci-pipeline
   ```

2. Make a small change (e.g., update README)

3. Commit and push:
   ```bash
   git add .
   git commit -m "test: verify CI pipeline"
   git push origin test/ci-pipeline
   ```

4. Create a Pull Request

5. Verify all checks run:
   - Code Quality Checks
   - Unit Tests
   - Instrumentation Tests (may take 15-20 minutes)
   - Build Validation
   - Security Scan

### Test Main CI Workflow

1. Merge the PR above
2. Go to Actions tab
3. Verify "Enhanced Android CI" runs
4. Check all jobs complete successfully

## Step 8: Configure Play Store Deployment (Optional)

### Prerequisites
- Google Play Console account
- App created in Play Console
- First release uploaded manually

### Setup Steps

1. **Create Service Account**
   - Go to Google Cloud Console
   - Create new project or select existing
   - Enable Google Play Developer API
   - Create service account with JSON key

2. **Grant Access**
   - Go to Play Console → Setup → API access
   - Link service account
   - Grant "Release manager" permissions

3. **Add Secret**
   - Copy JSON key content
   - Add as `PLAY_STORE_JSON_KEY` secret

4. **Update Package Name**
   - Edit `.github/workflows/release.yml`
   - Update `packageName` to match your app

5. **Test**
   - Create a test release
   - Verify it uploads to Play Console

## Step 9: Local Testing

Before pushing, test workflows locally:

### Run Tests
```bash
./gradlew testDevDebugUnitTest
./gradlew connectedDevDebugAndroidTest
```

### Run Lint
```bash
./gradlew lintDevDebug
```

### Generate Coverage Report
```bash
./gradlew testDevDebugUnitTest jacocoTestReport
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

### Build All Variants
```bash
./gradlew assemble
```

## Step 10: Monitor and Maintain

### Daily
- Review PR checks
- Merge approved PRs
- Check for failing workflows

### Weekly
- Review Dependabot PRs
- Check coverage trends
- Review security alerts

### Per Release
- Run version bump workflow
- Verify release builds successfully
- Test release APK
- Monitor Play Store deployment (if enabled)

## Workflow Triggers Summary

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| PR Validation | Pull request to main/develop | Validate code before merge |
| Enhanced CI | Push to main/develop | Continuous integration |
| Production Release | Tag `v*.*.*` or manual | Create release builds |
| Version Bump | Manual only | Update app version |
| Dependency Update | Weekly schedule or manual | Check dependencies |

## Troubleshooting

### Workflow Fails on Signing

**Issue:** Release build fails at signing step

**Solutions:**
1. Verify all signing secrets are set correctly
2. Test keystore locally:
   ```bash
   keytool -list -v -keystore your-keystore.jks
   ```
3. Ensure base64 encoding is correct (no line breaks)

### Tests Fail in CI but Pass Locally

**Issue:** Tests pass locally but fail in GitHub Actions

**Possible causes:**
1. Timezone differences
2. File path assumptions
3. Network dependencies
4. Flaky tests

**Solutions:**
1. Run tests with `--info` flag to see details
2. Make tests deterministic (no random values)
3. Mock network calls
4. Add retry logic for flaky tests

### Coverage Upload Fails

**Issue:** Codecov upload step fails

**Solutions:**
1. Check `CODECOV_TOKEN` is set
2. Verify coverage report is generated
3. Check Codecov service status
4. Add `fail_ci_if_error: false` to continue on error

### Play Store Upload Fails

**Issue:** Release doesn't upload to Play Store

**Solutions:**
1. Verify service account has correct permissions
2. Check package name matches
3. Ensure version code is incremented
4. Verify AAB is signed correctly
5. Check Play Console for error messages

## Best Practices

### For Development

1. **Always create feature branches**
   ```bash
   git checkout -b feature/new-feature
   ```

2. **Write tests for new code**
   - Unit tests for business logic
   - UI tests for critical user flows

3. **Run tests locally before pushing**
   ```bash
   ./gradlew test
   ```

4. **Keep PRs small and focused**
   - One feature/fix per PR
   - Makes review easier
   - Faster CI runs

### For Releases

1. **Use Version Bump workflow**
   - Maintains consistency
   - Auto-generates changelog
   - Prevents version conflicts

2. **Test release builds**
   - Download from artifacts
   - Test on physical device
   - Verify all features work

3. **Write meaningful release notes**
   - Highlight new features
   - List bug fixes
   - Mention breaking changes

4. **Monitor after release**
   - Check crash reports
   - Monitor user feedback
   - Be ready to hotfix

### For Maintenance

1. **Keep dependencies updated**
   - Review Dependabot PRs weekly
   - Test after updates
   - Read changelogs

2. **Monitor coverage trends**
   - Aim for increasing coverage
   - Don't decrease coverage
   - Focus on critical paths

3. **Review security alerts**
   - Act on high severity immediately
   - Plan fixes for medium severity
   - Evaluate low severity

4. **Optimize workflow performance**
   - Use caching effectively
   - Parallelize jobs when possible
   - Skip unnecessary steps

## Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Gradle Build Variants](https://developer.android.com/studio/build/build-variants)
- [Android Testing](https://developer.android.com/training/testing)
- [Codecov Documentation](https://docs.codecov.com)
- [Google Play Upload](https://github.com/r0adkll/upload-google-play)

## Support

For issues or questions:
1. Check workflow logs in Actions tab
2. Review troubleshooting section above
3. Check `.github/workflows/CICD_DOCUMENTATION.md`
4. Create an issue with details
