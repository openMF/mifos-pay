/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.nav

/**
 * Marks a `NavGraphBuilder` extension as a TOP-LEVEL feature destination — one the app shell
 * registers directly on the authenticated graph.
 *
 * `:cmp-navigation:generateFeatureDestinations` scans for this and emits
 * `GeneratedFeatureDestinations`, so adding a feature no longer means editing `ForkFeatureRegistry`.
 *
 * WHY AN ANNOTATION RATHER THAN A NAMING CONVENTION
 * "Top-level" cannot be read off the signature. `amortizationScheduleDestination` has the exact
 * shape of a top-level entry but is nested INSIDE `loansGraph`; `cloudTodoGraph` is a top-level
 * shape too, but belongs to `ForkShowcaseRegistry`, not the feature graph. A convention keyed on
 * `*Graph` vs `*Destination` gets both wrong. Registration is a decision, so it is declared.
 *
 * The annotated function MUST have the signature `NavGraphBuilder.(NavController) -> Unit` — that is
 * the shape the generated aggregate invokes. A destination needing extra collaborators resolves them
 * from the NavController (e.g. `navController.popBackStackSafely()`) rather than widening the
 * signature, which would make it un-aggregatable.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
public annotation class FeatureDestination
