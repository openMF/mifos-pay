# `feature/showcase/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 31 tracked files — 20× `.xml`, 9× `.kt`, 1× `.md`, 1× `.kts`

**9 Kotlin files** — 2 screen(s), 0 ViewModel(s).

Screens: `StateGalleryScreen`, `TransitionGalleryScreen`

### Docs in the tree

| Path | |
|---|---|
| `feature/showcase/README.md` | :feature:showcase |

<!-- tree-scaffold:end -->
## Significance

The developer-facing galleries: every `ScreenState` rendered side by side, and every motion transition
demonstrable on a device.

Their value is regression-catching. A state gallery makes a broken empty state visible in one screen
instead of requiring a specific data condition, and the transition gallery is how a motion change is
reviewed before it ships across every navigation edge. `--clean` removes them with the rest of the demo.

<!-- api-docs:begin module=feature/showcase sha=eae345ada814d088de037eab5fd9c2d197a9ee16 -->
## API reference

_Generated from `feature/showcase` at tree `eae345ada814` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the showcase feature. Consumed by Compose UI tests in `feature/showcase/src/commonTest/`. APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).

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

- `const val SCREEN: String = "state_gallery_screen"` — Root scaffold — always rendered.
- `const val SCREEN: String = "transition_gallery_screen"` — Root scaffold — always rendered.

### `feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryNavigation.kt`

```kotlin
data object StateGalleryRoute
```
Route for the screen-state gallery — every `ScreenState` rendered side by side.

```kotlin
fun NavGraphBuilder.stateGalleryGraph(navController: NavController)
```
Registers the state-gallery graph.

### `feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt`

```kotlin
fun StateGalleryScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier)
```
Dev-only screen exhibiting every `ScreenState` variant and each of the 5 component-scale primitives (`InlineErrorPill`, `ErrorChip`, `CardLoadingSkeleton`, `RowLoadingShimmer`, `CardStateBox`).

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryNavigation.kt:31</code></summary>

```kotlin
fun NavGraphBuilder.stateGalleryGraph(navController: NavController) {
    composableWithPushTransitions<StateGalleryRoute> {
        StateGalleryScreen(
            onBackClick = { navController.popBackStackSafely() },
        )
    }
}
```

</details>

### `feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/transitions/TransitionGalleryNavigation.kt`

```kotlin
data object TransitionGalleryRoute
```
Route for the transition gallery.

```kotlin
data class TransitionDemoRoute(val variantName: String)
```
Route for one transition's demo screen.

```kotlin
fun NavGraphBuilder.transitionGalleryGraph(navController: NavController)
```
Registers the transition-gallery graph.

### `feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/transitions/TransitionGalleryScreen.kt`

```kotlin
fun TransitionGalleryScreen(
```
Dev-only screen exhibiting every transition factory. Each row tile, when tapped, navigates to a destination using a specific transition, then the destination auto-pops after 2s.

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/transitions/TransitionGalleryNavigation.kt:49</code></summary>

```kotlin
fun NavGraphBuilder.transitionGalleryGraph(navController: NavController) {
    composableWithPushTransitions<TransitionGalleryRoute> {
        TransitionGalleryScreen(
            onNavigateToDemo = { variant ->
                navController.navigate(TransitionDemoRoute(variant.name))
            },
            onBackClick = { navController.popBackStackSafely() },
```

</details>

```kotlin
enum class TransitionVariant(val displayName: String)
```
The transition factories the gallery exercises.

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/transitions/TransitionGalleryNavigation.kt:59</code></summary>

```kotlin
    composableWithPushTransitions<TransitionDemoRoute> { backStackEntry ->
        val variantName = backStackEntry.toRoute<TransitionDemoRoute>().variantName
        val variant = TransitionVariant.valueOf(variantName)
        TransitionDemoScreen(variant = variant, onAutoPop = { navController.popBackStackSafely() })
    }
}
```

</details>

---

_5 type(s), 6 function(s)/property(ies); 11 carry KDoc at source; 0 authored example(s); 4 live call site(s)._
<!-- api-docs:end -->
