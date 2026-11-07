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

import kotlinx.serialization.Serializable

/**
 * Request body for saving a new payment card.
 *
 * @property firstName Cardholder first name.
 * @property lastName Cardholder last name.
 * @property cardNumber Card number.
 * @property cvv Card verification value.
 * @property expiryDate Card expiry date as MMYY.
 * @property backgroundColor Background colour of the card as shown in the UI.
 */
@Serializable
data class CardPayload(
    val firstName: String,
    val lastName: String,
    val cardNumber: String,
    val cvv: String,
    val expiryDate: String,
    val backgroundColor: String? = null,
)
