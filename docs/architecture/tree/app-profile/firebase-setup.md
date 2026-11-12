# Firebase Setup Script(Incubating)

> \[!Note] The script might not work as expected

## Overview

The Firebase Setup Script is an automated tool designed to streamline the Firebase project
configuration process for Kotlin Multiplatform projects. It intelligently manages Firebase app
registrations, generates configuration files, and integrates Firebase services with minimal manual
intervention.

### Key Capabilities

- Automated Firebase project creation and configuration
- Smart Android app variant registration (2 Firebase apps supporting 4 build variants)
- iOS app registration and configuration
- Automatic retrieval of Android package namespace from Gradle version catalog
- Generation and merging of `google-services.json` with support for multiple build variants
- Integration with Fastlane CI/CD configuration
- Backup management of sensitive configuration files

## Architecture

The script implements an efficient Firebase app registration strategy:

### Firebase Console Registration

- **Release Variant**: Base application package
- **Demo Variant**: Demo environment package

### Local Configuration (google-services.json)

- **Release**: Production variant
- **Debug**: Development variant (shares Release credentials)
- **Demo**: Demo environment variant
- **Demo Debug**: Demo development variant (shares Demo credentials)

This approach optimizes Firebase project quota usage while maintaining full support for all Gradle
build variants.

## Prerequisites

### Required Tools

1. **Firebase CLI** (v11.0.0 or higher)
   ```bash
   npm install -g firebase-tools
   ```

2. **Python 3** (v3.6 or higher)
    - Required for JSON configuration merging
    - Must be available in system PATH

3. **Bash Shell** (v4.0 or higher)
    - Pre-installed on macOS and Linux
    - Windows users: Use Git Bash or WSL

### Authentication Requirements

- Valid Google account with Firebase project creation permissions
- Authenticated Firebase CLI session

### Project Requirements

- Kotlin Multiplatform project structure
- `gradle/libs.versions.toml` file with `androidPackageNamespace` defined
- Standard Android and iOS module structure

## Installation

### 1. Download the Script

```bash
# The script is included in the project template
cd /path/to/your/project
```

### 2. Make Executable

```bash
chmod +x scripts/white-label/firebase.sh
```

### 3. Authenticate with Firebase

```bash
firebase login
```

### 4. Verify Prerequisites

```bash
# Check Firebase CLI
firebase --version

# Check Python 3
python3 --version

# Check Bash version
bash --version
```

## Configuration

### Gradle Version Catalog

Ensure your `gradle/libs.versions.toml` contains the Android package namespace:

```toml
[versions]
# Android Version
androidPackageNamespace = "com.yourcompany.yourapp"

# Other version configurations...
```

The script automatically reads this value to configure all Firebase app variants.

## Usage

### Basic Syntax

```bash
bash scripts/white-label/firebase.sh <firebase-project-id> <ios-bundle-id>
```

### Parameters

| Parameter             | Type   | Description                        | Example             |
|-----------------------|--------|------------------------------------|---------------------|
| `firebase-project-id` | String | Unique Firebase project identifier | `myapp-prod-12345`  |
| `ios-bundle-id`       | String | iOS application bundle identifier  | `com.example.myapp` |

**Note**: The Android package namespace is automatically read from `gradle/libs.versions.toml`.

### Example Usage

```bash
# Standard setup
bash scripts/white-label/firebase.sh mycompany-app-production com.mycompany.myapp

# Development environment
bash scripts/white-label/firebase.sh mycompany-app-dev com.mycompany.myapp.dev

# Staging environment
bash scripts/white-label/firebase.sh mycompany-app-staging com.mycompany.myapp.staging
```

## Execution Flow

### Phase 1: Initialization

1. Validates Firebase CLI installation and authentication
2. Reads Android package namespace from version catalog
3. Verifies project structure and dependencies

### Phase 2: Firebase Project Setup

1. Creates Firebase project (if not exists)
2. Configures project settings
3. Sets active Firebase project context

### Phase 3: Application Registration

1. Registers Android Release variant
2. Registers Android Demo variant
3. Registers iOS application
4. Retrieves Firebase App IDs for all registered applications

### Phase 4: Configuration Generation

