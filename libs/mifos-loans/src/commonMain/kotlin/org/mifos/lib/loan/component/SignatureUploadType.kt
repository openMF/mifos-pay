/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.component

/**
 * Defines the available methods for providing a user signature during the loan application.
 */
enum class SignatureUploadType {
    /** Draw the signature directly on screen. */
    SIGN,

    /** Capture the signature with the camera. */
    CAPTURE,

    /** Pick a signature image from the gallery. */
    GALLERY,
}
