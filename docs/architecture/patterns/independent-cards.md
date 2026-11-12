# Pattern: Independent Cards (Multi-Card Dashboard)

A dashboard screen showing N cards where each card carries its own `ScreenState`
— so a slow card can still spin while a fast one shows content, and a single
failed fetch ruins exactly one card instead of blanking the whole dashboard.

---

## What

**Use this pattern when:** your screen renders ≥ 2 distinct, independently-fetched
data sources side-by-side (or stacked vertically) and the failure of one shouldn't
hide the success of the other.

**Anti-pattern:** combining all sources into one `combineScreenStates(...)` and
rendering the whole grid via a single `ScreenContent`. That works for tightly
coupled data (e.g., "user profile + their open positions" — one without the
other is meaningless), but for true dashboards (Loans + Bills + Rates + FX) it
amplifies failure: if FX rate-limits, the user sees zero loans.

**Building blocks (`core-base/ui/dashboard/`):**
- `DashboardProgressState` — value class (loaded / total / isAnyLoading /
  hasAnyError / hasAnyEmpty) suitable for top-of-screen rollup display.
- `aggregateDashboardProgress()` — `Flow<List<ScreenState<*>>>` extension that
  rolls per-card states into one `DashboardProgressState` snapshot.
- `DashboardProgressBar` — Material 3 linear progress + "X of Y loaded" label;
  auto-hides when `total == 0` or `loaded == total`.
- `IndependentCardLayout` — vertical stack of cards, each wrapped in
  `ScreenContent` with its own retry + optional dismiss handler.

---

## How

### 1. ViewModel — expose a list of per-card streams

```kotlin
class HomeViewModel(
    loansStream: ScreenDataStream<List<Loan>>,
    billsStream: ScreenDataStream<List<BillReminder>>,
    ratesStream: ScreenDataStream<RateSnapshot>,
    fxStream: ScreenDataStream<FxSnapshot>,
) : ViewModel() {

    /** Order = visual order of cards on the dashboard. */
    val cards: StateFlow<List<ScreenState<*>>> = combine(
        loansStream.state,
        billsStream.state,
        ratesStream.state,
        fxStream.state,
    ) { l, b, r, f -> listOf(l, b, r, f) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val progress: StateFlow<DashboardProgressState> = cards
        .aggregateDashboardProgress()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DashboardProgressState(0, 0, false, false, false),
        )

    fun onRetryCard(index: Int) {
        when (index) {
            0 -> loansStream.refresh()
            1 -> billsStream.refresh()
            2 -> ratesStream.refresh()
            3 -> fxStream.refresh()
        }
    }
}
```

### 2. Screen — consume IndependentCardLayout

```kotlin
@Composable
fun HomeScreen(vm: HomeViewModel = koinViewModel()) {
    val cards by vm.cards.collectAsStateWithLifecycle()
    val progress by vm.progress.collectAsStateWithLifecycle()

    Column {
        DashboardProgressBar(state = progress)

        IndependentCardLayout(
            states = cards,
            onRetry = vm::onRetryCard,
        ) { index, data, freshness ->
            when (index) {
                0 -> LoansCard(data as List<Loan>, freshness)
                1 -> BillsCard(data as List<BillReminder>, freshness)
                2 -> RatesCard(data as RateSnapshot, freshness)
                3 -> FxCard(data as FxSnapshot, freshness)
            }
        }
    }
}
```

The `index` parameter is the contract that ties card slot → card content. Keep
the order between `cards`, `onRetryCard`, and the per-index render branch in
sync — extracting a typed sealed `CardSlot` enum is a common next refactor once
you have ≥ 5 cards.

---

## Refresh semantics

- **Per-card refresh:** every card's `onRetry` only refreshes that one card's
  Store. The other cards are not touched.
- **"Refresh all" affordance:** if you expose a screen-level pull-to-refresh,
  iterate `cards.indices.forEach { vm.onRetryCard(it) }`. Cards backed by
  `CACHE_ONLY` will re-emit instantly (they never hit the network); cards
  backed by `NETWORK_WITH_CACHE` or `NETWORK_ONLY` will fan out parallel
  network calls.
- **Stale-while-revalidate:** each card exposes a `FreshnessIndicator(signal, onRefresh)`
  driven by `ScreenDataStream.freshness` — a pure time-based staleness signal
  (`FreshnessBand.Initial / Fresh / Stale / VeryStale`) decoupled from
  `NetworkMonitor`. Network connectivity is rendered separately by the global
  `ConnectivityBanner`. The card never blanks back to `Loading` once it has cached
  content (handled by `ScreenDataStream`'s state-machine — no extra wiring needed
  at the screen level).

---

## Paged + non-paged mix

A dashboard can mix `PagingScreenContent` (infinite-scroll list) with non-paged
cards in the same `IndependentCardLayout`. The trick is that `PagingScreenContent`
needs its own internal `LazyColumn`, so wrap that branch in a bounded-height
container:

```kotlin
IndependentCardLayout(states = cards, onRetry = vm::onRetryCard) { index, data, freshness ->
    when (index) {
        0 -> Box(modifier = Modifier.height(320.dp)) {
            // Bounded so the inner LazyColumn doesn't try to be infinite-tall.
            PagingTransactionsCard(stream = vm.transactionsPagingStream)
        }
        1 -> AccountSummaryCard(data as AccountSummary, freshness)
        // ...
    }
}
```

If the paged list is the dominant card, consider promoting it to the outer
`LazyColumn` and rendering the other cards as `item { }` slots above —
that's a different layout but the per-card `ScreenContent` discipline still
applies inside each `item { }`.

---

## Related patterns

- **`ScreenContent`** (`core-base/ui/screen/`) — the single-card primitive each
  IndependentCardLayout slot wraps. Drop in here directly if your screen is a
  single-card detail page rather than a dashboard.
- **`combineScreenStates`** (`core-base/store/screen/`) — opposite trade-off:
  fan-in multiple streams into ONE `ScreenState`. Use when the data sources
  are conceptually one screen, not N cards.
- **`MutationScreenContent`** + `SubmitHandler` — for input screens (forms,
  wizards). Not a dashboard pattern, but commonly sits beside a dashboard
  (tap a card → navigate to a `MutationScreenContent` edit form).
- **`PagingScreenContent`** — infinite-scroll list primitive. Embeds inside an
  `IndependentCardLayout` slot per the recipe above.

For the full screen-archetype taxonomy table, see [core/store/README.md](README.md).

---

## Reference implementation

The demo `Home` dashboard (`feature/home/`) is the canonical example
once it's refactored onto this pattern — see Phase 9-10 of the
`core-base-store-coverage` epic for the worked migration. Until then, the
framework primitives (`IndependentCardLayout`, `DashboardProgressBar`,
`aggregateDashboardProgress`) are usable on their own — opt in per screen.
