# `feature/merchants/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 14 tracked files — 7× `.kt`, 4× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**7 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `MerchantScreen`, `MerchantTransferScreen`

ViewModels: `MerchantTransferViewModel`, `MerchantViewModel`

### Docs in the tree

| Path | |
|---|---|
| `feature/merchants/README.md` | :feature:merchants module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/merchants sha=ee9bd4fe3318dd80eef838b4f9c9692409cc7308 -->
## API reference

_Generated from `feature/merchants` at tree `ee9bd4fe3318` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/merchants/src/commonMain/kotlin/org/mifospay/feature/merchants/MerchantTransferViewModel.kt`

```kotlin
class MerchantTransferViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/org/mifospay/feature/merchants/di/MerchantsModule.kt:19</code></summary>

```kotlin
val MerchantsModule = module {
    viewModelOf(::MerchantViewModel)
    viewModelOf(::MerchantTransferViewModel)
}
```

</details>

```kotlin
sealed interface MerchantTransferEvent
```
_No KDoc at source._

```kotlin
sealed interface MerchantTransferAction
```
_No KDoc at source._

```kotlin
sealed class MerchantTransferUiState
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/org/mifospay/feature/merchants/ui/MerchantTransferScreen.kt:102</code></summary>

```kotlin
@VisibleForTesting
internal fun MerchantTransferScreen(
    uiState: MerchantTransferUiState,
    merchantName: String,
    merchantVPA: String,
    onBackPressed: () -> Unit,
    checkBalanceAvailability: (String, String) -> Unit,
```

</details>

### `feature/merchants/src/commonMain/kotlin/org/mifospay/feature/merchants/MerchantViewModel.kt`

```kotlin
class MerchantViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/org/mifospay/feature/merchants/di/MerchantsModule.kt:18</code></summary>

```kotlin

val MerchantsModule = module {
    viewModelOf(::MerchantViewModel)
    viewModelOf(::MerchantTransferViewModel)
}
```

</details>

```kotlin
data class MerchantState(
```
_No KDoc at source._

```kotlin
sealed interface MerchantEvent
```
_No KDoc at source._

```kotlin
sealed interface MerchantAction
```
_No KDoc at source._

```kotlin
sealed class MerchantUiState
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/org/mifospay/feature/merchants/ui/MerchantScreen.kt:81</code></summary>

```kotlin
@VisibleForTesting
internal fun MerchantScreen(
    merchantUiState: MerchantUiState,
    merchantListUiState: MerchantUiState,
    isRefreshing: Boolean,
    updateQuery: (String) -> Unit,
    onRefresh: () -> Unit,
```

</details>

### `feature/merchants/src/commonMain/kotlin/org/mifospay/feature/merchants/di/MerchantsModule.kt`

```kotlin
val MerchantsModule = module
```
_No KDoc at source._

### `feature/merchants/src/commonMain/kotlin/org/mifospay/feature/merchants/navigation/MerchantTransferNavigation.kt`

```kotlin
fun NavGraphBuilder.merchantTransferScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToMerchantTransferScreen(
```
_No KDoc at source._

### `feature/merchants/src/commonMain/kotlin/org/mifospay/feature/merchants/ui/MerchantScreen.kt`

```kotlin
fun MerchantScreen(
```
_No KDoc at source._

```kotlin
val sampleMerchantList = List(10)
```
_No KDoc at source._

---

_9 type(s), 5 function(s)/property(ies); 0 carry KDoc at source; 0 authored example(s); 4 live call site(s)._
<!-- api-docs:end -->
