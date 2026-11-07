/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards.createOrUpdate

import kotlinx.serialization.Serializable

/**
 * Mode of the add/edit card screen.
 * @property savedCardId Id of the card being edited, or null when adding.
 */
@Serializable
sealed class CardAddEditType {

    abstract val savedCardId: Long?

    /**
     * Add mode: a new card is created.
     */
    @Serializable
    data object AddItem : CardAddEditType() {
        override val savedCardId: Long?
            get() = null
    }

    /**
     * Edit mode for an existing card.
     * @property savedCardId Id of the card being edited.
     */
    @Serializable
    data class EditItem(
        override val savedCardId: Long,
    ) : CardAddEditType()
}
