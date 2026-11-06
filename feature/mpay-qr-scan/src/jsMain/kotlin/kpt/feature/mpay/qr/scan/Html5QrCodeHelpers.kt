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

/**
 * Camera facing mode constraints.
 */
object Html5QrcodeFacingMode {
    const val ENVIRONMENT = "environment"
    const val USER = "user"
}

/**
 * Create a scan configuration object.
 */
fun createScanConfig(
    fps: Int = 10,
    qrboxWidth: Int = 250,
    qrboxHeight: Int = 250,
    aspectRatio: Double? = null,
    disableFlip: Boolean = false,
): Html5QrcodeScanConfig {
    val config = js("({})").unsafeCast<Html5QrcodeScanConfig>()
    config.fps = fps
    val qrbox = js("({})")
    qrbox.width = qrboxWidth
    qrbox.height = qrboxHeight
    config.qrbox = qrbox
    config.aspectRatio = aspectRatio
    config.disableFlip = disableFlip
    return config
}

/**
 * Create camera constraints for back camera (environment facing).
 */
fun createBackCameraConstraints(): dynamic {
    return js("({ facingMode: 'environment' })")
}

/**
 * Create camera constraints for front camera (user facing).
 */
fun createFrontCameraConstraints(): dynamic {
    return js("({ facingMode: 'user' })")
}
