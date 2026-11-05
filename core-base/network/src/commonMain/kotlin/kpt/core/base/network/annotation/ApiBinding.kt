/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.network.annotation

/**
 * Binds this API type to a declared access point, so its Koin binding is derived rather than written.
 *
 * `tools/network-ksp` emits `GeneratedApiBindings` from these, picking the binding shape from the
 * point's declared kind:
 *  - `rest`     -> `restApi("<id>") { it.create<Simple>() }`   (the Ktorfit factory, by convention)
 *  - `supabase` -> `supabaseApi("<id>") { <Simple>(it) }`      (single-arg ctor taking the config client)
 *
 * ```kotlin
 * @ApiBinding("coingecko")
 * interface CoinGeckoApi { ... }
 * ```
 *
 * ## Why only the binding, and not the endpoint
 * The access point itself — `base_url`, `type`, `owner`, `secret_alias`, `publishable_key_env` — stays in
 * `app-profile/app.yaml#network.access_points`. That is per-fork DEPLOYMENT config: a fork's URL and
 * key names differ from the template's, and these API classes are template-owned showcase code. Put
 * a URL in an annotation here and a fork has to edit a template-owned class to change it, which is
 * exactly the 3-way merge this whole contract exists to remove.
 *
 * What DID belong on the class is the class itself. The point used to carry `api: <fully.qualified.Name>`
 * — a string restating a type that already exists, checked only by a gate at build time. As an
 * annotation it is checked by the compiler, cannot drift from a rename, and travels with the class
 * when `remove-demo.sh` deletes its package.
 *
 * @param accessPoint the point's `id:` in app-profile. An id no point declares is a BUILD ERROR —
 *                    a binding for an endpoint that does not exist would fail at graph construction.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ApiBinding(val accessPoint: String)
