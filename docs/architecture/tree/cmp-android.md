# `cmp-android/`

> **Kind:** Project area  
> **Measured:** 48 tracked files — 17× `.xml`, 10× `.webp`, 8× `.kt`, 3× `.txt`, 2× `.md`

## Shape

| Path | Files |
|---|---:|
| `cmp-android/src/` | 36 |
| (files at the root) | 10 |
| `cmp-android/dependencies/` | 2 |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `cmp-android/CONSUMPTION.md` | Consuming `cmp-android` in a fork |
| `cmp-android/README.md` |  |

<!-- tree-scaffold:end -->
## Significance

The Android application module: the `Activity`, the manifest, the flavor wiring and the committed
`google-services.json`.

**That Firebase file is PUBLIC, not a secret.** It carries project and app ids plus a client API key
that ships inside every APK; it is protected by Firebase Security Rules and App Check, not by secrecy.
It lives at the module root because Gradle's Google-Services plugin applies one shared config to every
flavor. Treating it as vault material — gitignoring it, routing it through `/secrets` — breaks the
build for no security gain.

**Flavors are `prod` and `demo`,** and the per-flavor endpoints, demo credentials and log tag are read
into `BuildConfig` by `KMPFlavorsConventionPlugin`. Adding a flavor dimension is a fork concern and
belongs in `build-logic`'s `LocalFlavors` hook, which ships empty with a worked example and is
`owner: fork` so a sync never overwrites it.
