# `feature/editpassword/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 10 tracked files — 4× `.kt`, 3× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**4 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `EditPasswordScreen`

ViewModels: `EditPasswordViewModel`

**Consumes:** `core/store/repository`

### Docs in the tree

| Path | |
|---|---|
| `feature/editpassword/README.md` | Edit Password Feature |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/editpassword sha=acf17aa6223407bce5f2232ade012cda2e783cf3 -->
## API reference

_Generated from `feature/editpassword` at tree `acf17aa62234` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/editpassword/src/commonMain/kotlin/kpt/feature/editpassword/di/EditPasswordModule.kt`

```kotlin
val EditPasswordModule = module
```
Koin module providing the edit-password ViewModel.

### `feature/editpassword/src/commonMain/kotlin/kpt/feature/editpassword/navigation/EditPasswordScreenNavigation.kt`

```kotlin
fun NavGraphBuilder.editPasswordScreen(
```
Registers the edit-password screen; `navigateBack` pops it, `onLogOut` runs after a password change.

```kotlin
fun NavController.navigateToEditPassword()
```
Navigates to the edit-password screen.

---

_0 type(s), 3 function(s)/property(ies); 3 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->
