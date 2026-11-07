# `feature/standing-instruction/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 18 tracked files — 13× `.kt`, 2× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**13 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `AddEditSIScreen`, `SIDetailsScreen`, `StandingInstructionScreen`

ViewModels: `AddEditSIViewModel`, `SIDetailViewModel`, `StandingInstructionViewModel`

**Consumes:** `core/store/repository`

### Docs in the tree

| Path | |
|---|---|
| `feature/standing-instruction/README.md` | Standing Instruction Feature |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/standing-instruction sha=90835cf8c6c20a9dedd705d7cb47ec9d7c516510 -->
## API reference

_Generated from `feature/standing-instruction` at tree `90835cf8c6c2` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/StandingInstructionScreen.kt`

```kotlin
fun StandingInstructionsScreen(
```
Screen listing the client's standing instructions with actions to add, edit, view or delete one.

### `feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/StandingInstructionViewModel.kt`

```kotlin
class StandingInstructionViewModel(
```
ViewModel for the standing-instructions list; loads the client's instructions and handles delete and navigation actions.

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/StandingInstructionScreen.kt:79</code></summary>

```kotlin
    onShowSIDetails: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StandingInstructionViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
```

</details>

```kotlin
data class SIUiState(
```
UI state of the standing-instructions list.

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/StandingInstructionScreen.kt:117</code></summary>

```kotlin
@Composable
private fun SIDialogs(
    dialogState: SIUiState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is SIUiState.DialogState.DeleteSI -> ForkBasicDialog(
```

</details>

```kotlin
sealed interface SIEvent
```
One-shot events emitted by `StandingInstructionViewModel`.

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/StandingInstructionScreen.kt:89</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is SIEvent.OnAddEditSI -> onAddEditSI.invoke(event.type)
            is SIEvent.OnNavigateToSIDetails -> onShowSIDetails.invoke(event.siId)
            is SIEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(event.message))
```

</details>

```kotlin
sealed interface SIAction
```
Actions handled by `StandingInstructionViewModel`.

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/StandingInstructionScreen.kt:102</code></summary>

```kotlin
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(SIAction.DismissDialog) }
        },
    )

    StandingInstructionScreen(
```

</details>

### `feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/components/InstructionTypeChip.kt`

```kotlin
fun InstructionTypeChip(
```
Chip showing a standing instruction's type (fixed or dues), colored per type.

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/details/SIDetailsScreen.kt:221</code></summary>

```kotlin
                        text = "Instruction Type",
                    )
                    InstructionTypeChip(
                        type = item.instructionType,
                    )
                }
```

</details>

```kotlin
enum class StandingInstructionType
```
Standing instruction types supported by the wallet.

### `feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/components/PriorityChip.kt`

```kotlin
fun PriorityChip(
```
Chip showing a standing instruction's priority option, colored per priority.

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/details/SIDetailsScreen.kt:211</code></summary>

```kotlin
                        )

                        PriorityChip(
                            priority = item.priority,
                        )
                    }
                }
```

</details>

### `feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/createOrUpdate/AddEditSINavigation.kt`

```kotlin
data class SIAddEditArgs(
```
Navigation arguments of the add/edit standing instruction screen.

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/createOrUpdate/AddEditSIViewModel.kt:61</code></summary>

```kotlin
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        val client = requireNotNull(userRepository.client.value)
        val type = SIAddEditArgs(savedStateHandle).addEditType

        AddEditSIState(
            client = client,
            type = type,
```

</details>

```kotlin
fun NavGraphBuilder.addEditSIScreen(
```
Registers the add/edit standing instruction destination.

```kotlin
fun NavController.navigateToSIAddEdit(
```
Navigates to the add/edit standing instruction screen for `addEditType`.

### `feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/createOrUpdate/AddEditSIViewModel.kt`

```kotlin
sealed interface AddEditSIEvent
```
One-shot events emitted by the add/edit standing instruction view model.

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/createOrUpdate/AddEditSIScreen.kt:95</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is AddEditSIEvent.OnNavigateBack -> navigateBack.invoke()
            is AddEditSIEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(message = getString(event.message))
                }
```

</details>

```kotlin
sealed interface AddEditSIAction
```
Actions handled by the add/edit standing instruction view model.

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/createOrUpdate/AddEditSIScreen.kt:107</code></summary>

```kotlin
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(AddEditSIAction.DismissDialog) }
        },
    )

    AddEditSIScreen(
```

</details>

### `feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/createOrUpdate/SIAddEditType.kt`

```kotlin
sealed class SIAddEditType
```
Whether the add/edit screen creates a new standing instruction or edits an existing one.

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/StandingInstructionScreen.kt:76</code></summary>

```kotlin
@Composable
fun StandingInstructionsScreen(
    onAddEditSI: (SIAddEditType) -> Unit,
    onShowSIDetails: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StandingInstructionViewModel = koinViewModel(),
) {
```

</details>

### `feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/details/SIDetailsNavigation.kt`

```kotlin
fun NavGraphBuilder.siDetailsScreen(
```
Registers the standing instruction details destination.

```kotlin
fun NavController.navigateSIDetails(instructionId: Long, navOptions: NavOptions? = null)
```
Navigates to the details of the standing instruction `instructionId`.

### `feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/details/SIDetailsScreen.kt`

```kotlin
fun colorForMissing(value: String?): Color
```
Returns the error color when `value` is null, otherwise the normal text color.

### `feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/di/StandingInstructionModule.kt`

```kotlin
val StandingInstructionModule = module
```
Koin module providing the standing instruction view models.

---

_9 type(s), 9 function(s)/property(ies); 18 carry KDoc at source; 0 authored example(s); 10 live call site(s)._
<!-- api-docs:end -->
