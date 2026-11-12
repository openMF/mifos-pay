/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.submit

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus

/**
 * Policy controlling when [OfflineSubmitSyncer] retries pending outbox entries.
 *
 * Default ([OnlineOnly]) preserves the pre-2026-05-27 behavior — retry only
 * when fully online. Forks whose API is reachable behind a captive portal
 * (e.g. corporate WiFi after sign-in) can opt into [OnlineOrCaptivePortal]
 * so submissions resume immediately after portal auth instead of waiting
 * for the portal to disappear.
 */
sealed interface RetryOnNetworkStatus {

    /** Returns true if [status] should trigger an outbox retry sweep. */
    fun shouldRetry(status: NetworkStatus): Boolean

    /** Retry only when [NetworkStatus.Available]. Default. */
    object OnlineOnly : RetryOnNetworkStatus {
        override fun shouldRetry(status: NetworkStatus): Boolean =
            status is NetworkStatus.Available
    }

    /**
     * Retry when online OR when on a captive portal (assumes portal already
     * signed in). Use when the consumer's API reachability through portal
     * connections is verified — otherwise retries will burn the user's
     * outbox on a closed portal.
     */
    object OnlineOrCaptivePortal : RetryOnNetworkStatus {
        override fun shouldRetry(status: NetworkStatus): Boolean =
            status is NetworkStatus.Available || status is NetworkStatus.CaptivePortal
    }
}
