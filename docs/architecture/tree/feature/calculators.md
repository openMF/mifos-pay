# `feature/calculators/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 49 tracked files — 27× `.kt`, 20× `.xml`, 1× `.md`, 1× `.kts`

**27 Kotlin files** — 4 screen(s), 4 ViewModel(s).

Screens: `AffordabilityCalculatorScreen`, `AmortizationScreen`, `LoanCalcWizardScreen`, `LoanComparisonScreen`

ViewModels: `AffordabilityCalculatorViewModel`, `AmortizationViewModel`, `LoanCalcWizardViewModel`, `LoanComparisonViewModel`

**Consumes:** `core/store/banking`, `core/store/calc`, `core/store/config`

**Store5 archetype(s) exercised:** `CACHE_ONLY`, `LOAD_ONCE`, `OFFLINE_LOCAL_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/calculators/README.md` | :feature:calculators |

<!-- tree-scaffold:end -->
## Significance

Four calculators — affordability, amortization, comparison and a multi-step wizard — and the widest
range of screen patterns in one module.

The wizard is the **`DraftSubmitHandler`** showcase: it persists its form on every step so a part-filled
wizard survives process death, and resumes with a Continue / Discard prompt. The other three are pure
local state with no Store at all, which is the other thing this module demonstrates — that the
framework does not force a Store where there is no data to fetch.

<!-- api-docs:begin module=feature/calculators sha=246828f8b7aa9946d05f5eb38137529145bdce0a -->
## API reference

_Generated from `feature/calculators` at tree `246828f8b7aa` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the calculators feature. Consumed by Compose UI tests in `feature/calculators/src/commonTest/`.

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

- `const val SCREEN: String = "calc_affordability_screen"` — Root scaffold — always rendered regardless of input state.
- `const val SCREEN: String = "calc_amortization_screen"` — Root scaffold — always rendered regardless of input state.
- `const val SCREEN: String = "calc_comparison_screen"` — Root scaffold — always rendered regardless of input state.
- `const val SCREEN: String = "calc_wizard_screen"` — Root scaffold — always rendered regardless of wizard step.

### `feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/affordability/AffordabilityCalculatorScreen.kt`

```kotlin
fun AffordabilityCalculatorScreen(
```
Stateful entry point — resolves the ViewModel and hands its state to `AffordabilityCalculatorScreenContent`.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/navigation/CalculatorsNavigation.kt:114</code></summary>

```kotlin
    ) {
        composableWithPushTransitions<AffordabilityCalculatorRoute> {
            AffordabilityCalculatorScreen(onBackClick = { navController.popBackStackSafely() })
        }
        composableWithPushTransitions<AmortizationRoute> { entry ->
            val route = entry.toRoute<AmortizationRoute>()
            AmortizationScreen(
```

</details>

### `feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/affordability/AffordabilityCalculatorViewModel.kt`

```kotlin
class AffordabilityCalculatorViewModel :
```
Pure-compute VM for B5 Affordability Calculator. Demonstrates the "no Store, no network" archetype: input state → derived `AffordabilityResult` flow.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/affordability/AffordabilityCalculatorScreen.kt:71</code></summary>

```kotlin
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AffordabilityCalculatorViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val result by viewModel.affordability.collectAsStateWithLifecycle()
```

</details>

```kotlin
data class AffordabilityState(
```
The calculator's inputs. Pure local state — nothing is persisted or fetched, so the state IS the screen.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/affordability/AffordabilityCalculatorScreen.kt:93</code></summary>

```kotlin
@Composable
internal fun AffordabilityCalculatorScreenContent(
    state: AffordabilityState,
    result: AffordabilityResult,
    onBackClick: () -> Unit,
    onIncomeChange: (Double) -> Unit,
    onObligationsChange: (Double) -> Unit,
```

</details>

```kotlin
sealed class AffordabilityAction
```
One action per input, so a change is a single well-typed event rather than a whole-state replacement.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/affordability/AffordabilityCalculatorScreen.kt:80</code></summary>

