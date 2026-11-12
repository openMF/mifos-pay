# `feature/watchlist/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 31 tracked files — 20× `.xml`, 9× `.kt`, 1× `.md`, 1× `.kts`

**9 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `WatchlistScreen`

ViewModels: `WatchlistViewModel`

**Consumes:** `core/store/watchlist`

**Store5 archetype(s) exercised:** `CACHE_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/watchlist/README.md` | :feature:watchlist |

<!-- tree-scaffold:end -->
## Significance

A user-curated coin list — **`CACHE_ONLY`**, entirely local, no network of its own. It reads the same
`core/store/crypto` prices the market list does, so a watched coin never shows a different number than
the list it was added from.

<!-- api-docs:begin module=feature/watchlist sha=716e647a8c019f188ab15c189e12a59b5ae25806 -->
## API reference

_Generated from `feature/watchlist` at tree `716e647a8c01` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/watchlist/src/commonMain/kotlin/kpt/feature/watchlist/di/WatchlistModule.kt`

```kotlin
val WatchlistModule = module
```
Koin module for the Watchlist feature (`read_local_list` demo). `WatchlistViewModel` takes no parameters — it reads reactively from the DI-provided `kpt.core.data.watchlist.WatchlistRepository`.

### `feature/watchlist/src/commonMain/kotlin/kpt/feature/watchlist/navigation/WatchlistNavigation.kt`

```kotlin
data object WatchlistRoute
```
Watchlist entry destination.

```kotlin
fun NavController.navigateToWatchlist(navOptions: NavOptions? = null)
```
Navigates to the watchlist.

```kotlin
fun NavGraphBuilder.watchlistGraph(navController: NavController)
```
Watchlist feature's navigation graph. The host (cmp-navigation) wires this into the top-level RootNavGraph by calling `watchlistGraph` inside its own `NavHost { ... }` builder once this feature is enabled.

### `feature/watchlist/src/commonMain/kotlin/kpt/feature/watchlist/ui/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the watchlist feature. Consumed by Compose UI tests in `feature/watchlist/src/commonTest/`. APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).

<details><summary>Used in the template — <code>feature/add-to-watchlist/src/commonMain/kotlin/kpt/feature/addtowatchlist/ui/AddToWatchlistStar.kt:51</code></summary>

```kotlin
    IconButton(
        onClick = viewModel::onToggle,
        modifier = modifier.testTag(TestTags.AddToWatchlist.STAR_PREFIX + coinId),
    ) {
        Icon(
            imageVector = if (isTracked) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = stringResource(
```

</details>

- `const val SCREEN: String = "watchlist_screen"` — Root scaffold — always rendered regardless of load state.
- `const val ROW_PREFIX: String = "watchlist_row_"` — One row per saved coin. Suffix with the coinId for per-row assertions.
- `const val REMOVE_PREFIX: String = "watchlist_remove_"` — Per-row remove button. Suffix with the coinId.

### `feature/watchlist/src/commonMain/kotlin/kpt/feature/watchlist/ui/WatchlistScreen.kt`

```kotlin
fun WatchlistScreen(
```
Watchlist — the toolkit's `read_local_list` demo. Renders the user's saved coins as an offline Room-backed reactive list (non-paged). Each row offers a remove action; the list re-emits instantly as Room propagates the change. No network.

<details><summary>Used in the template — <code>feature/watchlist/src/commonMain/kotlin/kpt/feature/watchlist/navigation/WatchlistNavigation.kt:44</code></summary>

```kotlin
fun NavGraphBuilder.watchlistGraph(navController: NavController) {
    composableWithPushTransitions<WatchlistRoute> {
        WatchlistScreen(
            onBackClick = { navController.popBackStackSafely() },
        )
    }
}
```

</details>

### `feature/watchlist/src/commonMain/kotlin/kpt/feature/watchlist/ui/WatchlistViewModel.kt`

```kotlin
class WatchlistViewModel(
```
Watchlist ViewModel — the canonical `read_local_list` demo.

<details><summary>Used in the template — <code>feature/watchlist/src/commonMain/kotlin/kpt/feature/watchlist/di/WatchlistModule.kt:22</code></summary>

```kotlin
 */
val WatchlistModule = module {
    viewModel { WatchlistViewModel(repository = get()) }
}
```

</details>

```kotlin
sealed interface WatchlistAction
```
One-shot actions accepted by `WatchlistViewModel`.

---

_4 type(s), 7 function(s)/property(ies); 11 carry KDoc at source; 0 authored example(s); 3 live call site(s)._
<!-- api-docs:end -->
