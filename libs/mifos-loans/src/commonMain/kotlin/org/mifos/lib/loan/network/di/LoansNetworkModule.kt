/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.network.di

import kpt.core.base.network.restApi
import org.koin.dsl.module
import org.mifos.lib.loan.network.LoansService
import org.mifos.lib.loan.network.createLoansService

/**
 * Binds [LoansService] to the app's `selfservice` access point (app-profile): the signed-in customer's
 * Fineract self-service API, with its URL, tenant and `Authorization` supplied by core-base — the same
 * client every self-service API in the app uses. The library declares no client of its own.
 */
val LoansNetworkModule = module {
    restApi<LoansService>("selfservice") { it.createLoansService() }
}
