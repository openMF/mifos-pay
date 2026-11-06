# `feature/notification/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 17 tracked files — 10× `.xml`, 4× `.kt`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**4 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `NotificationScreen`

ViewModels: `NotificationViewModel`

**Consumes:** `core/store/repository`

### Docs in the tree

| Path | |
|---|---|
| `feature/notification/README.md` | Notification Feature |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/notification sha=39788abf34bc1bac3476962b5197573018b72fa6 -->
## API reference

_Generated from `feature/notification` at tree `39788abf34bc` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/notification/src/commonMain/kotlin/kpt/feature/notification/NotificationNavigation.kt`

```kotlin
fun NavGraphBuilder.notificationScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToNotification(navOptions: NavOptions? = null)
```
_No KDoc at source._

### `feature/notification/src/commonMain/kotlin/kpt/feature/notification/di/NotificationModule.kt`

```kotlin
val NotificationModule = module
```
_No KDoc at source._

---

_0 type(s), 3 function(s)/property(ies); 0 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->
