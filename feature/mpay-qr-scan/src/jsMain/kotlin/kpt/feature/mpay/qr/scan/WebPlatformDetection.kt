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

import kotlinx.browser.window

/**
 * Enum representing the type of web platform detected.
 */
enum class WebPlatformType {
    /** Mobile browser with camera - show live camera scanner */
    MOBILE_WITH_CAMERA,

    /** Desktop browser with camera - show file picker (camera less convenient on desktop) */
    DESKTOP_WITH_CAMERA,

    /** No camera available - show file picker only */
    NO_CAMERA,
}

/**
 * Utility object for detecting web platform characteristics.
 */
object WebPlatformDetection {

    /**
     * Detects if the current browser is on a mobile device.
     *
     * Uses multiple signals:
     * - User agent string patterns
     * - Touch capability
     * - Screen size
     */
    fun isMobileBrowser(): Boolean {
        val userAgent = window.navigator.userAgent.lowercase()

        // Check user agent for mobile patterns
        val mobileUserAgentPatterns = listOf(
            "android",
            "webos",
            "iphone",
            "ipad",
            "ipod",
            "blackberry",
            "windows phone",
            "opera mini",
            "iemobile",
            "mobile",
        )

        val hasMobileUserAgent = mobileUserAgentPatterns.any { pattern ->
            userAgent.contains(pattern)
        }

        // Check for touch capability
        val hasTouchScreen = js("'ontouchstart' in window || navigator.maxTouchPoints > 0").unsafeCast<Boolean>()

        // Check screen size (typically mobile devices have smaller screens)
        val isSmallScreen = window.innerWidth <= 768

        // Consider mobile if user agent matches OR (has touch AND small screen)
        return hasMobileUserAgent || (hasTouchScreen && isSmallScreen)
    }

    /**
     * Checks if camera API (getUserMedia) is available in the browser.
     */
    fun hasCameraApi(): Boolean {
        return js(
            """
            typeof navigator !== 'undefined' &&
            typeof navigator.mediaDevices !== 'undefined' &&
            typeof navigator.mediaDevices.getUserMedia === 'function'
            """,
        ).unsafeCast<Boolean>()
    }

    /**
     * Determines the platform type for QR scanning.
     *
     * @return [WebPlatformType] indicating which scanner UI to show
     */
    fun detectPlatformType(): WebPlatformType {
        val hasCamera = hasCameraApi()
        val isMobile = isMobileBrowser()

        return when {
            !hasCamera -> WebPlatformType.NO_CAMERA
            isMobile -> WebPlatformType.MOBILE_WITH_CAMERA
            else -> WebPlatformType.DESKTOP_WITH_CAMERA
        }
    }
}
