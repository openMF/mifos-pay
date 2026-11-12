/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.config

/**
 * THE FORK'S branding for the shared empty / error / no-network / loading visuals. Neutral on the
 * template — this is yours to fill.
 *
 * Implements the TEMPLATE-owned [ScreenStateOverrides]. [appScreenStateDefaults] builds the
 * framework defaults (localized copy, bundled Lottie specs, the categorised error mapper) and then
 * hands them to `customize`, so a fork overrides only what it rebrands and inherits the rest:
 *
 * ```kotlin
 * object ForkScreenStateDefaults : ScreenStateOverrides {
 *     override fun customize(defaults: ScreenStateDefaults): ScreenStateDefaults = defaults.copy(
 *         empty = defaults.empty.copy(visual = ScreenStateVisual.Lottie(spec = MyBrandAnimations.empty)),
 *         error = defaults.error.copy(onShown = { e -> AppTelemetry.recordError("screen_state_error", e) }),
 *     )
 * }
 * ```
 *
 * The template file used to carry `!! THIS IS THE FORK CUSTOMIZATION POINT !!` and two `TODO(fork)`
 * markers, which made all 103 lines `owner: fork`: rebranding one Lottie spec cost a fork every
 * later upstream fix to the wiring around it. Splitting the seam out is what lets both sides keep
 * developing — the template evolves `config/KptScreenStateDefaults.kt` and a sync full-copies it,
 * the fork owns this file, and a sync never has to merge them.
 *
 * The contract lives on the interface rather than on a top-level extension function so a NEW hook
 * added by the template arrives with a default body and this object keeps compiling across the sync
 * that introduces it.
 */
object ForkScreenStateDefaults : ScreenStateOverrides
