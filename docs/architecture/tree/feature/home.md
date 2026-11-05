# `feature/home/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 25 tracked files — 11× `.xml`, 9× `.kt`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**9 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `HomeScreen`

ViewModels: `HomeViewModel`

### Docs in the tree

| Path | |
|---|---|
| `feature/home/README.md` | :feature:home |

<!-- tree-scaffold:end -->
## Significance

The dashboard, and the **4-way `combineScreenStates` fan-in**: loans summary, upcoming bills, rates and
an exchange rate, each an independent card with its own loading, empty, error and content state.

It is also a **fork seam**. `HomeDashboard` is public rather than internal because `cmp-navigation`'s
`ForkBackboneRegistry.homeBody` renders it as the default home body, and it lives in the fork-owned `demo/`
package so `--clean` removes it together with that default. A fork replaces the body without touching
the shell.

<!-- api-docs:begin module=feature/home sha=3ac194aca58dc81ee260375944e1a72268fea115 -->
## API reference

_Generated from `feature/home` at tree `3ac194aca58d` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/home/src/commonMain/kotlin/kpt/feature/home/HomeDestination.kt`

```kotlin
data object HomeDestination
```
_No KDoc at source._

```kotlin
data object HomeRoute
```
_No KDoc at source._

```kotlin
fun NavController.navigateToHome(navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.homeGraph(
```
The backbone home graph. `homeBody` is the fork-owned home content (default supplied by `cmp-navigation`'s `ForkBackboneRegistry.homeBody`); this template graph carries zero demo imports.

### `feature/home/src/commonMain/kotlin/kpt/feature/home/di/HomeModule.kt`

```kotlin
val HomeModule = module {}
```
The backbone home module — `kpt.feature.home.HomeScreen` carries zero demo imports and takes no ViewModel of its own (the `homeBody` seam is supplied by `cmp-navigation`'s `ForkBackboneRegistry.homeBody`, whose own definitions live wherever that body's real implementation is installed). Empty on purpose.

### `feature/home/src/commonMain/kotlin/kpt/feature/home/ui/TestTags.kt`

```kotlin
object TestTags
```
Test-tag registry for the home / bottom-nav dashboard. Consumed by: - `HomeScreen` via `Modifier.testTag(...)`.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/HomeScreen.kt:54</code></summary>

```kotlin
) {
    Scaffold(
        modifier = Modifier.testTag(TestTags.Home.SCREEN),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
```

</details>

- `const val SCREEN: String = "home.screen"` — Root `Scaffold` surface of `HomeScreen` — always rendered regardless of dashboard loading state. Stable target for Compose UI tests.
- `const val DASHBOARD_SCROLL: String = "home_dashboard_scroll"` — The vertically-scrollable dashboard `Column`.

### `feature/home/src/commonMain/kotlin/org/mifospay/feature/home/HomeScreen.kt`

```kotlin
fun HomeScreenContent(
```
_No KDoc at source._

```kotlin
fun CardDropdownBox(
```
_No KDoc at source._

### `feature/home/src/commonMain/kotlin/org/mifospay/feature/home/HomeViewModel.kt`

```kotlin
class HomeViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/org/mifospay/feature/home/HomeScreen.kt:153</code></summary>

```kotlin
    navigateToHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.getAccounts()
    }
```

</details>

```kotlin
data class HomeState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/org/mifospay/feature/home/HomeScreen.kt:214</code></summary>

```kotlin
@Composable
fun HomeScreenContent(
    uiState: HomeState,
    viewState: ViewState,
    defaultAccountId: Long?,
    snackbarHostState: SnackbarHostState,
    isRefreshing: Boolean = false,
```

</details>

```kotlin
sealed interface ViewState
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountViewModel.kt:82</code></summary>

```kotlin
        .mapLatest { screenState ->
            when (screenState) {
                is ScreenState.Loading -> AccountState.ViewState.Loading

                is ScreenState.Empty ->
                    // Empty account-list branch → render an empty Content
                    // (screen still shows the FAB + empty-state copy; no
```

</details>

```kotlin
sealed interface HomeEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/org/mifospay/feature/home/HomeScreen.kt:167</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is HomeEvent.NavigateBack -> onNavigateBack()
            is HomeEvent.NavigateToRequestScreen -> onRequest(event.vpa)
            is HomeEvent.NavigateToSendScreen -> onPay()
            is HomeEvent.NavigateToAutoPayScreen -> onAutoPay.invoke()
            is HomeEvent.NavigateToPocketDashboard -> navigateToPocketDashboard.invoke()
```

</details>

```kotlin
sealed interface HomeAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/org/mifospay/feature/home/HomeScreen.kt:193</code></summary>

```kotlin
        dialogState = homeUIState.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(HomeAction.OnDismissDialog) }
        },
    )

    HomeScreenContent(
```

</details>

### `feature/home/src/commonMain/kotlin/org/mifospay/feature/home/di/HomeModule.kt`

```kotlin
val MifosHomeModule = module
```
_No KDoc at source._

### `feature/home/src/commonMain/kotlin/org/mifospay/feature/home/navigation/HomeNavigation.kt`

```kotlin
fun NavController.navigateToHome(navOptions: NavOptions? = null) = navigate(HOME_ROUTE, navOptions)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.homeScreen(
```
_No KDoc at source._

---

_8 type(s), 10 function(s)/property(ies); 5 carry KDoc at source; 0 authored example(s); 6 live call site(s)._
<!-- api-docs:end -->
