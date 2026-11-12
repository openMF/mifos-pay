# NetworkMonitor contract — operator guide

`cmp-network-monitor` (from MobileByteLabs KmpToolkit, v3.3.1+) is the canonical
network-state library this template integrates against. The template wraps
it through `kpt.core.base.data.infra.NetworkMonitor` (a typealias) and the
contract is documented in `kpt.core.base.data.infra.NetworkMonitorContract`.

## Contract invariants (enforced via NetworkMonitorContractTest)

| # | Invariant | Why it matters |
|---|---|---|
| 1 | `isOnline` is `StateFlow<Boolean>` | Subscribers always have a current value — no first-emission flicker |
| 2 | Initial value reflects platform state (sync query) or `true` (async fallback) | Screen opened while offline doesn't flicker through Loading → NoNetwork |
| 3 | `networkStatus` is `StateFlow<NetworkStatus>` (Available / Unavailable / CaptivePortal) | Captive-portal detection in addition to online/offline |
| 4 | OS-level debounce < 100ms; consumer-side debounce layered on top | Transient flaps suppressed; consumer tunes per-call |
| 5 | Cancellation-safe | Subscription cleanup releases OS resources |
| 6 | Hot-StateFlow variants (`isOnlineDebouncedState`, `networkStatusDebouncedState`) added in v3.3.0+ | Late subscribers see current value immediately |
| 7 | `NetworkMonitorProvider.version: StateFlow<Int>` generation counter | `rememberNetworkMonitor()` drops stale closed references after `reset()` |

Forks substituting their own NetworkMonitor MUST run `NetworkMonitorContractTest`
against their impl to verify compliance.

## v3.3.1 highlights (vs 3.2.x)

| Change | Impact |
|---|---|
| **M-001 / M-002 race fixes** (Android cold-start callback drops, JS/WasmJs close races) | Transparent — no API change |
| **New hot-StateFlow variants** (`isOnlineDebouncedState(scope, ms)`, `networkStatusDebouncedState(scope, ms)`) | Replaces cold-Flow `isOnlineDebounced` use where consumers want immediate seed-value semantics |
| **Compose helpers** gained optional `debounceMs: Long = 0L` parameter | `ConnectivityBanner`, `NetworkAwareContent`, `collectIsOnlineAsState`, `collectNetworkStatusAsState`, `collectNetworkQualityAsState` |
| **`NetworkMonitorProvider.version`** generation counter | `rememberNetworkMonitor()` correctly re-acquires after `reset()` |
| **`ProvideNetworkMonitor(monitor, content)`** CompositionLocal helper | Scope a monitor to a Compose subtree (testing, multi-instance) |

## Per-Store debounce tuning

Default 300ms reconnect debounce works for most apps. Override per stream via
`ScreenDataStream.asScreenStream(reconnectDebounceMs = ...)`:

```kotlin
store.asScreenStream(
    key = "rates",
    fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
    reconnectDebounceMs = 1_000L,     // long window for slow networks
    userRefreshDebounceMs = 500L,     // tighter user-tap window
    scope = viewModelScope,
)
```

| Scenario | reconnectDebounceMs |
|---|---|
| **Fast networks** (5G, fibre) | `100L` — refresh aggressively |
| **Default** | `300L` — sensible balance |
| **Unreliable networks** (cellular roaming, weak WiFi) | `2_000L` — avoid thrashing on flaps |
| **No-network screens** | `0L` (or use `FetchPolicy.CACHE_ONLY` which skips entirely) |

User-tap refresh debounce (defaults to 1s) protects against pull-to-refresh
spam. Tune via `userRefreshDebounceMs`; pass `0L` to disable.

## Captive-portal-aware retry (OfflineSubmitSyncer)

`OfflineSubmitSyncer` accepts a `RetryOnNetworkStatus` policy:

```kotlin
OfflineSubmitSyncer(
    scope = appScope,
    outbox = roomOutbox,
    networkStatusFlow = networkMonitor.networkStatus,
    submitBlock = { payload -> api.submit(payload) },
    retryOnStatus = RetryOnNetworkStatus.OnlineOnly,   // default
)
```

| Policy | Retries when status is… |
|---|---|
| `OnlineOnly` (default) | `Available` only |
| `OnlineOrCaptivePortal` | `Available` OR `CaptivePortal` — assumes portal already signed in |

Pick `OnlineOrCaptivePortal` when your fork's API is verifiably reachable
through captive-portal connections (e.g. corporate WiFi after sign-in).
Wrong choice burns the user's outbox on a closed portal.

## Refresh-signal & fetch dedup (Store5 — configuration-dependent)

N subscribers to the same Store key each receive their OWN reconnect-signal
(one per `ScreenDataStream` instance). Whether the underlying network fetch
is shared depends on **how each Store is configured**:

| Store config | Concurrent-subscriber fetch behavior |
|---|---|
| `StoreBuilder.from(fetcher).build()` (no SoT, no cachePolicy) | N subscribers → N fetcher invocations — no dedup |
| `StoreBuilder.from(fetcher).cachePolicy(MemoryPolicy.builder()...)` | First subscriber fires; subsequent ones hit the in-memory cache (TTL-bounded) |
| `StoreBuilder.from(fetcher, sourceOfTruth(...))` (Room / DataStore SoT) | All subscribers read through the SoT; fetcher fires only on cache miss / staleness |

For home-dashboard-style screens where 4 cards read the same Store, configure
that specific Store with a `cachePolicy` or `sourceOfTruth` to get fetch dedup.
Plain in-memory Stores (typical for showcase / preview / one-shot screens) fire
the fetcher per subscriber.

## Network-transition telemetry

`NetworkTelemetry` bridges `NetworkMonitor.networkStatus` transitions to
`AnalyticsHelper` as named events:

```kotlin
// Koin DI
single { NetworkTelemetry(get(), get()).also { it.start(get()) } }
```

Emits:
- `network.transition.online_to_offline`
- `network.transition.offline_to_online`
- `network.transition.captive_portal_detected`
- `network.transition.captive_portal_resolved`

Useful for diagnosing reports of "app doesn't refresh after I reconnect" — the
event stream confirms whether the OS-level transition reached the app.

## Related files

| File | Purpose |
|---|---|
| `core-base/data/.../infra/NetworkMonitor.kt` | Framework typealias to cmp-network-monitor's NetworkMonitor |
| `core-base/data/.../infra/NetworkMonitorContract.kt` | Documented contract + canonical debounce defaults |
| `core/data/.../infra/NetworkMonitorContractTest.kt` | Contract-verification test (runs against installed impl) |
| `core-base/store/.../screen/ScreenDataStream.kt` | Consumer-side fusion of Store + NetworkMonitor |
| `core-base/store/.../submit/OfflineSubmitSyncer.kt` | Outbox retry-on-reconnect |
| `core-base/store/.../submit/RetryOnNetworkStatus.kt` | OnlineOnly / OnlineOrCaptivePortal policies |
| `core/firebase/.../analytics/` | Transition → AnalyticsHelper bridge — a fork adds it here; there is no `core/analytics` module |
