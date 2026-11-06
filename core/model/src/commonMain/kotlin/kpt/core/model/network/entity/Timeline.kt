/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity

import kotlinx.serialization.Serializable

@Serializable
data class Timeline(
    val submittedOnDate: List<Int?> = ArrayList(),
    val submittedByUsername: String? = null,
    val submittedByFirstname: String? = null,
    val submittedByLastname: String? = null,
    val activatedOnDate: List<Int?> = ArrayList(),
    val activatedByUsername: String? = null,
    val activatedByFirstname: String? = null,
    val activatedByLastname: String? = null,
    val closedOnDate: List<Int?> = ArrayList(),
    val closedByUsername: String? = null,
    val closedByFirstname: String? = null,
    val closedByLastname: String? = null,
) {
    constructor() : this(
        submittedOnDate = ArrayList(),
        submittedByUsername = "",
        submittedByFirstname = "",
        submittedByLastname = "",
        activatedOnDate = ArrayList(),
        activatedByUsername = "",
        activatedByFirstname = "",
        activatedByLastname = "",
        closedOnDate = ArrayList(),
        closedByUsername = "",
        closedByFirstname = "",
        closedByLastname = "",
    )
}
