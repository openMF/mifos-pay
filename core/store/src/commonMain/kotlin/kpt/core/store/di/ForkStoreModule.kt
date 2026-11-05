/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.di

import org.koin.dsl.module

/**
 * THE FORK'S store-layer DI seam. Empty on the neutral template — this is yours to fill.
 *
 * It completes the `di/Fork<X>Module` set the white-label contract already assumes: `core/data`,
 * `core/database`, `core/datastore` and `core/network` each ship one, and `core/store` was the only
 * core layer without. `scripts/product-health/checks/white-label-di-seams.sh` names the gap it
 * closes in its own header — a fork with "nowhere to register a repository, a DAO, a store or a
 * network single".
 *
 * ## What does NOT go here
 * **Stores.** Every store is declared by annotating its provider, and `store-ksp` derives the Koin
 * qualifier, the TTL, the cache keys, the binding AND the logout purge from that one fact:
 * ```kotlin
 * @StoreProvider(id = "myThing", ttl = "5m")
 * @CacheKey(name = "LIST", key = "myThing")
 * fun provideMyThingStore(api: MyApi, dao: MyDao): Store<Unit, List<MyThing>> = …
 * ```
 * Hand-binding a store here reintroduces exactly the defect [appStoreModule] was rewritten to kill:
 * a binding and a separate logout registration that can disagree, so a store survives sign-out and
 * shows the previous user's cached rows to the next person on a shared device. The annotation cannot
 * drift from itself; two lists can.
 *
 * ## What DOES go here
 * Store-layer singles that are NOT derivable from a `@StoreProvider`. `CoreBaseStoreModule` binds
 * exactly this class by hand — `StoreCacheManager`, `DraftInventory`, `MutationGateway` — which is
 * the proof the class exists and needs a fork-side counterpart:
 *
 * - a replacement `StoreCacheManager` (extra caches to purge on logout)
 * - a fork's own `DraftInventory` implementation
 * - a custom `Bookkeeper` / conflict-resolution policy
 * - any collaborator a `@StoreProvider` function takes that no other layer already provides
 *
 * ```kotlin
 * val ForkStoreModule = module {
 *     single<DraftInventory> { MyForkDraftInventory(draftDao = get(), audit = get()) }
 * }
 * ```
 *
 * ADDING is the supported case. REPLACING a single `CoreBaseStoreModule` already binds depends on Koin's
 * override semantics rather than on where the module sits in the list — `KoinModules.allModules`
 * states outright that "order is irrelevant to Koin". So treat a same-type rebinding as deliberate:
 * verify which instance resolves, rather than assuming the later module wins.
 *
 * THIS FILE IS `owner: fork`. `/kmp-project-template-sync` preserves it — the template ships it empty
 * and never overwrites what you add. That is why the seam lives here and not in [appStoreModule],
 * which resolves `owner: template` and is blind-copied by every sync, silently reverting a fork's
 * additions on the next one.
 */
val ForkStoreModule = module {
    // Intentionally empty on the template — a fork adds its own non-derivable store-layer singles
    // here. Stores themselves need no entry: annotate the provider with @StoreProvider and
    // GeneratedStoreBindings carries both the binding and the logout purge.
}
