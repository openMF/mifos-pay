/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.firebase.di

import io.github.mobilebytelabs.kmptoolkit.firebase.FirebaseKit
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsConfig
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsHelper
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.di.AnalyticsModule
import io.github.mobilebytelabs.kmptoolkit.firebase.crashlytics.CrashReporter
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Base Firebase integration (the "main implementation", template-owned in core-base/firebase).
 *
 * Binds the published `cmp-firebase` engine's **real Firebase-backed** [AnalyticsHelper] (GitLive on
 * supported targets, Measurement-Protocol/NoOp fallback elsewhere) + its [PerformanceTracker]. This
 * is the production wiring. Collection still starts OFF and follows the user's consent — including this
 * module makes analytics AVAILABLE, never enabled without agreement.
 *
 * The project layer (`core/firebase`) supplies the toolkit **default** (NoOp — privacy-respecting
 * demo build) plus the app-owned domain event catalog; last-binding-wins lets a fork swap in a
 * different provider. Crashlytics wiring will join this module as core-base/firebase grows into the
 * single host for all Firebase services.
 */
val firebaseModule: Module = module {
    single<AnalyticsHelper> {
        AnalyticsModule.analyticsHelper(
            mode = AnalyticsModule.Mode.Firebase,
            // Collection starts OFF on every platform: analytics is consent-based processing (GDPR), and
            // nothing may be sent before the user's stored choice is read. The app shell's consent applier
            // turns it on only when the user has agreed (seeded by app-profile/app.yaml#privacy).
            config = AnalyticsConfig(collectionEnabledByDefault = false),
        )
    }
    single { AnalyticsModule.performanceTracker(get()) }

    // The SAME reporter the library auto-installs at process start (Android FirebaseInitProvider), so a
    // consent change reaches the instance that is actually capturing. Collection follows the user's consent
    // (setCollectionEnabled, driven by the app shell's consent applier); install() no longer forces it on.
    single<CrashReporter> { FirebaseKit.crashReporter }
}
