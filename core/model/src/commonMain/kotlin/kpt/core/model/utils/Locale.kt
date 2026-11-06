/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.utils

import kotlinx.serialization.Serializable
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
@Parcelize
@Serializable
data class Locale(
    val countryName: String,
    val localName: String,
    val dominantName: String,
) : Parcelable

fun List<Locale>.filterLocales(filterText: String): List<Locale> {
    return if (filterText.isNotEmpty()) {
        this.filter {
            it.countryName.contains(filterText, true) ||
                it.localName.contains(filterText, true) ||
                it.dominantName.contains(filterText, true)
        }
    } else {
        this
    }
}
