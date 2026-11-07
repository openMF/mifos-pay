/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savingsaccount

import kotlinx.serialization.Serializable

/**
 * Server response after blocking or unblocking a savings account.
 *
 * @property officeId Identifier of the account office.
 * @property clientId Identifier of the account client.
 * @property savingsId Identifier of the savings account.
 * @property resourceId Identifier of the affected resource.
 * @property changes Changes applied by the command.
 */
@Serializable
data class BlockUnblockResponseEntity(
    val officeId: Long,
    val clientId: Long,
    val savingsId: Long,
    val resourceId: Long,
    val changes: ChangesEntity,
)

/**
 * The set of changes reported by a block/unblock command.
 *
 * @property subStatus New sub-status of the account.
 */
@Serializable
data class ChangesEntity(
    val subStatus: SubStatus,
)
