/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.component

/**
* Represents the specific categories of documents required for a loan application.
*/
enum class DocumentType {
    /** Bank statement supporting the applicant income. */
    BANK_STATEMENT,

    /** Property ownership document, typically for collateral. */
    PROPERTY_DOCUMENT,

    /** Applicant's signature. */
    SIGNATURE,
}
