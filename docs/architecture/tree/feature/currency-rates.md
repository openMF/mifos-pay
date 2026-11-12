# `feature/currency-rates/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 37 tracked files — 20× `.xml`, 15× `.kt`, 1× `.md`, 1× `.kts`

**15 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `CurrencyRatesScreen`, `RateHistoryScreen`

ViewModels: `CurrencyRatesViewModel`, `RateHistoryViewModel`

**Consumes:** `core/store/config`, `core/store/currency`

**Store5 archetype(s) exercised:** `NETWORK_WITH_CACHE`, `PERIODIC` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/currency-rates/README.md` | :feature:currency-rates |

<!-- tree-scaffold:end -->
## Significance

Live FX rates plus a historical chart — the **`NETWORK_WITH_CACHE`** showcase, and the one place a
windowed series is keyed correctly.

The chart's window length is *part of the store key*, so widening it is a different key and a full
re-fetch rather than a page append. Getting that wrong produces a chart that silently shows a short
window's data under a long window's axis.

<!-- api-docs:begin module=feature/currency-rates sha=b698160d84367c9cdcabfae0f98b8ff146341795 -->
## API reference

_Generated from `feature/currency-rates` at tree `b698160d8436` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/di/CurrencyRatesModule.kt`

```kotlin
val CurrencyRatesModule = module
```
Koin bindings for the currency-rates feature.

### `feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/navigation/CurrencyRatesNavigation.kt`

```kotlin
data object CurrencyRatesGraphRoute
```
Route for the currency-rates nested graph.

```kotlin
data object CurrencyRatesRoute
```
Route for the live rates list — the graph's start destination.

```kotlin
data object RateHistoryRoute
```
Route for the historical chart.

```kotlin
fun NavController.navigateToCurrencyRates(navOptions: NavOptions? = null)
```
Navigates to the rates list.

```kotlin
fun NavGraphBuilder.currencyRatesGraph(navController: NavController)
```
Registers the currency-rates nested graph.

```kotlin
fun NavController.navigateToRateHistory(navOptions: NavOptions? = null)
```
Navigates to the historical chart.

### `feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/ui/CurrencyRatesScreen.kt`

```kotlin
fun CurrencyRatesScreen(
```
Live FX rates for a base currency, with search and an inline converter.

<details><summary>Used in the template — <code>feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/navigation/CurrencyRatesNavigation.kt:53</code></summary>

```kotlin
    navigation<CurrencyRatesGraphRoute>(startDestination = CurrencyRatesRoute) {
        composableWithPushTransitions<CurrencyRatesRoute> {
            CurrencyRatesScreen(onBackClick = { navController.popBackStackSafely() })
        }

        composableWithPushTransitions<RateHistoryRoute> {
            RateHistoryScreen(onBackClick = { navController.popBackStackSafely() })
```

</details>

### `feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/ui/CurrencyRatesViewModel.kt`

```kotlin
class CurrencyRatesViewModel(
```
**Archetype showcase: CACHE_ONLY + NETWORK_ONLY** In addition to the regular exchange-rates stream (NETWORK_WITH_CACHE default), this ViewModel demonstrates policy routing based on network status: - Online → `FetchPolicy.NETWORK_ONLY` (always-fresh spot rate, no stale cache) - Offline → `FetchPolicy.CACHE_ONLY` (read cached value, never call API) The `spotConversionRate` property is the canonical reference implementation for this archetype pattern. See `AppStoreRegistry.SpotRate` for the store registration.

<details><summary>Used in the template — <code>feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/di/CurrencyRatesModule.kt:20</code></summary>

```kotlin
val CurrencyRatesModule = module {
    viewModel {
        CurrencyRatesViewModel(
            currencyRepository = get(),
            networkMonitor = get(),
        )
    }
```

</details>

```kotlin
data class RatesLocalState(
```
_No KDoc at source._

```kotlin
data class RatesDisplay(val base: String, val date: String, val rates: Map<String, Double>)
```
What the list renders: the base currency, its date, and the rates.

```kotlin
sealed interface RatesAction
```
What the screen can be asked to do.

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

### `feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/ui/RateHistoryScreen.kt`

```kotlin
fun RateHistoryScreen(
```
Historical FX chart for a pair, over a selectable window.

<details><summary>Used in the template — <code>feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/navigation/CurrencyRatesNavigation.kt:57</code></summary>

```kotlin

        composableWithPushTransitions<RateHistoryRoute> {
            RateHistoryScreen(onBackClick = { navController.popBackStackSafely() })
        }
    }
}
```

</details>

### `feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/ui/RateHistoryViewModel.kt`

```kotlin
class RateHistoryViewModel(
```
Drives the historical chart. The pair and window form the store key, so changing either re-keys the stream rather than filtering in memory.

<details><summary>Used in the template — <code>feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/di/CurrencyRatesModule.kt:25</code></summary>

```kotlin
        )
    }
    viewModel { RateHistoryViewModel(get()) }
}
```

</details>

```kotlin
data class HistoryLocalState(val targetCurrency: String = "INR", val periodDays: Int = 30)
```
The chart's selection — which pair and how far back.

```kotlin
sealed interface HistoryAction
```
What the chart can be asked to do.

<details><summary>Used in the template — <code>feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/ui/RateHistoryScreen.kt:139</code></summary>

```kotlin
                        selectedPeriod = localState.periodDays,
                        onSelectCurrency = {
                            viewModel.trySendAction(HistoryAction.SelectCurrency(it))
                        },
                        onSelectPeriod = {
                            viewModel.trySendAction(HistoryAction.SelectPeriod(it))
                        },
```

</details>

### `feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/ui/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the currency-rates feature. Consumed by: - Compose UI tests in `feature/currency-rates/src/commonTest/` (`CurrencyRatesScreenUiTest`, `RateHistoryScreenUiTest`).

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

- `const val ROOT: String = "currency_rates_root"` — Root `Scaffold` of the currency-rates list screen.
- `const val CONVERTER: String = "currency_rates_converter"` — The currency-converter card — the NETWORK_ONLY/CACHE_ONLY spot-rate showcase surface (renders `CurrencyRatesViewModel.spotConversionRate`).
- `const val ROOT: String = "rate_history_root"` — Root `Scaffold` of the rate-history screen.

---

_11 type(s), 9 function(s)/property(ies); 19 carry KDoc at source; 0 authored example(s); 7 live call site(s)._
<!-- api-docs:end -->
