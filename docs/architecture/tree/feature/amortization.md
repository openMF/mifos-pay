# `feature/amortization/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 31 tracked files — 20× `.xml`, 9× `.kt`, 1× `.md`, 1× `.kts`

**9 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `AmortizationScheduleScreen`

ViewModels: `AmortizationScheduleViewModel`

**Consumes:** `core/store/banking`

**Store5 archetype(s) exercised:** `CACHE_ONLY`, `LOAD_ONCE`, `OFFLINE_LOCAL_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/amortization/README.md` | :feature:amortization |

<!-- tree-scaffold:end -->
## Significance

The month-by-month payment schedule for a tracked loan — **`OFFLINE_LOCAL_ONLY`**, a projection of the
loan repository with no network involved.

Its cost scales with tenure: a 30-year loan is 360 rows. That is why the schedule is Store-backed
despite being computed — a revisit is free, and `SCHEDULE_ROWS` is a crash key precisely because an
out-of-memory failure here scales with that number.

<!-- api-docs:begin module=feature/amortization sha=d45a2fe4984bc82319280e36a30e25bb8be34cad -->
## API reference

_Generated from `feature/amortization` at tree `d45a2fe4984b` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/amortization/src/commonMain/kotlin/kpt/feature/amortization/di/AmortizationModule.kt`

```kotlin
val AmortizationModule = module
```
Koin module for the amortization-schedule feature. Wired into the app graph by `cmp-navigation/.../KoinModules.kt`. The `LoanRepository` binding is provided by `core/data/.../RepositoryModule.kt`.

### `feature/amortization/src/commonMain/kotlin/kpt/feature/amortization/navigation/AmortizationNavigation.kt`

```kotlin
data class AmortizationScheduleRoute(val loanId: String)
```
Route for one loan's amortization schedule.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/navigation/LoansNavigation.kt:86</code></summary>

```kotlin
                },
                onAmortizationClick = { loanId ->
                    navController.navigate(AmortizationScheduleRoute(loanId))
                },
            )
        }
        composableWithPushTransitions<AddOrEditLoanRoute> { entry ->
```

</details>

```kotlin
fun NavController.navigateToAmortizationSchedule(loanId: String)
```
Navigates to a loan's amortization schedule.

```kotlin
fun NavGraphBuilder.amortizationScheduleDestination(navController: NavController)
```
Registers the amortization-schedule destination.

### `feature/amortization/src/commonMain/kotlin/kpt/feature/amortization/ui/AmortizationScheduleScreen.kt`

```kotlin
fun AmortizationScheduleScreen(
```
Month-by-month payment breakdown for one tracked loan.

<details><summary>Used in the template — <code>feature/amortization/src/commonMain/kotlin/kpt/feature/amortization/navigation/AmortizationNavigation.kt:47</code></summary>

```kotlin
    composableWithPushTransitions<AmortizationScheduleRoute> { entry ->
        val route = entry.toRoute<AmortizationScheduleRoute>()
        AmortizationScheduleScreen(
            loanId = route.loanId,
            onBackClick = { navController.popBackStackSafely() },
        )
    }
```

</details>

### `feature/amortization/src/commonMain/kotlin/kpt/feature/amortization/ui/AmortizationScheduleViewModel.kt`

```kotlin
class AmortizationScheduleViewModel(
```
ViewModel for `AmortizationScheduleScreen`. Derives a complete month-by-month payment schedule from the current loan snapshot held in `LoanRepository`.

<details><summary>Used in the template — <code>feature/amortization/src/commonMain/kotlin/kpt/feature/amortization/di/AmortizationModule.kt:24</code></summary>

```kotlin
val AmortizationModule = module {
    viewModel { params ->
        AmortizationScheduleViewModel(repository = get(), loanId = params.get())
    }
}
```

</details>

### `feature/amortization/src/commonMain/kotlin/kpt/feature/amortization/ui/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the amortization feature. Consumed by Compose UI tests in `feature/amortization/src/commonTest/`. APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).

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

- `const val SCREEN: String = "amortization_schedule_screen"` — Root scaffold — always rendered regardless of load state.

---

_3 type(s), 5 function(s)/property(ies); 8 carry KDoc at source; 0 authored example(s); 4 live call site(s)._
<!-- api-docs:end -->
