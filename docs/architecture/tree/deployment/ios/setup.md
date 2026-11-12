# iOS Setup Guide

Complete guide to setting up iOS deployment for your Kotlin Multiplatform project.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Detailed Setup](#detailed-setup)
- [Configuration Architecture](#configuration-architecture)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### Required

- **macOS** with Xcode installed
- **Apple Developer Account** ($99/year)
  - Enroll at: https://developer.apple.com/programs/
- **Git** for code signing certificate storage
- **Ruby** and **Bundler** for Fastlane

### Optional but Recommended

- **GitHub CLI** (`gh`) for easier repository management
- **Firebase account** for app distribution

## Quick Start

Run the comprehensive iOS setup wizard:

```bash
bash scripts/ios/setup_ios_complete.sh
```

This interactive wizard will guide you through:
1. ✅ Team ID configuration
2. ✅ App Store Connect API key setup
3. ✅ Fastlane Match repository configuration
4. ✅ SSH key generation for Match
5. ✅ Certificate synchronization
6. ✅ TestFlight & App Store review contact information

## Detailed Setup

### Step 1: Get Your Team ID

1. Log in to [Apple Developer Portal](https://developer.apple.com/account)
2. Go to **Membership** section
3. Copy your **Team ID** (10 characters, e.g., `L432S2FZP5`)

### Step 2: Create App Store Connect API Key

This allows Fastlane to upload builds and manage TestFlight/App Store submissions automatically.

#### Steps:
1. Log in to [App Store Connect](https://appstoreconnect.apple.com/)
2. Go to **Users and Access** → **Keys** tab
3. Click **+** to generate a new key
4. Enter name: `Fastlane Deploy Key`
5. Select role: **App Manager** or **Admin**
6. Click **Generate**
7. **Download the .p8 key file** (you can only do this once!)
8. Note the **Key ID** (10 characters)
9. Note the **Issuer ID** (UUID format)

**Important:** Store the .p8 file securely! You cannot download it again.

### Step 3: Set Up Match Repository

Fastlane Match stores your iOS certificates and provisioning profiles in a Git repository, allowing sharing across team members and CI/CD.

#### Create Match Repository:

**Option A: Using GitHub CLI (recommended)**
```bash
gh repo create ios-certificates --private
```

**Option B: Manually**
1. Create a new **private** repository on GitHub/GitLab/Bitbucket
2. Name it: `ios-certificates` or `ios-provisioning-profile`
3. Keep it empty (no README)

#### Generate SSH Key for Match:
```bash
# The setup wizard does this automatically, or manually:
ssh-keygen -t ed25519 -C "fastlane-match" -f secrets/apple/match/match_ci_key -N ""
```

#### Add Deploy Key to Repository:
1. Go to your Match repository on GitHub
2. **Settings** → **Deploy keys**
3. Click **Add deploy key**
4. Title: `Fastlane Match CI`
5. Paste the public key from `secrets/apple/match/match_ci_key.pub`
6. ✅ Check **Allow write access** (Match needs to push certificates)
7. Click **Add key**

### Step 4: Generate Match Password

Match encrypts your certificates with a password:

```bash
# Generate secure password
openssl rand -base64 32 > secrets/apple/match/.match_password
```

**Important:** Store this password in your password manager! You'll need it on all machines and in CI/CD.

### Step 5: Run Setup Wizard

Now that you have all the prerequisites, run the setup wizard:

```bash
bash scripts/ios/setup_ios_complete.sh
```

The wizard will:
- Collect all required information
- Populate `gradle/fork.properties` with non-secret identity/metadata
- Write secret values as per-value files under `secrets/<platform>/`
- Set up SSH keys for Match
- Initialize Match (sync certificates)
- Validate configuration

## Configuration Architecture

This project uses a **shared vs app-specific** configuration pattern:

### Shared Configuration (IOS_SHARED)

Located in `fastlane-config/project_config.rb`. Values are resolved from
`ENV → secrets/<platform>/file → gradle/fork.properties → default`.

**Same for ALL apps:**
- Team ID (non-secret: `gradle/fork.properties` → `apple.team.id`)
- App Store Connect API credentials (secret: `secrets/apple/appstore/key_id`, `issuer_id`, `AuthKey.p8`)
- Match repository URL/branch (non-secret: `gradle/fork.properties` → `apple.match.git.url/branch`)
- TestFlight & App Store review contact information (non-secret: `gradle/fork.properties` → `org.*`)

**Why shared?** When you create multiple apps from this template, they all use the same Apple Developer account and infrastructure.

### App-Specific Configuration (IOS)

Located in `fastlane-config/project_config.rb`:

**Changes per app:**
- Bundle identifier (e.g., `com.example.myapp`)
- Firebase App ID
- App version and build number
- Project paths

**Why separate?** Each app you create from the template has a unique bundle ID and Firebase configuration.

### How scripts/white-label/fork-init.sh Works

When you run `scripts/white-label/fork-init.sh` with a new package name:
- ✅ Updates `IOS[:app_identifier]` to your new bundle ID
- ✅ Updates Firebase app ID
- ✅ **Preserves `IOS_SHARED` completely** (shared infrastructure)
- ✅ Updates Xcode `project.pbxproj` with new bundle ID

## Files Created

After setup, these files will exist:

```
gradle/
└── fork.properties              # Non-secret identity/metadata (committed if desired)

secrets/                         # All gitignored
├── apple/
│   ├── appstore/AuthKey.p8      # App Store Connect API key
│   ├── appstore/key_id          # ASC Key ID
│   ├── appstore/issuer_id       # ASC Issuer ID
│   └── match/
│       ├── match_ci_key         # Match SSH private key
│       ├── match_ci_key.pub     # Match SSH public key
│       └── .match_password      # Match encryption password
└── ios/
    └── apn/APNAuthKey.p8        # APN push key (optional)
```

## Optional: APN Setup (Push Notifications)

If your app uses Firebase Cloud Messaging for push notifications:

```bash
bash scripts/ios/setup_apn_key.sh
```

### Steps:
1. Go to [Apple Developer Keys](https://developer.apple.com/account/resources/authkeys/list)
2. Create new key with **Apple Push Notifications service (APNs)** enabled
3. Download the .p8 key file
4. Run the setup script to configure
5. Upload to Firebase Console (Cloud Messaging → APNs authentication key)

## Verification

Verify your setup:

```bash
# Verify APN configuration (if applicable)
bash scripts/ios/verify_apn_setup.sh

# Test deployment to Firebase
bash scripts/deploy/deploy_firebase.sh

# Check Fastlane configuration
bundle exec fastlane ios --help
```

## Troubleshooting

### Issue: "SSH key not authorized"

**Solution:**
1. Verify deploy key added to Match repository
2. Check "Allow write access" is enabled
3. Test SSH connection:
   ```bash
   ssh -i secrets/apple/match/match_ci_key -T git@github.com
   ```

### Issue: "Invalid Match password"

**Solution:**
1. Verify `secrets/apple/match/.match_password` contains the correct password
2. If lost, you'll need to revoke certificates and regenerate (contact Apple)

### Issue: "Certificate already exists"

**Solution:**
This is normal on first run if certificates already exist in Match repo. Match will download and use them.

### Issue: "No provisioning profiles found"

**Solution:**
```bash
# Manually sync certificates
bundle exec fastlane ios sync_certificates match_type:adhoc
bundle exec fastlane ios sync_certificates match_type:appstore
```

### Issue: "Team ID mismatch"

**Solution:**
Verify `apple.team.id` in `gradle/fork.properties` matches your Apple Developer account.

### Issue: "Build number conflict"

**Solution:**
Fastlane automatically increments build numbers. If there's a conflict, manually increment in Xcode or delete the conflicting build in App Store Connect.

## Security Best Practices

1. ✅ **Never commit secrets to git**
   - All secrets are in `.gitignore`
   - Only commit `.template` files

2. ✅ **Rotate API keys periodically**
   - Regenerate App Store Connect API keys every 6-12 months

3. ✅ **Use strong Match passwords**
   - Minimum 16 characters
   - Store in password manager

4. ✅ **Limit API key permissions**
   - Use "App Manager" role instead of "Admin" when possible

5. ✅ **Store secrets securely**
   - Use 1Password, LastPass, or similar for team sharing

## Next Steps

Once setup is complete:

1. **Test Firebase Deployment:**
   ```bash
   bash scripts/deploy/deploy_firebase.sh
   ```

2. **Test TestFlight Deployment:**
   ```bash
   bash scripts/deploy/deploy_testflight.sh
   ```

3. **Configure App Store Metadata:**
   - Add app description, screenshots, etc. in App Store Connect

4. **Set up CI/CD:**
   - Add secrets to GitHub Actions / CI environment
   - See `.github/workflows/` for CI templates

## Support

- **Apple Developer Support:** https://developer.apple.com/support/
- **Fastlane Docs:** https://docs.fastlane.tools/
- **Match Documentation:** https://docs.fastlane.tools/actions/match/
- **App Store Connect:** https://developer.apple.com/support/app-store-connect/

## Common Commands Reference

```bash
# Setup
bash scripts/ios/setup_ios_complete.sh        # Complete iOS setup
bash scripts/ios/setup_apn_key.sh             # Setup push notifications
bash scripts/ios/verify_apn_setup.sh          # Verify APN setup

# Deployment
bash scripts/deploy/deploy_firebase.sh                       # Deploy to Firebase
bash scripts/deploy/deploy_testflight.sh                     # Deploy to TestFlight
bash scripts/deploy/deploy_appstore.sh                       # Deploy to App Store

# Certificate Management
bundle exec fastlane ios sync_certificates match_type:adhoc      # Sync AdHoc certs
bundle exec fastlane ios sync_certificates match_type:appstore   # Sync App Store certs

# Debugging
bundle exec fastlane ios --help            # Show all lanes
cat gradle/fork.properties                         # View non-secret config
ls secrets/apple/ secrets/apple/                     # List secret files
ls -la secrets/                            # List all secret files
```

---

**Ready to deploy?** See [IOS_DEPLOYMENT.md](deployment.md) for deployment workflows and best practices.
