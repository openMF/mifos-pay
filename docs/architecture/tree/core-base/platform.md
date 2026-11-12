# `core-base/platform`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_PLATFORM.md`
> **Measured:** 25 Kotlin files, 2 test files

## Principal types

`AppContext`, `AppReviewManager`, `AppReviewManagerImpl`, `AppUpdateManager`, `AppUpdateManagerImpl`, `GarbageCollectionManager`, `GarbageCollectionManagerImpl`, `IntentManager`, `IntentManagerImpl`, `MimeType`, `ShareManager`, `ShareManagerImpl`, `UpdateOutcome`, `UrlLauncher`  …and 1 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/platform sha=958153c7f9ee4f423cc8ad64396279cc0b79a488 -->
## API reference

_Generated from `core-base/platform` at tree `958153c7f9ee` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/LocalManagerProviders.kt`

```kotlin
fun LocalManagerProvider(
```
Provides every platform manager to composition. This was an `expect fun` with androidMain and nonAndroidMain actuals.

```kotlin
val LocalAppReviewManager: ProvidableCompositionLocal<AppReviewManager> =
```
Prompts for an app-store review. Provided by `LocalManagerProvider`.

```kotlin
val LocalIntentManager: ProvidableCompositionLocal<IntentManager> =
```
Launches platform intents — view, pick, open settings. Provided by `LocalManagerProvider`.

```kotlin
val LocalUrlLauncher: ProvidableCompositionLocal<UrlLauncher> =
```
Opens URLs, email, maps, phone and SMS. Provided by `LocalManagerProvider`.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:138</code></summary>

```kotlin
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val urlLauncher = LocalUrlLauncher.current

    // System back steps to the previous page; on the first page it falls through to the system, which leaves
    // without saving — both choices stay off and onboarding shows again next launch.
    BackHandler(enabled = state.page != OnboardingPage.Overview) {
```

</details>

```kotlin
val LocalShareManager: ProvidableCompositionLocal<ShareManager> =
```
Shares text, URLs, files and images to other apps. Provided by `LocalManagerProvider`.

```kotlin
val LocalAppUpdateManager: ProvidableCompositionLocal<AppUpdateManager> =
```
Checks for and starts app updates. Provided by `LocalManagerProvider`.

```kotlin
val LocalClipboardManager: ProvidableCompositionLocal<ClipboardManager> =
```
Reads and writes the system clipboard, plus history, change observation and URL detection. Typed as the toolkit's `ClipboardManager` rather than a template wrapper — see the note in `platformModule`.

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/ui/MerchantScreen.kt:167</code></summary>

```kotlin
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val navController = rememberNavController()

    LazyColumn(
        modifier = modifier
```

</details>

```kotlin
val LocalBubbleManager: ProvidableCompositionLocal<Bubble> =
```
Shows floating bubbles, overlays and heads-up UI. Provided by `LocalManagerProvider`.

```kotlin
val LocalPdfManager: ProvidableCompositionLocal<PdfManager> =
```
Generates PDFs — statements, receipts, invoices. Provided by `LocalManagerProvider`.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/context/AppContext.kt`

```kotlin
expect abstract class AppContext
```
Represents an abstract context for the application that provides platform-specific functionality. This class must be implemented in each platform-specific source set.

<details><summary>Used in the template — <code>core-base/platform/src/androidMain/kotlin/kpt/core/base/platform/context/AppContext.android.kt:23</code></summary>

```kotlin
 * and other platform functionality.
 */
actual typealias AppContext = android.content.Context

/**
 * Android-specific implementation of LocalContext.
 *
```

</details>

```kotlin
expect val LocalContext: ProvidableCompositionLocal<AppContext>
```
A composition local that provides the current `AppContext` to the composition tree. This allows composable functions to access the platform-specific context without explicit parameters.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/androidMain/kotlin/kpt/feature/mpay/qr/scan/CameraPermissionState.android.kt:32</code></summary>

```kotlin
    val accPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val context = LocalContext.current
    val wrapper = remember(accPermissionState) {
        AccompanistPermissionWrapper(accPermissionState, context)
    }
```

</details>

```kotlin
expect val AppContext.activity: Any
```
The platform-specific activity or view controller associated with the current context. This property is accessible only from within a Composable function.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/di/PlatformModule.kt`

```kotlin
val ToolkitQualifier = named("kmptoolkit")
```
Qualifier for the toolkit bindings whose simple names collide with the template's own (`ShareManager`, `IntentManager`). Inject them with `koinInject<…>(ToolkitQualifier)`.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/LocalManagerProviders.kt:74</code></summary>

```kotlin
        // it exists so `rememberShareManager()` and `rememberAppIntentsManager()` resolve to the
        // app's managers. Qualified: see ToolkitQualifier in PlatformModule.
        ToolkitLocalShareManager provides koinInject<ToolkitShareManager>(ToolkitQualifier),
        ToolkitLocalIntentManager provides koinInject<ToolkitIntentManager>(ToolkitQualifier),
        LocalAppIntentsManager provides koinInject(),
    ) {
        content()
```

</details>

```kotlin
val platformModule = module
```
Koin module binding every `core-base/platform` capability — toast, share, URL launch, app update, garbage-collection hint — plus the per-target bindings from each `actual`. Include it once from the app graph.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/garbage/GarbageCollectionManager.kt`

```kotlin
interface GarbageCollectionManager
```
A hint to the platform that now is a reasonable moment to reclaim memory. A HINT, never a guarantee — no runtime here promises to collect on request.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/di/PlatformModule.kt:58</code></summary>

```kotlin

    single<CoroutineDispatcher> { Dispatchers.Unconfined }
    single<GarbageCollectionManager> { GarbageCollectionManagerImpl(get()) }

    // The three platform-capability managers. Bound here as well as provided through the
    // CompositionLocals in LocalManagerProviders, so a ViewModel or repository can inject one
    // without reaching into composition. All three are stateless — the per-target behaviour lives
```

</details>

- `fun tryCollect()` — Calls the garbage collector on the `Runtime` in an effort to clear the unused resources in the heap.

```kotlin
expect val garbageCollector: () -> Unit
```
The platform's collection hint, bound per target — `Runtime.getRuntime().gc()` on the JVM, a no-op where the runtime exposes no such control. Prefer the injectable `GarbageCollectionManager` at call sites; this exists for its `actual`s.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/garbage/GarbageCollectionManagerImpl.kt`

```kotlin
class GarbageCollectionManagerImpl(
```
Default `GarbageCollectionManager`: forwards to the platform `garbageCollector` on `dispatcher`, keeping a single in-flight job so repeated calls coalesce instead of queueing pauses.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/di/PlatformModule.kt:58</code></summary>

```kotlin

    single<CoroutineDispatcher> { Dispatchers.Unconfined }
    single<GarbageCollectionManager> { GarbageCollectionManagerImpl(get()) }

    // The three platform-capability managers. Bound here as well as provided through the
    // CompositionLocals in LocalManagerProviders, so a ViewModel or repository can inject one
    // without reaching into composition. All three are stateless — the per-target behaviour lives
```

</details>

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/intent/IntentManager.kt`

```kotlin
interface IntentManager
```
Asks the OS for a specific system screen or document flow, and reports what came back. ## Scope — system intents ONLY This interface used to also carry `launchUri` and the `share*` family, which conflated three different acts.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/LocalManagerProviders.kt:145</code></summary>

```kotlin

/** Launches platform intents — view, pick, open settings. Provided by [LocalManagerProvider]. */
val LocalIntentManager: ProvidableCompositionLocal<IntentManager> =
    managerCompositionLocal("LocalIntentManager")

/** Opens URLs, email, maps, phone and SMS. Provided by [LocalManagerProvider]. */
val LocalUrlLauncher: ProvidableCompositionLocal<UrlLauncher> =
```

</details>

- `suspend fun openAppSettings(): IntentResult` — Open this app's entry in system settings — the target for a denied-permission rationale.
- `suspend fun createDocument(fileName: String, mimeType: String = "*/*"): IntentResult` — Ask the OS for a save location, returning the chosen document's uri.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/intent/IntentManagerImpl.kt`

```kotlin
class IntentManagerImpl : IntentManager
```
The one `IntentManager`, for every target. `cmp-intent-launcher` carries the per-target `actual`s, so there is no source-set split here.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/di/PlatformModule.kt:66</code></summary>

```kotlin
    single<UrlLauncher> { UrlLauncherImpl() }
    single<ShareManager> { ShareManagerImpl() }
    single<IntentManager> { IntentManagerImpl() }

    // Bindable as a single since cmp-in-app-update replaced the Play Core impl: the engine
    // resolves the target itself, so there is no Activity to hold and nothing per-platform
    // to construct.
```

</details>

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/model/MimeType.kt`

```kotlin
enum class MimeType(val value: String, vararg val extensions: String)
```
Represents standardized MIME (Multipurpose Internet Mail Extensions) types for various file formats.

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrViewModel.kt:314</code></summary>

```kotlin
                    fileName = "qr_code.png",
                    bytes = data,
                    mime = MimeType.IMAGE,
                ),
            )
        }
    }
