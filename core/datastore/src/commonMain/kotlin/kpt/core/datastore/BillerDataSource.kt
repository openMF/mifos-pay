/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)

package kpt.core.datastore

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kpt.core.model.autopay.Biller
private const val BILLERS_KEY = "billers"

class BillerDataSource(
    private val settings: Settings,
    private val dispatcher: CoroutineDispatcher,
) {
    private val _billers = MutableStateFlow(
        settings.decodeValue(
            key = BILLERS_KEY,
            serializer = ListSerializer(Biller.serializer()),
            defaultValue = emptyList(),
        ),
    )

    val billers: Flow<List<Biller>> = _billers

    suspend fun updateBillers(billers: List<Biller>) {
        withContext(dispatcher) {
            settings.putBillers(billers)
            _billers.value = billers
        }
    }

    suspend fun addBiller(biller: Biller) {
        withContext(dispatcher) {
            val currentBillers = _billers.value.toMutableList()
            if (!currentBillers.any { it.id == biller.id }) {
                currentBillers.add(biller)
                settings.putBillers(currentBillers)
                _billers.value = currentBillers
            }
        }
    }

    suspend fun removeBiller(billerId: String) {
        withContext(dispatcher) {
            val currentBillers = _billers.value.toMutableList()
            currentBillers.removeAll { it.id == billerId }
            settings.putBillers(currentBillers)
            _billers.value = currentBillers
        }
    }

    suspend fun clearBillers() {
        withContext(dispatcher) {
            settings.remove(BILLERS_KEY)
            _billers.value = emptyList()
        }
    }
}

private fun Settings.putBillers(billers: List<Biller>) {
    encodeValue(
        key = BILLERS_KEY,
        serializer = ListSerializer(Biller.serializer()),
        value = billers,
    )
}
