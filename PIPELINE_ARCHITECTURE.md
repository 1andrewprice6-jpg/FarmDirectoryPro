# CI/CD Pipeline Architecture

## Overview Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    FARM DIRECTORY CI/CD PIPELINE                 │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                        DEVELOPER WORKFLOW                         │
└──────────────────────────────────────────────────────────────────┘

    Developer                  GitHub                    Artifacts
        │                        │                           │
        │  1. Create Feature     │                           │
        │  Branch & Code         │                           │
        ├────────────────────────>                           │
        │                        │                           │
        │  2. Push Code          │                           │
        ├────────────────────────>                           │
        │                        │                           │
        │  3. Create PR          │                           │
        ├────────────────────────>                           │
        │                        │                           │
        │                  [PR Validation]                   │
        │                        │                           │
        │                    ┌───┴───┐                      │
        │                    │  Lint  │                      │
        │                    └───┬───┘                      │
        │                        │                           │
        │                    ┌───┴───────┐                  │
        │                    │Unit Tests  │                  │
        │                    └───┬────────┘                 │
        │                        │                           │
        │                    ┌───┴──────────┐               │
        │                    │ UI Tests     │               │
        │                    └───┬──────────┘               │
        │                        │                           │
        │                    ┌───┴──────────┐               │
        │                    │Build Variants│               │
        │                    └───┬──────────┘               │
        │                        │                           │
        │                    ┌───┴─────────┐                │
        │                    │Security Scan│                │
        │                    └───┬─────────┘                │
        │                        │                           │
        │  4. Review Results     │                           │
        │<────────────────────   │                           │
        │                        ├──────────────────────────>│
        │                        │    Upload Artifacts       │
        │                        │                           │
        │  5. Merge PR           │                           │
        ├────────────────────────>                           │
        │                        │                           │
        │                  [Enhanced CI]                     │
        │                        │                           │
        │                    ┌───┴──────┐                   │
        │                    │Build All │                   │
        │                    │Variants  │                   │
        │                    └───┬──────┘                   │
        │                        │                           │
        │                    ┌───┴──────────┐               │
        │                    │Quality Gate  │               │
        │                    └───┬──────────┘               │
        │                        │                           │
        │                        ├──────────────────────────>│
        │                        │    Store APKs (30d)       │
        │                        │                           │
        │  6. Bump Version       │                           │
        │  (Manual Workflow)     │                           │
        ├────────────────────────>                           │
        │                        │                           │
        │                  [Version Bump]                    │
        │                        │                           │
        │                    ┌───┴────────────┐             │
        │                    │Update Version  │             │
        │                    └───┬────────────┘             │
        │                        │                           │
        │                    ┌───┴────────────┐             │
        │                    │Create Changelog│             │
        │                    └───┬────────────┘             │
        │                        │                           │
        │                    ┌───┴────────────┐             │
        │                    │Create Tag      │             │
        │                    └───┬────────────┘             │
        │                        │                           │
        │                [Production Release]                │
        │                        │                           │
        │                    ┌───┴────────────┐             │
        │                    │Validate & Test │             │
        │                    └───┬────────────┘             │
        │                        │                           │
        │                    ┌───┴────────────┐             │
        │                    │Build APK + AAB │             │
        │                    └───┬────────────┘             │
        │                        │                           │
        │                    ┌───┴────────────┐             │
        │                    │Sign Artifacts  │             │
        │                    └───┬────────────┘             │
        │                        │                           │
        │                    ┌───┴────────────┐             │
        │                    │GitHub Release  │             │
        │                    └───┬────────────┘             │
        │                        │                           │
        │                    ┌───┴────────────┐             │
        │                    │Play Store      │             │
        │                    │(Optional)      │             │
        │                    └───┬────────────┘             │
        │                        │                           │
        │  7. Download Release   │                           │
        │<───────────────────────┤                           │
        │                        │                           │
        ▼                        ▼                           ▼
```

## Workflow Details

### 1. Pull Request Validation

```
┌─────────────────────────────────────────────────────────┐
│               PULL REQUEST VALIDATION                   │
└─────────────────────────────────────────────────────────┘

Trigger: Pull Request to main/master/develop
Duration: ~10-15 minutes

┌──────────────┐
│ Code Quality │ ──> Lint checks
└──────┬───────┘     Super-linter
       │             Upload reports
       │
┌──────▼───────┐
│  Unit Tests  │ ──> Run all unit tests
└──────┬───────┘     Generate coverage
       │             Post to PR
       │             Upload reports
       │
┌──────▼────────┐
│Instrumentation│ ──> Start emulator
│    Tests      │     Run UI tests
└──────┬────────┘     Upload results
       │
