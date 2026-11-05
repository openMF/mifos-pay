fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android deployApkOnFirebase

```sh
[bundle exec] fastlane android deployApkOnFirebase
```

Publish Android APK to Firebase App Distribution (parameterized on flavor + build_type)

### android deployReleaseApkOnFirebase

```sh
[bundle exec] fastlane android deployReleaseApkOnFirebase
```

Alias: prod release APK → Firebase (delegates to deployApkOnFirebase(flavor: :prod, build_type: :release))

### android deployDemoApkOnFirebase

```sh
[bundle exec] fastlane android deployDemoApkOnFirebase
```

Alias: demo release APK → Firebase (delegates to deployApkOnFirebase(flavor: :demo, build_type: :release))

### android promoteToBeta

```sh
[bundle exec] fastlane android promoteToBeta
```

Promote internal track → beta on Google Play (flavor-neutral by API; flavor passthrough for secrets)

### android promoteToClosed

```sh
[bundle exec] fastlane android promoteToClosed
```

Promote internal track to closed testing (alpha) on Google Play

### android deployInternal

```sh
[bundle exec] fastlane android deployInternal
```

Deploy Android AAB to Google Play Store (parameterized on flavor + build_type; track from config)

### android promote_to_production

```sh
[bundle exec] fastlane android promote_to_production
```

Promote to production on Google Play (internal → production for first-time apps; beta → production otherwise)

### android upload_android_screenshots

```sh
[bundle exec] fastlane android upload_android_screenshots
```

Upload Android screenshots + feature graphic to Google Play Store (all active locales)

### android sync_play_listing

```sh
[bundle exec] fastlane android sync_play_listing
```

Sync full Play Store listing (text + screenshots + feature graphic)

### android android_screenshots

```sh
[bundle exec] fastlane android android_screenshots
```

Alias for upload_android_screenshots

### android syncListing

```sh
[bundle exec] fastlane android syncListing
```

Sync Play Store listing (metadata + screenshots) — no build, no binary upload

----


## Mac

### mac buildNotarizedMacDmg

```sh
[bundle exec] fastlane mac buildNotarizedMacDmg
```

Tier-2 Apple-notarized macOS DMG → GitHub Releases (Developer ID via Match + notarytool via Fastlane notarize)

### mac desktop_testflight

```sh
[bundle exec] fastlane mac desktop_testflight
```

Build and upload macOS desktop build to TestFlight (Mac App Store track)

### mac create_mac_installer_cert

```sh
[bundle exec] fastlane mac create_mac_installer_cert
```

One-shot: create proper Mac Installer Distribution cert in Apple Developer Portal + push to Match repo.

### mac promoteMacToExternalBeta

```sh
[bundle exec] fastlane mac promoteMacToExternalBeta
```

Stage 1 → Stage 2 promotion: distribute an already-uploaded Mac TF build to external testers (no rebuild). Triggers Apple's beta review (~24h).

### mac syncMacListing

```sh
[bundle exec] fastlane mac syncMacListing
```

Sync Mac App Store listing (metadata + screenshots) — no binary upload, no submission (parity with ios upload_ios_screenshots).

### mac promoteMacToAppStore

```sh
[bundle exec] fastlane mac promoteMacToAppStore
```

Promote an existing Mac TestFlight build to Mac App Store review — no rebuild, no re-upload.

### mac desktop_release

```sh
[bundle exec] fastlane mac desktop_release
```

Full Mac App Store release — build PKG from source + deliver (use promoteMacToAppStore when a TestFlight build already exists)

### mac buildMacDmg

```sh
[bundle exec] fastlane mac buildMacDmg
```

Build unsigned macOS DMG and upload to GitHub Release (direct-distro Stage 1/2/3 via STAGE env)

----


## desktop

### desktop syncListing

```sh
[bundle exec] fastlane desktop syncListing
```

Sync Desktop store listings — no binary upload (GitHub Release + Mac App Store)

----


## iOS

### ios promoteToAppStore

```sh
[bundle exec] fastlane ios promoteToAppStore
```

Promote an existing TestFlight build to App Store review — no rebuild, no re-upload. Mirrors Android's promote_to_production.

### ios attachAppStoreSubscriptions

```sh
[bundle exec] fastlane ios attachAppStoreSubscriptions
```

Attach the app's subscriptions to the App Store review submission (no-op when there are none)

### ios uploadAppStore

```sh
[bundle exec] fastlane ios uploadAppStore
```

Upload an already-built IPA to App Store (skips build; use after release build succeeded but deliver failed)

### ios release

```sh
[bundle exec] fastlane ios release
```

Upload iOS application to App Store (parameterized on flavor + build_type; scheme from resolver)

### ios renewCerts

```sh
[bundle exec] fastlane ios renewCerts
```

Renew expired iOS Distribution certificate: revoke from Apple portal + create fresh adhoc + appstore certs

### ios renewAllCerts

```sh
[bundle exec] fastlane ios renewAllCerts
```

Full cross-platform cert renewal — all 4 cert types across all managed bundle IDs.

Covers:
  • appstore (iOS)                    — TestFlight + App Store (all app IDs)
  • adhoc (iOS)                       — Firebase App Distribution (all app IDs)
  • appstore (macOS)                  — Mac .app codesign (primary app ID)
  • mac_installer_distribution (macOS) — Mac .pkg signing (primary app ID)

Options:
  force:true          — renew even if certs are not near expiry (default false)
  type:TYPE           — all | appstore | adhoc | mac_installer_distribution (default all)

Run locally:
  cd deployment && bundle exec fastlane ios renewAllCerts
  cd deployment && bundle exec fastlane ios renewAllCerts force:true
  cd deployment && bundle exec fastlane ios renewAllCerts type:mac_installer_distribution

This is the local equivalent of openMF/ios-provisioning-profile cron (cert-renewal.yml).
See openMF/ios-provisioning-profile/cert-renewal.sh for a standalone bash runner
that also works outside of this Fastlane context (e.g. from ios-provisioning-profile).


### ios frame_ios_screenshots

```sh
[bundle exec] fastlane ios frame_ios_screenshots
```

Frame iOS screenshots with device bezels via frameit

### ios upload_ios_screenshots

```sh
[bundle exec] fastlane ios upload_ios_screenshots
```

Upload framed iOS screenshots to App Store Connect

### ios ios_screenshots

```sh
[bundle exec] fastlane ios ios_screenshots
```

Frame + upload iOS screenshots (generation is handled by /release)

### ios syncListing

```sh
[bundle exec] fastlane ios syncListing
```

OVERRIDE App Store listing TEXT (name/subtitle/keywords/description) from the promoted deck — no build, no submission. Screenshots are overridden separately by the deploy runtime via asc-upload-screenshots.rb (reliable clear-and-replace).

### ios uploadTestFlight

```sh
[bundle exec] fastlane ios uploadTestFlight
```

Upload an already-built IPA to TestFlight (skips build; use after beta build succeeded but pilot upload failed)

### ios beta

```sh
[bundle exec] fastlane ios beta
```

Upload beta build to TestFlight (parameterized on flavor + build_type; scheme from resolver)

### ios promoteToExternalBeta

```sh
[bundle exec] fastlane ios promoteToExternalBeta
```

Stage 1 → Stage 2 promotion: distribute an already-uploaded TF build to external testers (no rebuild, no re-upload). Triggers Apple's beta review (~24h).

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