```

</details>

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/review/AppReviewManager.kt`

```kotlin
interface AppReviewManager
```
Manages application review requests across platforms. This interface abstracts the platform-specific implementations for requesting user reviews of the application.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/LocalManagerProviders.kt:141</code></summary>

```kotlin

/** Prompts for an app-store review. Provided by [LocalManagerProvider]. */
val LocalAppReviewManager: ProvidableCompositionLocal<AppReviewManager> =
    managerCompositionLocal("LocalAppReviewManager")

/** Launches platform intents — view, pick, open settings. Provided by [LocalManagerProvider]. */
val LocalIntentManager: ProvidableCompositionLocal<IntentManager> =
```

</details>

- `val capabilities: AppReviewCapabilities` — What review means on THIS target: `nativeInAppReview`, `storeListing`, and the derived `canRequestReview`.
- `val canRequestReview: Boolean` — Whether a review can actually be requested on THIS target right now. Ask before rendering a "Rate this app" affordance.
- `suspend fun promptForReview()` — Prompts the user to review the app using the platform's standard review flow. This method triggers the native system review prompt, which is typically managed by the platform to control frequency and prevent review fatigue.
- `fun promptForCustomReview()` — Prompts the user to review the app using a custom application-defined review flow.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/review/AppReviewManagerImpl.kt`

```kotlin
class AppReviewManagerImpl : AppReviewManager
```
The single implementation of `AppReviewManager`, for every target. Replaces the previous androidMain/nonAndroidMain pair.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/di/PlatformModule.kt:82</code></summary>

```kotlin
    // the global `AppReview` object directly, so nothing needs the toolkit's binding. A fork that
    // wants the toolkit module should pass its own listing: `includes(appReviewModule(listing))`.
    single<AppReviewManager> { AppReviewManagerImpl() }

    // Capabilities added with the 3.5.28 toolkit bump. Each library ships its OWN Koin module, so
    // they are INCLUDED rather than re-declared here — the toolkit owns what its bindings are, and
    // hand-rolling them is the same duplication-that-drifts problem as wrapping the libraries.
