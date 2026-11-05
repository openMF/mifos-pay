/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.network

import io.github.jan.supabase.SupabaseClientBuilder

/**
 * Fork seam for the supabase-kt modules the default exposer does not install.
 *
 * [SupabaseConfigClient] installs Postgrest only — Auth / Realtime / Storage are opt-in. A fork that
 * needed one of them used to have no way to say so, so it built its OWN `createSupabaseClient`.
 * That produced TWO clients for one access point: the fork signed in on its own, while the generated
 * `supabaseApi(...)` binding handed `@ApiBinding` types the other one —
 * which carried no session, so every RLS-gated call resolved no `auth.uid()`. The only escape was to
 * abandon `@ApiBinding` and hand-wire the singles, i.e. exactly the drift NAP-7 refuses.
 *
 * Binding one of these in the fork's own Koin module keeps it to ONE client per point: sign-in and
 * every table API share it, and the annotation keeps doing the wiring.
 *
 * ```kotlin
 * single<SupabaseExtrasProvider> {
 *     SupabaseExtrasProvider { id ->
 *         when (id) {
 *             // Sign-in (Google, Apple, guest): io.github.mobilebytelabs:cmp-supabase-auth installs Auth +
 *             // ComposeAuth onto THIS client. Pair it with `kmpSupabaseAuthNetwork(config) {
 *             //   get<SupabaseClientFactory>().requireClientFor(AUTH_PROJECT).client }` — see mbs/cappy.
 *             // secureStorage (kmp-supabase-auth 0.3.3) keeps the session in encrypted storage.
 *             AUTH_PROJECT -> kmpSupabaseAuthInstall(authConfig) { secureStorage(get(named("secure"))) }
 *             ANALYTICS_PROJECT -> {
 *                 { install(Realtime) }
 *             }
 *             // Shared across several projects — a `when` branch takes a comma-separated list, which
 *             // an if/else chain cannot express without repeating the block.
 *             PROJECT_A, PROJECT_B -> {
 *                 { install(Storage) }
 *             }
 *             else -> {
 *                 {}
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * Keyed by access-point id because a fork may run several projects and want Auth on only one. Use
 * `when` rather than `if (id != X)`: the negated-if shape reads as "one project, everything else is
 * a no-op" and quietly stops scaling the moment a second project appears — `when` makes the N-project
 * case and the shared case (comma-separated branch) the natural way to write it, with `else -> {}` as
 * the explicit "this point needs nothing beyond Postgrest".
 */
fun interface SupabaseExtrasProvider {
    /** Extra modules to install on [id]'s client; an empty block for points that need none. */
    fun forId(id: String): SupabaseClientBuilder.() -> Unit
}
