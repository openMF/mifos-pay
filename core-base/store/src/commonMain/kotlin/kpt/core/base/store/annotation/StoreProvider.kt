/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.annotation

/**
 * Marks a Store5 provider function. `store-ksp` derives the store's entire addressing surface from
 * it: a row in `config/AppStoreRegistry` (Koin qualifier, plus a `Ttl` entry), one in
 * `config/AppCacheKeys` (its cache keys, nested under the store's own object), and an entry in
 * `di/GeneratedStoreBindings` (the binding AND its logout purge).
 *
 * ## Why an annotation rather than a declaration file
 * The dependencies come from the FUNCTION SIGNATURE. An `app-profile` row had to restate them —
 * `deps: [api, networkMonitor, dao]` next to `provideCoinMarketsStore(api:, networkMonitor:, dao:)`
 * — which is the same fact written twice, in two files, by hand.
 *
 * Generated output is a BUILD ARTIFACT, never committed source. That removes a whole class of
 * question: nothing to hand-edit, nothing to keep in sync on a template sync, no ownership row to
 * declare for it, and nothing for `remove-demo.sh` to reset. Delete the package and its bindings
 * cease to exist because the annotations went with it.
 *
 * Ownership stays where the strip reads it — `app-profile/app.yaml#core_store.packages[]`, one
 * `owner:` per package. A store inherits its package's lifecycle rather than declaring its own.
 *
 * @param id       Koin qualifier name and the store's stable identity.
 * @param qualifier Registry member name; defaults to `id` capitalised (`loans` ->
 *                 `AppStoreRegistry.Loans` / `AppCacheKeys.Loans`).
 * @param ttl      Freshness window — `5m` / `1h` / `7d`. Empty means the store declares none.
 * @param logout   Register with StoreCacheManager for logout purge. MUST be false for a
 *                 MutableStore: Store5 5.1 does not make it a `Store` subtype, so `register`
 *                 cannot accept one. Its rows are still purged via the paired read store's table.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class StoreProvider(
    val id: String,
    val qualifier: String = "",
    val ttl: String = "",
    val logout: Boolean = true,
)

/**
 * A stream cache key for the annotated store — the string that keys per-stream freshness tracking.
 *
 * Emitted into the store's own object inside `AppCacheKeys`, so a repository writes
 * `AppCacheKeys.Loans.LIST` / `AppCacheKeys.Loans.item(id)` and never spells the format at the call site.
 *
 * Exactly one of [name] (a constant) or [fn] (a typed builder) must be set. Placeholders in [key]
 * bind to [params] by name; a mismatch is a BUILD ERROR from the processor rather than something a
 * separate gate has to notice later.
 *
 * @param name   Constant name, e.g. `LIST` -> `const val LIST = "loans"`.
 * @param fn     Builder name, e.g. `item` -> `fun item(id: String): String`.
 * @param key    The key, with `{placeholder}` slots, e.g. `"loan:{id}"`.
 * @param params `"name:Type"` pairs for a builder, e.g. `["id:String", "days:Int"]`.
 */
@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class CacheKey(
    val key: String,
    val name: String = "",
    val fn: String = "",
    val params: Array<String> = [],
)
