/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.di

import org.koin.core.qualifier.named

/** Koin qualifier for the HTTP client targeting the self-service API. */
val SelfClient = named("SelfClient")

/** Koin qualifier for the HTTP client targeting the base Fineract API. */
val BaseClient = named("BaseClient")

/** Koin qualifier for the HTTP client targeting the inter-bank API. */
val InterBankClient = named("InterBankClient")
