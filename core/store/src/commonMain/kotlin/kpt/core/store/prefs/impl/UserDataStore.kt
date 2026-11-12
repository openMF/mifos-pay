/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MatchingDeclarationName")

package kpt.core.store.prefs.impl

import kotlinx.coroutines.flow.Flow
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.model.user.UserData
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * The read PORT for the user-preferences store.
 *
 * The preferences themselves live in `core/datastore`, which `core/store` does not depend on
 * (and must not — `core/datastore` sits beside it in the layer order, not below). The store
 * therefore declares the read seam and the DI site in `core/data`, which already owns
 * `UserDataRepositoryImpl` and its `UserPreferencesRepository`, supplies it.
 */
fun interface UserDataSource {
    /**
     * Streams the preference aggregate. A pass-through to the preferences repository, kept as a store-shaped surface
     * so a screen depends on `core/store` alone.
     */
    fun observe(): Flow<UserData>
}

/**
 * OFFLINE_LOCAL_ONLY Store5 store over user preferences
 * (`feature_profile.combo_id: local_only_prefs`).
 *
 * There is no network, so `createOfflineStore` streams purely from the source of truth — here
 * the multiplatform-settings-backed preferences flow rather than a Room DAO. Wrapping prefs in
 * a Store is what puts the settings screen on the same `ScreenState` read path as every other
 * screen: it gets Loading / Content / Error for free instead of the settings ViewModel
 * hand-rolling its own `SettingsUiState.Loading` sentinel.
 *
 * The source of truth is READ-ONLY here: preference WRITES keep flowing through
 * `UserDataRepository`'s existing typed setters (`setThemeBrand`, `setLanguage`, …), which
 * write to the preferences store and cause [UserDataSource.observe] to re-emit. Routing writes
 * through `store.write` instead would mean funnelling every individual typed setting through a
 * single whole-`UserData` blob write, losing the per-setting API for no benefit — so the writer
 * is deliberately not part of this store's contract.
 */
@StoreProvider(id = "userData")
@CacheKey(name = "KEY", key = "userData")
fun provideUserDataStore(source: UserDataSource): Store<Unit, UserData> =
    StoreFactory.createOfflineStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { _: Unit -> source.observe() },
            // Writes go through UserDataRepository's typed setters (see KDoc above); this
            // writer exists only to satisfy SourceOfTruth's shape and is never invoked,
            // because nothing calls `store.write` for this key.
            writer = { _: Unit, _: UserData -> Unit },
        ),
    )
