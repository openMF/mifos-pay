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

<!-- api-docs:begin module=feature/savedcards sha=2106b7782965aa6b20155fdb0f74655de16e6008 -->
## API reference

_Generated from `feature/savedcards` at tree `2106b7782965` by `scripts/docs/api-docs-gen.sh`._
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
_No KDoc at source._

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/CardsScreen.kt:119</code></summary>

```kotlin
@Composable
private fun SavedCardDialogs(
    dialogState: CardState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is CardState.DialogState.DeleteCard -> MifosBasicDialog(
```

</details>

```kotlin
sealed interface CardEvent
```
_No KDoc at source._

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
_No KDoc at source._

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
_No KDoc at source._

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

- `val logoSize: Dp = 60.dp`
- `val logoTint: Color? = null`
- `val logoEnterTransition: EnterTransition = fadeIn() + expandIn()`
- `val logoExitTransition: ExitTransition = fadeOut() + shrinkOut()`
- `val dotRadius = 4.dp`
- `val spaceBetweenDots = 8.dp`
- `val spaceBetweenGroups = 16.dp`

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/components/CardNumberFilter.kt`

```kotlin
class CreditCardVisualTransformation(
```
_No KDoc at source._

```kotlin
fun getCardNumberTransformation(cardNumber: String, isMasked: Boolean): VisualTransformation
```
_No KDoc at source._

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/components/ColorUtils.kt`

```kotlin
fun Color.toHsl(): FloatArray
```
_No KDoc at source._

```kotlin
fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color
```
_No KDoc at source._

```kotlin
fun Color.setSaturation(newSaturation: Float): Color
```
_No KDoc at source._

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/createOrUpdate/AddEditCardNavigation.kt`

```kotlin
data class CardAddEditArgs(
```
_No KDoc at source._

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
_No KDoc at source._

```kotlin
fun NavController.navigateToCardAddEdit(
```
_No KDoc at source._

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/createOrUpdate/AddEditCardScreen.kt`

```kotlin
fun CreditCardColorSelector(
```
_No KDoc at source._

```kotlin
fun ColorSelector(
```
_No KDoc at source._

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/createOrUpdate/CardAddEditType.kt`

```kotlin
sealed class CardAddEditType
```
_No KDoc at source._

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
_No KDoc at source._

```kotlin
fun NavController.navigateToCardDetails(cardId: Long)
```
_No KDoc at source._

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/di/SavedCardsModule.kt`

```kotlin
val SavedCardsModule = module
```
_No KDoc at source._

### `feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/utils/CreditCardUtils.kt`

```kotlin
object CreditCardUtils
```
_No KDoc at source._

- `fun String.detectCardType(): CardType = CardType.detectCardType(this)`
- `fun String.isValidCreditCardNumber(): Boolean`
- `val cleanNumber = this.replace("\\s".toRegex(), "")`
- `val cardType = detectCardType()`
- `fun String.isValidCVV(cardType: CardType): Boolean`
- `val cleanCVV = this.replace("\\s".toRegex(), "")`
- `fun parse(input: String): ExpiryDate?`
- `val (month, year) = input.split("/")`
- `val (month, year) = input.split("/")`
- `fun isValid(): Boolean`
- `val now = Clock.System.now()`
- `val expiryDate = LocalDate(`
- `fun String.isValidExpiryDate(): Boolean`
- `val expiryDate = ExpiryDate.parse(this) ?: return false`
  _…more members; read the file._

```kotlin
enum class CardMaskStyle
```
_No KDoc at source._

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
_No KDoc at source._

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
_No KDoc at source._

---

_11 type(s), 34 function(s)/property(ies); 1 carry KDoc at source; 0 authored example(s); 9 live call site(s)._
<!-- api-docs:end -->
