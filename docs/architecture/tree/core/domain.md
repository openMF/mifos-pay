# `core/domain`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DOMAIN.md`
> **Measured:** 2 Kotlin files, 0 test files

## Principal types

`LoginUseCase`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/domain sha=2469cbf0313b241d1784d3397e3484a858cda40b -->
## API reference

_Generated from `core/domain` at tree `2469cbf0313b` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/domain/src/commonMain/kotlin/kpt/core/domain/LoginUseCase.kt`

```kotlin
class LoginUseCase(
```
Logs a user in: authenticates, stores the session token and loads the client and user info into preferences.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginViewModel.kt:32</code></summary>

```kotlin
/** View model for the login screen; authenticates with username and password and drives the status dialog. */
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val instanceConfigManager: InstanceConfigManager,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoginState, LoginEvent, LoginAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE)
```

</details>

### `core/domain/src/commonMain/kotlin/kpt/core/domain/di/DomainModule.kt`

```kotlin
val DomainModule = module
```
Koin module providing the domain use cases.

---

_1 type(s), 1 function(s)/property(ies); 2 carry KDoc at source; 0 authored example(s); 1 live call site(s)._
<!-- api-docs:end -->
