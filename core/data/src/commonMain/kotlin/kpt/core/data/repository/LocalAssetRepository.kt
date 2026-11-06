/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

import kotlinx.coroutines.flow.StateFlow
import kpt.core.model.utils.CurrencyCode
import kpt.core.model.utils.Locale
interface LocalAssetRepository {
    val localeList: StateFlow<List<Locale>>

    val currencyList: StateFlow<List<CurrencyCode>>
}
