/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:JsModule("html5-qrcode")
@file:JsNonModule

package kpt.feature.mpay.qr.scan

import kotlin.js.Promise

/**
 * JavaScript interop declarations for the html5-qrcode library.
 *
 * @see <a href="https://github.com/mebjas/html5-qrcode">html5-qrcode GitHub</a>
 */

/**
 * Html5Qrcode class for scanning QR codes from camera or files.
 */
external class Html5Qrcode(elementId: String) {
    /**
     * Start scanning using camera.
     *
     * @param cameraIdOrConfig Camera device ID or configuration object
     * @param config Scanning configuration
     * @param qrCodeSuccessCallback Called when QR is scanned successfully
     * @param qrCodeErrorCallback Called on scan error (optional)
     */
    fun start(
        cameraIdOrConfig: dynamic,
        config: Html5QrcodeScanConfig,
        qrCodeSuccessCallback: (String, dynamic) -> Unit,
        qrCodeErrorCallback: ((String, dynamic) -> Unit)?,
    ): Promise<Unit>

    /**
     * Stop scanning.
     */
    fun stop(): Promise<Unit>

    /**
     * Clear the scanner element.
     */
    fun clear(): Promise<Unit>

    /**
     * Scan a file for QR code.
     *
     * @param file The image file to scan
     * @param showImage Whether to show image in the scanner element
     */
    fun scanFile(file: dynamic, showImage: Boolean): Promise<String>

    /** Static members of the html5-qrcode scanner. */
    companion object {
        /**
         * Get list of available cameras.
         */
        fun getCameras(): Promise<Array<Html5QrcodeCamera>>
    }
}

/**
 * Camera device information.
 */
external interface Html5QrcodeCamera {
    /** Camera device identifier. */
    val id: String

    /** Human-readable camera label. */
    val label: String
}

/**
 * Scanning configuration.
 */
external interface Html5QrcodeScanConfig {
    /** Frames per second to scan at. */
    var fps: Int

    /** Size of the scanning box, as a number or object. */
    var qrbox: dynamic

    /** Aspect ratio of the camera feed. */
    var aspectRatio: Double?

    /** Whether to disable flipping of the video feed. */
    var disableFlip: Boolean?

    /** Extra video constraints passed to the browser camera. */
    var videoConstraints: dynamic
}
