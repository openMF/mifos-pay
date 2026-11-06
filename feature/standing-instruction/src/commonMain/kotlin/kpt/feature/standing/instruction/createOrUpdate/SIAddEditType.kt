/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.standing.instruction.createOrUpdate

import kotlinx.serialization.Serializable

@Serializable
sealed class SIAddEditType {

    abstract val standingInsId: Long?

    @Serializable
    data object AddItem : SIAddEditType() {
        override val standingInsId: Long?
            get() = null
    }

    @Serializable
    data class EditItem(
        override val standingInsId: Long,
    ) : SIAddEditType()
}
