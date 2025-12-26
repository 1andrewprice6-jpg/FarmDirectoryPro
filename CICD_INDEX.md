# CI/CD Pipeline Documentation Index

## Quick Navigation

This index helps you find the right documentation for your needs.

## For First-Time Setup

**Start here:** [CICD_SETUP_GUIDE.md](./CICD_SETUP_GUIDE.md)
- Step-by-step setup instructions
- GitHub secrets configuration
- Branch protection setup
- First release guide

## For Daily Development

**Start here:** [QUICK_CICD_REFERENCE.md](./QUICK_CICD_REFERENCE.md)
- Common commands
- Quick fixes
- Build variants
- Workflow triggers

## For Understanding the System

**Start here:** [PIPELINE_ARCHITECTURE.md](./PIPELINE_ARCHITECTURE.md)
- Visual diagrams
- Workflow flows
- Build matrix
- Architecture overview

## For Detailed Information

**Start here:** [.github/workflows/CICD_DOCUMENTATION.md](./.github/workflows/CICD_DOCUMENTATION.md)
- Complete workflow details
- Configuration options
- Troubleshooting guide
- Best practices

## For Overview

**Start here:** [CICD_ENHANCEMENTS_SUMMARY.md](./CICD_ENHANCEMENTS_SUMMARY.md)
- What was changed
- Features delivered
- Before vs after
- Complete file list

## Documentation by Role

### Developers

1. **Getting Started**
   - [QUICK_CICD_REFERENCE.md](./QUICK_CICD_REFERENCE.md) - Daily commands
   - [.github/PULL_REQUEST_TEMPLATE.md](./.github/PULL_REQUEST_TEMPLATE.md) - PR guidelines

