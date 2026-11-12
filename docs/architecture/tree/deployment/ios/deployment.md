# iOS Deployment Guide

Complete guide to deploying your iOS app to Firebase, TestFlight, and the App Store.

## Table of Contents

- [Deployment Overview](#deployment-overview)
- [Firebase App Distribution](#firebase-app-distribution)
- [TestFlight Beta Testing](#testflight-beta-testing)
- [App Store Production](#app-store-production)
- [Workflows & Best Practices](#workflows--best-practices)
- [Troubleshooting](#troubleshooting)

## Deployment Overview

This project supports three iOS deployment targets:

| Target | Purpose | Review Time | Script |
|--------|---------|-------------|--------|
| **Firebase** | Internal testing, QA | None | `bash scripts/deploy/deploy_firebase.sh` |
| **TestFlight** | Beta testing (up to 10,000 testers) | 24-48 hours | `bash scripts/deploy/deploy_testflight.sh` |
| **App Store** | Production release to public | 24-72 hours | `bash scripts/deploy/deploy_appstore.sh` |

## Firebase App Distribution

Firebase is ideal for internal testing before submitting to Apple.

### When to Use Firebase
- ✅ Internal testing within your team
- ✅ QA and bug testing
- ✅ Rapid iteration cycles
- ✅ No Apple review required
- ✅ Immediate distribution

### Deploy to Firebase

```bash
bash scripts/deploy/deploy_firebase.sh
```

### What Happens
1. Validates configuration and prerequisites
2. Syncs code signing certificates via Match
3. Builds signed IPA with AdHoc provisioning
4. Uploads to Firebase App Distribution
5. Notifies tester groups

### Tester Setup
Testers receive an email with download instructions. They need:
- iOS device with UDID registered in your Apple Developer account
- Firebase App Tester app (or direct download link)

**Add Tester UDIDs:**
1. Get UDID from tester (iTunes, Xcode, or https://www.udid.io/)
2. Add to Apple Developer Portal → Devices
3. Regenerate provisioning profiles:
   ```bash
   bundle exec fastlane ios sync_certificates match_type:adhoc
   ```

## TestFlight Beta Testing

TestFlight allows beta testing with up to 10,000 external testers (10,000 internal testers).

### When to Use TestFlight
- ✅ Public beta testing
- ✅ Larger tester groups
- ✅ Pre-production testing
- ✅ Collecting feedback before App Store release
- ✅ Testing App Store features (in-app purchases, etc.)

### Deploy to TestFlight

```bash
bash scripts/deploy/deploy_testflight.sh
```

### What Happens
1. Validates configuration (requires App Store Connect API key)
2. Syncs App Store code signing certificates
3. Increments version and build number
4. Builds signed IPA with App Store provisioning
5. Uploads to App Store Connect
6. Submits for Beta App Review

### Beta Review Process

**Timeline:**
1. Upload → **Processing** (10-30 minutes)
2. Processing → **Waiting for Review** (hours to days)
3. **In Review** by Apple (few hours)
4. **Approved** or **Rejected**

**What Apple Reviews:**
- Basic functionality
- Compliance with App Store guidelines
- No obvious crashes or bugs

**Review typically takes 24-48 hours.**

### Managing Testers

#### Internal Testers (No Review Required)
- Up to 100 internal testers
- Add in App Store Connect → TestFlight → Internal Testing
- Builds available immediately after processing

#### External Testers (Review Required)
- Up to 10,000 external testers
- Create groups in App Store Connect → TestFlight → External Testing
- Requires Beta App Review (24-48 hours)
- Configure groups in `gradle/fork.properties`:
  ```properties
  apple.tf.groups=beta-testers,external-testers
  ```

### Beta App Review Requirements

Configured in `fastlane-config/project_config.rb` → `IOS_SHARED[:testflight]`:

```ruby
beta_app_review_info: {
  contact_email: "team@example.com",
  contact_first_name: "Your",
  contact_last_name: "Name",
  contact_phone: "+1234567890",
  demo_account_name: "",        # If app requires login
  demo_account_password: "",    # If app requires login
  notes: "Instructions for reviewers..."
}
```

## App Store Production

Deploy to the App Store for public release.

### When to Use App Store
- ✅ Public production release
- ✅ Stable, thoroughly tested builds
- ✅ Ready for worldwide distribution

### Deploy to App Store

```bash
bash scripts/deploy/deploy_appstore.sh
```

**With options:**
```bash
# Don't auto-submit for review (manual submit later)
bash scripts/deploy/deploy_appstore.sh --submit-for-review=false

# Manual release after approval (no auto-release)
bash scripts/deploy/deploy_appstore.sh --automatic-release=false
```

### What Happens
1. Double confirmation prompt (production deployment!)
2. Validates configuration
3. Syncs App Store certificates
4. Increments version and build number
5. Updates Info.plist with privacy descriptions
6. Builds signed IPA
7. Uploads to App Store Connect
8. Submits for App Store Review (if enabled)

### App Store Review Process

**Timeline:**
1. Upload → **Processing** (10-30 minutes)
2. **Ready for Sale** (if already approved version)
3. OR **Waiting for Review** (hours to 2 weeks, typically 24-72 hours)
4. **In Review** (few hours to 1 day)
5. **Approved** → **Pending Developer Release** or **Ready for Sale**
6. OR **Rejected** → Fix issues and resubmit

### Before Submitting

**Pre-Flight Checklist:**
- ✅ Thoroughly test on physical devices
- ✅ Test on multiple iOS versions
- ✅ Verify all features work correctly
- ✅ Check for crashes and bugs
- ✅ Test in-app purchases (if applicable)
- ✅ Verify push notifications work (if applicable)
- ✅ Prepare marketing materials in App Store Connect:
  - App description (all locales)
  - Screenshots (all required sizes)
  - App icon
  - Keywords
  - Privacy policy URL
  - Support URL
  - Age rating

### App Review Information

Configured in `fastlane-config/project_config.rb` → `IOS_SHARED[:appstore]`:

```ruby
app_review_information: {
  first_name: "Your",
  last_name: "Name",
  phone_number: "+1234567890",
  email_address: "review@example.com",
  demo_user: "",              # If app requires login
  demo_password: "",          # If app requires login
  notes: "Instructions for reviewers..."
}
```

### Common Rejection Reasons

1. **2.1 - App Completeness**
   - App crashes on launch
   - Features don't work as described
   - Missing content

2. **2.3 - Accurate Metadata**
   - Screenshots don't match app
   - Description misleading

3. **4.0 - Design**
   - Poor user interface
   - Confusing navigation

4. **5.1 - Privacy**
   - Missing privacy policy
   - Privacy violations

**Read:** [App Store Review Guidelines](https://developer.apple.com/app-store/review/guidelines/)

## Workflows & Best Practices

### Development Workflow

```
┌─────────────┐
│   Develop   │
│    Code     │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Deploy    │
│  Firebase   │  ← Internal testing, QA
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Deploy    │
│ TestFlight  │  ← Beta testing with external users
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Deploy    │
│  App Store  │  ← Production release
└─────────────┘
```

### Version Numbering

**Automatic versioning:**
- Version number set in `fastlane-config/project_config.rb` → `IOS[:version_number]`
- Build number automatically incremented from latest TestFlight build
- Format: `1.0.0 (123)` where `123` is build number

**Manual version bump:**
1. Update `version_number` in `project_config.rb`
2. Or pass version at runtime:
   ```bash
   bundle exec fastlane ios beta version_number:1.1.0
   ```

### Release Notes

Release notes are automatically generated from git commits:
- Uses commits since last tag
- Format: `- [commit message]`
- Customize in `fastlane/Fastfile` → `generateReleaseNote()`

**Best practice:** Use conventional commits:
```
feat: Add user profile screen
fix: Fix crash on startup
docs: Update README
```

### Staged Rollout (Phased Release)

Enable in `fastlane-config/project_config.rb` → `IOS_SHARED[:appstore]`:

```ruby
phased_release: true
```

**What it does:**
- Releases to 1% of users on day 1
- Gradually increases to 100% over 7 days
- Allows catching issues before full rollout
- Can pause/resume in App Store Connect

### Hot Fixes

For critical production bugs:

1. **Create hotfix branch:**
   ```bash
   git checkout -b hotfix/1.0.1 main
   ```

2. **Fix the bug and test thoroughly**

3. **Deploy to TestFlight for quick verification:**
   ```bash
   bash scripts/deploy/deploy_testflight.sh
   ```

4. **Once verified, deploy to App Store:**
   ```bash
   bash scripts/deploy/deploy_appstore.sh
   ```

5. **Expedited Review:**
   - In App Store Connect, request "Expedited Review"
   - Explain the critical bug being fixed
   - Typically reviewed in 24 hours (vs 24-72 hours)

## Troubleshooting

### Deployment Failures

#### Issue: "Match password incorrect"

**Solution:**
```bash
cat secrets/apple/match/.match_password  # Verify password
# If lost, regenerate certificates (nuclear option)
```

#### Issue: "No matching provisioning profiles found"

**Solution:**
```bash
# Regenerate profiles
bundle exec fastlane ios sync_certificates match_type:appstore
```

#### Issue: "Build number already exists"

**Cause:** Build number conflict with existing build in App Store Connect.

**Solution:**
Fastlane automatically increments, but if there's a conflict:
```bash
# Manually set build number
bundle exec fastlane ios beta build_number:124
```

#### Issue: "App Store Connect API authentication failed"

**Solution:**
1. Verify `secrets/apple/appstore/AuthKey.p8` exists and is valid
2. Check `secrets/apple/appstore/key_id` and `secrets/apple/appstore/issuer_id` exist and contain the correct values
3. Regenerate API key if revoked

#### Issue: "Certificate not trusted"

**Cause:** Match certificates not installed or expired.

**Solution:**
```bash
# Re-sync certificates
bundle exec fastlane ios sync_certificates match_type:adhoc
bundle exec fastlane ios sync_certificates match_type:appstore
```

### TestFlight Issues

#### Issue: "Build stuck in 'Processing'"

**Wait time:** Processing typically takes 10-30 minutes but can take up to 2 hours.

**If stuck for >2 hours:**
1. Check App Store Connect for error messages
2. Re-upload the build

#### Issue: "Missing Beta App Review Information"

**Solution:**
Update `gradle/fork.properties` with complete contact information:
```properties
org.email=team@example.com
org.first.name=Your
org.last.name=Name
org.phone=+1234567890
```

#### Issue: "Beta review rejected"

**Common reasons:**
- App crashes on launch
- Features don't work
- Sign-in issues (provide demo account)

**Solution:**
1. Read rejection message in App Store Connect
2. Fix the issues
3. Redeploy to TestFlight

### App Store Issues

#### Issue: "App review rejected"

**Steps:**
1. Read detailed rejection in Resolution Center
2. Fix issues in your app
3. Respond in Resolution Center if you disagree
4. Resubmit for review

#### Issue: "Metadata rejected"

**Common reasons:**
- Misleading description
- Screenshots don't match app
- Inappropriate content

**Solution:**
1. Update metadata in App Store Connect
2. Resubmit for review (no new build needed)

#### Issue: "Binary upload failed"

**Solution:**
1. Check Xcode organizer for detailed error
2. Verify code signing settings
3. Check for invalid entitlements

## Monitoring & Analytics

### App Store Connect Analytics

View in App Store Connect → Analytics:
- Downloads and installations
- Sessions and active devices
- Crashes and metrics
- Retention and engagement

### Crash Reporting

Enable in your app:
- Firebase Crashlytics
- Apple Crash Reports (App Store Connect)

Monitor regularly and fix critical crashes.

### TestFlight Feedback

Testers can provide feedback directly in TestFlight app:
- Screenshots of issues
- Device and iOS version info
- App version and build number

View feedback in App Store Connect → TestFlight → Feedback.

## CI/CD Integration

Add secrets to your CI/CD environment:

**GitHub Actions:**
```yaml
secrets:
  MATCH_PASSWORD: ${{ secrets.MATCH_PASSWORD }}
  APPSTORE_KEY_ID: ${{ secrets.APPSTORE_KEY_ID }}
  APPSTORE_ISSUER_ID: ${{ secrets.APPSTORE_ISSUER_ID }}
  TEAM_ID: ${{ secrets.TEAM_ID }}
```

**Deployment from CI:**
```bash
# Load secrets
export MATCH_PASSWORD="${MATCH_PASSWORD}"
export TEAM_ID="${TEAM_ID}"

# Deploy
bash scripts/deploy/deploy_testflight.sh
```

## Support Resources

- **Apple Developer Support:** https://developer.apple.com/support/
- **App Store Connect Help:** https://help.apple.com/app-store-connect/
- **Fastlane Documentation:** https://docs.fastlane.tools/
- **TestFlight Documentation:** https://developer.apple.com/testflight/
- **App Store Review Guidelines:** https://developer.apple.com/app-store/review/guidelines/

## Quick Reference

```bash
# Deployment
bash scripts/deploy/deploy_firebase.sh                      # Firebase (internal testing)
bash scripts/deploy/deploy_testflight.sh                    # TestFlight (beta testing)
bash scripts/deploy/deploy_appstore.sh                      # App Store (production)

# Certificate Management
bundle exec fastlane ios sync_certificates match_type:adhoc
bundle exec fastlane ios sync_certificates match_type:appstore

# Manual Lanes
bundle exec fastlane ios build_ios                    # Build unsigned
bundle exec fastlane ios build_signed_ios             # Build signed
bundle exec fastlane ios deploy_on_firebase           # Firebase only
bundle exec fastlane ios beta                         # TestFlight only
bundle exec fastlane ios release                      # App Store only

# Debugging
bundle exec fastlane ios --help                       # Show all lanes
cat gradle/fork.properties                                   # View non-secret config
ls secrets/apple/ secrets/apple/                               # List secret files
open https://appstoreconnect.apple.com                # Open App Store Connect
```

---

**Questions?** See [IOS_SETUP.md](setup.md) for initial setup instructions.
