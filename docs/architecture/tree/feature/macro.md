# `feature/macro/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 44 tracked files — 22× `.kt`, 20× `.xml`, 1× `.md`, 1× `.kts`

**22 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `CountryMacroScreen`, `CountryPickerScreen`, `MacroIndicatorDetailScreen`

ViewModels: `CountryMacroViewModel`, `CountryPickerViewModel`, `MacroIndicatorDetailViewModel`

**Consumes:** `core/store/economic`

**Store5 archetype(s) exercised:** `MEMORY_ONLY`, `NETWORK_WITH_CACHE` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/macro/README.md` | :feature:macro |

<!-- tree-scaffold:end -->
## Significance

World Bank country indicators — GDP, CPI, unemployment — and the **multi-source combine with a country
picker**.

Three cards, three independent states, and an aggregate freshness band so the TopAppBar can show one
STALE badge without a caller folding the three by hand. The picker round-trips a country code through
navigation rather than holding it in a shared singleton, which is what keeps the screen restorable.

<!-- api-docs:begin module=feature/macro sha=9e1afe9c00f837ba68eb0cf7eafd3f741d95912b -->
## API reference

_Generated from `feature/macro` at tree `9e1afe9c00f8` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/macro/src/commonMain/kotlin/kpt/feature/macro/di/MacroModule.kt`

```kotlin
val MacroModule = module
```
Koin module for the Country Macro Snapshot feature. - `CountryMacroViewModel` takes the initial country code as a parameter so the navigation entry can pre-select (US by default; query-string-driven in deeplinks).

### `feature/macro/src/commonMain/kotlin/kpt/feature/macro/navigation/MacroNavigation.kt`

```kotlin
data object MacroGraphRoute
```
Root of the country-macro nav graph.

```kotlin
data class CountryMacroRoute(val countryCode: String = "US")
```
Dashboard entry. The default country is the United States; the picker round-trips a new code via `CountryPickerRoute`.

```kotlin
data object CountryPickerRoute
```
Picker overlay launched from the dashboard's country chip.

```kotlin
data class IndicatorDetailRoute(
```
Full-history detail screen for a single indicator.

```kotlin
fun NavController.navigateToMacroGraph(navOptions: NavOptions? = null)
```
Navigates to the macro graph.

```kotlin
fun NavGraphBuilder.macroGraph(navController: NavController)
```
Macro feature's navigation graph. The host (cmp-navigation) wires this into the top-level RootNavGraph by calling `macroGraph` inside its own `NavHost { ... }` builder once this feature is enabled.

### `feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroScreen.kt`

```kotlin
fun CountryMacroScreen(
```
Stateful entry point — resolves the ViewModel and hands its state to `CountryMacroScreenContent`.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/navigation/MacroNavigation.kt:85</code></summary>

```kotlin
        composableWithPushTransitions<CountryMacroRoute> { entry ->
            val route = entry.toRoute<CountryMacroRoute>()
            CountryMacroScreen(
                countryCode = route.countryCode,
                onBackClick = { navController.popBackStackSafely() },
                onPickCountry = { navController.navigate(CountryPickerRoute) },
                onOpenIndicator = { kind ->
```

</details>

### `feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroViewModel.kt`

```kotlin
class CountryMacroViewModel(
```
Country Macro Snapshot ViewModel. Multi-source-combine showcase: holds **three independent** `ScreenDataStream`s (GDP, Inflation, Unemployment) and exposes their states as three independent cells on `MacroUiState`.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/di/MacroModule.kt:32</code></summary>

```kotlin
val MacroModule = module {
    viewModel { (countryCode: String) ->
        CountryMacroViewModel(initialCountryCode = countryCode, repository = get())
    }
    viewModel { (countryCode: String, kind: IndicatorKind) ->
        MacroIndicatorDetailViewModel(
            countryCode = countryCode,
```

</details>

```kotlin
data class MacroUiState(
```
Aggregated UI state for the country-macro dashboard. Each indicator is its own `ScreenState` so the screen can render three cards in three independent states (e.g., GDP showing content while inflation is still loading).

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroScreen.kt:115</code></summary>

```kotlin
@Composable
internal fun CountryMacroScreenContent(
    uiState: MacroUiState,
    onBackClick: () -> Unit,
    onPickCountry: () -> Unit,
    onOpenIndicator: (IndicatorKind) -> Unit,
    onRefreshAll: () -> Unit,
```

</details>

```kotlin
sealed interface MacroAction
```
User intents the country-macro dashboard accepts.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroScreen.kt:94</code></summary>

```kotlin
        onPickCountry = onPickCountry,
        onOpenIndicator = onOpenIndicator,
        onRefreshAll = { viewModel.trySendAction(MacroAction.RefreshAll) },
        onRetryIndicator = { index -> viewModel.trySendAction(MACRO_RETRY_ACTIONS[index]) },
        modifier = modifier,
    )
}
```

</details>

### `feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryPickerScreen.kt`

```kotlin
fun CountryPickerScreen(
```
Country picker — search + flag-emoji list. Selecting a row reports the chosen `Country.code` back via `onCountryPicked`; the host screen owns the "navigate back, ChangeCountry action" wiring.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/navigation/MacroNavigation.kt:101</code></summary>

```kotlin

        composableWithPushTransitions<CountryPickerRoute> {
            CountryPickerScreen(
                onBackClick = { navController.popBackStackSafely() },
                onCountryPicked = { code ->
                    // Replace the dashboard's countryCode so the ViewModel
                    // hosting the new country is created cleanly. The picker
```

</details>

### `feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryPickerViewModel.kt`

