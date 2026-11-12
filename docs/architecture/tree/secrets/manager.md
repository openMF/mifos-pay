# Secrets Manager

A comprehensive bash script for managing Android keystores, automating the signing configuration,
and handling GitHub secrets.

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Key Features](#key-features)
- [Usage](#usage)
    - [Generating Keystores](#generating-keystores)
    - [Viewing Secrets](#viewing-secrets)
    - [Adding Secrets to GitHub](#adding-secrets-to-github)
    - [Listing GitHub Secrets](#listing-github-secrets)
    - [Deleting GitHub Secrets](#deleting-github-secrets)
    - [Deleting All GitHub Secrets](#deleting-all-github-secrets)
- [Configuration](#configuration)
    - [Environment File (secrets.env)](#environment-file-secretsenv)
    - [Files Updated by Script](#files-updated-by-script)
- [Directory Structure](#directory-structure)
- [Troubleshooting](#troubleshooting)
- [FAQ](#faq)

## Overview

The Android Keystore Manager automates the process of creating, storing, and configuring Android app
signing keys. It can:

- Generate debug (ORIGINAL) and release (UPLOAD) keystores with custom information
- Securely store keystore configurations and passwords
- Update fastlane and Gradle build configurations automatically
- Manage GitHub secrets for CI/CD pipelines
- Provide a clear view of all managed secrets

This tool is especially useful for development teams that need to maintain consistent signing
configurations across multiple environments and CI/CD pipelines.

## Installation

1. Make it executable:
   ```bash
   chmod +x scripts/white-label/keystore.sh
   ```

2. Ensure you have the required dependencies:
    - Java Development Kit (JDK) for keytool
    - GitHub CLI (gh) for GitHub secrets management

## Key Features

- **Dual Keystore Management**: Creates and maintains both debug (ORIGINAL) and release (UPLOAD)
  keystores
- **Automated Configuration**: Updates Gradle and fastlane configurations automatically
- **GitHub Secrets Integration**: Easily manage secrets for CI/CD pipelines
- **Secret Visualization**: View all secrets in a nicely formatted table
- **Secure Storage**: Keystores are stored in a separate 'keystores' directory
- **Flexible Certificate Information**: Support for custom company and organization details

## Usage

### Generating Keystores

```bash
scripts/white-label/keystore.sh generate
```

This command:

1. Creates the `keystores` directory if it doesn't exist
2. Generates the UPLOAD keystore (Play App Signing — single keystore model)
3. Reads keystore DN (CN/OU/O/L/ST/C) from `gradle/fork.properties` (`legal.company.name`
   and `keystore.dn.*` keys) — **not** from `secrets.env` (retired)
4. Reads passwords from `secrets/android/keystores/{keystore_password,keystore_alias,
   keystore_alias_password}` per-value files — **not** from `secrets.env` (retired)
5. Updates fastlane config in `fastlane-config/project_config.rb`
6. Updates Gradle config in `cmp-android/build.gradle.kts`

### Viewing Secrets

```bash
scripts/white-label/keystore.sh view
```

Displays all secrets from `secrets/shared/secrets.env` in a nicely formatted table. For multiline
values (like base64-encoded keystores), it shows `[MULTILINE VALUE]` instead of the actual content.

> **Note:** This subcommand reads the legacy `secrets/shared/secrets.env` bundle which is still used
> for iOS/macOS secrets. Android keystore passwords now live in `secrets/android/keystores/`.

### Adding Secrets to GitHub

```bash
scripts/white-label/keystore.sh add --repo=username/repo
```

**DEPRECATED.** Adds all secrets from `secrets/shared/secrets.env` to the specified GitHub
repository (excludes certificate information and local configuration values).

Prefer `scripts/secrets/sync-secrets-to-github.sh` which reads `secrets/` per-value files
directly and does not require an intermediate `secrets.env` bundle.

You can target a specific GitHub environment:

```bash
scripts/white-label/keystore.sh add --repo=username/repo --env=production
```

### Listing GitHub Secrets

```bash
scripts/white-label/keystore.sh list --repo=username/repo
```

Lists all secrets currently stored in the specified GitHub repository.

### Deleting GitHub Secrets

```bash
scripts/white-label/keystore.sh delete --repo=username/repo --name=SECRET_NAME
```

Deletes a specific secret from the GitHub repository.

### Deleting All GitHub Secrets

```bash
scripts/white-label/keystore.sh delete-all --repo=username/repo
```

**DEPRECATED.** Deletes all secrets defined in `secrets/shared/secrets.env` from the GitHub
repository (excluding certificate information and local configuration values).

To also delete excluded secrets:

```bash
scripts/white-label/keystore.sh delete-all --repo=username/repo --include-excluded
```

## Configuration

### Secret Storage — New Model (2026-06-23)

`secrets.env` at the repo root is **retired**. Secrets now have canonical homes:

| Category | File(s) | Secret? |
|---|---|---|
| Keystore password | `secrets/android/keystores/keystore_password` | Yes — gitignored |
| Keystore alias | `secrets/android/keystores/keystore_alias` | Yes — gitignored |
| Alias password | `secrets/android/keystores/keystore_alias_password` | Yes — gitignored |
| Keystore DN (CN/OU/O/L/ST/C) | `gradle/fork.properties` (`legal.company.name`, `keystore.dn.*`) | No — gitignored template, non-secret values |

To populate the password files run `scripts/secrets/setup-secrets.sh android` or have
`setup-project.sh` write them automatically.

The legacy `secrets/shared/secrets.env` bundle is still used by the iOS/macOS sync flow
(`scripts/white-label/keystore.sh sync`) — it is not removed, only the Android keystore password section
is retired from it.

### Files Updated by Script

The script automatically updates several configuration files:

1. **fastlane-config/android_config.rb**:
   ```ruby
   module FastlaneConfig
     module AndroidConfig
       STORE_CONFIG = {
         default_store_file: "upload.keystore",
         default_store_password: "upload_keystore_password",
         default_key_alias: "upload_key",
         default_key_password: "upload_key_password"
       }
       # ... other configurations ...
     end
   end
   ```

2. **cmp-android/build.gradle.kts**:
   ```kotlin
   android {
     signingConfigs {
       create("release") {
         storeFile = file(System.getenv("KEYSTORE_PATH") ?: "../keystores/upload.keystore")
         storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "upload_keystore_password"
         keyAlias = System.getenv("KEYSTORE_ALIAS") ?: "upload_key"
         keyPassword = System.getenv("KEYSTORE_ALIAS_PASSWORD") ?: "upload_key_password"
         enableV1Signing = true
         enableV2Signing = true
       }
     }
   }
   ```

## Directory Structure

After running the script, you should have the following directory structure:

```
project/
├── deployment/_shared/scripts/keystore-manager.sh  # The main script
├── keystores/                                       # Keystore files (gitignored)
│   └── upload_keystore.keystore                     # Upload keystore (Play App Signing)
├── secrets/android/keystores/                       # Keystore passwords (gitignored)
│   ├── keystore_password
│   ├── keystore_alias
│   └── keystore_alias_password
├── gradle/fork.properties                           # Keystore DN + org identity (gitignored)
├── fastlane-config/
│   └── project_config.rb                            # Updated by scripts/white-label/keystore.sh generate
└── cmp-android/
    └── build.gradle.kts                             # Updated by scripts/white-label/keystore.sh generate
```

> `secrets.env` at the repo root has been retired (2026-06-23).

## Troubleshooting

### Common Issues

1. **Error: keytool command not found**
    - Make sure you have JDK installed and keytool is in your PATH
    - Install JDK if needed: `sudo apt install default-jdk`

2. **GitHub CLI (gh) is not installed**
    - Install GitHub
      CLI: [https://cli.github.com/manual/installation](https://cli.github.com/manual/installation)
    - Log in with: `gh auth login`

3. **Error with multiline values (iOS/macOS sync)**
    - Ensure all multiline blocks in `secrets/shared/secrets.env` are properly closed with `EOF`
    - Check for proper formatting: `KEY<<EOF` and `EOF` should be on separate lines

4. **Configuration files not updated**
    - Check if the files exist at the expected paths
    - Verify file permissions: `chmod +rw path/to/file`

### Verifying Keystores

To verify that a keystore was generated correctly:

```bash
keytool -list -v -keystore keystores/upload_keystore.keystore \
  -storepass "$(cat secrets/android/keystores/keystore_password)"
```

## FAQ

**Q: What's the difference between ORIGINAL and UPLOAD keystores?**  
A: ORIGINAL (debug) keystores are for development/testing, while UPLOAD (release) keystores are for
production builds that get published to the Play Store.

**Q: Will my certificate information be uploaded to GitHub?**  
A: No. Certificate information (COMPANY_NAME, DEPARTMENT, etc.) is excluded from GitHub secrets by
default.

**Q: How do I change the keystore passwords?**  
A: Edit the per-value files in `secrets/android/keystores/` (keystore_password, keystore_alias,
keystore_alias_password), then re-run `scripts/white-label/keystore.sh generate`. The script reads those
files directly — `secrets.env` is retired.

**Q: Can I use this script in CI/CD pipelines?**  
A: Yes, the script is designed to work in both interactive and automated environments. For CI/CD,
you would typically use the `add` command to upload secrets.

**Q: How are my keystore files secured?**  
A: Keystore files are stored in the `keystores` directory and never directly uploaded to GitHub.
Only base64-encoded versions are added as GitHub secrets.

**Q: Do I need to manually update Gradle and fastlane configurations?**  
A: No, the script automatically updates both configurations when you run the `generate` command.