/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kpt.core.data.repository.LocalAssetRepository
import kpt.core.data.util.SAMPLE_CURRENCY
import kpt.core.data.util.SAMPLE_LOCALE
import kpt.core.model.utils.CurrencyCode
import kpt.core.model.utils.Locale
/** Default [LocalAssetRepository] providing locales and currencies. */
class LocalAssetRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher,
    unconfinedDispatcher: CoroutineDispatcher,
    private val networkJson: Json,
) : LocalAssetRepository {
    private val coroutineScope = CoroutineScope(unconfinedDispatcher)

    override val localeList: StateFlow<List<Locale>>
        get() = flow {
            val data = withContext(ioDispatcher) {
                networkJson.decodeFromString<List<Locale>>(SAMPLE_LOCALE)
            }

            emit(data)
        }.catch {
            Logger.e(it) { "Error while fetching locale list" }
        }.stateIn(
            scope = coroutineScope,
            initialValue = emptyList(),
            started = SharingStarted.Eagerly,
        )

    override val currencyList: StateFlow<List<CurrencyCode>>
        get() = flow {
            val data = withContext(ioDispatcher) {
                networkJson.decodeFromString<List<CurrencyCode>>(SAMPLE_CURRENCY)
            }

            emit(data)
        }.catch {
            Logger.e(it) { "Error while fetching currency list" }
        }.stateIn(
            scope = coroutineScope,
            initialValue = emptyList(),
            started = SharingStarted.Eagerly,
        )
}
