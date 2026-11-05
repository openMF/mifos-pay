# `feature/auth/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 30 tracked files — 15× `.kt`, 10× `.xml`, 2× `.pro`, 1× `.gitignore`, 1× `.md`

**15 Kotlin files** — 4 screen(s), 3 ViewModel(s).

Screens: `LoginScreen`, `MobileVerificationScreen`, `SignupMethodScreen`, `SignupScreen`

ViewModels: `LoginViewModel`, `MobileVerificationViewModel`, `SignupViewModel`

### Docs in the tree

| Path | |
|---|---|
| `feature/auth/README.md` | :feature:auth module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/auth sha=2e58e6909ccbc628922dd396de3796d5d1db3cfd -->
## API reference

_Generated from `feature/auth` at tree `2e58e6909ccb` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/di/AuthModule.kt`

```kotlin
val AuthModule = module
```
_No KDoc at source._

### `feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/login/LoginViewModel.kt`

```kotlin
class LoginViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/di/AuthModule.kt:19</code></summary>

```kotlin

val AuthModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignupViewModel)
    viewModelOf(::MobileVerificationViewModel)
}
```

</details>

```kotlin
data class LoginState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/login/LoginScreen.kt:105</code></summary>

```kotlin
    )

    if (state.dialogState is LoginState.DialogState.Loading) {
        MifosProgressIndicatorOverlay()
    }
}
```

</details>

```kotlin
sealed class LoginEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/login/LoginScreen.kt:77</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is LoginEvent.NavigateBack -> onNavigateBack.invoke()
            is LoginEvent.NavigateToSignup -> navigateToSignupScreen.invoke()
            is LoginEvent.NavigateToMifosPasscodeScreen -> navigateToMifosPasscodeScreen()
            is LoginEvent.ShowToast -> {
                scope.launch {
```

</details>

```kotlin
sealed class LoginAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/login/LoginScreen.kt:91</code></summary>

```kotlin
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(LoginAction.ErrorDialogDismiss) }
        },
    )

    LoginScreen(
```

</details>

### `feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/mobileVerify/MobileVerificationScreen.kt`

```kotlin
fun PhoneNoVerifyContent(
```
_No KDoc at source._

```kotlin
fun OtpVerifyContent(
```
_No KDoc at source._

### `feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/mobileVerify/MobileVerificationViewModel.kt`

```kotlin
class MobileVerificationViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/di/AuthModule.kt:21</code></summary>

```kotlin
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignupViewModel)
    viewModelOf(::MobileVerificationViewModel)
}
```

</details>

```kotlin
sealed class MobileVerificationState
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/mobileVerify/MobileVerificationScreen.kt:95</code></summary>

```kotlin
@Composable
private fun MobileVerificationScreen(
    uiState: MobileVerificationState,
    onEvent: (MobileVerificationAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
```

</details>

```kotlin
sealed interface MobileVerificationEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/mobileVerify/MobileVerificationScreen.kt:68</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is MobileVerificationEvent.NavigateBack -> onNavigateBack.invoke()

            is MobileVerificationEvent.NavigateToSignup -> {
                onOtpVerificationSuccess(event.phoneNo)
            }
```

</details>

```kotlin
sealed interface MobileVerificationAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/mobileVerify/MobileVerificationScreen.kt:96</code></summary>

```kotlin
private fun MobileVerificationScreen(
    uiState: MobileVerificationState,
    onEvent: (MobileVerificationAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
```

</details>

### `feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/navigation/LoginScreenNavigation.kt`

```kotlin
fun NavGraphBuilder.loginScreen(
```
Registers the login destination.

```kotlin
fun NavController.navigateToLogin(username: String = "")
```
Pushes `LOGIN_ROUTE`. Pre-fills the username field with `username` if non-empty (used by sign-up → login hand-off).

### `feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/navigation/MobileVerificationScreenNavigation.kt`

```kotlin
fun NavGraphBuilder.mobileVerificationScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToMobileVerification()
```
_No KDoc at source._

### `feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/navigation/SignupScreenNavigation.kt`

```kotlin
fun NavGraphBuilder.signupScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToSignup(
```
_No KDoc at source._

### `feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/signup/SignupViewModel.kt`

```kotlin
class SignupViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/di/AuthModule.kt:20</code></summary>

```kotlin
val AuthModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignupViewModel)
    viewModelOf(::MobileVerificationViewModel)
}
```

</details>

```kotlin
data class SignUpState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/signup/SignupScreen.kt:122</code></summary>

```kotlin
@Composable
private fun SignupScreen(
    state: SignUpState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onAction: (SignUpAction) -> Unit,
) {
```

</details>

```kotlin
sealed interface SignUpEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/signup/SignupScreen.kt:92</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is SignUpEvent.NavigateBack -> onNavigateBack.invoke()
            is SignUpEvent.NavigateToLogin -> onNavigateToLogin.invoke(event.username)
            is SignUpEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message)
```

</details>

```kotlin
sealed interface SignUpAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/signup/SignupScreen.kt:105</code></summary>

```kotlin
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(SignUpAction.ErrorDialogDismiss) }
        },
    )

    SignupScreen(
```

</details>

### `feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/socialSignup/SignupMethodNavigation.kt`

```kotlin
fun NavGraphBuilder.signupMethodScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToSignupMethod()
```
_No KDoc at source._

---

_12 type(s), 11 function(s)/property(ies); 2 carry KDoc at source; 0 authored example(s); 12 live call site(s)._
<!-- api-docs:end -->
