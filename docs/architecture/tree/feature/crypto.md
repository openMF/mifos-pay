# `feature/crypto/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 36 tracked files — 20× `.xml`, 14× `.kt`, 1× `.md`, 1× `.kts`

**14 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `CoinDetailScreen`, `CoinMarketsScreen`

ViewModels: `CoinDetailViewModel`, `CoinMarketsViewModel`

**Consumes:** `core/store/crypto`

### Docs in the tree

| Path | |
|---|---|
| `feature/crypto/README.md` | :feature:crypto |

<!-- tree-scaffold:end -->
## Significance

CoinGecko market list and coin detail — the **paging** showcase over a real remote API.

`PagingScreenStream` accumulates pages, and the cached rows carry the `page` they arrived on so a
targeted refresh can evict one page rather than the whole list. The detail screen is reachable by deep
link, which is why `CoinDetail` carries the market rank: a detail opened cold must not need the list.

<!-- api-docs:begin module=feature/crypto sha=e15e64e71b34cfa7a075129d04da2214c1f91414 -->
## API reference

_Generated from `feature/crypto` at tree `e15e64e71b34` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/di/CryptoFeatureModule.kt`

```kotlin
val CryptoFeatureModule = module
```
Koin module for the crypto feature. Wired into the app graph by `cmp-navigation/.../KoinModules.kt` (`featureModule.includes(CryptoFeatureModule)`). The `CryptoRepository` binding is provided by `core/data/.../RepositoryModule.kt`.

### `feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/navigation/CryptoNavigation.kt`

```kotlin
data object CryptoGraphRoute
```
Root of the crypto feature graph. Navigated to via `navigateToCrypto`.

```kotlin
data object CoinMarketsListRoute
```
Nav destination for the CoinMarkets list.

```kotlin
data class CoinDetailRoute(val coinId: String)
```
Per-coin detail destination — the drill-down target for a tapped CoinMarkets row.

```kotlin
fun NavController.navigateToCrypto(navOptions: NavOptions? = null)
```
Convenience navigator so callers do not construct the route type by hand.

```kotlin
fun NavController.navigateToCoinDetail(coinId: String, navOptions: NavOptions? = null)
```
Navigates to one coin's detail.

```kotlin
fun NavGraphBuilder.cryptoGraph(navController: NavController)
```
Wires the crypto graph into the parent NavHost. Mirrors `RatesNavigation.ratesGraph` shape: a list destination whose row-tap navigates to a per-item detail destination inside the same `navigation<CryptoGraphRoute>` block.

### `feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinDetailViewModel.kt`

```kotlin
sealed interface CoinDetailAction
```
What the detail screen can be asked to do.

### `feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinMarketsRoute.kt`

```kotlin
fun CoinMarketsRoute(onBackClick: () -> Unit, onCoinClick: (String) -> Unit)
```
Thin nav-facing wrapper around `CoinMarketsScreen`.

<details><summary>Used in the template — <code>feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/navigation/CryptoNavigation.kt:82</code></summary>

```kotlin
    navigation<CryptoGraphRoute>(startDestination = CoinMarketsListRoute) {
        composableWithPushTransitions<CoinMarketsListRoute> {
            CoinMarketsRoute(
                onBackClick = { navController.popBackStackSafely() },
                onCoinClick = { coinId ->
                    navController.navigateToCoinDetail(coinId = coinId)
                },
```

</details>

### `feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinMarketsScreen.kt`

```kotlin
fun CoinMarketsScreen(
```
CoinMarkets — CoinGecko-backed paged list.

<details><summary>Used in the template — <code>feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinMarketsRoute.kt:22</code></summary>

```kotlin
@Composable
fun CoinMarketsRoute(onBackClick: () -> Unit, onCoinClick: (String) -> Unit) {
    CoinMarketsScreen(
        onBackClick = onBackClick,
        onCoinClick = onCoinClick,
    )
}
```

</details>

### `feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinMarketsViewModel.kt`

```kotlin
class CoinMarketsViewModel(
```
Read-side ViewModel for `CoinMarketsScreen`.

<details><summary>Used in the template — <code>feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/di/CryptoFeatureModule.kt:26</code></summary>

```kotlin
 */
val CryptoFeatureModule = module {
    viewModelOf(::CoinMarketsViewModel)

    // Detail screen — `coinId` comes from the nav route via
    // koinViewModel { parametersOf(coinId) }.
    viewModel { params ->
```

</details>

```kotlin
sealed interface CoinMarketsAction
```
Placeholder action hierarchy — the read-only screen has no dispatch surface today.

### `feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/TestTags.kt`

```kotlin
object TestTags
```
Test-tag registry for the crypto feature. Consumed by: - `CoinMarketsScreen` via `Modifier.testTag(...)`.

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

- `const val SCREEN: String = "coinMarkets.screen"` — Root `Scaffold` surface — always rendered regardless of paging state. Stable assertion target for Compose UI tests.
- `const val LIST: String = "coinMarkets.list"` — Scrollable `PagingScreenContent` LazyColumn.
- `const val ROW: String = "coinMarkets.row"` — Individual `CoinMarketRow` in the paging list.
- `const val DETAIL_SCREEN: String = "coinDetail.screen"` — Root `Scaffold` surface of `CoinDetailScreen` — the drill-down target for a tapped `ROW`. Stable assertion target regardless of stream state.

---

_7 type(s), 10 function(s)/property(ies); 17 carry KDoc at source; 0 authored example(s); 4 live call site(s)._
<!-- api-docs:end -->