```

</details>

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/share/ShareManager.kt`

```kotlin
interface ShareManager
```
Hands content to the platform share chooser.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/LocalManagerProviders.kt:153</code></summary>

```kotlin

/** Shares text, URLs, files and images to other apps. Provided by [LocalManagerProvider]. */
val LocalShareManager: ProvidableCompositionLocal<ShareManager> =
    managerCompositionLocal("LocalShareManager")

/** Checks for and starts app updates. Provided by [LocalManagerProvider]. */
val LocalAppUpdateManager: ProvidableCompositionLocal<AppUpdateManager> =
```

</details>

- `suspend fun shareText(text: String)` — Share plain text.
- `suspend fun shareUrl(url: String)` — Share a URL as a link rather than as text, so receivers render a preview.
- `suspend fun shareFile(fileUri: String, mimeType: MimeType)` — Share a file by uri. `mimeType` decides which apps the chooser offers.
- `suspend fun shareFile(fileUri: String, mimeType: MimeType, extraText: String)` — Share a file alongside a message — one chooser, both payloads.
- `suspend fun shareImage(title: String, image: ImageBitmap)` — Share an in-memory image. Encoded to PNG on the way out; `title` names the file.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/share/ShareManagerImpl.kt`

```kotlin
class ShareManagerImpl : ShareManager
```
The one `ShareManager`, for every target. `cmp-share` carries the per-target `actual`s.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/di/PlatformModule.kt:65</code></summary>

```kotlin
    // in the toolkit engines they delegate to — so `single` is safe.
    single<UrlLauncher> { UrlLauncherImpl() }
    single<ShareManager> { ShareManagerImpl() }
    single<IntentManager> { IntentManagerImpl() }

    // Bindable as a single since cmp-in-app-update replaced the Play Core impl: the engine
    // resolves the target itself, so there is no Activity to hold and nothing per-platform
