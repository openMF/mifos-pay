# `feature/pocket/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 19 tracked files — 14× `.kt`, 3× `.xml`, 1× `.md`, 1× `.kts`

**14 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `ManagePocketScreen`, `PocketDashboardScreen`

ViewModels: `ManagePocketViewModel`, `PocketDashboardViewModel`

### Docs in the tree

| Path | |
|---|---|
| `feature/pocket/README.md` | :feature:pocket module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/pocket sha=a8c39860b8bc84ffb7bad46302b299c46b6530d1 -->
## API reference

_Generated from `feature/pocket` at tree `a8c39860b8bc` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/pocket/src/commonMain/kotlin/org/mifospay/feature/pocket/di/PocketModule.kt`

```kotlin
val PocketModule = module
```
_No KDoc at source._

### `feature/pocket/src/commonMain/kotlin/org/mifospay/feature/pocket/navigation/ManagePocketRoute.kt`

```kotlin
fun NavController.navigateToManagePocket()
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.managePocketDestination(
```
_No KDoc at source._

### `feature/pocket/src/commonMain/kotlin/org/mifospay/feature/pocket/navigation/PocketDashboardRoute.kt`

```kotlin
fun NavController.navigateToPocketDashboard()
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.pocketDashboardScreen(
```
_No KDoc at source._

### `feature/pocket/src/commonMain/kotlin/org/mifospay/feature/pocket/screens/PocketDashboardScreen.kt`

```kotlin
fun AccountStatus.toColor(): Color =
```
_No KDoc at source._

### `feature/pocket/src/commonMain/kotlin/org/mifospay/feature/pocket/viewmodels/PocketDashboardViewModel.kt`

```kotlin
data class PocketDashboardState(
```
_No KDoc at source._

```kotlin
data class DetailedPocket(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/pocket/src/commonTest/kotlin/org/mifospay/feature/pocket/screens/PocketDashboardScreenTest.kt:47</code></summary>

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

_2 type(s), 6 function(s)/property(ies); 0 carry KDoc at source; 0 authored example(s); 1 live call site(s)._
<!-- api-docs:end -->