2. **Testing**
   - [CICD_DOCUMENTATION.md](./.github/workflows/CICD_DOCUMENTATION.md#common-tasks) - Running tests
   - [app/build.gradle.kts](./app/build.gradle.kts) - Build configuration

3. **Troubleshooting**
   - [CICD_SETUP_GUIDE.md](./CICD_SETUP_GUIDE.md#troubleshooting) - Common issues
   - [QUICK_CICD_REFERENCE.md](./QUICK_CICD_REFERENCE.md#quick-fixes) - Quick fixes

### DevOps/Maintainers

1. **Setup**
   - [CICD_SETUP_GUIDE.md](./CICD_SETUP_GUIDE.md) - Complete setup
   - [CICD_ENHANCEMENTS_SUMMARY.md](./CICD_ENHANCEMENTS_SUMMARY.md#required-github-secrets) - Secrets

2. **Workflows**
   - [CICD_DOCUMENTATION.md](./.github/workflows/CICD_DOCUMENTATION.md#workflows) - Workflow details
   - [PIPELINE_ARCHITECTURE.md](./PIPELINE_ARCHITECTURE.md) - Architecture

3. **Monitoring**
   - [CICD_DOCUMENTATION.md](./.github/workflows/CICD_DOCUMENTATION.md#monitoring) - Monitoring guide
   - [codecov.yml](./codecov.yml) - Coverage config

### Project Managers

1. **Overview**
   - [CICD_ENHANCEMENTS_SUMMARY.md](./CICD_ENHANCEMENTS_SUMMARY.md) - Complete summary
   - [PIPELINE_VERIFICATION.txt](./PIPELINE_VERIFICATION.txt) - Verification checklist

2. **Release Process**
   - [CICD_DOCUMENTATION.md](./.github/workflows/CICD_DOCUMENTATION.md#creating-a-release) - Release guide
   - [QUICK_CICD_REFERENCE.md](./QUICK_CICD_REFERENCE.md#version-release) - Quick release

## Documentation by Topic

### Testing

- **Unit Tests**: [CICD_DOCUMENTATION.md](./.github/workflows/CICD_DOCUMENTATION.md#running-tests-locally)
- **Coverage**: [codecov.yml](./codecov.yml)
- **Test Files**:
  - [app/src/test/java/.../ExampleUnitTest.kt](./app/src/test/java/com/example/farmdirectoryupgraded/ExampleUnitTest.kt)
  - [app/src/androidTest/java/.../ExampleInstrumentedTest.kt](./app/src/androidTest/java/com/example/farmdirectoryupgraded/ExampleInstrumentedTest.kt)

### Code Quality

- **Lint**: [app/build.gradle.kts](./app/build.gradle.kts#L178-L188) - Lint configuration
- **Quality Standards**: [CICD_DOCUMENTATION.md](./.github/workflows/CICD_DOCUMENTATION.md#quality-standards)

### Security

- **Dependency Scanning**: [.github/workflows/pr-validation.yml](./.github/workflows/pr-validation.yml#L128-L156)
- **Dependabot**: [.github/dependabot.yml](./.github/dependabot.yml)

### Build & Deploy

- **Build Variants**: [PIPELINE_ARCHITECTURE.md](./PIPELINE_ARCHITECTURE.md#build-variants-matrix)
- **Release Process**: [.github/workflows/release.yml](./.github/workflows/release.yml)
- **Version Management**: [.github/workflows/version-bump.yml](./.github/workflows/version-bump.yml)

### Workflows

- **PR Validation**: [.github/workflows/pr-validation.yml](./.github/workflows/pr-validation.yml)
- **Main CI**: [.github/workflows/android-ci.yml](./.github/workflows/android-ci.yml)
- **Release**: [.github/workflows/release.yml](./.github/workflows/release.yml)
- **Version Bump**: [.github/workflows/version-bump.yml](./.github/workflows/version-bump.yml)
- **Dependencies**: [.github/workflows/dependency-update.yml](./.github/workflows/dependency-update.yml)

## Documentation by Question

### "How do I...?"

**...set up the pipeline?**
→ [CICD_SETUP_GUIDE.md](./CICD_SETUP_GUIDE.md)

**...run tests locally?**
→ [QUICK_CICD_REFERENCE.md](./QUICK_CICD_REFERENCE.md#common-commands)

**...create a release?**
→ [CICD_DOCUMENTATION.md](./.github/workflows/CICD_DOCUMENTATION.md#creating-a-release)

**...fix a failing build?**
→ [CICD_SETUP_GUIDE.md](./CICD_SETUP_GUIDE.md#troubleshooting)

**...understand the workflows?**
→ [PIPELINE_ARCHITECTURE.md](./PIPELINE_ARCHITECTURE.md)

**...configure secrets?**
→ [CICD_SETUP_GUIDE.md](./CICD_SETUP_GUIDE.md#step-1-configure-github-secrets)

**...build a specific variant?**
→ [QUICK_CICD_REFERENCE.md](./QUICK_CICD_REFERENCE.md#build-specific-variants)

**...update dependencies?**
→ [CICD_DOCUMENTATION.md](./.github/workflows/CICD_DOCUMENTATION.md#dependency-updates)

### "What is...?"

**...the pipeline architecture?**
→ [PIPELINE_ARCHITECTURE.md](./PIPELINE_ARCHITECTURE.md)

**...changed in this update?**
→ [CICD_ENHANCEMENTS_SUMMARY.md](./CICD_ENHANCEMENTS_SUMMARY.md)

**...the coverage requirement?**
→ [CICD_DOCUMENTATION.md](./.github/workflows/CICD_DOCUMENTATION.md#quality-standards)

**...each workflow for?**
→ [CICD_DOCUMENTATION.md](./.github/workflows/CICD_DOCUMENTATION.md#workflows)

**...each build variant?**
→ [PIPELINE_ARCHITECTURE.md](./PIPELINE_ARCHITECTURE.md#build-variants-matrix)

## Quick Reference Table

| Need | Document | Section |
|------|----------|---------|
| Setup instructions | CICD_SETUP_GUIDE.md | All |
| Daily commands | QUICK_CICD_REFERENCE.md | Common Commands |
| Workflow details | CICD_DOCUMENTATION.md | Workflows |
| Architecture | PIPELINE_ARCHITECTURE.md | All |
| Troubleshooting | CICD_SETUP_GUIDE.md | Troubleshooting |
| Release process | CICD_DOCUMENTATION.md | Creating a Release |
| Test guide | CICD_DOCUMENTATION.md | Common Tasks |
| Build variants | PIPELINE_ARCHITECTURE.md | Build Variants |
| Secrets config | CICD_SETUP_GUIDE.md | Step 1 |
| PR template | PULL_REQUEST_TEMPLATE.md | All |

## File Locations

### Workflows
```
.github/workflows/
├── pr-validation.yml          # PR validation
├── android-ci.yml             # Main CI
├── release.yml                # Production release
├── version-bump.yml           # Version management
├── dependency-update.yml      # Dependency updates
└── CICD_DOCUMENTATION.md      # Workflow docs
```

### Configuration
```
.github/
├── dependabot.yml            # Dependency automation
└── PULL_REQUEST_TEMPLATE.md  # PR template

codecov.yml                   # Coverage config
```

### Documentation
```
CICD_SETUP_GUIDE.md           # Setup guide
CICD_ENHANCEMENTS_SUMMARY.md  # Summary
QUICK_CICD_REFERENCE.md       # Quick ref
PIPELINE_ARCHITECTURE.md      # Architecture
PIPELINE_VERIFICATION.txt     # Checklist
CICD_INDEX.md                 # This file
```

### Tests
```
app/src/
├── test/java/...
│   └── ExampleUnitTest.kt    # Unit test example
└── androidTest/java/...
    └── ExampleInstrumentedTest.kt  # UI test example
```

### Build
```
app/build.gradle.kts          # Build configuration
```

## Recommended Reading Order

### For New Team Members
1. CICD_ENHANCEMENTS_SUMMARY.md - Overview
2. QUICK_CICD_REFERENCE.md - Daily usage
3. CICD_SETUP_GUIDE.md - If setting up

### For Setting Up
1. CICD_SETUP_GUIDE.md - Complete guide
2. PIPELINE_VERIFICATION.txt - Checklist
3. CICD_DOCUMENTATION.md - Reference

### For Understanding
1. PIPELINE_ARCHITECTURE.md - Visual overview
2. CICD_ENHANCEMENTS_SUMMARY.md - Details
3. CICD_DOCUMENTATION.md - Deep dive

## Getting Help

1. **Check the docs** - Start with this index
2. **Check workflow logs** - GitHub Actions tab
3. **Review artifacts** - Download from workflow runs
4. **Read error messages** - Often self-explanatory
5. **Create an issue** - With logs and details

## Contributing

When updating CI/CD:
1. Update relevant documentation
2. Test changes in a branch
3. Update this index if adding files
4. Create PR with documentation updates

## Version

**Pipeline Version:** 1.0.0
**Last Updated:** 2025-12-25
**Status:** Production Ready

---

**Need quick help?** Start with [QUICK_CICD_REFERENCE.md](./QUICK_CICD_REFERENCE.md)

**Setting up?** Start with [CICD_SETUP_GUIDE.md](./CICD_SETUP_GUIDE.md)

**Want details?** Start with [CICD_DOCUMENTATION.md](./.github/workflows/CICD_DOCUMENTATION.md)
