# `feature/savedcards/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 34 tracked files — 16× `.xml`, 15× `.kt`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**15 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `AddEditCardScreen`, `CardDetailScreen`, `CardsScreen`

ViewModels: `AddEditCardViewModel`, `CardDetailViewModel`, `CardsScreenViewModel`

**Consumes:** `core/store/repository`

### Docs in the tree

| Path | |
|---|---|
| `feature/savedcards/README.md` | Saved Card Feature |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/savedcards sha=c201d5e10c8462f47f716e80ff9b765c9a3ae143 -->
## API reference

_Generated from `feature/savedcards` at tree `c201d5e10c84` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/CardsScreen.kt`

```kotlin
fun CardsScreen(
```
Known Issue, On deleting card, state isn't updating automatically whereas on adding or updating, card state is updating properly, This issue will be fixed soon.

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/CardsScreenViewModel.kt`

```kotlin
class CardsScreenViewModel(
```
ViewModel of the saved cards list screen; loads cards for the signed-in client and handles view, edit, add and delete actions.

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/CardsScreen.kt:77</code></summary>

```kotlin
    navigateToAddEdit: (CardAddEditType) -> Unit,
    navigateToViewDetail: (Long) -> Unit,
    viewModel: CardsScreenViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
```

</details>

```kotlin
data class CardState(
```
UI state of the saved cards screen.

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/CardsScreen.kt:119</code></summary>

```kotlin
@Composable
private fun SavedCardDialogs(
    dialogState: CardState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is CardState.DialogState.DeleteCard -> ForkBasicDialog(
```

</details>

```kotlin
sealed interface CardEvent
```
One-off events emitted by the saved cards ViewModel to the UI.

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/CardsScreen.kt:87</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is CardEvent.OnNavigateToAddEdit -> navigateToAddEdit.invoke(event.type)

            is CardEvent.OnNavigateToCardDetails -> navigateToViewDetail.invoke(event.cardId)

            is CardEvent.ShowToast -> {
```

</details>

```kotlin
sealed interface CardAction
```
User and internal actions handled by the saved cards ViewModel.

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/CardsScreen.kt:102</code></summary>

```kotlin
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(CardAction.DismissDialog) }
        },
    )

    CardsScreen(
```

</details>

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/components/CardConfig.kt`

```kotlin
object CardConfig
```
Shared visual constants for the card widgets.

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/components/CreditCard.kt:118</code></summary>

```kotlin
                androidx.compose.animation.AnimatedVisibility(
                    visible = card.cardType != CardType.UNKNOWN,
                    enter = CardConfig.logoEnterTransition,
                    exit = CardConfig.logoExitTransition,
                ) {
                    Image(
                        painter = painterResource(card.cardType.cardImage),
```

</details>

- `val logoSize: Dp = 60.dp` — Size of the card network logo.
- `val logoTint: Color? = null` — Optional tint applied to the card logo; null keeps original colors.
- `val logoEnterTransition: EnterTransition = fadeIn() + expandIn()` — Animation used when the card logo appears.
- `val logoExitTransition: ExitTransition = fadeOut() + shrinkOut()` — Animation used when the card logo disappears.
- `val dotRadius = 4.dp` — Radius of a masked-digit dot.
- `val spaceBetweenDots = 8.dp` — Horizontal gap between masked-digit dots.
- `val spaceBetweenGroups = 16.dp` — Horizontal gap between groups of card digits.

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/components/CardNumberFilter.kt`

```kotlin
class CreditCardVisualTransformation(
```
Visual transformation that formats a card number into groups for the given card type, optionally masking it.

```kotlin
fun getCardNumberTransformation(cardNumber: String, isMasked: Boolean): VisualTransformation
```
_No KDoc at source._

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/components/ColorUtils.kt`

```kotlin
fun Color.toHsl(): FloatArray
```
Converts this color to HSL, returned as a float array of hue, saturation and lightness.

```kotlin
fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color
```
Builds a `Color` from `hue` (degrees), `saturation` and `lightness` (0..1).

```kotlin
fun Color.setSaturation(newSaturation: Float): Color
```
Returns this color with its saturation replaced by `newSaturation`, keeping hue and lightness.

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/createOrUpdate/AddEditCardNavigation.kt`

```kotlin
data class CardAddEditArgs(
```
Arguments of the add/edit card screen, parsed from saved state.

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/createOrUpdate/AddEditCardViewModel.kt:48</code></summary>

```kotlin
    initialState = savedStateHandle.getSerialized(ADD_EDIT_CARD_STATE_KEY) ?: run {
        val clientId = requireNotNull(userRepository.clientId.value)
        val type = CardAddEditArgs(savedStateHandle).cardAddEditType

        AECardState(
            clientId = clientId,
            type = type,
```

</details>

```kotlin
fun NavGraphBuilder.addEditCardScreen(
```
Registers the add/edit card screen in the navigation graph.

```kotlin
fun NavController.navigateToCardAddEdit(
```
Navigates to the add/edit card screen for the given `addEditType`.

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/createOrUpdate/AddEditCardScreen.kt`

```kotlin
fun CreditCardColorSelector(
```
Horizontally scrolling row of color swatches for choosing a card color.

```kotlin
fun ColorSelector(
```
A single selectable color swatch, outlined when `isSelected`.

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/createOrUpdate/CardAddEditType.kt`

```kotlin
sealed class CardAddEditType
```
Mode of the add/edit card screen.

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/CardsScreen.kt:75</code></summary>

```kotlin
fun CardsScreen(
    modifier: Modifier = Modifier,
    navigateToAddEdit: (CardAddEditType) -> Unit,
    navigateToViewDetail: (Long) -> Unit,
    viewModel: CardsScreenViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
```

</details>

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/details/CardDetailNavigation.kt`

```kotlin
fun NavGraphBuilder.cardDetailRoute(
```
Registers the card details screen in the navigation graph.

```kotlin
fun NavController.navigateToCardDetails(cardId: Long)
```
Navigates to the details of the card with id `cardId`.

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/di/SavedCardsModule.kt`

```kotlin
val SavedCardsModule = module
```
Koin module providing the ViewModels of the saved cards feature.

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/utils/CreditCardUtils.kt`

```kotlin
object CreditCardUtils
```
Card number, CVV and expiry helpers: detection, Luhn validation, formatting and masking.

- `fun String.detectCardType(): CardType = CardType.detectCardType(this)`
- `fun String.isValidCreditCardNumber(): Boolean`
- `val cleanNumber = this.replace("\\s".toRegex(), "")`
- `val cardType = detectCardType()`
- `fun String.isValidCVV(cardType: CardType): Boolean`
- `val cleanCVV = this.replace("\\s".toRegex(), "")`
- `fun parse(input: String): ExpiryDate?` — Parses `MM/YY` or `MM/YYYY` into an `ExpiryDate`, or returns null if the format is invalid.
- `val (month, year) = input.split("/")`
- `val (month, year) = input.split("/")`
- `fun isValid(): Boolean` — Returns true if the month is valid and the card has not expired yet (one month of grace).
- `val now = Clock.System.now()`
- `val expiryDate = LocalDate(`
- `fun String.isValidExpiryDate(): Boolean`
- `val expiryDate = ExpiryDate.parse(this) ?: return false`
  _…more members; read the file._

```kotlin
enum class CardMaskStyle
```
How much of a card number is revealed when displayed.

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/components/CreditCard.kt:75</code></summary>

```kotlin
            AnimatedCardNumberInput(
                cardNumber = card.cardNumber,
                maskStyle = CardMaskStyle.SHOW_ALL,
            )
            // Positioned to corner top left
            SpaceWrapper(
                modifier = Modifier.align(Alignment.TopStart),
```

</details>

```kotlin
enum class CardType(
```
Supported card networks with their number rules.

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/CardsScreen.kt:265</code></summary>

```kotlin
            },
            leadingContent = {
                val cardImage = savedCard.cardNumber.detectCardType().cardImage

                Box(
                    modifier = Modifier
                        .size(48.dp),
```

</details>

```kotlin
val creditCardColors: List<Color> = listOf(
```
Palette of colors a user can pick for a saved card.

---

_11 type(s), 34 function(s)/property(ies); 32 carry KDoc at source; 0 authored example(s); 9 live call site(s)._
<!-- api-docs:end -->
