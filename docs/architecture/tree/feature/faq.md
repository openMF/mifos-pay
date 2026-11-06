# `feature/faq/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 18 tracked files — 10× `.xml`, 5× `.kt`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**5 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `FaqScreen`

ViewModels: `FAQViewModel`

### Docs in the tree

| Path | |
|---|---|
| `feature/faq/README.md` | FAQ Module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/faq sha=b5d1d13c90beb4526085a3cc6c6d645e0e08cb3f -->
## API reference

_Generated from `feature/faq` at tree `b5d1d13c90be` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/faq/src/commonMain/kotlin/kpt/feature/faq/di/FaqModule.kt`

```kotlin
val FaqModule = module
```
_No KDoc at source._

### `feature/faq/src/commonMain/kotlin/kpt/feature/faq/navigation/FAQNavigation.kt`

```kotlin
fun NavController.navigateToFAQ(navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.faqScreen(
```
_No KDoc at source._

---

_0 type(s), 3 function(s)/property(ies); 0 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->
