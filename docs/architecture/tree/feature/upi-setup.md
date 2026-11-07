# `feature/upi-setup/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 16 tracked files — 10× `.kt`, 3× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**10 Kotlin files** — 4 screen(s), 0 ViewModel(s).

Screens: `DebitCardScreen`, `OtpScreen`, `SetUpUPiPinScreen`, `UpiPinScreen`

**Consumes:** `core/store/repository`

### Docs in the tree

| Path | |
|---|---|
| `feature/upi-setup/README.md` | :feature:upi-setup module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/upi-setup sha=7652d08dd7170714f7883bd19d3600dee0a6b8d0 -->
## API reference

_Generated from `feature/upi-setup` at tree `7652d08dd717` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/di/UpiSetupModule.kt`

```kotlin
val UpiSetupModule = module
```
Koin module providing the ViewModels of the UPI setup feature.

### `feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/navigation/SetupUpiPinNavigation.kt`

```kotlin
fun NavGraphBuilder.setupUpiPinScreen(
```
Was a nav-registered-but-blank destination — the composable body called no screen (`SetupUpiPinScreenRoute(...)` was commented out) and `navigateToSetupUpiPin` had zero callers anywhere in the app.

```kotlin
fun NavController.navigateToSetupUpiPin(
```
Navigates to the UPI PIN screen for `type` (setup, change or forgot) of the given bank account.

### `feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/screens/SetUpUPiPinScreen.kt`

```kotlin
fun PreviewSetupUpiPin()
```
Preview of the set-up UPI PIN screen.

```kotlin
fun PreviewChangeUpi()
```
Preview of the change UPI PIN screen.

```kotlin
fun PreviewForgetUpi()
```
Preview of the forgot UPI PIN screen.

```kotlin
fun getBankAccountDetails(): BankAccountDetails
```
Returns sample bank account details used by the previews.

### `feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/screens/UpiPinScreen.kt`

```kotlin
fun showSnackbar(snackbarHostState: SnackbarHostState, message: String)
```
Shows `message` in the snackbar host on the main dispatcher.

### `feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/viewmodel/DebitCardViewModal.kt`

```kotlin
class DebitCardViewModel :
```
ViewModel that checks the format of a debit card number before UPI setup continues.

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/di/UpiSetupModule.kt:20</code></summary>

```kotlin
 */
val UpiSetupModule = module {
    viewModelOf(::DebitCardViewModel)
    viewModelOf(::SetUpUpiViewModal)
}
```

</details>

```kotlin
sealed interface DebitCardEvent
```
One-off events of the debit card ViewModel (none defined).

```kotlin
sealed interface DebitCardAction
```
Actions handled by the debit card ViewModel.

```kotlin
sealed class DebitCardUiState
```
UI state of the debit card verification.

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/screens/DebitCardScreen.kt:56</code></summary>

```kotlin
@VisibleForTesting
internal fun DebitCardScreenWithHeaderAndContent(
    debitCardUiState: DebitCardUiState,
    onDebitCardVerified: () -> Unit,
    onDebitCardVerificationFailed: (String) -> Unit,
    onDone: (String, String, String) -> Unit,
    modifier: Modifier = Modifier,
```

</details>

### `feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/viewmodel/SetUpUpiViewModal.kt`

```kotlin
class SetUpUpiViewModal(
```
Real OTP request/verify — reads through `TwoFactorAuthRepository` (a real Fineract self-service two-factor-auth endpoint, previously bound in DI but never actually consumed anywhere in this app).

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/di/UpiSetupModule.kt:21</code></summary>

```kotlin
val UpiSetupModule = module {
    viewModelOf(::DebitCardViewModel)
    viewModelOf(::SetUpUpiViewModal)
}
```

</details>

```kotlin
data class SetUpUpiState(
```
UI state of the UPI PIN setup flow.

```kotlin
sealed interface OtpRequestState
```
Progress of requesting an OTP.

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/screens/OtpScreen.kt:42</code></summary>

```kotlin
@Composable
internal fun OtpScreen(
    otpRequestState: OtpRequestState,
    otpVerifyState: OtpVerifyState,
    onOtpEntered: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentVisibility: Boolean = false,
```

</details>

```kotlin
sealed interface OtpVerifyState
```
Progress of verifying an OTP.

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/screens/OtpScreen.kt:43</code></summary>

```kotlin
internal fun OtpScreen(
    otpRequestState: OtpRequestState,
    otpVerifyState: OtpVerifyState,
    onOtpEntered: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentVisibility: Boolean = false,
    verificationStatus: Boolean = false,
```

</details>

```kotlin
sealed interface SetUpUpiEvent
```
One-off events of the UPI setup ViewModel.

```kotlin
sealed interface SetUpUpiAction
```
Actions handled by the UPI setup ViewModel.

---

_10 type(s), 8 function(s)/property(ies); 18 carry KDoc at source; 0 authored example(s); 5 live call site(s)._
<!-- api-docs:end -->
