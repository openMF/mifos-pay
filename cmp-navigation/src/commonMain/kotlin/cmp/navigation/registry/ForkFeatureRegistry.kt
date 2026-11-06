/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import cmp.navigation.registry.app.ForkAppModules
import kpt.core.data.di.ForkRepositoryModule
import kpt.core.database.di.ForkDatabaseModule
import kpt.core.datastore.di.ForkDatastoreModule
import kpt.core.network.di.ForkNetworkModule
import kpt.core.store.di.ForkStoreModule
import org.koin.core.module.Module

/**
 * ForkFeatureRegistry — the FORK-OWNED white-label seam for feature contributions.
 *
 * The template infra modules READ from this registry; a fork extends the app by editing THIS ONE file
 * (+ its build.gradle deps + settings.gradle include), never the template infra files:
 *   - `cmp-navigation/di/KoinModules.kt` includes [featureKoinModules] into the app DI graph.
 *   - `cmp-navigation/.../AuthenticatedNavigation.kt` invokes [featureDestinations] to register routes.
 *
 * Ownership: `owner: fork` in customization-surface.yaml — `sync-dirs`/`white-label-doctor` NEVER
 * overwrite it, so a template sync full-copies the infra modules while your features survive. The
 * template ships this file pre-populated with its demo feature set as the default; a fork replaces the
 * contents with its own (the customizer `--clean` empties both lists).
 */
object ForkFeatureRegistry {
    /**
     * The four per-layer fork seams, plus every `feature/<f>/di` Koin module.
     *
     * The feature half is DERIVED — `:cmp-navigation:generateFeatureKoinBindings` reads each
     * feature module's `di` package and emits [GeneratedFeatureKoinBindings]. Adding a
     * feature no longer means editing this file: previously it took an import AND a list entry
     * here, and forgetting either compiled cleanly while the feature's ViewModels failed to
     * resolve at runtime.
     *
     * The `Fork*Module` seams stay listed BY HAND on purpose — they are core-layer fork seams,
     * not features, and nothing under `feature/` declares them.
     *
     * Feature Koin modules the app installs. The framework SHELL modules (Home, Settings) live in
     * [cmp.navigation.di.KoinModules] and are always present; this is the fork's own features.
     */
    val featureKoinModules: List<Module> = listOf(
        ForkRepositoryModule,
        ForkNetworkModule,
        ForkDatabaseModule,
        ForkDatastoreModule,
        ForkStoreModule,
        GeneratedFeatureKoinBindings,
    ) + ForkAppModules.allModules // the wallet app shell rendered by ForkAppShell.root

    /**
     * Every top-level feature destination, DERIVED from `@FeatureDestination`.
     *
     * `:cmp-navigation:generateFeatureDestinations` scans each feature's `navigation` package and
     * emits [GeneratedFeatureDestinations]. Adding a screen to the app graph is now a matter of
     * annotating it where it is declared, rather than editing this file — which previously took an
     * import AND a call, and where a missed call meant a route that silently did not exist.
     *
     * Nested and non-feature-graph entries are left unannotated on purpose:
     * `amortizationScheduleDestination` is registered inside `loansGraph`, and `cloudTodoGraph`
     * belongs to ForkShowcaseRegistry.
     *
     * Feature nav destinations — registered into the authenticated graph. The shell destinations
     * (settings, notification) stay in [cmp.navigation.authenticated] template; this is the fork's routes.
     */
    val featureDestinations: NavGraphBuilder.(NavController) -> Unit = GeneratedFeatureDestinations
}
