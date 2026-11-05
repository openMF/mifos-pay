# `deployment/ios/`

> Part of [`deployment/`](../deployment.md)  
> **Measured:** 93 tracked files — 39× `.txt`, 20× `.png`, 12× `.gitkeep`, 6× `.yaml`, 6× `.rb`

### Lanes

| Lane | Purpose |
|---|---|
| `attachAppStoreSubscriptions` | Attach the app's subscriptions to the App Store review submission (no-op when there are none) |
| `beta` | Upload beta build to TestFlight (parameterized on flavor + build_type; scheme from resolver) |
| `deployIpaOnFirebase` | Upload iOS IPA to Firebase App Distribution (parameterized on flavor + build_type) |
| `frame_ios_screenshots` | Frame iOS screenshots with device bezels via frameit |
| `ios_screenshots` | Frame + upload iOS screenshots (generation is handled by /release) |
| `promoteToAppStore` | Promote an existing TestFlight build to App Store review — no rebuild, no re-upload. Mirrors Android's promote_to_production. |
| `promoteToExternalBeta` | Stage 1 → Stage 2 promotion: distribute an already-uploaded TF build to external testers (no rebuild, no re-upload). Triggers Apple's beta review (~24h). |
| `release` | Upload iOS application to App Store (parameterized on flavor + build_type; scheme from resolver) |
| `renewAllCerts` |  |
| `renewCerts` | Renew expired iOS Distribution certificate: revoke from Apple portal + create fresh adhoc + appstore certs |
| `syncListing` | OVERRIDE App Store listing TEXT (name/subtitle/keywords/description) from the promoted deck — no build, no submission. Screenshots are overridden separately by the deploy runtime via asc-upload-screenshots.rb (reliable clear-and-replace). |
| `uploadAppStore` | Upload an already-built IPA to App Store (skips build; use after release build succeeded but deliver failed) |
| `uploadTestFlight` | Upload an already-built IPA to TestFlight (skips build; use after beta build succeeded but pilot upload failed) |
| `upload_ios_screenshots` | Upload framed iOS screenshots to App Store Connect |

### Contents

`appstore/`, `certs/`, `firebase/`, `screenshots/`, `sync-listing/`, `testflight/`

### Docs in the tree

| Path | |
|---|---|
| `deployment/ios/appstore/README.md` | deployment/ios/appstore — App Store (production) |
| `deployment/ios/firebase/README.md` | deployment/ios/firebase — iOS Firebase App Distribution |
| `deployment/ios/testflight/README.md` | deployment/ios/testflight — TestFlight |

### Guides

- [iOS Deployment Verification Checklist](ios/checklist.md)
- [iOS Deployment Guide](ios/deployment.md)
- [iOS Setup Guide](ios/setup.md)

<!-- tree-scaffold:end -->
## Significance

TestFlight and the App Store, plus Firebase for pre-release builds. Every lane is parameterised on
flavor and build type, and the Xcode scheme is resolved rather than hardcoded.

**Signing is Fastlane Match, exclusively.** Certificates and profiles live in an encrypted
provisioning repository; a lane fetches them into a throwaway keychain and never touches the login
keychain. Hand-importing a `.p12` is the single most common way to break local signing, which is why
that path is refused rather than documented.

**Two promotions that do not rebuild.** `promoteToExternalBeta` moves an uploaded TestFlight build to
external testers (triggering Apple's ~24h beta review); `promoteToAppStore` moves it to App Store
review. Neither re-uploads, so the binary under review is the one that was tested.

**The version sanitisation is not cosmetic.** Gradle produces `YYYY.M.D-beta.N+sha`; the App Store
accepts only `YYYY.M.N`. Fastlane converts it, and `check_ios_version.sh` explains the mapping —
submitting an unsanitised version is rejected at upload, after the build.
