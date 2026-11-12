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
 * "Top-level" cannot be read off the signature. `cloudTodoGraph` has the exact shape of a top-level
 * entry but belongs to `ForkShowcaseRegistry`, not the feature graph, and `loansGraph` has a
 * `*Graph` name but is wired in `ForkFeatureRegistry`. A convention keyed on `*Graph` vs
 * `*Destination` gets both wrong. Registration is a decision, so it is declared.
 *
 * The annotated function MUST have the signature `NavGraphBuilder.(NavController) -> Unit` — that is
 * the shape the generated aggregate invokes — and MUST sit in the feature's `navigation` package, the
 * only one the aggregate reads (elsewhere it compiles and is never registered). A destination needing
 * extra collaborators resolves them from the NavController (e.g. `navController.popBackStackSafely()`).
 *
 * Only a self-contained feature is annotated. Features never depend on each other, so a graph that
 * opens another feature's screen, or shows another feature's widget, takes that link as a callback or
 * a composable slot and is wired in `ForkFeatureRegistry` (e.g. `loansGraph(navController,
 * onAmortizationClick)`).
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
public annotation class FeatureDestination
