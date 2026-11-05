# `feature/kyc/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 20 tracked files — 13× `.kt`, 4× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**13 Kotlin files** — 4 screen(s), 4 ViewModel(s).

Screens: `KYCDescriptionScreen`, `KYCLevel1Screen`, `KYCLevel2Screen`, `KYCLevel3Screen`

ViewModels: `KYCDescriptionViewModel`, `KYCLevel1ViewModel`, `KYCLevel2ViewModel`, `KYCLevel3ViewModel`

### Docs in the tree

| Path | |
|---|---|
| `feature/kyc/README.md` | KYC Feature |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/kyc sha=d563709e097cf92672ca8af40484c18b936ccb28 -->
## API reference

_Generated from `feature/kyc` at tree `d563709e097c` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/KYCDescriptionScreen.kt`

```kotlin
fun KYCScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/navigation/KYCNavigation.kt:29</code></summary>

```kotlin
) {
    composable(route = KYC_ROUTE) {
        KYCScreen(
            onLevel1Clicked = onLevel1Clicked,
            onLevel2Clicked = onLevel2Clicked,
            onLevel3Clicked = onLevel3Clicked,
        )
```

</details>

### `feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/KYCDescriptionViewModel.kt`

```kotlin
class KYCDescriptionViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/KYCDescriptionScreen.kt:56</code></summary>

```kotlin
    onLevel3Clicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KYCDescriptionViewModel = koinViewModel(),
) {
    val state by viewModel.kycState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
```

</details>

```kotlin
data class KycState(
```
_No KDoc at source._

```kotlin
sealed interface KYCDescriptionUiState
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/KYCDescriptionScreen.kt:79</code></summary>

```kotlin
@Composable
private fun KYCDescriptionScreen(
    kUiState: KYCDescriptionUiState,
    onAction: (KycAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
```

</details>

```kotlin
enum class KycLevel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/KYCDescriptionScreen.kt:113</code></summary>

```kotlin
@Composable
private fun KYCDescriptionScreen(
    currentLevel: KycLevel?,
    onLevel1Clicked: () -> Unit,
    onLevel2Clicked: () -> Unit,
    onLevel3Clicked: () -> Unit,
    modifier: Modifier = Modifier,
```

</details>

```kotlin
sealed interface KycEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/KYCDescriptionScreen.kt:62</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            KycEvent.OnLevel1Clicked -> onLevel1Clicked.invoke()
            KycEvent.OnLevel2Clicked -> onLevel2Clicked.invoke()
            KycEvent.OnLevel3Clicked -> onLevel3Clicked.invoke()
        }
    }
```

</details>

```kotlin
sealed interface KycAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/KYCDescriptionScreen.kt:80</code></summary>

```kotlin
private fun KYCDescriptionScreen(
    kUiState: KYCDescriptionUiState,
    onAction: (KycAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
```

</details>

### `feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/KYCLevel3Screen.kt`

```kotlin
fun KYCLevel3ScreenContent(
```
_No KDoc at source._

### `feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/KYCLevel3ViewModel.kt`

```kotlin
class KYCLevel3ViewModel :
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/KYCLevel3Screen.kt:26</code></summary>

```kotlin
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KYCLevel3ViewModel = koinViewModel(),
) {
    KYCLevel3ScreenContent(
        modifier = modifier,
        navigateBack = navigateBack,
```

</details>

```kotlin
data object KYCLevel3State
```
_No KDoc at source._

```kotlin
sealed interface KYCLevel3Event
```
_No KDoc at source._

```kotlin
sealed interface KYCLevel3Action
```
_No KDoc at source._

### `feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/di/KYCModule.kt`

```kotlin
val KYCModule = module
```
_No KDoc at source._

### `feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/navigation/KYCLevel1Navigation.kt`

```kotlin
fun NavController.navigateToKYCLevel1()
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.kycLevel1Screen(
```
_No KDoc at source._

### `feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/navigation/KYCLevel2Navigation.kt`

```kotlin
fun NavController.navigateToKYCLevel2(navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.kycLevel2Screen(
```
_No KDoc at source._

### `feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/navigation/KYCLevel3Navigation.kt`

```kotlin
fun NavController.navigateToKYCLevel3(navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.kycLevel3Screen(
```
_No KDoc at source._

### `feature/kyc/src/commonMain/kotlin/org/mifospay/feature/kyc/navigation/KYCNavigation.kt`

```kotlin
fun NavController.navigateToKYC()
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.kycScreen(
```
_No KDoc at source._

---

_10 type(s), 11 function(s)/property(ies); 0 carry KDoc at source; 0 authored example(s); 7 live call site(s)._
<!-- api-docs:end -->
