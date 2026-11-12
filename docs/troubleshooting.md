# Troubleshooting Guide - KMP Project Template

**Last Updated:** 2026-02-13
**Project:** KMP Project Template
**Platforms:** Android | iOS | macOS | Desktop | Web

---

## 📖 Table of Contents

1. [Quick Diagnosis](#quick-diagnosis)
2. [Android Build Issues](#android-build-issues)
3. [iOS Build Issues](#ios-build-issues)
4. [macOS Build Issues](#macos-build-issues)
5. [Desktop Build Issues](#desktop-build-issues)
6. [Web Build Issues](#web-build-issues)
7. [Deployment Failures](#deployment-failures)
8. [CI/CD Issues](#cicd-issues)
9. [Code Signing Problems](#code-signing-problems)
10. [Known Infrastructure Bugs](#known-infrastructure-bugs)
11. [Emergency Fixes](#emergency-fixes)

---

## Quick Diagnosis

**First Steps for Any Issue:**

```bash
# 1. Check the fork's setup — doctor.sh is the ONE entry for setup, verify and sync
scripts/white-label/doctor.sh --verify

# 2. Verify Git status
git status

# 3. Check recent commits
git log --oneline -5

# 4. Clean and rebuild
./gradlew clean

# 5. Check for known issues
cat docs/analysis/BUGS_AND_ISSUES.md
```

**Platform-Specific Quick Checks:**

| Platform | Quick Check Command |
|----------|---------------------|
| Android | `./gradlew :cmp-android:assembleDebug --stacktrace` |
| iOS | `./scripts/ios/verify_ios_deployment.sh` |
| macOS | `./gradlew :cmp-desktop:packageReleaseDistributionForCurrentOS` |
| Desktop | `./gradlew :cmp-desktop:packageDebugDistributionForCurrentOS` |
| Web | `./gradlew :cmp-web:jsBrowserDevelopmentWebpack` |

---

## Android Build Issues

### Issue 1: Manifest Merger Failure

**Symptoms:**
```
Execution failed for task ':cmp-android:processDebugManifest'
> Manifest merger failed
```

**Causes:**
- Conflicting permissions between main manifest and libraries
- Duplicate activity declarations
- Invalid AndroidManifest.xml syntax

**Solutions:**

```bash
# 1. View merged manifest
./gradlew :cmp-android:processDebugManifest --console=verbose

# 2. Check for duplicate declarations
grep -r "android:name" cmp-android/src/main/AndroidManifest.xml

# 3. Check library manifests
./gradlew :cmp-android:dependencies --configuration debugRuntimeClasspath

# 4. Add namespace override in build.gradle.kts if needed
android {
    namespace = "com.your.package"

    // Override conflicting permission
    defaultConfig {
        manifestPlaceholders["appName"] = "@string/app_name"
    }
}
```

**File:** `cmp-android/src/main/AndroidManifest.xml`

---

### Issue 2: Dependency Resolution Failed

**Symptoms:**
```
Could not resolve: com.example:library:1.0.0
```

**Causes:**
- Network issues preventing dependency download
- Wrong repository configuration
- Version conflict between dependencies
- Corrupted Gradle cache

**Solutions:**

```bash
# 1. Clear Gradle cache
./gradlew clean --refresh-dependencies

# 2. Check dependency tree for conflicts
./gradlew :cmp-android:dependencies

# 3. Force dependency update
rm -rf ~/.gradle/caches
./gradlew build

# 4. Check repository configuration
cat build.gradle.kts | grep -A 10 "repositories {"

# 5. Try offline mode if cache exists
./gradlew build --offline
```

**Files:**
- `build.gradle.kts` - Check repositories
- `gradle/libs.versions.toml` - Check version catalog
- `cmp-android/build.gradle.kts` - Check dependencies

---

### Issue 3: Duplicate Class Found

**Symptoms:**
```
Duplicate class com.example.MyClass found in modules:
  module1.jar (com.example:module1:1.0)
  module2.jar (com.example:module2:2.0)
```

**Causes:**
- Multiple dependencies provide the same class
- Transitive dependency conflicts
- Library bundling issues

**Solutions:**

```bash
# 1. View full dependency tree
./gradlew :cmp-android:dependencies --configuration debugRuntimeClasspath

# 2. Find duplicate in tree output
./gradlew :cmp-android:dependencies | grep -A 5 -B 5 "duplicate.class"

# 3. Exclude duplicate dependency in build.gradle.kts
dependencies {
    implementation("com.example:library:1.0.0") {
        exclude(group = "duplicate.group", module = "duplicate.module")
    }
}

# 4. Or force a specific version
configurations.all {
    resolutionStrategy {
        force("com.example:library:1.0.0")
    }
}
```

**File:** `cmp-android/build.gradle.kts`

---

### Issue 4: AAPT2 Error: Resource Not Found

**Symptoms:**
```
AAPT: error: resource drawable/my_image not found
```

**Causes:**
- Missing resource file
- Incorrect resource reference
- Resource file name contains invalid characters
- Resource not in correct directory

**Solutions:**

```bash
# 1. Check if resource exists
find cmp-android/src/main/res -name "my_image*"

# 2. Validate XML files
find cmp-android/src/main/res -type f -name "*.xml" -exec xmllint --noout {} \;

# 3. Check resource references in code
grep -r "R.drawable.my_image" cmp-android/src/

# 4. Clean and rebuild
./gradlew clean
./gradlew :cmp-android:assembleDebug

# 5. Check for resource name conflicts
find cmp-android/src/main/res -type f | sort | uniq -d
```

**Common Fixes:**
- Ensure resource file name uses only lowercase letters, numbers, underscores
- No hyphens allowed: `my-image.png` → `my_image.png`
- Check resource is in correct directory: `drawable/`, `layout/`, etc.

---

### Issue 5: Unsupported Class File Major Version

**Symptoms:**
```
Unsupported class file major version 61
```

**Causes:**
- Java version mismatch
- Dependency compiled with newer Java version
- JAVA_HOME not set correctly

**Solutions:**

```bash
# 1. Check Java version (should be 17)
java -version
./gradlew -version

# 2. Install correct Java version (macOS)
brew install openjdk@17

# 3. Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# 4. Verify in gradle.properties
cat gradle.properties | grep "org.gradle.java.home"

# 5. Update toolchain in build.gradle.kts if needed
kotlin {
    jvmToolchain(17)
}
```

**Files:**
- `gradle.properties`
- `build.gradle.kts`

---

### Issue 6: google-services.json Missing

**Symptoms:**
```
File google-services.json is missing
```

**Causes:**
- Firebase not set up
- File not in correct location
- File excluded by .gitignore

**Solutions:**

```bash
# 1. Check if file exists
ls -la cmp-android/google-services.json

# 2. Run Firebase setup if missing
scripts/white-label/firebase.sh

# 3. Or download manually from Firebase Console
# - Go to Firebase Console
# - Select project
# - Project Settings → Your apps → Android app
# - Download google-services.json
# - Place in cmp-android/ directory

# 4. Verify file structure (should have 4 variants)
cat cmp-android/google-services.json | jq '.client[].client_info.mobilesdk_app_id'

# 5. Check .gitignore doesn't exclude it
grep "google-services.json" .gitignore
```

**Expected location:** `cmp-android/google-services.json`

---

### Issue 7: Signing Configuration Missing

**Symptoms:**
```
Task :cmp-android:packageRelease FAILED
No key with alias 'MyKey' found in keystore
```

**Causes:**
- Keystore not generated
- Wrong keystore password
- Signing config not set up

**Solutions:**

```bash
# 1. Generate keystores if missing
scripts/white-label/keystore.sh generate

# 2. View keystore info
scripts/white-label/keystore.sh view

# 3. Check signing configuration in build.gradle.kts
cat cmp-android/build.gradle.kts | grep -A 20 "signingConfigs"

# 4. Verify keystore file exists
ls -la keystores/

# 5. Test keystore password
keytool -list -v -keystore keystores/original-release-key.jks

# 6. Check keystore credentials (new model — secrets.env is retired)
cat secrets/android/keystores/keystore_password
cat secrets/android/keystores/keystore_alias
```

**Files:**
- `keystores/upload_keystore.keystore`
- `secrets/android/keystores/keystore_password`
- `secrets/android/keystores/keystore_alias`
- `secrets/android/keystores/keystore_alias_password`
- `gradle/fork.properties` (keystore DN — non-secret)

---

## iOS Build Issues

### Issue 1: Code Sign Error - No Identity Found

**Symptoms:**
```
CodeSign error: No identity found
Command CodeSign failed with a nonzero exit code
```

**Causes:**
- Match certificates not installed
- Wrong provisioning profile
- Certificate expired

**Solutions:**

```bash
# 1. Run comprehensive iOS verification
./scripts/ios/verify_ios_deployment.sh

# 2. Re-fetch Match certificates
cd cmp-ios
bundle exec fastlane match adhoc --readonly
bundle exec fastlane match appstore --readonly

# 3. Check certificate expiration
security find-identity -v -p codesigning

# 4. Verify Match configuration
cat gradle/fork.properties | grep apple.match
cat secrets/apple/match/.match_password | wc -c   # verify password file exists

# 5. Check Xcode signing settings
open cmp-ios/iosApp.xcodeproj
# Target → Signing & Capabilities → check Team and Profile

# 6. Force new certificates if needed
bundle exec fastlane match adhoc --force_for_new_devices
bundle exec fastlane match appstore --force_for_new_devices
```

**Files:**
- `gradle/fork.properties` (Match URL/branch)
- `secrets/apple/match/match_ci_key`
- `secrets/apple/match/.match_password`
- `fastlane/Matchfile`

---

### Issue 2: ComposeApp XCFramework Missing or Stale

**Symptoms:**
```
error: no such module 'ComposeApp'
# or a stale UI — code changes in cmp-shared don't show up in the app
```

> Since the E6 SwiftPM/XCFramework migration there is no dependency-install step — iOS links the
> Kotlin `ComposeApp` framework as an XCFramework assembled by Gradle and embedded by the
> Xcode `[KMP] Embed and Sign ComposeApp XCFramework` Run-Script phase. This issue replaces
> the old "sandbox out of sync" class of failure.

**Causes:**
- The `ComposeApp` XCFramework was never assembled (a fresh checkout / clean build dir)
- A stale XCFramework slice from an earlier `cmp-shared` state
- The Run-Script build phase was disabled or the framework search paths were edited

**Solutions:**

```bash
# 1. Assemble the ComposeApp XCFramework fresh (Debug + Release slices)
./gradlew :cmp-shared:assembleComposeAppXCFramework
#   → cmp-shared/build/XCFrameworks/{debug,release}/ComposeApp.xcframework

# 2. Clean Xcode derived data, then rebuild — the Run-Script phase re-assembles
#    and re-embeds the flavor-matched framework on every build
rm -rf ~/Library/Developer/Xcode/DerivedData/*

# 3. Rebuild (the app is a plain .xcodeproj, no .xcworkspace)
xcodebuild -project cmp-ios/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  clean build
```

**Files:**
- `cmp-ios/Package.swift` (SwiftPM binary target → `ComposeApp.xcframework`)
- `cmp-ios/scripts/embed-xcframework.sh` (the `[KMP] Embed and Sign …` Run-Script phase)

---

### Issue 3: Framework Not Found

**Symptoms:**
```
ld: framework not found 'ComposeApp'
```

**Causes:**
- `ComposeApp` XCFramework not assembled
- Shared module not built
- Framework search paths incorrect

**Solutions:**

```bash
# 1. Build shared module
./gradlew :cmp-shared:assemble

# 2. Assemble the ComposeApp XCFramework (both slices via the umbrella task)
./gradlew :cmp-shared:assembleComposeAppXCFramework

# 3. Clean derived data
rm -rf ~/Library/Developer/Xcode/DerivedData/*

# 4. Clean and rebuild in Xcode
# Product → Clean Build Folder (Cmd+Shift+K)
# Product → Build (Cmd+B)

# 5. Check framework search paths
open cmp-ios/iosApp.xcodeproj
# Target → Build Settings → Framework Search Paths
```

---

### Issue 4: No Such Module 'ComposeApp'

**Symptoms:**
```
No such module 'ComposeApp'
import ComposeApp
```

**Causes:**
- KMP shared module not built
- `ComposeApp` XCFramework not assembled / not embedded
- SwiftPM package or Run-Script embed phase misconfigured

**Solutions:**

```bash
# 1. Build shared module
./gradlew :cmp-shared:assemble

# 2. Assemble the ComposeApp XCFramework
./gradlew :cmp-shared:assembleComposeAppReleaseXCFramework

# 3. Verify the framework is in the build directory (the path Package.swift points at)
ls -la cmp-shared/build/XCFrameworks/release/ComposeApp.xcframework

# 4. In Xcode, re-resolve SwiftPM packages if you consume via Package.swift
# File → Packages → Reset Package Caches, then File → Packages → Resolve Package Versions

# 5. Clean Xcode
rm -rf ~/Library/Developer/Xcode/DerivedData/*

# 6. Rebuild (plain .xcodeproj, no .xcworkspace)
xcodebuild -project cmp-ios/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  clean build
```

---

### Issue 5: Provisioning Profile Doesn't Include Signing Certificate

**Symptoms:**
```
Provisioning profile doesn't include signing certificate
```

**Causes:**
- Certificate-profile mismatch
- Profile expired
- Match certificates out of sync

**Solutions:**

```bash
# 1. Re-fetch Match certificates
bundle exec fastlane match adhoc --force_for_new_devices
bundle exec fastlane match appstore --force_for_new_devices

# 2. Check certificate in Keychain
security find-identity -v -p codesigning

# 3. Verify profile includes certificate
# Xcode → Preferences → Accounts → Download Manual Profiles

# 4. Check Match password file exists
ls -la secrets/apple/match/.match_password

# 5. Verify SSH key for Match repo
ssh -T git@github.com -i secrets/apple/match/match_ci_key

# 6. Run full iOS verification
./scripts/ios/verify_ios_deployment.sh
```

---

### Issue 6: Version Sanitization Error

**Symptoms:**
```
Invalid version format for App Store: 2026.1.1-beta.0.9
```

**Causes:**
- Gradle generates semantic version with pre-release
- App Store only accepts MAJOR.MINOR.PATCH

**Solutions:**

**This is NOT an error** - it's handled automatically by Fastlane.

```bash
# 1. Check version sanitization
./scripts/ios/check_ios_version.sh

# 2. View sanitization logic
cat fastlane/Fastfile | grep -A 20 "sanitize_version_for_testflight"

# 3. Verify version in Info.plist after build
cat cmp-ios/iosApp/Info.plist | grep -A 1 "CFBundleShortVersionString"
```

**How it works:**
- Gradle: `2026.1.1-beta.0.9+abc123` (full semver)
- Firebase: `2026.1.1-beta.0.9` (accepts pre-release)
- App Store: `2026.1.9` (sanitized to YYYY.M.CommitCount)

**See:** [Version Handling Guide](architecture/tree/gradle/version-handling.md)

---

## macOS Build Issues

### Issue 1: macOS Signing Certificate Not Found

**Symptoms:**
```
No certificate found for macOS Developer ID
```

**Causes:**
- Certificate not installed
- Certificate expired
- Wrong certificate type

**Solutions:**

```bash
# 1. Verify macOS setup
scripts/white-label/doctor.sh --verify

# 2. Check certificates in Keychain
security find-identity -v -p codesigning | grep "Developer ID"

# 3. Install certificate from base64
echo "$MACOS_SIGNING_KEY" | base64 -d > cert.p12
security import cert.p12 -P "$MACOS_SIGNING_PASSWORD" -A

# 4. Check GitHub secrets
gh secret list | grep MACOS

# 5. Verify certificate in fastlane-config
cat fastlane-config/macos_config.rb | grep certificate
```

---

## Desktop Build Issues

### Issue 1: Could Not Find or Load Main Class

**Symptoms:**
```
Error: Could not find or load main class MainKt
```

**Causes:**
- Main class not specified correctly
- Packaging issue
- Class path incorrect

**Solutions:**

```bash
# 1. Check main class in build.gradle.kts
grep "mainClass" cmp-desktop/build.gradle.kts

# 2. Clean and rebuild
./gradlew clean
./gradlew :cmp-desktop:packageDebugDistributionForCurrentOS

# 3. Run directly
./gradlew :cmp-desktop:run

# 4. Check compose configuration
cat cmp-desktop/build.gradle.kts | grep -A 20 "compose.desktop"
```

---

### Issue 2: UnsatisfiedLinkError - Native Library

**Symptoms:**
```
java.lang.UnsatisfiedLinkError: Can't load library
```

**Causes:**
- Native library missing for platform
- Compose version doesn't support OS
- Architecture mismatch

**Solutions:**

```bash
# 1. Check Compose version
cat gradle/libs.versions.toml | grep compose

# 2. Update Compose if needed
# Edit gradle/libs.versions.toml
compose = "1.7.0"

# 3. Check OS compatibility
uname -a

# 4. Rebuild for current OS
./gradlew :cmp-desktop:packageDebugDistributionForCurrentOS

# 5. Check architecture (should be x86_64 or arm64)
arch
```

---

## Web Build Issues

### Issue 1: Webpack Compilation Failed

**Symptoms:**
```
Webpack compilation failed
```

**Causes:**
- Webpack configuration issue
- Dependency version conflict
- JavaScript module not found

**Solutions:**

```bash
# 1. Clean Kotlin/JS build
./gradlew clean
./gradlew :cmp-web:jsBrowserDevelopmentWebpack

# 2. Update webpack
./gradlew :cmp-web:kotlinUpgradeYarnLock

# 3. Check webpack configuration
cat cmp-web/webpack.config.d/*.js

# 4. Rebuild for production
./gradlew :cmp-web:jsBrowserDistribution

# 5. Check browser compatibility
cat cmp-web/build.gradle.kts | grep browser
```

---

### Issue 2: JavaScript Module Not Found

**Symptoms:**
```
Module not found: Error: Can't resolve 'moduleName'
```

**Causes:**
- Missing npm dependency
- Incorrect import path
- Webpack alias not configured

**Solutions:**

```bash
# 1. Check Kotlin/JS dependencies
cat cmp-web/build.gradle.kts | grep implementation

# 2. Update yarn lock
./gradlew :cmp-web:kotlinUpgradeYarnLock

# 3. Clean and rebuild
./gradlew clean
./gradlew :cmp-web:jsBrowserDevelopmentWebpack

# 4. Check webpack externals
cat cmp-web/webpack.config.d/*.js
```

---

## Deployment Failures

### Issue 1: Firebase Upload Failed - Groups Parameter Ignored

**Symptoms:**
- Firebase upload succeeds
- Wrong tester group receives build
- Expected testers don't get notification

**Cause:**
🔴 **KNOWN BUG:** Firebase `groups` parameter is ignored by Fastlane

**Details:**
- GitHub Actions pass `--groups` to Fastlane
- Fastlane receives it but doesn't use it
- Uses default group instead

**Workaround:**

```bash
# Option 1: Set environment variable
export FIREBASE_GROUPS="my-custom-group"
bundle exec fastlane ios deploy_on_firebase

# Option 2: Manually specify in lane call
bundle exec fastlane ios deploy_on_firebase groups:"my-group"

# Option 3: Update Fastfile to use parameter
# Edit fastlane/Fastfile line 157-159
firebase_app_distribution(
  groups: options[:groups] || ENV['FIREBASE_GROUPS'] || 'internal-testers'
)
```

**See:** [BUGS_AND_ISSUES.md#1](architecture/tree/deployment/known-issues.md#1-firebase-tester-groups-parameter-ignored-critical)

---

### Issue 2: Play Store Upload - Build Already Exists

**Symptoms:**
```
Version 123 has already been uploaded
```

**Causes:**
- Build number not incremented
- Attempting to re-upload same version

**Solutions:**

```bash
# 1. Fastlane auto-increments, so this shouldn't happen
# If it does, manually increment in build.gradle.kts

# 2. Check current version code
./gradlew printVersionInfo

# 3. Or manually set in cmp-android/build.gradle.kts
android {
    defaultConfig {
        versionCode = 124 // Increment
    }
}

# 4. Rebuild and redeploy
./gradlew clean
bundle exec fastlane android deployInternal
```

---

### Issue 3: TestFlight Upload Stuck "Processing"

**Symptoms:**
- Upload completes
- Build shows "Processing" for hours
- No error message

**Cause:**
This is **normal** - Apple processing takes time (10-60 minutes)

**Solutions:**

```bash
# 1. Wait - this is expected behavior
# Apple processes builds on their servers

# 2. Check status in App Store Connect
# - Go to TestFlight → Builds
# - Check processing status

# 3. Email notification when ready
# You'll receive email when processing completes

# 4. If stuck > 2 hours, contact Apple Support
# Or try re-uploading
bundle exec fastlane ios beta
```

---

### Issue 4: App Store Rejection - Privacy Policy Missing

**Symptoms:**
- App rejected by Apple reviewers
- Reason: Privacy policy not accessible

**Solutions:**

```bash
# 1. Add privacy policy URL to App Store Connect
# - App Store Connect → App Information
# - Privacy Policy URL: https://yoursite.com/privacy

# 2. Ensure URL is publicly accessible
curl -I https://yoursite.com/privacy

# 3. Update Info.plist if needed
# Add NSPrivacyPolicy key

# 4. Resubmit
bundle exec fastlane ios release
```

---

### Issue 5: Keystore Parameters - Naming Inconsistency

**Symptoms:**
- Signing fails with "keystore not found"
- Works with some scripts, fails with others

**Cause:**
🔴 **KNOWN BUG:** Inconsistent keystore parameter naming

**Details:**
- Actions use: `KEYSTORE_FILE` (without ORIGINAL prefix)
- Both should work, but causes confusion

**Workaround:**

```bash
# Set both versions in GitHub secrets
gh secret set KEYSTORE_FILE < keystores/original-release-key.jks.b64
gh secret set UPLOAD_KEYSTORE_FILE < keystores/original-release-key.jks.b64

# Or use keystore-manager which handles both
scripts/white-label/keystore.sh add
```

**See:** [BUGS_AND_ISSUES.md#2](architecture/tree/deployment/known-issues.md#2-signing-parameter-naming-inconsistency-critical)

---

## CI/CD Issues

### Issue 1: GitHub Actions - Secret Not Found

**Symptoms:**
```
Error: Secret FIREBASECREDS not found
```

**Causes:**
- Secret not added to repository
- Secret name typo
- Secret deleted

**Solutions:**

```bash
# 1. List current secrets
gh secret list

# 2. Add missing secrets
scripts/white-label/keystore.sh add

# 3. Or manually encode and add
scripts/white-label/keystore.sh encode-secrets
# Copy output and add via GitHub UI

# 4. Verify secret name matches workflow
cat .github/workflows/*.yml | grep FIREBASECREDS

# 5. Check secret mapping
cat docs/analysis/BUGS_AND_ISSUES.md | grep -A 20 "Secret Name Mapping"
```

**Secret Mapping:**
| File | GitHub Secret Name |
|------|-------------------|
| `firebaseAppDistributionServiceCredentialsFile.json` | `FIREBASECREDS` |
| `google-services.json` | `GOOGLESERVICES` |
| `playStorePublishServiceCredentialsFile.json` | `PLAYSTORECREDS` |

---

### Issue 2: GitHub Actions - Workflow Timeout

**Symptoms:**
- Workflow runs for 6 hours
- Cancelled due to timeout

**Causes:**
- Build hanging
- Slow dependencies download
- Matrix builds taking too long

**Solutions:**

```bash
# 1. Enable Gradle build cache in workflow
# Already enabled via gradle/actions/setup-gradle@v4

# 2. Check if specific job is slow
# View workflow logs to identify bottleneck

# 3. Split large jobs if needed
# Separate build and deploy steps

# 4. Use faster runners if available
# GitHub Actions: macos-latest is slower than ubuntu-latest

# 5. Cache dependencies
# Add caching step in workflow:
- uses: actions/cache@v4
  with:
    path: ~/.gradle/caches
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
```

---

### Issue 3: GitHub Actions - Permission Denied

**Symptoms:**
```
Permission denied (publickey)
```

**Causes:**
- SSH key not configured for Match
- Deploy key not added to repository

**Solutions:**

```bash
# 1. Check Match SSH key exists
ls -la secrets/apple/match/match_ci_key

# 2. View public key
cat secrets/apple/match/match_ci_key.pub

# 3. Add deploy key to Match repository
# - Go to Match repo → Settings → Deploy keys
# - Add key with write access
# - Paste public key

# 4. Test SSH connection
ssh -T git@github.com -i secrets/apple/match/match_ci_key

# 5. Verify in GitHub Actions
# Check MATCH_SSH_PRIVATE_KEY secret is set
gh secret list | grep MATCH
```

---

## Code Signing Problems

### Issue 1: Certificate Expired

**Symptoms:**
```
Certificate has expired
```

**Solutions:**

```bash
# For iOS (Match)
bundle exec fastlane match adhoc --force_for_new_devices
bundle exec fastlane match appstore --force_for_new_devices

# For macOS
# - Generate new certificate in Apple Developer
# - Export as .p12
# - Base64 encode and update GitHub secret
openssl base64 -in cert.p12 -out cert.b64
gh secret set MACOS_SIGNING_KEY < cert.b64
```

---

### Issue 2: Provisioning Profile Invalid

**Symptoms:**
```
Provisioning profile is invalid
```

**Solutions:**

```bash
# 1. Delete profile
rm ~/Library/MobileDevice/Provisioning\ Profiles/*.mobileprovision

# 2. Re-fetch from Match
bundle exec fastlane match adhoc --readonly
bundle exec fastlane match appstore --readonly

# 3. Or download manually from Xcode
# Xcode → Preferences → Accounts → Download Manual Profiles

# 4. Verify profile
security cms -D -i ~/Library/MobileDevice/Provisioning\ Profiles/*.mobileprovision
```

---

## Known Infrastructure Bugs

### 🔴 Critical Bugs

#### 1. Firebase Groups Parameter Ignored
**Impact:** Wrong testers receive builds
**Workaround:** Set `FIREBASE_GROUPS` environment variable
**See:** [BUGS_AND_ISSUES.md#1](architecture/tree/deployment/known-issues.md#1-firebase-tester-groups-parameter-ignored-critical)

#### 2. Signing Parameter Naming Inconsistency
**Impact:** Confusion with keystore parameters
**Workaround:** Set both `KEYSTORE_FILE` and `UPLOAD_KEYSTORE_FILE`
**See:** [BUGS_AND_ISSUES.md#2](architecture/tree/deployment/known-issues.md#2-signing-parameter-naming-inconsistency-critical)

### 🟡 Medium Severity Bugs

#### 3. Hardcoded Keystore Filename
**Impact:** Can't use custom keystore names
**Workaround:** Use standard names (`original-release-key.jks`)
**See:** [BUGS_AND_ISSUES.md#3](architecture/tree/deployment/known-issues.md#3-hardcoded-keystore-filename-medium)

#### 4. Version Generation Silent Failure
**Impact:** May fail without error
**Workaround:** Always verify version generated
**See:** [BUGS_AND_ISSUES.md#4](architecture/tree/deployment/known-issues.md#4-version-generation-may-fail-silently-medium)

#### 5. Production Promotion No Validation
**Impact:** Can promote non-existent beta
**Workaround:** Manually verify beta exists first
**See:** [BUGS_AND_ISSUES.md#5](architecture/tree/deployment/known-issues.md#5-production-promotion-has-no-validation-medium)

---

## Emergency Fixes

### Emergency: Can't Build Anything

```bash
# Nuclear option - reset everything
./gradlew clean
rm -rf build/
rm -rf */build/
rm -rf ~/.gradle/caches
rm -rf ~/Library/Developer/Xcode/DerivedData/*
./gradlew :cmp-shared:assembleComposeAppXCFramework   # re-assemble the iOS ComposeApp XCFramework
./gradlew build
```

### Emergency: Deployment Broken

```bash
# 1. Verify all secrets
scripts/white-label/keystore.sh view

# 2. Re-add all secrets
scripts/white-label/keystore.sh add

# 3. Verify iOS setup
./scripts/ios/verify_ios_deployment.sh

# 4. Check Firebase credentials
ls -la secrets/android/firebaseAppDistributionServiceCredentialsFile.json

# 5. Test local deployment
bundle exec fastlane android deployDemoApkOnFirebase
```

### Emergency: Production is Down

```bash
# 1. Check current production version
# Android: Play Console → Production
# iOS: App Store Connect → App Store

# 2. Rollback via console if needed
# Android: Play Console → Production → Rollback
# iOS: Cannot rollback - submit hotfix

# 3. Hotfix deployment
git checkout -b hotfix/urgent-fix
# Make fix
git commit -m "fix: critical bug"
git push origin hotfix/urgent-fix

# 4. Deploy hotfix
gh workflow run multi-platform-build-and-publish.yml \
  --ref hotfix/urgent-fix \
  -f release_type=beta
```

---

## Debugging Strategies

### Strategy 1: Systematic Elimination

1. **Clean everything** - `./gradlew clean`
2. **Check dependencies** - `./gradlew dependencies`
3. **Verify environment** - `scripts/white-label/doctor.sh --verify`
4. **Rebuild one module** - `./gradlew :module:build`
5. **Check for recent changes** - `git diff HEAD~5`

### Strategy 2: Verbose Logging

```bash
# Android
./gradlew :cmp-android:assembleDebug --stacktrace --info --debug

# iOS
xcodebuild -project cmp-ios/iosApp.xcodeproj \
  -scheme demoDebug \
  -configuration demoDebug \
  build | tee build.log

# Fastlane
bundle exec fastlane ios deploy_on_firebase --verbose

# Desktop
./gradlew :cmp-desktop:run --debug

# Web
./gradlew :cmp-web:jsBrowserDevelopmentWebpack --info
```

### Strategy 3: Bisect

```bash
# Find which commit broke it
git bisect start
git bisect bad HEAD
git bisect good <known-good-commit>
# Test each commit
./gradlew build
git bisect good # or bad
# Repeat until found
```

---

## Additional Resources

- **Known Issues:** [BUGS_AND_ISSUES.md](architecture/tree/deployment/known-issues.md)
- **GitHub Actions:** [.github/CLAUDE.md](../.github/CLAUDE.md)
- **Fastlane:** [fastlane/CLAUDE.md](architecture/tree/deployment/known-issues.md)
- **Scripts:** [scripts/CLAUDE.md](../scripts/CLAUDE.md)
- **Deployment:** [Deployment Playbook](architecture/tree/deployment/playbook.md)
- **Version Handling:** [Version Guide](architecture/tree/gradle/version-handling.md)

---

## Getting Help

**Before asking for help, prepare:**
1. Full error message (copy entire stack trace)
2. Command that failed
3. Platform and OS version
4. Recent changes (last 5 commits)
5. Output of `scripts/white-label/doctor.sh --verify`

**Search first:**
```bash
# Search known issues
grep -r "your error message" docs/analysis/

# Search in guides
grep -r "your error message" docs/claude/

# Check Git history
git log --all --grep="your error message"
```

**Interactive Troubleshooting:**
Use Claude Code skill:
```
/troubleshoot-build
```

---

**Last Updated:** 2026-02-13
**Maintainer:** See CLAUDE.md
