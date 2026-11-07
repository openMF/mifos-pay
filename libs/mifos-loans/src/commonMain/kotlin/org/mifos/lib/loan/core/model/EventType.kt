/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Outcome category of a loan application submission event. */
@Serializable
enum class EventType {
    /** The operation succeeded. */
    @SerialName("success")
    SUCCESS,

    /** The operation failed. */
    @SerialName("failure")
    FAILURE,

    /** The server raised an exception. */
    @SerialName("server")
    SERVER_EXCEPTION,
}