1. Downloads `google-services.json` for Release variant
2. Downloads `google-services.json` for Demo variant
3. Generates Debug and Demo Debug configurations via duplication
4. Merges all four variants into single `google-services.json`
5. Downloads `GoogleService-Info.plist` for iOS

### Phase 5: Integration

1. Updates Fastlane configuration with Firebase App IDs
2. Copies configuration files to secrets directory
3. Validates configuration integrity

## Output Files

### Generated Files

| File                       | Location          | Purpose                                     |
|----------------------------|-------------------|---------------------------------------------|
| `google-services.json`     | `cmp-android/`    | Android Firebase configuration (4 variants) |
| `GoogleService-Info.plist` | `cmp-ios/iosApp/` | iOS Firebase configuration                  |
| `google-services.json`     | `secrets/`        | Backup copy for CI/CD                       |

### Modified Files

| File                | Location           | Modifications                         |
|---------------------|--------------------|---------------------------------------|
| `project_config.rb` | `fastlane-config/` | Firebase App IDs for Release and Demo |

## Configuration File Structure

### google-services.json

```json
{
  "project_info": {
    "project_number": "123456789",
    "project_id": "your-project-id",
    "storage_bucket": "your-project-id.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "android_client_info": {
          "package_name": "com.yourcompany.yourapp"
        }
      },
      "oauth_client": [
        ...
      ],
      "api_key": [
        ...
      ],
      "services": {
        ...
      }
    },
    {
      "client_info": {
        "android_client_info": {
          "package_name": "com.yourcompany.yourapp.debug"
        }
      },
      "oauth_client": [
        ...
      ],
      "api_key": [
        ...
      ],
      "services": {
        ...
      }
    },
    {
      "client_info": {
        "android_client_info": {
          "package_name": "com.yourcompany.yourapp.demo"
        }
      },
      "oauth_client": [
        ...
      ],
      "api_key": [
        ...
      ],
      "services": {
        ...
      }
    },
    {
      "client_info": {
        "android_client_info": {
          "package_name": "com.yourcompany.yourapp.demo.debug"
        }
      },
      "oauth_client": [
        ...
      ],
      "api_key": [
        ...
      ],
      "services": {
        ...
      }
    }
  ]
}
```

## Verification

### Validate Firebase Console

