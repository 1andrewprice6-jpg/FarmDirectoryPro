# CI/CD Pipeline Documentation

## Overview

This project uses GitHub Actions for continuous integration and deployment. The pipeline includes automated testing, code quality checks, security scanning, and deployment automation.

## Workflows

### 1. Pull Request Validation (`pr-validation.yml`)

**Trigger:** Pull requests to `main`, `master`, or `develop` branches

**Purpose:** Validate code quality before merging

**Jobs:**
- **Code Quality:** Runs lint checks and uploads reports
- **Unit Tests:** Executes unit tests with coverage reporting
- **Instrumentation Tests:** Runs Android UI tests on emulator
- **Build Validation:** Builds all variants to ensure compilation
- **Security Scan:** OWASP dependency vulnerability checking
- **PR Summary:** Aggregates results for easy review

**Artifacts:**
- Lint results (7 days)
- Test results (7 days)
- Coverage reports (7 days)
- APKs for each variant (7 days)
- Security reports (7 days)

### 2. Enhanced Android CI (`android-ci.yml`)

**Trigger:** Push to `main`, `master`, or `develop` branches

**Purpose:** Continuous integration for merged code

**Jobs:**
- **Lint Analysis:** Code style and quality checks
- **Unit Tests & Coverage:** Test execution with Jacoco coverage
- **Build APKs:** Matrix build for all variants (dev/prod x debug/staging)
- **Security Analysis:** Dependency analysis
- **Quality Gate:** Pass/fail gate based on all checks

**Features:**
- Codecov integration for coverage tracking
- Super-linter for additional code quality
- Concurrency control to cancel outdated runs
- Comprehensive test result publishing

**Artifacts:**
- Lint reports (14 days)
- Test reports (14 days)
- Coverage reports (14 days)
- APKs for all variants (30 days)
- Dependency reports (14 days)

### 3. Production Release (`release.yml`)

**Trigger:**
- Git tags matching `v*.*.*` pattern
- Manual workflow dispatch

**Purpose:** Automated production release process

**Jobs:**
1. **Pre-release Validation**
   - Run all production tests
   - Run lint checks

2. **Build Release**
   - Extract version from tag or input
   - Calculate version code
   - Build signed APK and AAB
   - Generate checksums

3. **Create GitHub Release**
   - Generate changelog from commits
   - Create release with APK/AAB attachments
   - Include checksums for verification

4. **Deploy to Play Store** (optional)
   - Upload AAB to Google Play Console
   - Requires `PLAY_STORE_JSON_KEY` secret

**Manual Release:**
```bash
# Via GitHub UI: Actions -> Production Release -> Run workflow
# Or via CLI:
gh workflow run release.yml -f version=1.2.3 -f release_notes="Bug fixes and improvements"
```

**Automatic Release:**
```bash
git tag -a v1.2.3 -m "Release version 1.2.3"
git push origin v1.2.3
```

### 4. Version Bump (`version-bump.yml`)

**Trigger:** Manual workflow dispatch

**Purpose:** Automated version management

**Inputs:**
- `bump_type`: patch, minor, or major
- `create_release`: Whether to create a release tag

**Process:**
1. Reads current version from `build.gradle.kts`
2. Calculates new version based on semver
3. Updates version code and name
4. Generates changelog
5. Commits changes
6. Optionally creates release tag

**Usage:**
```bash
# Via GitHub UI: Actions -> Version Bump -> Run workflow
# Select bump type and whether to create release

# Via CLI:
gh workflow run version-bump.yml -f bump_type=minor -f create_release=true
```

### 5. Dependency Updates (`dependency-update.yml`)

**Trigger:**
- Weekly schedule (Monday 9 AM UTC)
- Manual dispatch

**Purpose:** Keep dependencies up to date

**Features:**
- Checks for outdated dependencies
- Generates update reports
- Auto-merges Dependabot patch updates
- Comments on major/minor updates for review

## Build Variants

The project uses Gradle build variants for different environments:

### Build Types
- **debug:** Development builds with debugging enabled
  - API: `https://dev-api.farmdirectory.com`
  - Coverage enabled

- **staging:** Pre-production testing
  - API: `https://staging-api.farmdirectory.com`
  - Minification enabled
  - Suffix: `.staging`

- **release:** Production builds
  - API: `https://api.farmdirectory.com`
  - Minification and shrinking enabled
  - Requires signing configuration

### Product Flavors
- **dev:** Development environment
  - Suffix: `.dev`

- **prod:** Production environment

### Combined Variants
The matrix creates these combinations:
- `devDebug` - Development debug build
- `devStaging` - Development staging build
- `devRelease` - Development release build (not used typically)
- `prodDebug` - Production debug build
- `prodStaging` - Production staging build
- `prodRelease` - Production release build

