# `feature/emi-calculator/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 31 tracked files — 20× `.xml`, 9× `.kt`, 1× `.md`, 1× `.kts`

**9 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `EmiCalculatorScreen`

ViewModels: `EmiCalculatorViewModel`

**Consumes:** `core/store/emi`

### Docs in the tree

| Path | |
|---|---|
| `feature/emi-calculator/README.md` | :feature:emi-calculator |

<!-- tree-scaffold:end -->
## Significance

The single-purpose EMI calculator, and the **`MEMORY_ONLY`** showcase.

Store-backed even though nothing persists: the calculation is deterministic in its inputs, so the params
double as the cache key and dragging a tenure slider back over a value already tried is served from
memory. It also means the result reaches the screen as a `ScreenState` like every other read surface,
so the calculator renders through the same wrapper as the rest of the app rather than a bespoke
nullable `StateFlow`.

<!-- api-docs:begin module=feature/emi-calculator sha=09c821e8a3d587e40c2f2b75fccf04b77e32ccd8 -->
## API reference

_Generated from `feature/emi-calculator` at tree `09c821e8a3d5` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/emi-calculator/src/commonMain/kotlin/kpt/feature/emicalculator/di/EmiCalculatorModule.kt`

```kotlin
val EmiCalculatorModule = module
```
Koin bindings for the EMI calculator. Listed in `ForkFeatureRegistry`'s generated bindings, so a fork that removes this feature drops the module with it.

### `feature/emi-calculator/src/commonMain/kotlin/kpt/feature/emicalculator/navigation/EmiCalculatorNavigation.kt`

```kotlin
data object EmiCalculatorRoute
```
Route for the EMI calculator.

```kotlin
fun NavController.navigateToEmiCalculator(navOptions: NavOptions? = null)
```
Navigates to the EMI calculator.

```kotlin
fun NavGraphBuilder.emiCalculatorDestination(navController: NavController)
```
Registers the EMI-calculator destination.

### `feature/emi-calculator/src/commonMain/kotlin/kpt/feature/emicalculator/ui/EmiCalculatorScreen.kt`

```kotlin
fun EmiCalculatorScreen(
```
Stateful entry point — resolves the ViewModel and hands its state to `EmiCalculatorScreenContent`.

<details><summary>Used in the template — <code>feature/emi-calculator/src/commonMain/kotlin/kpt/feature/emicalculator/navigation/EmiCalculatorNavigation.kt:47</code></summary>

```kotlin
        // `onBackClick` lambda: the aggregate invokes one uniform shape, and a bespoke signature
        // is exactly what kept this entry hand-wired in ForkFeatureRegistry.
        EmiCalculatorScreen(onBackClick = { navController.popBackStackSafely() })
    }
}
```

</details>

### `feature/emi-calculator/src/commonMain/kotlin/kpt/feature/emicalculator/ui/EmiCalculatorViewModel.kt`

```kotlin
class EmiCalculatorViewModel(
```
EMI calculator — the `calculator_pure` combo on the DYNAMIC-KEY read shape. The form inputs ARE the Store key, so each distinct parameter set is its own cache entry and `flatMapLatest` re-streams on every change.

<details><summary>Used in the template — <code>feature/emi-calculator/src/commonMain/kotlin/kpt/feature/emicalculator/di/EmiCalculatorModule.kt:32</code></summary>

```kotlin
    }

    viewModelOf(::EmiCalculatorViewModel)
}
```

</details>

```kotlin
data class EmiState(
```
The calculator's inputs.

<details><summary>Used in the template — <code>feature/emi-calculator/src/commonMain/kotlin/kpt/feature/emicalculator/ui/EmiCalculatorScreen.kt:92</code></summary>

```kotlin
@Composable
internal fun EmiCalculatorScreenContent(
    state: EmiState,
    emiState: ScreenState<EmiResult>,
    onBackClick: () -> Unit,
    onPrincipalChange: (Double) -> Unit,
    onRateChange: (Double) -> Unit,
```

</details>

```kotlin
sealed class EmiAction
```
One action per input.

<details><summary>Used in the template — <code>feature/emi-calculator/src/commonMain/kotlin/kpt/feature/emicalculator/ui/EmiCalculatorScreen.kt:80</code></summary>

```kotlin
        emiState = emiState,
        onBackClick = onBackClick,
        onPrincipalChange = { viewModel.trySendAction(EmiAction.UpdatePrincipal(it)) },
        onRateChange = { viewModel.trySendAction(EmiAction.UpdateRate(it)) },
        onTenureChange = { viewModel.trySendAction(EmiAction.UpdateTenure(it)) },
        onRetry = viewModel::onRetry,
        modifier = modifier,
```

</details>

### `feature/emi-calculator/src/commonMain/kotlin/kpt/feature/emicalculator/ui/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the emi-calculator feature. Consumed by Compose UI tests in `feature/emi-calculator/src/commonTest/`. APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).

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

- `const val SCREEN: String = "emi_calculator_screen"` — Root scaffold — always rendered regardless of input state.

---

_5 type(s), 5 function(s)/property(ies); 10 carry KDoc at source; 0 authored example(s); 5 live call site(s)._
<!-- api-docs:end -->
