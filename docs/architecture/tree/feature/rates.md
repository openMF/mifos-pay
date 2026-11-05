# `feature/rates/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 38 tracked files — 20× `.xml`, 16× `.kt`, 1× `.md`, 1× `.kts`

**16 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `InterestRateDetailScreen`, `InterestRatesScreen`

ViewModels: `InterestRateDetailViewModel`, `InterestRatesViewModel`

**Consumes:** `core/store/economic`

**Store5 archetype(s) exercised:** `MEMORY_ONLY`, `NETWORK_WITH_CACHE` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/rates/README.md` | :feature:rates |

<!-- tree-scaffold:end -->
## Significance

FRED-backed interest rates — federal funds, prime, 30-year mortgage, 10-year treasury — each its own
independent `ScreenState` combined into one dashboard.

The point is the **4-way fan-in**: one slow or failing series must not blank the other three. Adding a
series means extending `RateSeriesCatalog.kt`; no client change is needed, which is the seam a fork
exercises when it points the feature at its own data.

<!-- api-docs:begin module=feature/rates sha=82c1d6694a3adc301549235bdd3f7a92e150d8ee -->
## API reference

_Generated from `feature/rates` at tree `82c1d6694a3a` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/rates/src/commonMain/kotlin/kpt/feature/rates/di/RatesModule.kt`

```kotlin
val RatesModule = module
```
Koin module for the B7 Interest Rate Tracker.

### `feature/rates/src/commonMain/kotlin/kpt/feature/rates/navigation/RatesNavigation.kt`

```kotlin
data object RatesGraphRoute
```
Parent graph route for the B7 Interest Rate Tracker.

```kotlin
data object RatesListRoute
```
Default destination — the multi-series dashboard.

```kotlin
data class RateDetailRoute(val seriesId: String)
```
Per-series detail destination.

```kotlin
fun NavController.navigateToRates(navOptions: NavOptions? = null)
```
Navigates to the interest-rates dashboard.

```kotlin
fun NavController.navigateToRateDetail(seriesId: String, navOptions: NavOptions? = null)
```
Navigates to one series' detail.

```kotlin
fun NavGraphBuilder.ratesGraph(navController: NavController)
```
Add the B7 Interest Rate Tracker nav graph to the host `NavGraphBuilder`. Host apps wire this from their root navigation by calling `ratesGraph(navController)`.

### `feature/rates/src/commonMain/kotlin/kpt/feature/rates/ui/InterestRateDetailViewModel.kt`

```kotlin
sealed interface DetailAction
```
What the detail screen can be asked to do.

### `feature/rates/src/commonMain/kotlin/kpt/feature/rates/ui/InterestRatesViewModel.kt`

```kotlin
data class RatesUiState(
```
Aggregate state for the rate-tracker dashboard. Each slot is an independent `ScreenState` so the screen can render per-row Loading / Empty / Error / Content transitions without spinning up a master `combineScreenStates` reduce.

```kotlin
sealed interface RatesAction
```
What the dashboard can be asked to do.

<details><summary>Used in the template — <code>feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/ui/CurrencyRatesScreen.kt:114</code></summary>

```kotlin
                targetCode = localState.converterTarget,
                spotState = spotState,
                onAmountChange = { viewModel.trySendAction(RatesAction.ConverterAmount(it)) },
                onTargetChange = { viewModel.trySendAction(RatesAction.ConverterTarget(it)) },
                onRetry = viewModel::onRetry,
                modifier = Modifier
                    .padding(horizontal = sp.lg, vertical = sp.sm)
```

</details>

### `feature/rates/src/commonMain/kotlin/kpt/feature/rates/ui/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the interest-rates feature. Consumed by: - Compose UI tests in `feature/rates/src/commonTest/` (`InterestRatesScreenUiTest`, `InterestRateDetailScreenUiTest`).

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

- `const val RATES_SCROLL: String = "rates_scroll"` — The root vertically-scrollable `Column` that hosts the four rate-row cards. Always rendered regardless of per-row `ScreenState` — suitable as the stable root assertion node in Compose UI tests.
- `const val DETAIL_ROOT: String = "rate_detail_root"` — The root `Scaffold` surface of the detail screen. Tagged via the `modifier` parameter so it is always in the composition tree irrespective of the current `ScreenState` (Loading / Error / Content).

---

_7 type(s), 6 function(s)/property(ies); 13 carry KDoc at source; 0 authored example(s); 2 live call site(s)._
<!-- api-docs:end -->
