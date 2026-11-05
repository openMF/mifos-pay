# `feature/add-to-watchlist/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 27 tracked files — 20× `.xml`, 5× `.kt`, 1× `.md`, 1× `.kts`

**5 Kotlin files** — 0 screen(s), 1 ViewModel(s).

ViewModels: `AddToWatchlistViewModel`

**Consumes:** `core/store/watchlist`

**Store5 archetype(s) exercised:** `CACHE_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/add-to-watchlist/README.md` | :feature:add-to-watchlist |

<!-- tree-scaffold:end -->
## Significance

The add-to-watchlist sheet, split out as its own module so the watchlist and the market list can both
present it without depending on each other. Small on purpose: a shared UI surface that lives in one
feature is a dependency the other feature cannot remove.

<!-- api-docs:begin module=feature/add-to-watchlist sha=3937fd2762a94da8f67634cb9bc3e98cf9cfb049 -->
## API reference

_Generated from `feature/add-to-watchlist` at tree `3937fd2762a9` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/add-to-watchlist/src/commonMain/kotlin/kpt/feature/addtowatchlist/di/AddToWatchlistModule.kt`

```kotlin
val AddToWatchlistModule = module
```
Koin module for the embedded add-to-watchlist star (`submit_offline_write` demo). `AddToWatchlistViewModel` takes a runtime `coinId` parameter (`parametersOf(coinId)`), so it is registered as a parameterised `viewModel { params -> ... }`.

### `feature/add-to-watchlist/src/commonMain/kotlin/kpt/feature/addtowatchlist/ui/AddToWatchlistStar.kt`

```kotlin
fun AddToWatchlistStar(
```
The embedded WRITE-side widget of the Personal Crypto Watchlist — a single star icon-button, NOT a navigable screen. Host it on any coin surface (`feature/crypto` CoinMarkets rows + Coin Detail) by passing the row's `coinId`.

<details><summary>Used in the template — <code>feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinMarketsScreen.kt:160</code></summary>

```kotlin
            // Embedded write-side widget — toggles this coin's watchlist membership.
            // The IconButton consumes its own tap, so it does NOT trigger the row's nav onClick.
            AddToWatchlistStar(coinId = coin.id)
        }
    }
}
```

</details>

### `feature/add-to-watchlist/src/commonMain/kotlin/kpt/feature/addtowatchlist/ui/AddToWatchlistViewModel.kt`

```kotlin
class AddToWatchlistViewModel(
```
The WRITE side of the Personal Crypto Watchlist — the canonical `submit_offline_write` ("input simple" `SubmitHandler`) reference.

<details><summary>Used in the template — <code>feature/add-to-watchlist/src/commonMain/kotlin/kpt/feature/addtowatchlist/di/AddToWatchlistModule.kt:24</code></summary>

```kotlin
val AddToWatchlistModule = module {
    viewModel { params ->
        AddToWatchlistViewModel(repository = get(), coinId = params.get())
    }
}
```

</details>

```kotlin
sealed interface AddToWatchlistAction
```
MVI action surface for the star toggle.

### `feature/add-to-watchlist/src/commonMain/kotlin/kpt/feature/addtowatchlist/ui/TestTags.kt`

```kotlin
object TestTags
```
Stable test tags for the embedded add-to-watchlist star toggle.

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

- `const val STAR_PREFIX = "watchlist_star_"` — Per-coin star toggle: `watchlist_star_{coinId}` (mirrors idea-layer ui.yaml).

---

_3 type(s), 3 function(s)/property(ies); 6 carry KDoc at source; 0 authored example(s); 3 live call site(s)._
<!-- api-docs:end -->
