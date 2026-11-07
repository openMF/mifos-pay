# `feature/kyc/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 20 tracked files — 13× `.kt`, 4× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**13 Kotlin files** — 4 screen(s), 4 ViewModel(s).

Screens: `KYCDescriptionScreen`, `KYCLevel1Screen`, `KYCLevel2Screen`, `KYCLevel3Screen`

ViewModels: `KYCDescriptionViewModel`, `KYCLevel1ViewModel`, `KYCLevel2ViewModel`, `KYCLevel3ViewModel`

**Consumes:** `core/store/repository`, `core/store/util`

### Docs in the tree

| Path | |
|---|---|
| `feature/kyc/README.md` | KYC Feature |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/kyc sha=46d0223c85d457837c9f97330b6d50ee8580e0f5 -->
## API reference

_Generated from `feature/kyc` at tree `46d0223c85d4` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCDescriptionScreen.kt`

```kotlin
fun KYCScreen(
```
Screen describing the KYC levels; each level calls its matching click callback.

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/navigation/KYCNavigation.kt:31</code></summary>

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

### `feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCDescriptionViewModel.kt`

```kotlin
class KYCDescriptionViewModel(
```
ViewModel exposing the client's current KYC level and routing level selection events.

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCDescriptionScreen.kt:56</code></summary>

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
Persisted UI state of the KYC description screen.

```kotlin
sealed interface KYCDescriptionUiState
```
Loading, content and error states of the KYC description screen.

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCDescriptionScreen.kt:79</code></summary>

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
KYC verification levels offered in the wallet.

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCDescriptionScreen.kt:113</code></summary>

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
One-off navigation events emitted by the KYC description screen.

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCDescriptionScreen.kt:62</code></summary>

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
User actions on the KYC description screen.

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCDescriptionScreen.kt:80</code></summary>

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

### `feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCLevel3Screen.kt`

```kotlin
fun KYCLevel3ScreenContent(
```
Static KYC level 3 (review and submit) screen body with a back button via `navigateBack`.

### `feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCLevel3ViewModel.kt`

```kotlin
class KYCLevel3ViewModel :
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCLevel3Screen.kt:25</code></summary>

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
Empty UI state of the KYC level 3 stub screen.

```kotlin
sealed interface KYCLevel3Event
```
One-off events of the KYC level 3 screen (none defined).

```kotlin
sealed interface KYCLevel3Action
```
User actions of the KYC level 3 screen (none defined).

### `feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/di/KYCModule.kt`

```kotlin
val KYCModule = module
```
Koin module providing the KYC description and level 1 to 3 view models.

### `feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/navigation/KYCLevel1Navigation.kt`

```kotlin
fun NavController.navigateToKYCLevel1()
```
Navigates to the KYC level 1 screen.

```kotlin
fun NavGraphBuilder.kycLevel1Screen(
```
Registers the KYC level 1 destination in the navigation graph.

### `feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/navigation/KYCLevel2Navigation.kt`

```kotlin
fun NavController.navigateToKYCLevel2(navOptions: NavOptions? = null)
```
Navigates to the KYC level 2 screen.

```kotlin
fun NavGraphBuilder.kycLevel2Screen(
```
Registers the KYC level 2 destination in the navigation graph.

### `feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/navigation/KYCLevel3Navigation.kt`

```kotlin
fun NavController.navigateToKYCLevel3(navOptions: NavOptions? = null)
```
Navigates to the KYC level 3 screen.

```kotlin
fun NavGraphBuilder.kycLevel3Screen(
```
Registers the KYC level 3 destination in the navigation graph.

### `feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/navigation/KYCNavigation.kt`

```kotlin
fun NavController.navigateToKYC()
```
Navigates to the KYC description screen.

```kotlin
fun NavGraphBuilder.kycScreen(
```
Registers the KYC description destination with callbacks for each KYC level.

---

_10 type(s), 11 function(s)/property(ies); 20 carry KDoc at source; 0 authored example(s); 7 live call site(s)._
<!-- api-docs:end -->
