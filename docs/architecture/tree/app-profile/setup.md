# Setup Guide

This guide provides detailed instructions for setting up the KMP Multi-Module Project Generator on your development environment, configuring it for your specific needs, and getting started with development across all supported platforms.

## Prerequisites

Before you begin, ensure your development environment meets the following requirements:

### Essential Tools
- **Bash 4.0+**: Required for running the project scripts
- **Git**: For version control and accessing the repository
- **JDK 17**: Required for Kotlin and Gradle

### Platform-Specific Requirements

#### Android Development
- **Android Studio Electric Eel (2022.1.1) or newer**: Recommended IDE for Android development
- **Android SDK 33+**: Latest Android platform APIs
- **Android Build Tools 33.0.0+**: Required for building Android applications

#### iOS Development
- **macOS 13.0+**: Required for iOS development
- **Xcode 14.1+**: Apple's IDE for iOS development (resolves SwiftPM packages)
- **Ruby 2.7+**: Required for Fastlane
- iOS links the Kotlin `ComposeApp` framework as an XCFramework via SwiftPM (`cmp-ios/Package.swift`)

#### Desktop Development
- **IntelliJ IDEA 2022.3+**: Recommended IDE for desktop development (Community Edition is sufficient)

#### Web Development
- **Node.js 18.0+**: Required for Kotlin/JS development
- **npm 8.0+**: Node.js package manager

### Optional Tools
- **Fastlane 2.212.0+**: For automated deployment workflows (required for CI/CD)
- **GitHub CLI**: For easier interaction with GitHub repositories
- **Docker**: For containerized development and testing

## Installation

Follow these steps to set up your development environment:

### 1. Clone the Repository

```bash
git clone https://github.com/openMF/kmp-project-template.git
cd kmp-project-template
```

### 2. Customize the Project (Optional)

The project includes a customization script that will update package names, application IDs, and other project identifiers to match your organization's naming conventions.

```bash
scripts/white-label/fork-init.sh org.example.myapp MyKMPProject
```

Parameters:
- First parameter: Package name (e.g., `org.example.myapp`)
- Second parameter: Project name (e.g., `MyKMPProject`)

> **Note**: This branch is designed for partial customization. It doesn't rename application modules but changes all `core` and `feature` module namespaces and packages. For full customization, use the `full-customizable` branch.

### 3. Install Dependencies

#### Android/Desktop

The project uses Gradle's dependency management, so no additional steps are required.

```bash
# Validate Gradle setup
./gradlew tasks
```

#### iOS

iOS integrates the Kotlin `ComposeApp` framework as an XCFramework (SwiftPM binary
target, `cmp-ios/Package.swift`) — there is no dependency-install step. Assemble the framework,
then open the project in Xcode; its `[KMP] Embed and Sign ComposeApp XCFramework`
Run-Script phase assembles + embeds it on every build:

```bash
./gradlew :cmp-shared:assembleComposeAppXCFramework
open cmp-ios/iosApp.xcodeproj
```

#### Web

For web development, install Node.js dependencies:

```bash
cd cmp-web
npm install
cd ..
```

### 4. Configure Secrets Manager (Optional)

If you plan to use CI/CD or need to manage keystores for Android app signing, configure the Secrets Manager:

```bash
scripts/white-label/keystore.sh generate
```

This will:
- Generate debug and release keystores for Android
- Update Gradle and Fastlane configurations
- Write keystore DN to `gradle/fork.properties` (non-secret)
- Write keystore passwords to `secrets/android/keystores/` per-value files (gitignored)
- Update Fastlane and Gradle configurations

## IDE Setup

### Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to and select the cloned project directory
4. Wait for the Gradle sync to complete
5. Configure an Android run configuration to test your setup

### Xcode (for iOS)

1. Open the `cmp-ios/iosApp.xcodeproj` file in Xcode (a plain project — there is no
   generated `.xcworkspace` since the SwiftPM/XCFramework migration)
2. Select your development team in the project settings
3. Configure an iOS simulator or device for testing

### IntelliJ IDEA (for Desktop)

1. Open IntelliJ IDEA
2. Select "Open" and choose the cloned project directory
3. Wait for the Gradle sync to complete
4. Create a run configuration for the `cmp-desktop` module

## Building and Running

### Building All Platforms

```bash
./gradlew build
```

### Running on Specific Platforms

#### Android

```bash
./gradlew cmp-android:installDebug
```

Or use Android Studio's run button with a configured AVD (Android Virtual Device).

#### iOS

Open `cmp-ios/iosApp.xcodeproj` in Xcode, pick a flavor scheme (`demoDebug`, `prodRelease`, …),
and run on a simulator or device.

Alternatively, use the command line:

```bash
xcodebuild -project cmp-ios/iosApp.xcodeproj -scheme demoDebug -configuration demoDebug \
  -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 16,OS=latest'
```

#### Desktop

```bash
./gradlew cmp-desktop:run
```

#### Web

```bash
./gradlew cmp-web:jsBrowserDevelopmentRun
```

This will start a development server and automatically open your default browser.

## Common Issues and Troubleshooting

### Gradle Build Issues

If you encounter Gradle build issues:

```bash
# Clean the build
./gradlew clean

# Refresh Gradle dependencies
./gradlew --refresh-dependencies build
```

### Android SDK Issues

Ensure your `local.properties` file contains the correct path to your Android SDK:

```properties
sdk.dir=/path/to/your/Android/sdk
```

### iOS Build Issues

1. Make sure the Kotlin `ComposeApp` XCFramework is assembled and up-to-date (SwiftPM/XCFramework):

```bash
./gradlew :cmp-shared:assembleComposeAppXCFramework
rm -rf ~/Library/Developer/Xcode/DerivedData/*
```

2. Check that Xcode command line tools are installed:

```bash
xcode-select --install
```

### Sync Issues

If you need to sync your fork with the upstream repository:

```bash
# Basic sync with the provided script
scripts/white-label/sync-dirs.sh

# Sync with preview (dry run)
scripts/white-label/sync-dirs.sh --dry-run
```

## Next Steps

Once your environment is set up, you can:

1. Explore the [Architecture Overview](../../ARCHITECTURE.md) to understand the project structure
2. Review the [Source Set Hierarchy](../../cross-cutting/source-set-hierarchy.md) to learn about code sharing
3. Check the [Code Style Guide](../../cross-cutting/style-guide.md) for coding conventions
4. Start developing your own features following the established patterns

## Additional Configuration

### Custom Module Creation

To create a new feature module:

1. Create a new directory in the `feature` folder
2. Add the module to `settings.gradle.kts`
3. Configure build scripts following the existing modules
4. Add the module to the `includedBuild` in the root project

### Dependency Management

Manage dependencies using the version catalog in `gradle/libs.versions.toml`:

```toml
[versions]
# Add your library versions here

[libraries]
# Reference versions and define libraries

[plugins]
# Define Gradle plugins
```

### CI/CD Configuration

The project includes GitHub Actions workflows for CI/CD. Customize them in the `.github/workflows` directory to match your project's needs.

## Support and Resources

If you encounter any issues during setup, you can:

- Join the [Slack channel](https://join.slack.com/t/mifos/shared_invite/zt-2wvi9t82t-DuSBdqdQVOY9fsqsLjkKPA) for community support
- Report issues on [GitHub](https://github.com/openMF/kmp-project-template/issues)
- Check the progress and plans on [Jira](https://mifosforge.jira.com/jira/software/c/projects/KMPPT/boards/63)