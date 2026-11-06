/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.standing.instruction.details

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kpt.core.ui.composableWithPushTransitions
private const val SID_DETAILS_ROUTE = "sid_details_route"
private const val SID_DETAILS_ARG = "instructionId"

private const val BASE_ROUTE = "$SID_DETAILS_ROUTE/{$SID_DETAILS_ARG}"

fun NavGraphBuilder.siDetailsScreen(
    navigateBack: () -> Unit,
) {
    composableWithPushTransitions(
        route = BASE_ROUTE,
        arguments = listOf(navArgument(SID_DETAILS_ARG) { type = NavType.LongType }),
    ) {
        SIDetailsScreen(
            navigateBack = navigateBack,
        )
    }
}

fun NavController.navigateSIDetails(instructionId: Long, navOptions: NavOptions? = null) {
    navigate(route = "$SID_DETAILS_ROUTE/$instructionId", navOptions)
}