┌──────▼──────────┐
│Build Validation│ ──> Build all variants
└──────┬──────────┘    devDebug
       │               devStaging
       │               prodDebug
       │               prodStaging
       │
┌──────▼──────────┐
│ Security Scan  │ ──> OWASP dependency check
└──────┬──────────┘    Generate reports
       │
┌──────▼──────────┐
│  PR Summary    │ ──> Aggregate results
└────────────────┘     Pass/Fail status
```

### 2. Enhanced Android CI

```
┌─────────────────────────────────────────────────────────┐
│                 ENHANCED ANDROID CI                     │
└─────────────────────────────────────────────────────────┘

Trigger: Push to main/master/develop
Duration: ~8-12 minutes
Concurrency: Latest only

     ┌─────────────┐     ┌─────────────┐
     │    Lint     │     │ Unit Tests  │
     │             │     │ + Coverage  │
     └──────┬──────┘     └──────┬──────┘
            │                   │
            │      ┌────────────┼──────────────┐
            │      │            │              │
     ┌──────▼──────▼──────┐ ┌──▼──────┐  ┌───▼─────┐
     │  Build devDebug   │ │prodDebug│  │devStaging│
     └───────────────────┘ └─────────┘  └──────────┘
            │                   │              │
            └────────┬──────────┴──────────────┘
                     │
              ┌──────▼──────────┐
              │ Security Analysis│
              └──────┬───────────┘
                     │
              ┌──────▼──────────┐
              │  Quality Gate   │
              │  Pass/Fail      │
              └─────────────────┘
```

### 3. Production Release

```
┌─────────────────────────────────────────────────────────┐
│                PRODUCTION RELEASE                       │
└─────────────────────────────────────────────────────────┘

Trigger: Git tag v*.*.* or Manual
Duration: ~12-15 minutes

       ┌──────────────┐
       │   Validate   │
       │  Tests+Lint  │
       └──────┬───────┘
              │
       ┌──────▼────────┐
       │Extract Version│
       │Calculate Code │
       └──────┬────────┘
              │
       ┌──────▼────────┐
       │  Build APK    │
       │  Build AAB    │
       └──────┬────────┘
              │
       ┌──────▼────────┐
       │ Sign Artifacts│
       │  Generate     │
       │  Checksums    │
       └──────┬────────┘
              │
       ┌──────▼────────────┐
       │ Create GH Release │
       │   + Changelog     │
       │   + APK/AAB       │
       │   + Checksums     │
       └──────┬────────────┘
              │
       ┌──────▼────────────┐
       │  Play Store       │
       │  Deploy           │
       │  (if configured)  │
       └───────────────────┘
```

## Build Variants Matrix

```
┌────────────────────────────────────────────────────────────────┐
│                    BUILD VARIANTS MATRIX                        │
└────────────────────────────────────────────────────────────────┘

                        Product Flavors
                    ┌─────────┬─────────┐
                    │   dev   │  prod   │
         ┌──────────┼─────────┼─────────┤
         │  debug   │devDebug │prodDebug│
Build    │          │         │         │
Types    ├──────────┼─────────┼─────────┤
         │ staging  │devStage │prodStage│
         │          │         │         │
         ├──────────┼─────────┼─────────┤
         │ release  │devRel   │prodRel  │
         └──────────┴─────────┴─────────┘

Characteristics:

devDebug:
  - API: dev-api.farmdirectory.com
  - Package: .dev
  - Minify: No
  - Coverage: Yes
  - Use: Daily development

devStaging:
  - API: staging-api.farmdirectory.com
  - Package: .dev.staging
  - Minify: Yes
  - Coverage: No
  - Use: Pre-release testing

prodDebug:
  - API: api.farmdirectory.com
  - Package: base
  - Minify: No
  - Coverage: No
  - Use: Debug production issues

prodStaging:
  - API: staging-api.farmdirectory.com
  - Package: .staging
  - Minify: Yes
  - Coverage: No
  - Use: Pre-production QA

prodRelease:
  - API: api.farmdirectory.com
  - Package: base
  - Minify: Yes
  - Signing: Required
  - Use: Production release
