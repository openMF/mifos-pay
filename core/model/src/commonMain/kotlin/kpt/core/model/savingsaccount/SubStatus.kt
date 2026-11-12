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
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
/**
 * Savings account sub-status (for example dormant or blocked) with state flags.
 *
 * @property id Sub-status id.
 * @property code Sub-status code.
 * @property value Human-readable sub-status label.
 * @property none No sub-status applies.
 * @property inactive Account is inactive.
 * @property dormant Account is dormant.
 * @property escheat Account has escheated.
 * @property block Account is fully blocked.
 * @property blockCredit Credits are blocked.
 * @property blockDebit Debits are blocked.
 */
@Serializable
@Parcelize
data class SubStatus(
    val id: Long,
    val code: String,
    val value: String,
    val none: Boolean,
    val inactive: Boolean,
    val dormant: Boolean,
    val escheat: Boolean,
    val block: Boolean,
    val blockCredit: Boolean,
    val blockDebit: Boolean,
) : Parcelable
