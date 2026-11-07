# `feature/beneficiary/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 12 tracked files — 9× `.kt`, 2× `.xml`, 1× `.kts`

**9 Kotlin files** — 2 screen(s), 3 ViewModel(s).

Screens: `AddEditBeneficiaryScreen`, `BeneficiaryListScreen`

ViewModels: `AddEditBeneficiaryViewModel`, `BeneficiaryListViewModel`, `DeleteBeneficiaryViewModel`

**Consumes:** `core/store/repository`

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/beneficiary sha=ae79224418b75ef40f864af2872eb839a4e9130e -->
## API reference

_Generated from `feature/beneficiary` at tree `ae79224418b7` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/addupdatebeneficiary/BeneficiaryAddEditType.kt`

```kotlin
sealed class BeneficiaryAddEditType
```
Whether the beneficiary form adds a new beneficiary or edits an existing one.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:182</code></summary>

```kotlin

            is AccountAction.AddTPTBeneficiary -> {
                sendEvent(AccountEvent.OnAddOrEditTPTBeneficiary(BeneficiaryAddEditType.AddItem()))
            }

            is AccountAction.EditBeneficiary -> {
                launchIO {
```

</details>

### `feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/addupdatebeneficiary/BeneficiaryNavigation.kt`

```kotlin
data class BeneficiaryAddEditArgs(
```
Navigation arguments of the add/edit beneficiary screen.

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/addupdatebeneficiary/AddEditBeneficiaryViewModel.kt:65</code></summary>

```kotlin
) : BaseViewModel<AEBState, AEBEvent, AEBAction>(
    initialState = savedStateHandle.getSerialized(ADD_EDIT_BENEFICIARY_KEY) ?: run {
        when (val addEditType = BeneficiaryAddEditArgs(savedStateHandle).addEditType) {
            is BeneficiaryAddEditType.AddItem -> {
                // Parse sourceQrData if available (for post-add navigation)
                val sourceQrData = addEditType.sourceQrData?.let { qrDataJson ->
                    try {
```

</details>

```kotlin
fun NavGraphBuilder.addEditBeneficiaryScreen(
```
Registers the add/edit beneficiary destination.

```kotlin
fun NavController.navigateToBeneficiaryAddEdit(
```
Navigates to the add/edit beneficiary screen for `addEditType`.

### `feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/deletebeneficiary/DeleteBeneficiaryBottomSheet.kt`

```kotlin
fun DeleteBeneficiaryBottomSheet(
```
Bottom sheet composable for delete beneficiary flow.

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/list/BeneficiaryListScreen.kt:106</code></summary>

```kotlin

    // Delete beneficiary bottom sheet - managed by separate ViewModel
    DeleteBeneficiaryBottomSheet(
        dialogState = deleteState.dialogState,
        onConfirmDelete = remember(deleteViewModel) {
            { beneficiaryId -> deleteViewModel.confirmDelete(beneficiaryId) }
        },
```

</details>

### `feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/deletebeneficiary/DeleteBeneficiaryViewModel.kt`

```kotlin
class DeleteBeneficiaryViewModel(
```
ViewModel for handling beneficiary deletion. This is a standalone ViewModel that manages the delete flow independently from the beneficiary list, preventing unwanted list refreshes during the delete confirmation flow.

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/di/BeneficiaryModule.kt:21</code></summary>

```kotlin
    viewModelOf(::AddEditBeneficiaryViewModel)
    viewModelOf(::BeneficiaryListViewModel)
    viewModelOf(::DeleteBeneficiaryViewModel)
}
```

</details>

```kotlin
data class DeleteBeneficiaryState(
```
UI state of the delete-beneficiary flow.

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/deletebeneficiary/DeleteBeneficiaryBottomSheet.kt:55</code></summary>

```kotlin
@Composable
fun DeleteBeneficiaryBottomSheet(
    dialogState: DeleteBeneficiaryState.DialogState?,
    onConfirmDelete: (beneficiaryId: Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
```

</details>

```kotlin
sealed interface DeleteBeneficiaryEvent
```
One-off events of the delete-beneficiary flow (none currently).

```kotlin
sealed interface DeleteBeneficiaryAction
```
Actions handled by the delete-beneficiary view model.

### `feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/di/BeneficiaryModule.kt`

```kotlin
val BeneficiaryModule = module
```
Koin module providing the beneficiary add/edit, list and delete ViewModels.

### `feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/list/BeneficiaryListScreen.kt`

```kotlin
fun BeneficiaryListScreen(
```
Screen listing the client's saved third-party transfer beneficiaries, with add, edit and delete actions.

```kotlin
fun BeneficiariesList(
```
Scrollable list of `beneficiaries`; row actions go to `onAction`, deletes to `onDeleteBeneficiary`.

```kotlin
fun BeneficiaryItem(
```
Single beneficiary row with edit and delete buttons.

```kotlin
fun PreviewBeneficiaryListScreen()
```
Compose preview of the beneficiary list screen with sample data.

### `feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/list/BeneficiaryListViewModel.kt`

```kotlin
class BeneficiaryListViewModel(
```
ViewModel that loads the client's beneficiaries and handles add, edit and refresh actions.

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/di/BeneficiaryModule.kt:20</code></summary>

```kotlin
val BeneficiaryModule = module {
    viewModelOf(::AddEditBeneficiaryViewModel)
    viewModelOf(::BeneficiaryListViewModel)
    viewModelOf(::DeleteBeneficiaryViewModel)
}
```

</details>

```kotlin
data class BeneficiaryListState(
```
UI state of the beneficiary list.

```kotlin
sealed interface BeneficiaryListEvent
```
One-off events emitted by `BeneficiaryListViewModel`.

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/list/BeneficiaryListScreen.kt:95</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is BeneficiaryListEvent.OnAddOrEditTPTBeneficiary -> onAddOrEditBeneficiary.invoke(event.type)

            is BeneficiaryListEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(event.message))
```

</details>

```kotlin
sealed interface BeneficiaryListAction
```
User actions handled by `BeneficiaryListViewModel`.

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/list/BeneficiaryListScreen.kt:142</code></summary>

```kotlin
    state: ScreenState<List<Beneficiary>>,
    onRetry: () -> Unit,
    onAction: (BeneficiaryListAction) -> Unit,
    onDeleteBeneficiary: (Long, String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
```

</details>

---

_10 type(s), 8 function(s)/property(ies); 18 carry KDoc at source; 0 authored example(s); 8 live call site(s)._
<!-- api-docs:end -->
