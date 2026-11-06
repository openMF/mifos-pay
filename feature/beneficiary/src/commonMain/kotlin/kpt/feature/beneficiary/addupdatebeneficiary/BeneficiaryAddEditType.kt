/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.beneficiary.addupdatebeneficiary

import kotlinx.serialization.Serializable
import kpt.core.model.utils.QrCodeType
@Serializable
sealed class BeneficiaryAddEditType {

    /**
     * Add a new beneficiary.
     * - If [beneficiary] is null: empty form (manual add)
     * - If [beneficiary] is non-null: pre-filled form (QR scan add)
     * - If [sourceQrType] is non-null: came from QR scan, navigate to transfer after success
     * - If [sourceQrData] is non-null: contains full QR data for post-add navigation
     *
     * Shows "Add Beneficiary" title and "Save" button, calls POST API.
     */
    @Serializable
    data class AddItem(
        val beneficiary: String? = null,
        val sourceQrType: QrCodeType? = null,
        val sourceQrData: String? = null,
    ) : BeneficiaryAddEditType()

    /**
     * Edit an existing beneficiary.
     * Shows "Update Beneficiary" title and "Update" button, calls PUT API.
     */
    @Serializable
    data class EditItem(
        val beneficiary: String,
    ) : BeneficiaryAddEditType()
}
