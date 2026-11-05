/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.kmp.core.base.library.convention)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(libs.calf.permissions)

            // KmpToolkit IPC modules — power the cross-platform IntentManager impl
            // in nonAndroidMain (Android keeps its native ACTION_SEND/ACTION_VIEW path).
            implementation(libs.cmp.share)
            implementation(libs.cmp.intent.launcher)
            implementation(libs.cmp.open.url)
            implementation(libs.cmp.inapp.update)
            // Review, clipboard, app-intents, bubble and PDF. Each resolves the target itself, so
            // none needs an expect/actual here. All but review are bound in platformModule as the
            // toolkit's own types rather than behind a template wrapper — see the note there.
            // `api`, not `implementation`: these types appear in this module's PUBLIC signatures —
            // LocalClipboardManager / LocalBubbleManager / LocalPdfManager / LocalAppIntentsManager
            // are typed with them, and AppReviewManager.capabilities returns AppReviewCapabilities.
            // Declared `implementation` they compile here but a consumer reading
            // `LocalClipboardManager.current` cannot name the type it gets back.
            api(libs.cmp.app.review)
            api(libs.cmp.clipboard)
            api(libs.cmp.app.intents)
            api(libs.cmp.bubble)
            api(libs.cmp.pdf.generator)
            // Bound (not rendered) here: toastModule binds ONE ToastHostState and exposes the same
            // instance as ToastDispatcher, so a ViewModel injecting the dispatcher and the
            // KptToastHost rendering the state share one queue. The composable lives in
            // core-base/designsystem.
            implementation(libs.cmp.toast)
            // Needed to PROVIDE these libraries' own CompositionLocals from LocalManagerProvider.
            // They default to constructing a separate impl when unprovided, so a fork importing the
            // toolkit's LocalShareManager instead of the template's would get a second object with
            // no error. Providing them here points both names at the same DI-bound instance.
            implementation(libs.cmp.share.compose)
            implementation(libs.cmp.intent.launcher.compose)
            implementation(libs.cmp.app.intents.compose)
            // The CompositionLocals read their managers OUT of Koin rather than constructing a
            // second copy — platformModule is the single owner. Same pattern as core-base/security.
            implementation(libs.koin.compose)

            // Explicit (rather than transitive via compose.runtime) — nonAndroidMain
            // IntentManagerImpl owns its own CoroutineScope for fire-and-forget dispatch.
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.ktx)
            implementation(libs.androidx.activity.compose)

            implementation(libs.androidx.metrics)
            implementation(libs.androidx.browser)
            implementation(libs.androidx.compose.runtime)

            implementation(compose.material3)

            // Play Core review (libs.review / libs.review.ktx) was removed here: cmp-app-review
            // brings its own Play In-App Review path on Android and a real implementation on every
            // other target, replacing the non-Android no-op. With review no longer needing an
            // Activity, no manager is Activity-bound and LocalManagerProvider's android/nonAndroid
            // split is now a single commonMain implementation.
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
