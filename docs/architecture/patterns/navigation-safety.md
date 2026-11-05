# Navigation Safety Pattern

## The problem

`navController::popBackStack` is a bare method reference — no guard. After Phase 08
symmetrized exit-fade durations to **450ms** (correct for visual smoothness), a natural
double-tap (250–450ms between taps) fires a second pop while the screen is still
mid-exit-transition. The destination's lifecycle drops to `STARTED` during the transition;
that second pop fires regardless and clears the entire nav graph → dark gray void.

A secondary issue: the sequential fade-through animation (`exit fades → 200ms gap → enter
fades`) created a brief background blink on every screen-open.

## The fix — three layers

### 1. `NavController.popBackStackSafely()` (lifecycle guard)

```kotlin
// core-base/ui/.../nav/SafeNavController.kt
fun NavController.popBackStackSafely() {
    if (currentBackStackEntry?.lifecycle?.currentState
            ?.isAtLeast(Lifecycle.State.RESUMED) == true
    ) {
        popBackStack()
    }
}
```

**Rule:** Never pass `navController::popBackStack` as `onBackClick`. Always use:

```kotlin
onBackClick = { navController.popBackStackSafely() }
```

For `@Composable` screens that want an additional time-debounce on top:

```kotlin
val onBack = navController.rememberSafeBackPress(debounceMs = 500L)
KptTopAppBar(title = "…", onNavigationIconClick = onBack)
```

### 2. `KptFadeThrough.enter()` — crossfade instead of sequential fade

Removed the 200ms `delayMillis` from the enter animation. Old and new screen now fade
simultaneously (crossfade). Previously: old fades to 0 → 200ms gap → new fades in.
That 200ms gap showed the background surface as a dark blink.

### 3. `composableWithPushTransitions` — non-null stay transitions

Changed `exitTransition` and `popEnterTransition` from the nullable `TransitionProviders`
variants to `RootTransitionProviders.Kpt` (always non-null). Previously, cross-graph
navigation caused these to return `null`, falling through to the root NavHost's fade-through
exit — which, combined with the fade-through gap, compounded the blink.

## Why the bug was latent before Phase 08

Pre-Phase 08, `KptSharedAxis.exitForward` faded over **200ms**. For a double-tap to trigger
a second pop, both taps had to land within 200ms — at the edge of human tap-rate (nearly
impossible in normal use). Phase 08 symmetrized fades to 450ms (the right fix for visual
quality), which **2.25×ed the dangerous window** and made the bug routinely triggerable.

## Invariant (regression class prevention)

The `SafeNavControllerTest` asserts:
- Only `RESUMED` state allows a pop (all other states block)
- A second tap arriving while the exit animation runs (state = `STARTED`) is blocked
- A double-tap during the 450ms exit window results in at most 1 actual pop

## Related

- `core-base/ui/MOTION.md` — Phase 08 transition duration symmetry fix
- `SharedAxisSpecTest` — symmetry regression gate for enter/exit durations
