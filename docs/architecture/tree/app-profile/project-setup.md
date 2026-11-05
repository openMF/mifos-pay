# KMP Project Master Setup Script(Incubating)

## Table of Contents

- [Overview](#overview)
- [What This Script Does](#what-this-script-does)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Detailed Setup Process](#detailed-setup-process)
- [Configuration Options](#configuration-options)
- [Generated Files and Structure](#generated-files-and-structure)
- [Post-Setup Steps](#post-setup-steps)
- [Troubleshooting](#troubleshooting)
- [Advanced Usage](#advanced-usage)
- [Frequently Asked Questions](#frequently-asked-questions)

## Overview

The KMP Project Master Setup Script is an interactive automation tool designed to streamline the
initial configuration of Kotlin Multiplatform projects. It orchestrates a comprehensive setup
process that includes project customization, Firebase integration, and security configuration—all
through a single, guided interface.

This script eliminates the complexity of manually configuring multiple tools and services by
automating the coordination of several specialized setup scripts, while providing clear feedback and
validation at each step.

### Key Benefits

- **Single Entry Point**: Configure your entire project with one command
- **Interactive Guidance**: Step-by-step prompts with validation
- **Error Prevention**: Built-in checks for common configuration mistakes
- **Time Saving**: Automates tasks that typically take hours into minutes
- **Consistency**: Ensures proper configuration across all project components
- **Documentation**: Automatically generates reference documentation for your setup

## What This Script Does

The master setup script performs four major configuration phases:

### Phase 1: Project Customization

Transforms the template project into your custom application by:

- Updating the Android package namespace throughout the codebase
- Renaming the project and application display name
- Modifying all Kotlin source files with new package declarations
- Updating Gradle build configurations
- Configuring Fastlane deployment settings
- Adjusting version catalog settings

### Phase 2: Firebase Integration

Establishes Firebase backend services by:

- Creating or connecting to a Firebase project
- Registering Android application variants (Release and Demo)
- Registering iOS application
- Downloading platform-specific configuration files
- Generating merged configuration for all build variants
- Integrating Firebase App IDs into CI/CD configuration

### Phase 3: Security and Signing

Configures application signing infrastructure by:

- Generating production and upload Android keystores
- Creating secure password storage
- Encoding sensitive credentials
- Updating build configurations with keystore information
- Preparing secrets for CI/CD environments

### Phase 4: Documentation and Verification

Finalizes setup by:

- Generating comprehensive setup documentation
- Creating a configuration reference file
- Providing detailed next steps
- Offering command references for future operations

## Prerequisites

### Required Software

The following tools must be installed and accessible in your system PATH:

1. **Bash Shell** (version 4.0 or higher)
    - Pre-installed on macOS and Linux
    - Windows users should use Git Bash or WSL

2. **Java Development Kit (JDK)** (version 8 or higher)
    - Required for the `keytool` utility
    - Verify with: `keytool -help`

3. **Firebase CLI** (version 11.0.0 or higher)
   ```bash
   npm install -g firebase-tools
   ```
    - Required for Firebase project configuration
    - Must be authenticated: `firebase login`

4. **Python 3** (version 3.6 or higher)
   ```bash
   # Verify installation
   python3 --version
   ```
    - Required for JSON configuration merging

### Optional Tools

5. **GitHub CLI** (latest version)
   ```bash
   # macOS
   brew install gh
   
   # Linux
   # See: https://cli.github.com/manual/installation
   ```
    - Required only if using GitHub secrets management

### System Requirements

- **Operating System**: macOS 10.15+, Linux (Ubuntu 18.04+), or Windows 10+ with WSL
- **Disk Space**: Minimum 500MB for generated files and dependencies
- **Network**: Active internet connection for Firebase and package operations

### Project Requirements

- Kotlin Multiplatform project structure initialized
- Gradle wrapper present (`gradlew` or `gradlew.bat`)
- Standard Android and iOS module structure
- Version catalog file at `gradle/libs.versions.toml`

## Quick Start

For experienced developers who want to get started immediately:

```bash
# 1. Make the script executable
chmod +x setup-project.sh

# 2. Run the setup
./setup-project.sh

# 3. Follow the interactive prompts
# 4. Review the generated PROJECT_SETUP_INFO.txt
# 5. Complete the post-setup steps shown in the summary
```

The script will guide you through all necessary configuration steps interactively.

## Detailed Setup Process

### Preparation

Before running the script, gather the following information:

1. **Android Package Name**
    - Format: Reverse domain notation (e.g., `com.company.appname`)
    - Must be unique on Google Play Store
    - Lowercase letters, numbers, and underscores only
    - Example: `com.acmecorp.productivityapp`

2. **Project Name**
    - Internal codename for your project
    - Used in Gradle configurations and build scripts
    - PascalCase recommended (e.g., `ProductivityApp`)

3. **Application Display Name**
    - User-facing name shown in app stores and device
    - Can contain spaces and special characters
    - Example: "Acme Productivity Suite"

4. **iOS Bundle Identifier**
    - Similar to Android package name
    - Must be unique on App Store
    - Typically matches Android package (recommended)
    - Example: `com.acmecorp.productivityapp`

5. **Firebase Project ID**
    - Globally unique identifier for Firebase project
    - Lowercase letters, numbers, and hyphens only
    - Cannot be changed after creation
    - Example: `acme-productivity-prod`

6. **Company Information**
    - Company/Organization name
    - Department (optional)
    - City, State/Province, Country
    - Used in keystore certificates

7. **Keystore Credentials**
    - Strong password (minimum 8 characters recommended)
    - Key alias (defaults to project name)
    - Store securely - required for all future app updates

### Execution

#### Step 1: Launch the Script

```bash
./setup-project.sh
```

The script will display a welcome banner and outline the four-phase setup process.

#### Step 2: Prerequisite Validation

The script automatically checks for required tools and their versions. If any tools are missing:

- **Critical tools** (bash, keytool): Script will prompt you to install before continuing
- **Optional tools** (firebase, gh): Script will warn but allow you to continue

You can choose to:

- Install missing tools and restart
- Continue without optional tools (can run relevant steps later)

#### Step 3: Interactive Configuration

Respond to each prompt with the information you prepared:

```
Enter Android package name [e.g., com.example.myapp]: 
```

**Validation features:**

- Package name format validation
- Password confirmation matching
- Required field verification
- Format guidelines and examples

After all inputs are collected, the script displays a comprehensive summary for your review.

#### Step 4: Confirmation

Review the configuration summary carefully:

```
Configuration Summary
═════════════════════

Please review your configuration:
  Package Name: com.acmecorp.productivityapp
  Project Name: ProductivityApp
  ...

Is this correct? [y/n]:
```

Type `y` to proceed or `n` to restart the configuration process.

#### Step 5: Automated Execution

The script executes four sequential steps:

**STEP 1: Project Customization** (1-2 minutes)

- Updates package names in all source files
- Renames project references
- Modifies configuration files
- Updates build scripts

**STEP 2: Firebase Setup** (2-5 minutes)

- Prompts for Firebase CLI execution
- Creates/selects Firebase project
- Registers applications
- Downloads configuration files

**Note**: You can skip Firebase setup and run it manually later.

**STEP 3: Keystore Generation** (1-2 minutes)

- Creates signing keystores
- Generates encrypted secrets file
- Updates build configurations
- Encodes sensitive files

**STEP 4: Summary and Documentation** (< 1 minute)

- Generates setup reference document
- Displays configuration summary
- Provides next steps guidance

### Completion

Upon successful completion, you'll see:

```
╔════════════════════════════════════════════════════════════════╗
║                                                                ║
║  🚀 Your KMP project is ready for development! 🚀              ║
║                                                                ║
╚════════════════════════════════════════════════════════════════╝
```

## Configuration Options

### Input Parameters

All configuration is provided through interactive prompts. The script does not accept command-line
arguments to ensure proper validation and user confirmation.

### Required Inputs

| Input               | Validation                    | Example            |
|---------------------|-------------------------------|--------------------|
| Android Package     | Lowercase, dots, valid format | `com.company.app`  |
| Project Name        | Non-empty string              | `MyKMPApp`         |
| Firebase Project ID | Lowercase, hyphens allowed    | `myapp-prod-12345` |
| Keystore Password   | Minimum 6 characters          | `********`         |
| Company Name        | Non-empty string              | `Acme Corporation` |
| City                | Non-empty string              | `San Francisco`    |
| State               | Non-empty string              | `California`       |
| Country Code        | 2-letter code                 | `US`               |

### Optional Inputs

| Input            | Default         | Purpose                       |
|------------------|-----------------|-------------------------------|
| App Display Name | Project Name    | User-facing application name  |
| iOS Bundle ID    | Android Package | iOS application identifier    |
| Department       | (empty)         | Certificate organization unit |
| Key Alias        | Project Name    | Keystore key identifier       |

## Generated Files and Structure

After successful execution, your project will contain:

### Keystore Files

```
keystores/
├── original.keystore      # Production release signing key
└── upload.keystore        # Google Play upload signing key
```

**Security Note**: These files are critical and should never be committed to version control or
shared publicly.

### Configuration Files

```
secrets/android/keystores/keystore_password        # Keystore password (gitignored)
secrets/android/keystores/keystore_alias           # Keystore alias (gitignored)
secrets/android/keystores/keystore_alias_password  # Alias password (gitignored)
gradle/fork.properties                             # Keystore DN + org identity (gitignored)
PROJECT_SETUP_INFO.txt                             # Setup documentation
```

> **Note:** `secrets.env` is retired (2026-06-23). Keystore passwords now live in
> `secrets/android/keystores/` per-value files; keystore DN is in `gradle/fork.properties`.

### Firebase Configuration

```
cmp-android/
└── google-services.json   # Android Firebase config (4 build variants)

cmp-ios/iosApp/
└── GoogleService-Info.plist  # iOS Firebase config

secrets/
└── google-services.json   # Backup copy
```

### Updated Project Files

The following existing files are modified:

- `gradle/libs.versions.toml` - Package namespace
- `settings.gradle.kts` - Root project name
- `cmp-android/build.gradle.kts` - Build configuration
- `fastlane-config/project_config.rb` - CI/CD configuration
- All Kotlin source files - Package declarations
- All module `build.gradle.kts` files - Namespace declarations

## Post-Setup Steps

The setup script prepares your project foundation, but several manual steps are required to complete
the configuration:

### 1. Firebase Service Configuration

Navigate to the [Firebase Console](https://console.firebase.google.com) and enable required
services:

**Recommended Services:**

- **Authentication**: User sign-in and identity management
- **Cloud Firestore**: Real-time NoSQL database
- **Cloud Storage**: File and media storage
- **Crashlytics**: Crash reporting and analytics
- **Analytics**: User behavior tracking
- **App Distribution**: Internal testing distribution

**For each service:**

1. Click "Get Started" or "Enable"
2. Follow service-specific configuration
3. Review security rules and settings
4. Configure platform-specific options

### 2. Firebase App Distribution Setup

For internal testing builds:

1. Navigate to **Release & Monitor > App Distribution**
2. Create tester groups:
    - Internal testers (development team)
    - QA testers
    - Beta testers
3. Add tester emails to appropriate groups
4. Configure distribution settings in `fastlane-config/project_config.rb`

### 3. Download Service Account Credentials

For CI/CD automation, download Firebase service account keys:

1. Go to **Project Settings > Service Accounts**
2. Click **Generate New Private Key**
3. Save as `firebaseAppDistributionServiceCredentialsFile.json`
4. Place in the `secrets/` directory
5. Run: `bash scripts/white-label/keystore.sh encode-secrets`

### 4. Play Store Configuration (Optional)

For Google Play Store deployment:

1. Create service account in Google Cloud Console
2. Grant required permissions in Play Console
3. Download JSON key file
4. Save as `playStorePublishServiceCredentialsFile.json`
5. Place in `secrets/` directory
6. Run: `bash scripts/white-label/keystore.sh encode-secrets`

### 5. iOS Code Signing Configuration

For iOS builds and deployment:

1. Open `cmp-ios/iosApp.xcodeproj` in Xcode
2. Select the project in the navigator
3. Go to **Signing & Capabilities** tab
4. Configure:
    - Team: Select your Apple Developer team
    - Bundle Identifier: Verify it matches your iOS Bundle ID
    - Signing Certificate: Configure or let Xcode manage automatically

5. Add `GoogleService-Info.plist` to Xcode project:
    - Drag file from `cmp-ios/iosApp/` to Xcode
    - Ensure "Copy items if needed" is checked
    - Add to appropriate targets

### 6. Verify Build Configuration

Test all build variants to ensure proper configuration:

```bash
# Android builds
./gradlew :cmp-android:assembleRelease
./gradlew :cmp-android:assembleDebug
./gradlew :cmp-android:assembleDemoRelease
./gradlew :cmp-android:assembleDemoDebug

# iOS build (in Xcode)
# Product > Build (⌘B)
```

All builds should complete successfully without configuration errors.

### 7. Version Control Setup

Before committing to version control:

1. **Review `.gitignore`**: Ensure sensitive files are excluded
   ```gitignore
   # Already included in template .gitignore
   # secrets.env — retired; replaced by secrets/android/keystores/ per-value files
   secrets/*
   keystores/*.keystore
   local.properties
   ```

2. **Safe to commit**:
    - `cmp-android/google-services.json`
    - `cmp-ios/iosApp/GoogleService-Info.plist`
    - `fastlane-config/project_config.rb`
    - All source code changes

3. **Initial commit**:
   ```bash
   git add .
   git commit -m "Configure project with custom package and Firebase"
   git push origin main
   ```

### 8. CI/CD Integration (Optional)

If using GitHub Actions or similar CI/CD:

1. Upload secrets to repository:
   ```bash
   bash scripts/white-label/keystore.sh add --repo=username/repository
   ```

2. Configure environment-specific settings
3. Test CI/CD pipeline with test builds
4. Set up automated deployments

## Troubleshooting

### Common Issues and Solutions

#### Script Won't Execute

**Symptom**: `Permission denied` error

**Solution**:

```bash
chmod +x setup-project.sh
./setup-project.sh
```

#### Bash Version Error

**Symptom**: `Bash version 4+ required`

**Solution** (macOS):

```bash
# Install modern bash
brew install bash

# Use the new bash
/usr/local/bin/bash setup-project.sh
```

#### Firebase CLI Not Found

**Symptom**: `Firebase CLI is not installed`

**Solution**:

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Verify installation
firebase --version

# Login
firebase login
```

#### Firebase Authentication Error

**Symptom**: `Not logged in to Firebase`

**Solution**:

```bash
# Logout and login again
firebase logout
firebase login

# Verify
firebase projects:list
```

#### Python 3 Not Found

**Symptom**: `Python3 is required`

**Solution**:

```bash
# macOS
brew install python3

# Ubuntu/Debian
sudo apt-get update
sudo apt-get install python3

# Verify
python3 --version
```

#### Invalid Package Name

**Symptom**: `Invalid package name format`

**Solution**: Ensure your package name:

- Uses only lowercase letters
- Contains at least one dot
- Follows reverse domain notation
- Contains no spaces or special characters (except dots)

**Valid examples**:

- ✅ `com.company.app`
- ✅ `io.github.username.project`
- ✅ `dev.myteam.mobile.android`

**Invalid examples**:

- ❌ `MyApp` (no dots)
- ❌ `Com.Company.App` (uppercase letters)
- ❌ `com.company app` (space)
- ❌ `com.company.app!` (special character)

#### Keystore Generation Failed

**Symptom**: `Failed to create keystore`

**Solution**:

1. Verify keytool is accessible: `keytool -help`
2. Ensure Java JDK is properly installed
3. Check available disk space
4. Verify keystores directory permissions

#### Firebase Project Already Exists

**Symptom**: `Firebase project already exists`

**Status**: This is expected behavior, not an error.

**Action**: The script will use the existing project and continue normally.

#### Configuration Interrupted

**Symptom**: Script stopped mid-execution

**Solution**:

- You can safely re-run the script
- Previously completed steps will detect existing configurations
- The script will resume from the interrupted point
- Some manual cleanup may be required for partial operations

### Getting Help

If you encounter issues not covered here:

1. **Review the Error Message**: The script provides detailed error messages with context
2. **Check Prerequisites**: Ensure all required tools are properly installed
3. **Verify Permissions**: Confirm you have write permissions in the project directory
4. **Review Logs**: Check terminal output for specific error details
5. **Manual Execution**: Try running individual scripts separately to isolate issues

## Advanced Usage

### Running Individual Steps

If you prefer more control or need to re-run specific steps:

```bash
# Step 1: Customization only
bash scripts/white-label/fork-init.sh com.company.app MyApp "My Application"

# Step 2: Firebase only
bash scripts/white-label/firebase.sh my-firebase-project com.company.app

# Step 3: Keystore generation only
bash scripts/white-label/keystore.sh generate
```

### Re-running the Setup

To reconfigure an existing project:

1. **Backup your changes**: Commit or stash any modifications
2. **Delete generated files**:
   ```bash
   rm -rf keystores/
   rm -rf secrets/android/keystores/
   rm PROJECT_SETUP_INFO.txt
   ```
3. **Run setup again**: `./setup-project.sh`

**Warning**: This will overwrite existing configurations.

### Environment-Specific Setup

For multiple environments (development, staging, production):

```bash
# Development
./setup-project.sh
# Use: myapp-dev, com.company.app.dev

# Staging
./setup-project.sh
# Use: myapp-staging, com.company.app.staging

# Production
./setup-project.sh
# Use: myapp-prod, com.company.app
```

Maintain separate Firebase projects and package suffixes for each environment.

### Customizing the Setup Process

To modify the setup behavior:

1. **Fork or copy the script**
2. **Edit configuration sections** as needed
3. **Preserve validation logic** to prevent configuration errors
4. **Test thoroughly** before using in production projects

## Frequently Asked Questions

### Can I change my package name later?

Yes, but it requires manual updates across many files. It's recommended to choose the correct
package name initially. If needed, you can:

1. Run the customizer script again with the new package name
2. Manually update Firebase configuration
3. Re-generate keystores if the package name is used in certificates

### What happens if I skip Firebase setup?

You can skip Firebase setup during the main script execution and run it later:

```bash
bash scripts/white-label/firebase.sh <firebase-project-id> <ios-bundle-id>
```

The Android and iOS apps will build successfully without Firebase, but Firebase features won't work
until configured.

### Are the generated keystores production-ready?

Yes, the generated keystores use industry-standard security:

- RSA algorithm with 2048-bit key size
- 25-year validity period
- Secure password protection

However, ensure you:

- Store keystores securely
- Back up keystores to multiple secure locations
- Never commit keystores to version control
- Use strong, unique passwords

### Can I use this for existing projects?

This script is designed for new projects from the template. For existing projects:

- Review what the script does
- Run individual component scripts as needed
- Manually verify all changes
- Test thoroughly before committing

### How do I update Firebase configuration later?

To update Firebase settings:

1. Make changes in Firebase Console
2. Download new configuration files
3. Replace existing files in your project:
    - `cmp-android/google-services.json`
    - `cmp-ios/iosApp/GoogleService-Info.plist`
4. Rebuild the project

### What if my company has different keystore requirements?

You can modify the keystore generation settings:

- Edit `gradle/fork.properties` to set your DN fields (`keystore.dn.org_unit`, `keystore.dn.city`,
  `keystore.dn.state`, `keystore.dn.country`, `legal.company.name`) before running keystore generation
- Generation constants (VALIDITY=25 years, KEYALG=RSA, KEYSIZE=2048) are hard-coded defaults
  in `deployment/_shared/scripts/keystore-manager.sh` and intentionally not per-fork config

### Do I need to run this script every time I clone the repository?

No. The script is only needed once during initial project setup. Team members cloning the repository
need only:

1. Set up their local environment (JDK, Android SDK, etc.)
2. Obtain keystore password files from a secure team store and place them in
   `secrets/android/keystores/` (keystore_password, keystore_alias, keystore_alias_password)
3. Run a standard Gradle sync

### How do I share the configuration with my team?

**Safe to share via version control**:

- All source code changes
- Build configuration files
- Firebase configuration files (if not sensitive)

**Share securely outside version control**:

- `secrets/android/keystores/` per-value files (use a secure team password manager or
  encrypted storage — `secrets.env` is retired)
- Actual keystore files (secure backup location, limited access)

### Can I automate this script for CI/CD?

The script is interactive and requires user input. For automated environments:

1. Use the individual component scripts with command-line arguments
2. Pre-populate `secrets/android/keystores/` per-value files and `gradle/fork.properties`
3. Set environment variables for credentials
4. Use non-interactive modes where available
