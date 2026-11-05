# Deployment Playbook - Production Deployment Guide

**For:** Anyone deploying to production
**Time:** Reference (varies by platform)
**Last Updated:** 2026-02-13

---

## 📖 Overview

This playbook provides step-by-step deployment procedures for all 5 platforms.

**Deployment Targets:**

| Platform | Targets | Typical Timeline |
|----------|---------|------------------|
| **Android** | Firebase, Play Store (Internal/Beta/Production) | 5-15 minutes |
| **iOS** | Firebase, TestFlight, App Store | 15-60 minutes |
| **macOS** | TestFlight, App Store | 20-60 minutes |
| **Desktop** | GitHub Releases | 10-20 minutes |
| **Web** | GitHub Pages | 2-5 minutes |

---

## 📋 Table of Contents

1. [Pre-Deployment Checklist](#pre-deployment-checklist)
2. [Android Deployment](#android-deployment)
3. [iOS Deployment](#ios-deployment)
4. [macOS Deployment](#macos-deployment)
5. [Desktop Deployment](#desktop-deployment)
6. [Web Deployment](#web-deployment)
7. [Post-Deployment Verification](#post-deployment-verification)
8. [Rollback Procedures](#rollback-procedures)
9. [Production Monitoring](#production-monitoring)

---

## Pre-Deployment Checklist

### Universal Checklist (All Platforms)

- [ ] **All tests passing** - Run `./gradlew test`
- [ ] **Code quality checks pass** - Run `./gradlew spotlessCheck detekt`
- [ ] **No uncommitted changes** - Run `git status`
- [ ] **On correct branch** - `dev` for beta, `main`/`master` for production
- [ ] **Version correct** - Run `./gradlew printVersionInfo`
- [ ] **Changelog updated** - Release notes prepared
- [ ] **Secrets configured** - Run `scripts/white-label/keystore.sh view`
- [ ] **Team notified** - Announce deployment in team chat

### Platform-Specific Checklists

#### Android

- [ ] **Keystores available** - Check `keystores/` directory
- [ ] **google-services.json present** - Check `cmp-android/google-services.json`
- [ ] **Play Console access** - Can login to Play Console
- [ ] **Firebase project configured** - Firebase console accessible

#### iOS

- [ ] **iOS setup complete** - Run `./scripts/ios/verify_ios_deployment.sh`
- [ ] **Match certificates valid** - Run `bundle exec fastlane match adhoc --readonly`
- [ ] **ComposeApp XCFramework assembles** - Run `./gradlew :cmp-shared:assembleComposeAppReleaseXCFramework` (SwiftPM/XCFramework)
- [ ] **App Store Connect access** - Can login to App Store Connect
- [ ] **Screenshots uploaded** (App Store only) - Check App Store Connect
- [ ] **Privacy policy URL set** (App Store only) - Check App Information

#### Desktop

- [ ] **Signing certificates ready** - Check platform-specific certificates
- [ ] **GitHub release permissions** - Can create releases

#### Web

- [ ] **GitHub Pages enabled** - Check repository settings
- [ ] **Custom domain configured** (if applicable) - Check DNS settings

---

## Android Deployment

### Option 1: Firebase App Distribution (Beta Testing)

**Purpose:** Distribute beta builds to internal testers

#### Step-by-Step

**1. Choose Variant**

```bash
# For production variant (prod)
VARIANT="release"

# For demo variant (demo)
VARIANT="demo"
```

**2. Pre-flight Checks**

```bash
# Verify secrets
ls -la secrets/android/firebaseAppDistributionServiceCredentialsFile.json
ls -la cmp-android/google-services.json
ls -la keystores/original-release-key.jks

# Verify Firebase project
cat cmp-android/google-services.json | jq '.project_info.project_id'
```

**3. Deploy**

**Option A: GitHub Actions (Recommended)**

```bash
gh workflow run multi-platform-build-and-publish.yml \
  --ref dev \
  -f release_type=internal \
  -f android_package_name=cmp-android \
  -f ios_package_name=cmp-ios \
  -f desktop_package_name=cmp-desktop \
  -f web_package_name=cmp-web
```

**Option B: Local Fastlane**

```bash
# Production variant
bundle exec fastlane android deployReleaseApkOnFirebase

# Demo variant
bundle exec fastlane android deployDemoApkOnFirebase
```

**4. Verify Upload**

```bash
# Check Firebase Console
# → App Distribution → Releases
# → Verify latest version appears

# Check tester notification
# ⚠️ Known Issue: groups parameter ignored
# Workaround: Set FIREBASE_GROUPS environment variable
```

**Timeline:** 5-10 minutes

---

### Option 2: Play Store (Internal Track)

**Purpose:** Internal testing before public beta

#### Step-by-Step

**1. Pre-flight Checks**

```bash
# Verify Play Store credentials
ls -la secrets/android/playStorePublishServiceCredentialsFile.json

# Verify keystores
scripts/white-label/keystore.sh view

# Check version will increment
./gradlew printVersionInfo
```

**2. Deploy**

**Option A: GitHub Actions (Recommended)**

```bash
gh workflow run multi-platform-build-and-publish.yml \
  --ref dev \
  -f release_type=internal \
  -f android_package_name=cmp-android \
  -f ios_package_name=cmp-ios \
  -f desktop_package_name=cmp-desktop \
  -f web_package_name=cmp-web
```

**Option B: Local Fastlane**

```bash
bundle exec fastlane android deployInternal
```

**3. Verify Upload**

```bash
# Check Play Console
# → Testing → Internal testing
# → Verify latest version appears

# Check rollout percentage
# → Default: 100% to internal testers
```

**Timeline:** 10-15 minutes (includes Play Store processing)

---

### Option 3: Play Store (Beta Track)

**Purpose:** Public beta testing with wider audience

#### Step-by-Step

**1. ⚠️ Deployment Confirmation**

**Audience:** Beta track is visible to opted-in users in Play Console

Ask yourself:
- Is internal testing complete?
- Are all critical bugs fixed?
- Is the build stable enough for wider testing?

**2. Deploy**

**Option A: GitHub Actions**

```bash
gh workflow run multi-platform-build-and-publish.yml \
  --ref dev \
  -f release_type=beta \
  -f android_package_name=cmp-android \
  -f ios_package_name=cmp-ios \
  -f desktop_package_name=cmp-desktop \
  -f web_package_name=cmp-web
```

**Option B: Local Fastlane**

```bash
# Deploy to internal first
bundle exec fastlane android deployInternal

# Then promote to beta
bundle exec fastlane android promoteToBeta
```

**3. Verify**

```bash
# Play Console → Testing → Open testing or Closed testing
# → Verify latest version in beta track
# → Check rollout percentage (default: 100%)
```

**Timeline:** 10-15 minutes

---

### Option 4: Play Store (Production)

**Purpose:** ⚠️ **PRODUCTION RELEASE** to all users

#### Step-by-Step

**1. ⚠️⚠️ PRODUCTION CHECKLIST**

**STOP!** Ensure:
- [ ] Beta testing complete (minimum 7 days recommended)
- [ ] All critical bugs fixed
- [ ] Crash rate < 1% in beta
- [ ] User feedback reviewed
- [ ] Release notes finalized
- [ ] Team notified
- [ ] Rollback plan ready

**2. Double Confirmation Required**

Type `DEPLOY TO PRODUCTION` to confirm.

**3. Deploy**

**Option A: GitHub Actions**

```bash
gh workflow run promote-to-production.yml --ref main
```

**Option B: Local Fastlane**

```bash
# Ensure beta track has the version to promote
bundle exec fastlane android promote_to_production
```

**⚠️ Known Issue:** No validation that beta exists - verify manually first!

**4. Verify**

```bash
# Play Console → Production
# → Verify version promoted
# → Check rollout percentage (default: staged rollout 10% → 100%)
```

**5. Monitor**

```bash
# First 24 hours critical
# → Monitor crash rate
# → Monitor ratings/reviews
# → Monitor support tickets
```

**Timeline:** 5-10 minutes deployment, 1-2 hours Google review, 2-24 hours for phased rollout

---

## iOS Deployment

### Option 1: Firebase App Distribution (Beta Testing)

**Purpose:** Distribute beta builds to testers outside TestFlight

#### Step-by-Step

**1. Pre-flight Checks**

```bash
# Verify iOS setup
./scripts/ios/verify_ios_deployment.sh

# Check Match certificates
bundle exec fastlane match adhoc --readonly

# Check Firebase credentials
ls -la secrets/android/firebaseAppDistributionServiceCredentialsFile.json
ls -la cmp-ios/GoogleService-Info.plist
```

**2. Deploy**

**Option A: GitHub Actions**

```bash
gh workflow run multi-platform-build-and-publish.yml \
  --ref dev \
  -f release_type=internal \
  -f android_package_name=cmp-android \
  -f ios_package_name=cmp-ios \
  -f distribute_ios_firebase=true \
  -f shared_module=cmp-shared \
  -f desktop_package_name=cmp-desktop \
  -f web_package_name=cmp-web
```

**Option B: Local Script**

```bash
./scripts/deploy/deploy_firebase.sh
```

**Option C: Local Fastlane**

```bash
bundle exec fastlane ios deploy_on_firebase
```

**3. Version Handling**

**Important:** Firebase accepts full semantic version:

```
Gradle generates: 2026.1.1-beta.0.9+abc123
Firebase receives: 2026.1.1-beta.0.9 ✅ (pre-release OK)
```

**No sanitization needed** for Firebase!

**4. Verify**

```bash
# Firebase Console → App Distribution → iOS
# → Verify latest version
# → Check tester notifications

# ⚠️ Known Issue: groups parameter ignored (same as Android)
```

**Timeline:** 15-20 minutes

---

### Option 2: TestFlight (Internal/External Testing)

**Purpose:** Apple's official beta testing platform

#### Step-by-Step

**1. Pre-flight Checks**

```bash
# Verify iOS setup
./scripts/ios/verify_ios_deployment.sh

# Check Match certificates (appstore profile needed)
bundle exec fastlane match appstore --readonly

# Assemble the ComposeApp XCFramework (SwiftPM/XCFramework)
./gradlew :cmp-shared:assembleComposeAppReleaseXCFramework

# Check version
./scripts/ios/check_ios_version.sh
```

**2. Understand Version Sanitization**

**Critical:** App Store requires sanitized version

```
Gradle: 2026.1.1-beta.0.9+abc123
↓ (Fastlane auto-sanitizes)
TestFlight: 2026.1.9 (YYYY.M.CommitCount)
```

**Why?** App Store only accepts `MAJOR.MINOR.PATCH` format.

**See:** [Version Handling Guide](../gradle/version-handling.md)

**3. Deploy**

**Option A: GitHub Actions**

```bash
gh workflow run multi-platform-build-and-publish.yml \
  --ref dev \
  -f release_type=beta \
  -f android_package_name=cmp-android \
  -f ios_package_name=cmp-ios \
  -f distribute_ios_testflight=true \
  -f shared_module=cmp-shared \
  -f desktop_package_name=cmp-desktop \
  -f web_package_name=cmp-web
```

**Option B: Local Script**

```bash
./scripts/deploy/deploy_testflight.sh
```

**Option C: Local Fastlane**

```bash
bundle exec fastlane ios beta
```

**4. Wait for Apple Processing**

⏳ **Apple processes builds: 10-60 minutes**

You'll receive email when ready.

**5. Verify**

```bash
# App Store Connect → TestFlight → iOS Builds
# → Verify version appears
# → Check build status

# Statuses:
# - Processing: Wait (normal)
# - Missing Compliance: Add export compliance info
# - Ready to Submit: For external testing
# - Ready to Test: Available to testers
```

**6. Invite Testers**

```bash
# App Store Connect → TestFlight → Testers
# → Internal Testing: Available immediately
# → External Testing: Requires beta review (<24 hours)

# Add testers to groups
# They receive email invitation
```

**Timeline:** 15-30 minutes upload + 10-60 minutes processing

---

### Option 3: App Store (Production)

**Purpose:** ⚠️⚠️ **PRODUCTION RELEASE** to all App Store users

#### Step-by-Step

**1. ⚠️⚠️ PRODUCTION CHECKLIST**

**STOP!** Ensure:
- [ ] TestFlight testing complete (minimum 7 days recommended)
- [ ] All critical bugs fixed
- [ ] Crash rate < 0.5% in TestFlight
- [ ] User feedback reviewed
- [ ] Screenshots uploaded to App Store Connect
- [ ] App description updated
- [ ] Privacy policy URL accessible
- [ ] Review notes prepared for Apple
- [ ] Demo account ready (if app requires login)
- [ ] Release notes finalized
- [ ] Team notified
- [ ] **On main/master branch**

**2. Triple Confirmation Required**

Type `DEPLOY TO APP STORE FOR PRODUCTION` to confirm.

**3. Pre-flight Checks**

```bash
# Must be on production branch
BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [[ "$BRANCH" != "main" && "$BRANCH" != "master" ]]; then
  echo "❌ ERROR: Must be on main or master branch"
  exit 1
fi

# All tests passing
./gradlew test

# iOS verification
./scripts/ios/verify_ios_deployment.sh

# Version check
./scripts/ios/check_ios_version.sh
```

**4. Deploy**

**Option A: GitHub Actions**

```bash
gh workflow run multi-platform-build-and-publish.yml \
  --ref main \
  -f release_type=beta \
  -f android_package_name=cmp-android \
  -f ios_package_name=cmp-ios \
  -f distribute_ios_appstore=true \
  -f shared_module=cmp-shared \
  -f desktop_package_name=cmp-desktop \
  -f web_package_name=cmp-web
```

**Option B: Local Script**

```bash
./scripts/deploy/deploy_appstore.sh
```

**Option C: Local Fastlane**

```bash
bundle exec fastlane ios release
```

**5. What Happens**

Fastlane:
1. Builds signed IPA with App Store profile
2. Generates release notes from conventional commits
3. Writes to `fastlane/metadata/en-US/release_notes.txt`
4. Uploads to App Store
5. **Submits for review automatically** (configured in Fastfile)

**6. Apple Review Process**

```
Submitted → Waiting for Review → In Review → Processing → Ready for Sale
  (instant)    (1-7 days typically)  (1-2 hrs)   (1-2 hrs)      (live)
```

**7. Monitor Review**

```bash
# App Store Connect → App Store → iOS App
# → Check status

# Respond quickly to any questions from Apple Review team
# → Resolution Center in App Store Connect
```

**8. After Approval**

```bash
# Status: Ready for Sale
# → App live on App Store (may take 1-2 hours to propagate)

# Monitor:
# → App Analytics (downloads, retention, crashes)
# → Customer Reviews
# → Crash reports
```

**Common Rejection Reasons:**
- Missing or inaccessible privacy policy
- Insufficient demo account
- Crash on launch
- Guideline violations (check App Store Review Guidelines)
- Metadata doesn't match functionality

**Timeline:** 15-30 minutes upload + 1-7 days review + 2-24 hours release propagation

---

## macOS Deployment

### Option 1: TestFlight (Beta Testing)

**Purpose:** Beta testing for macOS app

#### Step-by-Step

**1. Pre-flight Checks**

```bash
# Verify macOS setup
./scripts/verify_macos_setup.sh

# Check certificates
security find-identity -v -p codesigning | grep "Developer ID"

# Check secrets
ls -la secrets/desktop/macos/*.p12
```

**2. Deploy**

**Option A: GitHub Actions**

```bash
gh workflow run multi-platform-build-and-publish.yml \
  --ref dev \
  -f release_type=beta \
  -f android_package_name=cmp-android \
  -f ios_package_name=cmp-ios \
  -f desktop_package_name=cmp-desktop \
  -f web_package_name=cmp-web
```

**Option B: Local Fastlane**

```bash
bundle exec fastlane macos beta
```

**3. Processing**

Similar to iOS, Apple processes macOS builds (10-60 minutes)

**Timeline:** 20-30 minutes upload + 10-60 minutes processing

---

### Option 2: App Store (Production)

**Purpose:** ⚠️ **PRODUCTION** release for macOS

**Process:** Similar to iOS App Store (see above)

- Requires all iOS checklist items
- macOS-specific: Ensure app is notarized
- Review time: 1-7 days typically

**Timeline:** 20-30 minutes upload + 1-7 days review

---

## Desktop Deployment

### GitHub Releases (Windows/macOS/Linux)

**Purpose:** Distribute desktop binaries to users

#### Step-by-Step

**1. Pre-flight Checks**

```bash
# Verify signing certificates (if configured)
ls -la secrets/windows_*.p12
ls -la secrets/desktop/macos/*.p12
ls -la secrets/linux_*.key

# Test build locally
./gradlew :cmp-desktop:packageDebugDistributionForCurrentOS
```

**2. Deploy**

**Option A: GitHub Actions (Recommended)**

```bash
gh workflow run multi-platform-build-and-publish.yml \
  --ref dev \
  -f release_type=internal \
  -f android_package_name=cmp-android \
  -f ios_package_name=cmp-ios \
  -f desktop_package_name=cmp-desktop \
  -f web_package_name=cmp-web
```

**What it does:**
- Builds on matrix: `[ubuntu-latest, windows-latest, macos-latest]`
- Packages platform-specific installers
- Creates GitHub pre-release
- Attaches all artifacts

**3. Verify**

```bash
# Check GitHub Releases
gh release list

# Download and test each platform
gh release download <tag> --pattern "*.exe"
gh release download <tag> --pattern "*.dmg"
gh release download <tag> --pattern "*.deb"
```

**4. Publish Release**

```bash
# Edit pre-release to full release
gh release edit <tag> --draft=false --prerelease=false

# Or via GitHub UI
# → Releases → Edit → Uncheck "Pre-release" → Publish release
```

**Timeline:** 10-20 minutes (parallel matrix builds)

---

## Web Deployment

### GitHub Pages

**Purpose:** Host web app on GitHub Pages

#### Step-by-Step

**1. Pre-flight Checks**

```bash
# Verify GitHub Pages enabled
# Repository → Settings → Pages → Source: gh-pages branch

# Test build locally
./gradlew :cmp-web:jsBrowserDistribution
```

**2. Deploy**

**Option A: GitHub Actions**

```bash
gh workflow run multi-platform-build-and-publish.yml \
  --ref dev \
  -f release_type=internal \
  -f android_package_name=cmp-android \
  -f ios_package_name=cmp-ios \
  -f desktop_package_name=cmp-desktop \
  -f web_package_name=cmp-web
```

**Option B: Local Script**

```bash
# Build
./gradlew :cmp-web:jsBrowserDistribution

# Deploy to gh-pages branch
cd cmp-web/build/distributions
git init
git checkout -b gh-pages
git add .
git commit -m "Deploy web app"
git remote add origin <repo-url>
git push origin gh-pages --force
```

**3. Verify**

```bash
# Check GitHub Pages URL (typically):
# https://<username>.github.io/<repository>/

# Or custom domain if configured
```

**4. Custom Domain (Optional)**

```bash
# Add CNAME file to cmp-web/src/main/resources/
echo "yourdomain.com" > cmp-web/src/main/resources/CNAME

# Configure DNS:
# CNAME record: www.yourdomain.com → <username>.github.io
```

**Timeline:** 2-5 minutes

---

## Post-Deployment Verification

### Verification Checklist

**Within 1 hour of deployment:**

- [ ] **Build appears in distribution platform**
  - Firebase: Check App Distribution console
  - Play Store: Check testing track
  - TestFlight: Check iOS Builds
  - GitHub: Check Releases
  - GitHub Pages: Visit web URL

- [ ] **Version number correct**
  - Android: Check Play Console version code
  - iOS: Check TestFlight build number
  - Desktop: Check GitHub release tag
  - Web: Check deployed version (footer or console log)

- [ ] **Tester notifications sent** (if applicable)
  - Firebase: Check tester emails
  - TestFlight: Check tester invitations

- [ ] **Install and launch test**
  - Download from distribution platform
  - Install on test device
  - Launch app - verify no crash
  - Test critical features

- [ ] **Monitoring setup**
  - Firebase: Crashlytics enabled
  - Play Console: Pre-launch reports
  - App Store Connect: Crash reports
  - GitHub: Issue tracker

**Within 24 hours:**

- [ ] **Crash rate < 1%** (beta) or **< 0.5%** (production)
- [ ] **No critical bugs reported**
- [ ] **User feedback reviewed**

---

## Rollback Procedures

### Android Play Store

**Option 1: Halt Rollout**

```bash
# Play Console → Production → Halt rollout
# → Stops phased rollout at current percentage
```

**Option 2: Rollback to Previous Version**

```bash
# Play Console → Production → Rollback
# → Select previous version
# → Confirm rollback

# Or via Fastlane (if custom action exists)
bundle exec fastlane android rollback_production version_code:<previous>
```

**⚠️ Limitation:** Cannot rollback if >50% of users have the version

### iOS App Store

**No direct rollback available!**

**Mitigation:**
```bash
# 1. Submit hotfix immediately
git checkout -b hotfix/critical-fix
# Make fix
git commit -m "fix: critical production bug"
git push

# 2. Expedited review request
# App Store Connect → Resolution Center
# → Request expedited review (only for critical issues)

# 3. Deploy hotfix
gh workflow run multi-platform-build-and-publish.yml \
  --ref hotfix/critical-fix \
  -f release_type=beta \
  -f distribute_ios_appstore=true

# Typical expedited review: 2-48 hours
```

### Desktop GitHub Releases

```bash
# 1. Delete problematic release
gh release delete <tag> --yes

# 2. Re-create from previous commit
git checkout <previous-good-commit>
gh workflow run multi-platform-build-and-publish.yml --ref <commit-sha>
```

### Web GitHub Pages

```bash
# Revert gh-pages branch to previous commit
git checkout gh-pages
git reset --hard <previous-commit>
git push origin gh-pages --force

# Or redeploy previous version
git checkout <previous-commit>
./gradlew :cmp-web:jsBrowserDistribution
# Deploy (see Web Deployment section)
```

---

## Production Monitoring

### Key Metrics to Monitor

#### Android (Play Console)

```bash
# Access: Play Console → Quality → Android vitals

# Monitor:
- Crash rate (target: <0.5%)
- ANR rate (target: <0.1%)
- User ratings (target: ≥4.0)
- User reviews (respond within 24 hours)
- Pre-launch reports (before releases)
```

#### iOS (App Store Connect)

```bash
# Access: App Store Connect → Analytics

# Monitor:
- Crash rate (target: <0.3%)
- App crashes (Xcode → Organizer → Crashes)
- User ratings (target: ≥4.0)
- User reviews (respond within 24 hours)
- Downloads and retention
```

#### Desktop (GitHub)

```bash
# Monitor:
- Download counts (GitHub Insights → Traffic)
- Issue reports (GitHub Issues)
- Release adoption rate
```

#### Web (Analytics)

```bash
# If analytics configured (Google Analytics, etc.)
- Page views
- User retention
- Error tracking (e.g., Sentry)
```

### Alerting Setup

**Firebase Crashlytics:**
```bash
# Configure email alerts for crash rate spikes
# Firebase Console → Crashlytics → Settings → Alerts
```

**Play Console:**
```bash
# Enable email notifications
# Play Console → Settings → Email preferences
# → Check: "New reviews", "Android vitals alerts"
```

**App Store Connect:**
```bash
# Enable notifications
# App Store Connect → Users and Access → Your Account
# → Notifications → Enable relevant alerts
```

---

## Emergency Procedures

### Critical Bug in Production

**Immediate Actions:**

1. **Assess Impact**
   ```bash
   # Check crash reports
   # Estimate affected user percentage
   # Determine severity (data loss? security? UX?)
   ```

2. **Notify Team**
   ```bash
   # Post in team chat
   # "🚨 PRODUCTION ISSUE: <brief description>"
   ```

3. **Triage**
   ```bash
   # Can wait for next release? → Document and fix normally
   # Needs hotfix? → Proceed to step 4
   ```

4. **Hotfix**
   ```bash
   # Create hotfix branch
   git checkout -b hotfix/critical-<description>

   # Make minimal fix
   # Test thoroughly
   # Commit with clear message
   git commit -m "fix: critical production bug - <description>"

   # Deploy following standard procedures
   # Request expedited review if needed (iOS)
   ```

5. **Post-Mortem**
   ```bash
   # After resolution, document:
   # - What happened
   # - Root cause
   # - Why it wasn't caught
   # - Preventive measures
   ```

---

## Additional Resources

- **Known Issues:** [BUGS_AND_ISSUES.md](known-issues.md)
- **Version Handling:** [version-handling.md](../gradle/version-handling.md)
- **Secrets Management:** [secrets-management.md](../secrets/management.md)
- **GitHub Actions:** [.github/CLAUDE.md](../../../../.github/CLAUDE.md)
- **Fastlane:** [fastlane/CLAUDE.md](known-issues.md)
- **Troubleshooting:** [troubleshooting.md](../../../troubleshooting.md)

---

## Deployment Scripts Quick Reference

| Task | Command |
|------|---------|
| **Android Firebase** | `bundle exec fastlane android deployReleaseApkOnFirebase` |
| **Android Play Internal** | `bundle exec fastlane android deployInternal` |
| **Android Play Beta** | `bundle exec fastlane android promoteToBeta` |
| **Android Play Production** | `bundle exec fastlane android promote_to_production` |
| **iOS Firebase** | `bundle exec fastlane ios deploy_on_firebase` |
| **iOS TestFlight** | `bundle exec fastlane ios beta` |
| **iOS App Store** | `bundle exec fastlane ios release` |
| **macOS TestFlight** | `bundle exec fastlane macos beta` |
| **macOS App Store** | `bundle exec fastlane macos release` |
| **All Platforms** | `gh workflow run multi-platform-build-and-publish.yml` |

---

**Last Updated:** 2026-02-13
**Maintainer:** See CLAUDE.md
