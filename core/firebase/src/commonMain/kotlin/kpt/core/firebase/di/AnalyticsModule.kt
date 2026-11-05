/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.firebase.di

import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsHelper
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.di.AnalyticsModule
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Project-layer Koin module — the toolkit **default** analytics binding.
 *
 * Binds [AnalyticsHelper] to the `cmp-firebase` library's **NoOp** mode: the toolkit ships WITHOUT
 * analytics (privacy-respecting demo build), so every event call is a silent no-op.
 *
 * Fork overrides (last-binding-wins):
 * - **Production Firebase** — include [kpt.core.base.firebase.di.firebaseModule] (real GitLive
 *   Firebase via `AnalyticsModule.Mode.Firebase`) instead of / after this module.
 * - **Debug logging** — `single<AnalyticsHelper> { AnalyticsModule.analyticsHelper(AnalyticsModule.Mode.Stub) }`.
 * - **Your own provider** — `single<AnalyticsHelper> { MixpanelAnalyticsHelper(...) }`.
 *
 * The app-owned cross-cutting catalog lives in `config/analytics/` + `config/crashlytics/` in this
 * project layer, separate from the generic library. A feature's own vocabulary lives outside
 * `config/`, in `kpt/core/firebase/<feature>/` — see `loans/`.
 */
val coreFirebaseModule: Module = module {
    single<AnalyticsHelper> { AnalyticsModule.analyticsHelper(AnalyticsModule.Mode.NoOp) }
}
