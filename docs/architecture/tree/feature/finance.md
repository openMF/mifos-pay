# `feature/finance/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 6 tracked files — 2× `.kt`, 1× `.gitignore`, 1× `.md`, 1× `.kts`, 1× `.xml`

**2 Kotlin files** — 1 screen(s), 0 ViewModel(s).

Screens: `FinanceScreen`

### Docs in the tree

| Path | |
|---|---|
| `feature/finance/README.md` | Finance Module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/finance sha=70200ab915a94b5ce2a637cae14933cd76a016a8 -->
## API reference

_Generated from `feature/finance` at tree `70200ab915a9` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/finance/src/commonMain/kotlin/kpt/feature/finance/FinanceScreen.kt`

```kotlin
enum class FinanceScreenContents
```
_No KDoc at source._

### `feature/finance/src/commonMain/kotlin/kpt/feature/finance/navigation/FinanceNavigation.kt`

```kotlin
fun NavController.navigateToFinance(navOptions: NavOptions) = navigate(FINANCE_ROUTE, navOptions)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.financeScreen(
```
_No KDoc at source._

---

_1 type(s), 2 function(s)/property(ies); 0 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->
