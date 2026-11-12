# GitHub Actions Deep Dive - CI/CD Architecture

**For:** Understanding CI/CD pipeline
**Time:** 30-45 minutes
**Last Updated:** 2026-02-13

---

## 📖 Overview

This guide provides a comprehensive deep dive into the GitHub Actions CI/CD pipeline architecture.

**What you'll learn:**
- Reusable workflow architecture
- Custom composite actions design
- Job dependency management
- Matrix builds for Desktop
- Workflow optimization techniques
- Debugging workflow failures

---

## 📋 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Reusable Workflows](#reusable-workflows)
3. [Custom Composite Actions](#custom-composite-actions)
4. [Workflow Triggers](#workflow-triggers)
5. [Job Dependencies](#job-dependencies)
6. [Matrix Builds](#matrix-builds)
7. [Secrets Management](#secrets-management)
8. [Workflow Optimization](#workflow-optimization)
9. [Debugging Workflows](#debugging-workflows)
10. [Best Practices](#best-practices)

---

## Architecture Overview

### High-Level Design

```
┌─────────────────────────────────────────────────┐
│     This Repository (.github/workflows/)        │
│  ┌──────────────────────────────────────────┐  │
│  │   multi-platform-build-and-publish.yml   │  │
│  │   pr-check.yml                          │  │
│  │   promote-to-production.yml             │  │
│  └────────────┬─────────────────────────────┘  │
│               │ calls                           │
└───────────────┼─────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────┐
│   Reusable Workflows (mifos-x-actionhub)        │
│   openMF/mifos-x-actionhub@v1.0.8              │
│  ┌──────────────────────────────────────────┐  │
│  │  multi-platform-build-and-publish.yaml   │  │
│  │  pr-check.yaml                          │  │
│  │  promote-to-production.yaml             │  │
│  └────────────┬─────────────────────────────┘  │
│               │ uses                            │
└───────────────┼─────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────┐
│     Custom Composite Actions (13 total)         │
│   openMF/mifos-x-actionhub/.github/actions/    │
│  ┌──────────────────────────────────────────┐  │
│  │  android-firebase-publish               │  │
│  │  publish-android-on-playstore-beta      │  │
│  │  publish-ios-on-firebase                │  │
│  │  publish-ios-on-appstore-testflight     │  │
│  │  ... (9 more actions)                   │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

### Key Concepts

**1. Separation of Concerns**
- Repository workflows: Define **what** to run
- Reusable workflows: Define **when** and **how**
- Custom actions: Define **implementation**

**2. Centralization**
- CI/CD logic centralized in `mifos-x-actionhub`
- Versioned (`@v1.0.8`) for stability
- Shared across multiple projects

**3. Composability**
- Workflows call reusable workflows
- Reusable workflows use custom actions
- Actions use third-party actions

---

## Reusable Workflows

### What Are Reusable Workflows?

**Reusable workflows** are templates that can be called from other workflows.

**Benefits:**
- DRY (Don't Repeat Yourself)
- Centralized updates
- Version control
- Shared across repositories

---

### multi-platform-build-and-publish.yaml

**Location:** `openMF/mifos-x-actionhub/.github/workflows/multi-platform-build-and-publish.yaml`

**Purpose:** Deploy to all platforms in parallel

**Workflow Structure:**

```yaml
name: Multi-platform build and publish

on:
  workflow_call:
    inputs:
      release_type:
        required: true
        type: string
      android_package_name:
        required: true
        type: string
      ios_package_name:
        required: true
        type: string
      # ... more inputs

    secrets:
        required: true
      GOOGLESERVICES:
        required: true
      # ... more secrets

jobs:
  publish_android_on_firebase:
    # ...

  publish_android_on_playstore:
    # ...

  publish_ios_app_to_firebase:
    # ...

  # ... 10 total jobs
```

**Jobs:**
1. `publish_android_on_firebase` - Android APK → Firebase
2. `publish_android_on_playstore` - Android AAB → Play Store
3. `publish_ios_app_to_firebase` - iOS IPA → Firebase
4. `publish_ios_app_to_testflight` - iOS IPA → TestFlight
5. `publish_ios_app_to_appstore` - iOS IPA → App Store
6. `publish_macos_app_to_testflight` - macOS → TestFlight
7. `publish_macos_app_to_appstore` - macOS → App Store
8. `publish_desktop_external` - Desktop installers (matrix)
9. `publish_web` - Web app → GitHub Pages
10. `github_release` - Create GitHub release with artifacts

**Parallelization:** All jobs run in parallel except `github_release` (depends on all others)

---

### pr-check.yaml

**Location:** `openMF/mifos-x-actionhub/.github/workflows/pr-check.yaml`

**Purpose:** Validate pull requests

**Jobs:**
1. `checks` - Static analysis (Spotless, Detekt, Dependency Guard)
2. `build_android_app` - Android debug build
3. `build_desktop_app` - Desktop debug build (matrix)
4. `build_web_app` - Web debug build
5. `build_ios_app` - iOS debug build (optional)

**All run in parallel** - no dependencies

---

### promote-to-production.yaml

**Location:** `openMF/mifos-x-actionhub/.github/workflows/promote-to-production.yaml`

**Purpose:** Promote Play Store beta → production

**Jobs:**
1. `play_promote_production` - Fastlane promotion

**Single job** - fast deployment

---

## Custom Composite Actions

### What Are Composite Actions?

**Composite actions** bundle multiple steps into reusable units.

**Structure:**

```yaml
name: 'Action Name'
description: 'What it does'

inputs:
  param1:
    description: 'Parameter description'
    required: true
  param2:
    description: 'Optional parameter'
    required: false
    default: 'default-value'

runs:
  using: 'composite'
  steps:
    - name: Step 1
      run: command1
      shell: bash

    - name: Step 2
      run: command2
      shell: bash
```

---

### Action: android-firebase-publish

**Location:** `openMF/mifos-x-actionhub/.github/actions/android-firebase-publish/action.yaml`

**Purpose:** Build and upload Android APK to Firebase

**Inputs:**
- `android_package_name` - Package name (e.g., `cmp-android`)
- `release_type` - `internal` or `beta`
- `java_version` - Java version (default: `17`)

**Secrets:**
- `keystore_file` - Base64 keystore
- `keystore_password` - Keystore password
- `keystore_alias` - Key alias
- `keystore_alias_password` - Key password
- `google_services` - google-services.json (base64)
- `firebase_creds` - Firebase credentials (base64)

**Steps:**

```yaml
steps:
  # 1. Setup Java
  - uses: actions/setup-java@v4
    with:
      java-version: ${{ inputs.java_version }}

  # 2. Setup Gradle
  - uses: gradle/actions/setup-gradle@v4

  # 3. Install Fastlane
  - run: bundle install

  # 4. Inflate secrets (decode base64)
  - run: |
      echo "${{ secrets.keystore_file }}" | base64 -d > keystores/original-release-key.jks
      echo "${{ secrets.google_services }}" | base64 -d > ${{ inputs.android_package_name }}/google-services.json
      echo "${{ secrets.firebase_creds }}" | base64 -d > secrets/android/firebaseAppDistributionServiceCredentialsFile.json

  # 5. Run Fastlane lane
  - run: |
      if [ "${{ inputs.release_type }}" == "internal" ]; then
        bundle exec fastlane android deployDemoApkOnFirebase
      else
        bundle exec fastlane android deployReleaseApkOnFirebase
      fi

  # 6. Clean up secrets
  - run: |
      rm -f keystores/original-release-key.jks
      rm -f ${{ inputs.android_package_name }}/google-services.json
      rm -f secrets/android/firebaseAppDistributionServiceCredentialsFile.json
    if: always()
```

**Key insight:** Secrets are inflated, used, then cleaned up

---

### Action: publish-ios-on-firebase

**Location:** `openMF/mifos-x-actionhub/.github/actions/publish-ios-on-firebase/action.yaml`

**Purpose:** Build and upload iOS IPA to Firebase

**Inputs:**
- `ios_package_name` - iOS module name (`cmp-ios`)
- `shared_module` - Shared module name (`cmp-shared`; the `ComposeApp` XCFramework producer)
- `xcode_version` - Xcode version

> Since the E6 SwiftPM/XCFramework migration the
> iOS job assembles the Kotlin `ComposeApp` XCFramework via Gradle and archives the plain
> `.xcodeproj` (no dependency-install step).

**Secrets:**
- `appstore_key_id` - App Store Connect API key ID
- `appstore_issuer_id` - Issuer ID
- `appstore_auth_key` - Auth key (.p8 file, base64)
- `match_password` - Match password
- `match_ssh_private_key` - SSH key for Match repo (base64)
- `firebase_creds` - Firebase credentials

**Steps:**

```yaml
steps:
  # 1. Setup Xcode
  - uses: maxim-lobanov/setup-xcode@v1
    with:
      xcode-version: ${{ inputs.xcode_version }}

  # 2. Setup SSH for Match
  - run: |
      mkdir -p ~/.ssh
      echo "${{ secrets.match_ssh_private_key }}" | base64 -d > ~/.ssh/match_ci_key
      chmod 600 ~/.ssh/match_ci_key
      ssh-keyscan github.com >> ~/.ssh/known_hosts

  # 3. Setup App Store Connect API
  - run: |
      mkdir -p secrets
      echo "${{ secrets.appstore_auth_key }}" | base64 -d > secrets/apple/appstore/AuthKey.p8

  # 4. Install Ruby gems (Fastlane)
  - run: bundle install

  # 5. Run Fastlane lane — the lane calls `assemble_ios_xcframework`, which runs
  #    `./gradlew :cmp-shared:assembleComposeApp{Debug,Release}XCFramework` and then
  #    `build_app` archives cmp-ios/iosApp.xcodeproj (no dependency-install step).
  - run: bundle exec fastlane ios deploy_on_firebase

  # 6. Clean up secrets
  - run: |
      rm -rf ~/.ssh/match_ci_key
      rm -rf secrets/apple/appstore/AuthKey.p8
      rm -f ${{ inputs.ios_package_name }}/GoogleService-Info.plist
    if: always()
```

---

### Action: publish-desktop-app-kmp

**Location:** `openMF/mifos-x-actionhub/.github/actions/publish-desktop-app-kmp/action.yaml`

**Purpose:** Build desktop installers for current OS

**Inputs:**
- `desktop_package_name` - Desktop module name
- `java_version` - Java version

**Secrets (platform-specific):**
- Windows: `windows_signing_key`, `windows_signing_password`
- macOS: `macos_signing_key`, `macos_signing_password`
- Linux: `linux_signing_key` (optional)

**Steps:**

```yaml
steps:
  # 1. Setup Java
  - uses: actions/setup-java@v4

  # 2. Setup Gradle
  - uses: gradle/actions/setup-gradle@v4

  # 3. Build for current OS
  - run: ./gradlew :${{ inputs.desktop_package_name }}:packageReleaseDistributionForCurrentOS

  # 4. Upload artifacts
  - uses: actions/upload-artifact@v4
    with:
      name: desktop-${{ runner.os }}
      path: |
        ${{ inputs.desktop_package_name }}/build/compose/binaries/main/**/*.exe
        ${{ inputs.desktop_package_name }}/build/compose/binaries/main/**/*.msi
        ${{ inputs.desktop_package_name }}/build/compose/binaries/main/**/*.dmg
        ${{ inputs.desktop_package_name }}/build/compose/binaries/main/**/*.deb
```

---

## Workflow Triggers

### Trigger Types

**1. workflow_dispatch (Manual)**

```yaml
on:
  workflow_dispatch:
    inputs:
      release_type:
        description: 'Release type'
        required: true
        type: choice
        options:
          - internal
          - beta
```

**Usage:**
```bash
gh workflow run multi-platform-build-and-publish.yml \
  -f release_type=internal
```

**2. pull_request**

```yaml
on:
  pull_request:
    types: [opened, synchronize, reopened]
```

**Triggers on:**
- PR opened
- New commits pushed
- PR reopened

**3. push**

```yaml
on:
  push:
    branches:
      - main
      - dev
```

**4. release**

```yaml
on:
  release:
    types: [published]
```

**5. workflow_call (Reusable)**

```yaml
on:
  workflow_call:
    inputs:
      # ...
    secrets:
      # ...
```

---

## Job Dependencies

### Parallel Execution (Default)

**All jobs run in parallel** unless dependencies specified:

```yaml
jobs:
  job1:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Job 1"

  job2:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Job 2"

  job3:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Job 3"
```

**Execution:**
```
job1  ─────→
job2  ─────→  (all run simultaneously)
job3  ─────→
```

---

### Sequential Execution (needs)

**Use `needs` to create dependencies:**

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Building"

  test:
    needs: build  # Waits for build to complete
    runs-on: ubuntu-latest
    steps:
      - run: echo "Testing"

  deploy:
    needs: [build, test]  # Waits for both
    runs-on: ubuntu-latest
    steps:
      - run: echo "Deploying"
```

**Execution:**
```
build  ─────→
              test  ─────→
                          deploy  ─────→
```

---

### Complex Dependencies

**Example:** GitHub Release Job

```yaml
jobs:
  publish_android_on_firebase:
    # ...

  publish_android_on_playstore:
    # ...

  publish_ios_app_to_firebase:
    # ...

  # ... 7 more jobs

  github_release:
    needs: [
      publish_android_on_firebase,
      publish_android_on_playstore,
      publish_ios_app_to_firebase,
      publish_ios_app_to_testflight,
      publish_ios_app_to_appstore,
      publish_macos_app_to_testflight,
      publish_macos_app_to_appstore,
      publish_desktop_external,
      publish_web
    ]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/download-artifact@v4  # Download all artifacts
      - uses: softprops/action-gh-release@v1  # Create release
```

**Execution:**
```
[9 parallel jobs]  ─────→  github_release
```

---

## Matrix Builds

### What Are Matrix Builds?

**Matrix builds** run the same job on multiple configurations in parallel.

**Use case:** Build desktop app for Windows, macOS, Linux simultaneously

---

### Desktop Matrix Example

```yaml
jobs:
  publish_desktop_external:
    strategy:
      matrix:
        os: [ubuntu-latest, windows-latest, macos-latest]

    runs-on: ${{ matrix.os }}

    steps:
      - uses: actions/checkout@v4

      - name: Build for ${{ matrix.os }}
        run: ./gradlew :cmp-desktop:packageReleaseDistributionForCurrentOS

      - name: Upload artifacts
        uses: actions/upload-artifact@v4
        with:
          name: desktop-${{ matrix.os }}
          path: cmp-desktop/build/compose/binaries/main/
```

**Execution:**

```
ubuntu-latest   ─────→  (builds .deb)
windows-latest  ─────→  (builds .exe, .msi)
macos-latest    ─────→  (builds .dmg)
```

**3 jobs run in parallel**, each on different runner OS

---

### Matrix with Exclude

```yaml
strategy:
  matrix:
    os: [ubuntu-latest, windows-latest, macos-latest]
    java: [17, 21]
    exclude:
      - os: windows-latest
        java: 21  # Exclude Windows with Java 21
```

**Resulting combinations:**
- ubuntu-latest + Java 17
- ubuntu-latest + Java 21
- windows-latest + Java 17
- macos-latest + Java 17
- macos-latest + Java 21

---

### Matrix with Include

```yaml
strategy:
  matrix:
    os: [ubuntu-latest, windows-latest]
    include:
      - os: macos-latest
        java: 17  # Add specific configuration
```

---

## Secrets Management

### Secret Sources

**1. GitHub Repository Secrets**

```bash
# Add secret via CLI
gh secret set KEYSTORE_FILE < keystores/original-release-key.jks.b64

# Add secret via UI
# Repository → Settings → Secrets and variables → Actions → New secret
```

**2. Environment Secrets**

```yaml
jobs:
  deploy:
    environment: production  # Uses production environment secrets
    steps:
      - run: echo "${{ secrets.PROD_API_KEY }}"
```

**3. Workflow Secrets (Passed from Caller)**

```yaml
# Caller workflow
jobs:
  deploy:
    uses: openMF/mifos-x-actionhub/.github/workflows/deploy.yaml@v1.0.8
    secrets:
      KEYSTORE_FILE: ${{ secrets.KEYSTORE_FILE }}
```

---

### Secret Handling Best Practices

**1. Base64 Encoding**

```bash
# Encode binary file
base64 -i keystores/original-release-key.jks > keystores/original-release-key.jks.b64

# Set as secret
gh secret set KEYSTORE_FILE < keystores/original-release-key.jks.b64
```

**2. Decoding in Workflow**

```yaml
- name: Inflate keystore
  run: |
    echo "${{ secrets.KEYSTORE_FILE }}" | base64 -d > keystores/original-release-key.jks
```

**3. Clean Up Secrets**

```yaml
- name: Clean up secrets
  if: always()  # Run even if previous steps fail
  run: |
    rm -f keystores/original-release-key.jks
    rm -f secrets/*.json
```

**4. Never Log Secrets**

```yaml
# Bad: Secrets might be logged
- run: echo "Key: ${{ secrets.API_KEY }}"

# Good: Use secret masking
- run: |
    echo "::add-mask::${{ secrets.API_KEY }}"
    echo "Key is set"
```

---

## Workflow Optimization

### 1. Caching Dependencies

**Gradle Caching:**

```yaml
- uses: gradle/actions/setup-gradle@v4  # Automatic caching
```

**Manual Caching:**

```yaml
- uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
    restore-keys: |
      ${{ runner.os }}-gradle-
```

**Kotlin/Native + SwiftPM Caching (iOS):**

Since the E6 SwiftPM/XCFramework migration there is no vendored dependency directory or
lockfile to cache. The iOS job caches the Kotlin/Native (`konan`) toolchain + the resolved
SwiftPM packages instead — the actionhub `pr-check-v2` iOS job already does this:

```yaml
- uses: actions/cache@v4
  with:
    path: |
      ~/.konan
      ~/Library/Caches/org.swift.swiftpm
    key: ${{ runner.os }}-kmp-ios-${{ hashFiles('gradle/libs.versions.toml', 'cmp-ios/Package.swift') }}
```

---

### 2. Artifact Management

**Upload Artifacts:**

```yaml
- uses: actions/upload-artifact@v4
  with:
    name: android-apk
    path: cmp-android/build/outputs/apk/release/*.apk
    retention-days: 30  # Auto-delete after 30 days
```

**Download Artifacts:**

```yaml
- uses: actions/download-artifact@v4
  with:
    name: android-apk
    path: artifacts/
```

---

### 3. Conditional Execution

**Skip Jobs Based on Input:**

```yaml
jobs:
  deploy_ios:
    if: ${{ inputs.distribute_ios_firebase == 'true' }}
    steps:
      # ...
```

**Skip Steps Based on OS:**

```yaml
- name: macOS only step
  if: runner.os == 'macOS'
  run: echo "Running on macOS"
```

---

### 4. Concurrency Control

**Prevent multiple deployments:**

```yaml
concurrency:
  group: deploy-production
  cancel-in-progress: false  # Don't cancel running deployment
```

**Cancel previous PR builds:**

```yaml
concurrency:
  group: pr-${{ github.ref }}
  cancel-in-progress: true  # Cancel old PR builds
```

---

## Debugging Workflows

### 1. Enable Debug Logging

```bash
# Set repository secret
gh secret set ACTIONS_STEP_DEBUG --body "true"

# Re-run workflow
gh run rerun <run-id>
```

**Output:** Detailed logs for each step

---

### 2. SSH into Runner

**Use:** `tmate` action for interactive debugging

```yaml
- name: Setup tmate session
  if: failure()  # Only on failure
  uses: mxschmitt/action-tmate@v3
  timeout-minutes: 30
```

**Access:** SSH into runner to debug interactively

---

### 3. Check Job Logs

```bash
# List recent runs
gh run list

# View specific run
gh run view <run-id>

# Download logs
gh run download <run-id>
```

---

### 4. Common Issues

**Issue 1: Secret not found**

```
Error: Secret KEYSTORE_FILE not found
```

**Fix:**
```bash
# Verify secret exists
gh secret list

# Add if missing
gh secret set KEYSTORE_FILE < file.b64
```

---

**Issue 2: Permission denied**

```
Permission denied (publickey)
```

**Fix:**
```bash
# Add SSH key as secret
gh secret set MATCH_SSH_PRIVATE_KEY < secrets/apple/match/match_ci_key.b64
```

---

**Issue 3: Timeout**

```
The job running on runner ... has exceeded the maximum execution time of 360 minutes.
```

**Fix:**
- Add caching (Gradle, Kotlin/Native `konan`, SwiftPM)
- Split into smaller jobs
- Use faster runners (if available)

---

## Best Practices

### 1. Use Reusable Workflows

**Benefits:**
- Centralized CI/CD logic
- Version control
- Easier updates

**When to create reusable workflow:**
- Logic used in multiple repositories
- Complex workflow (>100 lines)
- Frequent updates needed

---

### 2. Version Reusable Workflows

```yaml
# Good: Pinned version
uses: openMF/mifos-x-actionhub/.github/workflows/deploy.yaml@v1.0.8

# Bad: Unpinned (uses latest)
uses: openMF/mifos-x-actionhub/.github/workflows/deploy.yaml@main
```

---

### 3. Minimize Secrets

**Principle:** Only store secrets that can't be generated

**Good:**
- API keys (can't regenerate)
- Passwords (user-created)
- Certificates (externally managed)

**Avoid:**
- Derived values (calculate instead)
- Public data (fetch instead)

---

### 4. Clean Up Secrets

**Always clean up inflated secrets:**

```yaml
- name: Clean up
  if: always()
  run: rm -f secrets/*.json keystores/*.jks
```

---

### 5. Use Matrix for Parallelization

**When to use matrix:**
- Multiple OS/platforms
- Multiple versions (Java, Xcode)
- Same logic, different configs

**When NOT to use:**
- Different logic per config
- Sequential dependencies

---

### 6. Fail Fast

```yaml
strategy:
  fail-fast: true  # Stop all matrix jobs if one fails
  matrix:
    os: [ubuntu-latest, windows-latest, macos-latest]
```

**Use when:**
- All variants must pass
- Want to save CI minutes

**Don't use when:**
- Want all results (even if some fail)

---

## Additional Resources

- **GitHub Actions Docs:** https://docs.github.com/en/actions
- **Reusable Workflows:** https://docs.github.com/en/actions/using-workflows/reusing-workflows
- **Composite Actions:** https://docs.github.com/en/actions/creating-actions/creating-a-composite-action
- **Workflow Syntax:** https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions

---

**Last Updated:** 2026-02-13
**Maintainer:** See CLAUDE.md
