/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.platform.review

import com.mobilebytelabs.kmptoolkit.appreview.AppReview
import com.mobilebytelabs.kmptoolkit.appreview.AppReviewCapabilities

/**
 * The single implementation of [AppReviewManager], for every target.
 *
 * Replaces the previous androidMain/nonAndroidMain pair. The Android half drove Play Core's
 * `ReviewManagerFactory` and needed an `Activity`; the non-Android half was
 * `override fun promptForReview() {}` plus a `// TODO:: Implement custom review flow`, so every
 * iOS, desktop and web fork shipped a review prompt that silently did nothing.
 *
 * `cmp-app-review` resolves the target itself:
 *
 * | Target | Route |
 * |---|---|
 * | Android | Play In-App Review → `market://details?id=…` |
 * | iOS / macOS | `SKStoreReviewController` → store review composer |
 * | Everything else | configured store listing opened via `cmp-open-url` |
 *
 * Because it needs no `Activity`, this binds in `platformModule` alongside the other managers
 * instead of being constructed inside composition.
 *
 * Unlike the other managers here there is no `toolkit` escape hatch, because there is nothing to
 * hold: the library's entry point is the global `AppReview` object, reachable from anywhere a fork
 * already depends on `cmp-app-review`. [capabilities] surfaces the part worth reading through DI.
 *
 * ## Configuring the store listing (fork step)
 * The native flows on Android, iOS and macOS need no configuration. The fallback used by desktop
 * and web does: call `AppReview.configure(StoreListing(...))` once at startup with your store ids,
 * or bind the toolkit's `appReviewModule(listing)`. Without it [canRequestReview] reports `false`
 * on those targets — deliberately, so a fork hides the affordance rather than showing a dead button.
 */
class AppReviewManagerImpl : AppReviewManager {

    override val capabilities: AppReviewCapabilities get() = AppReview.capabilities

    override val canRequestReview: Boolean get() = capabilities.canRequestReview

    override suspend fun promptForReview() {
        AppReview.requestReview()
    }

    override fun promptForCustomReview() {
        AppReview.openStoreListing()
    }
}
