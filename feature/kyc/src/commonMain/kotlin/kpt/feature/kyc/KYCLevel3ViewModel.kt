/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.kyc

import kpt.core.ui.fork.utils.BaseViewModel
/**
 * MVI ViewModel for the KYC Level 3 (Review & Submit) screen.
 *
 * The screen is currently a static stub, so there are no user actions or one-shot
 * events yet — [KYCLevel3Event] and [KYCLevel3Action] are intentionally empty.
 * Conforming structurally to [BaseViewModel] lets future Level-3 review/submit
 * interactions slot in as [KYCLevel3Action] entries + `handleAction` branches
 * without another migration.
 */
// TODO: Implement KYC Level3 review & submit interactions
class KYCLevel3ViewModel :
    BaseViewModel<KYCLevel3State, KYCLevel3Event, KYCLevel3Action>(
        initialState = KYCLevel3State,
    ) {

    override fun handleAction(ignored: KYCLevel3Action) {
        // No actions on the static Level-3 stub yet.
    }
}

/** Empty UI state of the KYC level 3 stub screen. */
data object KYCLevel3State

/** One-off events of the KYC level 3 screen (none defined). */
sealed interface KYCLevel3Event

/** User actions of the KYC level 3 screen (none defined). */
sealed interface KYCLevel3Action
