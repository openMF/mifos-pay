/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.accounts.savingsaccount

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kpt.core.ui.composableWithSlideTransitions
private const val ADD_TYPE: String = "add_saving"
private const val EDIT_TYPE: String = "edit_saving"
private const val EDIT_ITEM_ID: String = "saving_edit_id"

private const val ADD_EDIT_ITEM_PREFIX: String = "saving_add_edit_item"
private const val ADD_EDIT_ITEM_TYPE: String = "saving_add_edit_type"

private const val ADD_EDIT_ITEM_ROUTE: String =
    ADD_EDIT_ITEM_PREFIX +
        "/{$ADD_EDIT_ITEM_TYPE}" +
        "?$EDIT_ITEM_ID={$EDIT_ITEM_ID}"

/**
 * Navigation arguments of the add/edit savings account screen.
 *
 * @property savingsAddEditType Whether to add a new account or edit an existing one.
 */
data class SavingAccountAddEditArgs(
    val savingsAddEditType: SavingsAddEditType,
) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        savingsAddEditType = when (requireNotNull(savedStateHandle[ADD_EDIT_ITEM_TYPE])) {
            ADD_TYPE -> SavingsAddEditType.AddItem

            EDIT_TYPE -> {
                val accountId = requireNotNull(savedStateHandle.get<String>(EDIT_ITEM_ID)).toLong()
                SavingsAddEditType.EditItem(savingsAccountId = accountId)
            }

            else -> throw IllegalStateException("Unknown SavingsAddEditType.")
        },
    )
}

/** Registers the add/edit savings account destination. */
fun NavGraphBuilder.addEditSavingAccountScreen(
    navigateBack: () -> Unit,
) {
    composableWithSlideTransitions(
        route = ADD_EDIT_ITEM_ROUTE,
        arguments = listOf(
            navArgument(ADD_EDIT_ITEM_TYPE) { type = NavType.StringType },
        ),
    ) {
        AddEditSavingAccountScreen(
            navigateBack = navigateBack,
        )
    }
}

/** Navigates to the add/edit savings account screen for [addEditType]. */
fun NavController.navigateToSavingAccountAddEdit(
    addEditType: SavingsAddEditType,
    navOptions: NavOptions? = null,
) {
    navigate(
        route = "$ADD_EDIT_ITEM_PREFIX/${addEditType.toTypeString()}" +
            "?$EDIT_ITEM_ID=${addEditType.toIdOrNull()}",
        navOptions = navOptions,
    )
}

private fun SavingsAddEditType.toTypeString(): String =
    when (this) {
        is SavingsAddEditType.AddItem -> ADD_TYPE
        is SavingsAddEditType.EditItem -> EDIT_TYPE
    }

private fun SavingsAddEditType.toIdOrNull(): String? =
    when (this) {
        is SavingsAddEditType.AddItem -> null
        is SavingsAddEditType.EditItem -> savingsAccountId.toString()
    }
