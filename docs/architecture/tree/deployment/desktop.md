# `deployment/desktop/`

> Part of [`deployment/`](../deployment.md)  
> **Measured:** 93 tracked files — 35× `.txt`, 14× `.yaml`, 10× `.png`, 8× `.md`, 7× `.yml`

### Lanes

| Lane | Purpose |
|---|---|
| `buildMacDmg` | Build unsigned macOS DMG and upload to GitHub Release (direct-distro Stage 1/2/3 via STAGE env) |
| `buildNotarizedMacDmg` | Tier-2 Apple-notarized macOS DMG → GitHub Releases (Developer ID via Match + notarytool via Fastlane notarize) |
| `create_mac_installer_cert` | One-shot: create proper Mac Installer Distribution cert in Apple Developer Portal + push to Match repo. |
| `desktop_release` | Full Mac App Store release — build PKG from source + deliver (use promoteMacToAppStore when a TestFlight build already exists) |
| `desktop_testflight` | Build and upload macOS desktop build to TestFlight (Mac App Store track) |
| `macos_screenshots` | Upload macOS screenshots (generation is handled by /release) |
| `promoteMacToAppStore` | Promote an existing Mac TestFlight build to Mac App Store review — no rebuild, no re-upload. |
| `promoteMacToExternalBeta` | Stage 1 → Stage 2 promotion: distribute an already-uploaded Mac TF build to external testers (no rebuild). Triggers Apple's beta review (~24h). |
| `syncListing` | Sync Desktop store listings — no binary upload (GitHub Release + Mac App Store) |
| `syncMacListing` | Sync Mac App Store listing (metadata + screenshots) — no binary upload, no submission (parity with ios upload_ios_screenshots). |
| `upload_macos_screenshots` | Upload macOS screenshots to Mac App Store via deliver |

### Contents

`dmg-notarized/`, `linux-deb/`, `mac-app-store/`, `macos-dmg-unsigned/`, `microsoft-store/`, `msi-signed/`, `sync-listing/`, `windows-exe/`

### Docs in the tree

| Path | |
|---|---|
| `deployment/desktop/dmg-notarized/README.md` | deployment/desktop/dmg-notarized — Notarized macOS DMG |
| `deployment/desktop/linux-deb/README.md` | deployment/desktop/linux-deb — GitHub Releases (Linux DEB) |
| `deployment/desktop/mac-app-store/README.md` | deployment/desktop/mac-app-store — Mac App Store |
| `deployment/desktop/macos-dmg-unsigned/README.md` | deployment/desktop/macos-dmg-unsigned — GitHub Releases (macOS DMG, unsigned) |
| `deployment/desktop/microsoft-store/README.md` | deployment/desktop/microsoft-store — Microsoft Store |
| `deployment/desktop/msi-signed/README.md` | deployment/desktop/msi-signed — Signed Windows MSI (Azure Trusted Signing) |
| `deployment/desktop/windows-exe/README.md` | deployment/desktop/windows-exe — GitHub Releases (Windows EXE) |

<!-- tree-scaffold:end -->
## Significance

Three operating systems from one Gradle task. `packageReleaseDistributionForCurrentOS` runs on a
matrix and produces EXE + MSI on Windows, DMG on macOS, DEB on Linux — which is why `deployment/linux/`
and `deployment/windows/` hold metadata but no lanes of their own: they are targets of this platform,
not platforms beside it, exactly as `DEPLOYMENT_MANIFEST.yaml` declares.

**macOS is the complicated one.** It has two distinct paths: a notarised DMG for direct download
(Developer ID + hardened runtime + `notarytool`), and a `.pkg` for the Mac App Store (a different
certificate, a sandbox, and an entitlements file). Both draw certificates from the same Match
repository as iOS, so CI needs no `.p12` in a secret.

Hardened runtime is a notarisation *requirement*, not a hardening option — a DMG signed without it is
rejected by the notary service after the upload completes.
