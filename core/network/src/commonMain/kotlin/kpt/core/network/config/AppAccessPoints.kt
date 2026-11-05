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

import kpt.core.base.network.AccessPoint
import kpt.core.base.network.AccessPointKind
import kpt.core.base.network.HttpLogDetail
import kpt.core.base.network.NetworkPolicy

/**
 * The per-fork list of network access points this app talks to — REST and Supabase in ONE place.
 *
 * **SoT: `app-profile/app.yaml#network.access_points`.** A fork declares its N typed endpoints there and
 * runs `./gradlew syncForkConfig`, which regenerates the sentinel-bounded [points] block below (do not
 * hand-edit it). The framework-owned `AccessPointRegistry` (core-base/network) wraps this list — the
 * `NetworkModule` binds `single { AccessPointRegistry(AppAccessPoints.points) }` — and the `restApi("<id>")`
 * DSL resolves transports from it, so a fork only writes API interfaces + one `restApi("<id>")` line each.
 *
 * The default entries are real `https://` URLs (no `YOUR_` placeholder) so the Supabase point is
 * default-configured, not inert. A fork replaces these via app-profile + syncForkConfig.
 */
object AppAccessPoints {
    // syncForkConfig:access-points:begin — GENERATED from app-profile/app.yaml#network.access_points.
    // Edit access points THERE (the SoT) and run `./gradlew syncForkConfig`; do not hand-edit this block.
    // `type` defaults to UrlType(id.uppercase()) — value-class-equal to the AppUrlTypes.* constants.
    /** This project's declared access points, in app-profile order. */
    val points: List<AccessPoint> = listOf(
        AccessPoint(
            id = "main",
            kind = AccessPointKind.REST,
            baseUrl = "https://api.openmf.org/",
            loggableHost = "api.openmf.org",
        ),
        AccessPoint(
            id = "lwmswhoxvvoagzkqxiyd",
            kind = AccessPointKind.SUPABASE,
            baseUrl = "https://lwmswhoxvvoagzkqxiyd.supabase.co",
            loggableHost = "lwmswhoxvvoagzkqxiyd.supabase.co",
            supabase = kpt.core.base.network.SupabaseSpec(
                publishableKey = kpt.core.network.BuildKonfig.SUPABASE_PUBLISHABLE_KEY,
            ),
        ),
    )

    /** Network privacy policy — app-profile `network.logging` / `network.web_cors_proxy`. */
    val policy: NetworkPolicy = NetworkPolicy(
        releaseLogging = HttpLogDetail.NONE,
        debugLogging = HttpLogDetail.BODY,
        webCorsProxy = "/cors",
    )
    // syncForkConfig:access-points:end
}
