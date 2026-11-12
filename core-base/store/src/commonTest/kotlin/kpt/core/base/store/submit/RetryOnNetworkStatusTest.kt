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

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Asserts the gating contract of [RetryOnNetworkStatus] — that OnlineOnly
 * (default) restricts retry to fully-online, and OnlineOrCaptivePortal
 * additionally allows retry through captive-portal connections.
 */
class RetryOnNetworkStatusTest {

    private val available = NetworkStatus.Available(
        NetworkInfo(type = NetworkType.WiFi, isMetered = false),
    )
    private val captivePortal = NetworkStatus.CaptivePortal(
        NetworkInfo(type = NetworkType.WiFi, isMetered = false),
    )
    private val unavailable = NetworkStatus.Unavailable

    @Test
    fun onlineOnly_retriesOnAvailable() {
        assertTrue(RetryOnNetworkStatus.OnlineOnly.shouldRetry(available))
    }

    @Test
    fun onlineOnly_doesNotRetryOnCaptivePortal() {
        assertFalse(RetryOnNetworkStatus.OnlineOnly.shouldRetry(captivePortal))
    }

    @Test
    fun onlineOnly_doesNotRetryOnUnavailable() {
        assertFalse(RetryOnNetworkStatus.OnlineOnly.shouldRetry(unavailable))
    }

    @Test
    fun onlineOrCaptivePortal_retriesOnAvailable() {
        assertTrue(RetryOnNetworkStatus.OnlineOrCaptivePortal.shouldRetry(available))
    }

    @Test
    fun onlineOrCaptivePortal_retriesOnCaptivePortal() {
        assertTrue(RetryOnNetworkStatus.OnlineOrCaptivePortal.shouldRetry(captivePortal))
    }

    @Test
    fun onlineOrCaptivePortal_doesNotRetryOnUnavailable() {
        assertFalse(RetryOnNetworkStatus.OnlineOrCaptivePortal.shouldRetry(unavailable))
    }
}
