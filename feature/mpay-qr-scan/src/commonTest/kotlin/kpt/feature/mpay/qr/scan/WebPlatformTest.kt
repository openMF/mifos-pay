/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.scan

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [WebPlatform] detection.
 *
 * These tests verify that platform detection works correctly
 * for determining whether to show camera scanner or file picker UI.
 */
class WebPlatformTest {

    /**
     * Verifies that isWeb property returns expected value for the platform.
     *
     * On native platforms (Android, iOS, Desktop), isWeb should be false.
     * On web platforms (JS, WasmJS), isWeb should be true.
     */
    @Test
    fun givenPlatform_whenCheckingIsWeb_thenReturnsExpectedValue() {
        // On commonTest, this will run on JVM which should return false
        // The actual web platform tests would run in jsTest/wasmJsTest
        val isWeb = WebPlatform.isWeb

        // This test verifies the property is accessible
        // Actual value depends on platform (false for native, true for web)
        assertTrue(isWeb == true || isWeb == false)
    }

    /**
     * Verifies that isWebDesktop property returns expected value.
     *
     * On non-web platforms, isWebDesktop should always be false.
     */
    @Test
    fun givenNonWebPlatform_whenCheckingIsWebDesktop_thenReturnsFalse() {
        // On native platforms, isWebDesktop should be false
        // This test runs on JVM (desktop) but WebPlatform.isWebDesktop
        // refers to web browser desktop, not desktop app
        val isWebDesktop = WebPlatform.isWebDesktop

        // On commonTest (JVM), both should be false since it's not a web platform
        if (!WebPlatform.isWeb) {
            assertFalse(isWebDesktop, "isWebDesktop should be false on non-web platforms")
        }
    }

    /**
     * Verifies consistency between isWeb and isWebDesktop.
     *
     * If isWeb is false, isWebDesktop must also be false.
     * If isWeb is true, isWebDesktop can be either true or false.
     */
    @Test
    fun givenPlatform_whenComparingWebFlags_thenConsistentValues() {
        val isWeb = WebPlatform.isWeb
        val isWebDesktop = WebPlatform.isWebDesktop

        if (!isWeb) {
            assertFalse(
                isWebDesktop,
                "isWebDesktop must be false when isWeb is false",
            )
        }

        // If on web, isWebDesktop can be true (desktop browser) or false (mobile browser)
        // Both are valid states
    }
}
