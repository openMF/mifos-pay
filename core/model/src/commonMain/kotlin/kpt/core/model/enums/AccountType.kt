/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.enums

/**
 * Kind of account in Fineract, with its display label.
 *
 * @property value Human-readable label.
 */
enum class AccountType(val value: String) {

    /**
     * Savings account.
     */
    SAVINGS("Savings Account"),

    /**
     * Loan account.
     */
    LOAN("Loan Account"),

    /**
     * Share account.
     */
    SHARE("Share Account"),
}
