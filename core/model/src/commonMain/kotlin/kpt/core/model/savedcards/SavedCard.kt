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
/**
 * A payment card the user has saved in the wallet.
 *
 * @property id Identifier of the saved card; 0 before it is stored.
 * @property clientId Identifier of the client.
 * @property firstName Cardholder first name.
 * @property lastName Cardholder last name.
 * @property cardNumber Card number.
 * @property cvv Card verification value.
 * @property expiryDate Card expiry date as MMYY.
 * @property backgroundColor Background colour of the card as shown in the UI.
 * @property createdAt Creation timestamp components as reported by the server.
 * @property updatedAt Last-update timestamp components as reported by the server.
 */
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
    /** Cardholder first and last name combined. */
    @IgnoredOnParcel
    val fullName = "$firstName $lastName"

    /** Expiry date formatted as MM/YY. */
    @IgnoredOnParcel
    val formattedExpiryDate: String
        get() = "${expiryDate.substring(0, 2)}/${expiryDate.substring(2, 4)}"

    /** CVV replaced with asterisks for display. */
    @IgnoredOnParcel
    val maskedCvv: String
        get() = "*".repeat(cvv.length)
}
