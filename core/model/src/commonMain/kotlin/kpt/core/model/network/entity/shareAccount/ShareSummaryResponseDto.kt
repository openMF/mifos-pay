/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.shareAccount

import kotlinx.serialization.Serializable
import kpt.core.model.network.entity.common.CurrencyResponseDto
/**
 * Summary of a share account with approved and pending share counts.
 *
 * @property id Unique id.
 * @property accountNo Account number.
 * @property productId Id of the product.
 * @property productName Name of the product.
 * @property status Current status.
 * @property currency Currency.
 * @property timeline Lifecycle timeline.
 * @property totalApprovedShares Number of approved shares.
 * @property totalPendingForApprovalShares Number of shares pending approval.
 */
@Serializable
data class ShareSummaryResponseDto(
    val id: Long? = null,
    val accountNo: String? = null,
    val productId: Long? = null,
    val productName: String? = null,
    val status: ShareStatusResponseDto? = null,
    val currency: CurrencyResponseDto? = null,
    val timeline: ShareTimelineResponseDto? = null,
    val totalApprovedShares: Int? = null,
    val totalPendingForApprovalShares: Int? = null,
)
