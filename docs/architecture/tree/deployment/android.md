# `deployment/android/`

> Part of [`deployment/`](../deployment.md)  
> **Measured:** 36 tracked files — 10× `.yaml`, 7× `.rb`, 6× `.md`, 6× `.yml`, 5× `.txt`

### Lanes

| Lane | Purpose |
|---|---|
| `android_screenshots` | Alias for upload_android_screenshots |
| `deployApkOnFirebase` | Publish Android APK to Firebase App Distribution (parameterized on flavor + build_type) |
| `deployInternal` | Deploy Android AAB to Google Play Store (parameterized on flavor + build_type; track from config) |
| `promoteToBeta` | Promote internal track → beta on Google Play (flavor-neutral by API; flavor passthrough for secrets) |
| `promoteToClosed` | Promote internal track to closed testing (alpha) on Google Play |
| `promote_to_production` | Promote to production on Google Play (internal → production for first-time apps; beta → production otherwise) |
| `syncListing` | Sync Play Store listing (metadata + screenshots) — no build, no binary upload |
| `sync_play_listing` | Sync full Play Store listing (text + screenshots + feature graphic) |
| `upload_android_screenshots` | Upload Android screenshots + feature graphic to Google Play Store (all active locales) |

### Contents

`app-content/`, `firebase/`, `metadata/`, `play-beta/`, `play-closed/`, `play-internal/`, `play-production/`, `screenshots/`, `sync-listing/`

### Docs in the tree

| Path | |
|---|---|
| `deployment/android/firebase/README.md` | deployment/android/firebase — Firebase App Distribution |
| `deployment/android/metadata/README.md` | android/media/ — Google Play Store listing graphics |
| `deployment/android/play-beta/README.md` | deployment/android/play-beta — Play Store beta track (promotion) |
| `deployment/android/play-closed/README.md` | deployment/android/play-closed — Play Store closed testing / alpha track (promotion) |
| `deployment/android/play-internal/README.md` | deployment/android/play-internal — Play Store internal track |
| `deployment/android/play-production/README.md` | deployment/android/play-production — Play Store production track (promotion) |

<!-- tree-scaffold:end -->
## Significance

Play Store and Firebase App Distribution. Two flavors (`prod`, `demo`) × two build types, each a
separate artifact, and the lanes are parameterised on the pair rather than duplicated per combination.

**Two keystores, and the distinction matters.** ORIGINAL signs the app; UPLOAD is what Play Console
accepts. Losing the ORIGINAL key means a fork can never update its own listing again — which is why
`keystores/` is gitignored, the vault holds the only other copy, and the badging check exists to catch
a manifest surface changing under you.

**The track ladder is in config, not in the lanes:** `internal → beta → production`, with an optional
staged-rollout fraction used only at production. `promote_to_production` moves an existing beta build;
it does not rebuild, so what ships is byte-identical to what was reviewed.
