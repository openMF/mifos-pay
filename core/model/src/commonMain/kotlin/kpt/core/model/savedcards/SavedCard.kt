/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savedcards

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kpt.core.common.IgnoredOnParcel
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
@Serializable
@Parcelize
data class SavedCard(
    val id: Long = 0,
    @SerialName("client_id")
    val clientId: Long,
    val firstName: String,
    val lastName: String,
    val cardNumber: String,
    val cvv: String,
    val expiryDate: String,
    val backgroundColor: String,
    @SerialName("created_at")
    val createdAt: List<Long>,
    @SerialName("updated_at")
    val updatedAt: List<Long>,
) : Parcelable {
    @IgnoredOnParcel
    val fullName = "$firstName $lastName"

    @IgnoredOnParcel
    val formattedExpiryDate: String
        get() = "${expiryDate.substring(0, 2)}/${expiryDate.substring(2, 4)}"

    @IgnoredOnParcel
    val maskedCvv: String
        get() = "*".repeat(cvv.length)
}
