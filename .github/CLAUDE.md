# GitHub Actions - CI/CD Infrastructure

**Last Updated:** 2026-06-18
**Reusable Workflows:** [openMF/mifos-x-actionhub — releases](https://github.com/openMF/mifos-x-actionhub/releases) (workflows pin specific tags; check actual `.github/workflows/*.yml` files for the live pin)
**Custom Actions:** 13 total (4 Android, 4 iOS, 2 macOS, 1 Desktop, 1 Web, 1 Static Analysis)

[← Back to Main](../CLAUDE.md)

---

> 📌 **About version pins in this document**
>
> Throughout this guide you'll see references like `@v1.0.8`, `@v1.0.2`, etc. — those are **the versions in use at the time this section was last reviewed**, not necessarily the current pin. The authoritative source for the version any workflow uses is **the workflow file itself**:
>
> ```bash
> grep "openMF/mifos-x-actionhub" .github/workflows/*.yml
> ```
>
> For the **latest available release** of each repo, browse:
>
> | Repo | Releases |
> |---|---|
> | `openMF/mifos-x-actionhub` (orchestrator) | [releases page](https://github.com/openMF/mifos-x-actionhub/releases) |
> | `*-publish-android-kmp` | [releases](https://github.com/openMF/mifos-x-actionhub-publish-android-kmp/releases) |
> | `*-publish-apple-kmp` (iOS + macOS) | [releases](https://github.com/openMF/mifos-x-actionhub-publish-apple-kmp/releases) |
> | `*-publish-desktop-kmp` | [releases](https://github.com/openMF/mifos-x-actionhub-publish-desktop-kmp/releases) |
> | `*-publish-web-kmp` | [releases](https://github.com/openMF/mifos-x-actionhub-publish-web-kmp/releases) |
>
> When bumping a pin, update the workflow `.yml` — **don't try to bump every doc reference**. The doc is descriptive; the workflow is authoritative.

---

## Table of Contents

1. [Overview](#overview)
2. [Workflows](#workflows)
3. [Custom Actions](#custom-actions)
4. [Secrets](#secrets)
5. [Troubleshooting](#troubleshooting)

---

## Overview

This project uses the **v2 reusable workflows** from the `openMF/mifos-x-actionhub`
repository. Each local wrapper is a thin file that declares dispatch inputs + passes
secrets through; all orchestration lives in the centralized reusable workflow.

> **Version pins are per-workflow and change over time — the workflow file itself is
> authoritative.** As of this writing the v2 reusable workflows are pinned around
> `@v1.0.21`, with `release-multi-platform-v2` at `@v1.0.47`. To read the exact pin any
> local wrapper uses:
> ```bash
> grep -H 'uses: openMF/mifos-x-actionhub' .github/workflows/*.yml
> ```

**Architecture:**
```
Local wrapper workflows (.github/workflows/*.yml)
    ↓  uses: …/mifos-x-actionhub/.github/workflows/<name>-v2.yaml@<pin>
Reusable v2 workflows (mifos-x-actionhub/.github/workflows/)
    ↓
Custom Actions (mifos-x-actionhub-*/)
    ↓
Fastlane Lanes (fastlane/Fastfile)
```

---

## Workflows

The current release model is the actionhub **v2 rung-ladder** with **environment-gated
approvals** — not the older `multi-platform-build-and-publish.yml` / `promote-to-production.yml`
pair (those no longer exist). The local `.github/workflows/` directory holds these wrappers:

| Workflow | Reusable (v2) | Trigger | Purpose |
|---|---|---|---|
| `release-multi-platform.yml` | `release-multi-platform-v2` | `workflow_dispatch` | Main release. Per-platform **rung** inputs (`<platform>_rung`) — pick the TOP rung to reach; lower rungs auto-fire. |
| `release-android-only.yml` | `publish-android` (direct) | `workflow_dispatch` | Android-only minimal graph — bypasses the large multi-platform reusable graph (avoids a fork-side `startup_failure`). |
| `pr-check.yml` | `pr-check-v2` | `pull_request` | PR validation — static analysis + per-platform debug builds. |
| `quality-gate.yml` | *(local, no reusable)* | `pull_request` | Spotless + Detekt + Dependency Guard + Kover. Kept local (the reusable v2 bundles SBOM unconditionally, which breaks on Gradle 9 + KMP 2.3). |
| `tag-weekly-release.yml` | `tag-weekly-release-v2` | `cron '0 4 * * 0'` + dispatch | Sunday Beta tag (`vYYYY.W##.0`, calver-week) → lands at **Stage 2 (Beta)**. |
| `tag-monthly-release.yml` | `tag-monthly-release-v2` | `cron '30 3 1 * *'` + dispatch | Monthly Production tag (`vYYYY.MM.0`, calver-month) → lands at **Stage 3 (Production)**. |
| `rollback.yml` | `rollback-v2` | `workflow_dispatch` | Per-platform release rollback. `reason` required (audit). Each platform job uses a `rollback-<platform>` environment with required reviewers. |
| `deployment-status.yml` | `deployment-status-v2` | `cron '0 * * * *'` + dispatch | Hourly rung-matrix dashboard rendered from `deployment/PROMOTION_LOG.yaml` into the run's step summary. |

> Exact pins live in the wrapper files (see the Overview grep). Each wrapper is thin —
> it maps its `workflow_dispatch` inputs onto the reusable workflow's `with:` block and
> passes `secrets: inherit`; the orchestration is entirely in the reusable v2 workflow.

### Rung ladder (how a release promotes)

Promotion is **not** a separate workflow anymore — it's expressed as a rung on the main
release dispatch. For each platform you pick `<platform>_rung` = the **top rung to reach**,
and every lower rung fires first:

```
internal  →  beta / testflight-external  →  production / app-store / stable
```

- `release-multi-platform.yml` maps the dispatch `*_rung` inputs to the orchestrator's
  `*_target` inputs (a historical `*_rung`→`*_target` name mismatch was fixed here).
- Android supports a staged rollout fraction (`android_staged_rollout`, `0.0–1.0`) used
  only when `android_rung: production`.
- Picking `production` runs the whole ladder for that platform in one dispatch.

### Environment-gated approvals

Each store/distribution stage in the reusable workflow declares `environment: <name>`
(e.g. `android-play-production`, `ios-app-store`, `web-<host>-production`). GitHub pauses
that stage behind an in-graph **"Approve and deploy"** button **only if the environment
has required reviewers** — otherwise the job runs immediately with no prompt.

Those reviewers are configured per-repo by
[`scripts/configure-release-environments.sh`](../scripts/configure-release-environments.sh)
(admin-only API). Prefer a **team** reviewer so any team member can approve and the gate
survives personnel changes:

```bash
# Gate only the public/production-facing stages, team-approved, 30-min wait on the top rung.
bash scripts/configure-release-environments.sh \
  --repo openMF/kmp-project-template \
  --reviewer-team openMF/<release-approvers-team-slug> \
  --production-only \
  --wait-prod 30
```

**Required Secrets:** the release dispatch needs the full platform secret set (30+ — see
[Secrets](#secrets)); `pr-check.yml` / `quality-gate.yml` need none (debug + static only).

**Required Secrets:** `PLAYSTORECREDS`

---

## Custom Actions

### Android Actions (4)

#### 1. `mifos-x-actionhub-build-android-app@v1.0.2`

**Purpose:** Build Android APK or AAB (debug or release)

**Inputs:**
- `android_package_name` (required): Module name (e.g., `cmp-android`)
- `build_type` (required): `Debug` or `Release`
- `key_store` (optional): Base64-encoded keystore
- `google_services` (optional): Base64-encoded google-services.json
- `key_store_password`, `key_store_alias`, `key_store_alias_password` (optional)
- `java-version` (default: `17`)

**What it does:**
1. Sets up Java 17 (Zulu distribution)
2. Caches Gradle dependencies
3. **Version Generation** (if Release):
   - Tries: `./gradlew versionFile` → reads `version.txt`
   - Fallback: Git-based version (latest tag + commit count)
   - Calculates `VERSION_CODE` from commit count
4. Inflates keystore and google-services.json from base64
5. Runs: `./gradlew :{package_name}:assembleRelease` (or `assembleDebug`)
6. Uploads APK as artifact

**⚠️ Known Issue:** `set +e` swallows versionFile errors. See [BUGS_AND_ISSUES.md](../docs/architecture/tree/deployment/known-issues.md#4-version-generation-task-may-fail-silently).

**Output Artifacts:**
- `android-app` (all APKs from `**/build/outputs/apk/**/*.apk`)

---

#### 2. `mifos-x-actionhub-android-firebase-publish@v1.0.0`

**Purpose:** Deploy Android APK to Firebase App Distribution

**Inputs:**
- `android_package_name` (required)
- `release_type` (optional): `prod` (default) or `demo`
- `keystore_file`, `keystore_password`, `keystore_alias`, `keystore_alias_password`
- `google_services` (required): Base64-encoded google-services.json
- `firebase_creds` (required): Base64-encoded Firebase service account JSON
- `tester_groups` (required): Firebase tester group name

**What it does:**
1. Installs Fastlane + plugins (`firebase_app_distribution`, `increment_build_number`)
2. Inflates secrets to:
   - `{package_name}/google-services.json`
   - `keystores/upload_keystore.keystore`
   - `secrets/android/firebaseAppDistributionServiceCredentialsFile.json`
3. Calls Fastlane lane:
   - Prod: `bundle exec fastlane android deployReleaseApkOnFirebase`
   - Demo: `bundle exec fastlane android deployDemoApkOnFirebase`
4. Cleans up secrets

**⚠️ CRITICAL BUG:** The `tester_groups` input is **IGNORED**. Fastlane lane doesn't use it.
- **Workaround:** Set `ENV['FIREBASE_GROUPS']` in workflow environment
- See [BUGS_AND_ISSUES.md](../docs/architecture/tree/deployment/known-issues.md#1-firebase-tester-groups-parameter-ignored)

**Output Artifacts:**
- `firebase-app` (all APKs)

---

#### 3. `mifos-x-actionhub-publish-android-on-playstore-beta@v1.0.0`

**Purpose:** Deploy AAB to Play Store (internal track, optionally promote to beta)

**Inputs:**
- `android_package_name` (required)
- `release_type` (required): `internal` or `beta`
- Keystore parameters (same as above)
- `google_services` (required)
- `playstore_creds` (required): Base64-encoded Play Store service account JSON

**What it does:**
1. Inflates secrets to:
   - `{package_name}/google-services.json`
   - `keystores/upload_keystore.keystore`
   - `secrets/android/playStorePublishServiceCredentialsFile.json`
2. Calls Fastlane lane: `bundle exec fastlane android deployInternal`
   - Uploads AAB to internal track
3. If `release_type == 'beta'`: `bundle exec fastlane android promoteToBeta`
   - Promotes internal → beta
4. Cleans up secrets

**Fastlane Lanes Used:**
- `deployInternal` (line 108): Builds AAB, uploads to internal track
- `promoteToBeta` (line 139): Promotes internal → beta

**Output Artifacts:**
- `play-store-app` (AAB from `**/build/outputs/bundle/**/*.aab`)

---

#### 4. `mifos-x-actionhub-publish-android-on-playstore-production@v1.0.0`

**Purpose:** Promote Play Store beta track → production

**Inputs:**
- `playstore_creds` (required): Base64-encoded service account JSON

**What it does:**
1. Installs Fastlane
2. Inflates Play Store credentials to `secrets/android/playStorePublishServiceCredentialsFile.json`
3. Calls: `bundle exec fastlane android promote_to_production`
4. Cleans up secrets

**⚠️ Known Issue:** No validation that beta release exists. See [BUGS_AND_ISSUES.md](../docs/architecture/tree/deployment/known-issues.md#5-production-promotion-has-no-validation).

**Fastlane Lane Used:**
- `promote_to_production` (line 151): Promotes beta → production

**No Output Artifacts**

---

### iOS Actions (4)

#### 5. `mifos-x-actionhub-build-ios-app@v1.0.3`

**Purpose:** Build iOS IPA (debug unsigned or release signed)

**Inputs:**
- `ios_package_name` (required): iOS module name (e.g., `cmp-ios`)
- `build_type` (required): `Debug` or `Release`
- `shared_module` (required): Shared module path
- For Release builds:
  - `appstore_key_id`, `appstore_issuer_id` (App Store Connect API)
  - `appstore_auth_key` (Base64-encoded .p8 file)
  - `match_password`: Fastlane Match passphrase
  - `match_ssh_private_key` (Base64-encoded SSH key for Match repo)

**What it does:**
1. Sets up Ruby + Fastlane
2. Sets up Xcode (default: 15.2)
3. If Release:
   - Writes App Store Connect API key to `secrets/apple/appstore/AuthKey.p8`
   - Configures SSH for Fastlane Match: `secrets/apple/match/match_ci_key` + `~/.ssh/config`
   - Calls: `bundle exec fastlane ios build_signed_ios`
4. If Debug:
   - Calls: `bundle exec fastlane ios build_ios` (no code signing)
5. Cleans up secrets

**Fastlane Lanes Used:**
- `build_ios` (line 436): Debug build, skip codesigning
- `build_signed_ios` (line 456): Release build with Match certificates

**Output Artifacts:**
- `ios-app` (IPA from `**/build/**/*.ipa`)

---

#### 6. `mifos-x-actionhub-publish-ios-on-firebase@v1.0.3`

**Purpose:** Deploy iOS IPA to Firebase App Distribution

**Inputs:**
- `ios_package_name` (required)
- `shared_module` (required)
- App Store Connect API parameters (same as build-ios-app)
- `firebase_creds` (required): Base64-encoded service account JSON
- `tester_groups` (required): Firebase tester group

**What it does:**
1. Installs Fastlane + `firebase_app_distribution`, `increment_build_number` plugins
2. Writes secrets:
   - `secrets/apple/appstore/AuthKey.p8`
   - `secrets/apple/match/match_ci_key`
   - `secrets/android/firebaseAppDistributionServiceCredentialsFile.json`
   - SSH config for Match
3. Calls: `bundle exec fastlane ios deploy_on_firebase`
   - Auto-increments build number from latest Firebase release
   - Builds signed IPA
   - Uploads to Firebase
4. Cleans up secrets

**⚠️ CRITICAL BUG:** Same as Android - `tester_groups` input is ignored.
- **Workaround:** Set `ENV['FIREBASE_GROUPS']`
- See [BUGS_AND_ISSUES.md](../docs/architecture/tree/deployment/known-issues.md#1-firebase-tester-groups-parameter-ignored)

**Fastlane Lane Used:**
- `deploy_on_firebase` (line 508): Increment version, build, upload

**Output Artifacts:**
- `firebase-app-ios` (IPA)

---

#### 7. `mifos-x-actionhub-publish-ios-on-appstore-testflight@v1.0.1`

**Purpose:** Upload iOS build to TestFlight

**Inputs:**
- Same as publish-ios-on-firebase (no Firebase creds needed)

**What it does:**
1. Writes secrets (App Store Connect API key, Match SSH key)
2. Calls: `bundle exec fastlane ios beta`
   - Gets version from Gradle (sanitized for App Store: `YYYY.M.{commitCount}`)
   - Increments build number from latest TestFlight build
   - Builds signed IPA with appstore provisioning profile
   - Uploads to TestFlight with comprehensive metadata
3. Cleans up secrets

**Fastlane Lane Used:**
- `beta` (line 532): Version, build, upload to TestFlight

**Version Sanitization:**
- Gradle: `2026.1.1-beta.0.9+abc123` → App Store: `2026.1.9`
- See [Version Handling Guide](../docs/architecture/tree/gradle/version-handling.md)

**Output Artifacts:**
- `testflight-app` (IPA)

---

#### 8. `mifos-x-actionhub-publish-ios-on-appstore@v1.0.1`

**Purpose:** Submit iOS app to App Store for review

**Inputs:**
- Same as TestFlight action

**What it does:**
1. Writes secrets
2. Calls: `bundle exec fastlane ios release`
   - Gets sanitized version from Gradle
   - Increments build number from TestFlight
   - Updates Info.plist with privacy strings
   - Builds signed IPA
   - Generates release notes from conventional commits
   - Uploads to App Store with metadata
   - Submits for review
3. Cleans up secrets

**Fastlane Lane Used:**
- `release` (line 635): Version, build, upload, submit for review

**Output Artifacts:**
- `appstore-app` (IPA)

---

### macOS Actions (2)

#### 9. `mifos-x-actionhub-publish-macos-on-appstore-testflight-kmp@v1.0.0`

**Purpose:** Deploy macOS app to TestFlight

**Inputs:**
- `desktop_package_name` (required)
- App Store Connect API parameters
- `mac_signing_certificate` (Base64-encoded .p12)
- `mac_signing_certificate_password`
- `mac_installer_certificate` (Base64-encoded .p12)
- `mac_installer_certificate_password`
- `mac_provisioning_profile_base64` (Base64-encoded .provisionprofile)
- `bundle_identifier` (required)

**What it does:**
1. Creates temporary keychain
2. Imports signing certificates (.p12 files)
3. Writes provisioning profile to `~/Library/MobileDevice/Provisioning Profiles/`
4. Calls: `bundle exec fastlane mac desktop_testflight`
5. Cleans up keychain and secrets

**Note:** macOS uses **manual certificate management** (not Fastlane Match)

---

#### 10. `mifos-x-actionhub-publish-macos-on-appstore-kmp@v1.0.0`

**Purpose:** Deploy macOS app to App Store

**Inputs:**
- Same as macOS TestFlight action

**What it does:**
- Same as TestFlight, but calls: `bundle exec fastlane mac desktop_release`

**Note:** Production macOS deployment

---

### Desktop Action (1)

#### 11. `mifos-x-actionhub-publish-desktop-app-kmp@v1.0.1`

**Purpose:** Build Desktop apps for Windows, macOS, Linux

**Matrix Strategy:**
```yaml
strategy:
  matrix:
    os: [ubuntu-latest, windows-latest, macos-latest]
runs-on: ${{ matrix.os }}
```

**Inputs:**
- `desktop_package_name` (required)
- Windows, macOS, Linux signing parameters (9 total secrets)
- `java-version` (default: `17`)

**What it does:**
1. Sets up Java 17
2. Sets up Gradle
3. Runs: `./gradlew :${{ desktop_package_name }}:packageReleaseDistributionForCurrentOS`
4. Uploads platform-specific artifacts:
   - **Windows:** `*.exe`, `*.msi` (lines 72-91)
   - **macOS:** `*.dmg` (lines 97-107)
   - **Linux:** `*.deb` (lines 102-107)

**Compose Desktop Gradle Task:**
- `packageReleaseDistributionForCurrentOS` (Compose Desktop plugin)

**Output Artifacts:**
- `desktop-app-windows`, `desktop-app-macos`, `desktop-app-linux`

---

### Web Action (1)

#### 12. `mifos-x-actionhub-web-publish-kmp@v1.0.1`

**Purpose:** Deploy web app to GitHub Pages

**Inputs:**
- `web_package_name` (required)
- `java-version` (default: `17`)

**What it does:**
1. Sets up Java 17
2. Runs: `./gradlew :${{ web_package_name }}:jsBrowserDistribution`
3. Deploys to GitHub Pages:
   - Uses `peaceiris/actions-gh-pages@v4`
   - Publishes `build/dist/js/productionExecutable/` to `gh-pages` branch

**Kotlin/JS Gradle Task:**
- `jsBrowserDistribution` (Kotlin/JS plugin)

**Outputs:**
- `page_url`: GitHub Pages URL

**Output Artifacts:**
- `web-app` (JavaScript distribution)

---

### Static Analysis Action (1)

#### 13. `mifos-x-actionhub-static-analysis-check@v1.0.1`

**Purpose:** Run code quality checks

**Inputs:**
- `java-version` (default: `17`)

**What it does:**
1. Sets up Java 17
2. Sets up Gradle
3. Runs checks sequentially:
   ```bash
   ./gradlew check -p build-logic        # Build logic checks
   ./gradlew spotlessCheck                # Code formatting
   ./gradlew detekt                       # Kotlin linting
   ./gradlew dependencyGuard              # Dependency validation
   ```
4. Uploads Detekt reports as artifacts

**Tools:**
- **Spotless:** Enforces code formatting (Kotlin, KTS files)
- **Detekt:** Kotlin static analysis and linting
- **Dependency Guard:** Validates dependency changes

**Output Artifacts:**
- `detekt-reports` (Detekt HTML/XML reports)

---

## Secrets

### Secret Categories

| Category | Count | Secrets |
|----------|-------|---------|
| **Android** | 4 | UPLOAD_KEYSTORE_FILE, UPLOAD_KEYSTORE_FILE_PASSWORD, UPLOAD_KEYSTORE_ALIAS, UPLOAD_KEYSTORE_ALIAS_PASSWORD |
| **Firebase** | 3 | FIREBASECREDS, GOOGLESERVICES |
| **Play Store** | 1 | PLAYSTORECREDS |
| **iOS** | 5 | APPSTORE_KEY_ID, APPSTORE_ISSUER_ID, APPSTORE_AUTH_KEY, MATCH_PASSWORD, MATCH_GIT_PRIVATE_KEY |
| **Desktop** | 9 | WINDOWS_SIGNING_KEY, WINDOWS_SIGNING_PASSWORD, WINDOWS_SIGNING_CERTIFICATE, MACOS_SIGNING_KEY, MACOS_SIGNING_PASSWORD, MACOS_SIGNING_CERTIFICATE, LINUX_SIGNING_KEY, LINUX_SIGNING_PASSWORD, LINUX_SIGNING_CERTIFICATE |
| **Shared** | 1 | GITHUB_TOKEN (auto-provided) |

**Total:** 30+ secrets

### File-to-Secret Mapping

Use `scripts/white-label/keystore.sh` to encode secrets:

| File in `secrets/` | GitHub Secret Name | Used By |
|-------------------|--------------------|---------|
| `firebaseAppDistributionServiceCredentialsFile.json` | `FIREBASECREDS` | Android/iOS Firebase publish |
| `google-services.json` | `GOOGLESERVICES` | Android build/deploy |
| `playStorePublishServiceCredentialsFile.json` | `PLAYSTORECREDS` | Play Store publish/promote |
| `Auth_key.p8` | `APPSTORE_AUTH_KEY` | iOS build/deploy |
| `match_ci_key` | `MATCH_GIT_PRIVATE_KEY` | iOS build/deploy (Match access) |

**Commands:**
```bash
# Encode all secrets for GitHub Actions
./keystore-manager.sh encode-secrets

# Add secrets to GitHub repository (requires gh CLI)
./keystore-manager.sh add

# View current secrets
./keystore-manager.sh view
```

See [Secrets Management Guide](../docs/architecture/tree/secrets/management.md) for complete reference.

---

## Troubleshooting

### Common Issues

#### 1. Workflow fails with "Secret not found"

**Cause:** Missing GitHub secret

**Fix:**
```bash
# Check what secrets are configured
gh secret list

# Add missing secret
./keystore-manager.sh add  # Interactive mode
```

---

#### 2. Firebase deployment succeeds but wrong tester group

**Cause:** `tester_groups` input is ignored (known bug)

**Fix:** Set environment variable in workflow:
```yaml
env:
  FIREBASE_GROUPS: "my-tester-group"
```

See [BUGS_AND_ISSUES.md](../docs/architecture/tree/deployment/known-issues.md#1-firebase-tester-groups-parameter-ignored)

---

#### 3. iOS build fails with "Match certificates not found"

**Possible causes:**
- Match SSH key not configured
- Match password incorrect
- Match repository empty

**Fix:**
```bash
# Run iOS setup wizard
./scripts/ios/setup_ios_complete.sh

# Verify Match configuration
./scripts/ios/verify_ios_deployment.sh
```

---

#### 4. Version mismatch between platforms

**Cause:** Version generation from Gradle → Firebase → App Store requires sanitization

**Fix:** Fastlane automatically sanitizes versions. See [Version Handling Guide](../docs/architecture/tree/gradle/version-handling.md).

---

#### 5. Production promotion fails with "No beta release found"

**Cause:** Beta track empty (known issue - no pre-flight validation)

**Fix:**
1. Check Play Console for beta releases
2. Deploy to beta first: `release_type: beta` in multi-platform workflow
3. Wait for beta review to complete
4. Then promote to production

---

#### 6. Desktop build fails on specific OS

**Cause:** Platform-specific signing issues or missing dependencies

**Check:**
- Signing certificates are valid for target platform
- Compose Desktop version supports target OS
- Required native dependencies installed on runner

---

#### 7. Web deployment fails with "Permission denied"

**Cause:** `GITHUB_TOKEN` lacks Pages write permissions

**Fix:**
1. Go to Settings → Actions → General
2. Workflow permissions → "Read and write permissions"
3. Re-run workflow

---

### Debugging Tips

1. **Check Action Logs:**
   - GitHub Actions → Failed workflow → Expand failed step
   - Look for Fastlane errors, Gradle failures, or secret inflation issues

2. **Validate Locally:**
   ```bash
   # Test Fastlane lanes locally
   bundle exec fastlane android deployInternal --verbose
   bundle exec fastlane ios beta --verbose
   ```

3. **Dry Run:**
   - Use `skip_submission: true` in Fastlane lanes to test build without uploading

4. **Secret Validation:**
   ```bash
   # Verify secrets are properly encoded
   ./keystore-manager.sh view

   # Re-encode if corrupted
   ./keystore-manager.sh encode-secrets
   ```

---

**Need more help?**
- [Deployment Playbook](../docs/architecture/tree/deployment/playbook.md)
- [Known Issues](../docs/architecture/tree/deployment/known-issues.md)
- [GitHub Actions Deep Dive](../docs/architecture/tree/github/actions-deep-dive.md)

[← Back to Main](../CLAUDE.md)