```

## Test Coverage Flow

```
┌────────────────────────────────────────────────────────┐
│              CODE COVERAGE WORKFLOW                    │
└────────────────────────────────────────────────────────┘

   Code Changes
        │
        ▼
   ┌─────────┐
   │  Commit │
   └────┬────┘
        │
        ▼
   ┌─────────┐
   │Push/PR  │
   └────┬────┘
        │
        ▼
   ┌──────────────┐
   │ Run Tests    │
   │ with Jacoco  │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │Generate XML  │
   │& HTML Reports│
   └──────┬───────┘
          │
          ├──────────────┐
          │              │
          ▼              ▼
   ┌──────────┐   ┌──────────┐
   │ Upload to│   │Post to PR│
   │ Codecov  │   │ Comment  │
   └──────────┘   └──────────┘
          │              │
          └──────┬───────┘
                 │
                 ▼
          ┌──────────┐
          │Check Min │
          │Threshold │
          │40% / 60% │
          └─────┬────┘
                │
         ┌──────┴──────┐
         │             │
    Pass ▼        Fail ▼
   ┌──────┐     ┌──────┐
   │ ✅   │     │  ❌  │
   └──────┘     └──────┘
```

## Dependency Management

```
┌────────────────────────────────────────────────────────┐
│           DEPENDENCY UPDATE WORKFLOW                   │
└────────────────────────────────────────────────────────┘

    Weekly Schedule
    (Monday 9 AM UTC)
         │
         ▼
   ┌──────────────┐
   │  Dependabot  │
   │ Checks Deps  │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │  New Version │
   │   Available? │
   └──────┬───────┘
          │
    ┌─────┴─────┐
    │           │
   Yes          No
    │           │
    ▼           ▼
┌────────┐   [End]
│Create  │
│  PR    │
└───┬────┘
    │
    ▼
┌─────────────┐
│ PR Created  │
│ Auto-labeled│
└──────┬──────┘
       │
       ▼
┌──────────────────┐
│  Check Update    │
│  Type (semver)   │
└────────┬─────────┘
         │
    ┌────┴────┐
    │         │
  Patch    Minor/Major
    │         │
    ▼         ▼
┌────────┐ ┌──────────────┐
│Auto-   │ │ Require      │
│Merge   │ │ Review       │
└────────┘ └──────────────┘
```

## Version Management

```
┌────────────────────────────────────────────────────────┐
│             VERSION BUMP WORKFLOW                      │
└────────────────────────────────────────────────────────┘

  Manual Trigger
  (patch/minor/major)
         │
         ▼
   ┌──────────────┐
   │Read Current  │
   │Version from  │
   │build.gradle  │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │Calculate New │
   │Version (sem) │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │Update Files: │
   │build.gradle  │
   │CHANGELOG.md  │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │Commit Changes│
   │Push to Main  │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │Create Tag?   │
   └──────┬───────┘
          │
     ┌────┴────┐
     │         │
    Yes        No
     │         │
     ▼         ▼
┌─────────┐  [End]
│Create & │
│Push Tag │
└────┬────┘
     │
     ▼
┌─────────┐
│Trigger  │
│Release  │
│Workflow │
└─────────┘
```

## Security Scanning

```
┌────────────────────────────────────────────────────────┐
│              SECURITY SCAN WORKFLOW                    │
└────────────────────────────────────────────────────────┘

    PR or Push
        │
        ▼
   ┌──────────────┐
   │ Analyze      │
   │ Dependencies │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │  OWASP       │
   │ Dependency   │
   │  Check       │
   └──────┬───────┘
          │
          ▼
   ┌──────────────────┐
   │ Scan for Known   │
   │ Vulnerabilities  │
   └────────┬─────────┘
            │
            ▼
   ┌─────────────────┐
   │Generate Report  │
   │  (HTML/JSON)    │
   └────────┬────────┘
            │
            ▼
   ┌─────────────────┐
   │Upload Artifact  │
   └────────┬────────┘
            │
      ┌─────┴─────┐
      │           │
   Critical    Low/Med
      │           │
      ▼           ▼
   ┌─────┐    ┌──────┐
   │Fail │    │Warn  │
   │ ❌  │    │  ⚠️  │
   └─────┘    └──────┘
```

## Pipeline Metrics

```
┌────────────────────────────────────────────────────────┐
│                  PERFORMANCE METRICS                   │
└────────────────────────────────────────────────────────┘

Workflow          Avg Time    Success Rate    Frequency
─────────────────────────────────────────────────────────
PR Validation     10-15 min      ~95%         Per PR
Enhanced CI        8-12 min      ~98%         Per push
Release           12-15 min      ~99%         Per tag
Version Bump       2-3 min       100%         Manual
Dependencies       5-8 min       ~95%         Weekly

Resource Usage:
- Parallel jobs: Up to 4 concurrent
- Caching: Gradle dependencies
- Artifacts retention: 7-90 days
- Concurrency control: Latest run only
```

This architecture provides a robust, scalable CI/CD pipeline for the Farm Directory Android application.
