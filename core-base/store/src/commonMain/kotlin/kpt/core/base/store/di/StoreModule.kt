/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.di

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import kpt.core.base.store.infra.DraftInventory
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.infra.StoreCacheManager
import kpt.core.base.store.infra.impl.DraftInventoryImpl
import kpt.core.base.store.infra.impl.RoomFetchedAtRepository
import kpt.core.base.store.infra.impl.StoreCacheManagerImpl
import kpt.core.base.store.mutation.DefaultMutationGateway
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.conflict.ConflictInbox
import kpt.core.base.store.mutation.conflict.impl.RoomConflictInbox
import kpt.core.base.store.screen.ScreenStreamContext
import org.koin.dsl.module
import kotlin.time.Clock

/**
 * Koin module providing base Store infrastructure.
 *
 * Consumer apps should include this module and add their own store bindings
 * in their `core/data` DI module using `StoreFactory` to create store instances.
 *
 * Also provides the framework write-SoT: the [MutationGateway] (the single write door every repo
 * routes mutations through) and its Room-backed [ConflictInbox]. Requires a `NetworkMonitor` (from
 * `cmp-network-monitor`) and the framework `ConflictDao` (from `DatabaseModule`) on the graph.
 */
val StoreModule = module {
    // Room-backed conflict inbox surfaced in Settings.
    single<ConflictInbox> {
        RoomConflictInbox(dao = get(), now = { Clock.System.now().toEpochMilliseconds() })
    }
    // The single write door — composes MutableStore.write + the conflict inbox + connectivity.
    single<MutationGateway> {
        DefaultMutationGateway(
            isOnline = { get<NetworkMonitor>().isOnline.value },
            conflictInbox = get(),
        )
    }
    // The read-path infra bundle every repository's `store.asScreenStream(...)` needs — resolved by
    // `asScreenStream` itself (Koin default), so it is NOT threaded through repository constructors.
    // Moved DOWN from core/data's DataModule for the same reason as the two below: type AND impl are
    // core-base/store, and the only dependency arrives through `get()`. ScreenStreamContext right
    // above already consumes it, so the binding now sits beside its consumer instead of a layer up.
    single<FetchedAtRepository> { RoomFetchedAtRepository(get()) }

    single { ScreenStreamContext(networkMonitor = get(), fetchedAtRepository = get()) }

    // Moved DOWN from core/store's appStoreModule. Both the TYPE and the IMPL are core-base
    // (`kpt.core.base.store.infra`), and both dependencies arrive through `get()` — so this binding
    // never needed to sit in the fork-facing layer. It sat there only because that is where it was
    // first written.
    //
    // Relocating between these two modules cannot change the resolved graph: appStoreModule
    // unconditionally `includes(CoreBaseStoreModule)` and KoinModules unconditionally includes
    // appStoreModule, so the same definitions reach the same container either way.
    single<StoreCacheManager> {
        StoreCacheManagerImpl(
            bookkeeperDao = get(),
            draftDao = get(),
        )
    }

    // Cross-form drafts inventory — the live feed + actions behind the template-level
    // Settings -> "Sync & Drafts" screen. Framework infra, so it belongs with the framework.
    single<DraftInventory> { DraftInventoryImpl(draftDao = get()) }
}
