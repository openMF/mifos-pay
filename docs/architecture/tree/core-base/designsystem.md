# `core-base/designsystem`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_DESIGNSYSTEM.md`
> **Measured:** 41 Kotlin files, 6 test files

**Defines annotations:** `@ComponentDsl`, `@TopAppBarDsl`

**Consumes contracts:** `@ComponentDsl`, `@TopAppBarDsl`

## Principal types

`AccessibilityProvider`, `Animatable`, `BarGeometry`, `BreakpointConfiguration`, `Clickable`, `ComponentColors`, `ComponentComposer`, `ComponentConfiguration`, `ComponentConfigurationScope`, `ComponentDsl`, `ComponentElevation`, `ComponentFactory`, `ComponentRegistry`, `ComponentRenderer`  …and 54 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/designsystem sha=09b77963fa63857fa6b9fefef89c25c33bc39c24 -->
## API reference

_Generated from `core-base/designsystem` at tree `09b77963fa63` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptMaterialTheme.kt`

```kotlin
fun KptMaterialTheme(
```
KptMaterialTheme provides Material3 integration for KptTheme. This composable applies KptTheme values to MaterialTheme automatically, making all Material3 components use KptTheme design tokens.

<details><summary>Example</summary>

```kotlin
KptMaterialTheme {
    MaterialTheme.colorScheme.primary   // == KptTheme.colorScheme.primary
    MaterialTheme.typography.titleLarge // == KptTheme.typography.titleLarge
    KptTheme.spacing.md                 // Kpt-only tokens stay reachable
}
```

</details>

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/FastMpayScreen.kt:329</code></summary>

```kotlin
@org.jetbrains.compose.ui.tooling.preview.Preview
private fun FastMpayScreenPreview() {
    kpt.core.base.designsystem.KptMaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
```

</details>

```kotlin
fun KptMaterialTheme(
```
KptMaterialTheme with dark theme support. Provides automatic light/dark theme switching with Material3 integration.

<details><summary>Example</summary>

```kotlin
KptMaterialTheme(
    lightTheme = kptTheme { colors { primary = Color.Blue } },
    darkThemeProvider = kptTheme { colors { primary = Color.Cyan } },
) { /* switches with the system setting */ }
```

</details>

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/FastMpayScreen.kt:329</code></summary>

```kotlin
@org.jetbrains.compose.ui.tooling.preview.Preview
private fun FastMpayScreenPreview() {
    kpt.core.base.designsystem.KptMaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
```

</details>

```kotlin
fun KptMaterialTheme(
```
Builds the theme from the dark-mode flag, for a palette that differs by more than a few colours.

<details><summary>Example</summary>

```kotlin
KptMaterialTheme(themeBuilder = { isDark ->
    kptTheme { colors { primary = if (isDark) Color.Cyan else Color.Blue } }
}) { /* content */ }
```

</details>

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/FastMpayScreen.kt:329</code></summary>

```kotlin
@org.jetbrains.compose.ui.tooling.preview.Preview
private fun FastMpayScreenPreview() {
    kpt.core.base.designsystem.KptMaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptTheme.kt`

```kotlin
fun KptTheme(
```
KptTheme provides the core theming composable that makes all KPT design tokens available to child components through Composition Locals.

<details><summary>Example</summary>

```kotlin
KptTheme {
    // All child components can now access:
    // KptTheme.colorScheme
    // KptTheme.typography
    // KptTheme.shapes
    // KptTheme.spacing
    // KptTheme.elevation
    MyScreen()
}
```

</details>

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:197</code></summary>

```kotlin
                        subTitle = stringResource(Res.string.feature_accounts_unexpected_error_subtitle),
                        modifier = Modifier,
                        iconTint = KptTheme.colorScheme.error,
                    )
                }

                is AccountState.ViewState.Content -> {
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptThemeExtensions.kt`

```kotlin
fun KptSpacing.paddingValues(
```
Creates `PaddingValues` using KPT spacing tokens with horizontal and vertical values. This extension function provides a convenient way to create consistent padding using the design system's spacing scale.

<details><summary>Example</summary>

```kotlin
Box(
    modifier = Modifier.padding(
        KptTheme.spacing.paddingValues(
            horizontal = KptTheme.spacing.lg,
            vertical = KptTheme.spacing.md
        )
    )
)
```

</details>

```kotlin
fun KptSpacing.paddingValues(
```
Creates `PaddingValues` using KPT spacing tokens with individual edge values. This extension function provides fine-grained control over padding for each edge while maintaining consistency with the design system's spacing scale.

<details><summary>Example</summary>

```kotlin
Card(
    modifier = Modifier.padding(
        KptTheme.spacing.paddingValues(
            start = KptTheme.spacing.lg,
            top = KptTheme.spacing.md,
            end = KptTheme.spacing.lg,
            bottom = KptTheme.spacing.xl
        )
    )
)
```

</details>

```kotlin
fun KptTypography.toMaterial3Typography(fontFamily: FontFamily? = FontFamily.Default): Typography
```
Adapts a `KptTypography` to Material 3's `Typography`, so Kpt components and raw Material components render the same type.

```kotlin
fun Typography.toKptTypography(fontFamily: FontFamily? = FontFamily.Default): KptTypography =
```
Adapts a Material 3 `Typography` into a `KptTypography`, applying `fontFamily` to every style.

```kotlin
fun KptTypography.toMaterial3Typography(): Typography
```
Extension function to convert KptTypography to Material3 Typography This ensures that all Material3 components automatically use KptTheme typography

```kotlin
fun Typography.toKptTypography(): KptTypography = KptTypographyImpl(
```
Adapts a Material 3 `Typography` into a `KptTypography`, keeping each style's own font family.

```kotlin
fun KptColorScheme.toMaterial3ColorScheme(): ColorScheme
```
Extension function to convert KptColorScheme to Material3 ColorScheme This ensures that all Material3 components automatically use KptTheme colors

```kotlin
fun ColorScheme.toKptColorScheme(): KptColorScheme = KptColorSchemeImpl(
```
Adapts a Material 3 `ColorScheme` into a `KptColorScheme` — role for role, no colour invented.

```kotlin
fun KptShapes.toMaterial3Shapes(): Shapes
```
Extension function to convert KptShapes to Material3 Shapes This ensures that all Material3 components automatically use KptTheme shapes

```kotlin
fun Shapes.toKptShapes(): KptShapes = KptShapesImpl(
```
Adapts a Material 3 `Shapes` into a `KptShapes`.

```kotlin
fun KptElevation.cardElevation(
```
Get CardDefaults.cardElevation using KptTheme elevation

```kotlin
object KptSpacingDefaults
```
Predefined spacing combinations for common UI patterns. This object provides convenient access to commonly used padding configurations that follow design system best practices.

<details><summary>Example</summary>

```kotlin
// Apply standard screen padding
Column(
    modifier = Modifier.padding(KptSpacingDefaults.screenPadding())
) {
    // Screen content
}

// Apply card content padding
Card {
    Column(
        modifier = Modifier.padding(KptSpacingDefaults.cardPadding())
    ) {
        // Card content
    }
}
```

</details>

- `fun screenPadding() = KptTheme.spacing.paddingValues(` — Standard padding for screen-level content.
- `fun cardPadding() = KptTheme.spacing.paddingValues(` — Standard padding for card content.
- `fun buttonPadding() = KptTheme.spacing.paddingValues(` — Standard padding for button content.

```kotlin
object KptElevationDefaults
```
Predefined elevation configurations for common UI patterns. This object provides semantically meaningful elevation presets that follow Material Design elevation guidelines.

<details><summary>Example</summary>

```kotlin
// Standard card elevation
Card(elevation = KptElevationDefaults.card()) {
    // Card content
}

// Prominent card for important content
Card(elevation = KptElevationDefaults.raisedCard()) {
    // Important content
}
```

</details>

- `fun card() = KptTheme.elevation.cardElevation(` — Standard card elevation for normal content.
- `fun raisedCard() = KptTheme.elevation.cardElevation(` — Elevated card for prominent content.
- `fun dialogCard() = KptTheme.elevation.cardElevation(` — High elevation for modal content.

```kotlin
val KptColorScheme.containerColors: ContainerColors
```
Provides convenient access to container color combinations. This extension property groups related container colors and their corresponding content colors for easy access.

<details><summary>Example</summary>

```kotlin
val colors = KptTheme.colorScheme.containerColors

Card(
    colors = CardDefaults.cardColors(
        containerColor = colors.primary,
        contentColor = colors.onPrimary
    )
) {
    // Card content
}
```

</details>

```kotlin
data class ContainerColors(
```
A collection of container colors and their corresponding content colors.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/chart/BarGeometry.kt`

```kotlin
object BarGeometry
```
Pure-function math for bar chart composables. Normalizes each bar to a fraction in `[0f, 1f]` against the series' max. **Degenerate-input contracts**: - Empty list → empty result.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptBarChart.kt:80</code></summary>

```kotlin
) {
    val palette = ChartTokens.multiSeriesColors()
    val fractions = remember(data) { BarGeometry.normalizedHeights(data.map { it.value }) }
    val animatedFraction by animateFloatAsState(
        targetValue = if (data.isEmpty()) 0f else 1f,
        animationSpec = animationSpec,
        label = "kptBarFraction",
```

</details>

- `fun normalizedHeights(values: List<Float>): List<Float>` — Per-bar height as a fraction `[0, 1]` of the canvas height.
- `val max = values.maxOrNull() ?: 0f`

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/chart/DonutGeometry.kt`

```kotlin
object DonutGeometry
```
Pure-function math for donut chart composables. Extracted from the Composable so sweep-angle correctness can be tested without a Compose test rule. **Degenerate-input contracts**: - Empty list → empty result.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptDonutChart.kt:75</code></summary>

```kotlin
) {
    val palette = ChartTokens.multiSeriesColors()
    val sweeps = remember(slices) { DonutGeometry.sweepAngles(slices.map { it.value }) }
    // Animate the per-slice sweep total. Once at-rest, sweeps[i] is the per-slice arc;
    // during transition, we scale by the animatedFraction so all arcs grow together.
    val animatedFraction by animateFloatAsState(
        targetValue = if (sweeps.any { it > 0f }) 1f else 0f,
```

</details>

- `fun sweepAngles(values: List<Float>): List<Float>` — Per-slice sweep angle in degrees, in the same order as `values`.
- `val total = values.sum()`

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/chart/SparklineGeometry.kt`

```kotlin
object SparklineGeometry
```
Pure-function path geometry for sparkline / area chart composables. Extracted from the `Canvas { drawPath() }` block so the math is unit-testable without a Compose test rule.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptAreaChart.kt:49</code></summary>

```kotlin
        if (values.isEmpty()) return@Canvas

        val points = SparklineGeometry.normalize(
            values = values,
            width = size.width,
            height = size.height,
        )
```

</details>

- `fun normalize(values: List<Double>, width: Float, height: Float): List<Pair<Float, Float>>` — Normalize `values` to canvas coordinates fitting `[0, width] × [0, height]`.
- `val result: List<Pair<Float, Float>> = when`
- `val min = values.min()`
- `val max = values.max()`
- `val range = max - min`
- `val stepX = width / (values.size - 1)`
- `val x = index * stepX`
- `val y = if (range == 0.0)`
- `val normalized = ((value - min) / range).toFloat()`

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/AppCard.kt`

```kotlin
fun AppCard(
```
Material 3 elevated card for grouping related content (loan rows, form sections, dashboard tiles).

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsScreen.kt:165</code></summary>

```kotlin
) {
    val sp = MaterialTheme.spacing
    AppCard(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        accentColor = accentColor,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/BounceAnimation.kt`

```kotlin
fun BounceAnimation(
```
A composable that briefly enlarges the content to create a bounce effect when triggered.

```kotlin
fun RevealAnimation(
```
A composable that reveals or hides content with a combination of fade and scale animations.

```kotlin
fun <T> StaggeredAnimation(
```
Animates a list of items with a staggered vertical slide-in and fade-in effect.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/HeroCard.kt`

```kotlin
fun HeroCard(
```
Hero card — the dashboard's first impression.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/NotificationScreen.kt:56</code></summary>

```kotlin
            verticalArrangement = Arrangement.Center,
        ) {
            HeroCard {
                Column(
                    modifier = Modifier.padding(sp.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(sp.sm),
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/KptAnimationSpecs.kt`

```kotlin
object KptAnimationSpecs
```
Centralized animation specifications following Material Motion design guidelines. This object provides consistent animation timing and easing curves throughout the KPT design system.

<details><summary>Example</summary>

```kotlin
// For simple property animations
val animatedAlpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = KptAnimationSpecs.medium
)

// For spring-based animations
val animatedScale by animateFloatAsState(
    targetValue = if (pressed) 0.95f else 1f,
    animationSpec = KptAnimationSpecs.fastSpring
)
```

</details>

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/BounceAnimation.kt:82</code></summary>

```kotlin
    visible: Boolean,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = KptAnimationSpecs.medium,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
```

</details>

- `val fast = tween<Float>(durationMillis = 150, easing = FastOutSlowInEasing)` — Fast tween animation (150ms) for quick transitions like state changes. Best used for: button states, small UI element appearances/disappearances.
- `val medium = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)` — Medium tween animation (300ms) for standard UI transitions. Best used for: screen transitions, modal appearances, content changes.
- `val slow = tween<Float>(durationMillis = 500, easing = FastOutSlowInEasing)` — Slow tween animation (500ms) for complex or large-scale transitions. Best used for: page transitions, complex layout changes, dramatic effects.
- `val fastSpring = spring<Float>(` — Fast spring animation with medium bounce for responsive interactions. Best used for: button presses, interactive feedback, quick selections.
- `val mediumSpring = spring<Float>(` — Medium spring animation with low bounce for smooth transitions. Best used for: drawer openings, sheet expansions, smooth scrolling effects.
- `val slowSpring = spring<Float>(` — Slow spring animation with no bounce for stable, smooth animations. Best used for: large content movements, settling animations, smooth stops.
- `val emphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)` — Emphasized easing for important transitions that should draw attention. Creates a slow start with a quick finish.
- `val emphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)` — Emphasized accelerate easing for elements leaving the screen. Quick start that maintains momentum.
- `val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)` — Emphasized decelerate easing for elements entering the screen. Maintains momentum then slows to a smooth stop.
- `val standardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)` — Standard easing for general purpose animations. Provides a balanced, natural feeling motion.

```kotlin
fun AnimatedVisibilityScope.slideInFromStart(
```
Slide in animation from the start edge of the screen (left in LTR, right in RTL). This extension function provides a convenient way to create slide-in animations that respect the current layout direction.

```kotlin
fun AnimatedVisibilityScope.slideInFromEnd(
```
Slide in animation from the end edge of the screen (right in LTR, left in RTL). This extension function provides a convenient way to create slide-in animations that respect the current layout direction.

```kotlin
fun AnimatedVisibilityScope.slideInFromTop(
```
Slide in animation from the top edge of the screen. Creates a smooth vertical slide-in effect commonly used for notifications, drop-down menus, or top-anchored content.

```kotlin
fun AnimatedVisibilityScope.slideInFromBottom(
```
Slide in animation from the bottom edge of the screen. Creates a smooth vertical slide-in effect commonly used for bottom sheets, action panels, or bottom-anchored content.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/KptButton.kt`

```kotlin
fun KptButton(
```
Primary filled button following the KPT design system. Wraps `Button` with project-standard defaults. Prefer this over importing Material3 `Button` directly so design-system tokens can be applied in one place.

<details><summary>Example</summary>

```kotlin
KptButton(onClick = viewModel::onSubmit, enabled = uiState.canInteract) {
    Text("Save")
}
```

</details>

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/DraftResumeBanner.kt:82</code></summary>

```kotlin
                    Text(stringResource(Res.string.draft_discard))
                }
                KptButton(onClick = onResume) {
                    Text(stringResource(Res.string.draft_resume))
                }
            }
        }
```

</details>

```kotlin
fun KptOutlinedButton(
```
Outlined variant of `KptButton`.

```kotlin
fun KptTextButton(
```
Text (flat) variant of `KptButton`.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/DraftResumeBanner.kt:79</code></summary>

```kotlin
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                KptTextButton(onClick = onDiscard) {
                    Text(stringResource(Res.string.draft_discard))
                }
                KptButton(onClick = onResume) {
                    Text(stringResource(Res.string.draft_resume))
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/KptShimmerLoadingBox.kt`

```kotlin
fun KptShimmerLoadingBox(
```
Animated shimmer placeholder box. **Future migration:** new call sites should prefer `KptProgress(KptProgress.Shimmer(...))` from `kpt.core.base.designsystem.component.progress` — the unified progress family.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/CardLoadingSkeleton.kt:36</code></summary>

```kotlin
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KptShimmerLoadingBox(
                modifier = Modifier.fillMaxWidth().height(20.dp),
                shape = RoundedCornerShape(4.dp),
            )
            KptShimmerLoadingBox(
```

</details>

```kotlin
fun KptShimmerListItem(
```
Shimmer placeholder shaped like a list row — avatar, title and subtitle blocks. A loading affordance, not content: it must never outlive the load, or it reads as a broken row.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/progress/KptProgressRenderer.kt:92</code></summary>

```kotlin
            ) {
                repeat(variant.rowCount) {
                    KptShimmerListItem(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/KptToastHost.kt`

```kotlin
fun KptToastHost(
```
The app's transient-message host. Place once near the root of the UI. Replaces `KptSnackbarHost`, which wrapped Material3's `SnackbarHost`. Two reasons it went: 1.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/KptTopAppBar.kt`

```kotlin
fun KptTopAppBar(
```
Top app bar built from a declarative `KptTopAppBarConfiguration`. The DSL form — prefer it when a screen's bar is assembled from data or varies by state; the parameter overloads below are shorthands over this.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptForkScaffold.kt:69</code></summary>

```kotlin
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
```

</details>

```kotlin
fun KptTopAppBar(
```
Title-only bar with no navigation icon — a tab root. `variant` selects the Material 3 size.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptForkScaffold.kt:69</code></summary>

```kotlin
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
```

</details>

```kotlin
fun KptTopAppBar(
```
Title plus a back affordance, always shown — a pushed detail screen.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptForkScaffold.kt:69</code></summary>

```kotlin
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
```

</details>

```kotlin
fun KptTopAppBar(
```
Title with a CONDITIONAL back affordance, for a screen reachable both as a tab root and as a pushed destination.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptForkScaffold.kt:69</code></summary>

```kotlin
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
```

</details>

```kotlin
fun KptTopAppBar(
```
Two-line bar: title over a subtitle, for a screen whose context needs naming (an account, a date range) without stealing the title.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptForkScaffold.kt:69</code></summary>

```kotlin
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
```

</details>

```kotlin
fun KptTopAppBar(
```
Title plus ONE trailing action. For more than one, use the configuration overload rather than growing the parameter list.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptForkScaffold.kt:69</code></summary>

```kotlin
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
```

</details>

```kotlin
fun KptSearchAppBar(
```
Bar whose title area is a live search field, with the query hoisted to the caller.

```kotlin
fun KptProfileAppBar(
```
Bar carrying a profile avatar as its trailing action.

```kotlin
fun KptSettingsAppBar(
```
Bar preset for settings screens — back affordance plus the settings title treatment.

```kotlin
fun KptSmallTopAppBar(
```
Shorthand for the Small Material 3 bar — the default height.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonTest/kotlin/kpt/core/base/designsystem/component/KptTopAppBarUiTest.kt:35</code></summary>

```kotlin
    fun smallForwardsTheNavigationClick() = runComposeUiTest {
        var clicked = 0
        setContent { KptSmallTopAppBar(title = "T", onNavigationIconClick = { clicked++ }) }
        onNodeWithContentDescription("Navigation").performClick()
        assertEquals(1, clicked)
    }
```

</details>

```kotlin
fun KptCenterAlignedTopAppBar(
```
Shorthand for the centre-aligned bar.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonTest/kotlin/kpt/core/base/designsystem/component/KptTopAppBarUiTest.kt:43</code></summary>

```kotlin
    fun centerAlignedForwardsTheNavigationClick() = runComposeUiTest {
        var clicked = 0
        setContent { KptCenterAlignedTopAppBar(title = "T", onNavigationIconClick = { clicked++ }) }
        onNodeWithContentDescription("Navigation").performClick()
        assertEquals(1, clicked)
    }
```

</details>

```kotlin
fun KptMediumTopAppBar(
```
Shorthand for the Medium (collapsing) bar.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonTest/kotlin/kpt/core/base/designsystem/component/KptTopAppBarUiTest.kt:51</code></summary>

```kotlin
    fun mediumForwardsTheNavigationClick() = runComposeUiTest {
        var clicked = 0
        setContent { KptMediumTopAppBar(title = "T", onNavigationIconClick = { clicked++ }) }
        onNodeWithContentDescription("Navigation").performClick()
        assertEquals(1, clicked)
    }
```

</details>

```kotlin
fun KptLargeTopAppBar(
```
Shorthand for the Large (collapsing) bar.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonTest/kotlin/kpt/core/base/designsystem/component/KptTopAppBarUiTest.kt:59</code></summary>

```kotlin
    fun largeForwardsTheNavigationClick() = runComposeUiTest {
        var clicked = 0
        setContent { KptLargeTopAppBar(title = "T", onNavigationIconClick = { clicked++ }) }
        onNodeWithContentDescription("Navigation").performClick()
        assertEquals(1, clicked)
    }
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/SlideTransition.kt`

```kotlin
fun KptSlideTransition(
```
Slides `content` in and out along `direction`, using the shared motion durations.

```kotlin
enum class SlideDirection
```
Direction a `KptSlideTransition` enters from.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/utils/Transition.kt:274</code></summary>

```kotlin
        val slideUp: NonNullEnterTransitionProvider = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(DEFAULT_SLIDE_TRANSITION_TIME_MS),
            )
        }
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/progress/KptProgress.kt`

```kotlin
sealed interface KptProgress
```
Sealed family of "something is in progress" UI variants.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/progress/KptProgressRenderer.kt:41</code></summary>

```kotlin
 */
@Composable
fun KptProgress(
    variant: KptProgress,
    modifier: Modifier = Modifier,
) {
    val (diameter, stroke) = ProgressSizeSpec.dpFor(variant.size)
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/progress/KptProgressRenderer.kt`

```kotlin
fun KptProgress(
```
Single dispatch composable for every "something is in progress" UI in the toolkit. Pick a `KptProgress` variant; this renderer wires it to the right primitive at the right size.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/progress/KptProgress.kt:22</code></summary>

```kotlin
 * Use via the `KptProgress(variant, modifier)` composable in [KptProgressRenderer].
 */
sealed interface KptProgress {
    /** T-shirt size — drives diameter/stroke for Circular, height for Linear, row sizing for Skeleton. */
    val size: ProgressSize

    /**
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/progress/ProgressSizeSpec.kt`

```kotlin
enum class ProgressSize
```
T-shirt sizes for `KptProgress` variants. Maps to (diameter, stroke) dp pairs via `ProgressSizeSpec.dpFor` — keeps every project-wide progress indicator on a single set of rhythm-aligned dimensions.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/progress/KptProgress.kt:24</code></summary>

```kotlin
sealed interface KptProgress {
    /** T-shirt size — drives diameter/stroke for Circular, height for Linear, row sizing for Skeleton. */
    val size: ProgressSize

    /**
     * Material 3 [androidx.compose.material3.LinearProgressIndicator]. Use for top-of-
     * screen progress (refresh banners, multi-step flows).
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/core/ComponentStateHolder.kt`

```kotlin
class ComponentStateHolder<T>(initialValue: T) : ComponentState<T>
```
A concrete implementation of `ComponentState` that holds and manages component state. This class provides a thread-safe way to hold and update state values within components, with automatic recomposition when the state changes.

<details><summary>Example</summary>

```kotlin
class MyComponentState(initialExpanded: Boolean) {
    private val _expanded = ComponentStateHolder(initialExpanded)
    val expanded: ComponentState<Boolean> = _expanded

    fun toggleExpanded() {
        _expanded.update(!_expanded.value)
    }
}
```

</details>

```kotlin
fun <T> rememberComponentState(initialValue: T): ComponentState<T>
```
Remembers a `ComponentState` instance across recompositions.

<details><summary>Example</summary>

```kotlin
@Composable
fun ExpandableCard() {
    val expandedState = rememberComponentState(initialValue = false)

    Card(
        modifier = Modifier.clickable {
            expandedState.update(!expandedState.value)
        }
    ) {
        if (expandedState.value) {
            DetailContent()
        } else {
            SummaryContent()
        }
    }
}
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/core/KptComponent.kt`

```kotlin
interface KptComponent
```
The base contract every `Kpt*` component satisfies: a test tag, a content description and a caller-supplied `Modifier`.

- `val testTag: String?` — Stable identifier for UI tests. Null means the component is not addressable — acceptable only for purely decorative content.
- `val contentDescription: String?` — Screen-reader description, or null when the component is decorative and should be skipped by accessibility services.
- `val modifier: Modifier` — Caller-supplied modifier, applied to the component's outermost node so padding and sizing from the call site win.

```kotlin
interface Clickable
```
Mixed into components that respond to a tap. `interactionSource` is exposed so a caller can hoist ripple/press state — a component that owns it privately cannot participate in a parent's interaction handling.

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/ui/MerchantsItem.kt:44</code></summary>

```kotlin
) {
    MifosCard(
        modifier = modifier.combinedClickable(
            onClick = onMerchantClicked,
            onLongClick = {
                onMerchantLongPressed(savingsWithAssociations.id.toString())
            },
```

</details>

- `val onClick: () -> Unit` — Invoked on tap. Not called while `enabled` is false.
- `val enabled: Boolean` — Whether the tap is accepted. A disabled component still renders and is still read by accessibility services.
- `val interactionSource: MutableInteractionSource?` — Hoisted press/ripple state, or null to let the component own it. Pass one when a parent must react to the same interaction.

```kotlin
interface Styleable
```
Mixed into components whose colors, shape and elevation can be overridden at the call site.

- `val colors: ComponentColors?` — Color overrides, or null to inherit the theme's.
- `val shape: Shape?` — Shape override, or null to inherit the theme's shape for this component's size class.
- `val elevation: ComponentElevation?` — Elevation override, or null to inherit the theme's.

```kotlin
interface Themeable
```
Mixed into components that accept a whole `ComponentTheme` rather than individual style slots.

- `val theme: ComponentTheme?` — A complete theme for this component, or null to resolve from the ambient one.

```kotlin
interface ComponentColors
```
Marker for a component's color set. Each component defines its own slots; the marker exists so `Styleable` can carry them without knowing the shape.

```kotlin
interface ComponentElevation
```
Marker for a component's elevation set, per interaction state (resting, pressed, focused).

```kotlin
interface ComponentTheme
```
Marker for a complete component theme — colors, shape and elevation resolved together.

```kotlin
interface ThemeStrategy
```
Resolves the `ComponentTheme` for a component, letting a fork swap the whole theming rule rather than overriding components one at a time.

- `fun applyTheme(component: KptComponent): ComponentTheme` — Resolves the theme for `component`. Called per render, so it must be cheap and free of side effects.

```kotlin
interface ComponentFactory<T : KptComponent>
```
Builds a component of type `T` from a `ComponentConfiguration` — the seam that lets components be constructed from data (a registry, a server-driven layout) instead of only from Kotlin call sites.

- `fun create(configuration: ComponentConfiguration): T` — Builds the component described by `configuration`.

```kotlin
interface ComponentConfiguration
```
A component's declarative description, convertible to the component itself via `build`.

- `fun build(): KptComponent` — Materialises this description into a component.

```kotlin
interface ComponentState<T>
```
Observable holder for one component's mutable value. `@Stable` so Compose can skip recomposition when the reference is unchanged; mutate through `update` rather than replacing the holder, or that guarantee is lost.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/core/ComponentStateHolder.kt:48</code></summary>

```kotlin
 */
@Stable
class ComponentStateHolder<T>(initialValue: T) : ComponentState<T> {
    /**
     * The current value of the state. Reading this property in a composable
     * will cause that composable to recompose when the value changes.
     *
```

</details>

- `val value: T` — The current value.
- `fun update(newValue: T)` — Replaces the value in place. Mutating through this preserves the `@Stable` contract; swapping the holder does not.

```kotlin
sealed interface ComponentVariant
```
A named visual variant of a component (filled, outlined, tonal, …). Sealed so the variant set is closed and exhaustively handled at each render site.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/core/KptTopAppBarConfiguration.kt:28</code></summary>

```kotlin
 * @see KptTopAppBarConfiguration
 */
sealed interface TopAppBarVariant : ComponentVariant {
    override val name: String

    /**
     * Standard compact top app bar suitable for most screens.
```

</details>

```kotlin
interface ComponentComposer
```
Renders a list of components as one composition — used where a screen's content is assembled from data rather than written out.

- `fun Compose(components: List<KptComponent>)` — Renders `components` in order as one composition.

```kotlin
interface Animatable
```
Mixed into components with a tunable transition. See `theme/Motion.kt` for the shared durations; overriding per component is what makes an app's motion feel inconsistent.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/LoadingWheel.kt:61</code></summary>

```kotlin
    // Specifies the float animation for slowly drawing out the lines on entering
    val startValue = if (LocalInspectionMode.current) 0F else 1F
    val floatAnimValues = (0 until NUM_OF_LINES).map { remember { Animatable(startValue) } }
    LaunchedEffect(floatAnimValues) {
        (0 until NUM_OF_LINES).map { index ->
            launch {
                floatAnimValues[index].animateTo(
```

</details>

- `val animationDuration: Long` — Transition length in milliseconds. Prefer the shared values in `theme/Motion.kt`; a per-component number is what makes motion feel uneven.
- `val animationEasing: androidx.compose.animation.core.Easing?` — Easing curve, or null for the theme's default.

```kotlin
interface AccessibilityProvider
```
Supplies a component's semantics — description, role and any extra properties. Separate from `KptComponent` so a component can delegate accessibility to a wrapper rather than re-declaring it.

- `val semantics: androidx.compose.ui.semantics.SemanticsPropertyReceiver.() -> Unit` — Extra semantics applied to the component's node, beyond description and role.
- `val contentDescription: String?` — Screen-reader description, or null when the component is decorative and should be skipped by accessibility services.
- `val role: androidx.compose.ui.semantics.Role?` — The component's accessibility role, or null to let the platform infer it.

```kotlin
interface KptThemeProvider
```
The whole design language in one object: colors, typography, shapes, spacing and elevation. A fork supplies its own and every component follows, which is the point of the indirection.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptMaterialTheme.kt:45</code></summary>

```kotlin
@Composable
fun KptMaterialTheme(
    theme: KptThemeProvider = KptThemeProviderImpl(),
    content: @Composable () -> Unit,
) {
    // Convert KptTheme values to Material3 equivalents
    val materialColorScheme = theme.colors.toMaterial3ColorScheme()
```

</details>

- `val colors: KptColorScheme` — Color overrides, or null to inherit the theme's.
- `val typography: KptTypography` — The type scale.
- `val shapes: KptShapes` — The corner-shape scale.
- `val spacing: KptSpacing` — The spacing scale.
- `val elevation: KptElevation` — Elevation override, or null to inherit the theme's.

```kotlin
interface KptColorScheme
```
The full Material 3 color role set. Roles, not literal colors — a component asks for `onSurfaceVariant`, never a hex value, so light and dark themes and a fork's palette all work without touching the component.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/ColorScheme.kt:226</code></summary>

```kotlin
)

val lightKptColorScheme = LightDefaultColorScheme.toKptColorScheme()
val darkKptColorScheme = DarkDefaultColorScheme.toKptColorScheme()
```

</details>

- `val primary: Color` — The brand's main accent — filled buttons, active selection, the FAB.
- `val onPrimary: Color` — Content drawn on `primary`. Guaranteed to meet contrast against it.
- `val primaryContainer: Color` — A low-emphasis primary surface, for a tonal button or a selected chip.
- `val onPrimaryContainer: Color` — Content drawn on `primaryContainer`.
- `val inversePrimary: Color` — Primary as seen on an inverted surface — a snackbar's action, which sits on `inverseSurface`.
- `val secondary: Color` — A supporting accent, for controls that must be visible without competing with `primary`.
- `val onSecondary: Color` — Content drawn on `secondary`.
- `val secondaryContainer: Color` — A low-emphasis secondary surface, typically a navigation item's selected indicator.
- `val onSecondaryContainer: Color` — Content drawn on `secondaryContainer`.
- `val tertiary: Color` — A contrasting accent used to draw attention to a distinct third category — not a third brand colour.
- `val onTertiary: Color` — Content drawn on `tertiary`.
- `val tertiaryContainer: Color` — A low-emphasis tertiary surface.
- `val onTertiaryContainer: Color` — Content drawn on `tertiaryContainer`.
- `val background: Color` — The window's base colour, behind all content.
  _…more members; read the file._

```kotlin
interface KptTypography
```
The Material 3 type scale — display through label, each in three sizes.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:52</code></summary>

```kotlin
    }.toKptColorScheme()

    val mifosTypography = Typography().toKptTypography(fontFamily)

    val themeProvider = KptThemeProviderImpl(
        colors = colorScheme,
        typography = mifosTypography,
```

</details>

- `val displayLarge: androidx.compose.ui.text.TextStyle` — The largest type — a single short string on a splash or hero. Never body copy.
- `val displayMedium: androidx.compose.ui.text.TextStyle` — Display at medium size.
- `val displaySmall: androidx.compose.ui.text.TextStyle` — Display at small size.
- `val headlineLarge: androidx.compose.ui.text.TextStyle` — A screen's title.
- `val headlineMedium: androidx.compose.ui.text.TextStyle` — A major section heading.
- `val headlineSmall: androidx.compose.ui.text.TextStyle` — A minor section heading.
- `val titleLarge: androidx.compose.ui.text.TextStyle` — A card or dialog title.
- `val titleMedium: androidx.compose.ui.text.TextStyle` — A list item's primary line.
- `val titleSmall: androidx.compose.ui.text.TextStyle` — A dense list item's primary line.
- `val bodyLarge: androidx.compose.ui.text.TextStyle` — Default reading copy — the longest text on a screen.
- `val bodyMedium: androidx.compose.ui.text.TextStyle` — Secondary copy and supporting text.
- `val bodySmall: androidx.compose.ui.text.TextStyle` — Captions, timestamps and footnotes.
- `val labelLarge: androidx.compose.ui.text.TextStyle` — A button's label.
- `val labelMedium: androidx.compose.ui.text.TextStyle` — A chip or tab label.
  _…more members; read the file._

```kotlin
interface KptShapes
```
The corner-shape scale, from `extraSmall` to `extraLarge`, applied by component size rather than chosen per call site.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/ThemeExtensions.kt:33</code></summary>

```kotlin
 */

fun KptShapes.toRoundedCornerShape(
    topStart: Dp = 0.dp,
    topEnd: Dp = 0.dp,
    bottomStart: Dp = 0.dp,
    bottomEnd: Dp = 0.dp,
```

</details>

- `val extraSmall: CornerBasedShape` — Tightest corner — a badge or a small chip.
- `val small: CornerBasedShape` — A text field or a compact button.
- `val medium: CornerBasedShape` — The default: cards and most containers.
- `val large: CornerBasedShape` — A bottom sheet or a large dialog.
- `val extraLarge: CornerBasedShape` — A full-bleed or hero surface.

```kotlin
interface KptSpacing
```
The spacing scale every layout measures with. Components reference these rather than literal `.dp` values so density stays uniform and a fork can retune the whole app's rhythm in one place.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptThemeExtensions.kt:61</code></summary>

```kotlin
 */
@Composable
fun KptSpacing.paddingValues(
    horizontal: Dp = md,
    vertical: Dp = md,
): PaddingValues = PaddingValues(horizontal = horizontal, vertical = vertical)
```

</details>

- `val xs: Dp` — Tightest step — icon-to-label gaps and chip padding.
- `val sm: Dp` — Padding inside a compact control.
- `val md: Dp` — The default step: padding inside a card, and the gap between sibling controls.
- `val lg: Dp` — Gap between sections of a screen.
- `val xl: Dp` — Screen-edge margin on a large window.
- `val xxl: Dp` — Reserved for full-bleed layouts; rarely the right answer inside a card.

```kotlin
interface KptElevation
```
The elevation scale, in Material 3 levels 0–5.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptThemeExtensions.kt:414</code></summary>

```kotlin
 */
@Composable
fun KptElevation.cardElevation(
    defaultElevation: Dp = level1,
    pressedElevation: Dp = level2,
    focusedElevation: Dp = level2,
    hoveredElevation: Dp = level2,
```

</details>

- `val level0: Dp` — Flat on the surface — no tint, no shadow.
- `val level1: Dp` — A resting card.
- `val level2: Dp` — A resting menu or a raised button.
- `val level3: Dp` — A dialog, or a card while pressed.
- `val level4: Dp` — A navigation drawer.
- `val level5: Dp` — The highest step — reserved for a temporary overlay above everything else.

```kotlin
interface ComponentRenderer<T : KptComponent>
```
Renders component type `T`. Registered in a `ComponentRegistry` so a data-driven layout can resolve a renderer by type at runtime.

- `fun Render(component: T)` — Renders `component`.

```kotlin
interface ComponentRegistry
```
Maps component types to their renderers and factories — the lookup a `ComponentComposer` uses.

- `fun <T : KptComponent> register(type: KClass<T>, renderer: ComponentRenderer<T>)` — Registers `renderer` for `type`, replacing any previous registration.
- `fun <T : KptComponent> getRenderer(type: KClass<T>): ComponentRenderer<T>?` — The renderer for `type`, or null when none is registered — a data-driven layout must handle null rather than assume coverage.

```kotlin
annotation class ComponentDsl
```
DSL marker for the component-configuration builders. Stops an inner builder from implicitly seeing an outer scope's receivers, which is how nested DSL blocks silently configure the wrong component.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/theme/KptColorSchemeImpl.kt:266</code></summary>

```kotlin
 * DSL builder for a complete [KptThemeProvider]. Entry point: [kptTheme].
 */
@ComponentDsl
class KptThemeBuilder {
    private var colors: KptColorScheme = KptColorSchemeImpl()
    private var typography: KptTypography = KptTypographyImpl()
    private var shapes: KptShapes = KptShapesImpl()
```

</details>

```kotlin
interface ComponentConfigurationScope
```
Receiver for the component-configuration DSL, scoped by `ComponentDsl`.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/core/KptTopAppBarConfiguration.kt`

```kotlin
sealed interface TopAppBarVariant : ComponentVariant
```
Defines the visual variants available for the KPT top app bar. Each variant corresponds to a different Material3 top app bar style with different visual characteristics and use cases.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/KptTopAppBar.kt:130</code></summary>

```kotlin

    when (configuration.variant) {
        TopAppBarVariant.Small -> TopAppBar(
            title = titleContent,
            modifier = finalModifier,
            navigationIcon = { NavigationIconSlot(configuration) },
            actions = actionsContent,
```

</details>

```kotlin
data class KptTopAppBarConfiguration(
```
Configuration class that defines all properties for a KPT top app bar. This immutable data class encapsulates all the customization options for the top app bar, providing a clean API for complex configurations.

<details><summary>Example</summary>

```kotlin
val config = KptTopAppBarConfiguration(
    title = "My Screen",
    variant = TopAppBarVariant.Large,
    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
    onNavigationIonClick = { navController.navigateUp() },
    actions = listOf(
        TopAppBarAction(
            icon = Icons.Default.Search,
            contentDescription = "Search",
            onClick = { openSearch() }
        )
    ),
    subtitle = "Optional subtitle"
)
```

</details>

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/KptTopAppBar.kt:55</code></summary>

```kotlin
 */
@Composable
private fun NavigationIconSlot(configuration: KptTopAppBarConfiguration) {
    val icon = configuration.navigationIcon ?: return
    IconButton(
        onClick = configuration.onNavigationIonClick ?: {},
        enabled = configuration.onNavigationIonClick != null,
```

</details>

```kotlin
data class TopAppBarAction(
```
Represents an action button in the top app bar. Action buttons are displayed on the right side of the top app bar and provide quick access to common functions like search, menu, or other contextual actions.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptForkScaffold.kt:63</code></summary>

```kotlin
        .only(WindowInsetsSides.Horizontal),
    snackbarHost: @Composable () -> Unit = {},
    actions: List<TopAppBarAction> = emptyList(),
    content: @Composable () -> Unit = {},
) {
    Scaffold(
        topBar = {
```

</details>

```kotlin
annotation class TopAppBarDsl
```
DSL marker for the top-app-bar builders, so a nested block cannot implicitly configure an outer bar.

```kotlin
class KptTopAppBarBuilder
```
DSL builder class for creating `KptTopAppBarConfiguration` instances. This builder provides a fluent API for configuring top app bars with a clean, readable syntax. All properties have sensible defaults and can be customized as needed.

<details><summary>Example</summary>

```kotlin
val config = kptTopAppBar {
    title = "Settings"
    variant = TopAppBarVariant.Large
    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
    onNavigationClick = { navController.navigateUp() }

    action(Icons.Default.Search, "Search") { openSearch() }
    action(Icons.Default.MoreVert, "More options") { showMenu() }

    subtitle = "Customize your experience"
    testTag = "SettingsTopAppBar"
}
```

</details>

```kotlin
fun kptTopAppBar(block: KptTopAppBarBuilder.() -> Unit): KptTopAppBarConfiguration
```
DSL function for creating a `KptTopAppBarConfiguration` using a builder pattern. This function provides a convenient way to configure top app bars with a clean, type-safe DSL syntax. All configuration is done within the builder block.

<details><summary>Example</summary>

```kotlin
val topAppBarConfig = kptTopAppBar {
    title = "My Screen"
    variant = TopAppBarVariant.Medium
    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
    onNavigationClick = { navController.navigateUp() }

    action(Icons.Default.Search, "Search") {
        // Handle search
    }

    action(Icons.Default.Favorite, "Add to favorites") {
        // Handle favorite
    }
}

KptTopAppBar(topAppBarConfig)
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/AdaptiveListDetailPaneScaffold.kt`

```kotlin
fun AdaptiveListDetailPaneScaffold(
```
A layout scaffold for adaptive list-detail navigation using Material 3's `ListDetailPaneScaffold`. This composable allows you to build responsive UIs with two primary panes: a main (list) pane and a detail pane, with an optional third pane.

<details><summary>Example</summary>

```kotlin
AdaptiveListDetailPaneScaffold(
    mainPaneContent = { navigateToDetail ->
        LazyColumn {
            items(itemsList) { item ->
                ListItem(
                    headlineText = { Text(item.title) },
                    modifier = Modifier.clickable { navigateToDetail() }
                )
            }
        }
    },
    detailPaneContent = { navigateBack ->
        Column {
            Text("Detail view")
            Button(onClick = navigateBack) { Text("Back") }
        }
    }
)
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/AdaptiveNavigableListDetailScaffold.kt`

```kotlin
fun <T : PaneScaffoldItem<*>> AdaptiveNavigableListDetailPaneScaffold(
```
List-detail scaffold that adapts to window size: two panes side by side when wide, and a navigable single pane when narrow.

```kotlin
sealed interface SelectionVisibilityState
```
Describes the current selection state for the list pane within an adaptive layout. Used to determine how list items should behave (clickable vs. selectable) and how they are styled.

```kotlin
interface PaneScaffoldItem<T : Any>
```
An item the scaffold can show in either pane, carrying the identity it is selected by.

- `val id: T` — The item's identity, used to match a selection across a pane change. Must be stable across recompositions, or the detail pane loses its selection on rotation or a fold.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/AdaptiveNavigableSupportingPaneScaffold.kt`

```kotlin
fun AdaptiveNavigableSupportingPaneScaffold(
```
A composable layout for adaptive UIs that implements a navigable two-pane structure using Material 3's `SupportingPaneScaffold`.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/AdaptiveNavigationSuiteScaffold.kt`

```kotlin
fun AdaptiveNavigationSuiteScaffold(
```
A responsive scaffold that adapts the navigation UI (drawer, rail, or bottom bar) based on the current window size and device posture.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptFlowColumn.kt`

```kotlin
fun KptFlowColumn(
```
Column that wraps into additional columns when content exceeds the available height.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonTest/kotlin/kpt/core/base/designsystem/layout/KptFlowColumnUiTest.kt:38</code></summary>

```kotlin
    fun rendersASingleColumnWithoutCrashing() = runComposeUiTest {
        setContent {
            KptFlowColumn {
                Box(Modifier.size(20.dp).testTag("a"))
                Box(Modifier.size(20.dp).testTag("b"))
            }
        }
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptFlowRow.kt`

```kotlin
fun KptFlowRow(
```
Row that wraps onto additional lines when content exceeds the available width.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonTest/kotlin/kpt/core/base/designsystem/layout/KptFlowRowUiTest.kt:42</code></summary>

```kotlin
    fun rendersASingleRowWithoutCrashing() = runComposeUiTest {
        setContent {
            KptFlowRow {
                Box(Modifier.size(20.dp).testTag("a"))
                Box(Modifier.size(20.dp).testTag("b"))
            }
        }
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptGrid.kt`

```kotlin
fun KptGrid(
```
Responsive grid whose column count derives from the available width via `BreakpointConfiguration`.

```kotlin
interface GridScope
```
Receiver for `KptGrid` content — declare items and spans here.

- `fun Modifier.gridItem(span: Int = 1): Modifier` — Claims `span` columns for this item. Clamped to the grid's column count, so an over-wide span degrades to full width instead of overflowing.

```kotlin
data class GridConfiguration(
```
Spacing and padding for a `KptGrid`; defaults come from the theme spacing scale.

```kotlin
data class BreakpointConfiguration(
```
Width thresholds mapping available width to a column count.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptMasonryGrid.kt`

```kotlin
fun KptMasonryGrid(
```
Staggered grid for items of differing heights, packing each column independently.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptResponsiveLayout.kt`

```kotlin
fun KptResponsiveLayout(
```
A responsive layout composable that adapts content based on screen size breakpoints.

<details><summary>Example</summary>

```kotlin
KptResponsiveLayout(
    compact = {
        // Single column layout for phones
        LazyColumn {
            items(data) { item -> ItemCard(item) }
        }
    },
    medium = {
        // Two column grid for tablets
        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            items(data) { item -> ItemCard(item) }
        }
    },
    expanded = {
        // Three column layout with sidebar for desktop
        Row {
            Sidebar(modifier = Modifier.width(240.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f)
            ) {
                items(data) { item -> ItemCard(item) }
            }
        }
    }
)
```

</details>

```kotlin
class ResponsiveLayoutInfo(
```
Contains information about the current screen size and responsive breakpoints. This class provides both the raw screen dimensions and convenience boolean flags for determining which breakpoint the current screen size falls into.

```kotlin
fun rememberResponsiveLayoutInfo(): ResponsiveLayoutInfo
```
Remembers and provides responsive layout information based on the current window size.

<details><summary>Example</summary>

```kotlin
@Composable
fun MyScreen() {
    val layoutInfo = rememberResponsiveLayoutInfo()

    when {
        layoutInfo.isCompact -> CompactLayout()
        layoutInfo.isMedium -> MediumLayout()
        layoutInfo.isExpanded -> ExpandedLayout()
    }
}
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptSidebarLayout.kt`

```kotlin
fun KptSidebarLayout(
```
Persistent sidebar beside content, for wide windows. `sidebarVisible` is hoisted, so this never toggles itself: it reports a user-initiated dismissal through `onSidebarVisibilityChange` and leaves the decision to the caller.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonTest/kotlin/kpt/core/base/designsystem/layout/KptSidebarLayoutUiTest.kt:42</code></summary>

```kotlin
    fun anOverlaySidebarIsComposedExactlyOnce() = runComposeUiTest {
        setContent {
            KptSidebarLayout(
                sidebarContent = { Box(Modifier.fillMaxSize().testTag(panelTag)) },
                configuration = SidebarConfiguration(overlay = true),
                sidebarVisible = true,
                content = { Box(Modifier.fillMaxSize()) },
```

</details>

```kotlin
data class SidebarConfiguration(
```
Width and behaviour of a `KptSidebarLayout` sidebar.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonTest/kotlin/kpt/core/base/designsystem/layout/KptSidebarLayoutUiTest.kt:44</code></summary>

```kotlin
            KptSidebarLayout(
                sidebarContent = { Box(Modifier.fillMaxSize().testTag(panelTag)) },
                configuration = SidebarConfiguration(overlay = true),
                sidebarVisible = true,
                content = { Box(Modifier.fillMaxSize()) },
            )
        }
```

</details>

```kotlin
enum class SidebarPosition
```
Which edge the sidebar occupies.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptSplitPane.kt`

```kotlin
fun KptSplitPane(
```
Two panes with a draggable divider, for wide windows. `minLeftWidth` stops the divider being dragged to a width where the left pane is unusable.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonTest/kotlin/kpt/core/base/designsystem/layout/KptSplitPaneUiTest.kt:43</code></summary>

```kotlin
    fun draggingTheDividerMovesTheSplit() = runComposeUiTest {
        setContent {
            KptSplitPane(
                leftContent = { Box(Modifier.fillMaxSize().testTag("left")) },
                rightContent = { Box(Modifier.fillMaxSize().testTag("right")) },
                minLeftWidth = 0.dp,
                minRightWidth = 0.dp,
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptStack.kt`

```kotlin
fun KptStack(
```
Z-stacks its children with a shared alignment — overlays, badges, layered art.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/theme/KptColorSchemeImpl.kt`

```kotlin
data class KptColorSchemeImpl(
```
Default `KptColorScheme` — the Material 3 baseline palette. `@Immutable` so Compose can skip recomposition when the instance is unchanged. A fork overrides only the roles it brands and inherits the rest, rather than restating all fifty.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptThemeExtensions.kt:346</code></summary>

```kotlin
 * Adapts a Material 3 `ColorScheme` into a [KptColorScheme] — role for role, no colour invented.
 */
fun ColorScheme.toKptColorScheme(): KptColorScheme = KptColorSchemeImpl(
    primary = this.primary,
    onPrimary = this.onPrimary,
    primaryContainer = this.primaryContainer,
    onPrimaryContainer = this.onPrimaryContainer,
```

</details>

```kotlin
data class KptTypographyImpl(
```
Default `KptTypography` — the Material 3 type scale at its standard sizes and weights.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptThemeExtensions.kt:178</code></summary>

```kotlin
 */
fun Typography.toKptTypography(fontFamily: FontFamily? = FontFamily.Default): KptTypography =
    KptTypographyImpl(
        displayLarge = this.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = this.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = this.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = this.headlineLarge.copy(fontFamily = fontFamily),
```

</details>

```kotlin
data class KptShapesImpl(
```
Default `KptShapes` — the Material 3 corner scale, 4dp through 28dp.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptThemeExtensions.kt:402</code></summary>

```kotlin
 * Adapts a Material 3 `Shapes` into a [KptShapes].
 */
fun Shapes.toKptShapes(): KptShapes = KptShapesImpl(
    extraSmall = this.extraSmall,
    small = this.small,
    medium = this.medium,
    large = this.large,
```

</details>

```kotlin
data class KptSpacingImpl(
```
Default `KptSpacing` — a 4dp-based scale. Components reference these rather than literal `.dp`, so retuning density is one edit here instead of a sweep through every layout.

```kotlin
data class KptElevationImpl(
```
Default `KptElevation` — Material 3 levels 0–5.

```kotlin
data class KptThemeProviderImpl(
```
Default `KptThemeProvider`, composing the five default scales into one design language.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:54</code></summary>

```kotlin
    val mifosTypography = Typography().toKptTypography(fontFamily)

    val themeProvider = KptThemeProviderImpl(
        colors = colorScheme,
        typography = mifosTypography,
    )
```

</details>

```kotlin
val LocalKptColors = staticCompositionLocalOf<KptColorScheme> { KptColorSchemeImpl() }
```
CompositionLocal carrying the active `KptColorScheme`. `static` because the theme changes rarely — a read does not subscribe, so a palette swap recomposes the subtree rather than every reader.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptMaterialTheme.kt:55</code></summary>

```kotlin
    // Provide both KptTheme composition locals and MaterialTheme
    CompositionLocalProvider(
        LocalKptColors provides theme.colors,
        LocalKptTypography provides theme.typography,
        LocalKptShapes provides theme.shapes,
        LocalKptSpacing provides theme.spacing,
        LocalKptElevation provides theme.elevation,
```

</details>

```kotlin
val LocalKptTypography = staticCompositionLocalOf<KptTypography> { KptTypographyImpl() }
```
CompositionLocal carrying the active `KptTypography`.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptMaterialTheme.kt:56</code></summary>

```kotlin
    CompositionLocalProvider(
        LocalKptColors provides theme.colors,
        LocalKptTypography provides theme.typography,
        LocalKptShapes provides theme.shapes,
        LocalKptSpacing provides theme.spacing,
        LocalKptElevation provides theme.elevation,
    ) {
```

</details>

```kotlin
val LocalKptShapes = staticCompositionLocalOf<KptShapes> { KptShapesImpl() }
```
CompositionLocal carrying the active `KptShapes`.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptMaterialTheme.kt:57</code></summary>

```kotlin
        LocalKptColors provides theme.colors,
        LocalKptTypography provides theme.typography,
        LocalKptShapes provides theme.shapes,
        LocalKptSpacing provides theme.spacing,
        LocalKptElevation provides theme.elevation,
    ) {
        MaterialTheme(
```

</details>

```kotlin
val LocalKptSpacing = staticCompositionLocalOf<KptSpacing> { KptSpacingImpl() }
```
CompositionLocal carrying the active `KptSpacing`.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptMaterialTheme.kt:58</code></summary>

```kotlin
        LocalKptTypography provides theme.typography,
        LocalKptShapes provides theme.shapes,
        LocalKptSpacing provides theme.spacing,
        LocalKptElevation provides theme.elevation,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
```

</details>

```kotlin
val LocalKptElevation = staticCompositionLocalOf<KptElevation> { KptElevationImpl() }
```
CompositionLocal carrying the active `KptElevation`.

<details><summary>Used in the template — <code>core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptMaterialTheme.kt:59</code></summary>

```kotlin
        LocalKptShapes provides theme.shapes,
        LocalKptSpacing provides theme.spacing,
        LocalKptElevation provides theme.elevation,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = materialTypography,
```

</details>

```kotlin
class KptThemeBuilder
```
DSL builder for a complete `KptThemeProvider`. Entry point: `kptTheme`.

```kotlin
class KptColorSchemeBuilder
```
DSL builder for a `KptColorScheme`; unset roles keep their defaults.

```kotlin
class KptTypographyBuilder
```
DSL builder for a `KptTypography`; unset styles keep their defaults.

```kotlin
class KptShapesBuilder
```
DSL builder for a `KptShapes`; unset corners keep their defaults.

```kotlin
class KptSpacingBuilder
```
DSL builder for a `KptSpacing`; unset steps keep their defaults.

```kotlin
class KptElevationBuilder
```
DSL builder for a `KptElevation`; unset levels keep their defaults.

```kotlin
object KptTheme
```
Composition-local accessor for the active design language — `KptTheme.colors`, `.typography`, `.shapes`, `.spacing`, `.elevation`. The read side of the theme; `kptTheme` is the write side.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:197</code></summary>

```kotlin
                        subTitle = stringResource(Res.string.feature_accounts_unexpected_error_subtitle),
                        modifier = Modifier,
                        iconTint = KptTheme.colorScheme.error,
                    )
                }

                is AccountState.ViewState.Content -> {
```

</details>

- `val colorScheme: KptColorScheme` — The active colour scheme, from the nearest `KptTheme` in the composition.
- `val typography: KptTypography` — The active type scale.
- `val shapes: KptShapes` — The active corner-shape scale.
- `val spacing: KptSpacing` — The active spacing scale — what every layout measures with.
- `val elevation: KptElevation` — The active elevation scale, Material 3 levels 0–5.

```kotlin
fun kptTheme(block: KptThemeBuilder.() -> Unit): KptThemeProvider
```
Builds a `KptThemeProvider` with the DSL, overriding only what a fork brands: ```kotlin val theme = kptTheme { colors { primary = BrandPurple } } ```

<details><summary>Example</summary>

```kotlin
val theme = kptTheme { colors { primary = BrandPurple } }
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/theme/Motion.kt`

```kotlin
data class Motion(
```
Shared motion specs — durations, easings, and motion-pattern parameters. Values align with Material 3 motion guidance (https://m3.material.io/styles/motion/easing-and-duration). All durations in milliseconds.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:75</code></summary>

```kotlin
        CompositionLocalProvider(
            LocalFinanceColors provides financeColors,
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
        ) {
```

</details>

```kotlin
val LocalMotion = staticCompositionLocalOf { Motion() }
```
CompositionLocal carrying the app's motion scale — the shared durations and easings. Read this rather than hardcoding a duration: symmetric enter/exit timings are what make transitions feel like one system instead of per-screen choices.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:75</code></summary>

```kotlin
        CompositionLocalProvider(
            LocalFinanceColors provides financeColors,
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
        ) {
```

</details>

```kotlin
val MaterialTheme.motion: Motion
```
Resolve the active `Motion` specs from composition.

```kotlin
object MotionSnapshot
```
Last-read snapshot of the active `Motion`. Updated as a side effect whenever any `@Composable` site reads `MaterialTheme.motion`.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/nav/NavGraphBuilderExtensions.kt:41</code></summary>

```kotlin
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composableWithSlideTransitions<T>(MotionSnapshot.current, typeMap, deepLinks, content)

/** Registers a destination with slide transitions — a modal-style arrival. */
inline fun <reified T : Any> NavGraphBuilder.composableWithSlideTransitions(
    motion: Motion,
```

</details>

---

_64 type(s), 179 function(s)/property(ies); 233 carry KDoc at source; 20 authored example(s); 60 live call site(s)._
<!-- api-docs:end -->
