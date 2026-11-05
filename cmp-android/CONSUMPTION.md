# Consuming `cmp-android` in a fork

`cmp-android` is the Android app-shell — `AndroidApp` (the `Application`) and `MainActivity`. Both
are thin: they call into `cmp-shared`'s `SharedApp`, which owns the actual UI/DI. A fork rarely
edits these files directly.

## What's here

- **`AndroidApp`** (`app/AndroidApp.kt`) — calls `initKoin { androidContext(this); androidLogger() }`
  on `onCreate`, restores the user's saved language (`UserDataRepository.userData.appLanguage`) to
  `AppCompatDelegate` before any `Activity` is created, and implements
  `SingletonImageLoader.Factory` for a disk-cached Coil `ImageLoader`.
- **`MainActivity`** (`app/MainActivity.kt`) — installs the splash screen, wires
  `AppUpdateManagerImpl`, `ShareUtils.setActivityProvider`, `FileKit.init`, and
  `AnalyticsHelper`/`AppLifecycleTracker`, then calls `SharedApp` with the platform callbacks
  (`updateScreenCapture`, `handleRecreate`, `handleThemeMode` → `AppCompatDelegate`,
  `handleAppLocale` → `AppCompatDelegate` + `Locale.setDefault`, `onSplashScreenRemoved`).
- **`BuildConfigUtils`**, **`ComponentActivityExtensions`**, **`ConfigurationExtension`**,
  **`AppThemeExtensions`** — small platform helpers these two classes lean on.

## What a fork touches

- **Identity / signing** — `applicationId`, versioning, and the release `signingConfigs` all read
  from `app-profile/` via the `appId`/`appDisplayName` version catalog entries and
  `resolveSecretPath("upload_keystore")` (see `build.gradle.kts`). Set identity in
  `app-profile/app.yaml`, run `./gradlew syncForkConfig`, and manage the keystore via
  `/secrets pull` — never hand-edit `build.gradle.kts` signing config.
- **Screen-capture policy** — `MainActivity.updateScreenCapture` defaults `FLAG_SECURE` on for
  release builds; adjust the `BuildConfig.DEBUG` bypass if your fork's threat model differs.
- **App behavior/UI** — add features via the `cmp-navigation` registries (`ForkFeatureRegistry`,
  `ForkBackboneRegistry`, `ForkTabRegistry`, `ForkInitializers`), not by editing `AndroidApp`/`MainActivity`.

## What to leave to sync

`AndroidApp`/`MainActivity`'s Koin bootstrap, splash-screen wiring, and `SharedApp` call are
template infrastructure — a template sync full-copies this module's shell files while your
`app-profile/` identity and registry registrations survive untouched.

See [`README.md`](README.md) for the module graph, and `cmp-navigation`'s
[`CONSUMPTION.md`](../cmp-navigation/CONSUMPTION.md) for how to add features.

Symbols: AndroidApp, MainActivity
