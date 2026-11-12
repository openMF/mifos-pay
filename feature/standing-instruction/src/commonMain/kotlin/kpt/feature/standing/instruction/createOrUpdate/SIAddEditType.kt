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

/** Whether the add/edit screen creates a new standing instruction or edits an existing one. */
@Serializable
sealed class SIAddEditType {

    /** Id of the instruction being edited, or null when adding. */
    abstract val standingInsId: Long?

    /** Create a new standing instruction. */
    @Serializable
    data object AddItem : SIAddEditType() {
        override val standingInsId: Long?
            get() = null
    }

    /**
     * Edit an existing standing instruction.
     * @property standingInsId Id of the instruction to edit.
     */
    @Serializable
    data class EditItem(
        override val standingInsId: Long,
    ) : SIAddEditType()
}
