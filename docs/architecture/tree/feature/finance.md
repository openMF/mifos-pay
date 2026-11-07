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

<!-- api-docs:begin module=feature/finance sha=bf4890395da49bed84081d46e488a8d711dbbcf1 -->
## API reference

_Generated from `feature/finance` at tree `bf4890395da4` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/finance/src/commonMain/kotlin/kpt/feature/finance/FinanceScreen.kt`

```kotlin
enum class FinanceScreenContents
```
Tabs shown on the finance screen.

### `feature/finance/src/commonMain/kotlin/kpt/feature/finance/navigation/FinanceNavigation.kt`

```kotlin
fun NavController.navigateToFinance(navOptions: NavOptions) = navigate(FINANCE_ROUTE, navOptions)
```
Navigates to the finance screen using `navOptions`.

```kotlin
fun NavGraphBuilder.financeScreen(
```
Registers the finance screen in the nav graph, showing the given `tabContents`.

---

_1 type(s), 2 function(s)/property(ies); 3 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->
