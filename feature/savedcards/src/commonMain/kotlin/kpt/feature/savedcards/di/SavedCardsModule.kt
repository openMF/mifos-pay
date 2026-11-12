/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards.di

import kpt.feature.savedcards.CardsScreenViewModel
import kpt.feature.savedcards.createOrUpdate.AddEditCardViewModel
import kpt.feature.savedcards.details.CardDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
/**
 * Koin module providing the ViewModels of the saved cards feature.
 */
val SavedCardsModule = module {
    viewModelOf(::CardsScreenViewModel)
    viewModelOf(::CardDetailViewModel)
    viewModelOf(::AddEditCardViewModel)
}
