# Quick CI/CD Reference Guide

## Quick Start

### 1. First Time Setup (5 minutes)
1. Add GitHub secrets (Settings → Secrets → Actions):
   - `SIGNING_KEY`, `SIGNING_ALIAS`, `KEY_STORE_PASSWORD`, `KEY_PASSWORD`
2. Enable Dependabot (Settings → Code security → Enable all)
3. Set branch protection on `main` (Settings → Branches)

### 2. Daily Development

**Create a feature:**
```bash
git checkout -b feature/my-feature
# Make changes
./gradlew testDevDebugUnitTest  # Run tests locally
git commit -am "feat: add my feature"
git push origin feature/my-feature
# Create PR on GitHub → checks run automatically
```

**Merge when:**
- All checks pass ✅
- Coverage meets threshold ✅
- Code reviewed ✅

### 3. Release Process

**Option A: Automated Version Bump**
1. GitHub → Actions → Version Bump
2. Select bump type (patch/minor/major)
3. Check "create_release" ✅
4. Run workflow
5. Done! APK created automatically

**Option B: Manual Tag**
```bash
git tag -a v1.2.3 -m "Release 1.2.3"
git push origin v1.2.3
# Release workflow triggers automatically
```

## Common Commands

### Local Testing
```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew testDevDebugUnitTest jacocoTestReport

# View coverage report
open app/build/reports/jacoco/jacocoTestReport/html/index.html

# Run lint
./gradlew lintDevDebug

# Build all variants
./gradlew assemble
```

### Build Specific Variants
```bash
# Development debug (most common)
./gradlew assembleDevDebug

# Production staging (for testing)
./gradlew assembleProdStaging

# Production release (for distribution)
./gradlew assembleProdRelease
```

## Workflows At A Glance

| Workflow | When | What | Duration |
|----------|------|------|----------|
| **PR Validation** | Open/update PR | Tests, lint, coverage | 10-15 min |
| **Enhanced CI** | Merge to main | Build all, security scan | 8-12 min |
| **Release** | Tag `v*.*.*` | Build APK/AAB, sign, publish | 12-15 min |
| **Version Bump** | Manual | Update version, tag | 2-3 min |
| **Dependencies** | Weekly Mon 9am | Check updates | 5-8 min |

## Build Variants Quick Reference

| Variant | Environment | API URL | Use Case |
|---------|-------------|---------|----------|
| `devDebug` | Dev | dev-api.farmdirectory.com | Daily development |
| `devStaging` | Dev | staging-api... | Pre-testing dev |
| `prodDebug` | Prod | api.farmdirectory.com | Debug production issues |
| `prodStaging` | Prod | staging-api... | Pre-production testing |
| `prodRelease` | Prod | api.farmdirectory.com | Production release |

## Secrets Required

### Must Have (for releases)
- `SIGNING_KEY` - Base64 keystore
- `SIGNING_ALIAS` - Key alias
- `KEY_STORE_PASSWORD` - Keystore pwd
- `KEY_PASSWORD` - Key pwd

### Optional
- `PLAY_STORE_JSON_KEY` - Play Store upload
- `CODECOV_TOKEN` - Coverage tracking

## Quick Fixes

### "Tests failing in CI but pass locally"
```bash
# Run in same environment as CI
./gradlew clean test --info
```

### "Coverage too low"
```bash
# Check what needs tests
./gradlew jacocoTestReport
open app/build/reports/jacoco/jacocoTestReport/html/index.html
# Write tests for uncovered code
```

### "Lint errors blocking PR"
```bash
# See all lint issues
./gradlew lintDevDebug
open app/build/reports/lint/lint-results.html
# Fix issues or suppress if valid
```

### "Build fails on signing"
```bash
# Test keystore locally
keytool -list -v -keystore your-keystore.jks
# Verify secrets are set correctly in GitHub
```

## Version Numbers

**Semantic Versioning: MAJOR.MINOR.PATCH**

- **MAJOR** (1.0.0 → 2.0.0): Breaking changes
- **MINOR** (1.0.0 → 1.1.0): New features
- **PATCH** (1.0.0 → 1.0.1): Bug fixes

**Version Code:** Auto-calculated
- Formula: `MAJOR * 10000 + MINOR * 100 + PATCH`
- Example: 1.2.3 → 10203

## Important Files

```
.github/workflows/
├── pr-validation.yml       # PR checks
├── android-ci.yml          # Main CI
├── release.yml             # Production releases
├── version-bump.yml        # Version management
├── dependency-update.yml   # Dependency updates
└── CICD_DOCUMENTATION.md   # Full docs

app/build.gradle.kts        # Build config
codecov.yml                 # Coverage config
CICD_SETUP_GUIDE.md        # Setup instructions
```

## Monitoring

### Check Workflow Status
- GitHub → Actions tab
- Green ✅ = passed
- Red ❌ = failed (click for logs)

### Check Coverage
- PR comments (auto-posted)
- Codecov dashboard (if configured)
- Local reports in `app/build/reports/`

### Check Dependencies
- Dependabot PRs (weekly)
- Security alerts (GitHub UI)
- Dependency reports in artifacts

## Best Practices

✅ **DO:**
- Run tests before pushing
- Keep PRs focused and small
- Write tests for new features
- Review Dependabot PRs weekly
- Use Version Bump for releases

❌ **DON'T:**
- Push directly to main
- Skip CI checks
- Ignore failing tests
- Decrease code coverage
- Manually edit version numbers

## Getting Help

1. Check workflow logs (Actions → failed run → job → step)
2. Review `.github/workflows/CICD_DOCUMENTATION.md`
3. Read `CICD_SETUP_GUIDE.md` for setup issues
4. Check `CICD_ENHANCEMENTS_SUMMARY.md` for overview

## Status Badges (Add to README)

```markdown
![CI](https://github.com/YOUR_USERNAME/FarmDirectoryUpgraded/workflows/Enhanced%20Android%20CI/badge.svg)
![PR Validation](https://github.com/YOUR_USERNAME/FarmDirectoryUpgraded/workflows/Pull%20Request%20Validation/badge.svg)
[![codecov](https://codecov.io/gh/YOUR_USERNAME/FarmDirectoryUpgraded/branch/main/graph/badge.svg)](https://codecov.io/gh/YOUR_USERNAME/FarmDirectoryUpgraded)
```

Replace `YOUR_USERNAME` with your GitHub username.

## Emergency: Hotfix Process

```bash
# 1. Create hotfix branch from main
git checkout main
git pull
git checkout -b hotfix/critical-fix

# 2. Make minimal fix
# ... edit files ...

# 3. Test thoroughly
./gradlew test
./gradlew assembleProdRelease

# 4. Create PR (fast-track review)
git commit -am "fix: critical bug"
git push origin hotfix/critical-fix
# Create PR, get quick approval

# 5. After merge, create hotfix release
git checkout main
git pull
git tag -a v1.2.4 -m "Hotfix: critical bug"
git push origin v1.2.4

# 6. Monitor release workflow
# 7. Verify fix in production
```

## One-Pagers

### For Developers
"Run `./gradlew test` before pushing. Create PR. Wait for checks. Merge when green."

### For Releases
"Actions → Version Bump → Select patch/minor/major → Check create_release → Run. Done."

### For Reviewers
"Check: Tests pass? Coverage OK? Lint clean? Code looks good? Approve."

---

**Need more details?** See `CICD_SETUP_GUIDE.md` and `.github/workflows/CICD_DOCUMENTATION.md`
