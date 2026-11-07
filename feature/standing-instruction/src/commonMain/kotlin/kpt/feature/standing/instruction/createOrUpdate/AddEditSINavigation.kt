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

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kpt.core.ui.composableWithSlideTransitions
private const val ADD_TYPE: String = "add_si"
private const val EDIT_TYPE: String = "edit_si"
private const val EDIT_ITEM_ID: String = "si_edit_id"

private const val ADD_EDIT_ITEM_PREFIX: String = "si_add_edit_item"
private const val ADD_EDIT_ITEM_TYPE: String = "si_add_edit_type"

private const val ADD_EDIT_ITEM_ROUTE: String =
    ADD_EDIT_ITEM_PREFIX +
        "/{$ADD_EDIT_ITEM_TYPE}" +
        "?$EDIT_ITEM_ID={$EDIT_ITEM_ID}"

/**
 * Navigation arguments of the add/edit standing instruction screen.
 * @property addEditType Whether a new instruction is being added or an existing one edited.
 */
data class SIAddEditArgs(
    val addEditType: SIAddEditType,
) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        addEditType = when (requireNotNull(savedStateHandle[ADD_EDIT_ITEM_TYPE])) {
            ADD_TYPE -> SIAddEditType.AddItem
            EDIT_TYPE -> {
                val standingInsId = requireNotNull(savedStateHandle.get<String>(EDIT_ITEM_ID)).toLong()
                SIAddEditType.EditItem(standingInsId)
            }
            else -> throw IllegalStateException("Unknown SIAddEditType.")
        },
    )
}

/** Registers the add/edit standing instruction destination. */
fun NavGraphBuilder.addEditSIScreen(
    navigateBack: () -> Unit,
) {
    composableWithSlideTransitions(
        route = ADD_EDIT_ITEM_ROUTE,
        arguments = listOf(
            navArgument(ADD_EDIT_ITEM_TYPE) { type = NavType.StringType },
        ),
    ) {
        AddEditSIScreen(
            navigateBack = navigateBack,
        )
    }
}

/** Navigates to the add/edit standing instruction screen for [addEditType]. */
fun NavController.navigateToSIAddEdit(
    addEditType: SIAddEditType,
    navOptions: NavOptions? = null,
) {
    navigate(
        route = "$ADD_EDIT_ITEM_PREFIX/${addEditType.toTypeString()}" +
            "?$EDIT_ITEM_ID=${addEditType.toIdOrNull()}",
        navOptions = navOptions,
    )
}

private fun SIAddEditType.toTypeString(): String =
    when (this) {
        is SIAddEditType.AddItem -> ADD_TYPE
        is SIAddEditType.EditItem -> EDIT_TYPE
    }

private fun SIAddEditType.toIdOrNull(): String? =
    when (this) {
        is SIAddEditType.AddItem -> null
        is SIAddEditType.EditItem -> standingInsId.toString()
    }
