/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.di

import org.koin.core.module.Module
import org.koin.dsl.module
import kpt.core.base.store.di.StoreModule as CoreBaseStoreModule

/**
 * App-level Store wiring — FRAMEWORK ONLY.
 *
 * ## Why this file holds no store bindings
 * It used to carry ~30 hand-written `single(...) { provideXStore(...) }` blocks plus a second
 * hand-kept list registering those stores for logout purge, inside a `demo:` fence in a FORK-OWNED
 * file. Two problems followed. A fork never received upstream fixes to the framework wiring below,
 * because the whole file was excluded from sync to protect the fork's bindings. And the two lists
 * could disagree — a store bound but not registered survives sign-out and shows the previous user's
 * cached rows to the next person on a shared device.
 *
 * Both now come from one `@StoreProvider(logout = ...)` on the provider function, which `store-ksp`
 * turns into [GeneratedStoreBindings]: the binding AND the purge, from a single fact. This file is
 * template-owned again and a sync can blind-copy it.
 *
 * ## Adding a store
 * Annotate the provider. There is no wiring step, and no DI seam to add it to:
 * ```kotlin
 * @StoreProvider(id = "myThing", ttl = "5m")
 * @CacheKey(name = "LIST", key = "myThing")
 * fun provideMyThingStore(api: MyApi, dao: MyDao): Store<Unit, List<MyThing>> = …
 * ```
 * `store-ksp` derives the Koin qualifier, the TTL, the cache keys, the binding and the logout purge.
 * Dependencies come from the SIGNATURE — they are never restated.
 *
 * ## The fork seam is [ForkStoreModule], not this file
 * This file resolves `owner: template`, so a sync BLIND-COPIES it and anything a fork adds here is
 * silently reverted on the next one. Fork-owned store singles go in the sibling `ForkStoreModule`.
 *
 * That seam was removed when stores moved to `@StoreProvider` codegen, on the reasoning that the
 * annotation already covered them. True of STORES — but not of store-layer COLLABORATORS, which no
 * annotation derives. `StoreCacheManager` and `DraftInventory` were the standing counterexample
 * until they moved down to `CoreBaseStoreModule`, where framework wiring belongs. A fork swapping
 * either still needs somewhere sync-safe, and `white-label-di-seams.sh` names "a store" among the
 * things a fork must be able to register.
 *
 * ## Why this file is now two lines
 * Everything it used to bind was one of two things: framework wiring, which moved DOWN to
 * `core-base/store` where a fork never looks, or a store, which `@StoreProvider` derives. What is
 * left is pure composition — and both lines are themselves derivable, so this file is a candidate to
 * be GENERATED and deleted outright.
 *
 * Wire into Koin start-up:
 * ```kotlin
 * startKoin { modules(appStoreModule, /* … */) }
 * ```
 */
val appStoreModule: Module = module {
    // Framework write-SoT: the base-store module provides the single write door (MutationGateway)
    // + its Room-backed ConflictInbox. Every repo migrated onto `gateway.*` resolves `get()` here,
    // so a fork wiring `appStoreModule` gets the gateway for free (needs ConflictDao from
    // DatabaseModule + NetworkMonitor on the graph — both present in KoinModules.allModules).
    includes(CoreBaseStoreModule)

    // Every declared store: its qualifier binding AND its logout registration.
    includes(GeneratedStoreBindings)
}