1. Navigate to [Firebase Console](https://console.firebase.google.com)
2. Select your project
3. Navigate to **Project Settings > Your apps**
4. Verify the following apps are registered:
    - Android Release
    - Android Demo
    - iOS application

### Validate Local Configuration

```bash
# Verify google-services.json contains 4 clients
python3 << EOF
import json

with open('cmp-android/google-services.json', 'r') as f:
    data = json.load(f)
    clients = data.get('client', [])
    
    print(f"Total configured variants: {len(clients)}")
    print("\nConfigured packages:")
    for client in clients:
        pkg = client['client_info']['android_client_info']['package_name']
        print(f"  - {pkg}")
EOF
```

Expected output:

```
Total configured variants: 4

Configured packages:
  - com.yourcompany.yourapp
  - com.yourcompany.yourapp.debug
  - com.yourcompany.yourapp.demo
  - com.yourcompany.yourapp.demo.debug
```

### Validate Build Configuration

```bash
# Test all Android build variants
./gradlew :cmp-android:assembleRelease
./gradlew :cmp-android:assembleDebug
./gradlew :cmp-android:assembleDemoRelease
./gradlew :cmp-android:assembleDemoDebug
```

All builds should complete successfully without Firebase configuration errors.

## Troubleshooting

### Firebase CLI Not Found

**Error Message:**

```
Firebase CLI is not installed
```

**Resolution:**

```bash
npm install -g firebase-tools
firebase --version
```

### Authentication Failure

**Error Message:**

```
Not logged in to Firebase
```

**Resolution:**

```bash
firebase login
firebase projects:list
```

### Python 3 Not Available

**Error Message:**

```
Python3 is required to create google-services.json with multiple variants
```

**Resolution:**

**macOS:**

```bash
brew install python3
```

**Ubuntu/Debian:**

```bash
sudo apt-get update
sudo apt-get install python3
```

**Windows:**
Download and install from [python.org](https://www.python.org/downloads/)

### Package Namespace Not Found

**Error Message:**

```
Could not find androidPackageNamespace in libs.versions.toml
```

**Resolution:**

Edit `gradle/libs.versions.toml` and add:

```toml
[versions]
androidPackageNamespace = "com.yourcompany.yourapp"
```

### Firebase App Already Exists

**Warning Message:**

```
Already registered: com.yourcompany.yourapp
```

**Status:** This is expected behavior when re-running the script. The script detects existing apps
and skips re-registration.

### Configuration Download Failure

**Error Message:**

```
Failed to download google-services.json
```

**Resolution:**

1. Verify Firebase project exists and is accessible
2. Ensure Firebase App IDs are correctly registered
3. Check network connectivity
4. Re-authenticate with Firebase CLI:
   ```bash
   firebase logout
   firebase login
   ```

## Best Practices

### Project Naming Convention

Use descriptive, environment-specific Firebase project IDs:

```bash
# Production
bash scripts/white-label/firebase.sh mycompany-myapp-prod com.mycompany.myapp

# Staging
bash scripts/white-label/firebase.sh mycompany-myapp-staging com.mycompany.myapp

# Development
bash scripts/white-label/firebase.sh mycompany-myapp-dev com.mycompany.myapp
```

### Environment Separation

Maintain separate Firebase projects for different environments:

- **Development**: Internal testing, experimental features
- **Staging**: QA validation, client demos
- **Production**: Live user-facing application

### Security Considerations

1. **Version Control**: Never commit `google-services.json` to version control if it contains
   sensitive credentials
2. **CI/CD Integration**: Store Firebase configurations as encrypted secrets
3. **Access Control**: Limit Firebase project access to authorized team members only
4. **Monitoring**: Enable Firebase Analytics to track app usage patterns

### Configuration Management

```bash
# Store in secrets directory (gitignored)
cp cmp-android/google-services.json secrets/

# Use in CI/CD pipeline
echo "$FIREBASE_CONFIG" | base64 -d > cmp-android/google-services.json
```

## Integration with CI/CD

### GitHub Actions Example

```yaml
name: Firebase Configuration

on: [ push, pull_request ]

jobs:
  configure:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Setup Firebase Configuration
        env:
          GOOGLE_SERVICES: ${{ secrets.GOOGLE_SERVICES_BASE64 }}
        run: |
          echo "$GOOGLE_SERVICES" | base64 -d > cmp-android/google-services.json

      - name: Build Application
        run: ./gradlew :cmp-android:assembleRelease
```

### Fastlane Integration

The script automatically updates `fastlane-config/project_config.rb` with Firebase App IDs, enabling
seamless Fastlane integration:

```ruby
# Fastlane will automatically use the configured Firebase App IDs
lane :deploy_internal do
  gradle(task: 'assembleDemoRelease')
  firebase_app_distribution(
    app: ENV['FIREBASE_DEMO_APP_ID']
  )
end
```

## Advanced Usage

### Custom Package Namespace

To use a different package namespace without modifying `libs.versions.toml`:

1. Temporarily modify the version catalog
2. Run the script
3. Revert the version catalog changes

**Note**: This approach is not recommended for production use.

### Regenerating Configuration

To regenerate Firebase configuration:

1. Delete existing `google-services.json`
2. Re-run the script with the same Firebase project ID
3. Existing Firebase app registrations will be detected and reused

### Multiple iOS Targets

For projects with multiple iOS targets:

```bash
# Setup primary target
bash scripts/white-label/firebase.sh myapp-prod com.mycompany.myapp

# Manually register additional targets via Firebase Console
firebase apps:create IOS com.mycompany.myapp.widget \
  --project=myapp-prod
```

## Limitations

1. **Python Dependency**: Requires Python 3 for JSON merging operations
2. **Firebase CLI**: Must be authenticated before script execution
3. **Single iOS App**: Script registers one iOS app per execution
4. **Package Namespace Source**: Android package must be defined in version catalog

## Support

For issues, questions, or contributions:

1. Review this documentation thoroughly
2. Check the troubleshooting section
3. Verify all prerequisites are met
4. Consult Firebase documentation: [firebase.google.com/docs](https://firebase.google.com/docs)
