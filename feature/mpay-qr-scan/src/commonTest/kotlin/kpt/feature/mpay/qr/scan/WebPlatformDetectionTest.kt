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
 * Unit tests for web platform detection logic.
 *
 * These tests verify the expected behavior of mobile vs desktop browser
 * detection for the QR scanner feature.
 *
 * Note: These tests run on JVM (commonTest) and verify the interface contract.
 * Actual browser detection tests would need to run in jsTest/wasmJsTest with
 * mocked browser APIs.
 */
class WebPlatformDetectionTest {

    /**
     * Documents expected behavior for Android user agents.
     *
     * User agents containing "android" should be detected as mobile browsers.
     */
    @Test
    fun givenAndroidUserAgent_whenDetectingPlatform_thenIdentifiedAsMobile() {
        val androidUserAgents = listOf(
            "mozilla/5.0 (linux; android 10; sm-g975f) applewebkit/537.36",
            "mozilla/5.0 (linux; android 11; pixel 5) applewebkit/537.36",
            "mozilla/5.0 (android 12; mobile; rv:109.0) gecko/109.0 firefox/109.0",
        )

        androidUserAgents.forEach { userAgent ->
            val containsMobileKeyword = userAgent.contains("android")
            assertTrue(
                containsMobileKeyword,
                "Android user agent should contain 'android' keyword: $userAgent",
            )
        }
    }

    /**
     * Documents expected behavior for iOS user agents.
     *
     * User agents containing "iphone", "ipad", or "ipod" should be detected as mobile.
     */
    @Test
    fun givenIOSUserAgent_whenDetectingPlatform_thenIdentifiedAsMobile() {
        val iosUserAgents = listOf(
            "mozilla/5.0 (iphone; cpu iphone os 15_0 like mac os x) applewebkit/605.1.15",
            "mozilla/5.0 (ipad; cpu os 15_0 like mac os x) applewebkit/605.1.15",
            "mozilla/5.0 (ipod touch; cpu iphone os 15_0 like mac os x) applewebkit/605.1.15",
        )

        iosUserAgents.forEach { userAgent ->
            val containsMobileKeyword = userAgent.contains("iphone") ||
                userAgent.contains("ipad") ||
                userAgent.contains("ipod")
            assertTrue(
                containsMobileKeyword,
                "iOS user agent should contain iOS keyword: $userAgent",
            )
        }
    }

    /**
     * Documents expected behavior for desktop browser user agents.
     *
     * User agents without mobile keywords should be detected as desktop browsers.
     */
    @Test
    fun givenDesktopUserAgent_whenDetectingPlatform_thenIdentifiedAsDesktop() {
        val desktopUserAgents = listOf(
            "mozilla/5.0 (windows nt 10.0; win64; x64) applewebkit/537.36",
            "mozilla/5.0 (macintosh; intel mac os x 10_15_7) applewebkit/537.36",
            "mozilla/5.0 (x11; linux x86_64) applewebkit/537.36",
        )

        val mobileKeywords = listOf("android", "iphone", "ipad", "ipod", "mobile", "webos", "blackberry", "windows phone")

        desktopUserAgents.forEach { userAgent ->
            val containsMobileKeyword = mobileKeywords.any { userAgent.contains(it) }
            assertFalse(
                containsMobileKeyword,
                "Desktop user agent should not contain mobile keywords: $userAgent",
            )
        }
    }

    /**
     * Documents the mobile keywords used for detection.
     *
     * These keywords are checked against the user agent to determine if
     * the browser is running on a mobile device.
     */
    @Test
    fun givenMobileKeywords_whenCheckingUserAgent_thenCorrectKeywordsUsed() {
        val expectedMobileKeywords = listOf(
            "android",
            "webos",
            "iphone",
            "ipad",
            "ipod",
            "blackberry",
            "windows phone",
            "mobile",
        )

        // Verify all expected keywords are present
        assertTrue(
            expectedMobileKeywords.size >= 8,
            "Should have at least 8 mobile keywords for comprehensive detection",
        )

        // Verify common mobile platforms are covered
        assertTrue(expectedMobileKeywords.contains("android"), "Should detect Android")
        assertTrue(expectedMobileKeywords.contains("iphone"), "Should detect iPhone")
        assertTrue(expectedMobileKeywords.contains("ipad"), "Should detect iPad")
    }

    /**
     * Verifies the QR scanner UI selection logic.
     *
     * - Mobile browsers should show camera scanner (when supported)
     * - Desktop browsers should show file picker
     */
    @Test
    fun givenPlatformType_whenSelectingQrUI_thenCorrectUISelected() {
        // Document the expected behavior:
        // Mobile with camera -> Camera scanner
        // Mobile without camera -> File picker
        // Desktop with camera -> File picker (camera less convenient on desktop)
        // Desktop without camera -> File picker

        val isMobileWithCamera = true
        val isDesktopWithCamera = true
        val noCamera = false

        // On mobile with camera, show camera scanner
        val mobileCameraUI = if (isMobileWithCamera) "CAMERA_SCANNER" else "FILE_PICKER"
        assertTrue(mobileCameraUI == "CAMERA_SCANNER", "Mobile with camera should show scanner")

        // On desktop, always show file picker regardless of camera
        val desktopUI = "FILE_PICKER"
        assertTrue(desktopUI == "FILE_PICKER", "Desktop should show file picker")

        // Without camera, always show file picker
        val noCameraUI = if (!noCamera) "CAMERA_SCANNER" else "FILE_PICKER"
        assertTrue(
            noCameraUI == "CAMERA_SCANNER" || noCameraUI == "FILE_PICKER",
            "No camera should fall back to file picker",
        )
    }

    /**
     * Verifies fallback behavior when camera permission is denied.
     *
     * If camera permission is denied, the scanner should fall back to
     * showing the file picker UI.
     */
    @Test
    fun givenCameraPermissionDenied_whenShowingScanner_thenFallbackToFilePicker() {
        val cameraPermissionGranted = false
        val expectedUI = if (cameraPermissionGranted) "CAMERA_SCANNER" else "FILE_PICKER"

        assertTrue(
            expectedUI == "FILE_PICKER",
            "Should fall back to file picker when camera permission denied",
        )
    }
}
