# `feature/cloudtodo/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 30 tracked files — 20× `.xml`, 9× `.kt`, 1× `.kts`

**9 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `CloudTodoScreen`

ViewModels: `CloudTodoViewModel`

**Consumes:** `core/store/cloudtodo`

**Store5 archetype(s) exercised:** `MUTABLE` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

<!-- tree-scaffold:end -->
## Significance

The **MUTABLE Store5 archetype**, wired end to end and the only feature that writes to a server.

Reads stream from Room; toggling `completed` writes through the Store5 `Updater` (`PUT /todos/{id}`);
a write that fails offline is recorded by the RoomBookkeeper and retried on reconnect. It exists to
prove that path works, which is why it is deliberately trivial in every other respect — one boolean on
one entity. A richer demo would hide the mechanism it is meant to demonstrate.

<!-- api-docs:begin module=feature/cloudtodo sha=7a6f1adbf1125fbd7e467d9de774958cd490fea8 -->
## API reference

_Generated from `feature/cloudtodo` at tree `7a6f1adbf112` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/di/CloudTodoModule.kt`

```kotlin
val CloudTodoModule = module
```
Koin module for the Cloud Todo feature — the MUTABLE store-archetype write-path demo.

### `feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/navigation/CloudTodoNavigation.kt`

```kotlin
data object CloudTodoRoute
```
Route for the cloud-todo screen — the MUTABLE archetype showcase.

```kotlin
fun NavController.navigateToCloudTodo() = navigate(CloudTodoRoute)
```
Navigates to the cloud-todo screen. Takes no nav options: it is reached from the showcase list, never as a start destination or a back-stack-clearing jump.

```kotlin
fun NavGraphBuilder.cloudTodoGraph(
```
Dev-only destination for the Store5 write-path demo.

### `feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/ui/CloudTodoScreen.kt`

```kotlin
fun CloudTodoScreen(
```
**Cloud Todo (write-path demo)** — the on-device surface for the Store5 write path.

<details><summary>Used in the template — <code>feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/navigation/CloudTodoNavigation.kt:44</code></summary>

```kotlin
) {
    composableWithPushTransitions<CloudTodoRoute> {
        CloudTodoScreen(
            onBackClick = { navController.popBackStackSafely() },
            onResolveConflict = onResolveConflict,
        )
    }
```

</details>

### `feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/ui/CloudTodoViewModel.kt`

```kotlin
class CloudTodoViewModel(
```
MUTABLE-archetype write-path ViewModel — the reference implementation for driving `kpt.core.base.store.mutation.MutationGateway` from a screen.

<details><summary>Used in the template — <code>feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/di/CloudTodoModule.kt:25</code></summary>

```kotlin
 */
val CloudTodoModule = module {
    viewModel { CloudTodoViewModel(repository = get()) }
}
```

</details>

```kotlin
sealed interface MutationOutcome
```
Renderable outcome of the last write.

<details><summary>Used in the template — <code>feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/ui/CloudTodoScreen.kt:181</code></summary>

```kotlin
@Composable
internal fun OutcomeCard(
    outcome: MutationOutcome,
    onDismiss: () -> Unit,
    onResolveConflict: () -> Unit,
) {
    val sp = MaterialTheme.spacing
```

</details>

```kotlin
sealed interface CloudTodoAction
```
One-shot actions accepted by `CloudTodoViewModel`.

### `feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/ui/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the cloud-todo feature. Consumed by Compose UI tests in `feature/cloudtodo/src/commonTest/`. APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).

<details><summary>Used in the template — <code>feature/add-to-watchlist/src/commonMain/kotlin/kpt/feature/addtowatchlist/ui/AddToWatchlistStar.kt:51</code></summary>

```kotlin
    IconButton(
        onClick = viewModel::onToggle,
        modifier = modifier.testTag(TestTags.AddToWatchlist.STAR_PREFIX + coinId),
    ) {
        Icon(
            imageVector = if (isTracked) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = stringResource(
```

</details>

- `const val SCREEN: String = "cloudtodo_detail_screen"` — The screen root. A Maestro flow waits on this before asserting anything else on the screen.
- `const val SUMMARY: String = "cloudtodo_summary"` — The todo summary block.
- `const val TOGGLE_OPTIMISTIC: String = "cloudtodo_toggle_optimistic"` — The optimistic-toggle control — the one that writes locally first and syncs after, so it succeeds offline.
- `const val COMPLETE_ONLINE: String = "cloudtodo_complete_online"` — The network-required complete control.
- `const val OUTCOME: String = "cloudtodo_outcome"` — The outcome banner, present only after a mutation resolves. Its absence is what a test asserts to prove a write is still in flight.
- `const val OUTCOME_DISMISS: String = "cloudtodo_outcome_dismiss"` — The banner's dismiss control.
- `const val OUTCOME_RESOLVE: String = "cloudtodo_outcome_resolve"` — Its resolve-conflict control.

---

_5 type(s), 11 function(s)/property(ies); 16 carry KDoc at source; 0 authored example(s); 4 live call site(s)._
<!-- api-docs:end -->
