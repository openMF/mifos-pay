# `core-base/ui`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_UI.md`
> **Measured:** 80 Kotlin files, 14 test files

**Defines annotations:** `@FeatureDestination`

## Principal types

`AppInfo`, `BackgroundEvent`, `BaseMutationViewModel`, `BaseViewModel`, `DashboardProgressState`, `DefaultLottieAnimations`, `DisplayState`, `DraftPickerItem`, `FeatureDestination`, `FormDraftTestTags`, `FreshnessTint`, `FreshnessVisual`, `KptFadeThrough`, `KptSharedAxis`  …and 16 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/ui sha=401a8d9b378807f018895a5926c5a38863e1701c -->
## API reference

_Generated from `core-base/ui` at tree `401a8d9b3788` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/AppInfo.kt`

```kotlin
object AppInfo
```
The SINGLE common-code accessor for the app's user-facing display name.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/HomeScreen.kt:61</code></summary>

```kotlin
                        // App title from the common AppInfo.appDisplayName accessor (BuildKonfig →
                        // fork.properties#app.display.name), never a hardcoded brand string.
                        text = AppInfo.appDisplayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
```

</details>

- `val appDisplayName: String get() = BuildKonfig.APP_DISPLAY_NAME` — The fork's display name, from `app-profile` via BuildKonfig — never hardcoded, so a rebrand needs no code change.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/GestureDetector.kt`

```kotlin
fun Modifier.detectMultiTapGesture(
```
Modifier that detects a multi-tap gesture within a configurable timeout window.

<details><summary>Example</summary>

```kotlin
Text(
    text = "Tap me 5 times!",
    modifier = Modifier
        .detectMultiTapGesture {
            // Show hidden feature
            showDebugMenu = true
        }
)
Text(
    text = "Tap me 3 times quickly!",
    modifier = Modifier
        .detectMultiTapGesture(
            tapCount = 3,
            tapTimeoutMs = 500L,
        ) {
            // Show hidden feature
        }
)
```

</details>

```kotlin
fun Modifier.detectLongPressGesture(
```
Modifier that detects a long press gesture.

<details><summary>Example</summary>

```kotlin
Box(
    modifier = Modifier
        .detectLongPressGesture {
            // Handle long press
        }
)
```

</details>

```kotlin
fun Modifier.detectDoubleTapGesture(
```
Modifier that detects a double tap gesture.

<details><summary>Example</summary>

```kotlin
Image(
    modifier = Modifier
        .detectDoubleTapGesture {
            // Handle double tap (e.g., like action)
        }
)
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/KptConnectivityBanner.kt`

```kotlin
fun KptConnectivityBanner(
```
Thin wrapper around `ConnectivityBanner` from `cmp-network-monitor-compose`.

<details><summary>Used in the template — <code>core-base/ui/src/androidInstrumentedTest/kotlin/kpt/core/base/ui/KptConnectivityBannerTest.kt:49</code></summary>

```kotlin
    fun bannerNotVisible_whenOnline() {
        rule.setContent {
            KptConnectivityBanner(
                monitor = FakeConnectivityMonitor(online = true),
                debounceMs = 0L,
            )
        }
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/captiveportal/CaptivePortalLauncher.kt`

```kotlin
expect fun rememberOpenCaptivePortalSignIn(): () -> Unit
```
Returns a stable `() -> Unit` lambda that opens the platform-native captive-portal sign-in flow. Per-platform behaviour: - **Android** — fires `android.settings.WIFI_SETTINGS` via `startActivity`.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/dashboard/DashboardLoadingProgress.kt`

```kotlin
fun Flow<List<ScreenState<*>>>.aggregateDashboardProgress(): Flow<DashboardProgressState> =
```
Fan-in operator that converts a stream of per-card states into a single `DashboardProgressState` suitable for top-of-screen progress display.

<details><summary>Example</summary>

```kotlin
val cards: StateFlow<List<ScreenState<*>>> = combine(
    loansStream.state,
    billsStream.state,
    ratesStream.state,
    fxStream.state,
) { l, b, r, f -> listOf(l, b, r, f) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

val progress: StateFlow<DashboardProgressState> = cards
    .aggregateDashboardProgress()
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardProgressState(0, 0, false, false, false),
    )
DashboardProgressBar(state = progressState)
IndependentCardLayout(states = cards, onRetry = vm::onRetryCard) { i, content -> /* ... */ }
```

</details>

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:107</code></summary>

```kotlin
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

```kotlin
fun List<ScreenState<*>>.toDashboardProgressState(): DashboardProgressState =
```
Pure conversion from a list of per-card states to a single `DashboardProgressState`. Exposed so tests + non-Flow call sites can re-use the aggregation contract.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:88</code></summary>

```kotlin
                    // dedicated Empty ViewState variant exists here).
                    AccountState.ViewState.Content(
                        accounts = emptyList(),
                        beneficiaries = emptyList(),
                    )

                is ScreenState.NoNetwork ->
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/dashboard/DashboardProgressBar.kt`

