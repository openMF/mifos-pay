# `feature/upi-setup/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 16 tracked files — 10× `.kt`, 3× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**10 Kotlin files** — 4 screen(s), 0 ViewModel(s).

Screens: `DebitCardScreen`, `OtpScreen`, `SetUpUPiPinScreen`, `UpiPinScreen`

### Docs in the tree

| Path | |
|---|---|
| `feature/upi-setup/README.md` | :feature:upi-setup module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/upi-setup sha=d44e0ee50f8badb2a9a7ab0abd97a44f9c73c793 -->
## API reference

_Generated from `feature/upi-setup` at tree `d44e0ee50f8b` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/upi-setup/src/commonMain/kotlin/org/mifospay/feature/upi/setup/di/UpiSetupModule.kt`

```kotlin
val UpiSetupModule = module
```
_No KDoc at source._

### `feature/upi-setup/src/commonMain/kotlin/org/mifospay/feature/upi/setup/navigation/SetupUpiPinNavigation.kt`

```kotlin
fun NavGraphBuilder.setupUpiPinScreen(
```
Was a nav-registered-but-blank destination — the composable body called no screen (`SetupUpiPinScreenRoute(...)` was commented out) and `navigateToSetupUpiPin` had zero callers anywhere in the app.

```kotlin
fun NavController.navigateToSetupUpiPin(
```
_No KDoc at source._

### `feature/upi-setup/src/commonMain/kotlin/org/mifospay/feature/upi/setup/screens/SetUpUPiPinScreen.kt`

```kotlin
fun PreviewSetupUpiPin()
```
_No KDoc at source._

```kotlin
fun PreviewChangeUpi()
```
_No KDoc at source._

```kotlin
fun PreviewForgetUpi()
```
_No KDoc at source._

```kotlin
fun getBankAccountDetails(): BankAccountDetails
```
_No KDoc at source._

### `feature/upi-setup/src/commonMain/kotlin/org/mifospay/feature/upi/setup/screens/UpiPinScreen.kt`

```kotlin
fun showSnackbar(snackbarHostState: SnackbarHostState, message: String)
```
_No KDoc at source._

### `feature/upi-setup/src/commonMain/kotlin/org/mifospay/feature/upi/setup/viewmodel/DebitCardViewModal.kt`

```kotlin
class DebitCardViewModel :
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/org/mifospay/feature/upi/setup/di/UpiSetupModule.kt:18</code></summary>

```kotlin

val UpiSetupModule = module {
    viewModelOf(::DebitCardViewModel)
    viewModelOf(::SetUpUpiViewModal)
}
```

</details>

```kotlin
sealed interface DebitCardEvent
```
_No KDoc at source._

```kotlin
sealed interface DebitCardAction
```
_No KDoc at source._

```kotlin
sealed class DebitCardUiState
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/org/mifospay/feature/upi/setup/screens/DebitCardScreen.kt:57</code></summary>

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

### `feature/upi-setup/src/commonMain/kotlin/org/mifospay/feature/upi/setup/viewmodel/SetUpUpiViewModal.kt`

```kotlin
class SetUpUpiViewModal(
```
Real OTP request/verify — reads through `TwoFactorAuthRepository` (a real Fineract self-service two-factor-auth endpoint, previously bound in DI but never actually consumed anywhere in this app).

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/org/mifospay/feature/upi/setup/di/UpiSetupModule.kt:19</code></summary>

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
_No KDoc at source._

```kotlin
sealed interface OtpRequestState
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/org/mifospay/feature/upi/setup/screens/OtpScreen.kt:43</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/org/mifospay/feature/upi/setup/screens/OtpScreen.kt:44</code></summary>

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
_No KDoc at source._

```kotlin
sealed interface SetUpUpiAction
```
_No KDoc at source._

---

_10 type(s), 8 function(s)/property(ies); 2 carry KDoc at source; 0 authored example(s); 5 live call site(s)._
<!-- api-docs:end -->
