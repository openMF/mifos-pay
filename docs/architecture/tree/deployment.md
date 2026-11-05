# `deployment/`

> **Kind:** Release + deployment automation  
> **Measured:** 297 tracked files — 79× `.txt`, 41× `.yaml`, 35× `.png`, 30× `.md`, 30× `.rb`

## Shape

| Path | Files |
|---|---:|
| `deployment/desktop/` | 93 |
| `deployment/ios/` | 93 |
| `deployment/android/` | 36 |
| `deployment/web/` | 27 |
| `deployment/_shared/` | 25 |
| (files at the root) | 9 |
| `deployment/linux/` | 4 |
| `deployment/windows/` | 4 |
| `deployment/fastlane/` | 3 |
| `deployment/scripts/` | 2 |
| `deployment/.bundle/` | 1 |

## Fastlane lanes

| | |
|---|---|
| `fastlane … android_screenshots` | Alias for upload_android_screenshots |
| `fastlane … attachAppStoreSubscriptions` | Attach the app's subscriptions to the App Store review submission (no-op when there are none) |
| `fastlane … beta` | Upload beta build to TestFlight (parameterized on flavor + build_type; scheme from resolver) |
| `fastlane … buildMacDmg` | Build unsigned macOS DMG and upload to GitHub Release (direct-distro Stage 1/2/3 via STAGE env) |
| `fastlane … buildNotarizedMacDmg` | Tier-2 Apple-notarized macOS DMG → GitHub Releases (Developer ID via Match + notarytool via Fastlane notarize) |
| `fastlane … create_mac_installer_cert` | One-shot: create proper Mac Installer Distribution cert in Apple Developer Portal + push to Match repo. |
| `fastlane … deployApkOnFirebase` | Publish Android APK to Firebase App Distribution (parameterized on flavor + build_type) |
| `fastlane … deployInternal` | Deploy Android AAB to Google Play Store (parameterized on flavor + build_type; track from config) |
| `fastlane … deployIpaOnFirebase` | Upload iOS IPA to Firebase App Distribution (parameterized on flavor + build_type) |
| `fastlane … desktop_release` | Full Mac App Store release — build PKG from source + deliver (use promoteMacToAppStore when a TestFlight build already exists) |
| `fastlane … desktop_testflight` | Build and upload macOS desktop build to TestFlight (Mac App Store track) |
| `fastlane … frame_ios_screenshots` | Frame iOS screenshots with device bezels via frameit |
| `fastlane … ios_screenshots` | Frame + upload iOS screenshots (generation is handled by /release) |
| `fastlane … macos_screenshots` | Upload macOS screenshots (generation is handled by /release) |
| `fastlane … promoteMacToAppStore` | Promote an existing Mac TestFlight build to Mac App Store review — no rebuild, no re-upload. |
| `fastlane … promoteMacToExternalBeta` | Stage 1 → Stage 2 promotion: distribute an already-uploaded Mac TF build to external testers (no rebuild). Triggers Apple's beta review (~24h). |
| `fastlane … promoteToAppStore` | Promote an existing TestFlight build to App Store review — no rebuild, no re-upload. Mirrors Android's promote_to_production. |
| `fastlane … promoteToBeta` | Promote internal track → beta on Google Play (flavor-neutral by API; flavor passthrough for secrets) |
| `fastlane … promoteToClosed` | Promote internal track to closed testing (alpha) on Google Play |
| `fastlane … promoteToExternalBeta` | Stage 1 → Stage 2 promotion: distribute an already-uploaded TF build to external testers (no rebuild, no re-upload). Triggers Apple's beta review (~24h). |
| `fastlane … promote_to_production` | Promote to production on Google Play (internal → production for first-time apps; beta → production otherwise) |
| `fastlane … release` | Upload iOS application to App Store (parameterized on flavor + build_type; scheme from resolver) |
| `fastlane … renewAllCerts` |  |
| `fastlane … renewCerts` | Renew expired iOS Distribution certificate: revoke from Apple portal + create fresh adhoc + appstore certs |

## Subsections

| Unit | Files | Page |
|---|---:|---|
| `deployment/_shared/` | 25 | [_shared](deployment/_shared.md) |
| `deployment/android/` | 36 | [android](deployment/android.md) |
| `deployment/desktop/` | 93 | [desktop](deployment/desktop.md) |
| `deployment/fastlane/` | 3 | [fastlane](deployment/fastlane.md) |
| `deployment/ios/` | 93 | [ios](deployment/ios.md) |
| `deployment/web/` | 27 | [web](deployment/web.md) |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `deployment/BOOTSTRAP.md` | deployment/BOOTSTRAP.md — Fork onboarding guide |
| `deployment/DEVELOPMENT.md` | `deployment/` — DEVELOPMENT |
| `deployment/README.md` | deployment/ — kmp-project-template |
| `deployment/fastlane/README.md` | Installation |

## Guides

_Authored in depth, living in this area's own directory._

- [Fastlane Configuration](deployment/fastlane-configuration.md)
- [Known deployment issues](deployment/known-issues.md)
- [Deployment Playbook - Production Deployment Guide](deployment/playbook.md)
- [Release Onboarding Checklist — blocked rungs](deployment/release-onboarding.md)

<!-- tree-scaffold:end -->
## Significance

Eighteen deploy targets across five platforms, expressed as Fastlane lanes. This is where a build
becomes a release, and the area where a mistake is expensive: a bad upload reaches a store review
queue, and a signing error can lock a fork out of updating its own app.

**`DEPLOYMENT_MANIFEST.yaml` is the source of truth**, not the lanes. It declares every target with
its canonical name, lane and ownership; `scripts/deployment-manifest-validate.sh` fails when a target
has no lane or a lane has no target. That check is what stops the manifest and the Fastfile from
drifting into disagreement about what ships.

**Signing is Match-only.** macOS certificates come from the same Match repository as iOS, so CI needs
only `MATCH_PASSWORD` and an SSH key — never a `.p12` pasted into a repository secret. Lanes build a
THROWAWAY keychain and never touch the login keychain, so a failed build cannot leave a developer's
default keychain altered. Hand-importing a certificate is the single most common way to break local
signing, and it is why the ad-hoc keychain path is refused rather than documented.

**Production is gated deliberately.** Store-facing stages run in GitHub Environments with required
reviewers, configured once by `scripts/configure-release-environments.sh`. Without reviewers the
stage runs unprompted — the gate is the reviewers, not the workflow.
