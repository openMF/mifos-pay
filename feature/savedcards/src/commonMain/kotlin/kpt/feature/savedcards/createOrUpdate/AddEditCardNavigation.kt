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

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kpt.core.ui.composableWithSlideTransitions
private const val ADD_TYPE: String = "add_card"
private const val EDIT_TYPE: String = "edit_card"
private const val EDIT_ITEM_ID: String = "card_edit_id"

private const val ADD_EDIT_ITEM_PREFIX: String = "card_add_edit_item"
private const val ADD_EDIT_ITEM_TYPE: String = "card_add_edit_type"

private const val ADD_EDIT_ITEM_ROUTE: String =
    ADD_EDIT_ITEM_PREFIX +
        "/{$ADD_EDIT_ITEM_TYPE}" +
        "?$EDIT_ITEM_ID={$EDIT_ITEM_ID}"

/**
 * Arguments of the add/edit card screen, parsed from saved state.
 * @property cardAddEditType Whether a card is being added or an existing one edited.
 */
data class CardAddEditArgs(
    val cardAddEditType: CardAddEditType,
) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        cardAddEditType = when (requireNotNull(savedStateHandle[ADD_EDIT_ITEM_TYPE])) {
            ADD_TYPE -> CardAddEditType.AddItem

            EDIT_TYPE -> {
                val savedCardId =
                    requireNotNull(savedStateHandle.get<String>(EDIT_ITEM_ID)).toLong()
                CardAddEditType.EditItem(savedCardId = savedCardId)
            }

            else -> throw IllegalStateException("Unknown SavedCardAddEditType.")
        },
    )
}

/**
 * Registers the add/edit card screen in the navigation graph.
 */
fun NavGraphBuilder.addEditCardScreen(
    navigateBack: () -> Unit,
) {
    composableWithSlideTransitions(
        route = ADD_EDIT_ITEM_ROUTE,
        arguments = listOf(
            navArgument(ADD_EDIT_ITEM_TYPE) { type = NavType.StringType },
        ),
    ) {
        AddEditCardScreen(
            navigateBack = navigateBack,
        )
    }
}

/**
 * Navigates to the add/edit card screen for the given [addEditType].
 */
fun NavController.navigateToCardAddEdit(
    addEditType: CardAddEditType,
    navOptions: NavOptions? = null,
) {
    navigate(
        route = "$ADD_EDIT_ITEM_PREFIX/${addEditType.toTypeString()}" +
            "?$EDIT_ITEM_ID=${addEditType.toIdOrNull()}",
        navOptions = navOptions,
    )
}

private fun CardAddEditType.toTypeString(): String =
    when (this) {
        is CardAddEditType.AddItem -> ADD_TYPE
        is CardAddEditType.EditItem -> EDIT_TYPE
    }

private fun CardAddEditType.toIdOrNull(): String? =
    when (this) {
        is CardAddEditType.AddItem -> null
        is CardAddEditType.EditItem -> savedCardId.toString()
    }
