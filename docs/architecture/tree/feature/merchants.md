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

<!-- api-docs:begin module=feature/merchants sha=16d505f85fc77ebe3564c33833e39991bdd03fa5 -->
## API reference

_Generated from `feature/merchants` at tree `16d505f85fc7` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/MerchantTransferViewModel.kt`

```kotlin
class MerchantTransferViewModel(
```
ViewModel of the merchant transfer screen; currently a stub with no backend or actions.

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/di/MerchantsModule.kt:19</code></summary>

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
One-off events of the merchant transfer screen (none defined).

```kotlin
sealed interface MerchantTransferAction
```
User actions of the merchant transfer screen (none defined).

```kotlin
sealed class MerchantTransferUiState
```
UI states of the merchant transfer screen.

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/ui/MerchantTransferScreen.kt:101</code></summary>

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

### `feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/MerchantViewModel.kt`

```kotlin
class MerchantViewModel(
```
ViewModel of the merchants screen; holds the merchant list, search query and refresh state.

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/di/MerchantsModule.kt:18</code></summary>

```kotlin
/** Koin module providing the merchant list and merchant transfer view models. */
val MerchantsModule = module {
    viewModelOf(::MerchantViewModel)
    viewModelOf(::MerchantTransferViewModel)
}
```

</details>

```kotlin
data class MerchantState(
```
Single MVI state of the merchants screen.

```kotlin
sealed interface MerchantEvent
```
One-off events of the merchants screen (none defined).

```kotlin
sealed interface MerchantAction
```
User actions on the merchants screen.

```kotlin
sealed class MerchantUiState
```
UI states of merchant content.

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/ui/MerchantScreen.kt:81</code></summary>

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

### `feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/di/MerchantsModule.kt`

```kotlin
val MerchantsModule = module
```
Koin module providing the merchant list and merchant transfer view models.

### `feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/navigation/MerchantTransferNavigation.kt`

```kotlin
fun NavGraphBuilder.merchantTransferScreen(
```
Registers the merchant transfer destination, taking merchant name, VPA and account number as arguments.

```kotlin
fun NavController.navigateToMerchantTransferScreen(
```
Navigates to the transfer screen for the given merchant name, VPA and account number.

### `feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/ui/MerchantScreen.kt`

```kotlin
fun MerchantScreen(
```
Screen listing merchants with search and pull-to-refresh.

```kotlin
val sampleMerchantList = List(10)
```
Sample merchant list used for previews.

---

_9 type(s), 5 function(s)/property(ies); 14 carry KDoc at source; 0 authored example(s); 4 live call site(s)._
<!-- api-docs:end -->
