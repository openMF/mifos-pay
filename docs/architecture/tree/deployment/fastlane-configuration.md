# Fastlane Configuration

This repository contains Fastlane configuration for automating iOS and Android app deployment
processes. The setup includes support for Firebase App Distribution, Google Play Store deployment,
and automated versioning.

## Prerequisites

- Ruby version 2.5 or higher
- Fastlane installed (`gem install fastlane`)
- Firebase CLI (for Firebase deployments)
- Google Play Console access (for Android Play Store deployments)
- Apple Developer account (for iOS deployments)

## Setup

1. Clone the repository
2. Install dependencies:
   ```bash
   bundle install
   ```
3. Set up your environment variables or update your platform configurations in `fastlane-config/`
   directory.

## Configuration

### Environment Variables

The following environment variables can be set to override default configurations:

#### Android

```bash
ANDROID_STORE_FILE="path/to/keystore"
ANDROID_STORE_PASSWORD="your_store_password"
ANDROID_KEY_ALIAS="your_key_alias"
ANDROID_KEY_PASSWORD="your_key_password"
FIREBASE_ANDROID_APP_ID="your_firebase_app_id"
```

#### iOS

```bash
FIREBASE_IOS_APP_ID="your_firebase_app_id"
```

#### Common

```bash
FIREBASE_SERVICE_CREDS_FILE="path/to/service/creds.json"
FIREBASE_GROUPS="your_test_groups"
```

### Configuration File

The `fastlane-config` directory contains default configurations for each platform that can be
overridden by environment variables or lane parameters. Update these files with your
project-specific values.

## Available Lanes

### Android Lanes

```bash
# Build debug APK
fastlane android assembleDebugApks

# Build release APK
fastlane android assembleReleaseApks 
# Optional params: store_file, store_password, key_alias, key_password

# Deploy to Firebase App Distribution
fastlane android deployReleaseApkOnFirebase
# Optional params: store_file, store_password, key_alias, key_password

# Deploy demo build to Firebase
fastlane android deployDemoApkOnFirebase

# Deploy to Play Store internal track
fastlane android deployInternal

# Generate version number
fastlane android generateVersion
# Optional param: platform (git|firebase|playstore)

# Generate release notes
fastlane android generateReleaseNote

# Generate full release notes
fastlane android generateFullReleaseNote
# Optional param: fromTag
```

### iOS Lanes

```bash
# Build iOS app
fastlane ios build_ios
# Optional param: configuration (Debug|Release)

# Deploy to Firebase
fastlane ios deploy_on_firebase

# Increment version
fastlane ios increment_version
```

## Version Generation

The system supports three versioning strategies:

1. **Git-based**: Uses commit count (`git`)
2. **Firebase-based**: Increments from last Firebase version (`firebase`)
3. **Play Store-based**: Increments from last Play Store version (`playstore`)

## Release Notes Generation

Two types of release notes can be generated:

1. **Simple**: Latest commit message
   ```bash
   fastlane android generateReleaseNote
   ```

2. **Full**: Categorized changes since last tag
   ```bash
   fastlane android generateFullReleaseNote
   ```

## CI/CD Integration

### GitHub Actions Example

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Deploy to Firebase
        env:
          ANDROID_STORE_PASSWORD: ${{ secrets.ANDROID_STORE_PASSWORD }}
          ANDROID_KEY_PASSWORD: ${{ secrets.ANDROID_KEY_PASSWORD }}
        run: |
          fastlane android deployReleaseApkOnFirebase
```

### GitLab CI Example

```yaml
deploy_firebase:
  script:
    - fastlane android deployReleaseApkOnFirebase
  variables:
    ANDROID_STORE_PASSWORD: ${ANDROID_STORE_PASSWORD}
    ANDROID_KEY_PASSWORD: ${ANDROID_KEY_PASSWORD}
```

## Best Practices

1. Never commit sensitive data (passwords, keys) to version control
2. Use environment variables for sensitive data in CI/CD
3. Keep the `fastlane-config` directory files updated with latest configurations
4. Regularly update Fastlane and its plugins
5. Test deployment scripts in a staging environment first

## Troubleshooting

Common issues and solutions:

1. **Keystore not found**
    - Ensure keystore path is correct in config or environment variable
    - Verify keystore file exists in the specified location

2. **Firebase deployment fails**
    - Check Firebase service credentials file exists and is valid
    - Verify Firebase app ID is correct
    - Ensure you have required permissions

3. **Version generation fails**
    - For git-based: Ensure git history is available
    - For Firebase: Check Firebase credentials and permissions
    - For Play Store: Verify Play Console access and permissions