# `feature/pocket/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 19 tracked files — 14× `.kt`, 3× `.xml`, 1× `.md`, 1× `.kts`

**14 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `ManagePocketScreen`, `PocketDashboardScreen`

ViewModels: `ManagePocketViewModel`, `PocketDashboardViewModel`

**Consumes:** `core/store/repository`

### Docs in the tree

| Path | |
|---|---|
| `feature/pocket/README.md` | :feature:pocket module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/pocket sha=c5007dabd10016005d4610e6f16b3f2ab60189e7 -->
## API reference

_Generated from `feature/pocket` at tree `c5007dabd100` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/pocket/src/commonMain/kotlin/kpt/feature/pocket/di/PocketModule.kt`

```kotlin
val PocketModule = module
```
Koin module providing the pocket dashboard and manage-pocket ViewModels.

### `feature/pocket/src/commonMain/kotlin/kpt/feature/pocket/navigation/ManagePocketRoute.kt`

```kotlin
fun NavController.navigateToManagePocket()
```
Navigates to the manage-pocket screen.

```kotlin
fun NavGraphBuilder.managePocketDestination(
```
Registers the manage-pocket screen destination.

### `feature/pocket/src/commonMain/kotlin/kpt/feature/pocket/navigation/PocketDashboardRoute.kt`

```kotlin
fun NavController.navigateToPocketDashboard()
```
Navigates to the pocket dashboard screen.

```kotlin
fun NavGraphBuilder.pocketDashboardScreen(
```
Registers the pocket dashboard screen destination.

### `feature/pocket/src/commonMain/kotlin/kpt/feature/pocket/screens/PocketDashboardScreen.kt`

```kotlin
fun AccountStatus.toColor(): Color =
```
Maps an account status to the theme color used to display it.

### `feature/pocket/src/commonMain/kotlin/kpt/feature/pocket/viewmodels/PocketDashboardViewModel.kt`

```kotlin
data class PocketDashboardState(
```
UI state of the pocket dashboard screen.

```kotlin
data class DetailedPocket(
```
A pocket (account) entry shown on the pocket dashboard.

<details><summary>Used in the template — <code>feature/pocket/src/commonTest/kotlin/kpt/feature/pocket/screens/PocketDashboardScreenUiTest.kt:47</code></summary>

```kotlin
            totalBalance = "MX$ 10,000.00",
            savingsAccounts = listOf(
                DetailedPocket(
                    accountId = 1L,
                    name = "Emergency Fund",
                    accountNumber = "1004859238",
                    balanceOrStatus = "$ 5,000.00",
```

</details>

---

_2 type(s), 6 function(s)/property(ies); 8 carry KDoc at source; 0 authored example(s); 1 live call site(s)._
<!-- api-docs:end -->