```

</details>

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/update/AppUpdateManager.kt`

```kotlin
interface AppUpdateManager
```
In-app update check, with real behaviour on every target.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/LocalManagerProviders.kt:157</code></summary>

```kotlin

/** Checks for and starts app updates. Provided by [LocalManagerProvider]. */
val LocalAppUpdateManager: ProvidableCompositionLocal<AppUpdateManager> =
    managerCompositionLocal("LocalAppUpdateManager")

/**
 * Reads and writes the system clipboard, plus history, change observation and URL detection.
```

</details>

- `suspend fun checkForAppUpdate(): UpdateOutcome` — Check for an update and, if one is available, start the flow.
- `suspend fun checkForResumeUpdateState(): UpdateOutcome` — Re-check after the app returns to the foreground, so an update the user backgrounded mid-flow is offered again. Call from the host's resume hook.
- `fun isSupported(): Boolean` — Whether this target can perform an in-app update at all.

```kotlin
sealed interface UpdateOutcome
```
What an update check concluded. Flattened from the engine's richer result type.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/update/AppUpdateManagerImpl.kt:33</code></summary>

```kotlin
) : AppUpdateManager {

    override suspend fun checkForAppUpdate(): UpdateOutcome {
        val result = AppUpdate.checkForUpdate(config)
        if (result !is UpdateResult.Success) return result.toOutcome()
        // Start it here rather than returning "available" and trusting the caller: a check that
        // reports availability and never offers the update is the failure this class exists to end.
```

</details>

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/update/AppUpdateManagerImpl.kt`

```kotlin
class AppUpdateManagerImpl(
```
The one `AppUpdateManager`, for every target. There is deliberately no `androidMain` / `nonAndroidMain` split: `cmp-in-app-update` carries the per-target `actual`s.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/di/PlatformModule.kt:71</code></summary>

```kotlin
    // resolves the target itself, so there is no Activity to hold and nothing per-platform
    // to construct.
    single<AppUpdateManager> { AppUpdateManagerImpl() }

    // AppReviewManager moved here once cmp-app-review replaced the Play-Core-plus-no-op pair —
    // it no longer takes an Activity, so it is a single like the rest instead of being
    // constructed inside composition.
```

</details>

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/url/UrlLauncher.kt`

```kotlin
interface UrlLauncher
```
Opens a URL in whatever the platform considers the right handler.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/LocalManagerProviders.kt:149</code></summary>

```kotlin

/** Opens URLs, email, maps, phone and SMS. Provided by [LocalManagerProvider]. */
val LocalUrlLauncher: ProvidableCompositionLocal<UrlLauncher> =
    managerCompositionLocal("LocalUrlLauncher")

/** Shares text, URLs, files and images to other apps. Provided by [LocalManagerProvider]. */
val LocalShareManager: ProvidableCompositionLocal<ShareManager> =
```

</details>

- `fun launch(url: String): Boolean` — Launch `url` with the platform's default handler. Returns false if nothing could handle it.
- `fun launchInBrowser(url: String): Boolean` — Launch `url` in a browser specifically, bypassing any app that claims the link.
- `fun launchInApp(url: String): Boolean` — Show `url` in a browser INSIDE the app — Custom Tabs on Android, SFSafariViewController on iOS, the system browser where the platform has no in-app one. For pages the user reads and returns from: a privacy policy, terms, help.
- `fun canLaunch(url: String): Boolean` — Whether `url` has a handler — check before offering the action, not after it fails.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/url/UrlLauncherImpl.kt`

```kotlin
class UrlLauncherImpl : UrlLauncher
```
The one `UrlLauncher`, for every target. `cmp-open-url` carries the per-target `actual`s, so there is no source-set split here.

<details><summary>Used in the template — <code>core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/di/PlatformModule.kt:64</code></summary>

```kotlin
    // without reaching into composition. All three are stateless — the per-target behaviour lives
    // in the toolkit engines they delegate to — so `single` is safe.
    single<UrlLauncher> { UrlLauncherImpl() }
    single<ShareManager> { ShareManagerImpl() }
    single<IntentManager> { IntentManagerImpl() }

    // Bindable as a single since cmp-in-app-update replaced the Play Core impl: the engine
```

</details>

---

_15 type(s), 33 function(s)/property(ies); 48 carry KDoc at source; 0 authored example(s); 19 live call site(s)._
<!-- api-docs:end -->
