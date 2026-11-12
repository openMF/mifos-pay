# Onboarding Guide - Welcome to KMP Project Template

**For:** New Team Members
**Time:** 30-60 minutes
**Last Updated:** 2026-02-13

---

## 👋 Welcome!

Welcome to the KMP (Kotlin Multiplatform) Project Template! This guide will walk you through everything you need to know to start contributing.

**What you'll learn:**
- Project architecture and structure
- Development environment setup
- Building the app for all platforms
- Understanding the codebase
- Making your first deployment
- Common workflows and conventions

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Prerequisites](#prerequisites)
3. [Environment Setup](#environment-setup)
4. [First Build](#first-build)
5. [Project Structure](#project-structure)
6. [Development Workflow](#development-workflow)
7. [Making Changes](#making-changes)
8. [Testing](#testing)
9. [Deployment](#deployment)
10. [Common Tasks](#common-tasks)
11. [Getting Help](#getting-help)

---

## Project Overview

### What is This Project?

This is a **Kotlin Multiplatform (KMP)** template that targets **5 platforms** from a single codebase:

| Platform | Technology | Deployment Target |
|----------|-----------|-------------------|
| **Android** | Kotlin + Compose | Play Store + Firebase |
| **iOS** | Swift + Kotlin/Native | App Store + TestFlight + Firebase |
| **macOS** | SwiftUI + Kotlin/Native | App Store + TestFlight |
| **Desktop** | Compose Multiplatform | GitHub Releases (Windows/macOS/Linux) |
| **Web** | Kotlin/JS + Compose | GitHub Pages |

### Architecture

```
┌─────────────────────────────────────────┐
│         Shared Business Logic           │
│         (cmp-shared module)             │
│    - Data models                        │
│    - Business logic                     │
│    - API clients                        │
│    - Database                           │
└─────────────────────────────────────────┘
                    │
        ┌───────────┼───────────┐
        │           │           │
        ▼           ▼           ▼
┌─────────┐  ┌─────────┐  ┌─────────┐
│ Android │  │   iOS   │  │  macOS  │
│  (JVM)  │  │(Native) │  │(Native) │
└─────────┘  └─────────┘  └─────────┘
        │           │
        ▼           ▼
┌─────────┐  ┌─────────┐
│ Desktop │  │   Web   │
│(Compose)│  │  (JS)   │
└─────────┘  └─────────┘
```

### Key Technologies

- **Kotlin Multiplatform** - Code sharing across platforms
- **Compose Multiplatform** - UI framework for Android, Desktop, Web
- **Gradle** - Build system
- **Fastlane** - Deployment automation (iOS/Android)
- **GitHub Actions** - CI/CD pipelines
- **Firebase** - App Distribution for beta testing
- **Spotless + Detekt** - Code quality tools

---

## Prerequisites

### Required Tools

Before you start, install these tools:

#### macOS:

```bash
# 1. Homebrew (package manager)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 2. Java JDK 17
brew install openjdk@17

# 3. Git
brew install git

# 4. Xcode (for iOS/macOS development)
# Download from App Store

# 5. Xcode Command Line Tools
xcode-select --install

# 6. Ruby (for Fastlane) - usually pre-installed
ruby --version  # Should be 2.7+

# 7. Bundler (for Fastlane gems)
sudo gem install bundler
# (iOS links the Kotlin ComposeApp framework via SwiftPM/XCFramework — nothing extra to install.)

# Optional: GitHub CLI (for workflow management)
brew install gh
```

#### Linux:

```bash
# Ubuntu/Debian
sudo apt-get update

# 1. Java JDK 17
sudo apt-get install openjdk-17-jdk

# 2. Git
sudo apt-get install git

# 3. Build essentials
sudo apt-get install build-essential

# 4. GitHub CLI (optional)
sudo apt-get install gh
```

#### Windows:

```powershell
# 1. Install Scoop (package manager)
iwr -useb get.scoop.sh | iex

# 2. Java JDK 17
scoop install openjdk17

# 3. Git
scoop install git

# 4. GitHub CLI (optional)
scoop install gh
```

### Verify Installation

```bash
# Run environment check script
./scripts/check_environment.sh
```

**Expected output:**
```
✅ Java 17 found
✅ Git found
✅ Xcode found (macOS only)
✅ Ruby found
✅ Bundler found
```

---

## Environment Setup

### Step 1: Clone the Repository

```bash
# Clone the repo
git clone https://github.com/your-org/kmp-project-template.git
cd kmp-project-template

# Checkout development branch
git checkout dev
```

### Step 2: Install Dependencies

```bash
# Install Ruby gems (Fastlane, etc.)
bundle install

# Install Git hooks (optional but recommended)
cp .claude/hooks/* .git/hooks/
chmod +x .git/hooks/*
```

### Step 3: Configure Git

```bash
# Set your Git identity
git config user.name "Your Name"
git config user.email "your.email@example.com"

# Enable commit signing (recommended)
git config commit.gpgsign true
```

### Step 4: IDE Setup

#### Android Studio / IntelliJ IDEA (Recommended)

```bash
# Open project
open -a "Android Studio" .
# or
idea .

# Wait for Gradle sync to complete (may take 5-10 minutes first time)
```

**Configure IDE:**
1. Enable Kotlin plugin
2. Install Compose Multiplatform plugin
3. Set Java SDK to 17
4. Enable code style settings from `.editorconfig`

#### VS Code (Alternative)

```bash
# Open project
code .

# Install extensions:
# - Kotlin
# - Gradle for Java
# - EditorConfig
```

---

## First Build

Let's build the project for all platforms to ensure everything works.

### Android Build

```bash
# Debug build (fast, for testing)
./gradlew :cmp-android:assembleDebug

# Check output
ls -la cmp-android/build/outputs/apk/debug/
# You should see: cmp-android-debug.apk
```

**First build takes 5-10 minutes** (downloads dependencies)

**If it fails:** See [Troubleshooting](troubleshooting.md#android-build-issues)

### iOS Build (macOS only)

```bash
# iOS integrates the Kotlin ComposeApp framework as an XCFramework (SwiftPM binary
# target, cmp-ios/Package.swift). Open the Xcode project; its
# [KMP] Embed and Sign ComposeApp XCFramework Run-Script phase assembles + embeds
# the framework on every build:
open cmp-ios/iosApp.xcodeproj

# Or build unsigned from the CLI (the same Run-Script phase assembles the framework;
# the app is a plain .xcodeproj, no .xcworkspace):
xcodebuild -project cmp-ios/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  build
```

**First build takes 10-15 minutes**

**If it fails:** See [Troubleshooting](troubleshooting.md#ios-build-issues)

### Desktop Build

```bash
# Build for your current OS
./gradlew :cmp-desktop:packageDebugDistributionForCurrentOS

# Check output
# macOS: cmp-desktop/build/compose/binaries/main/dmg/
# Windows: cmp-desktop/build/compose/binaries/main/msi/
# Linux: cmp-desktop/build/compose/binaries/main/deb/
```

### Web Build

```bash
# Development build
./gradlew :cmp-web:jsBrowserDevelopmentWebpack

# Check output
ls -la cmp-web/build/distributions/
```

### macOS Build (macOS only)

macOS ships through the Compose Desktop target, not a separate Xcode app — there is no
`cmp-macos` module and no `.xcworkspace`:

```bash
./gradlew :cmp-desktop:packageDmg
```

### Success!

If all builds complete, you're ready to start developing! 🎉

---

## Project Structure

### Repository Layout

```
kmp-project-template/
│
├── cmp-android/              # Android application
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── kotlin/           # Android-specific code
│   ├── google-services.json  # Firebase config
│   └── build.gradle.kts
│
├── cmp-ios/                  # iOS application
│   ├── iosApp/               # Swift code
│   ├── iosApp.xcodeproj
│   ├── Package.swift          # SwiftPM binary target (ComposeApp XCFramework)
│   └── GoogleService-Info.plist
│
├── cmp-macos/                # macOS application
│   ├── macosApp/
│   └── macosApp.xcodeproj
│
├── cmp-desktop/              # Desktop application (Compose)
│   ├── src/main/kotlin/
│   └── build.gradle.kts
│
├── cmp-web/                  # Web application (Kotlin/JS)
│   ├── src/main/kotlin/
│   └── build.gradle.kts
│
├── cmp-shared/               # ⭐ Shared business logic
│   ├── src/
│   │   ├── commonMain/       # Shared across all platforms
│   │   ├── androidMain/      # Android-specific
│   │   ├── iosMain/          # iOS-specific
│   │   └── ...
│   └── build.gradle.kts
│
├── fastlane/                 # Deployment automation
│   ├── Fastfile             # 12 deployment lanes
│   ├── Matchfile            # iOS code signing config
│   └── metadata/            # App Store metadata
│
├── fastlane-config/         # Fastlane configuration
│   ├── project_config.rb    # Project settings
│   ├── android_config.rb    # Android settings
│   └── ios_config.rb        # iOS settings
│
├── scripts/                 # Bash utility scripts
│   ├── setup_ios_complete.sh
│   ├── deploy_*.sh
│   └── verify_*.sh
│
├── .github/                 # GitHub Actions workflows
│   ├── workflows/
│   │   ├── multi-platform-build-and-publish.yml
│   │   ├── pr-check.yml
│   │   └── promote-to-production.yml
│   └── actions/             # Custom composite actions
│
├── docs/                    # Documentation
│   ├── claude/              # Deep-dive guides
│   └── analysis/            # Infrastructure analysis
│
├── .claude/                 # Claude Code configuration
│   ├── settings.json
│   ├── rules.json
│   ├── hooks/               # Session hooks
│   └── skills/              # Interactive guides
│
├── keystores/               # Android signing keys (gitignored)
├── secrets/                 # Deployment secrets (gitignored)
│
├── build.gradle.kts         # Root Gradle config
├── settings.gradle.kts      # Project modules
├── gradle.properties        # Gradle properties
├── libs.versions.toml       # Dependency versions
├── CLAUDE.md                # Main documentation hub
└── README.md                # Project README
```

### Key Modules

#### 1. `cmp-shared` - The Heart of the Project

**Purpose:** Contains all shared business logic

**Structure:**
```
cmp-shared/src/
├── commonMain/          # ⭐ Code shared by ALL platforms
│   ├── kotlin/
│   │   ├── data/        # Data models
│   │   ├── domain/      # Business logic
│   │   ├── network/     # API clients
│   │   └── util/        # Utilities
│   └── resources/
│
├── androidMain/         # Android-specific implementations
├── iosMain/             # iOS-specific implementations
├── desktopMain/         # Desktop-specific implementations
└── jsMain/              # Web-specific implementations
```

**Key concept:** Write once in `commonMain`, use everywhere!

#### 2. Platform Modules

Each platform module depends on `cmp-shared`:

```kotlin
// In cmp-android/build.gradle.kts
dependencies {
    implementation(project(":cmp-shared"))
}
```

---

## Development Workflow

### Daily Workflow

```bash
# 1. Start your day - pull latest changes
git checkout dev
git pull origin dev

# 2. Create a feature branch
git checkout -b feature/my-awesome-feature

# 3. Make changes
# Edit files in your IDE

# 4. Run checks locally
./gradlew spotlessApply  # Auto-format code
./gradlew detekt         # Static analysis
./gradlew test           # Run tests

# 5. Commit changes
git add .
git commit -m "feat: add awesome feature"

# 6. Push and create PR
git push origin feature/my-awesome-feature
gh pr create --title "feat: add awesome feature" --body "Description"
```

### Branch Strategy

```
main/master    ─────○────────○────────○──────→  Production releases
                     │        │        │
                     │        │        └─────── v2026.2.0
                     │        └──────────────── v2026.1.0
                     │
dev            ─○─○─○┴○─○─○─○─○┴○─○──────→  Active development
                │ │ │  │ │ │ │ │  │ │
                │ │ │  │ │ │ │ │  │ └─────── feature/auth
                │ │ │  │ │ │ │ │  └───────── feature/profile
                │ │ │  │ │ │ │ └────────────  bugfix/crash
                │ │ │  └─┴─┴─┴──────────────  hotfix merged
                └─┴─┴─────────────────────── feature branches merged
```

**Branch naming:**
- Features: `feature/short-description`
- Bug fixes: `bugfix/issue-description`
- Hotfixes: `hotfix/critical-fix`
- Releases: `release/v2026.1.0`

### Commit Message Format

Use **Conventional Commits**:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

**Examples:**

```bash
# Good commits
git commit -m "feat(auth): add biometric authentication"
git commit -m "fix(android): crash on startup in Android 12"
git commit -m "docs(readme): update installation instructions"

# Bad commits (avoid)
git commit -m "update stuff"
git commit -m "fix bug"
git commit -m "changes"
```

**Why?** Conventional commits enable:
- Automatic changelog generation
- Semantic versioning
- Better Git history

---

## Making Changes

### Example: Add a New Feature

Let's walk through adding a simple feature: a "Dark Mode" toggle.

#### Step 1: Create Feature Branch

```bash
git checkout dev
git pull
git checkout -b feature/dark-mode-toggle
```

#### Step 2: Add Shared Logic

Theme preference lives in the user-data layer, not a shared manager — add a brand in
`core/model/src/commonMain/kotlin/kpt/core/model/user/ThemeBrand.kt` and brand its colours in
`core/designsystem`:

```kotlin
// cmp-shared/src/commonMain/kotlin/domain/ThemeManager.kt
package com.yourapp.domain

class ThemeManager {
    private var isDarkMode = false

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }

    fun isDarkModeEnabled(): Boolean = isDarkMode
}
```

#### Step 3: Update Android UI

Edit `cmp-android/src/main/kotlin/cmp/android/app/MainActivity.kt`:

```kotlin
// cmp-android/src/main/kotlin/MainActivity.kt
import com.yourapp.domain.ThemeManager

class MainActivity : ComponentActivity() {
    private val themeManager = ThemeManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDark = remember { mutableStateOf(themeManager.isDarkModeEnabled()) }

            MyAppTheme(darkTheme = isDark.value) {
                Button(onClick = {
                    themeManager.toggleTheme()
                    isDark.value = themeManager.isDarkModeEnabled()
                }) {
                    Text("Toggle Theme")
                }
            }
        }
    }
}
```

#### Step 4: Update iOS UI

Edit `cmp-ios/iosApp/ContentView.swift`:

```swift
// cmp-ios/iosApp/ContentView.swift
import SwiftUI
import shared

struct ContentView: View {
    @State private var isDark = false
    let themeManager = ThemeManager()

    var body: some View {
        Button("Toggle Theme") {
            themeManager.toggleTheme()
            isDark = themeManager.isDarkModeEnabled()
        }
        .preferredColorScheme(isDark ? .dark : .light)
    }
}
```

#### Step 5: Test Locally

```bash
# Test Android
./gradlew :cmp-android:assembleDebug
./gradlew :cmp-android:connectedDebugAndroidTest

# Test iOS (if on macOS) — no dependency-install step; the Xcode Run-Script phase assembles the
# ComposeApp XCFramework, and the app builds from the .xcodeproj (no .xcworkspace)
xcodebuild test -project cmp-ios/iosApp.xcodeproj -scheme iosApp

# Test Desktop
./gradlew :cmp-desktop:run
```

#### Step 6: Code Quality Checks

```bash
# Format code
./gradlew spotlessApply

# Run static analysis
./gradlew detekt

# Run all tests
./gradlew test

# Check dependency guard
./gradlew dependencyGuard
```

#### Step 7: Commit and Push

```bash
git add .
git commit -m "feat(ui): add dark mode toggle

- Add ThemeManager in shared module
- Implement toggle in Android and iOS
- Update UI for theme switching

Closes #123"

git push origin feature/dark-mode-toggle
```

#### Step 8: Create Pull Request

```bash
gh pr create \
  --title "feat(ui): add dark mode toggle" \
  --body "## Summary
Adds a dark mode toggle button to the app.

## Changes
- New ThemeManager class in shared module
- Android implementation with Compose
- iOS implementation with SwiftUI

## Testing
- Tested on Android Pixel 5 emulator
- Tested on iOS Simulator (iPhone 14)

Closes #123"
```

---

## Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run Android tests only
./gradlew :cmp-android:test

# Run shared module tests
./gradlew :cmp-shared:test

# Run with coverage
./gradlew :cmp-shared:koverReport
open cmp-shared/build/reports/kover/html/index.html
```

### Writing Tests

**Example:** Test the ThemeManager

```kotlin
// cmp-shared/src/commonTest/kotlin/domain/ThemeManagerTest.kt
package com.yourapp.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemeManagerTest {
    @Test
    fun `initially dark mode is disabled`() {
        val manager = ThemeManager()
        assertFalse(manager.isDarkModeEnabled())
    }

    @Test
    fun `toggle enables dark mode`() {
        val manager = ThemeManager()
        manager.toggleTheme()
        assertTrue(manager.isDarkModeEnabled())
    }

    @Test
    fun `double toggle returns to initial state`() {
        val manager = ThemeManager()
        manager.toggleTheme()
        manager.toggleTheme()
        assertFalse(manager.isDarkModeEnabled())
    }
}
```

**Run this test:**
```bash
./gradlew :cmp-shared:test --tests ThemeManagerTest
```

---

## Deployment

### Local Deployment (Testing)

#### Android to Firebase

```bash
# Deploy demo variant to Firebase
bundle exec fastlane android deployDemoApkOnFirebase

# Deploy production variant to Firebase
bundle exec fastlane android deployReleaseApkOnFirebase
```

**First time:** Requires Firebase setup and secrets. See [Secrets Management](architecture/tree/secrets/management.md) or run `./setup-project.sh`.

#### iOS to Firebase (macOS only)

```bash
# Deploy to Firebase
bundle exec fastlane ios deploy_on_firebase
```

**First time:** Requires iOS setup. See [iOS Setup](architecture/tree/app-profile/project-setup.md#step-5-setup-ios-optional).

### CI/CD Deployment

**GitHub Actions automatically deploy when you:**

1. **Create a PR** → Runs PR checks (builds, tests, static analysis)
2. **Push to `dev`** → Can trigger manual deployment
3. **Create a release** → Deploys to production

**Manual deployment via GitHub Actions:**

```bash
gh workflow run multi-platform-build-and-publish.yml \
  --ref dev \
  -f release_type=internal \
  -f android_package_name=cmp-android \
  -f ios_package_name=cmp-ios \
  -f desktop_package_name=cmp-desktop \
  -f web_package_name=cmp-web \
  -f distribute_ios_firebase=true \
  -f shared_module=cmp-shared
```

**See:** [Deployment Playbook](architecture/tree/deployment/playbook.md) for detailed deployment procedures.

---

## Common Tasks

### Task: Add a New Dependency

#### To Shared Module (All Platforms)

Edit `gradle/libs.versions.toml`:

```toml
[versions]
ktor = "2.3.7"

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
```

Edit `cmp-shared/build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
        }
    }
}
```

#### To Android Only

Edit `cmp-android/build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.android.library:name:1.0.0")
}
```

#### To iOS Only

iOS integrates native Swift dependencies via SwiftPM. Add the package to
`cmp-ios/Package.swift` (or via Xcode → File → Add Package Dependencies…):

```swift
dependencies: [
    .package(url: "https://github.com/example/SomeLibrary.git", from: "1.0.0"),
],
```

Xcode resolves the package on the next build — there is no dependency-install step.

**After adding dependencies:**

```bash
# Update dependency guard baseline
./gradlew dependencyGuardBaseline

# Commit updated baseline
git add **/dependencies/*.txt
git commit -m "chore: update dependency guard baseline"
```

### Task: Update Version Number

**Automatic:** Version is generated from Git (year.month.commitCount)

```bash
# Check current version
./gradlew printVersionInfo

# Output:
# Version: 2026.1.25
# Version code: 20260125
```

**Manual override:** Edit `build.gradle.kts`:

```kotlin
version = "2026.2.0" // Override automatic versioning
```

### Task: Run Code Formatting

```bash
# Check formatting
./gradlew spotlessCheck

# Auto-format all code
./gradlew spotlessApply
```

**Tip:** Set up pre-commit hook to auto-format:

```bash
# Copy hook
cp .claude/hooks/pre-commit .git/hooks/
chmod +x .git/hooks/pre-commit
```

### Task: View Build Reports

```bash
# After running detekt
open build/reports/detekt/detekt.html

# After running tests
open cmp-shared/build/reports/tests/test/index.html

# After code coverage
./gradlew :cmp-shared:koverReport
open cmp-shared/build/reports/kover/html/index.html
```

---

## Getting Help

### Documentation

| Topic | Resource |
|-------|----------|
| **Quick Reference** | [CLAUDE.md](../CLAUDE.md) |
| **Troubleshooting** | [troubleshooting.md](troubleshooting.md) |
| **Deployment** | [deployment-playbook.md](architecture/tree/deployment/playbook.md) |
| **GitHub Actions** | [.github/CLAUDE.md](../.github/CLAUDE.md) |
| **Fastlane** | [fastlane/CLAUDE.md](architecture/tree/deployment/known-issues.md) |
| **Scripts** | [scripts/CLAUDE.md](../scripts/CLAUDE.md) |
| **Known Issues** | [BUGS_AND_ISSUES.md](architecture/tree/deployment/known-issues.md) |

### Interactive Help

Use Claude Code skills:

```bash
# Setup help
/setup-project-guide

# Build troubleshooting
/troubleshoot-build

# Deployment guide
/deploy-android-guide
/deploy-ios-guide

# Workflow explanation
/explain-workflow
```

### Common Questions

**Q: Where do I add shared code?**
**A:** `cmp-shared/src/commonMain/kotlin/`

**Q: How do I run on a real device?**
**A:**
- Android: Connect via USB, enable USB debugging, run `./gradlew installDebug`
- iOS: Open in Xcode, select device, click Run

**Q: Build is failing, what do I do?**
**A:**
1. Clean: `./gradlew clean`
2. Check errors in [troubleshooting.md](troubleshooting.md)
3. Ask for help with full error message

**Q: How do I deploy to production?**
**A:** See [deployment-playbook.md](architecture/tree/deployment/playbook.md) - requires additional setup

**Q: Can I use Claude Code to help?**
**A:** Yes! Claude Code has skills and hooks set up for this project. Just ask!

---

## Next Steps

**You're ready to contribute! 🚀**

**Recommended path:**

1. ✅ Complete this onboarding guide
2. Read [Patterns & Best Practices](architecture/patterns/README.md)
3. Pick a small issue from the backlog
4. Make your first PR
5. Deploy your first build
6. Bookmark [Troubleshooting](troubleshooting.md) for reference

**Advanced topics:**

- [Version Handling](architecture/tree/gradle/version-handling.md) - Understanding version formats
- [Secrets Management](architecture/tree/secrets/management.md) - Managing deployment secrets
- [GitHub Actions Deep Dive](architecture/tree/github/actions-deep-dive.md) - CI/CD internals

---

**Welcome to the team! If you have questions, ask in the team chat or create a GitHub discussion.**

**Last Updated:** 2026-02-13
**Maintainer:** See CLAUDE.md
