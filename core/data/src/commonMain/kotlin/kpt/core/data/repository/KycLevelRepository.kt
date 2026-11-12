/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

import kpt.core.common.ScreenStateStream
import kpt.core.model.kyc.KYCLevel1Details
/** Repository for KYC (know-your-customer) level 1 details. */
interface KycLevelRepository {
    // Reads on ScreenState.
    // `null` payload is legal here (client has not yet submitted KYC); screens
    // treat Content(null) as "empty form" — not Empty (which would render a
    // blocking empty-state message).
    /** Streams the client's KYC level 1 details, or null when none exist. */
    fun fetchKYCLevel1Details(clientId: Long): ScreenStateStream<KYCLevel1Details?>

    // Message-only writes: return Unit and throw on error.
    /** Adds KYC level 1 details for the client. */
    suspend fun addKYCLevel1Details(
        clientId: Long,
        kycLevel1Details: KYCLevel1Details,
    )

    /** Updates the client's KYC level 1 details. */
    suspend fun updateKYCLevel1Details(
        clientId: Long,
        kycLevel1Details: KYCLevel1Details,
    )
}
