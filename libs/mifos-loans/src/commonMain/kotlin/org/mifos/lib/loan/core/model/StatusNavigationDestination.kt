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

import kotlinx.serialization.Serializable

/** Destination to navigate to from the status screen after an operation completes. */
@Serializable
enum class StatusNavigationDestination {
    /** Home screen. */
    HOME,

    /** Savings account application flow. */
    SAVINGS_APPLICATION,

    /** Loan application flow. */
    LOAN_APPLICATION,

    /** Share account application flow. */
    SHARE_APPLICATION,

    /** Previous screen on the back stack. */
    PREVIOUS_SCREEN,

    /** Login screen. */
    LOGIN,

    /** Loan account details. */
    LOAN_ACCOUNT,

    /** Savings account details. */
    SAVINGS_ACCOUNT,

    /** Third-party transfer flow. */
    THIRD_PARTY_TRANSFER,

    /** Savings account update flow. */
    SAVINGS_UPDATE,

    /** Savings account withdrawal flow. */
    SAVINGS_WITHDRAW,

    /** Beneficiary management. */
    BENEFICIARY,
}
