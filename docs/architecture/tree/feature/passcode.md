# `feature/passcode/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 71 tracked files — 57× `.xml`, 9× `.kt`, 4× `.jpg`, 1× `.kts`

**9 Kotlin files** — 2 screen(s), 1 ViewModel(s).

Screens: `AppPasscodeScreen`, `BiometricsSetupScreen`

ViewModels: `PasscodeViewModel`

**Consumes:** `core/store/repository`

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/passcode sha=76659cb4b6617402130d4f3931b3ba10c486047e -->
## API reference

_Generated from `feature/passcode` at tree `76659cb4b661` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/AppPasscodeScreen.kt`

```kotlin
fun AppPasscodeScreen(
```
Mifos Pay-themed wrapper around the library's `PasscodeScreen` that bridges the passcode and biometrics libraries (which never import each other).

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/PasscodeNavigation.kt:116</code></summary>

```kotlin
) {
    composableWithStayTransitions<RootPasscodeRoute> {
        AppPasscodeScreen(
            onAuthenticationSuccess = onAuthenticationSuccess,
            navigateToLogin = navigateToLogin,
            onPasscodeCreation = onPasscodeCreation,
            onAuthenticationFailed = onAuthenticationFailed,
```

</details>

### `feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/BiometricErrorMessages.kt`

```kotlin
class BiometricErrorMessages(
```
Pre-resolved localized strings for each `BiometricError` case, bundled so callers can map errors to strings outside a `@Composable` context — e.g.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/AppPasscodeScreen.kt:111</code></summary>

```kotlin
    // them across the viewModelScope.launch boundary via the action constructors
    // (stringResource() is not callable from inside the suspend launch block).
    val biometricErrorMessages = rememberBiometricErrorMessages()
    val biometricPromptStrings = rememberBiometricPromptStrings()

    val navEventState = rememberNavigationEventState(
        currentInfo = PasscodeCurrentInfo,
```

</details>

```kotlin
fun rememberBiometricErrorMessages(): BiometricErrorMessages = BiometricErrorMessages(
```
Composable factory for `BiometricErrorMessages` — reads all nine `feature_passcode_biometric_error_*` keys from compose resources at the current locale and bundles them into a single object the ViewModel can carry across `viewModelScope.launch` boundaries.

```kotlin
data class BiometricPromptStrings(
```
Pre-resolved OS-prompt strings passed to `PlatformAuthenticator.registerUser(...)` and `PlatformAuthenticationProvider.onAuthenticatorClick(...)` — the v2.3.0-beta library requires these as caller-supplied parameters.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/AppPasscodeScreen.kt:112</code></summary>

```kotlin
    // (stringResource() is not callable from inside the suspend launch block).
    val biometricErrorMessages = rememberBiometricErrorMessages()
    val biometricPromptStrings = rememberBiometricPromptStrings()

    val navEventState = rememberNavigationEventState(
        currentInfo = PasscodeCurrentInfo,
    )
```

</details>

```kotlin
fun rememberBiometricPromptStrings(): BiometricPromptStrings = BiometricPromptStrings(
```
Composable factory for `BiometricPromptStrings` — reads the four `feature_passcode_biometric_prompt_*` keys at the current locale.

### `feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/BiometricsKey.kt`

```kotlin
fun BiometricsKey(
```
Platform-aware biometric unlock button rendered in the `PasscodeScreen`'s `externalAuthButton` slot.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/AppPasscodeScreen.kt:243</code></summary>

```kotlin
        externalAuthButton = if (allowBiometricAuth) {
            { modifier ->
                BiometricsKey(
                    modifier = modifier,
                    systemAvailableAuthOption = systemAvailableAuthOption,
                    onClick = {
                        viewModel.trySendAction(
```

</details>

### `feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/BiometricsSetupScreen.kt`

```kotlin
fun BiometricSetupScreen(
```
First-time biometric enrolment screen, shown after `PasscodeResult.Created` lands on the root passcode flow. Calls `PlatformAuthenticationProvider.registerUser(...)` from the composition- scoped provider via `BiometricSetupScreenViewmodel`.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/BiometricsSetupScreenNavigation.kt:36</code></summary>

```kotlin
) {
    composable(route = BIOMETRIC_SETUP_ROUTE) {
        BiometricSetupScreen(
            onBiometricsRegistrationSuccess,
            onSkipBiometricSetup,
        )
    }
```

</details>

### `feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/BiometricsSetupScreenNavigation.kt`

```kotlin
fun NavController.navigateToBiometricSetupScreen(navOptions: NavOptions? = null) =
```
Pushes `BIOMETRIC_SETUP_ROUTE`. Called from `RootNavGraph` after `PasscodeResult.Created`.

```kotlin
fun NavGraphBuilder.biometricSetupScreen(
```
Registers the biometric-setup destination. Both `onBiometricsRegistrationSuccess` and `onSkipBiometricSetup` are terminal — wire both to "pop and route to main" in the caller.

### `feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/BiometricsSetupScreenViewmodel.kt`

```kotlin
class BiometricSetupScreenViewmodel(
```
ViewModel for `BiometricSetupScreen`. Drives the `PlatformAuthenticationProvider.registerUser` call against the platform authenticator.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/BiometricsSetupScreen.kt:77</code></summary>

```kotlin
    onBiometricsRegistrationSuccess: () -> Unit,
    onSkipBiometricSetup: () -> Unit,
    viewModel: BiometricSetupScreenViewmodel = koinViewModel(),
) {
    val navEventState = rememberNavigationEventState(
        currentInfo = BiometricSetupScreenCurrentInfo,
    )
```

</details>

```kotlin
data class BiometricSetupScreenState(
```
UI state for `BiometricSetupScreenViewmodel`.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/BiometricsSetupScreen.kt:126</code></summary>

```kotlin
@Composable
internal fun BiometricSetupContent(
    state: BiometricSetupScreenState,
    onSetupBiometrics: () -> Unit,
    onSkipBiometricSetup: () -> Unit,
    onDismissErrorDialog: () -> Unit,
) {
```

</details>

```kotlin
sealed interface BiometricSetupScreenAction
```
Actions dispatched to `BiometricSetupScreenViewmodel`.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/BiometricsSetupScreen.kt:107</code></summary>

```kotlin
        onSetupBiometrics = {
            viewModel.trySendAction(
                BiometricSetupScreenAction.ClickSetupBiometric(
                    platformAuthenticationProvider = platformAuthenticationProvider,
                    errorMessages = biometricErrorMessages,
                    promptStrings = biometricPromptStrings,
                ),
```

</details>

```kotlin
sealed interface BiometricSetupScreenEvent
```
One-shot navigation events emitted by `BiometricSetupScreenViewmodel`.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/BiometricsSetupScreen.kt:98</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            BiometricSetupScreenEvent.OnBiometricSetupSuccess -> onBiometricsRegistrationSuccess()
            BiometricSetupScreenEvent.OnSkipBiometricSetup -> onSkipBiometricSetup()
        }
    }
```

</details>

### `feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/PasscodeModule.kt`

```kotlin
val AuthenticatorModule = module
```
Koin module exposing the `:feature:passcode` ViewModels. Included by the top-level `cmp.navigation.registry.app.di.KoinModules` aggregator.

### `feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/PasscodeNavigation.kt`

```kotlin
data object RootPasscodeRoute
```
Type-safe route for the root unlock / first-time-setup passcode screen. Pushed onto the stack by `RootNavGraph` immediately after a successful login. Backed by `rootPasscodeScreen`.

```kotlin
data object ReAuthPasscodeRoute
```
Type-safe route for the background-resume re-authentication screen. Pushed by `ForkAppRoot` when the app was backgrounded for >15 s and is still unlocked. Backed by `reAuthPasscodeScreen`.

```kotlin
data class InternalPasscodeRoute(
```
Type-safe route for the in-app sensitive-operation passcode gate.

```kotlin
fun NavController.navigateToRootPasscodeScreen(navOptions: NavOptions? = null) =
```
Push the root passcode screen. See `RootPasscodeRoute`.

```kotlin
fun NavController.navigateToReAuthPasscodeScreen(navOptions: NavOptions? = null) =
```
Push the re-auth passcode screen. See `ReAuthPasscodeRoute`.

```kotlin
fun NavController.navigateToInternalPasscodeScreen(
```
Push the internal passcode gate. See `InternalPasscodeRoute` for the round-trip protocol and the meaning of each parameter.

```kotlin
fun NavGraphBuilder.rootPasscodeScreen(
```
Registers the root passcode destination.

```kotlin
fun NavGraphBuilder.reAuthPasscodeScreen(
```
Registers the background-resume re-auth destination. Pushed on top of the authenticated graph by the lifecycle observer in `ForkAppRoot`; pops on `PasscodeResult.Verified`. Back navigation disabled, biometric auth enabled.

```kotlin
fun NavGraphBuilder.internalPasscodeScreen(
```
Registers the in-app sensitive-operation passcode gate (change passcode, disable biometrics, intra-bank transfer auth). Back navigation is enabled so users can cancel; biometric auth is gated on `InternalPasscodeRoute.allowBiometricAuth`.

### `feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/PasscodeViewModel.kt`

```kotlin
class PasscodeViewModel(
```
ViewModel for `AppPasscodeScreen`. Owns the glue between: - `PasscodeManager` (passcode library) — provides step/state and emits `PasscodeResult`s on creation, verify, change, forget.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/AppPasscodeScreen.kt:98</code></summary>

```kotlin
    allowBackNavigation: Boolean = false,
    allowBiometricAuth: Boolean = true,
    viewModel: PasscodeViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val passcodeManager: PasscodeManager = koinInject<PasscodeManager>()
```

</details>

```kotlin
data class PasscodeState(
```
UI state for `PasscodeViewModel`.

```kotlin
sealed interface PasscodeDialogState
```
Dialog variants surfaced by `PasscodeViewModel`.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/AppPasscodeScreen.kt:176</code></summary>

```kotlin
        onConfirm = {
            when (state.dialogState) {
                is PasscodeDialogState.UserNotRegistered -> {
                    viewModel.trySendAction(PasscodeAction.ClickConfirmOnNotRegisteredDialog)
                }
                else -> viewModel.trySendAction(PasscodeAction.DismissDialog)
            }
```

</details>

```kotlin
sealed interface PasscodeAction
```
Actions dispatched to `PasscodeViewModel`.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/AppPasscodeScreen.kt:149</code></summary>

```kotlin
            when (event) {
                Lifecycle.Event.ON_START -> {
                    viewModel.trySendAction(PasscodeAction.OnStart)
                }

                Lifecycle.Event.ON_RESUME -> {
                    viewModel.trySendAction(
```

</details>

```kotlin
sealed interface PasscodeEvent
```
One-shot events emitted by `PasscodeViewModel` to the screen.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/AppPasscodeScreen.kt:132</code></summary>

```kotlin
        viewModel.eventFlow.collect { event ->
            when (event) {
                is PasscodeEvent.NavigateForResult -> {
                    when (event.result) {
                        PasscodeResult.Verified -> onAuthenticationSuccess()
                        PasscodeResult.Created -> onPasscodeCreation()
                        PasscodeResult.Changed -> onPasscodeChanged()
```

</details>

---

_14 type(s), 14 function(s)/property(ies); 28 carry KDoc at source; 0 authored example(s); 13 live call site(s)._
<!-- api-docs:end -->
