/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.datatables.invoice

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InvoiceEntity(
    val id: Long,
    val invoiceId: Long,
    val consumerId: String,
    val consumerName: String,
    val amount: Double,
    @SerialName("itemsbought")
    val itemsBought: String,
    val status: Long,
    val transactionId: String,
    val title: String,
    val date: String,
    val locale: String,
    val dateFormat: String,
)
