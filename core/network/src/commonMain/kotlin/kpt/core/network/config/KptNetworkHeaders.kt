/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.config

import kpt.core.base.network.DefaultHeaderProvider

/**
 * THE FORK'S default request headers. Neutral on the template — this is yours to fill.
 *
 * Implements the TEMPLATE-owned [DefaultHeaderProvider], so a new hook added upstream arrives with a
 * default body and this object keeps compiling — the same inheritance contract as
 * `ForkErrorMapper` / `ForkScreenStateDefaults` in `core/store`.
 *
 * Every REST client built by `restApi(...)` resolves this from Koin, so a header added here is sent
 * on every request to the access point you name — no client construction, no bypassing the generated
 * `@ApiBinding` wiring.
 *
 * ```kotlin
 * object KptNetworkHeaders : DefaultHeaderProvider {
 *     override fun headersFor(accessPointId: String): Map<String, String> = when (accessPointId) {
 *         "main" -> mapOf(
 *             "X-Client-Version" to BuildKonfig.VERSION_NAME,
 *             "X-Platform" to platformName(),
 *         )
 *         else -> emptyMap()
 *     }
 * }
 * ```
 *
 * Scope by access point rather than returning one map for every id: a key belongs to ONE endpoint,
 * and sending it everywhere hands a credential to hosts that never needed it.
 *
 * NOT here: `Authorization` (use `setupDefaultHttpClient`'s `authProviders` — a token pinned as a
 * header is captured once and never refreshes), and anything per-REQUEST such as a trace id, since
 * this map is read once per client rather than once per call.
 */
object KptNetworkHeaders : DefaultHeaderProvider