## Required Secrets

Configure these in GitHub Settings > Secrets and variables > Actions:

### Release Signing (Required for signed releases)
- `SIGNING_KEY` - Base64 encoded keystore file
- `SIGNING_ALIAS` - Keystore alias
- `KEY_STORE_PASSWORD` - Keystore password
- `KEY_PASSWORD` - Key password

### Play Store Deployment (Optional)
- `PLAY_STORE_JSON_KEY` - Google Play service account JSON

### Code Coverage (Optional)
- `CODECOV_TOKEN` - Codecov upload token

## Quality Standards

### Coverage Requirements
- Overall coverage: 40% minimum
- Changed files: 60% minimum

### Lint Configuration
- Abort on error: Yes
- Warnings as errors: No
- Reports: XML and HTML

### Test Requirements
All PRs must pass:
- Unit tests
- Lint checks
- Build for all variants

## Common Tasks

### Running Tests Locally
```bash
# Unit tests
./gradlew testDevDebugUnitTest

# All tests
./gradlew test

# With coverage
./gradlew testDevDebugUnitTest jacocoTestReport
```

### Building Locally
```bash
# Debug build
./gradlew assembleDevDebug

# Release build (staging)
./gradlew assembleProdStaging

# All variants
./gradlew assemble
```

### Lint Checks
```bash
# Run lint
./gradlew lintDevDebug

# All variants
./gradlew lint
```

### Creating a Release

1. **Manual via UI:**
   - Go to Actions → Version Bump
   - Select bump type (patch/minor/major)
   - Check "create_release" if ready to release
   - Run workflow
   - If release tag created, Production Release will trigger automatically

2. **Manual via Tag:**
   ```bash
   git tag -a v1.2.3 -m "Release 1.2.3"
   git push origin v1.2.3
   ```

3. **Direct Release Trigger:**
   - Go to Actions → Production Release
   - Click "Run workflow"
   - Enter version and release notes
   - Run workflow

## Troubleshooting

### Build Failures

**Lint Errors:**
- Check lint reports in artifacts
- Run `./gradlew lintDevDebug` locally
- Fix or suppress issues in `lint.xml`

**Test Failures:**
- Download test reports from artifacts
- Run specific test: `./gradlew test --tests ClassName.testName`
- Check test logs in `app/build/reports/tests/`

**Coverage Too Low:**
- Add more unit tests
- Adjust coverage thresholds in `pr-validation.yml`

### Release Issues

**Signing Fails:**
- Verify all signing secrets are configured
- Check keystore is valid base64
- Verify passwords are correct

**Version Conflicts:**
- Ensure version in build.gradle.kts matches tag
- Use Version Bump workflow for consistency

**Play Store Upload Fails:**
- Verify service account JSON is valid
- Check package name matches
- Ensure version code is incremented

## Best Practices

1. **Always create PRs for changes**
   - PRs trigger validation
   - Get coverage reports
   - Catch issues early

2. **Use Version Bump workflow**
   - Maintains semver consistency
   - Auto-generates changelog
   - Prevents version conflicts

3. **Review dependency updates**
   - Patch updates auto-merge
   - Review minor/major updates
   - Test after updates

4. **Monitor coverage trends**
   - Check Codecov reports
   - Maintain or improve coverage
   - Write tests for new features

5. **Tag releases properly**
   - Use semantic versioning
   - Write meaningful release notes
   - Test before tagging

## Pipeline Diagram

```
PR Created
    ↓
PR Validation
    ├─ Lint
    ├─ Unit Tests
    ├─ Instrumentation Tests
    ├─ Build All Variants
    └─ Security Scan
    ↓
Merge to Main
    ↓
Enhanced CI
    ├─ Lint Analysis
    ├─ Unit Tests + Coverage
    ├─ Build Matrix
    ├─ Security Analysis
    └─ Quality Gate
    ↓
Version Bump (Manual)
    ├─ Update version
    ├─ Generate changelog
    └─ Create tag (optional)
    ↓
Tag Created
    ↓
Production Release
    ├─ Pre-release Validation
    ├─ Build APK/AAB
    ├─ Sign Artifacts
    ├─ Create GitHub Release
    └─ Deploy to Play Store (optional)
```

## Monitoring

### GitHub Actions
- Check Actions tab for workflow runs
- Review artifacts for reports
- Monitor job summaries

### Codecov
- Visit Codecov dashboard
- Track coverage trends
- Review file-level coverage

### Dependabot
- Review dependency PRs
- Check security alerts
- Monitor update frequency

## Support

For issues with the CI/CD pipeline:
1. Check workflow logs in GitHub Actions
2. Review this documentation
3. Check artifacts for detailed reports
4. Create an issue with logs and error messages
