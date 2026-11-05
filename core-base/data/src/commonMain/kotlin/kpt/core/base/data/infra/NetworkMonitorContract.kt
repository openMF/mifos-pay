/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.data.infra

/**
 * Framework contract for [NetworkMonitor] implementations. The bundled
 * `cmp-network-monitor` (v3.3.1+, from MobileByteLabs KmpToolkit) satisfies
 * this contract; forks substituting their own implementation MUST also
 * satisfy it.
 *
 * **Invariants:**
 *
 * 1. `isOnline` MUST be a `StateFlow<Boolean>` — subscribers always receive
 *    the current value immediately. SharedFlow / Flow without state will
 *    cause initial-render flicker.
 *
 * 2. The initial value of `isOnline` MUST reflect the platform's current
 *    state — typically obtained via a synchronous platform query at
 *    construction time (Android `ConnectivityManager.getActiveNetworkInfo()`,
 *    iOS `NWPathMonitor`, JS `navigator.onLine`). If the platform query is
 *    asynchronous, the initial value MUST be `true` (assume online; correct
 *    on first OS callback). On Android, the cold-start race that previously
 *    dropped early callbacks is fixed in v3.3.0+ (see cmp-network-monitor
 *    MIGRATION.md M-002).
 *
 * 3. `networkStatus` MUST be a `StateFlow<NetworkStatus>` with the same
 *    initial-value rule. `NetworkStatus.CaptivePortal` MUST be detectable
 *    on Android (via HTTP probe to a known endpoint) and iOS (via
 *    `NWPath.status == .satisfied` + captive-portal flag).
 *
 * 4. Emissions MUST be debounced at the OS callback level (transient flaps
 *    < 100ms suppressed). Additional consumer-side debounce (e.g. 300ms via
 *    `isOnlineDebounced(N)` cold-Flow or `isOnlineDebouncedState(scope, N)`
 *    hot-StateFlow) is layered on top.
 *
 * 5. Subscriptions MUST be cancellation-safe — cancelling the collector cleans
 *    up OS-level callbacks. JS/WasmJs close races fixed in v3.3.0+ (see
 *    cmp-network-monitor MIGRATION.md M-001).
 *
 * 6. Hot-StateFlow variants (v3.3.0+ additions): `isOnlineDebouncedState(scope, ms)`
 *    and `networkStatusDebouncedState(scope, ms)` return `StateFlow<T>` instead of
 *    cold `Flow<T>`. Late subscribers see the current value immediately. Use
 *    these in long-lived scopes (Application, AppViewModel) where the seed-value
 *    guarantee matters; use the cold variants in short-lived scopes.
 *
 * 7. `NetworkMonitorProvider.version: StateFlow<Int>` is a generation counter
 *    that increments on `install()` / `reset()`. `rememberNetworkMonitor()`
 *    keys `remember` on it, so composables drop stale closed references after
 *    test-time `NetworkMonitorProvider.reset()`. No-op resets don't bump the
 *    counter (no spurious recompositions).
 *
 * Forks substituting their own NetworkMonitor MUST run the
 * `NetworkMonitorContractTest` against their impl to verify these invariants.
 */
object NetworkMonitorContract {
    /** Minimum sane reconnect-debounce window (anything lower thrashes on flaps). */
    const val MIN_DEBOUNCE_MS: Long = 100L

    /** Default reconnect-debounce window — sensible balance for most apps. */
    const val DEFAULT_DEBOUNCE_MS: Long = 300L

    /** Upper bound — beyond this, the user perceives the app as unresponsive to network changes. */
    const val MAX_DEBOUNCE_MS: Long = 5_000L
}