```kotlin
        result = result,
        onBackClick = onBackClick,
        onIncomeChange = { viewModel.trySendAction(AffordabilityAction.UpdateIncome(it)) },
        onObligationsChange = { viewModel.trySendAction(AffordabilityAction.UpdateObligations(it)) },
        onDtiPercentChange = { viewModel.trySendAction(AffordabilityAction.UpdateDti(it / 100.0)) },
        onRateChange = { viewModel.trySendAction(AffordabilityAction.UpdateRate(it)) },
        onTenureChange = { viewModel.trySendAction(AffordabilityAction.UpdateTenure(it)) },
```

</details>

### `feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/amortizationcalc/AmortizationScreen.kt`

```kotlin
fun AmortizationScreen(
```
The amortization schedule — month-by-month principal/interest split for a loan.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/navigation/CalculatorsNavigation.kt:118</code></summary>

```kotlin
        composableWithPushTransitions<AmortizationRoute> { entry ->
            val route = entry.toRoute<AmortizationRoute>()
            AmortizationScreen(
                onBackClick = { navController.popBackStackSafely() },
                loanId = route.loanId,
            )
        }
```

</details>

### `feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/amortizationcalc/AmortizationViewModel.kt`

```kotlin
class AmortizationViewModel(
```
VM for B3 Amortization Schedule. Two modes: - **Loan-backed** — when `loanId` is non-null, on init the VM reads the matching loan from `LoanRepository.observeById` and pre-fills the inputs.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/amortizationcalc/AmortizationScreen.kt:71</code></summary>

```kotlin
    modifier: Modifier = Modifier,
    loanId: String? = null,
    viewModel: AmortizationViewModel = koinViewModel { parametersOf(loanId) },
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val breakdownState by viewModel.breakdownState.collectAsStateWithLifecycle()
```

</details>

```kotlin
data class AmortizationState(
```
The schedule's inputs, plus which tracked loan they were prefilled from.

```kotlin
sealed class AmortizationAction
```
One action per input.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/amortizationcalc/AmortizationScreen.kt:116</code></summary>

```kotlin
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                viewModel.trySendAction(AmortizationAction.UpdatePrincipal(v))
                            }
                        },
                        label = { Text(stringResource(Res.string.screens_calc_amortization_principal_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
```

</details>

### `feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/comparison/LoanComparisonScreen.kt`

```kotlin
fun LoanComparisonScreen(
```
Compare Loans — full-width vertical-stack redesign.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/navigation/CalculatorsNavigation.kt:124</code></summary>

```kotlin
        }
        composableWithPushTransitions<LoanComparisonRoute> {
            LoanComparisonScreen(onBackClick = { navController.popBackStackSafely() })
        }
        composableWithPushTransitions<LoanCalcWizardRoute> { entry ->
            val route = entry.toRoute<LoanCalcWizardRoute>()
            LoanCalcWizardScreen(
```

</details>

### `feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/comparison/LoanComparisonViewModel.kt`

```kotlin
class LoanComparisonViewModel :
```
VM for B6 Loan Comparison. Manages exactly 3 loan scenarios side-by-side and emits a derived analysis (per-scenario `EmiResult` + index of the cheapest by total payable).

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/comparison/LoanComparisonScreen.kt:90</code></summary>

```kotlin
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanComparisonViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val analysis by viewModel.analysis.collectAsStateWithLifecycle()
```

</details>

```kotlin
data class LoanScenario(
```
One loan being compared.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/comparison/LoanComparisonScreen.kt:192</code></summary>

```kotlin
internal fun ScenarioCard(
    index: Int,
    scenario: LoanScenario,
    result: EmiResult?,
    isCheapest: Boolean,
    onChange: (LoanScenario) -> Unit,
) {
```

</details>

```kotlin
data class LoanComparisonState(
```
The scenarios under comparison.

