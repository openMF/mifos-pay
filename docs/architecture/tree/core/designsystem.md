# `core/designsystem`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DESIGNSYSTEM.md`
> **Measured:** 77 Kotlin files, 4 test files

## Principal types

`AppIcons`, `BackgroundTheme`, `BarDatum`, `BasicDialogState`, `Candle`, `ChartTokens`, `DonutSlice`, `Elevation`, `ExpirationDateMask`, `FinanceColors`, `FloatingActionButtonContent`, `ForkButtonDefaults`, `ForkIcons`, `ForkNavigationDefaults`  …and 18 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/designsystem sha=b6d1e61e782f8e6ca79f19fb0353bd127c0d8893 -->
## API reference

_Generated from `core/designsystem` at tree `b6d1e61e782f` by `scripts/docs/api-docs-gen.sh`._
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

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:491</code></summary>

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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/AlertDialog.kt`

```kotlin
fun ForkDialogBox(
```
Confirmation alert dialog with a title, optional text and confirm/dismiss buttons, shown while `showDialogState` is true.

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrScreen.kt:149</code></summary>

```kotlin

        is MpayQrState.DialogState.AddToPocketConfirmation -> {
            ForkDialogBox(
                showDialogState = true,
                onDismiss = { onAction(MpayQrAction.ConfirmAddToPocket(false)) },
                title = stringResource(UiRes.string.core_ui_add_to_pocket_title),
                message = stringResource(UiRes.string.core_ui_add_to_pocket_message),
```

</details>

```kotlin
fun ForkCustomDialog(
```
Basic dialog that hosts arbitrary `content`, dismissed through `onDismiss`.

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/SetAmountDialog.kt:56</code></summary>

```kotlin
    onAction: (MpayQrAction) -> Unit,
) {
    ForkCustomDialog(
        onDismiss = {
            onAction(MpayQrAction.DismissDialog)
        },
        content = {
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/Background.kt`

```kotlin
fun ForkBackground(
```
The main background for the app. Uses `LocalBackgroundTheme` to set the color and tonal elevation of a `Surface`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/Button.kt:298</code></summary>

```kotlin
fun ForkButtonPreview() {
    ForkTheme {
        ForkBackground(modifier = Modifier.size(150.dp, 50.dp)) {
            ForkButton(text = { Text("Test button") }, onClick = {})
        }
    }
}
```

</details>

```kotlin
fun ForkGradientBackground(
```
A gradient background for select screens. Uses `LocalBackgroundTheme` to set the gradient colors of a `Box` within a `Surface`.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/socialSignup/SignupMethodScreen.kt:75</code></summary>

```kotlin
        },
    ) {
        ForkGradientBackground {
            SignupMethodScreenContent(
                modifier = Modifier.padding(it),
                onSignUpAsMerchant = {
                    navigateToSignupScreen(WALLET_ACCOUNT_SAVINGS_PRODUCT_ID)
```

</details>

```kotlin
fun BackgroundDefault()
```
Preview of ForkBackground with the default theme.

```kotlin
fun BackgroundDynamic()
```
Preview of ForkBackground with a dynamic theme.

```kotlin
fun BackgroundAndroid()
```
Preview of ForkBackground with the Android theme.

```kotlin
fun GradientBackgroundDefault()
```
Preview of ForkGradientBackground with the default theme.

```kotlin
fun GradientBackgroundDynamic()
```
Preview of ForkGradientBackground with the dynamic theme.

```kotlin
fun GradientBackgroundAndroid()
```
Preview of ForkGradientBackground with the Android theme.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/BottomSheet.kt`

```kotlin
fun ForkBottomSheet(
```
Modal bottom sheet that hosts `content` and is dismissed via `onDismiss`.

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/deletebeneficiary/DeleteBeneficiaryBottomSheet.kt:62</code></summary>

```kotlin
    when (dialogState) {
        is DeleteBeneficiaryState.DialogState.Confirmation -> {
            ForkBottomSheet(
                onDismiss = onDismiss,
            ) {
                DeleteConfirmationContent(
                    beneficiaryName = dialogState.beneficiaryName,
```

</details>

```kotlin
fun ForkBottomSheetPreview()
```
Preview of `ForkBottomSheet`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/Button.kt`

```kotlin
fun ForkButton(
```
Mifos Wallet filled button with generic content slot. Wraps Material 3 `Button`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingAccountScreen.kt:517</code></summary>

```kotlin

        item("CreateOrUpdateSavingAccount") {
            ForkButton(
                text = {
                    Text(
                        text = btnText,
                    )
```

</details>

```kotlin
fun ForkButton(
```
Primary filled Mifos button with an optional leading icon and a composable `text` label.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingAccountScreen.kt:517</code></summary>

```kotlin

        item("CreateOrUpdateSavingAccount") {
            ForkButton(
                text = {
                    Text(
                        text = btnText,
                    )
```

</details>

```kotlin
fun ForkButton(
```
Mifos Wallet filled button with text and icon content slots.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingAccountScreen.kt:517</code></summary>

```kotlin

        item("CreateOrUpdateSavingAccount") {
            ForkButton(
                text = {
                    Text(
                        text = btnText,
                    )
```

</details>

```kotlin
fun ForkOutlinedButton(
```
Mifos Wallet outlined button with generic content slot. Wraps Material 3 `OutlinedButton`.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/socialSignup/SignupMethodScreen.kt:105</code></summary>

```kotlin
        )

        ForkOutlinedButton(
            modifier = Modifier.padding(top = KptTheme.spacing.xl),
            onClick = onSignUpAsMerchant,
            border = BorderStroke(
                1.dp,
```

</details>

```kotlin
fun ForkOutlinedButton(
```
Mifos Wallet outlined button with text and icon content slots.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/socialSignup/SignupMethodScreen.kt:105</code></summary>

```kotlin
        )

        ForkOutlinedButton(
            modifier = Modifier.padding(top = KptTheme.spacing.xl),
            onClick = onSignUpAsMerchant,
            border = BorderStroke(
                1.dp,
```

</details>

```kotlin
fun ForkTextButton(
```
Mifos Wallet text button with generic content slot. Wraps Material 3 `TextButton`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/ForkBasicDialog.kt:33</code></summary>

```kotlin
            onDismissRequest = onDismissRequest,
            confirmButton = {
                ForkTextButton(
                    content = {
                        Text(text = "Ok")
                    },
                    onClick = onDismissRequest,
```

</details>

```kotlin
fun ForkTextButton(
```
Mifos Wallet text button with text and icon content slots.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/ForkBasicDialog.kt:33</code></summary>

```kotlin
            onDismissRequest = onDismissRequest,
            confirmButton = {
                ForkTextButton(
                    content = {
                        Text(text = "Ok")
                    },
                    onClick = onDismissRequest,
```

</details>

```kotlin
fun ForkButtonPreview()
```
Preview of `ForkButton`.

```kotlin
fun ForkOutlinedButtonPreview()
```
Preview of ForkOutlinedButton.

```kotlin
fun ForkButtonLeadingIconPreview()
```
Preview of `ForkButton` with a leading icon.

```kotlin
object ForkButtonDefaults
```
Mifos Wallet button default values.

- `const val DISABLED_OUTLINED_BUTTON_BORDER_ALPHA = 0.12f` — Alpha applied to the border of a disabled outlined button, which Material does not do by default.
- `val OutlinedButtonBorderWidth = 1.dp` — Border width of outlined buttons (not exposed by Material ButtonDefaults).

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/Card.kt`

```kotlin
fun ForkCard(
```
Themed Material card wrapper with Mifos default shape, elevation and colors.

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/ui/MerchantsItem.kt:43</code></summary>

```kotlin
    modifier: Modifier = Modifier,
) {
    ForkCard(
        modifier = modifier.combinedClickable(
            onClick = onMerchantClicked,
            onLongClick = {
                onMerchantLongPressed(savingsWithAssociations.id.toString())
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/ForkAccountCard.kt`

```kotlin
fun ForkAccountCard(
```
Card summarising a wallet account (number, type, balance) that can be clicked or have actions applied.

<details><summary>Used in the template — <code>feature/pocket/src/commonMain/kotlin/kpt/feature/pocket/screens/PocketDashboardScreen.kt:202</code></summary>

```kotlin
            Column(verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md)) {
                buckets.savingsAccounts.forEach { account ->
                    ForkAccountCard(
                        accountId = account.accountId,
                        accountType = account.name
                            ?: stringResource(Res.string.feature_pocket_unknown_account),
                        accountNumber = account.accountNumber,
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/ForkAlertDialog.kt`

```kotlin
fun ForkAlertDialog(
```
Alert dialog showing an `icon`, `title` and `message` with confirm/dismiss actions.

<details><summary>Used in the template — <code>feature/pocket/src/commonMain/kotlin/kpt/feature/pocket/screens/ManagePocketScreen.kt:323</code></summary>

```kotlin

        is ManagePocketDialogState.Error -> {
            ForkAlertDialog(
                title = stringResource(Res.string.feature_pocket_dialog_error_title),
                message = stringResource(dialogState.message),
                icon = ForkIcons.Error,
                confirmText = stringResource(Res.string.feature_pocket_action_ok),
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/ForkBasicDialog.kt`

```kotlin
fun ForkBasicDialog(
```
Dialog that shows an error-style title and message from a `BasicDialogState` with a single dismiss action.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:454</code></summary>

```kotlin
) {
    when (dialogState) {
        is AccountState.DialogState.DeleteBeneficiary -> ForkBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = stringResource(dialogState.title),
                message = stringResource(dialogState.message),
            ),
```

</details>

```kotlin
fun ForkBasicDialog(
```
Dialog that shows a title and message from a `BasicDialogState` with confirm and dismiss actions.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:454</code></summary>

```kotlin
) {
    when (dialogState) {
        is AccountState.DialogState.DeleteBeneficiary -> ForkBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = stringResource(dialogState.title),
                message = stringResource(dialogState.message),
            ),
```

</details>

```kotlin
sealed class BasicDialogState
```
Models display of a `ForkBasicDialog`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:455</code></summary>

```kotlin
    when (dialogState) {
        is AccountState.DialogState.DeleteBeneficiary -> ForkBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = stringResource(dialogState.title),
                message = stringResource(dialogState.message),
            ),
            onConfirm = dialogState.onConfirm,
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/ForkLoadingDialog.kt`

```kotlin
fun ForkLoadingDialog(
```
Full-screen modal dialog with a loading indicator, driven by `LoadingDialogState`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:470</code></summary>

```kotlin
        )

        is AccountState.DialogState.Loading -> ForkLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
```

</details>

```kotlin
sealed class LoadingDialogState
```
Models display of a `ForkLoadingDialog`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:471</code></summary>

```kotlin

        is AccountState.DialogState.Loading -> ForkLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/ForkScaffold.kt`

```kotlin
fun ForkScaffold(
```
Scaffold with a standard top bar (title and back button) wrapping the screen `content`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:161</code></summary>

```kotlin
    modifier: Modifier = Modifier,
) {
    ForkScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
```

</details>

```kotlin
fun ForkScaffold(
```
Scaffold that takes caller-supplied top bar, bottom bar, snackbar and floating action button slots.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:161</code></summary>

```kotlin
    modifier: Modifier = Modifier,
) {
    ForkScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
```

</details>

```kotlin
fun ForkBottomSheetScaffold(
```
Scaffold with a persistent bottom sheet hosting `sheetContent`.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/confirm/TransferConfirmScreen.kt:288</code></summary>

```kotlin
    onAction: (TransferConfirmAction) -> Unit,
) {
    ForkBottomSheetScaffold(
        topBar = {
            ForkTopBar(
                topBarTitle = stringResource(Res.string.feature_make_transfer_review_title),
                backPress = {
```

</details>

```kotlin
data class FloatingActionButtonContent(
```
Describes a floating action button for `ForkScaffold`.

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
data class ForkPullToRefreshState(
```
State holder for pull-to-refresh behaviour of a scaffold.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleManagementScreen.kt:73</code></summary>

```kotlin
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val pullRefreshState = rememberForkPullToRefreshState(
        isEnabled = true,
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.trySendAction(AutoPayScheduleManagementAction.RefreshSchedules) },
    )
```

</details>

```kotlin
fun rememberForkPullToRefreshState(
```
Creates and remembers a `ForkPullToRefreshState`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/ForkTab.kt`

```kotlin
fun ForkTab(
```
Single tab with a text label, used inside tab rows.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/ScrollableTabRow.kt:47</code></summary>

```kotlin
    ) {
        tabContents.forEachIndexed { index, currentTab ->
            ForkTab(
                text = currentTab.tabName,
                selected = pagerState.currentPage == index,
                selectedColor = selectedContentColor,
                unselectedColor = unselectedContentColor,
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/ForkTabPager.kt`

```kotlin
fun ForkTabPager(
```
Tab row synchronised with a pager, rendering one page per tab.

<details><summary>Used in the template — <code>feature/pocket/src/commonMain/kotlin/kpt/feature/pocket/screens/ManagePocketScreen.kt:537</code></summary>

```kotlin
                .weight(1f),
        ) {
            ForkTabPager(
                modifier = Modifier.fillMaxSize(),
                pagerState = pagerState,
                currentPage = selectedTabIndex,
                tabs = tabs.map { it.name },
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/ForkTopAppBar.kt`

```kotlin
fun ForkTopAppBar(
```
Center-aligned top app bar with a title and a navigation icon.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationScreen.kt:111</code></summary>

```kotlin
        snackbarHostState = snackbarHostState,
        topBar = {
            ForkTopAppBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = NavigationIcon(
                    navigationIcon = ForkIcons.Back,
```

</details>

```kotlin
fun ForkTopAppBar(
```
Center-aligned top app bar with a title and optional `NavigationIcon` and action icons.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationScreen.kt:111</code></summary>

```kotlin
        snackbarHostState = snackbarHostState,
        topBar = {
            ForkTopAppBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = NavigationIcon(
                    navigationIcon = ForkIcons.Back,
```

</details>

```kotlin
fun ForkTopAppBar(
```
Top app bar with a title and subtitle, an optional `NavigationIcon` and action icons.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationScreen.kt:111</code></summary>

```kotlin
        snackbarHostState = snackbarHostState,
        topBar = {
            ForkTopAppBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = NavigationIcon(
                    navigationIcon = ForkIcons.Back,
```

</details>

```kotlin
data class NavigationIcon(
```
Navigation icon configuration for a top app bar.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationScreen.kt:114</code></summary>

```kotlin
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = NavigationIcon(
                    navigationIcon = ForkIcons.Back,
                    navigationIconContentDescription = "Go Back",
                    onNavigationIconClick = {
                        onEvent(MobileVerificationAction.CloseButtonClick)
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/ForkTopBar.kt`

```kotlin
fun ForkTopBar(
```
Top bar showing `topBarTitle` with a back button wired to `backPress`.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillScreen.kt:102</code></summary>

```kotlin
    ForkScaffold(
        topBar = {
            ForkTopBar(
                topBarTitle = "Add New Bill",
                backPress = onNavigateBack,
            )
        },
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/IconBox.kt`

```kotlin
fun IconBox(
```
Outlined icon button showing `icon`, used for small tappable icon actions.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/ForkTopBar.kt:39</code></summary>

```kotlin
        },
        navigationIcon = {
            IconBox(
                icon = ForkIcons.ArrowBack2,
                onClick = backPress,
            )
        },
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/LoadingWheel.kt`

```kotlin
fun ForkLoadingWheel(
```
Animated loading wheel with an accessibility content description.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentProcessingScreen.kt:92</code></summary>

```kotlin
    ) {
        if (state.isProcessing) {
            ForkLoadingWheel(
                contentDesc = "Processing Payment",
                modifier = Modifier.size(120.dp),
            )
        } else {
```

</details>

```kotlin
fun ForkOverlayLoadingWheel(
```
Loading wheel shown on a rounded overlay surface above the content.

```kotlin
fun MfOverlayLoadingWheel(
```
Overlay loading wheel with a default "Loading" description.

```kotlin
fun MfLoadingWheel(
```
Loading wheel with a default "Loading" description and configurable background color.

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/ui/MerchantScreen.kt:122</code></summary>

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
fun ForkLoadingWheelPreview()
```
Preview of `ForkLoadingWheel`.

```kotlin
fun NiaOverlayLoadingWheelPreview()
```
Preview of `ForkOverlayLoadingWheel`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/Navigation.kt`

```kotlin
fun RowScope.ForkNavigationBarItem(
```
Now in Android navigation bar item with icon and label content slots. Wraps Material 3 `NavigationBarItem`.

```kotlin
fun ForkNavigationBar(
```
Now in Android navigation bar with content slot. Wraps Material 3 `NavigationBar`.

```kotlin
fun ForkNavigationRailItem(
```
Now in Android navigation rail item with icon and label content slots. Wraps Material 3 `NavigationRailItem`.

```kotlin
fun ForkNavigationRail(
```
Now in Android navigation rail with header and content slots. Wraps Material 3 `NavigationRail`.

```kotlin
fun ForkNavigationBarPreview()
```
Preview of the Mifos bottom navigation bar.

```kotlin
fun ForkNavigationRailPreview()
```
Preview of the Mifos navigation rail.

```kotlin
object ForkNavigationDefaults
```
Now in Android navigation default values.

- `fun navigationContentColor() = KptTheme.colorScheme.onSurfaceVariant` — Color of unselected navigation item content.
- `fun navigationSelectedItemColor() = KptTheme.colorScheme.onPrimaryContainer` — Color of the selected navigation item content.
- `fun navigationIndicatorColor() = KptTheme.colorScheme.primaryContainer` — Color of the selected navigation item indicator.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/TextField.kt`

```kotlin
fun ForkOutlinedTextField(
```
Outlined text field with label, optional leading/trailing icon, error text and keyboard options.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginScreen.kt:181</code></summary>

```kotlin
        )
        Spacer(modifier = Modifier.padding(top = KptTheme.spacing.xl))
        ForkOutlinedTextField(
            label = stringResource(Res.string.feature_auth_username),
            value = state.username,
            onValueChange = {
                onAction(LoginAction.UsernameChanged(it))
```

</details>

```kotlin
fun ForkTextField(
```
Filled text field with label, optional icons, error text and keyboard options.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingAccountScreen.kt:228</code></summary>

```kotlin
    ) {
        item("Client Name") {
            ForkTextField(
                value = state.template.clientName,
                label = stringResource(Res.string.feature_accounts_add_client_name),
                onValueChange = {},
                readOnly = true,
```

</details>

```kotlin
fun ForkCustomTextField(
```
Custom-styled text field operating on a plain `String` value.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/ForkPasswordField.kt:63</code></summary>

```kotlin
    val focusRequester = remember { FocusRequester() }

    ForkCustomTextField(
        modifier = modifier
            .tabNavigation()
            .focusRequester(focusRequester),
        label = label,
```

</details>

```kotlin
fun ForkCustomTextField(
```
Custom-styled text field operating on a `TextFieldValue`, giving callers control over cursor and selection.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/ForkPasswordField.kt:63</code></summary>

```kotlin
    val focusRequester = remember { FocusRequester() }

    ForkCustomTextField(
        modifier = modifier
            .tabNavigation()
            .focusRequester(focusRequester),
        label = label,
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/TextUserImage.kt`

```kotlin
fun ForkTextUserImage(
```
Circular avatar showing the initials derived from `text`.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/components/BeneficiaryCard.kt:57</code></summary>

```kotlin
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ForkTextUserImage(
                text = getInitials(beneficiary.name),
                size = 44.dp,
            )
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/TopAppBar.kt`

```kotlin
fun ForkTopAppBar(
```
Top app bar with a title and optional navigation icon and actions.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationScreen.kt:111</code></summary>

```kotlin
        snackbarHostState = snackbarHostState,
        topBar = {
            ForkTopAppBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = NavigationIcon(
                    navigationIcon = ForkIcons.Back,
```

</details>

```kotlin
fun ForkNavigationTopAppBar(
```
Top app bar with a title and a back navigation button when `onNavigationClick` is provided.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/scrollbar/AppScrollbars.kt`

```kotlin
fun ScrollableState.DraggableScrollbar(
```
A `Scrollbar` that allows for fast scrolling of content by dragging its thumb. Its thumb disappears when the scrolling container is dormant.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/fork/HomeScreen.kt:369</code></summary>

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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/scrollbar/Scrollbar.kt`

```kotlin
class ScrollbarState
```
Holds the packed scroll metrics of a scrollbar and exposes the derived thumb size.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/scrollbar/AppScrollbars.kt:69</code></summary>

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

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/scrollbar/AppScrollbars.kt:75</code></summary>

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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/scrollbar/ScrollbarExt.kt`

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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/scrollbar/ThumbExt.kt`

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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/icon/ForkIcons.kt`

```kotlin
object ForkIcons
```
Mifos icons. Material icons are `ImageVector`s, custom icons are drawable resource IDs.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:177</code></summary>

```kotlin
                ) {
                    Icon(
                        imageVector = ForkIcons.Add,
                        stringResource(Res.string.feature_accounts_add),
                    )
                }
            }
```

</details>

- `val Fingerprint = Icons.Default.Fingerprint` — Fingerprint icon used for biometric authentication prompts.
- `val OutlinedInfo = Icons.Outlined.Info` — Outlined information icon.
- `val OutlinedLock = Icons.Outlined.Lock` — Outlined padlock icon, used for security and passcode settings.
- `val OutlinedNotifications = Icons.Outlined.Notifications` — Outlined notification bell icon.
- `val ChevronRight: ImageVector = Icons.Filled.ChevronRight` — Filled right-pointing chevron, typically a list-row navigation affordance.
- `val QrCode: ImageVector = Icons.Filled.QrCode` — Filled QR code icon.
- `val Close: ImageVector = Icons.Filled.Close` — Close (X) icon.
- `val Error: ImageVector = Icons.Filled.Error` — Filled error icon.
- `val AttachMoney: ImageVector = Icons.Filled.AttachMoney` — Dollar/money icon.
- `val OutlinedVisibilityOff: ImageVector = Icons.Outlined.VisibilityOff` — Outlined "hide" eye icon, used to mask password text.
- `val OutlinedVisibility: ImageVector = Icons.Outlined.Visibility` — Outlined "show" eye icon, used to reveal password text.
- `val VisibilityOff: ImageVector = Icons.Filled.VisibilityOff` — Filled "hide" eye icon.
- `val Visibility: ImageVector = Icons.Filled.Visibility` — Filled "show" eye icon.
- `val Check: ImageVector = Icons.Default.Check` — Check mark icon.
  _…more members; read the file._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/utils/ExpirationDateMask.kt`

```kotlin
class ExpirationDateMask : VisualTransformation
```
Visual transformation that formats card expiry input as MM/YY.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/utils/ModifierExt.kt`

```kotlin
fun Modifier.mirrorIfRtl(): Modifier =
```
Flips the content horizontally when the layout direction is right-to-left.

```kotlin
fun Modifier.tabNavigation(): Modifier
```
Moves focus to the next/previous element when Tab/Shift+Tab is pressed (desktop and web keyboards).

```kotlin
fun Modifier.onClick(
```
Makes the element clickable without a ripple unless an `indication` is supplied.

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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/BackgroundTheme.kt`

```kotlin
data class BackgroundTheme(
```
A class to model background color and tonal elevation values for Now in Android.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/fork/Theme.kt:61</code></summary>

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

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/Background.kt:44</code></summary>

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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Color.kt`

```kotlin
val primaryLight = Color(0xFF4338CA)
```
Light-scheme `primary` color role of the default Mifos Pay palette.

```kotlin
val onPrimaryLight = Color(0xFFFFFFFF)
```
Light-scheme `onPrimary` color role of the default Mifos Pay palette.

```kotlin
val primaryContainerLight = Color(0xFFE0E7FF)
```
Light-scheme `primaryContainer` color role of the default Mifos Pay palette.

```kotlin
val onPrimaryContainerLight = Color(0xFF1F1D75)
```
Light-scheme `onPrimaryContainer` color role of the default Mifos Pay palette.

```kotlin
val secondaryLight = Color(0xFF059669)
```
Light-scheme `secondary` color role of the default Mifos Pay palette.

```kotlin
val onSecondaryLight = Color(0xFFFFFFFF)
```
Light-scheme `onSecondary` color role of the default Mifos Pay palette.

```kotlin
val secondaryContainerLight = Color(0xFFD1FAE5)
```
Light-scheme `secondaryContainer` color role of the default Mifos Pay palette.

```kotlin
val onSecondaryContainerLight = Color(0xFF064E3B)
```
Light-scheme `onSecondaryContainer` color role of the default Mifos Pay palette.

```kotlin
val tertiaryLight = Color(0xFFD97706)
```
Light-scheme `tertiary` color role of the default Mifos Pay palette.

```kotlin
val onTertiaryLight = Color(0xFFFFFFFF)
```
Light-scheme `onTertiary` color role of the default Mifos Pay palette.

```kotlin
val tertiaryContainerLight = Color(0xFFFEF3C7)
```
Light-scheme `tertiaryContainer` color role of the default Mifos Pay palette.

```kotlin
val onTertiaryContainerLight = Color(0xFF78350F)
```
Light-scheme `onTertiaryContainer` color role of the default Mifos Pay palette.

```kotlin
val errorLight = Color(0xFFF87171)
```
Light-scheme `error` color role of the default Mifos Pay palette.

```kotlin
val onErrorLight = Color(0xFFFFFFFF)
```
Light-scheme `onError` color role of the default Mifos Pay palette.

```kotlin
val errorContainerLight = Color(0xFFFFE4E1)
```
Light-scheme `errorContainer` color role of the default Mifos Pay palette.

```kotlin
val onErrorContainerLight = Color(0xFF9F1239)
```
Light-scheme `onErrorContainer` color role of the default Mifos Pay palette.

```kotlin
val backgroundLight = Color(0xFFFAFAFB)
```
Light-scheme `background` color role of the default Mifos Pay palette.

```kotlin
val onBackgroundLight = Color(0xFF0F172A)
```
Light-scheme `onBackground` color role of the default Mifos Pay palette.

```kotlin
val surfaceLight = Color(0xFFFAFAFB)
```
Light-scheme `surface` color role of the default Mifos Pay palette.

```kotlin
val onSurfaceLight = Color(0xFF0F172A)
```
Light-scheme `onSurface` color role of the default Mifos Pay palette.

```kotlin
val surfaceVariantLight = Color(0xFFE2E8F0)
```
Light-scheme `surfaceVariant` color role of the default Mifos Pay palette.

```kotlin
val onSurfaceVariantLight = Color(0xFF475569)
```
Light-scheme `onSurfaceVariant` color role of the default Mifos Pay palette.

```kotlin
val outlineLight = Color(0xFF94A3B8)
```
Light-scheme `outline` color role of the default Mifos Pay palette.

```kotlin
val outlineVariantLight = Color(0xFFCBD5E1)
```
Light-scheme `outlineVariant` color role of the default Mifos Pay palette.

```kotlin
val scrimLight = Color(0xFF000000)
```
Light-scheme `scrim` color role of the default Mifos Pay palette.

```kotlin
val inverseSurfaceLight = Color(0xFF1E293B)
```
Light-scheme `inverseSurface` color role of the default Mifos Pay palette.

```kotlin
val inverseOnSurfaceLight = Color(0xFFF1F5F9)
```
Light-scheme `inverseOnSurface` color role of the default Mifos Pay palette.

```kotlin
val inversePrimaryLight = Color(0xFFA5B4FC)
```
Light-scheme `inversePrimary` color role of the default Mifos Pay palette.

```kotlin
val surfaceDimLight = Color(0xFFE2E8F0)
```
Light-scheme `surfaceDim` color role of the default Mifos Pay palette.

```kotlin
val surfaceBrightLight = Color(0xFFFAFAFB)
```
Light-scheme `surfaceBright` color role of the default Mifos Pay palette.

```kotlin
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
```
Light-scheme `surfaceContainerLowest` color role of the default Mifos Pay palette.

```kotlin
val surfaceContainerLowLight = Color(0xFFF8FAFC)
```
Light-scheme `surfaceContainerLow` color role of the default Mifos Pay palette.

```kotlin
val surfaceContainerLight = Color(0xFFF1F5F9)
```
Light-scheme `surfaceContainer` color role of the default Mifos Pay palette.

```kotlin
val surfaceContainerHighLight = Color(0xFFE2E8F0)
```
Light-scheme `surfaceContainerHigh` color role of the default Mifos Pay palette.

```kotlin
val surfaceContainerHighestLight = Color(0xFFCBD5E1)
```
Light-scheme `surfaceContainerHighest` color role of the default Mifos Pay palette.

```kotlin
val primaryDark = Color(0xFFA5B4FC)
```
Dark-scheme `primary` color role of the default Mifos Pay palette.

```kotlin
val onPrimaryDark = Color(0xFF1F1D75)
```
Dark-scheme `onPrimary` color role of the default Mifos Pay palette.

```kotlin
val primaryContainerDark = Color(0xFF3730A3)
```
Dark-scheme `primaryContainer` color role of the default Mifos Pay palette.

```kotlin
val onPrimaryContainerDark = Color(0xFFE0E7FF)
```
Dark-scheme `onPrimaryContainer` color role of the default Mifos Pay palette.

```kotlin
val secondaryDark = Color(0xFF6EE7B7)
```
Dark-scheme `secondary` color role of the default Mifos Pay palette.

```kotlin
val onSecondaryDark = Color(0xFF064E3B)
```
Dark-scheme `onSecondary` color role of the default Mifos Pay palette.

```kotlin
val secondaryContainerDark = Color(0xFF065F46)
```
Dark-scheme `secondaryContainer` color role of the default Mifos Pay palette.

```kotlin
val onSecondaryContainerDark = Color(0xFFD1FAE5)
```
Dark-scheme `onSecondaryContainer` color role of the default Mifos Pay palette.

```kotlin
val tertiaryDark = Color(0xFFFCD34D)
```
Dark-scheme `tertiary` color role of the default Mifos Pay palette.

```kotlin
val onTertiaryDark = Color(0xFF78350F)
```
Dark-scheme `onTertiary` color role of the default Mifos Pay palette.

```kotlin
val tertiaryContainerDark = Color(0xFF92400E)
```
Dark-scheme `tertiaryContainer` color role of the default Mifos Pay palette.

```kotlin
val onTertiaryContainerDark = Color(0xFFFEF3C7)
```
Dark-scheme `onTertiaryContainer` color role of the default Mifos Pay palette.

```kotlin
val errorDark = Color(0xFFFDBA74)
```
Dark-scheme `error` color role of the default Mifos Pay palette.

```kotlin
val onErrorDark = Color(0xFF7C2D12)
```
Dark-scheme `onError` color role of the default Mifos Pay palette.

```kotlin
val errorContainerDark = Color(0xFFC2410C)
```
Dark-scheme `errorContainer` color role of the default Mifos Pay palette.

```kotlin
val onErrorContainerDark = Color(0xFFFFEDD5)
```
Dark-scheme `onErrorContainer` color role of the default Mifos Pay palette.

```kotlin
val backgroundDark = Color(0xFF0F172A)
```
Dark-scheme `background` color role of the default Mifos Pay palette.

```kotlin
val onBackgroundDark = Color(0xFFF1F5F9)
```
Dark-scheme `onBackground` color role of the default Mifos Pay palette.

```kotlin
val surfaceDark = Color(0xFF0F172A)
```
Dark-scheme `surface` color role of the default Mifos Pay palette.

```kotlin
val onSurfaceDark = Color(0xFFF1F5F9)
```
Dark-scheme `onSurface` color role of the default Mifos Pay palette.

```kotlin
val surfaceVariantDark = Color(0xFF1E293B)
```
Dark-scheme `surfaceVariant` color role of the default Mifos Pay palette.

```kotlin
val onSurfaceVariantDark = Color(0xFFCBD5E1)
```
Dark-scheme `onSurfaceVariant` color role of the default Mifos Pay palette.

```kotlin
val outlineDark = Color(0xFF64748B)
```
Dark-scheme `outline` color role of the default Mifos Pay palette.

```kotlin
val outlineVariantDark = Color(0xFF334155)
```
Dark-scheme `outlineVariant` color role of the default Mifos Pay palette.

```kotlin
val scrimDark = Color(0xFF000000)
```
Dark-scheme `scrim` color role of the default Mifos Pay palette.

```kotlin
val inverseSurfaceDark = Color(0xFFF1F5F9)
```
Dark-scheme `inverseSurface` color role of the default Mifos Pay palette.

```kotlin
val inverseOnSurfaceDark = Color(0xFF1E293B)
```
Dark-scheme `inverseOnSurface` color role of the default Mifos Pay palette.

```kotlin
val inversePrimaryDark = Color(0xFF4338CA)
```
Dark-scheme `inversePrimary` color role of the default Mifos Pay palette.

```kotlin
val surfaceDimDark = Color(0xFF0F172A)
```
Dark-scheme `surfaceDim` color role of the default Mifos Pay palette.

```kotlin
val surfaceBrightDark = Color(0xFF374558)
```
Dark-scheme `surfaceBright` color role of the default Mifos Pay palette.

```kotlin
val surfaceContainerLowestDark = Color(0xFF020617)
```
Dark-scheme `surfaceContainerLowest` color role of the default Mifos Pay palette.

```kotlin
val surfaceContainerLowDark = Color(0xFF1E293B)
```
Dark-scheme `surfaceContainerLow` color role of the default Mifos Pay palette.

```kotlin
val surfaceContainerDark = Color(0xFF243044)
```
Dark-scheme `surfaceContainer` color role of the default Mifos Pay palette.

```kotlin
val surfaceContainerHighDark = Color(0xFF2D3B52)
```
Dark-scheme `surfaceContainerHigh` color role of the default Mifos Pay palette.

```kotlin
val surfaceContainerHighestDark = Color(0xFF374558)
```
Dark-scheme `surfaceContainerHighest` color role of the default Mifos Pay palette.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/ColorScheme.kt`

```kotlin
val lightKptColorScheme = LightDefaultColorScheme.toKptColorScheme()
```
Light `KptColorScheme` built from the default light color scheme.

```kotlin
val darkKptColorScheme = DarkDefaultColorScheme.toKptColorScheme()
```
Dark `KptColorScheme` built from the default dark color scheme.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Elevation.kt`

```kotlin
data class Elevation(
```
Shared elevation tier scale — five named tiers matching Material 3 elevation guidance. Access from any Composable via `MaterialTheme.elevation`.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayHistoryScreen.kt:170</code></summary>

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
Composition local providing the active `Elevation` scale.

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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/ForkTheme.deprecated.kt`

```kotlin
fun ForkTheme(
```
Back-compat shim — forwards to `KptTheme`. Composables cannot be `typealias`'d, so this file holds a thin `@Composable fun` wrapper that re-exports the old `ForkTheme` name for one release while consumers migrate.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginScreen.kt:287</code></summary>

```kotlin
@Composable
private fun LoginScreenPreview() {
    ForkTheme {
        LoginScreen(
            state = LoginState(dialogState = null),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/ForkTheme.kt`

```kotlin
val lightScheme = lightColorScheme(
```
The assembled Material 3 light scheme, wired from the palette tokens in `Color.kt`.

```kotlin
val darkScheme = darkColorScheme(
```
The assembled Material 3 dark scheme, wired from the palette tokens in `Color.kt`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/GradientColors.kt`

```kotlin
data class GradientColors(
```
A class to model gradient color values for Now in Android.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/Background.kt:68</code></summary>

```kotlin
fun ForkGradientBackground(
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

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fork/component/Background.kt:68</code></summary>

```kotlin
fun ForkGradientBackground(
    modifier: Modifier = Modifier,
    gradientColors: GradientColors = LocalGradientColors.current,
    content: @Composable () -> Unit,
) {
    val currentTopColor by rememberUpdatedState(gradientColors.top)
    val currentBottomColor by rememberUpdatedState(gradientColors.bottom)
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt`

```kotlin
fun KptTheme(
```
The main theme composable for the application. This composable uses KptMaterialTheme under the hood to provide seamless integration between KptTheme design tokens and Material3 theming system.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:198</code></summary>

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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Spacing.kt`

```kotlin
data class Spacing(
```
Shared spacing scale — replaces raw `.dp` literals scattered across features with a disciplined 4 / 8 / 12 / 16 / 24 / 32 / 48 progression. Access from any Composable via `MaterialTheme.spacing`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/details/SavingAccountDetailScreen.kt:426</code></summary>

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
Composition local providing the active `Spacing` scale.

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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/TextStyles.kt`

```kotlin
val styleMedium16sp =
```
Medium-weight 16sp sans-serif text style.

```kotlin
val styleNormal18sp =
```
Normal-weight 18sp sans-serif text style.

```kotlin
val styleMedium30sp =
```
Medium-weight 30sp sans-serif text style.

```kotlin
val styleForkTopBar =
```
Semi-bold dark text style used for top bar titles.

```kotlin
val styleSettingsButton = TextStyle(textAlign = TextAlign.Center)
```
Center-aligned text style for settings buttons.

```kotlin
val historyItemTextStyle = TextStyle(fontSize = 16.sp)
```
16sp text style for transaction history items.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/ThemeExtensions.kt`

```kotlin
fun KptShapes.toRoundedCornerShape(
```
Converts the given corner values into a `RoundedCornerShape`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/TintTheme.kt`

```kotlin
data class TintTheme(
```
A class to model background color and tonal elevation values for Now in Android.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/fork/Theme.kt:69</code></summary>

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

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/fork/Theme.kt:76</code></summary>

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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Type.kt`

```kotlin
val fontFamily: FontFamily
```
Outfit font family used by the app typography, with all weights from Thin to Black.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/fork/Color.kt`

```kotlin
val primaryLight = Color(0xFF0673BA)
```
Light-scheme `primary` color role of the fork-level palette.

```kotlin
val onPrimaryLight = Color(0xFFFFFFFF)
```
Light-scheme `onPrimary` color role of the fork-level palette.

```kotlin
val primaryContainerLight = Color(0xFFCEEAFD)
```
Light-scheme `primaryContainer` color role of the fork-level palette.

```kotlin
val onPrimaryContainerLight = Color(0xFF033D63)
```
Light-scheme `onPrimaryContainer` color role of the fork-level palette.

```kotlin
val secondaryLight = Color(0xFF984061)
```
Light-scheme `secondary` color role of the fork-level palette.

```kotlin
val onSecondaryLight = Color(0xFFFFFFFF)
```
Light-scheme `onSecondary` color role of the fork-level palette.

```kotlin
val secondaryContainerLight = Color(0xFFF0DBE3)
```
Light-scheme `secondaryContainer` color role of the fork-level palette.

```kotlin
val onSecondaryContainerLight = Color(0xFF361722)
```
Light-scheme `onSecondaryContainer` color role of the fork-level palette.

```kotlin
val tertiaryLight = Color(0xFF7D4996)
```
Light-scheme `tertiary` color role of the fork-level palette.

```kotlin
val onTertiaryLight = Color(0xFFFFFFFF)
```
Light-scheme `onTertiary` color role of the fork-level palette.

```kotlin
val tertiaryContainerLight = Color(0XFFE9DDEE)
```
Light-scheme `tertiaryContainer` color role of the fork-level palette.

```kotlin
val onTertiaryContainerLight = Color(0xFF2B1934)
```
Light-scheme `onTertiaryContainer` color role of the fork-level palette.

```kotlin
val errorLight = Color(0xFFBA1A1A)
```
Light-scheme `error` color role of the fork-level palette.

```kotlin
val onErrorLight = Color(0xFFFFFFFF)
```
Light-scheme `onError` color role of the fork-level palette.

```kotlin
val errorContainerLight = Color(0xFFFFDAD6)
```
Light-scheme `errorContainer` color role of the fork-level palette.

```kotlin
val onErrorContainerLight = Color(0xFF93000A)
```
Light-scheme `onErrorContainer` color role of the fork-level palette.

```kotlin
val backgroundLight = Color(0xFFF8F9FF)
```
Light-scheme `background` color role of the fork-level palette.

```kotlin
val onBackgroundLight = Color(0xFF181C20)
```
Light-scheme `onBackground` color role of the fork-level palette.

```kotlin
val surfaceLight = Color(0xFFF8F9FF)
```
Light-scheme `surface` color role of the fork-level palette.

```kotlin
val onSurfaceLight = Color(0xFF181C20)
```
Light-scheme `onSurface` color role of the fork-level palette.

```kotlin
val surfaceVariantLight = Color(0xFFDCE3EF)
```
Light-scheme `surfaceVariant` color role of the fork-level palette.

```kotlin
val onSurfaceVariantLight = Color(0xFF404751)
```
Light-scheme `onSurfaceVariant` color role of the fork-level palette.

```kotlin
val outlineLight = Color(0xFF717882)
```
Light-scheme `outline` color role of the fork-level palette.

```kotlin
val outlineVariantLight = Color(0xFFC0C7D2)
```
Light-scheme `outlineVariant` color role of the fork-level palette.

```kotlin
val scrimLight = Color(0xFF000000)
```
Light-scheme `scrim` color role of the fork-level palette.

```kotlin
val inverseSurfaceLight = Color(0xFF2D3136)
```
Light-scheme `inverseSurface` color role of the fork-level palette.

```kotlin
val inverseOnSurfaceLight = Color(0xFFEEF1F7)
```
Light-scheme `inverseOnSurface` color role of the fork-level palette.

```kotlin
val inversePrimaryLight = Color(0xFF9BCAFF)
```
Light-scheme `inversePrimary` color role of the fork-level palette.

```kotlin
val surfaceDimLight = Color(0xFFD8DAE0)
```
Light-scheme `surfaceDim` color role of the fork-level palette.

```kotlin
val surfaceBrightLight = Color(0xFFF8F9FF)
```
Light-scheme `surfaceBright` color role of the fork-level palette.

```kotlin
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
```
Light-scheme `surfaceContainerLowest` color role of the fork-level palette.

```kotlin
val surfaceContainerLowLight = Color(0xFFF1F3FA)
```
Light-scheme `surfaceContainerLow` color role of the fork-level palette.

```kotlin
val surfaceContainerLight = Color(0xFFECEEF4)
```
Light-scheme `surfaceContainer` color role of the fork-level palette.

```kotlin
val surfaceContainerHighLight = Color(0xFFE6E8EE)
```
Light-scheme `surfaceContainerHigh` color role of the fork-level palette.

```kotlin
val surfaceContainerHighestLight = Color(0xFFE0E2E9)
```
Light-scheme `surfaceContainerHighest` color role of the fork-level palette.

```kotlin
val primaryDark = Color(0xFF9CD6FC)
```
Dark-scheme `primary` color role of the fork-level palette.

```kotlin
val onPrimaryDark = Color(0xFF033D63)
```
Dark-scheme `onPrimary` color role of the fork-level palette.

```kotlin
val primaryContainerDark = Color(0xFF044C7C)
```
Dark-scheme `primaryContainer` color role of the fork-level palette.

```kotlin
val onPrimaryContainerDark = Color(0xFFE6F5FE)
```
Dark-scheme `onPrimaryContainer` color role of the fork-level palette.

```kotlin
val secondaryDark = Color(0xFFD9A5B8)
```
Dark-scheme `secondary` color role of the fork-level palette.

```kotlin
val onSecondaryDark = Color(0xFF361722)
```
Dark-scheme `onSecondary` color role of the fork-level palette.

```kotlin
val secondaryContainerDark = Color(0xFF5A2639)
```
Dark-scheme `secondaryContainer` color role of the fork-level palette.

```kotlin
val onSecondaryContainerDark = Color(0xFFF0DBE3)
```
Dark-scheme `onSecondaryContainer` color role of the fork-level palette.

```kotlin
val tertiaryDark = Color(0xFFC7A9D6)
```
Dark-scheme `tertiary` color role of the fork-level palette.

```kotlin
val onTertiaryDark = Color(0xFF3A2145)
```
Dark-scheme `onTertiary` color role of the fork-level palette.

```kotlin
val tertiaryContainerDark = Color(0xFF653A78)
```
Dark-scheme `tertiaryContainer` color role of the fork-level palette.

```kotlin
val onTertiaryContainerDark = Color(0xFFF4EEF7)
```
Dark-scheme `onTertiaryContainer` color role of the fork-level palette.

```kotlin
val errorDark = Color(0xFFFFB4AB)
```
Dark-scheme `error` color role of the fork-level palette.

```kotlin
val onErrorDark = Color(0xFF690005)
```
Dark-scheme `onError` color role of the fork-level palette.

```kotlin
val errorContainerDark = Color(0xFF93000A)
```
Dark-scheme `errorContainer` color role of the fork-level palette.

```kotlin
val onErrorContainerDark = Color(0xFFFFDAD6)
```
Dark-scheme `onErrorContainer` color role of the fork-level palette.

```kotlin
val backgroundDark = Color(0xFF101418)
```
Dark-scheme `background` color role of the fork-level palette.

```kotlin
val onBackgroundDark = Color(0xFFE0E2E9)
```
Dark-scheme `onBackground` color role of the fork-level palette.

```kotlin
val surfaceDark = Color(0xFF101418)
```
Dark-scheme `surface` color role of the fork-level palette.

```kotlin
val onSurfaceDark = Color(0xFFE0E2E9)
```
Dark-scheme `onSurface` color role of the fork-level palette.

```kotlin
val surfaceVariantDark = Color(0xFF404751)
```
Dark-scheme `surfaceVariant` color role of the fork-level palette.

```kotlin
val onSurfaceVariantDark = Color(0xFFC0C7D2)
```
Dark-scheme `onSurfaceVariant` color role of the fork-level palette.

```kotlin
val outlineDark = Color(0xFF8A919C)
```
Dark-scheme `outline` color role of the fork-level palette.

```kotlin
val outlineVariantDark = Color(0xFF404751)
```
Dark-scheme `outlineVariant` color role of the fork-level palette.

```kotlin
val scrimDark = Color(0xFF000000)
```
Dark-scheme `scrim` color role of the fork-level palette.

```kotlin
val inverseSurfaceDark = Color(0xFFE0E2E9)
```
Dark-scheme `inverseSurface` color role of the fork-level palette.

```kotlin
val inverseOnSurfaceDark = Color(0xFF2D3136)
```
Dark-scheme `inverseOnSurface` color role of the fork-level palette.

```kotlin
val inversePrimaryDark = Color(0xFF0062A0)
```
Dark-scheme `inversePrimary` color role of the fork-level palette.

```kotlin
val surfaceDimDark = Color(0xFF101418)
```
Dark-scheme `surfaceDim` color role of the fork-level palette.

```kotlin
val surfaceBrightDark = Color(0xFF36393E)
```
Dark-scheme `surfaceBright` color role of the fork-level palette.

```kotlin
val surfaceContainerLowestDark = Color(0xFF0B0E13)
```
Dark-scheme `surfaceContainerLowest` color role of the fork-level palette.

```kotlin
val surfaceContainerLowDark = Color(0xFF181C20)
```
Dark-scheme `surfaceContainerLow` color role of the fork-level palette.

```kotlin
val surfaceContainerDark = Color(0xFF1C2025)
```
Dark-scheme `surfaceContainer` color role of the fork-level palette.

```kotlin
val surfaceContainerHighDark = Color(0xFF272A2F)
```
Dark-scheme `surfaceContainerHigh` color role of the fork-level palette.

```kotlin
val surfaceContainerHighestDark = Color(0xFF32353A)
```
Dark-scheme `surfaceContainerHighest` color role of the fork-level palette.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/fork/Theme.kt`

```kotlin
fun ForkTheme(
```
Fork-level Mifos Material 3 theme that applies the color scheme and typography to `content`.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginScreen.kt:287</code></summary>

```kotlin
@Composable
private fun LoginScreenPreview() {
    ForkTheme {
        LoginScreen(
            state = LoginState(dialogState = null),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
```

</details>

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

---

_26 type(s), 298 function(s)/property(ies); 323 carry KDoc at source; 1 authored example(s); 70 live call site(s)._
<!-- api-docs:end -->
