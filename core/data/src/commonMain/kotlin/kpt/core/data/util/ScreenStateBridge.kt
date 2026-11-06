/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kpt.core.base.store.screen.ScreenState as KptScreenState
import kpt.core.common.ScreenState as ForkScreenState
/**
 * Bridge extension: converts a single `kpt.core.base.store.screen.ScreenState<T>`
 * (emitted by Store5 `asScreenStream(...)`) into the fork's
 * `kpt.core.common.ScreenState<T>`.
 *
 * Migration rationale (Phase-3 → Phase-4): the fork has kept its own sealed
 * interface [ForkScreenState] as the read-side model exposed on every
 * repository interface (see fork ScreenState.kt for the "mirror" contract).
 * With Store5 landing, impls now compose Flow<KptScreenState<T>> via
 * `store.asScreenStream(...).state` — which must be lifted back to the fork
 * ScreenState to satisfy the interface signature.
 *
 * Kotlin does NOT support nested-type access through a typealias, so we could
 * not collapse the two ScreenState types with a single-line alias in
 * core/common (callers use `ScreenState.Content(...)` etc. which resolves
 * against the class identifier, not through aliases). This bridge is the
 * minimally-invasive alternative — one file, ~10 lines of variant mapping,
 * used only where Store5 impls meet fork interfaces.
 *
 * NOTE on `Content`: we drop the KPT `freshnessSignal` field on the fork side
 * (the fork Content variant lacks it by design — the fork FreshnessIndicator
 * ships with the Phase-4 UI cutover). `fetchedAt` is preserved.
 */
fun <T> KptScreenState<T>.toForkScreenState(): ForkScreenState<T> = when (this) {
    is KptScreenState.Loading -> ForkScreenState.Loading
    is KptScreenState.Empty -> ForkScreenState.Empty
    is KptScreenState.Unauthenticated -> ForkScreenState.Unauthenticated
    is KptScreenState.NoNetwork -> ForkScreenState.NoNetwork(isCaptivePortal = isCaptivePortal)
    is KptScreenState.Error -> ForkScreenState.Error(error = error, isNetworkError = isNetworkError)
    is KptScreenState.Content -> ForkScreenState.Content(data = data, fetchedAt = fetchedAt)
}

/**
 * Flow-level convenience wrapper — every Store5 impl calls
 * `store.asScreenStream(...).state.toForkScreenStateFlow()` to satisfy the
 * fork repository return type.
 */
fun <T> Flow<KptScreenState<T>>.toForkScreenStateFlow(): Flow<ForkScreenState<T>> =
    map { it.toForkScreenState() }
