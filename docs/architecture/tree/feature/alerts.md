# `feature/alerts/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 35 tracked files — 20× `.xml`, 13× `.kt`, 1× `.md`, 1× `.kts`

**13 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `AlertCreateScreen`, `AlertsListScreen`

ViewModels: `AlertCreateViewModel`, `AlertsListViewModel`

**Consumes:** `core/store/alerts`, `core/store/config`

**Store5 archetype(s) exercised:** `CACHE_ONLY`, `OFFLINE_LOCAL_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/alerts/README.md` | :feature:alerts |

<!-- tree-scaffold:end -->
## Significance

Price alerts — a **draft-backed create form** plus a local alert store. `targetValueText` is held as raw
text rather than a parsed number so a partially typed value survives recomposition; parsing happens
once, on submit.

<!-- api-docs:begin module=feature/alerts sha=a6a1a0beb04c9822eb9022cea944c1af14863333 -->
## API reference

_Generated from `feature/alerts` at tree `a6a1a0beb04c` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/di/AlertsModule.kt`

```kotlin
val AlertsModule = module
```
Koin module for the Alerts feature (`submit_offline_write` demo). - `AlertsListViewModel` reads the reactive `kpt.core.data.alerts.AlertsRepository`.

### `feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/navigation/AlertsNavigation.kt`

```kotlin
data object AlertsGraphRoute
```
Root of the alerts nav graph.

```kotlin
data object AlertsListRoute
```
Alerts list entry destination.

```kotlin
data object AlertCreateRoute
```
Offline-first create-alert form.

```kotlin
fun NavController.navigateToAlertsGraph(navOptions: NavOptions? = null)
```
Navigates to the alerts graph.

```kotlin
fun NavGraphBuilder.alertsGraph(navController: NavController)
```
Alerts feature's navigation graph — list → create. The host (cmp-navigation) wires this into the top-level RootNavGraph by calling `alertsGraph` inside its own `NavHost { ... }`.

### `feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/ui/AlertCreateScreen.kt`

```kotlin
fun AlertCreateScreen(
```
Create-Price-Alert form — the toolkit's `submit_offline_write` demo UI.

<details><summary>Used in the template — <code>feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/navigation/AlertsNavigation.kt:58</code></summary>

```kotlin
        }
        composableWithPushTransitions<AlertCreateRoute> {
            AlertCreateScreen(
                onBackClick = { navController.popBackStackSafely() },
                onSubmitted = { navController.popBackStackSafely() },
            )
        }
```

</details>

### `feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/ui/AlertCreateViewModel.kt`

```kotlin
class AlertCreateViewModel(
```
Create-Price-Alert ViewModel — the toolkit's canonical `submit_offline_write` demo.

<details><summary>Used in the template — <code>feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/di/AlertsModule.kt:29</code></summary>

```kotlin
    viewModel { AlertsListViewModel(repository = get()) }
    viewModel {
        AlertCreateViewModel(
            repository = get(),
            outbox = get(qualifier = AppOutboxQualifiers.PriceAlert),
        )
    }
```

</details>

```kotlin
data class AlertFormState(
```
Editable form body for the create-alert screen. Kept separate from the `PriceAlert` payload so partial/invalid input (`targetValueText` mid-type) never has to round-trip through the domain model.

<details><summary>Used in the template — <code>feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/ui/AlertCreateScreen.kt:100</code></summary>

```kotlin
@Composable
internal fun AlertCreateScreenContent(
    form: AlertFormState,
    screenState: ScreenState<PriceAlert>,
    submitState: SubmitState<PriceAlert>,
    hasResumableDraft: Boolean,
    onBackClick: () -> Unit,
```

</details>

### `feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/ui/AlertsListScreen.kt`

```kotlin
fun AlertsListScreen(
```
Price-alerts list — the read side of the toolkit's `submit_offline_write` demo. Renders saved alerts (offline Room-backed reactive list) with per-row delete; a FAB routes to the offline-first create form (`AlertCreateScreen`).

<details><summary>Used in the template — <code>feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/navigation/AlertsNavigation.kt:52</code></summary>

```kotlin
    navigation<AlertsGraphRoute>(startDestination = AlertsListRoute) {
        composableWithPushTransitions<AlertsListRoute> {
            AlertsListScreen(
                onBackClick = { navController.popBackStackSafely() },
                onCreateClick = { navController.navigate(AlertCreateRoute) },
            )
        }
```

</details>

### `feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/ui/AlertsListViewModel.kt`

```kotlin
class AlertsListViewModel(
```
Read-side ViewModel for the price-alerts list.

<details><summary>Used in the template — <code>feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/di/AlertsModule.kt:27</code></summary>

```kotlin
 */
val AlertsModule = module {
    viewModel { AlertsListViewModel(repository = get()) }
    viewModel {
        AlertCreateViewModel(
            repository = get(),
            outbox = get(qualifier = AppOutboxQualifiers.PriceAlert),
```

</details>

```kotlin
sealed interface AlertsListAction
```
One-shot actions accepted by `AlertsListViewModel`.

### `feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/ui/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the alerts feature. Consumed by Compose UI tests in `feature/alerts/src/commonTest/`. APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).

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

- `const val SCREEN: String = "alerts_list_screen"` — Root scaffold — always rendered regardless of load state.
- `const val CREATE_FAB: String = "alerts_create_fab"` — FAB that routes to the create-alert form.
- `const val DELETE_PREFIX: String = "alerts_delete_"` — Per-row delete button. Suffix with the alert id.
- `const val SCREEN: String = "alert_create_screen"` — Root scaffold — always rendered regardless of load/submit state.
- `const val COIN_FIELD: String = "alert_create_coin_field"` — Coin id text field.
- `const val TARGET_FIELD: String = "alert_create_target_field"` — Target price text field.
- `const val SUBMIT: String = "alert_create_submit"` — Submit button.

---

_8 type(s), 12 function(s)/property(ies); 20 carry KDoc at source; 0 authored example(s); 6 live call site(s)._
<!-- api-docs:end -->
