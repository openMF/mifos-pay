/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards.details

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kpt.core.ui.composableWithSlideTransitions
private const val ROUTE = "card_detail"
private const val CARD_ID = "cardId"

private const val BASE_ROUTE = "$ROUTE?$CARD_ID={$CARD_ID}"

/**
 * Registers the card details screen in the navigation graph.
 */
fun NavGraphBuilder.cardDetailRoute(
    navigateBack: () -> Unit,
) {
    composableWithSlideTransitions(
        route = BASE_ROUTE,
        arguments = listOf(
            navArgument(CARD_ID) { type = NavType.LongType },
        ),
    ) {
        CardDetailScreen(
            navigateBack = navigateBack,
        )
    }
}

/**
 * Navigates to the details of the card with id [cardId].
 */
fun NavController.navigateToCardDetails(cardId: Long) {
    navigate("$ROUTE?$CARD_ID=$cardId")
}