```kotlin
data class LoanComparisonAnalysis(
```
The computed comparison: one EMI result per scenario plus which is cheapest. Derived state — never stored, so it cannot fall out of step with the scenarios on screen.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/comparison/LoanComparisonScreen.kt:150</code></summary>

```kotlin

@Composable
internal fun ComparisonHero(analysis: LoanComparisonAnalysis) {
    val sp = MaterialTheme.spacing
    val cheapest = analysis.results.getOrNull(analysis.cheapestIndex)
    val mostExpensive = analysis.results.maxByOrNull { it.totalPayment }
    val savings = if (cheapest != null && mostExpensive != null) {
```

</details>

```kotlin
sealed class LoanComparisonAction
```
What the comparison can be asked to do.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/comparison/LoanComparisonScreen.kt:140</code></summary>

```kotlin
                    onChange = { updated ->
                        viewModel.trySendAction(
                            LoanComparisonAction.UpdateScenario(idx, updated),
                        )
                    },
                )
            }
```

</details>

### `feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/di/CalculatorsModule.kt`

```kotlin
val CalculatorsModule = module
```
DI module for the four calculator ViewModels. - `AffordabilityCalculatorViewModel` / `LoanComparisonViewModel` — zero-dep pure-compute VMs.

### `feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/navigation/CalculatorsNavigation.kt`

```kotlin
data object CalculatorsGraphRoute
```
Route for the calculators nested graph.

```kotlin
data object AffordabilityCalculatorRoute
```
Route for the affordability calculator — the graph's start destination.

```kotlin
data class AmortizationRoute(val loanId: String? = null)
```
Route for the amortization schedule.

```kotlin
data object LoanComparisonRoute
```
Route for the side-by-side loan comparison.

```kotlin
data class LoanCalcWizardRoute(val scenarioId: String? = null)
```
Route for the multi-step loan wizard.

```kotlin
fun NavController.navigateToCalculators(navOptions: NavOptions? = null)
```
Navigates to the calculators graph.

```kotlin
fun NavController.navigateToAffordability(navOptions: NavOptions? = null)
```
Navigates to the affordability calculator.

```kotlin
fun NavController.navigateToAmortization(loanId: String? = null, navOptions: NavOptions? = null)
```
Navigates to the amortization schedule.

```kotlin
fun NavController.navigateToLoanComparison(navOptions: NavOptions? = null)
```
Navigates to the loan comparison.

```kotlin
fun NavController.navigateToLoanCalcWizard(scenarioId: String? = null, navOptions: NavOptions? = null)
```
Navigates to the loan wizard.

```kotlin
fun NavGraphBuilder.calculatorsGraph(navController: NavController)
```
Registers the calculators nested graph.

### `feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/wizard/LoanCalcWizardScreen.kt`

```kotlin
fun LoanCalcWizardScreen(
```
The multi-step loan wizard — the `DraftSubmitHandler` showcase, so a part-filled form survives process death.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/navigation/CalculatorsNavigation.kt:128</code></summary>

```kotlin
        composableWithPushTransitions<LoanCalcWizardRoute> { entry ->
            val route = entry.toRoute<LoanCalcWizardRoute>()
            LoanCalcWizardScreen(
                onBackClick = { navController.popBackStackSafely() },
                scenarioId = route.scenarioId,
            )
        }
```

</details>

### `feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/wizard/LoanCalcWizardViewModel.kt`

```kotlin
class LoanCalcWizardViewModel(
```
**Multi-step DraftSubmit showcase** — the B2 Loan Calculator Wizard.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/di/CalculatorsModule.kt:78</code></summary>

```kotlin
    }
    viewModel { (scenarioId: String?) ->
        LoanCalcWizardViewModel(
            outbox = get(qualifier = AppOutboxQualifiers.LoanCalcScenario),
            repository = get(),
            scenarioIdArg = scenarioId,
        )
```

</details>

---

_18 type(s), 15 function(s)/property(ies); 33 carry KDoc at source; 0 authored example(s); 15 live call site(s)._
<!-- api-docs:end -->
