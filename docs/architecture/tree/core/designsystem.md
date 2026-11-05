# `core/designsystem`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DESIGNSYSTEM.md`
> **Measured:** 77 Kotlin files, 4 test files

## Principal types

`AppIcons`, `BackgroundTheme`, `BarDatum`, `BasicDialogState`, `Candle`, `ChartTokens`, `DonutSlice`, `Elevation`, `ExpirationDateMask`, `FinanceColors`, `FloatingActionButtonContent`, `GradientColors`, `LoadingDialogState`, `MifosButtonDefaults`  …and 18 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/designsystem sha=0f54af0f1b9883c1d0c5c0b2f50675208efbcdf2 -->
## API reference

_Generated from `core/designsystem` at tree `0f54af0f1b98` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/ChartTokens.kt`

```kotlin
object ChartTokens
```
Shared visual tokens for every chart in `core/designsystem/chart/`. Reads from `MaterialTheme.colorScheme`, `MaterialTheme.typography`, and `MaterialTheme.finance`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptAreaChart.kt:66</code></summary>

```kotlin
        drawPath(
            path = areaPath,
            brush = ChartTokens.areaFillBrush(fillColor),
        )

        // Top line.
        val linePath = Path().apply {
```

</details>

- `fun multiSeriesColors(): List<Color>` — Six distinguishable series hues, in a fixed order so the same series keeps its colour across recompositions and across charts.
- `val finance = MaterialTheme.finance`
- `fun axisLabelStyle(): TextStyle = MaterialTheme.typography.bodySmall.copy(` — Text style for axis labels.
- `fun gridlineColor(): Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)` — Gridline colour — outline-variant at half alpha, so gridlines sit behind the data rather than competing with it.
- `fun areaFillBrush(strokeColor: Color): Brush = Brush.verticalGradient(` — Vertical gradient from `strokeColor` at 24% alpha down to transparent, for the fill under a line series.
- `val defaultStrokeWidth = 1.5.dp` — Line width for a data series.
- `val defaultAxisStrokeWidth = 1.0.dp` — Line width for an axis — deliberately thinner than `defaultStrokeWidth`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptAreaChart.kt`

```kotlin
fun KptAreaChart(
```
Filled area chart for the detail-screen Hero. Same geometry contract as `KptSparkline` but adds a gradient fill under the line via `ChartTokens.areaFillBrush`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptBarChart.kt`

```kotlin
data class BarDatum(
```
One bar of `KptBarChart`. `value` is unitless — the chart normalizes each bar to a fraction of the running max.

```kotlin
fun KptBarChart(
```
Vertical bar chart with rounded tops and optional axis labels. Practical input cap: ~24 bars (2 years monthly). For longer series, caller decimates or buckets. Empty input → renders nothing.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptCandlestick.kt`

```kotlin
data class Candle(
```
One open-high-low-close bar for `KptCandlestick`. All values are in the same unit (price, rate, etc.) and rendered against a shared y-axis covering the series' min/max.

<details><summary>Used in the template — <code>core/designsystem/src/commonTest/kotlin/kpt/core/designsystem/chart/KptCandlestickTest.kt:25</code></summary>

```kotlin
    @Test
    fun closeAboveOpenIsUp() {
        val candle = Candle(open = 100f, high = 110f, low = 95f, close = 108f)
        assertTrue(candle.isUp)
    }

    @Test
```

</details>

```kotlin
fun KptCandlestick(
```
Classic OHLC candlestick chart for price-like time series. Up candles (close ≥ open) use `upColor`; down candles use `downColor`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptDonutChart.kt`

```kotlin
data class DonutSlice(
```
One slice of `KptDonutChart`. `value` is unitless — the chart normalizes each slice to a fraction of the sum.

```kotlin
fun KptDonutChart(
```
Canvas-based donut chart. Renders concentric arcs around a hollow center, optionally hosting a centered Composable (e.g. summary number). Practical input cap: any size (rendering cost is per-slice, fixed).

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptSparkline.kt`

```kotlin
fun KptSparkline(
```
Minimal Canvas-based sparkline — a one-pass connected polyline whose y-axis is normalized to the series's own `[min, max]` range. No external chart dependency.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/AmountDisplay.kt`

```kotlin
fun AmountDisplay(
```
Big-and-bold currency presentation used at the top of dashboards and detail screens. Layout: optional label (small, dimmed) → large amount → optional supporting metadata row.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/MoneyText.kt`

```kotlin
fun MoneyText(
```
Currency text that picks its color from `MaterialTheme.finance` based on the amount's sign (or a forced `MoneyTone`). Use everywhere a monetary value is rendered so the app has a single visual grammar for money.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/MoneyTone.kt`

```kotlin
enum class MoneyTone
```
Money tone — how a monetary amount should be colored regardless of the raw value's sign.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/MoneyText.kt:36</code></summary>

```kotlin
    modifier: Modifier = Modifier,
    amount: Double = 0.0,
    tone: MoneyTone = MoneyTone.AutoFromSign,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    weight: FontWeight = FontWeight.SemiBold,
) {
    val color = resolveMoneyColor(tone, amount)
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/RateBadge.kt`

```kotlin
fun RateBadge(delta: String, direction: RateDirection, modifier: Modifier = Modifier)
```
Compact rate-change indicator — directional icon + percentage / delta text, both colored from `MaterialTheme.finance` (rateUp / rateDown / rateFlat). Renders inside a tinted container so it reads as a single visual unit.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/RateDirection.kt`

```kotlin
enum class RateDirection
```
Direction of a rate / price / metric change relative to the prior period.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/RateBadge.kt:44</code></summary>

```kotlin
 */
@Composable
fun RateBadge(delta: String, direction: RateDirection, modifier: Modifier = Modifier) {
    val (container, content, icon) = resolveRateColors(direction)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/SectionHeader.kt`

```kotlin
fun SectionHeader(
```
Section header — title (+ optional supporting text) + optional trailing action button. Use to break dashboards into scannable groups (Loans, Bills, Rates, Currencies).

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/StatusChip.kt`

```kotlin
fun StatusChip(text: String, intent: StatusChipIntent, modifier: Modifier = Modifier)
```
Compact colored pill used to convey state at a glance — bill status, loan stage, rate direction, sync state. Stays one line; no icons (use `UrgencyDot` when you want a leading accent).

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountsScreen.kt:491</code></summary>

```kotlin
    ) {
        activeStatus.forEach { statusEnum ->
            StatusChip(
                label = stringResource(statusEnum.labelRes),
                color = statusEnum.color,
            )
        }
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/StatusChipIntent.kt`

```kotlin
enum class StatusChipIntent
```
Semantic intent of a `StatusChip`. Maps to a (container, content) color pair derived from the active Material color scheme + finance palette.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/StatusChip.kt:33</code></summary>

```kotlin
 */
@Composable
fun StatusChip(text: String, intent: StatusChipIntent, modifier: Modifier = Modifier) {
    val (container, content) = resolveChipColors(intent)
    Text(
        text = text,
        modifier = modifier
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/Urgency.kt`

```kotlin
enum class Urgency
```
Due-date urgency tier — informs the color of a leading dot on a list row.

<details><summary>Used in the template — <code>core/designsystem/src/androidMain/kotlin/kpt/core/designsystem/theme/FinanceTokenPreview.kt:86</code></summary>

```kotlin
        SwatchRow("freshnessUpdating", f.freshnessUpdating)
        SwatchRow("freshnessOffline", f.freshnessOffline)
        SectionLabel("Urgency")
        SwatchRow("urgencyOverdue", f.urgencyOverdue)
        SwatchRow("urgencyToday", f.urgencyToday)
        SwatchRow("urgencyUpcoming", f.urgencyUpcoming)
        SwatchRow("urgencyDistant", f.urgencyDistant)
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/UrgencyDot.kt`

```kotlin
fun UrgencyDot(urgency: Urgency, modifier: Modifier = Modifier, size: Dp = 10.dp)
```
Solid colored dot used as the leading accent on a list row (bill reminder, loan due, task). Pairs cheaply with any list-item layout to encode urgency at a glance without stealing focus from the row's text content.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/CardLoadingSkeleton.kt`

```kotlin
fun CardLoadingSkeleton(modifier: Modifier = Modifier)
```
Whole-card shimmer placeholder. Drop in for `AppCard` / `Card` while the underlying data loads. Renders 3 stacked shimmer bars to suggest a typical card layout (title + 2 body lines).

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/CardStateBox.kt:46</code></summary>

```kotlin
) {
    when (state) {
        is ScreenState.Loading -> CardLoadingSkeleton(modifier = modifier)
        is ScreenState.Empty -> Card(modifier = modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center,
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/CardStateBox.kt`

```kotlin
fun <T> CardStateBox(
```
State-aware Card wrapper. Renders the right component-scale UI for each `ScreenState` variant *inside* the Card bounds — no full-screen overlay. Use for individual cards on a dashboard, where each card resolves its own data independently.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/ErrorChip.kt`

```kotlin
fun ErrorChip(message: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null)
```
M3 AssistChip styled for error reporting. Slightly more prominent than `InlineErrorPill` — adds an icon and uses chip semantics (clickable by default if `onClick` is non-null).

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/InlineErrorPill.kt`

```kotlin
fun InlineErrorPill(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null)
```
Pill-shaped inline error chip — for component-scale failures (a single row failing in an otherwise-loaded list, a stale field in a form, a card-local fetch failure).

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/CardStateBox.kt:64</code></summary>

```kotlin
                contentAlignment = Alignment.Center,
            ) {
                InlineErrorPill(message = state.error.message ?: "Something went wrong")
            }
        }
        is ScreenState.NoNetwork -> Card(modifier = modifier.fillMaxWidth()) {
            Box(
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/RowLoadingShimmer.kt`

```kotlin
fun RowLoadingShimmer(modifier: Modifier = Modifier)
```
Single-row shimmer placeholder. Use inside LazyColumn `items()` while paged data loads, or as a one-off row placeholder for a single record waiting on a network response. Layout: circle avatar + 2 stacked text bars (title + subtitle).

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/icon/AppIcons.kt`

```kotlin
object AppIcons
```
The app's icon set, named by ROLE rather than by glyph.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:557</code></summary>

```kotlin
        ) {
            Icon(
                imageVector = AppIcons.OutlinedLock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
```

</details>

- `val Language: ImageVector = Icons.Default.ArrowOutward` — `language` role — Material default `ArrowOutward`.
- `val CheckCircle: ImageVector = Icons.Filled.CheckCircle` — `check circle` role — Material filled `CheckCircle`.
- `val OutlinedInfo = Icons.Outlined.Info` — `outlined info` role — Material outlined `Info`.
- `val OutlinedLock = Icons.Outlined.Lock` — `outlined lock` role — Material outlined `Lock`.
- `val OutlinedNotifications = Icons.Outlined.Notifications` — `outlined notifications` role — Material outlined `Notifications`.
- `val ChevronRight: ImageVector = Icons.Filled.ChevronRight` — `chevron right` role — Material filled `ChevronRight`.
- `val QrCode: ImageVector = Icons.Filled.QrCode` — `qr code` role — Material filled `QrCode`.
- `val Close: ImageVector = Icons.Filled.Close` — `close` role — Material filled `Close`.
- `val AttachMoney: ImageVector = Icons.Filled.AttachMoney` — `attach money` role — Material filled `AttachMoney`.
- `val OutlinedVisibilityOff: ImageVector = Icons.Outlined.VisibilityOff` — `outlined visibility off` role — Material outlined `VisibilityOff`.
- `val OutlinedVisibility: ImageVector = Icons.Outlined.Visibility` — `outlined visibility` role — Material outlined `Visibility`.
- `val VisibilityOff: ImageVector = Icons.Filled.VisibilityOff` — `visibility off` role — Material filled `VisibilityOff`.
- `val Visibility: ImageVector = Icons.Filled.Visibility` — `visibility` role — Material filled `Visibility`.
- `val Check: ImageVector = Icons.Default.Check` — `check` role — Material default `Check`.
  _…more members; read the file._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Color.kt`

```kotlin
val primaryLight = Color(0xFF4338CA)
```
_No KDoc at source._

```kotlin
val onPrimaryLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val primaryContainerLight = Color(0xFFE0E7FF)
```
_No KDoc at source._

```kotlin
val onPrimaryContainerLight = Color(0xFF1F1D75)
```
_No KDoc at source._

```kotlin
val secondaryLight = Color(0xFF059669)
```
_No KDoc at source._

```kotlin
val onSecondaryLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val secondaryContainerLight = Color(0xFFD1FAE5)
```
_No KDoc at source._

```kotlin
val onSecondaryContainerLight = Color(0xFF064E3B)
```
_No KDoc at source._

```kotlin
val tertiaryLight = Color(0xFFD97706)
```
_No KDoc at source._

```kotlin
val onTertiaryLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val tertiaryContainerLight = Color(0xFFFEF3C7)
```
_No KDoc at source._

```kotlin
val onTertiaryContainerLight = Color(0xFF78350F)
```
_No KDoc at source._

```kotlin
val errorLight = Color(0xFFF87171)
```
_No KDoc at source._

```kotlin
val onErrorLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val errorContainerLight = Color(0xFFFFE4E1)
```
_No KDoc at source._

```kotlin
val onErrorContainerLight = Color(0xFF9F1239)
```
_No KDoc at source._

```kotlin
val backgroundLight = Color(0xFFFAFAFB)
```
_No KDoc at source._

```kotlin
val onBackgroundLight = Color(0xFF0F172A)
```
_No KDoc at source._

```kotlin
val surfaceLight = Color(0xFFFAFAFB)
```
_No KDoc at source._

```kotlin
val onSurfaceLight = Color(0xFF0F172A)
```
_No KDoc at source._

```kotlin
val surfaceVariantLight = Color(0xFFE2E8F0)
```
_No KDoc at source._

```kotlin
val onSurfaceVariantLight = Color(0xFF475569)
```
_No KDoc at source._

```kotlin
val outlineLight = Color(0xFF94A3B8)
```
_No KDoc at source._

```kotlin
val outlineVariantLight = Color(0xFFCBD5E1)
```
_No KDoc at source._

```kotlin
val scrimLight = Color(0xFF000000)
```
_No KDoc at source._

```kotlin
val inverseSurfaceLight = Color(0xFF1E293B)
```
_No KDoc at source._

```kotlin
val inverseOnSurfaceLight = Color(0xFFF1F5F9)
```
_No KDoc at source._

```kotlin
val inversePrimaryLight = Color(0xFFA5B4FC)
```
_No KDoc at source._

```kotlin
val surfaceDimLight = Color(0xFFE2E8F0)
```
_No KDoc at source._

```kotlin
val surfaceBrightLight = Color(0xFFFAFAFB)
```
_No KDoc at source._

```kotlin
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val surfaceContainerLowLight = Color(0xFFF8FAFC)
```
_No KDoc at source._

```kotlin
val surfaceContainerLight = Color(0xFFF1F5F9)
```
_No KDoc at source._

```kotlin
val surfaceContainerHighLight = Color(0xFFE2E8F0)
```
_No KDoc at source._

```kotlin
val surfaceContainerHighestLight = Color(0xFFCBD5E1)
```
_No KDoc at source._

```kotlin
val primaryDark = Color(0xFFA5B4FC)
```
_No KDoc at source._

```kotlin
val onPrimaryDark = Color(0xFF1F1D75)
```
_No KDoc at source._

```kotlin
val primaryContainerDark = Color(0xFF3730A3)
```
_No KDoc at source._

```kotlin
val onPrimaryContainerDark = Color(0xFFE0E7FF)
```
_No KDoc at source._

```kotlin
val secondaryDark = Color(0xFF6EE7B7)
```
_No KDoc at source._

```kotlin
val onSecondaryDark = Color(0xFF064E3B)
```
_No KDoc at source._

```kotlin
val secondaryContainerDark = Color(0xFF065F46)
```
_No KDoc at source._

```kotlin
val onSecondaryContainerDark = Color(0xFFD1FAE5)
```
_No KDoc at source._

```kotlin
val tertiaryDark = Color(0xFFFCD34D)
```
_No KDoc at source._

```kotlin
val onTertiaryDark = Color(0xFF78350F)
```
_No KDoc at source._

```kotlin
val tertiaryContainerDark = Color(0xFF92400E)
```
_No KDoc at source._

```kotlin
val onTertiaryContainerDark = Color(0xFFFEF3C7)
```
_No KDoc at source._

```kotlin
val errorDark = Color(0xFFFDBA74)
```
_No KDoc at source._

```kotlin
val onErrorDark = Color(0xFF7C2D12)
```
_No KDoc at source._

```kotlin
val errorContainerDark = Color(0xFFC2410C)
```
_No KDoc at source._

```kotlin
val onErrorContainerDark = Color(0xFFFFEDD5)
```
_No KDoc at source._

```kotlin
val backgroundDark = Color(0xFF0F172A)
```
_No KDoc at source._

```kotlin
val onBackgroundDark = Color(0xFFF1F5F9)
```
_No KDoc at source._

```kotlin
val surfaceDark = Color(0xFF0F172A)
```
_No KDoc at source._

```kotlin
val onSurfaceDark = Color(0xFFF1F5F9)
```
_No KDoc at source._

```kotlin
val surfaceVariantDark = Color(0xFF1E293B)
```
_No KDoc at source._

```kotlin
val onSurfaceVariantDark = Color(0xFFCBD5E1)
```
_No KDoc at source._

```kotlin
val outlineDark = Color(0xFF64748B)
```
_No KDoc at source._

```kotlin
val outlineVariantDark = Color(0xFF334155)
```
_No KDoc at source._

```kotlin
val scrimDark = Color(0xFF000000)
```
_No KDoc at source._

```kotlin
val inverseSurfaceDark = Color(0xFFF1F5F9)
```
_No KDoc at source._

```kotlin
val inverseOnSurfaceDark = Color(0xFF1E293B)
```
_No KDoc at source._

```kotlin
val inversePrimaryDark = Color(0xFF4338CA)
```
_No KDoc at source._

```kotlin
val surfaceDimDark = Color(0xFF0F172A)
```
_No KDoc at source._

```kotlin
val surfaceBrightDark = Color(0xFF374558)
```
_No KDoc at source._

```kotlin
val surfaceContainerLowestDark = Color(0xFF020617)
```
_No KDoc at source._

```kotlin
val surfaceContainerLowDark = Color(0xFF1E293B)
```
_No KDoc at source._

```kotlin
val surfaceContainerDark = Color(0xFF243044)
```
_No KDoc at source._

```kotlin
val surfaceContainerHighDark = Color(0xFF2D3B52)
```
_No KDoc at source._

```kotlin
val surfaceContainerHighestDark = Color(0xFF374558)
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Elevation.kt`

```kotlin
data class Elevation(
```
Shared elevation tier scale — five named tiers matching Material 3 elevation guidance. Access from any Composable via `MaterialTheme.elevation`.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayHistoryScreen.kt:170</code></summary>

```kotlin
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
```

</details>

```kotlin
val LocalElevation = staticCompositionLocalOf { Elevation() }
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:77</code></summary>

```kotlin
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
        ) {
            content()
        }
```

</details>

```kotlin
val MaterialTheme.elevation: Elevation
```
Resolve the active `Elevation` tier scale from composition.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/FinanceColors.kt`

```kotlin
data class FinanceColors(
```
Semantic finance color palette — extends Material 3's `androidx.compose.material3.ColorScheme` with money/rate/freshness/urgency tokens specific to financial UIs. Access from any Composable via `MaterialTheme.finance`.

<details><summary>Example</summary>

```kotlin
CompositionLocalProvider(LocalFinanceColors provides myForkFinanceColors()) {
    KptTheme { App() }
}
```

</details>

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:60</code></summary>

```kotlin

    val screenStateDefaults = appScreenStateDefaults()
    val financeColors = if (darkTheme) darkFinanceColors() else lightFinanceColors()

    KptMaterialTheme(theme = themeProvider) {
        // Provide the design-system token CompositionLocals app-wide so every widget
        // built on `core/designsystem/component/`, `chart/`, and `motion/` resolves
```

</details>

```kotlin
fun lightFinanceColors(): FinanceColors = FinanceColors(
```
Light-theme finance palette. Contrast-verified against `surfaceLight` / `surfaceContainerLight` etc.

```kotlin
fun darkFinanceColors(): FinanceColors = FinanceColors(
```
Dark-theme finance palette. Contrast-verified against `surfaceDark` / `surfaceContainerDark`. Mirrors light values but brightened for legibility on the deep navy `#13131B` background.

```kotlin
val LocalFinanceColors = staticCompositionLocalOf<FinanceColors>
```
CompositionLocal for the active `FinanceColors`. Provided by `KptTheme`. Direct access discouraged — use `MaterialTheme.finance` extension instead.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:74</code></summary>

```kotlin
        // core/store/KptScreenStateDefaults.kt — that's the single fork seam.
        CompositionLocalProvider(
            LocalFinanceColors provides financeColors,
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
```

</details>

```kotlin
val MaterialTheme.finance: FinanceColors
```
Resolve the active `FinanceColors` from composition.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/ForkTheme.kt`

```kotlin
val lightScheme = lightColorScheme(
```
The assembled Material 3 light scheme, wired from the palette tokens in `Color.kt`.

```kotlin
val darkScheme = darkColorScheme(
```
The assembled Material 3 dark scheme, wired from the palette tokens in `Color.kt`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt`

```kotlin
fun KptTheme(
```
The main theme composable for the application. This composable uses KptMaterialTheme under the hood to provide seamless integration between KptTheme design tokens and Material3 theming system.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountsScreen.kt:198</code></summary>

```kotlin
                        subTitle = stringResource(Res.string.feature_accounts_unexpected_error_subtitle),
                        modifier = Modifier,
                        iconTint = KptTheme.colorScheme.error,
                    )
                }

                is AccountState.ViewState.Content -> {
```

</details>

```kotlin
expect fun platformColorScheme(useDarkTheme: Boolean, dynamicColor: Boolean): ColorScheme
```
Resolves the active colour scheme per platform. `dynamicColor` is honoured only where the OS supplies one (Android 12+); every other target falls back to `lightScheme`/`darkScheme`, so a caller can request it unconditionally.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/MifosTheme.deprecated.kt`

```kotlin
fun MifosTheme(
```
Back-compat shim — forwards to `KptTheme`. Composables cannot be `typealias`'d, so this file holds a thin `@Composable fun` wrapper that re-exports the old `MifosTheme` name for one release while consumers migrate.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/login/LoginScreen.kt:288</code></summary>

```kotlin
@Composable
private fun LoginScreenPreview() {
    MifosTheme {
        LoginScreen(
            state = LoginState(dialogState = null),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Spacing.kt`

```kotlin
data class Spacing(
```
Shared spacing scale — replaces raw `.dp` literals scattered across features with a disciplined 4 / 8 / 12 / 16 / 24 / 32 / 48 progression. Access from any Composable via `MaterialTheme.spacing`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/savingsaccount/details/SavingAccountDetailScreen.kt:427</code></summary>

```kotlin
                    color = KptTheme.colorScheme.surface,
                    style = KptTheme.typography.headlineMedium,
                    letterSpacing = 0.50.sp,
                )
            }

            Row(
```

</details>

```kotlin
val LocalSpacing = staticCompositionLocalOf { Spacing() }
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:76</code></summary>

```kotlin
            LocalFinanceColors provides financeColors,
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
        ) {
            content()
```

</details>

```kotlin
val MaterialTheme.spacing: Spacing
```
Resolve the active `Spacing` scale from composition.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Type.kt`

```kotlin
val fontFamily: FontFamily
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/utils/ModifierExt.kt`

```kotlin
fun Modifier.mirrorIfRtl() = composed
```
Horizontally mirrors content when the layout direction is RTL. For glyphs with a direction (a back chevron, a progress arrow). NOT for text or logos, which must not be flipped.

```kotlin
fun Modifier.tabNavigation() = composed
```
Makes the element reachable and actionable by keyboard tab — desktop and web, where a tap target alone leaves it unusable.

```kotlin
fun Modifier.onClick(
```
Click handling with the app's ripple and accessibility semantics already attached, so a clickable element is announced correctly without each call site remembering to say so.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/utils/NonLetterColorVisualTransformation.kt`

```kotlin
fun nonLetterColorVisualTransformation(): VisualTransformation
```
Tints digits and symbols differently from letters in a text field — used for passwords and codes, where character class is hard to read at a glance. Presentation only: it never alters the field's value.

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/AlertDialog.kt`

```kotlin
fun MifosDialogBox(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/org/mifospay/feature/mpay/qr/MpayQrScreen.kt:149</code></summary>

```kotlin

        is MpayQrState.DialogState.AddToPocketConfirmation -> {
            MifosDialogBox(
                showDialogState = true,
                onDismiss = { onAction(MpayQrAction.ConfirmAddToPocket(false)) },
                title = stringResource(UiRes.string.core_ui_add_to_pocket_title),
                message = stringResource(UiRes.string.core_ui_add_to_pocket_message),
```

</details>

```kotlin
fun MifosCustomDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/org/mifospay/feature/mpay/qr/SetAmountDialog.kt:57</code></summary>

```kotlin
    onAction: (MpayQrAction) -> Unit,
) {
    MifosCustomDialog(
        onDismiss = {
            onAction(MpayQrAction.DismissDialog)
        },
        content = {
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/Background.kt`

```kotlin
fun MifosBackground(
```
The main background for the app. Uses `LocalBackgroundTheme` to set the color and tonal elevation of a `Surface`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/Button.kt:297</code></summary>

```kotlin
fun MifosButtonPreview() {
    MifosTheme {
        MifosBackground(modifier = Modifier.size(150.dp, 50.dp)) {
            MifosButton(text = { Text("Test button") }, onClick = {})
        }
    }
}
```

</details>

```kotlin
fun MifosGradientBackground(
```
A gradient background for select screens. Uses `LocalBackgroundTheme` to set the gradient colors of a `Box` within a `Surface`.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/socialSignup/SignupMethodScreen.kt:76</code></summary>

```kotlin
        },
    ) {
        MifosGradientBackground {
            SignupMethodScreenContent(
                modifier = Modifier.padding(it),
                onSignUpAsMerchant = {
                    navigateToSignupScreen(WALLET_ACCOUNT_SAVINGS_PRODUCT_ID)
```

</details>

```kotlin
fun BackgroundDefault()
```
_No KDoc at source._

```kotlin
fun BackgroundDynamic()
```
_No KDoc at source._

```kotlin
fun BackgroundAndroid()
```
_No KDoc at source._

```kotlin
fun GradientBackgroundDefault()
```
_No KDoc at source._

```kotlin
fun GradientBackgroundDynamic()
```
_No KDoc at source._

```kotlin
fun GradientBackgroundAndroid()
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/BottomSheet.kt`

```kotlin
fun MifosBottomSheet(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/org/mifospay/feature/beneficiary/deletebeneficiary/DeleteBeneficiaryBottomSheet.kt:63</code></summary>

```kotlin
    when (dialogState) {
        is DeleteBeneficiaryState.DialogState.Confirmation -> {
            MifosBottomSheet(
                onDismiss = onDismiss,
            ) {
                DeleteConfirmationContent(
                    beneficiaryName = dialogState.beneficiaryName,
```

</details>

```kotlin
fun MifosBottomSheetPreview()
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/Button.kt`

```kotlin
fun MifosButton(
```
Mifos Wallet filled button with generic content slot. Wraps Material 3 `Button`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/savingsaccount/AddEditSavingAccountScreen.kt:518</code></summary>

```kotlin

        item("CreateOrUpdateSavingAccount") {
            MifosButton(
                text = {
                    Text(
                        text = btnText,
                    )
```

</details>

```kotlin
fun MifosButton(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/savingsaccount/AddEditSavingAccountScreen.kt:518</code></summary>

```kotlin

        item("CreateOrUpdateSavingAccount") {
            MifosButton(
                text = {
                    Text(
                        text = btnText,
                    )
```

</details>

```kotlin
fun MifosButton(
```
Mifos Wallet filled button with text and icon content slots.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/savingsaccount/AddEditSavingAccountScreen.kt:518</code></summary>

```kotlin

        item("CreateOrUpdateSavingAccount") {
            MifosButton(
                text = {
                    Text(
                        text = btnText,
                    )
```

</details>

```kotlin
fun MifosOutlinedButton(
```
Mifos Wallet outlined button with generic content slot. Wraps Material 3 `OutlinedButton`.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/socialSignup/SignupMethodScreen.kt:106</code></summary>

```kotlin
        )

        MifosOutlinedButton(
            modifier = Modifier.padding(top = KptTheme.spacing.xl),
            onClick = onSignUpAsMerchant,
            border = BorderStroke(
                1.dp,
```

</details>

```kotlin
fun MifosOutlinedButton(
```
Mifos Wallet outlined button with text and icon content slots.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/socialSignup/SignupMethodScreen.kt:106</code></summary>

```kotlin
        )

        MifosOutlinedButton(
            modifier = Modifier.padding(top = KptTheme.spacing.xl),
            onClick = onSignUpAsMerchant,
            border = BorderStroke(
                1.dp,
```

</details>

```kotlin
fun MifosTextButton(
```
Mifos Wallet text button with generic content slot. Wraps Material 3 `TextButton`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/MifosBasicDialog.kt:33</code></summary>

```kotlin
            onDismissRequest = onDismissRequest,
            confirmButton = {
                MifosTextButton(
                    content = {
                        Text(text = "Ok")
                    },
                    onClick = onDismissRequest,
```

</details>

```kotlin
fun MifosTextButton(
```
Mifos Wallet text button with text and icon content slots.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/MifosBasicDialog.kt:33</code></summary>

```kotlin
            onDismissRequest = onDismissRequest,
            confirmButton = {
                MifosTextButton(
                    content = {
                        Text(text = "Ok")
                    },
                    onClick = onDismissRequest,
```

</details>

```kotlin
fun MifosButtonPreview()
```
_No KDoc at source._

```kotlin
fun MifosOutlinedButtonPreview()
```
_No KDoc at source._

```kotlin
fun MifosButtonLeadingIconPreview()
```
_No KDoc at source._

```kotlin
object MifosButtonDefaults
```
Mifos Wallet button default values.

- `const val DISABLED_OUTLINED_BUTTON_BORDER_ALPHA = 0.12f`
- `val OutlinedButtonBorderWidth = 1.dp`

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/Card.kt`

```kotlin
fun MifosCard(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/org/mifospay/feature/merchants/ui/MerchantsItem.kt:44</code></summary>

```kotlin
    modifier: Modifier = Modifier,
) {
    MifosCard(
        modifier = modifier.combinedClickable(
            onClick = onMerchantClicked,
            onLongClick = {
                onMerchantLongPressed(savingsWithAssociations.id.toString())
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/IconBox.kt`

```kotlin
fun IconBox(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/MifosTopBar.kt:39</code></summary>

```kotlin
        },
        navigationIcon = {
            IconBox(
                icon = MifosIcons.ArrowBack2,
                onClick = backPress,
            )
        },
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/LoadingWheel.kt`

```kotlin
fun MifosLoadingWheel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/org/mifospay/feature/send/money/PaymentProcessingScreen.kt:92</code></summary>

```kotlin
    ) {
        if (state.isProcessing) {
            MifosLoadingWheel(
                contentDesc = "Processing Payment",
                modifier = Modifier.size(120.dp),
            )
        } else {
```

</details>

```kotlin
fun MifosOverlayLoadingWheel(
```
_No KDoc at source._

```kotlin
fun MfOverlayLoadingWheel(
```
_No KDoc at source._

```kotlin
fun MfLoadingWheel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/org/mifospay/feature/merchants/ui/MerchantScreen.kt:122</code></summary>

```kotlin

                MerchantUiState.Loading -> {
                    MfLoadingWheel(
                        contentDesc = stringResource(Res.string.feature_merchants_loading),
                        backgroundColor = KptTheme.colorScheme.surface,
                    )
                }
```

</details>

```kotlin
fun MifosLoadingWheelPreview()
```
_No KDoc at source._

```kotlin
fun NiaOverlayLoadingWheelPreview()
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/MifosAccountCard.kt`

```kotlin
fun MifosAccountCard(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/pocket/src/commonMain/kotlin/org/mifospay/feature/pocket/screens/PocketDashboardScreen.kt:203</code></summary>

```kotlin
            Column(verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md)) {
                buckets.savingsAccounts.forEach { account ->
                    MifosAccountCard(
                        accountId = account.accountId,
                        accountType = account.name
                            ?: stringResource(Res.string.feature_pocket_unknown_account),
                        accountNumber = account.accountNumber,
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/MifosAlertDialog.kt`

```kotlin
fun MifosAlertDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/pocket/src/commonMain/kotlin/org/mifospay/feature/pocket/screens/ManagePocketScreen.kt:324</code></summary>

```kotlin

        is ManagePocketDialogState.Error -> {
            MifosAlertDialog(
                title = stringResource(Res.string.feature_pocket_dialog_error_title),
                message = stringResource(dialogState.message),
                icon = MifosIcons.Error,
                confirmText = stringResource(Res.string.feature_pocket_action_ok),
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/MifosBasicDialog.kt`

```kotlin
fun MifosBasicDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountsScreen.kt:454</code></summary>

```kotlin
) {
    when (dialogState) {
        is AccountState.DialogState.DeleteBeneficiary -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = stringResource(dialogState.title),
                message = stringResource(dialogState.message),
            ),
```

</details>

```kotlin
fun MifosBasicDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountsScreen.kt:454</code></summary>

```kotlin
) {
    when (dialogState) {
        is AccountState.DialogState.DeleteBeneficiary -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = stringResource(dialogState.title),
                message = stringResource(dialogState.message),
            ),
```

</details>

```kotlin
sealed class BasicDialogState
```
Models display of a `MifosBasicDialog`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountsScreen.kt:455</code></summary>

```kotlin
    when (dialogState) {
        is AccountState.DialogState.DeleteBeneficiary -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = stringResource(dialogState.title),
                message = stringResource(dialogState.message),
            ),
            onConfirm = dialogState.onConfirm,
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/MifosLoadingDialog.kt`

```kotlin
fun MifosLoadingDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountsScreen.kt:470</code></summary>

```kotlin
        )

        is AccountState.DialogState.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
```

</details>

```kotlin
sealed class LoadingDialogState
```
Models display of a `MifosLoadingDialog`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountsScreen.kt:471</code></summary>

```kotlin

        is AccountState.DialogState.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/MifosScaffold.kt`

```kotlin
fun MifosScaffold(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountsScreen.kt:161</code></summary>

```kotlin
    modifier: Modifier = Modifier,
) {
    MifosScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
```

</details>

```kotlin
fun MifosScaffold(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountsScreen.kt:161</code></summary>

```kotlin
    modifier: Modifier = Modifier,
) {
    MifosScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
```

</details>

```kotlin
fun MifosBottomSheetScaffold(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/org/mifospay/feature/transfer/intrabank/confirm/TransferConfirmScreen.kt:288</code></summary>

```kotlin
    onAction: (TransferConfirmAction) -> Unit,
) {
    MifosBottomSheetScaffold(
        topBar = {
            MifosTopBar(
                topBarTitle = stringResource(Res.string.feature_make_transfer_review_title),
                backPress = {
```

</details>

```kotlin
data class FloatingActionButtonContent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptForkScaffold.kt:56</code></summary>

```kotlin
    title: String? = null,
    containerColor: Color = KptTheme.colorScheme.background,
    floatingActionButtonContent: FloatingActionButtonContent? = null,
    pullToRefreshState: KptPullToRefreshState = rememberKptPullToRefreshState(),
    contentWindowInsets: WindowInsets = ScaffoldDefaults
        .contentWindowInsets
//        .union(WindowInsets.displayCutout)
```

</details>

```kotlin
data class MifosPullToRefreshState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScheduleManagementScreen.kt:72</code></summary>

```kotlin
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val pullRefreshState = rememberMifosPullToRefreshState(
        isEnabled = true,
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.trySendAction(AutoPayScheduleManagementAction.RefreshSchedules) },
    )
```

</details>

```kotlin
fun rememberMifosPullToRefreshState(
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/MifosTab.kt`

```kotlin
fun MifosTab(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/org/mifospay/core/ui/ScrollableTabRow.kt:47</code></summary>

```kotlin
    ) {
        tabContents.forEachIndexed { index, currentTab ->
            MifosTab(
                text = currentTab.tabName,
                selected = pagerState.currentPage == index,
                selectedColor = selectedContentColor,
                unselectedColor = unselectedContentColor,
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/MifosTabPager.kt`

```kotlin
fun MifosTabPager(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/pocket/src/commonMain/kotlin/org/mifospay/feature/pocket/screens/ManagePocketScreen.kt:538</code></summary>

```kotlin
                .weight(1f),
        ) {
            MifosTabPager(
                modifier = Modifier.fillMaxSize(),
                pagerState = pagerState,
                currentPage = selectedTabIndex,
                tabs = tabs.map { it.name },
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/MifosTopAppBar.kt`

```kotlin
fun MifosTopAppBar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/mobileVerify/MobileVerificationScreen.kt:112</code></summary>

```kotlin
        snackbarHostState = snackbarHostState,
        topBar = {
            MifosTopAppBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = NavigationIcon(
                    navigationIcon = MifosIcons.Back,
```

</details>

```kotlin
fun MifosTopAppBar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/mobileVerify/MobileVerificationScreen.kt:112</code></summary>

```kotlin
        snackbarHostState = snackbarHostState,
        topBar = {
            MifosTopAppBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = NavigationIcon(
                    navigationIcon = MifosIcons.Back,
```

</details>

```kotlin
fun MifosTopAppBar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/mobileVerify/MobileVerificationScreen.kt:112</code></summary>

```kotlin
        snackbarHostState = snackbarHostState,
        topBar = {
            MifosTopAppBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = NavigationIcon(
                    navigationIcon = MifosIcons.Back,
```

</details>

```kotlin
data class NavigationIcon(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/mobileVerify/MobileVerificationScreen.kt:115</code></summary>

```kotlin
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = NavigationIcon(
                    navigationIcon = MifosIcons.Back,
                    navigationIconContentDescription = "Go Back",
                    onNavigationIconClick = {
                        onEvent(MobileVerificationAction.CloseButtonClick)
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/MifosTopBar.kt`

```kotlin
fun MifosTopBar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AddBillScreen.kt:101</code></summary>

```kotlin
    MifosScaffold(
        topBar = {
            MifosTopBar(
                topBarTitle = "Add New Bill",
                backPress = onNavigateBack,
            )
        },
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/Navigation.kt`

```kotlin
fun RowScope.MifosNavigationBarItem(
```
Now in Android navigation bar item with icon and label content slots. Wraps Material 3 `NavigationBarItem`.

```kotlin
fun MifosNavigationBar(
```
Now in Android navigation bar with content slot. Wraps Material 3 `NavigationBar`.

```kotlin
fun MifosNavigationRailItem(
```
Now in Android navigation rail item with icon and label content slots. Wraps Material 3 `NavigationRailItem`.

```kotlin
fun MifosNavigationRail(
```
Now in Android navigation rail with header and content slots. Wraps Material 3 `NavigationRail`.

```kotlin
fun MifosNavigationBarPreview()
```
_No KDoc at source._

```kotlin
fun MifosNavigationRailPreview()
```
_No KDoc at source._

```kotlin
object MifosNavigationDefaults
```
Now in Android navigation default values.

- `fun navigationContentColor() = KptTheme.colorScheme.onSurfaceVariant`
- `fun navigationSelectedItemColor() = KptTheme.colorScheme.onPrimaryContainer`
- `fun navigationIndicatorColor() = KptTheme.colorScheme.primaryContainer`

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/TextField.kt`

```kotlin
fun MifosOutlinedTextField(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/login/LoginScreen.kt:182</code></summary>

```kotlin
        )
        Spacer(modifier = Modifier.padding(top = KptTheme.spacing.xl))
        MifosOutlinedTextField(
            label = stringResource(Res.string.feature_auth_username),
            value = state.username,
            onValueChange = {
                onAction(LoginAction.UsernameChanged(it))
```

</details>

```kotlin
fun MifosTextField(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/savingsaccount/AddEditSavingAccountScreen.kt:229</code></summary>

```kotlin
    ) {
        item("Client Name") {
            MifosTextField(
                value = state.template.clientName,
                label = stringResource(Res.string.feature_accounts_add_client_name),
                onValueChange = {},
                readOnly = true,
```

</details>

```kotlin
fun MifosCustomTextField(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/org/mifospay/core/ui/MifosPasswordField.kt:63</code></summary>

```kotlin
    val focusRequester = remember { FocusRequester() }

    MifosCustomTextField(
        modifier = modifier
            .tabNavigation()
            .focusRequester(focusRequester),
        label = label,
```

</details>

```kotlin
fun MifosCustomTextField(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/org/mifospay/core/ui/MifosPasswordField.kt:63</code></summary>

```kotlin
    val focusRequester = remember { FocusRequester() }

    MifosCustomTextField(
        modifier = modifier
            .tabNavigation()
            .focusRequester(focusRequester),
        label = label,
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/TextUserImage.kt`

```kotlin
fun MifosTextUserImage(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/org/mifospay/feature/transfer/intrabank/hub/components/BeneficiaryCard.kt:57</code></summary>

```kotlin
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MifosTextUserImage(
                text = getInitials(beneficiary.name),
                size = 44.dp,
            )
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/TopAppBar.kt`

```kotlin
fun MifosTopAppBar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/mobileVerify/MobileVerificationScreen.kt:112</code></summary>

```kotlin
        snackbarHostState = snackbarHostState,
        topBar = {
            MifosTopAppBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = NavigationIcon(
                    navigationIcon = MifosIcons.Back,
```

</details>

```kotlin
fun MifosNavigationTopAppBar(
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/scrollbar/AppScrollbars.kt`

```kotlin
fun ScrollableState.DraggableScrollbar(
```
A `Scrollbar` that allows for fast scrolling of content by dragging its thumb. Its thumb disappears when the scrolling container is dormant.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/org/mifospay/feature/home/HomeScreen.kt:364</code></summary>

```kotlin

        if (showScrollBar) {
            state.DraggableScrollbar(
                modifier = Modifier
                    .fillMaxHeight()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = KptTheme.spacing.xs)
```

</details>

```kotlin
fun ScrollableState.DecorativeScrollbar(
```
A simple `Scrollbar`. Its thumb disappears when the scrolling container is dormant.

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/scrollbar/Scrollbar.kt`

```kotlin
class ScrollbarState
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/scrollbar/AppScrollbars.kt:70</code></summary>

```kotlin
@Composable
fun ScrollableState.DraggableScrollbar(
    state: ScrollbarState,
    orientation: Orientation,
    onThumbMoved: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
```

</details>

```kotlin
value class ScrollbarStateValue internal constructor(
```
Class definition for the core properties of a scroll bar

```kotlin
fun scrollbarStateValue(
```
Creates a `ScrollbarStateValue` with the listed properties

```kotlin
fun Scrollbar(
```
A Composable for drawing a scrollbar

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/scrollbar/AppScrollbars.kt:76</code></summary>

```kotlin
) {
    val interactionSource = remember { MutableInteractionSource() }
    Scrollbar(
        modifier = modifier,
        orientation = orientation,
        interactionSource = interactionSource,
        state = state,
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/scrollbar/ScrollbarExt.kt`

```kotlin
fun LazyListState.scrollbarState(
```
Calculates a `ScrollbarState` driven by the changes in a `LazyListState`.

```kotlin
fun LazyGridState.scrollbarState(
```
Calculates a `ScrollbarState` driven by the changes in a `LazyGridState`

```kotlin
fun LazyStaggeredGridState.scrollbarState(
```
Remembers a `ScrollbarState` driven by the changes in a `LazyStaggeredGridState`

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/scrollbar/ThumbExt.kt`

```kotlin
fun LazyListState.rememberDraggableScroller(
```
Remembers a function to react to `Scrollbar` thumb position displacements for a `LazyListState`

```kotlin
fun LazyGridState.rememberDraggableScroller(
```
Remembers a function to react to `Scrollbar` thumb position displacements for a `LazyGridState`

```kotlin
fun LazyStaggeredGridState.rememberDraggableScroller(
```
Remembers a function to react to `Scrollbar` thumb position displacements for a `LazyStaggeredGridState`

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/icon/MifosIcons.kt`

```kotlin
object MifosIcons
```
Mifos icons. Material icons are `ImageVector`s, custom icons are drawable resource IDs.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountsScreen.kt:177</code></summary>

```kotlin
                ) {
                    Icon(
                        imageVector = MifosIcons.Add,
                        stringResource(Res.string.feature_accounts_add),
                    )
                }
            }
```

</details>

- `val Fingerprint = Icons.Default.Fingerprint`
- `val OutlinedInfo = Icons.Outlined.Info`
- `val OutlinedLock = Icons.Outlined.Lock`
- `val OutlinedNotifications = Icons.Outlined.Notifications`
- `val ChevronRight: ImageVector = Icons.Filled.ChevronRight`
- `val QrCode: ImageVector = Icons.Filled.QrCode`
- `val Close: ImageVector = Icons.Filled.Close`
- `val Error: ImageVector = Icons.Filled.Error`
- `val AttachMoney: ImageVector = Icons.Filled.AttachMoney`
- `val OutlinedVisibilityOff: ImageVector = Icons.Outlined.VisibilityOff`
- `val OutlinedVisibility: ImageVector = Icons.Outlined.Visibility`
- `val VisibilityOff: ImageVector = Icons.Filled.VisibilityOff`
- `val Visibility: ImageVector = Icons.Filled.Visibility`
- `val Check: ImageVector = Icons.Default.Check`
  _…more members; read the file._

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/theme/BackgroundTheme.kt`

```kotlin
data class BackgroundTheme(
```
A class to model background color and tonal elevation values for Now in Android.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/theme/Theme.kt:52</code></summary>

```kotlin
    }
    // Background theme
    val defaultBackgroundTheme = BackgroundTheme(
        color = Color.Transparent,
        tonalElevation = 2.dp,
    )
    val backgroundTheme = when {
```

</details>

```kotlin
val LocalBackgroundTheme = staticCompositionLocalOf { BackgroundTheme() }
```
A composition local for `BackgroundTheme`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/Background.kt:45</code></summary>

```kotlin
    content: @Composable () -> Unit,
) {
    val color = LocalBackgroundTheme.current.color
    val tonalElevation = LocalBackgroundTheme.current.tonalElevation
    Surface(
        color = if (color == Color.Unspecified) Color.Transparent else color,
        tonalElevation = if (tonalElevation == Dp.Unspecified) 0.dp else tonalElevation,
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/theme/Color.kt`

```kotlin
val primaryLight = Color(0xFF0673BA)
```
_No KDoc at source._

```kotlin
val onPrimaryLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val primaryContainerLight = Color(0xFFCEEAFD)
```
_No KDoc at source._

```kotlin
val onPrimaryContainerLight = Color(0xFF033D63)
```
_No KDoc at source._

```kotlin
val secondaryLight = Color(0xFF984061)
```
_No KDoc at source._

```kotlin
val onSecondaryLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val secondaryContainerLight = Color(0xFFF0DBE3)
```
_No KDoc at source._

```kotlin
val onSecondaryContainerLight = Color(0xFF361722)
```
_No KDoc at source._

```kotlin
val tertiaryLight = Color(0xFF7D4996)
```
_No KDoc at source._

```kotlin
val onTertiaryLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val tertiaryContainerLight = Color(0XFFE9DDEE)
```
_No KDoc at source._

```kotlin
val onTertiaryContainerLight = Color(0xFF2B1934)
```
_No KDoc at source._

```kotlin
val errorLight = Color(0xFFBA1A1A)
```
_No KDoc at source._

```kotlin
val onErrorLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val errorContainerLight = Color(0xFFFFDAD6)
```
_No KDoc at source._

```kotlin
val onErrorContainerLight = Color(0xFF93000A)
```
_No KDoc at source._

```kotlin
val backgroundLight = Color(0xFFF8F9FF)
```
_No KDoc at source._

```kotlin
val onBackgroundLight = Color(0xFF181C20)
```
_No KDoc at source._

```kotlin
val surfaceLight = Color(0xFFF8F9FF)
```
_No KDoc at source._

```kotlin
val onSurfaceLight = Color(0xFF181C20)
```
_No KDoc at source._

```kotlin
val surfaceVariantLight = Color(0xFFDCE3EF)
```
_No KDoc at source._

```kotlin
val onSurfaceVariantLight = Color(0xFF404751)
```
_No KDoc at source._

```kotlin
val outlineLight = Color(0xFF717882)
```
_No KDoc at source._

```kotlin
val outlineVariantLight = Color(0xFFC0C7D2)
```
_No KDoc at source._

```kotlin
val scrimLight = Color(0xFF000000)
```
_No KDoc at source._

```kotlin
val inverseSurfaceLight = Color(0xFF2D3136)
```
_No KDoc at source._

```kotlin
val inverseOnSurfaceLight = Color(0xFFEEF1F7)
```
_No KDoc at source._

```kotlin
val inversePrimaryLight = Color(0xFF9BCAFF)
```
_No KDoc at source._

```kotlin
val surfaceDimLight = Color(0xFFD8DAE0)
```
_No KDoc at source._

```kotlin
val surfaceBrightLight = Color(0xFFF8F9FF)
```
_No KDoc at source._

```kotlin
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val surfaceContainerLowLight = Color(0xFFF1F3FA)
```
_No KDoc at source._

```kotlin
val surfaceContainerLight = Color(0xFFECEEF4)
```
_No KDoc at source._

```kotlin
val surfaceContainerHighLight = Color(0xFFE6E8EE)
```
_No KDoc at source._

```kotlin
val surfaceContainerHighestLight = Color(0xFFE0E2E9)
```
_No KDoc at source._

```kotlin
val primaryDark = Color(0xFF9CD6FC)
```
_No KDoc at source._

```kotlin
val onPrimaryDark = Color(0xFF033D63)
```
_No KDoc at source._

```kotlin
val primaryContainerDark = Color(0xFF044C7C)
```
_No KDoc at source._

```kotlin
val onPrimaryContainerDark = Color(0xFFE6F5FE)
```
_No KDoc at source._

```kotlin
val secondaryDark = Color(0xFFD9A5B8)
```
_No KDoc at source._

```kotlin
val onSecondaryDark = Color(0xFF361722)
```
_No KDoc at source._

```kotlin
val secondaryContainerDark = Color(0xFF5A2639)
```
_No KDoc at source._

```kotlin
val onSecondaryContainerDark = Color(0xFFF0DBE3)
```
_No KDoc at source._

```kotlin
val tertiaryDark = Color(0xFFC7A9D6)
```
_No KDoc at source._

```kotlin
val onTertiaryDark = Color(0xFF3A2145)
```
_No KDoc at source._

```kotlin
val tertiaryContainerDark = Color(0xFF653A78)
```
_No KDoc at source._

```kotlin
val onTertiaryContainerDark = Color(0xFFF4EEF7)
```
_No KDoc at source._

```kotlin
val errorDark = Color(0xFFFFB4AB)
```
_No KDoc at source._

```kotlin
val onErrorDark = Color(0xFF690005)
```
_No KDoc at source._

```kotlin
val errorContainerDark = Color(0xFF93000A)
```
_No KDoc at source._

```kotlin
val onErrorContainerDark = Color(0xFFFFDAD6)
```
_No KDoc at source._

```kotlin
val backgroundDark = Color(0xFF101418)
```
_No KDoc at source._

```kotlin
val onBackgroundDark = Color(0xFFE0E2E9)
```
_No KDoc at source._

```kotlin
val surfaceDark = Color(0xFF101418)
```
_No KDoc at source._

```kotlin
val onSurfaceDark = Color(0xFFE0E2E9)
```
_No KDoc at source._

```kotlin
val surfaceVariantDark = Color(0xFF404751)
```
_No KDoc at source._

```kotlin
val onSurfaceVariantDark = Color(0xFFC0C7D2)
```
_No KDoc at source._

```kotlin
val outlineDark = Color(0xFF8A919C)
```
_No KDoc at source._

```kotlin
val outlineVariantDark = Color(0xFF404751)
```
_No KDoc at source._

```kotlin
val scrimDark = Color(0xFF000000)
```
_No KDoc at source._

```kotlin
val inverseSurfaceDark = Color(0xFFE0E2E9)
```
_No KDoc at source._

```kotlin
val inverseOnSurfaceDark = Color(0xFF2D3136)
```
_No KDoc at source._

```kotlin
val inversePrimaryDark = Color(0xFF0062A0)
```
_No KDoc at source._

```kotlin
val surfaceDimDark = Color(0xFF101418)
```
_No KDoc at source._

```kotlin
val surfaceBrightDark = Color(0xFF36393E)
```
_No KDoc at source._

```kotlin
val surfaceContainerLowestDark = Color(0xFF0B0E13)
```
_No KDoc at source._

```kotlin
val surfaceContainerLowDark = Color(0xFF181C20)
```
_No KDoc at source._

```kotlin
val surfaceContainerDark = Color(0xFF1C2025)
```
_No KDoc at source._

```kotlin
val surfaceContainerHighDark = Color(0xFF272A2F)
```
_No KDoc at source._

```kotlin
val surfaceContainerHighestDark = Color(0xFF32353A)
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/theme/ColorScheme.kt`

```kotlin
val lightKptColorScheme = LightDefaultColorScheme.toKptColorScheme()
```
_No KDoc at source._

```kotlin
val darkKptColorScheme = DarkDefaultColorScheme.toKptColorScheme()
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/theme/GradientColors.kt`

```kotlin
data class GradientColors(
```
A class to model gradient color values for Now in Android.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/Background.kt:69</code></summary>

```kotlin
fun MifosGradientBackground(
    modifier: Modifier = Modifier,
    gradientColors: GradientColors = LocalGradientColors.current,
    content: @Composable () -> Unit,
) {
    val currentTopColor by rememberUpdatedState(gradientColors.top)
    val currentBottomColor by rememberUpdatedState(gradientColors.bottom)
```

</details>

```kotlin
val LocalGradientColors = staticCompositionLocalOf { GradientColors() }
```
A composition local for `GradientColors`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/component/Background.kt:69</code></summary>

```kotlin
fun MifosGradientBackground(
    modifier: Modifier = Modifier,
    gradientColors: GradientColors = LocalGradientColors.current,
    content: @Composable () -> Unit,
) {
    val currentTopColor by rememberUpdatedState(gradientColors.top)
    val currentBottomColor by rememberUpdatedState(gradientColors.bottom)
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/theme/MifosTextStyle.kt`

```kotlin
val styleMedium16sp =
```
_No KDoc at source._

```kotlin
val styleNormal18sp =
```
_No KDoc at source._

```kotlin
val styleMedium30sp =
```
_No KDoc at source._

```kotlin
val styleMifosTopBar =
```
_No KDoc at source._

```kotlin
val styleSettingsButton = TextStyle(textAlign = TextAlign.Center)
```
_No KDoc at source._

```kotlin
val historyItemTextStyle = TextStyle(fontSize = 16.sp)
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/theme/Theme.kt`

```kotlin
fun MifosTheme(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/login/LoginScreen.kt:288</code></summary>

```kotlin
@Composable
private fun LoginScreenPreview() {
    MifosTheme {
        LoginScreen(
            state = LoginState(dialogState = null),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/theme/ThemeExtensions.kt`

```kotlin
fun KptShapes.toRoundedCornerShape(
```
Converts the given corner values into a `RoundedCornerShape`.

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/theme/TintTheme.kt`

```kotlin
data class TintTheme(
```
A class to model background color and tonal elevation values for Now in Android.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/theme/Theme.kt:60</code></summary>

```kotlin
    }
    val tintTheme = when {
        else -> TintTheme()
    }

    // Composition locals
    CompositionLocalProvider(
```

</details>

```kotlin
val LocalTintTheme = staticCompositionLocalOf { TintTheme() }
```
A composition local for `TintTheme`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/theme/Theme.kt:67</code></summary>

```kotlin
        LocalGradientColors provides gradientColors,
        LocalBackgroundTheme provides backgroundTheme,
        LocalTintTheme provides tintTheme,
    ) {
        KptMaterialTheme(
            theme = theme,
            content = content,
```

</details>

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/utils/ExpirationDateMask.kt`

```kotlin
class ExpirationDateMask : VisualTransformation
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/org/mifospay/core/designsystem/utils/ModifierExt.kt`

```kotlin
fun Modifier.mirrorIfRtl(): Modifier =
```
_No KDoc at source._

```kotlin
fun Modifier.tabNavigation(): Modifier
```
_No KDoc at source._

```kotlin
fun Modifier.onClick(
```
_No KDoc at source._

---

_26 type(s), 298 function(s)/property(ies); 98 carry KDoc at source; 1 authored example(s); 70 live call site(s)._
<!-- api-docs:end -->