```kotlin
class CountryPickerViewModel : BaseViewModel<CountryPickerState, Nothing, CountryPickerAction>(
```
Pure-local picker over `SupportedCountries`. The list is static and bundled at compile time — no Store, no network, no loading state.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/di/MacroModule.kt:41</code></summary>

```kotlin
        )
    }
    viewModel { CountryPickerViewModel() }
}
```

</details>

```kotlin
data class CountryPickerState(
```
Picker UI state.

```kotlin
sealed interface CountryPickerAction
```
User intents the picker accepts.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryPickerScreen.kt:90</code></summary>

```kotlin
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.trySendAction(CountryPickerAction.Search(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(Res.string.screens_macro_picker_search_placeholder)) },
```

</details>

### `feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/MacroIndicatorDetailScreen.kt`

```kotlin
fun MacroIndicatorDetailScreen(
```
Full-history view for a single (country, indicator) pair. The top half is a full-width sparkline of the historical series; below it, a table of year/value rows for users who want exact numbers.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/navigation/MacroNavigation.kt:116</code></summary>

```kotlin
        composableWithPushTransitions<IndicatorDetailRoute> { entry ->
            val route = entry.toRoute<IndicatorDetailRoute>()
            MacroIndicatorDetailScreen(
                countryCode = route.countryCode,
                indicatorKind = IndicatorKind.valueOf(route.indicatorKindName),
                onBackClick = { navController.popBackStackSafely() },
            )
```

</details>

### `feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/MacroIndicatorDetailViewModel.kt`

```kotlin
class MacroIndicatorDetailViewModel(
```
Deep-dive ViewModel for a single (country, indicator) tuple. Used by the indicator-detail screen — full historical series, table view, single retry.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/di/MacroModule.kt:35</code></summary>

```kotlin
    }
    viewModel { (countryCode: String, kind: IndicatorKind) ->
        MacroIndicatorDetailViewModel(
            countryCode = countryCode,
            indicatorKind = kind,
            repository = get(),
        )
```

</details>

```kotlin
sealed interface MacroDetailAction
```
What the detail screen can be asked to do.

### `feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the macro feature. Consumed by Compose UI tests in `feature/macro/src/commonTest/`. APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).

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

- `const val SCREEN: String = "country_macro_screen"` — Root scaffold — always rendered regardless of load state.
- `const val SCREEN: String = "country_picker_screen"` — Root scaffold — always rendered regardless of load state.
- `const val SCREEN: String = "macro_indicator_detail_screen"` — Root scaffold — always rendered regardless of load state.

### `feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/components/IndicatorCard.kt`

```kotlin
fun MacroIndicatorCardChrome(
```
Persistent per-card frame for a macro indicator: the `AppCard` shell + accent stripe + title, rendered identically across every `ScreenState`.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroScreen.kt:195</code></summary>

```kotlin
                onRetry = onRetryIndicator,
                cardChrome = { index, card ->
                    MacroIndicatorCardChrome(
                        indicatorKind = macroKinds[index],
                        onClick = { onOpenIndicator(macroKinds[index]) },
                    ) { card() }
                },
```

</details>

```kotlin
fun MacroContentBody(indicator: MacroIndicator, modifier: Modifier = Modifier)
```
The card body for a loaded indicator — headline figure plus its sparkline.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroScreen.kt:224</code></summary>

```kotlin
                },
            ) { _, data, _ ->
                MacroContentBody(data)
            }
        }
    }
}
```

</details>

```kotlin
fun MacroLoadingBody(modifier: Modifier = Modifier)
```
The card body while the indicator loads, sized to match the content body so the card does not jump.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroScreen.kt:200</code></summary>

```kotlin
                    ) { card() }
                },
                loading = { MacroLoadingBody() },
                empty = { index ->
                    MacroInlineMessage(
                        text = stringResource(Res.string.screens_macro_card_empty),
                        onRetry = { onRetryIndicator(index) },
```

</details>

```kotlin
fun MacroInlineMessage(text: String, onRetry: () -> Unit, modifier: Modifier = Modifier)
```
An in-card message with a retry action, for a cell that failed while its siblings succeeded.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroScreen.kt:202</code></summary>

```kotlin
                loading = { MacroLoadingBody() },
                empty = { index ->
                    MacroInlineMessage(
                        text = stringResource(Res.string.screens_macro_card_empty),
                        onRetry = { onRetryIndicator(index) },
                    )
                },
```

</details>

### `feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/components/IndicatorFormatting.kt`

```kotlin
fun IndicatorKind.displayName(): String = when (this)
```
Localised display name for an `IndicatorKind` — what the IndicatorCard's title text reads. Kept here next to its formatter so future labels can be added without touching the screen code.

```kotlin
fun MacroIndicator.headlineValue(): String
```
Best-effort headline value formatter.

```kotlin
fun MacroIndicator.latestYear(): Int? = observations.asReversed().firstOrNull { it.value != null }?.year
```
Year label for the headline value — used as the small subtitle under the value (e.g. "Latest: 2023"). Returns null when no usable observation exists.

### `feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/components/Sparkline.kt`

```kotlin
fun Sparkline(
```
Lightweight inline sparkline. Plots `values` horizontally as a polyline inside `modifier`'s bounds, mapping min..max to bottom..top. Renders no axis, no labels, no grid.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/MacroIndicatorDetailScreen.kt:145</code></summary>

```kotlin
                }
                AppCard {
                    Sparkline(
                        values = indicator.observations.map { it.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
```

</details>

---

_13 type(s), 17 function(s)/property(ies); 30 carry KDoc at source; 0 authored example(s); 15 live call site(s)._
<!-- api-docs:end -->