```kotlin
fun DashboardProgressBar(
```
Top-of-dashboard **freshness** strip: shows *when* the data was last loaded — "Updated 5m ago" — NOT a "2 of 4 loaded" count.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/dashboard/DashboardProgressState.kt`

```kotlin
data class DashboardProgressState(
```
Aggregate progress snapshot for a multi-card dashboard.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/dashboard/DashboardLoadingProgress.kt:45</code></summary>

```kotlin
 * ```
 */
fun Flow<List<ScreenState<*>>>.aggregateDashboardProgress(): Flow<DashboardProgressState> =
    map { states -> states.toDashboardProgressState() }

/**
 * Pure conversion from a list of per-card states to a single [DashboardProgressState].
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/dashboard/IndependentCardLayout.kt`

```kotlin
fun <T> IndependentCardLayout(
```
Renders a vertical stack of cards where each card carries its OWN `ScreenState` — so a slow card can still spin while a fast one shows content, and a single failed fetch only ruins one card instead of blanking the whole dashboard.

```kotlin
fun <T> IndependentCardLayout(
```
Direct `ScreenDataStream` overload of `IndependentCardLayout` — one Store5 read stream per independent card.

<details><summary>Example</summary>

```kotlin
IndependentCardLayout(
    streams = listOf(viewModel.loansStream, viewModel.billsStream, viewModel.ratesStream),
) { index, data, _ -> DashboardCard(index, data) }
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/draft/DraftPicker.kt`

```kotlin
fun DraftPickerList(
```
Full-screen picker for the **multi-pending** case — a form that can hold N concurrent drafts (one per `uniqueKey`, e.g. per-loan or per-bill).

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsPreview.kt:74</code></summary>

```kotlin
internal fun DraftPickerListPreview() {
    KptTheme {
        DraftPickerList(
            items = listOf(
                DraftPickerItem(1, "Rent · ₹1,200", "Saved offline — failed to sync", isFailed = true),
                DraftPickerItem(2, "Electricity", "Pending sync"),
                DraftPickerItem(3, "Internet · ₹800", "Pending sync"),
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/draft/DraftPickerItem.kt`

```kotlin
data class DraftPickerItem(
```
One selectable draft in the `DraftPickerList` — a **UI value object**, deliberately independent of the store/database types so this framework primitive never leaks `core-base/store` into its public API (consumers map their `DraftRecord` / outbox entry to this).

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsPreview.kt:76</code></summary>

```kotlin
        DraftPickerList(
            items = listOf(
                DraftPickerItem(1, "Rent · ₹1,200", "Saved offline — failed to sync", isFailed = true),
                DraftPickerItem(2, "Electricity", "Pending sync"),
                DraftPickerItem(3, "Internet · ₹800", "Pending sync"),
            ),
            onResume = {},
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/draft/DraftResolutionPrompt.kt`

```kotlin
fun DraftResolutionPrompt(
```
The **three-case** draft-resolution prompt shown when a user re-opens a form that already has a saved draft.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/effects/EventsEffect.kt`

```kotlin
fun <E> EventsEffect(
```
Convenience method for observing event flow from `BaseViewModel`. By default, events will only be consumed when the associated screen is resumed, to avoid bugs like duplicate navigation calls.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:110</code></summary>

```kotlin
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is AccountEvent.OnAddEditSavingsAccount -> {
                onAddEditSavingsAccount(event.type)
            }
```

</details>

```kotlin
fun <E> EventsEffect(
```
Convenience method for observing event flow from `BaseViewModel`. By default, events will only be consumed when the associated screen is resumed, to avoid bugs like duplicate navigation calls.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:110</code></summary>

```kotlin
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is AccountEvent.OnAddEditSavingsAccount -> {
                onAddEditSavingsAccount(event.type)
            }
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/effects/LifecycleEventEffect.kt`

```kotlin
fun LivecycleEventEffect(
```
Creates a side effect to observe lifecycle events.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/utils/LifecycleEventEffect.kt:24</code></summary>

```kotlin
 */
@Composable
fun LivecycleEventEffect(
    onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit,
) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/effects/ReportDrawnExt.kt`

```kotlin
expect fun ReportDrawnWhen(block: () -> Boolean)
```
Reports to the composition system that content is considered drawn when the specified condition is true. Platform-specific implementation that affects rendering optimizations.

<details><summary>Used in the template — <code>core-base/ui/src/androidMain/kotlin/kpt/core/base/ui/effects/ReportDrawnExt.android.kt:16</code></summary>

```kotlin
/** `ReportDrawnWhen` on Android. */
@Composable
actual fun ReportDrawnWhen(block: () -> Boolean) {
    androidx.activity.compose.ReportDrawnWhen { block() }
}
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/freshness/ErrorCategoryMessages.kt`

```kotlin
fun ErrorCategory.toShortMessage(): String = when (this)
```
Short (≤ 20 chars) message per `ErrorCategory` variant — used by `FreshnessIndicator` tooltip titles and `RefreshStateChip` labels.

```kotlin
fun ErrorCategory.toLongMessage(): String = when (this)
```
Longer descriptive message per `ErrorCategory` variant — used by `FreshnessIndicator` `RichTooltip` body text. `KptErrorMapper.mapErrorToUserMessage` delegates here so the message catalogue lives in one place.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/freshness/FreshnessIndicator.kt`

```kotlin
fun FreshnessIndicator(
```
Per-card freshness indicator: a small Material 3 info / clock / warning icon anchored to a `TooltipBox`.

<details><summary>Used in the template — <code>feature/pocket/src/commonMain/kotlin/kpt/feature/pocket/screens/ManagePocketScreen.kt:250</code></summary>

```kotlin
                            )
                            Spacer(modifier = Modifier.width(KptTheme.spacing.xs))
                            FreshnessIndicator(
                                signal = linkedFreshness,
                                onRefresh = { onAction(ManagePocketAction.Retry) },
                                modifier = Modifier.size(24.dp),
                            )
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/freshness/FreshnessStrings.kt`

```kotlin
fun humanizeDuration(d: Duration): String = when
```
Humanises a duration into a "X ago" tooltip phrase for `FreshnessIndicator`.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/freshness/RefreshStateChip.kt`

```kotlin
fun RefreshStateChip(
```
Inline dismissible "we're showing previous data" chip for **input-type screens** (Rate History period/currency picker, etc.) where the previous selection's data is preserved as a stale fallback while the new key's fetch is in-flight or has failed. Renders nothing unless `signal`.band is `FreshnessBand.VeryStale` — i.e. the preserved data is significantly out of date OR a fetch error has occurred. The chip's label categorises the error via `categorize` when `signal`.lastError is non-null ("No network · Showing previous data ↺", "Server error · Showing previous data ↺", etc.); falls back to a generic age-based label otherwise. Pair this with the per-card `FreshnessIndicator` on the title: the indicator surfaces the staleness, the chip explains it inline next to the content.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/motion/KptFadeThrough.kt`

```kotlin
object KptFadeThrough
```
Fade-through enter/exit factories. Use for **sibling navigation** — bottom-nav tab switches, settings section switches, paged tabs.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/motion/NavTransitions.kt:65</code></summary>

```kotlin
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    val enter = KptFadeThrough.enter()
    val exit = KptFadeThrough.exit()
    composable<T>(
        enterTransition = { enter },
        exitTransition = { exit },
```

</details>

- `fun enter(): EnterTransition = enter(MaterialTheme.motion)` — Fade-through enter using the ambient theme motion. Composable — call from composition.
- `fun enter(motion: Motion): EnterTransition = fadeIn(` — Fade-through enter using an explicit `motion`, so it is callable outside composition.
- `fun exit(): ExitTransition = exit(MaterialTheme.motion)` — Fade-through exit using the ambient theme motion.
- `fun exit(motion: Motion): ExitTransition = fadeOut(` — Fade-through exit using an explicit `motion`.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/motion/KptListItemEnter.kt`

```kotlin
fun Modifier.kptListItemEnter(
```
Staggered fade + translate-Y enter modifier for `LazyColumn` / `LazyRow` items.

<details><summary>Example</summary>

```kotlin
LazyColumn {
    itemsIndexed(loans) { index, loan ->
        LoanRowCard(
            loan,
            modifier = Modifier.kptListItemEnter(index),
        )
    }
}
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/motion/KptRefreshingPulse.kt`

```kotlin
fun Modifier.kptRefreshingPulse(active: Boolean): Modifier = composed
```
Infinite scale + alpha "breath" while `active` is true. Use to signal **"this surface is fetching live data right now"** — freshness dots, inline refresh badges, etc.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/motion/KptSharedAxis.kt`

```kotlin
object KptSharedAxis
```
Shared-axis-X enter/exit factories, per Material Motion. Use for **forward/back navigation in a single stack** — pushing onto the stack slides left-and-fades, popping reverses.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:211</code></summary>

```kotlin
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        KptSharedAxis.enterForward(motion) togetherWith KptSharedAxis.exitForward(motion)
                    } else {
                        KptSharedAxis.enterBack(motion) togetherWith KptSharedAxis.exitBack(motion)
                    }
                },
```

</details>

- `fun enterForward(): EnterTransition = enterForward(MaterialTheme.motion)` — Forward push — new screen slides in from the right, old screen slides out left.
- `fun enterForward(motion: Motion): EnterTransition` — Shared-axis enter for moving forward/deeper in a flow.
- `val slide = motion.sharedAxisSlideDistance`
- `fun exitForward(): ExitTransition = exitForward(MaterialTheme.motion)` — Forward push — companion exit (the popped/replaced screen).
- `fun exitForward(motion: Motion): ExitTransition` — Shared-axis exit for moving forward/deeper in a flow.
- `val slide = motion.sharedAxisSlideDistance`
- `fun enterBack(): EnterTransition = enterBack(MaterialTheme.motion)` — Back/pop — new screen slides in from the left, mirroring `enterForward`.
- `fun enterBack(motion: Motion): EnterTransition` — Shared-axis enter for moving back.
- `val slide = motion.sharedAxisSlideDistance`
- `fun exitBack(): ExitTransition = exitBack(MaterialTheme.motion)` — Back/pop — companion exit (the screen being popped).
- `fun exitBack(motion: Motion): ExitTransition` — Shared-axis exit for moving back.
- `val slide = motion.sharedAxisSlideDistance`

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/nav/FeatureDestination.kt`

```kotlin
public annotation class FeatureDestination
```
Marks a `NavGraphBuilder` extension as a TOP-LEVEL feature destination — one the app shell registers directly on the authenticated graph.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/nav/SafeNavController.kt`

```kotlin
fun NavController.popBackStackSafely()
```
Pops the back stack only when the current destination is fully `Lifecycle.State.RESUMED`.

<details><summary>Example</summary>

```kotlin
composableWithPushTransitions<MyRoute> {
    MyScreen(onBackClick = { navController.popBackStackSafely() })
}
```

</details>

```kotlin
fun NavController.rememberSafeBackPress(debounceMs: Long = 500L): () -> Unit
```
Returns a stable lambda that guards `NavController.popBackStack` with two layers: 1. **Lifecycle guard** — only fires when the current destination is `Lifecycle.State.RESUMED`.

<details><summary>Example</summary>

```kotlin
@Composable
fun MyScreen(navController: NavController) {
    val onBack = navController.rememberSafeBackPress()
    KptTopAppBar(title = "…", onNavigationIconClick = onBack)
}
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/paging/LoadMoreFooter.kt`

```kotlin
fun <T : Any> LoadMoreFooter(
```
Footer for paginated lists driven by a `PagingScreenStream`. Place as the last item in a LazyColumn. Shows loading indicator, error with retry, or end-of-list.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/paging/PagingScreenContent.kt:162</code></summary>

```kotlin
            lazyContent(items)
            item {
                LoadMoreFooter(
                    isLoadingMore = isLoadingMore,
                    hasMore = hasMore,
                    loadMoreError = loadMoreError,
                    onRetry = pagingStream::loadNextPage,
```

</details>

```kotlin
fun LoadMoreFooter(
```
Footer overload taking raw state values. Lets screens that drive paging without exposing `PagingScreenStream` directly still use the canonical footer rendering.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/paging/PagingScreenContent.kt:162</code></summary>

```kotlin
            lazyContent(items)
            item {
                LoadMoreFooter(
                    isLoadingMore = isLoadingMore,
                    hasMore = hasMore,
                    loadMoreError = loadMoreError,
                    onRetry = pagingStream::loadNextPage,
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/paging/LoadMoreTrigger.kt`

```kotlin
fun rememberLoadMoreTrigger(
```
Returns a `State` that flips to `true` when the user has scrolled within `threshold` items of the end of the list AND more pages are available AND a load is not already in flight. Reads `listState.layoutInfo.totalItemsCount` directly.

<details><summary>Example</summary>

```kotlin
val shouldLoadMore by rememberLoadMoreTrigger(listState, hasMore, isLoadingMore)
LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) viewModel.onLoadMore() }
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/paging/PagingScreenContent.kt`

```kotlin
fun <T : Any> PagingScreenContent(
```
`ScreenContent` variant for paginated lists — slot-only overload for screens with custom layouts (sticky headers, sectioned lists, etc.).

<details><summary>Example</summary>

```kotlin
PagingScreenContent(
    pagingStream = viewModel.pagingStream,
    onRetry = viewModel::retry,
) { items, freshness ->
    LazyColumn {
        items(items) { item -> ItemRow(item) }
        item { LoadMoreFooter(pagingStream = viewModel.pagingStream) }
    }
}
```

</details>

```kotlin
fun <T : Any> PagingScreenContent(
```
Recommended `PagingScreenContent` overload — owns the `LazyColumn`, `LoadMoreFooter`, and load-more trigger. Screens just provide the per-item lazy content. Use this for "infinite scrolling list" screens.

<details><summary>Example</summary>

```kotlin
PagingScreenContent(
    pagingStream = viewModel.pagingStream,
    onRetry = viewModel::onRetry,
) { coins ->
    items(coins) { coin -> CoinItem(coin = coin) }
}
```

</details>

```kotlin
fun <T : Any> PagingScreenContent(
```
Non-stream (Store5-optional) `PagingScreenContent` overload — a caller with an already-materialized `List<T>` (static content, a test fixture, or a non-Store5 source) renders through the SAME paging chrome (owned `LazyColumn` + empty-state slot) without being forced to construct a `PagingScreenStream`. There are no further pages to fetch, so the `LoadMoreFooter` and load-more trigger are intentionally omitted — the whole list is present. The trailing `lazyContent` lambda is signature-identical to the recommended stream overload above, so a screen upgrades from a static list to a live Store5 paging stream by swapping `items = staticList` for `pagingStream = viewModel.pagingStream` with ZERO change to its per-item content (the D2 dual-input, Store5-optional contract). Usage: ``` PagingScreenContent(items = uiState.coins) { coins -> items(coins) { coin -> CoinItem(coin = coin) } } ```

<details><summary>Example</summary>

```kotlin
PagingScreenContent(items = uiState.coins) { coins ->
    items(coins) { coin -> CoinItem(coin = coin) }
}
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/screen/DataFreshnessIndicator.kt`

```kotlin
fun DataFreshnessIndicator(
```
Compact updating banner shown above content when a background refresh is in flight.

<details><summary>Used in the template — <code>core-base/store/src/commonTest/kotlin/kpt/core/base/store/paging/PagingScreenStreamOfflineTest.kt:188</code></summary>

```kotlin
        assertNotNull(
            contentState.fetchedAt,
            "Successful page load must set fetchedAt so DataFreshnessIndicator shows the timestamp.",
        )
    }
}
```

</details>

```kotlin
fun DefaultRefreshingBanner(fetchedAt: Instant? = null)
```
Default refreshing banner used as the initial value of `ScreenContent`'s `refreshingIndicator` slot. Pass `null` for the slot to suppress all in-flight progress UI.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/screen/ScreenContent.kt:86</code></summary>

```kotlin
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    refreshingIndicator: (@Composable () -> Unit)? = { DefaultRefreshingBanner() },
    onDismiss: (() -> Unit)? = null,
    loading: @Composable () -> Unit = { DefaultLoadingContent() },
    empty: @Composable () -> Unit = { DefaultEmptyContent() },
    noNetwork: @Composable (isCaptivePortal: Boolean) -> Unit = { captive ->
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/screen/DefaultLottieAnimations.kt`

```kotlin
object DefaultLottieAnimations
```
Convenience suspend loaders for the bundled default Lottie animations that ship with `core-base/ui`.

<details><summary>Example</summary>

```kotlin
ScreenStateEmpty(
    visual = ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.empty),
)
ScreenStateVisual.Lottie(spec = {
    LottieCompositionSpec.JsonString(MyAppRes.readBytes("anim.json").decodeToString())
})
```

</details>

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/KptScreenStateDefaults.kt:86</code></summary>

```kotlin
            loading = ScreenStateLoading.Skeleton(rowCount = 5),
            empty = ScreenStateEmpty(
                visual = ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.empty),
                title = emptyTitle,
                message = emptyMessage,
            ),
            error = ScreenStateError(
```

</details>

- `val empty: suspend () -> LottieCompositionSpec = { loadJson("files/screenstate/empty.json") }` — Animation for the empty state.
- `val error: suspend () -> LottieCompositionSpec = { loadJson("files/screenstate/error.json") }` — Animation for the error state.
- `val noNetwork: suspend () -> LottieCompositionSpec = { loadJson("files/screenstate/no_network.json") }` — Animation for the no-network state.
- `val loading: suspend () -> LottieCompositionSpec = { loadJson("files/screenstate/loading.json") }` — Animation for the loading state.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/screen/RefreshableScreenContent.kt`

```kotlin
fun <T> RefreshableScreenContent(
```
Direct `ScreenDataStream` overload — the ViewModel exposes the repository-built stream and Compose collects `stream.state` lifecycle-aware here, wiring the pull gesture to `stream.refresh()`. Mirrors `ScreenContent`'s stream overload.

<details><summary>Example</summary>

```kotlin
RefreshableScreenContent(stream = viewModel.rates) { rates, _ -> RatesList(rates) }
```

</details>

<details><summary>Used in the template — <code>core-base/ui/src/commonTest/kotlin/kpt/core/base/ui/screen/RefreshableScreenContentUiTest.kt:45</code></summary>

```kotlin
    fun aCallersEmptySlotIsRendered() = runComposeUiTest {
        setContent {
            RefreshableScreenContent(
                state = ScreenState.Empty,
                onRefresh = {},
                empty = { Text("none yet", modifier = Modifier.testTag(CUSTOM)) },
                content = content,
```

</details>

```kotlin
fun <T> RefreshableScreenContent(
```
Non-stream (Store5-optional) overload — a caller with a plain `ScreenState` value (static content, a test fixture, a projected/combined payload the ViewModel owns) renders through the SAME `PullToRefreshBox` chrome without being forced to construct a `ScreenDataStream`. The refreshing indicator tracks `ScreenState.Content.freshnessSignal``.isRefreshing`. ```kotlin RefreshableScreenContent(state = uiState, onRefresh = viewModel::onRefresh) { data, _ -> … } ```

<details><summary>Example</summary>

```kotlin
RefreshableScreenContent(state = uiState, onRefresh = viewModel::onRefresh) { data, _ -> … }
```

</details>

<details><summary>Used in the template — <code>core-base/ui/src/commonTest/kotlin/kpt/core/base/ui/screen/RefreshableScreenContentUiTest.kt:45</code></summary>

```kotlin
    fun aCallersEmptySlotIsRendered() = runComposeUiTest {
        setContent {
            RefreshableScreenContent(
                state = ScreenState.Empty,
                onRefresh = {},
                empty = { Text("none yet", modifier = Modifier.testTag(CUSTOM)) },
                content = content,
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/screen/ScreenContent.kt`

```kotlin
fun <T> ScreenContent(
```
Generic composable that renders any `ScreenState` with sensible defaults. Animates between states with a fade transition. When `ScreenState.Content.isRefreshing` is true, invokes the `refreshingIndicator` slot above the content area.

<details><summary>Example</summary>

```kotlin
@Composable
fun SavingsScreen(vm: SavingsViewModel) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    ScreenContent(state = state, onRetry = vm::onRetry) { data, freshness ->
        SavingsList(data.accounts)
    }
}
```

</details>

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/list/BeneficiaryListScreen.kt:179</code></summary>

```kotlin
        // the stream's pre-decided `ScreenState`. Only the Content list body is
        // authored here; the empty state keeps the feature's existing copy.
        ScreenContent(
            state = state,
            onRetry = onRetry,
            modifier = Modifier
                .padding(paddingValues)
```

</details>

```kotlin
fun <T> ScreenContent(
```
Direct `ScreenDataStream` overload — the ViewModel exposes the stream (built in the repository) and Compose consumes it here: `stream.state` is collected lifecycle-aware and retry is wired to `stream.retry()`.

<details><summary>Example</summary>

```kotlin
// ViewModel:  val alerts = repository.alertsStream(viewModelScope)   // just the stream
ScreenContent(stream = viewModel.alerts) { alerts, _ -> LazyColumn { … } }
```

</details>

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/list/BeneficiaryListScreen.kt:179</code></summary>

```kotlin
        // the stream's pre-decided `ScreenState`. Only the Content list body is
        // authored here; the empty state keeps the feature's existing copy.
        ScreenContent(
            state = state,
            onRetry = onRetry,
            modifier = Modifier
                .padding(paddingValues)
```

</details>

```kotlin
fun DefaultLoadingContent(
```
The framework's loading rendering — spinner, skeleton or branded animation, per `config`.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/dashboard/IndependentCardLayout.kt:98</code></summary>

```kotlin
                        { loading(index) }
                    } else {
                        { DefaultLoadingContent() }
                    },
                    empty = if (empty != null) {
                        { empty(index) }
                    } else {
```

</details>

```kotlin
fun DefaultEmptyContent(
```
The framework's empty rendering — for a successful read that returned nothing. Distinct from error on purpose: "no results" is a valid outcome, and showing a retry affordance for it invites the user to retry something that already worked.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/dashboard/IndependentCardLayout.kt:103</code></summary>

```kotlin
                        { empty(index) }
                    } else {
                        { DefaultEmptyContent() }
                    },
                    noNetwork = if (noNetwork != null) {
                        { captive -> noNetwork(index, captive) }
                    } else {
```

</details>

```kotlin
fun DefaultNoNetworkContent(
```
The framework's offline rendering, shown when the failure was connectivity rather than the request.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/dashboard/IndependentCardLayout.kt:109</code></summary>

```kotlin
                    } else {
                        { captive ->
                            DefaultNoNetworkContent(
                                onRetry = { onRetry(index) },
                                isCaptivePortal = captive,
                                onDismiss = cardDismiss,
                            )
```

</details>

```kotlin
fun DefaultErrorContent(
```
The framework's error rendering, with the retry affordance wired to the screen's stream.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/dashboard/IndependentCardLayout.kt:120</code></summary>

```kotlin
                    } else {
                        { throwable ->
                            DefaultErrorContent(
                                throwable,
                                onRetry = { onRetry(index) },
                                onDismiss = cardDismiss,
                            )
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/screen/ScreenStateDefaults.kt`

```kotlin
data class ScreenStateDefaults(
```
App-level defaults for `ScreenContent` and `PagingScreenContent` slots. Provides three-tier overrides: 1. **Per-call**: pass a slot lambda to `ScreenContent(empty = { ... })`. 2.

<details><summary>Example</summary>

```kotlin
@Composable
fun KptTheme(content: @Composable () -> Unit) {
    val defaults = remember {
        ScreenStateDefaults(
            empty = ScreenStateEmpty(
                visual = ScreenStateVisual.Vector(MyBrandIcons.EmptyBox),
                title = "Nothing here yet",
                message = "When you have data, it'll show up here.",
            ),
            error = ScreenStateError(
                messageFor = ::mapErrorToUserMessage,
                onShown = { error -> AppTelemetry.recordError("screen_state_error", error) },
            ),
        )
    }
    CompositionLocalProvider(LocalScreenStateDefaults provides defaults) {
        MaterialTheme { content() }
    }
}
```

</details>

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:59</code></summary>

```kotlin
    )

    val screenStateDefaults = appScreenStateDefaults()
    val financeColors = if (darkTheme) darkFinanceColors() else lightFinanceColors()

    KptMaterialTheme(theme = themeProvider) {
        // Provide the design-system token CompositionLocals app-wide so every widget
```

</details>

```kotlin
data class ScreenStateEmpty(
```
Configuration for the empty state.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/KptScreenStateDefaults.kt:85</code></summary>

```kotlin
        ScreenStateDefaults(
            loading = ScreenStateLoading.Skeleton(rowCount = 5),
            empty = ScreenStateEmpty(
                visual = ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.empty),
                title = emptyTitle,
                message = emptyMessage,
            ),
```

</details>

```kotlin
data class ScreenStateError(
```
Configuration for the error state.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/KptScreenStateDefaults.kt:90</code></summary>

```kotlin
                message = emptyMessage,
            ),
            error = ScreenStateError(
                visual = ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.error),
                title = errorTitle,
                messageFor = errorMessageFor,
                retryText = errorRetry,
```

</details>

```kotlin
val DefaultErrorMessageFor: (Throwable) -> String = ::defaultErrorMessage
```
Singleton reference used as the default for `ScreenStateError.messageFor`.

```kotlin
fun defaultErrorMessage(error: Throwable): String = when (val cat = categorize(error))
```
Library default for `ScreenStateError.messageFor`. Routes through `categorize` so apps that don't supply a custom mapper still get sensible category-specific copy out of the box.

```kotlin
data class ScreenStateNoNetwork(
```
Configuration for the no-network state, including captive-portal variant. When the rendered state is `isCaptivePortal == true`, `DefaultNoNetworkContent` surfaces a primary CTA button labelled `captivePortalActionText`.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/KptScreenStateDefaults.kt:96</code></summary>

```kotlin
                retryText = errorRetry,
            ),
            noNetwork = ScreenStateNoNetwork(
                message = nonetMessage,
                captivePortalMessage = captiveMessage,
                captivePortalActionText = captiveAction,
                retryText = nonetRetry,
```

</details>

```kotlin
data class ScreenStateCta(
```
A primary call-to-action shown alongside an empty/error state.

<details><summary>Used in the template — <code>core-base/ui/src/commonTest/kotlin/kpt/core/base/ui/screen/ScreenStateDefaultsTest.kt:88</code></summary>

```kotlin
    @Test
    fun screenStateEmpty_ctaIsNullByDefaultButCarriesData() {
        val cta = ScreenStateCta(label = "Add new") { /* no-op */ }
        val cfg = ScreenStateEmpty(cta = cta)
        assertEquals("Add new", cfg.cta?.label)
    }
```

</details>

```kotlin
sealed interface ScreenStateLoading
```
Loading-state strategy. Library ships `Spinner` and `Skeleton`; consumers can supply `Custom`.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/KptScreenStateDefaults.kt:84</code></summary>

```kotlin
    ) {
        ScreenStateDefaults(
            loading = ScreenStateLoading.Skeleton(rowCount = 5),
            empty = ScreenStateEmpty(
                visual = ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.empty),
                title = emptyTitle,
                message = emptyMessage,
```

</details>

```kotlin
sealed interface ScreenStateVisual
```
Visual element for empty/error/no-network states. Sealed: extend by adding a new variant here and a branch in `ScreenStateVisualRenderer`.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/KptScreenStateDefaults.kt:86</code></summary>

```kotlin
            loading = ScreenStateLoading.Skeleton(rowCount = 5),
            empty = ScreenStateEmpty(
                visual = ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.empty),
                title = emptyTitle,
                message = emptyMessage,
            ),
            error = ScreenStateError(
```

</details>

```kotlin
val LocalScreenStateDefaults = compositionLocalOf { ScreenStateDefaults() }
```
App-wide ScreenState defaults.

<details><summary>Example</summary>

```kotlin
CompositionLocalProvider(LocalScreenStateDefaults provides myDefaults) { App() }
```

</details>

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:78</code></summary>

```kotlin
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
        ) {
            content()
        }
    }
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/screen/WithScreenStateDefaults.kt`

```kotlin
fun WithScreenStateDefaults(
```
Wraps `content` so that its screen-state widgets (`ScreenContent`, `PagingScreenContent`) see `overrides` merged on top of the ambient `LocalScreenStateDefaults`. Useful for per-screen tweaks (e.g.

<details><summary>Example</summary>

```kotlin
WithScreenStateDefaults(
    overrides = LocalScreenStateDefaults.current.copy(
        empty = ScreenStateEmpty(title = "No bills", message = "Add your first bill below."),
    ),
) {
    ScreenContent(state = state, onRetry = ::onRetry) { bills -> /* ... */ }
}
```

</details>

```kotlin
fun ScreenStateDefaults.mergeWith(other: ScreenStateDefaults): ScreenStateDefaults =
```
Returns a new `ScreenStateDefaults` where every field in `other` overrides the corresponding field in `this`. Used by `WithScreenStateDefaults`. Simple "other wins" merge: every nested field from `other` replaces `this`'s.

<details><summary>Example</summary>

```kotlin
val partial = LocalScreenStateDefaults.current.copy(
    empty = ScreenStateEmpty(title = "Custom"),
)
WithScreenStateDefaults(overrides = partial) { /* ... */ }
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/DraftResumeBanner.kt`

```kotlin
fun DraftResumeBanner(
```
Banner that surfaces a saved draft when `state` is `DraftResumeState.HasDraft`. Renders nothing when `state` is `DraftResumeState.None`. Place above the form in a Column, or inside the `MutationScreenContent` draft-aware overload.

<details><summary>Example</summary>

```kotlin
DraftResumeBanner(
    state = draftState,
    onResume  = viewModel::onResumeDraft,
    onDiscard = viewModel::onDiscardDraft,
)
```

</details>

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/MutationScreenContent.kt:221</code></summary>

```kotlin
    )
    Column(modifier = modifier.fillMaxSize()) {
        DraftResumeBanner(
            state = draftResumeState,
            onResume = onResumeClick,
            onDiscard = onDiscardClick,
        )
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/DraftSavePrompt.kt`

```kotlin
fun DraftSavePrompt(
```
Out-of-box dialog shown when a form submission fails due to a network error.

<details><summary>Example</summary>

```kotlin
Box {
    ScreenContent(state) { data -> FormBody(data) }
    SubmitProgressOverlay(submitState)
    DraftSavePrompt(
        submitState = submitState,
        onSaveDraft = viewModel::onSaveDraft,
        onDismiss   = viewModel::onDismissDraftPrompt,
    )
}
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/FormDraftBanner.kt`

```kotlin
fun FormDraftBanner(
```
"You have a saved draft" with Resume / Discard — shown when a create form opens on a slot that holds one.

```kotlin
fun FormDraftFailure(
```
Under a create that did not land: what went wrong, the server's own reason, and then either the note that it was kept as a draft (`draftSaved`) or a "Save as draft" button.

```kotlin
object FormDraftTestTags
```
Test tags for `FormDraftBanner` and `FormDraftFailure`.

- `const val SAVE = "form-draft:save"` — The "Save draft" button under a failed create.
- `const val SAVED = "form-draft:saved"` — The "Draft saved" confirmation that replaces it.
- `const val BANNER = "form-draft:banner"` — The Resume / Discard banner.
- `const val RESUME = "form-draft:resume"` — The banner's Resume button.
- `const val DISCARD = "form-draft:discard"` — The banner's Discard button.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/MutationScreenContent.kt`

```kotlin
fun <T, R> MutationScreenContent(
```
Convenience composable for edit/mutation screens. Combines `ScreenContent` + `SubmitProgressOverlay` + `SubmitResultHandler` into a single call.

<details><summary>Example</summary>

```kotlin
MutationScreenContent(
    screenState = uiState.screen,
    submitState = uiState.submit,
    onRetry = viewModel::onRetry,
    onSubmitted = { navigateBack() },
    onFailed = { _, category -> showError(category) },
) { data, _ ->
    EditForm(data = data, enabled = uiState.canInteract, onSave = viewModel::onSave)
}
```

</details>

```kotlin
fun <T, R> MutationScreenContent(
```
Direct `ScreenDataStream` (Store5 read) overload of `MutationScreenContent`.

<details><summary>Example</summary>

```kotlin
MutationScreenContent(
    stream = viewModel.editStream,          // repository-built ScreenDataStream
    submitState = uiState.submit,
    onSubmitted = { navigateBack() },
) { data, _ ->
    EditForm(data = data, onSave = viewModel::onSave)
}
```

</details>

```kotlin
fun <T, R> MutationScreenContent(
```
`MutationUiState`-typed overload. Reduces boilerplate when the ViewModel exposes a single `MutationUiState` property.

<details><summary>Example</summary>

```kotlin
MutationScreenContent(
    state = uiState.mutation,
    onRetry = viewModel::onRetry,
    onSubmitted = { navigateBack() },
) { data, _ ->
    EditForm(data = data, enabled = uiState.mutation.canInteract, onSave = viewModel::onSave)
}
```

</details>

```kotlin
fun <T, R> MutationScreenContent(
```
Draft-aware overload of `MutationScreenContent`. Shows a `DraftResumeBanner` above the form content when `draftResumeState` is `DraftResumeState.HasDraft`. The banner is omitted when `draftResumeState` is `DraftResumeState.None` (default).

<details><summary>Example</summary>

```kotlin
MutationScreenContent(
    screenState = uiState.screen,
    submitState = uiState.submit,
    draftResumeState = draftState,
    onResumeClick = viewModel::onResumeDraft,
    onDiscardClick = viewModel::onDiscardDraft,
    onRetry = viewModel::onRetry,
    onSubmitted = { navigateBack() },
) { data, _ ->
    EditForm(data = data, enabled = uiState.canInteract)
}
```

</details>

```kotlin
fun <T, R> MutationScreenContent(
```
`MutationUiState`-typed draft-aware overload. Combines read state, write state, and draft resume into a single call.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/SubmitButton.kt`

```kotlin
fun <R> SubmitButton(
```
Submit button that auto-disables while a submission is in-flight. Wraps `KptButton` and derives `enabled` from `SubmitState` so callers never have to wire `enabled = !submitState.isSubmitting` manually.

<details><summary>Example</summary>

```kotlin
SubmitButton(state = submitState, onClick = viewModel::onSubmit) {
    Text("Save")
}
```

</details>

```kotlin
fun SubmitButton(
```
Boolean-driven overload for callers that already compute the enabled flag externally (e.g., `screenState.canInteract(submitState)`).

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/SubmitProgressOverlay.kt`

```kotlin
fun SubmitProgressOverlay(
```
Semi-transparent scrim + centered `KptProgress` circular spinner shown while a submission is in-flight. Render above form content inside a `Box`.

<details><summary>Example</summary>

```kotlin
Box(Modifier.fillMaxSize()) {
    FormContent(enabled = !submitState.isSubmitting, onSubmit = { … })
    SubmitProgressOverlay(visible = submitState.isSubmitting)
}
```

</details>

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/MutationScreenContent.kt:90</code></summary>

```kotlin
            )
        }
        SubmitProgressOverlay(state = submitState)
    }
}

// STORE5-COMPLETENESS: mutation-stream — stream overload
```

</details>

```kotlin
fun <R> SubmitProgressOverlay(
```
`SubmitState`-typed overload. Shows the overlay when `state` is `SubmitState.Submitting`.

<details><summary>Example</summary>

```kotlin
SubmitProgressOverlay(state = submitState)
```

</details>

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/MutationScreenContent.kt:90</code></summary>

```kotlin
            )
        }
        SubmitProgressOverlay(state = submitState)
    }
}

// STORE5-COMPLETENESS: mutation-stream — stream overload
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/SubmitResultHandler.kt`

```kotlin
fun <R> SubmitResultHandler(
```
Side-effect composable that fires callbacks when `state` reaches a terminal state. - `onSubmitted` fires once when `SubmitState.Submitted` is observed. - `onFailed` fires once when `SubmitState.Failed` is observed.

<details><summary>Example</summary>

```kotlin
SubmitResultHandler(
    state = submitState,
    onSubmitted = { clientId ->
        onNavigateToDetail(clientId)
        // no reset needed — navigating away destroys the ViewModel
    },
    onFailed = { error, category ->
        viewModel.onDismiss()          // calls submit.reset()
        when (category) {
            ErrorCategory.Network -> showNoNetworkSheet()
            ErrorCategory.Auth    -> onNavigateToLogin()
            else                  -> showErrorDialog(error.message)
        }
    },
)
```

</details>

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/MutationScreenContent.kt:58</code></summary>

```kotlin
    content: @Composable (data: T, freshnessSignal: FreshnessSignal) -> Unit,
) {
    SubmitResultHandler(
        state = submitState,
        onSubmitted = onSubmitted,
        onFailed = onFailed,
    )
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/ImageLoaderExt.kt`

```kotlin
fun rememberImageLoader(): ImageLoader
```
Creates and remembers an instance of the default ImageLoader configured with platform-specific settings for caching and logging. The ImageLoader is initialized using the current platform context.

```kotlin
fun getDefaultImageLoader(context: PlatformContext): ImageLoader = ImageLoader
```
Creates and returns a default instance of an ImageLoader configured with common settings such as logging, caching policies, memory caching, and platform-specific component support.

```kotlin
fun LocalImageLoaderProvider(imageLoader: ImageLoader, content: @Composable () -> Unit)
```
Provides a composable local context for an `ImageLoader` to be used within a Composable hierarchy.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/JankStatsExtension.kt`

```kotlin
expect fun TrackScrollJank(scrollableState: ScrollableState, stateName: String)
```
Reports dropped frames during scrolling of `scrollableState`, tagged `stateName` so one screen's jank is attributable in a trace.

<details><summary>Used in the template — <code>core/ui/src/androidMain/kotlin/kpt/core/ui/JankStatsExtensions.kt:74</code></summary>

```kotlin
 */
@Composable
fun TrackScrollJank(scrollableState: ScrollableState, stateName: String) {
    TrackJank(scrollableState) { metricsHolder ->
        snapshotFlow { scrollableState.isScrollInProgress }.collect { isScrollInProgress ->
            metricsHolder.state?.apply {
                if (isScrollInProgress) {
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/ShareUtils.kt`

```kotlin
expect object ShareUtils
```
Platform-specific utility for sharing content with other applications. This expect class requires platform-specific implementations.

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrViewModel.kt:303</code></summary>

```kotlin
    private fun shareQrCode(data: ByteArray) {
        viewModelScope.launch {
            ShareUtils.shareFile(
                file = ShareFileModel(
                    fileName = "qr_code.png",
                    bytes = data,
                    mime = MimeType.IMAGE,
```

</details>

- `suspend fun shareText(text: String)` — Shares text content with other applications.
- `suspend fun shareImage(title: String, image: ImageBitmap)` — Shares an image with other applications.
- `suspend fun shareImage(title: String, byte: ByteArray)` — Shares an image with other applications using raw byte data.
- `fun openUrl(url: String)` — Opens the specified URL in the device's default web browser.
- `fun openAppInfo()` — Opens the application info screen in the device settings. Typically used to allow users to manage app permissions, storage, or other app-specific settings.
- `fun callPhone(number: String)` — Initiates a phone call using the platform dialer UI.
- `fun sendEmail(to: String, subject: String? = null, body: String? = null)` — Opens the platform email composer with the given parameters.
- `fun sendViaSMS(number: String, message: String)` — Opens the platform SMS composer with the given parameters.
- `fun copyText(text: String)` — Copies the given text to the system clipboard.
- `suspend fun shareApp(storeLink: String, message: String = "")` — Shares the app store link with a custom message. Opens the platform share sheet with the store link and message combined, allowing users to share the app with others via various apps.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/SharedElementExt.kt`

```kotlin
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
```
CompositionLocal that provides access to an `AnimatedVisibilityScope` within the composition. Default value is null, requiring an explicit provider upstream in the composition.

```kotlin
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
```
CompositionLocal that provides access to a `SharedTransitionScope` within the composition. Used for creating shared element transitions between composables. Default value is null, requiring an explicit provider upstream in the composition.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/StringExt.kt`

```kotlin
val String.capitalizeEachWord: String
```
Extension property that returns a string with the first letter of each word capitalized. For example, "hello world" becomes "Hello World".

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/Transition.kt`

```kotlin
typealias EnterTransitionProvider =
```
Function type for providing nullable enter transitions in navigation. Used with `AnimatedContentTransitionScope` to define how a destination enters the screen.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/utils/Transition.kt:23</code></summary>

```kotlin
import kotlin.jvm.JvmSuppressWildcards

typealias EnterTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)

typealias ExitTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)
```

</details>

```kotlin
typealias ExitTransitionProvider =
```
Function type for providing nullable exit transitions in navigation. Used with `AnimatedContentTransitionScope` to define how a destination exits the screen.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/utils/Transition.kt:26</code></summary>

```kotlin
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)

typealias ExitTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)

typealias NonNullEnterTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition)
```

</details>

```kotlin
typealias NonNullEnterTransitionProvider =
```
Function type for providing non-null enter transitions in navigation. Used with `AnimatedContentTransitionScope` to define how a destination enters the screen.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/utils/Transition.kt:29</code></summary>

```kotlin
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)

typealias NonNullEnterTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition)

typealias NonNullExitTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition)
```

</details>

```kotlin
typealias NonNullExitTransitionProvider =
```
Function type for providing non-null exit transitions in navigation. Used with `AnimatedContentTransitionScope` to define how a destination exits the screen.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/utils/Transition.kt:32</code></summary>

```kotlin
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition)

typealias NonNullExitTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition)

/**
 * The default transition time (in milliseconds) for all fade transitions in the
```

</details>

```kotlin
val DEFAULT_STAY_TRANSITION_TIME_MS: Int =
```
The default transition time (in milliseconds) for all "stay"/no-op transitions in the `TransitionProviders`. This should be at least as large as any other transition that might also be happening during a navigation.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/utils/Transition.kt:60</code></summary>

```kotlin
 * navigation.
 */
val DEFAULT_STAY_TRANSITION_TIME_MS: Int =
    maxOf(
        DEFAULT_FADE_TRANSITION_TIME_MS,
        DEFAULT_SLIDE_TRANSITION_TIME_MS,
        DEFAULT_PUSH_TRANSITION_TIME_MS,
```

</details>

```kotlin
val AnimatedContentTransitionScope<NavBackStackEntry>.isSameGraphNavigation: Boolean
```
Checks if the parent of the destination before and after the navigation is the same. This is useful to ignore certain enter/exit transitions when navigating between distinct, nested flows.

```kotlin
object TransitionProviders
```
Contains standard "transition providers" that may be used to specify the `EnterTransition` and `ExitTransition` used when building a typical composable destination.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/NavGraphBuilderExtensions.kt:30</code></summary>

```kotlin
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = TransitionProviders.Enter.slideUp,
        exitTransition = TransitionProviders.Exit.stay,
        popEnterTransition = TransitionProviders.Enter.stay,
        popExitTransition = TransitionProviders.Exit.slideDown,
        content = content,
```

</details>

- `val fadeIn: EnterTransitionProvider =` — Fades the new screen in. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val pushLeft: EnterTransitionProvider =` — Slides the new screen in from the left of the screen.
- `val pushRight: EnterTransitionProvider =` — Slides the new screen in from the right of the screen.
- `val slideUp: EnterTransitionProvider =` — Slides the new screen in from the bottom of the screen. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val stay: EnterTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately appear while the other screen transitions away.
- `val fadeOut: ExitTransitionProvider =` — Fades the current screen out. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val pushLeft: ExitTransitionProvider =` — Slides the current screen out to the left of the screen.
- `val pushRight: ExitTransitionProvider =` — Slides the current screen out to the right of the screen.
- `val slideDown: ExitTransitionProvider =` — Slides the current screen down to the bottom of the screen. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val stay: ExitTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately disappear while the other screen transitions into place.
- `val sharedAxisForward: EnterTransitionProvider = TransitionProviders.Enter.pushLeft` — Hardcoded aliases — kept for callers that don't have a `Motion` in scope.
- `val sharedAxisBack: EnterTransitionProvider = TransitionProviders.Enter.pushRight` — Back shared-axis — the mirror of the forward move, for returning.
- `val fadeThrough: EnterTransitionProvider = TransitionProviders.Enter.fadeIn` — Fade-through — a lateral move between peers with no spatial relationship.
- `val slideUp: EnterTransitionProvider = TransitionProviders.Enter.slideUp` — Slide-up — a modal-style arrival.
  _…more members; read the file._

```kotlin
object RootTransitionProviders
```
Contains standard "transition providers" that may be used to specify the `EnterTransition` and `ExitTransition` used when building a root `NavHost`, which requires a non-null value.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/utils/Transition.kt:92</code></summary>

```kotlin
         */
        val fadeIn: EnterTransitionProvider = {
            RootTransitionProviders
                .Enter
                .fadeIn(this)
                .takeIf { isSameGraphNavigation }
        }
```

</details>

- `val fadeIn: NonNullEnterTransitionProvider =` — Fades the new screen in.
- `val none: NonNullEnterTransitionProvider =` — There is no transition for the entering screen.
- `val pushLeft: NonNullEnterTransitionProvider =` — Slides the new screen in from the left of the screen.
- `val pushRight: NonNullEnterTransitionProvider =` — Slides the new screen in from the right of the screen.
- `val slideUp: NonNullEnterTransitionProvider =` — Slides the new screen in from the bottom of the screen.
- `val stay: NonNullEnterTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately appear while the other screen transitions away.
- `val fadeOut: NonNullExitTransitionProvider =` — Fades the current screen out.
- `val none: NonNullExitTransitionProvider =` — There is no transition for the exiting screen. Unlike the `stay` transition, this will immediately remove the outgoing screen even if there is an ongoing enter transition happening for the new screen.
- `val pushLeft: NonNullExitTransitionProvider =` — Slides the current screen out to the left of the screen.
- `val pushRight: NonNullExitTransitionProvider =` — Slides the current screen out to the right of the screen.
- `val slideDown: NonNullExitTransitionProvider =` — Slides the current screen down to the bottom of the screen.
- `val stay: NonNullExitTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately disappear while the other screen transitions into place.
- `val sharedAxisForward: NonNullEnterTransitionProvider = RootTransitionProviders.Enter.pushLeft` — Hardcoded aliases — kept for callers that don't have a `Motion` in scope.
- `val sharedAxisBack: NonNullEnterTransitionProvider = RootTransitionProviders.Enter.pushRight` — Back shared-axis — the mirror of the forward move, for returning.
  _…more members; read the file._

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/viewmodel/BackgroundEvent.kt`

```kotlin
interface BackgroundEvent
```
Almost all the events in the app involve navigation or toasts. To prevent accidentally navigating to the same view twice, by default, events are ignored if the view is not currently resumed.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyOptionsViewModel.kt:70</code></summary>

```kotlin
    data object NavigateToFineractPayments : SendMoneyOptionsEvent
    data object NavigateToAutoPay : SendMoneyOptionsEvent
    data class QrCodeScanned(val data: String) : SendMoneyOptionsEvent, BackgroundEvent
    data class NavigateToPayeeDetails(val qrCodeData: String) : SendMoneyOptionsEvent, BackgroundEvent
}

sealed interface SendMoneyOptionsAction {
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/viewmodel/BaseMutationViewModel.kt`

```kotlin
abstract class BaseMutationViewModel<T, R>(
```
The single base class for edit/mutation-screen ViewModels — the one a feature module extends.

<details><summary>Example</summary>

```kotlin
// In-session (no persistence) — mode defaults to MutationMode.InSession:
class EditEntityViewModel(private val repo: EntityRepository, private val id: String) :
    BaseMutationViewModel<Entity, Unit>() {
    init { viewModelScope.launch { mutableScreenState.value = ScreenState.Content(repo.get(id)) } }
    override suspend fun performSubmit(payload: Entity) = repo.update(id, payload)
}

// Offline-resilient (durable draft + auto-retry + three-case resume):
class EditEntityViewModel(
    outbox: SubmitOutbox<Entity>,
    entityId: String?,
    private val repo: EntityRepository,
) : BaseMutationViewModel<Entity, Entity>(
    MutationMode.Draft(
        outbox = outbox,
        formKey = "entity",                // groups this form's drafts
        uniqueKey = entityId ?: "new",     // N concurrent drafts (one per entity); null = singleton
        autoSaveDraft = true,              // persist silently on network failure
    ),
) {
    override suspend fun performSubmit(payload: Entity): Entity = repo.upsert(payload)
}
```

</details>

- `val uiState: StateFlow<MutationUiState<T, R>> get() = stateFlow` — Backward-compat alias for the inherited `stateFlow`.
- `val resumeStream: Flow<DraftResumeState<T>> = draftMode?.let { m ->`
- `open fun onSubmit(payload: T) = trySendAction(MutationAction.Submit(payload))` — Trigger a form submission. No-op if already `SubmitState.Submitting`.
- `open fun onRetry() = trySendAction(MutationAction.Retry)` — Retry the last failed submission.
- `open fun onDismissResult() = trySendAction(MutationAction.Dismiss)` — Dismiss the result overlay and return to `SubmitState.Idle`. Override to add VM-local cleanup (e.g. reset form fields); always call `super.onDismissResult()` to keep the reset behaviour.
- `fun onSaveDraft()` — User-prompted draft save (Draft mode only) — persists the in-memory pending payload after the user confirms "Save Draft". No-op in `MutationMode.InSession` and in `autoSaveDraft = true` mode.
- `fun onDiscardDraft()` — Discard the in-memory pending payload and dismiss the save prompt (Draft mode only). Does NOT delete an already-persisted draft — use `onDiscardSavedDraft` for that.
- `open fun onResumeDraft() = trySendAction(MutationAction.ResumeDraft)` — Resume the saved draft — the screen reads `MutationUiState.resumableDraft` to pre-fill; hides the prompt.
- `open fun onStartFresh() = trySendAction(MutationAction.StartFreshDraft)` — Keep the saved draft (recoverable in Sync & Drafts) but start a blank new entry.
- `open fun onDiscardSavedDraft() = trySendAction(MutationAction.DiscardSavedDraft)` — Permanently delete the saved draft for this form, then edit blank.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/viewmodel/BaseViewModel.kt`

```kotlin
abstract class BaseViewModel<S, E, A>(
```
A base `ViewModel` that helps enforce the unidirectional data flow pattern and associated responsibilities of a typical ViewModel: - Maintaining and emitting a current state (of type `S`) with the given `initialState`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:48</code></summary>

```kotlin
    private val repository: SelfServiceRepository,
    private val json: Json,
) : BaseViewModel<AccountState, AccountEvent, AccountAction>(
    initialState = run {
        val clientId = userRepository.clientId.value
        val defaultAccount = userRepository.defaultAccountId.value
```

</details>

- `val stateFlow: StateFlow<S> = mutableStateFlow.asStateFlow()` — A `StateFlow` representing state updates.
- `val eventFlow: Flow<E> = eventChannel.receiveAsFlow()` — A `Flow` of one-shot events. These may be received and consumed by only a single consumer. Any additional consumers will receive no events.
- `val actionChannel: SendChannel<A> = internalActionChannel` — A `SendChannel` for sending actions to the ViewModel for processing.
- `fun trySendAction(action: A)` — Convenience method for sending an action to the `actionChannel`.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/viewmodel/MutationAction.kt`

```kotlin
sealed interface MutationAction<out T>
```
MVI actions emitted by mutation-flow screens (form edit, settings save, etc.).

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/viewmodel/BaseMutationViewModel.kt:80</code></summary>

```kotlin
abstract class BaseMutationViewModel<T, R>(
    private val mode: MutationMode<T> = MutationMode.InSession,
) : BaseViewModel<MutationUiState<T, R>, Nothing, MutationAction<T>>(MutationUiState()) {

    private val draftMode: MutationMode.Draft<T>? = mode as? MutationMode.Draft<T>

    private val inSessionHandler = if (draftMode == null) viewModelScope.submitHandler<R>() else null
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/viewmodel/MutationMode.kt`

```kotlin
sealed interface MutationMode<out T>
```
How a `BaseMutationViewModel` handles its submit — the single knob that replaces the old `BaseSubmitMutationViewModel` / `BaseDraftMutationViewModel` class split.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/viewmodel/BaseMutationViewModel.kt:79</code></summary>

```kotlin
 */
abstract class BaseMutationViewModel<T, R>(
    private val mode: MutationMode<T> = MutationMode.InSession,
) : BaseViewModel<MutationUiState<T, R>, Nothing, MutationAction<T>>(MutationUiState()) {

    private val draftMode: MutationMode.Draft<T>? = mode as? MutationMode.Draft<T>
```

</details>

---

_27 type(s), 147 function(s)/property(ies); 169 carry KDoc at source; 32 authored example(s); 52 live call site(s)._
<!-- api-docs:end -->
