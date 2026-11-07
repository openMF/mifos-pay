# `feature/auth/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 30 tracked files — 15× `.kt`, 10× `.xml`, 2× `.pro`, 1× `.gitignore`, 1× `.md`

**15 Kotlin files** — 4 screen(s), 3 ViewModel(s).

Screens: `LoginScreen`, `MobileVerificationScreen`, `SignupMethodScreen`, `SignupScreen`

ViewModels: `LoginViewModel`, `MobileVerificationViewModel`, `SignupViewModel`

**Consumes:** `core/store/repository`, `core/store/util`

### Docs in the tree

| Path | |
|---|---|
| `feature/auth/README.md` | :feature:auth module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/auth sha=40c0cbb9ac560d4634623dd09a23c26bcc3e8dde -->
## API reference

_Generated from `feature/auth` at tree `40c0cbb9ac56` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/auth/src/commonMain/kotlin/kpt/feature/auth/di/AuthModule.kt`

```kotlin
val AuthModule = module
```
Koin module that registers the login, signup and mobile-verification view models.

### `feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginViewModel.kt`

```kotlin
class LoginViewModel(
```
View model for the login screen; authenticates with username and password and drives the status dialog.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/di/AuthModule.kt:19</code></summary>

```kotlin
/** Koin module that registers the login, signup and mobile-verification view models. */
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
UI state of the login screen.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginScreen.kt:104</code></summary>

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
One-off events emitted by the login view model.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginScreen.kt:76</code></summary>

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
User actions handled by the login view model.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginScreen.kt:90</code></summary>

```kotlin
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(LoginAction.ErrorDialogDismiss) }
        },
    )

    LoginScreen(
```

</details>

### `feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationScreen.kt`

```kotlin
fun PhoneNoVerifyContent(
```
Content for the phone-number entry step of mobile verification.

```kotlin
fun OtpVerifyContent(
```
Content for the OTP entry step of mobile verification.

### `feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationViewModel.kt`

```kotlin
class MobileVerificationViewModel(
```
View model that verifies a mobile number: requests an OTP for the number, then verifies the entered OTP.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/di/AuthModule.kt:21</code></summary>

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
UI state of the mobile verification flow, one subtype per step.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationScreen.kt:94</code></summary>

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
One-off events emitted by the mobile verification view model.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationScreen.kt:67</code></summary>

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
User actions handled by the mobile verification view model.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationScreen.kt:95</code></summary>

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

### `feature/auth/src/commonMain/kotlin/kpt/feature/auth/navigation/LoginScreenNavigation.kt`

```kotlin
fun NavGraphBuilder.loginScreen(
```
Registers the login destination.

```kotlin
fun NavController.navigateToLogin(username: String = "")
```
Pushes `LOGIN_ROUTE`. Pre-fills the username field with `username` if non-empty (used by sign-up → login hand-off).

### `feature/auth/src/commonMain/kotlin/kpt/feature/auth/navigation/MobileVerificationScreenNavigation.kt`

```kotlin
fun NavGraphBuilder.mobileVerificationScreen(
```
Registers the mobile verification destination in the nav graph.

```kotlin
fun NavController.navigateToMobileVerification()
```
Navigates to the mobile verification screen.

### `feature/auth/src/commonMain/kotlin/kpt/feature/auth/navigation/SignupScreenNavigation.kt`

```kotlin
fun NavGraphBuilder.signupScreen(
```
Registers the signup destination, with optional savings product, mobile number and business name arguments.

```kotlin
fun NavController.navigateToSignup(
```
Navigates to the signup screen, optionally prefilling the savings product, mobile number and business name.

### `feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt`

```kotlin
class SignupViewModel(
```
View model for the signup screen; validates the form and registers a new user and client.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/di/AuthModule.kt:20</code></summary>

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
UI state of the signup form.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupScreen.kt:121</code></summary>

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
One-off events emitted by the signup view model.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupScreen.kt:91</code></summary>

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
User actions handled by the signup view model.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupScreen.kt:104</code></summary>

```kotlin
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(SignUpAction.ErrorDialogDismiss) }
        },
    )

    SignupScreen(
```

</details>

### `feature/auth/src/commonMain/kotlin/kpt/feature/auth/socialSignup/SignupMethodNavigation.kt`

```kotlin
fun NavGraphBuilder.signupMethodScreen(
```
Registers the signup method destination in the nav graph.

```kotlin
fun NavController.navigateToSignupMethod()
```
Navigates to the signup method screen.

---

_12 type(s), 11 function(s)/property(ies); 23 carry KDoc at source; 0 authored example(s); 12 live call site(s)._
<!-- api-docs:end -->
