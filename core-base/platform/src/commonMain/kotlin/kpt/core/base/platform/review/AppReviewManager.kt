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

import com.mobilebytelabs.kmptoolkit.appreview.AppReviewCapabilities

/**
 * Manages application review requests across platforms.
 *
 * This interface abstracts the platform-specific implementations for requesting
 * user reviews of the application. It provides methods to prompt users for app
 * reviews using either standard system-provided flows or custom-designed review
 * experiences.
 *
 * Platform-specific implementations of this interface typically integrate with:
 * - Google Play In-App Review API on Android
 * - StoreKit Review Controller on iOS
 * - Other platform-specific review mechanisms
 *
 * Review requests should be triggered at appropriate moments in the user journey
 * when the user has completed a meaningful interaction with the application and
 * is likely to have a positive experience to report.
 */
interface AppReviewManager {

    /**
     * What review means on THIS target: `nativeInAppReview`, `storeListing`, and the derived
     * `canRequestReview`.
     *
     * The full descriptor rather than only the boolean, so a fork can word its affordance
     * accurately — "Rate in the App Store" reads wrong when the native composer will appear
     * in-app, and vice versa.
     */
    val capabilities: AppReviewCapabilities

    /**
     * Whether a review can actually be requested on THIS target right now.
     *
     * Ask before rendering a "Rate this app" affordance. `false` means neither a native review
     * flow nor a store listing is reachable — on those targets a button would do nothing, which
     * is precisely what the previous non-Android implementation shipped.
     *
     * `true` on Android, iOS and macOS unconditionally (native review flows). Elsewhere it
     * depends on whether the fork configured a store listing — see [AppReviewManagerImpl].
     */
    val canRequestReview: Boolean

    /**
     * Prompts the user to review the app using the platform's standard review flow.
     *
     * This method triggers the native system review prompt, which is typically
     * managed by the platform to control frequency and prevent review fatigue.
     * The actual display of the review prompt may be deferred or throttled by
     * the platform based on internal policies.
     *
     * Where no native flow exists, this falls back to opening the configured store listing, so
     * the user's intent is still served rather than silently dropped.
     *
     * Suspending because the underlying platform APIs are asynchronous — Play's In-App Review
     * returns a task, and the previous non-suspend signature could only be honoured by ignoring
     * the result. Call it from `rememberCoroutineScope().launch { }` in composition.
     */
    suspend fun promptForReview()

    /**
     * Prompts the user to review the app using a custom application-defined review flow.
     *
     * This method initiates a custom review experience designed within the application,
     * which may include custom UI elements, multi-step processes, or conditional
     * logic before directing users to the appropriate store page for leaving a review.
     *
     * Custom review flows provide more control over the user experience but require
     * careful implementation to comply with platform guidelines and avoid potential
     * rejection during app review processes.
     *
     * Here it opens the configured store listing directly, skipping the native throttled prompt —
     * which is what a custom flow wants once it has decided the moment is right.
     */
    fun promptForCustomReview()
}
