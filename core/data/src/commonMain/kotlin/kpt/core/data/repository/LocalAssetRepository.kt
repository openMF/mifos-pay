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
/** Repository for static reference data bundled with the app. */
interface LocalAssetRepository {
    /** Locales available in the app. */
    val localeList: StateFlow<List<Locale>>

    /** Currency codes available in the app. */
    val currencyList: StateFlow<List<CurrencyCode>>
}
