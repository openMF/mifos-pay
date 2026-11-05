# `core/domain`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DOMAIN.md`
> **Measured:** 2 Kotlin files, 0 test files

## Principal types

`LoginUseCase`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/domain sha=4685efd032e4de10057d9cb819076cbbf6cefb42 -->
## API reference

_Generated from `core/domain` at tree `4685efd032e4` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/domain/src/commonMain/kotlin/org/mifospay/core/domain/LoginUseCase.kt`

```kotlin
class LoginUseCase(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/login/LoginViewModel.kt:32</code></summary>

```kotlin

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val instanceConfigManager: InstanceConfigManager,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoginState, LoginEvent, LoginAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE)
```

</details>

### `core/domain/src/commonMain/kotlin/org/mifospay/core/domain/di/DomainModule.kt`

```kotlin
val DomainModule = module
```
_No KDoc at source._

---

_1 type(s), 1 function(s)/property(ies); 0 carry KDoc at source; 0 authored example(s); 1 live call site(s)._
<!-- api-docs:end -->
