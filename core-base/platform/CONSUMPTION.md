# Consuming `core-base/platform`

> Framework-shared platform bridges. `core/platform` re-exports this module with
> `api(projects.coreBase.platform)`, so a feature depends on `core/platform` and never on this one
> directly (`G-CORE-BASE-ENCAP`). The app root wires `LocalManagerProvider`.

## Call sequence

1. Wrap the app root with `LocalManagerProvider(LocalContext.current) { ... }` — it provides
   `LocalAppReviewManager`, `LocalIntentManager`, `LocalUrlLauncher`, `LocalShareManager` and
   `LocalAppUpdateManager` to the whole composition
   tree via `CompositionLocal`. The shipped call site is `cmp-shared/src/commonMain/kotlin/cmp/shared/SharedApp.kt`.
2. Read a manager from any composable, e.g. `val share = LocalShareManager.current`. The three
   capabilities are deliberately separate interfaces, because they are three different outcomes:
   - **`UrlLauncher`** — `open` / `openInBrowser` / `canOpen`. Sends the user to a handler.
   - **`ShareManager`** — `shareText` / `shareUrl` / `shareFile` / `shareImage`. Raises a chooser and
     hands content to another app.
   - **`IntentManager`** — `openAppSettings` / `createDocument`. Asks the OS and returns an
     `IntentResult`.

   They used to be one `IntentManager`, where `launchUri` was implemented with `Share.url(...)` — so
   "launch this URI" raised a share sheet instead of opening the link. Handling content shared *into* the app is not
   covered here — the `getShareDataFromIntent` this doc used to name has never existed in source.
3. Trigger the platform review prompt with `LocalAppReviewManager.current.promptForReview()`. Check
   for updates with `LocalAppUpdateManager.current.checkForAppUpdate()` — it suspends, so call it
   from a `LaunchedEffect` rather than bare in composition — and `checkForResumeUpdateState()` from
   your root Activity's `onResume` inside `lifecycleScope.launch { }`. Both return an
   `UpdateOutcome` (`UpToDate` / `UpdateStarted` / `Cancelled` / `NotSupported` / `Failed`).
4. Inject `GarbageCollectionManager` (bound in `platformModule` as `single<GarbageCollectionManager>`)
   and call `tryCollect()` after freeing large resources (e.g. large bitmap/file buffers).

## Notes

- `MimeType.fromExtension(ext)` / `fromFileName(name)` resolve a MIME type for `ShareManager.shareFile`.
- `UrlLauncherImpl` / `ShareManagerImpl` / `IntentManagerImpl` are **commonMain only** — the kmptoolkit
  engines (`cmp-open-url`, `cmp-share`, `cmp-intent-launcher`) carry the per-target `actual`s, so every
  platform gets real behaviour from one implementation. The sole split piece is `encodeImageAsPng`
  (bitmap encoding needs the platform codec).
- `AppUpdateManagerImpl` is **commonMain** too, over `cmp-in-app-update` (11 targets). It takes no
  `Activity` — that was Play Core's requirement, not the contract's — so it is a Koin `single` and
  the CompositionLocal reads it from there. `checkForAppUpdate` / `checkForResumeUpdateState` now
  **suspend** and return an `UpdateOutcome`; they used to be fire-and-forget, which is what let a
  no-op implementation pass for a working one.
- `AppReviewManagerImpl` is the **last** per-target pair whose non-Android body is a no-op: Android
  works via Play Core, everything else silently does nothing. It is also the last manager that takes
  an `Activity`, and therefore the only remaining reason `LocalManagerProvider` is `expect`/`actual`.
  The durable fix is a toolkit review engine; there is none yet.
- `AppContext` / `LocalContext` is the platform-agnostic Context handle: on Android it's
  `android.content.Context`; elsewhere it's a singleton placeholder object.
- `platformModule` also binds `CoroutineDispatcher` to `Dispatchers.Unconfined` — used by
  `GarbageCollectionManagerImpl`, not a general-purpose dispatcher for feature code.

Canonical example: `cmp-shared/src/commonMain/kotlin/cmp/shared/SharedApp.kt` wraps the whole app with
`LocalManagerProvider`.

Symbols: LocalManagerProvider, LocalIntentManager, LocalAppReviewManager, LocalAppUpdateManager, IntentManager, GarbageCollectionManager, MimeType
