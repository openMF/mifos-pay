# `app-profile/`

> **Kind:** Fork-owned configuration source of truth  
> **Measured:** 96 tracked files — 31× `.png`, 24× `.yaml`, 15× `.gitkeep`, 10× `.webp`, 5× `.md`

## Shape

| Path | Files |
|---|---:|
| `app-profile/platforms/` | 70 |
| `app-profile/icons/` | 20 |
| (files at the root) | 6 |

## Declared configuration

| | |
|---|---|
| `app-profile/app.schema.yaml` | who owns each key of app-profile/app.yaml when a fork syncs from the template. |
| `app-profile/app.yaml` | fork-owned white-label deployment SoT (COMMON, platform-agnostic). |
| `app-profile/deploy-targets.yaml` | ═════════════════════════════════════════════════════════════════════════════ |
| `app-profile/migration-ledger.yaml` | the FORK's Room schema state. owner: fork, never synced. |
| `app-profile/package-ledger.yaml` | the FORK's record of what it did to the template's declared packages. |
| `app-profile/platforms/android/android.yaml` | Android-only (Play Store + Firebase) values. |
| `app-profile/platforms/android/app-content/app-content.yaml` | app-profile/platforms/android/app-content/app-content.yaml |
| `app-profile/platforms/android/app-content/content-rating.yaml` | app-profile/platforms/android/app-content/content-rating.yaml |
| `app-profile/platforms/apple/apple.yaml` | Apple-shared (iOS + macOS: signing, Firebase, TestFlight, ASC). |
| `app-profile/platforms/apple/ios/app-content/age-rating.yaml` | deployment-layer/store-listing/variants/v1/app-content/ios/en-US/age-rating.yaml |
| `app-profile/platforms/apple/ios/app-content/content-rights.yaml` | deployment-layer/store-listing/variants/v1/app-content/ios/en-US/content-rights.yaml |
| `app-profile/platforms/apple/ios/app-content/export-compliance.yaml` | deployment-layer/store-listing/variants/v1/app-content/ios/en-US/export-compliance.yaml |
| `app-profile/platforms/apple/ios/ios.yaml` | iOS-ONLY overrides (deep-merged under `apple.ios`). |
| `app-profile/platforms/apple/macos/app-content/age-rating.yaml` | deployment-layer/store-listing/variants/v1/app-content/macos/en-US/age-rating.yaml |

## Subsections

| Unit | Files | Page |
|---|---:|---|
| `app-profile/platforms/` | 70 | [platforms](app-profile/platforms.md) |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `app-profile/README.md` | app-profile/ — fork-owned white-label deployment SoT |
| `app-profile/icons/README.md` | Fork app icons |

## Guides

_Authored in depth, living in this area's own directory._

- [Firebase Setup Script(Incubating)](app-profile/firebase-setup.md)
- [Fork Quickstart — Day-1 Customization Checklist](app-profile/fork-quickstart.md)
- [KMP Project Master Setup Script(Incubating)](app-profile/project-setup.md)
- [Setup Guide](app-profile/setup.md)

<!-- tree-scaffold:end -->
## Significance

**The fork-owned source of truth for identity.** Every brand-touching value lives here — app id,
display name, version, org details, endpoints, icons, store listing, the store-compliance forms — and
`./gradlew syncForkConfig` projects it onto every generated surface: `gradle/fork.properties`, the
version catalog, `Config.xcconfig`, per-module BuildKonfig, per-flavor BuildConfig, the platform icon
trees and the deployment metadata.

**The deployment metadata is derived, so it must derive from nothing but a clone.** Each platform's
`deployment/…/metadata/` tree is gitignored and the template tracks only some sub-dirs, so
`syncForkConfig` creates any missing sub-dir (`review_information/`, `<locale>/changelogs/`) under a
metadata root that exists — the App Review contact and the Play changelog are written on a fresh CI
checkout too. A platform with no metadata root is one the fork does not ship, and is skipped
(`ensureMetadataParent`, pinned by `MetadataParentTest`). `org.phone` ships empty: it is the App
Review contact, and a placeholder there is uploaded as every fork's real number.

**Edit here, never downstream.** `gradle/fork.properties` is a generated bridge, not the SoT, and
`libs.versions.toml#appId` is written from it — `scripts/product-health/checks/appid-consistency.sh`
fails CI when the two drift. A hand-edit to the bridge survives exactly until the next sync.

**The compliance YAMLs are not paperwork.** The age-rating, content-rights, export-compliance and
data-safety declarations are what a store checks before review. An incorrect answer is a policy
violation rather than a listing error, and a missing
`ITSAppUsesNonExemptEncryption` stops every upload to ask — which blocks an automated release. They
are authored here so the answers are reviewable in a diff and reproducible on re-submission.
