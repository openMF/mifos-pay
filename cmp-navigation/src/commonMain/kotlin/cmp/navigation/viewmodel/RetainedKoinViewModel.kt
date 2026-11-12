/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import org.koin.compose.viewmodel.koinNavViewModel

/**
 * NavBackStackEntry-scoped ViewModel acquisition. The VM is retained across
 * recomposition AND across sibling nav destinations, cleared only when the
 * enclosing destination pops off the back-stack. Screens call this instead of
 * [org.koin.compose.viewmodel.koinViewModel] when they want their VM to survive
 * a tab switch or a forward-then-back nav return. VMs are still `viewModel { }`
 * factory bindings in feature Koin modules — this is a scope change at the
 * acquisition site, NOT a Koin binding-kind change (the D14 fence still holds).
 *
 * ## VM scoping enumeration (Phase 3, D9/D14)
 *
 * ### Migrated to retainedKoinViewModel() (nav-scoped):
 *   - kpt.feature.loans.ui.PersonalLoansListViewModel   (list screen)
 *   - kpt.feature.home.ui.HomeViewModel                 (bottom-nav tab; the
 *                                                        combined-state fan-in
 *                                                        exercised by
 *                                                        HomeDashboardViewModelTest)
 *
 * ### Kept as koinViewModel() (Activity-scoped):
 *   - kpt.feature.loans.ui.LoanDetailViewModel   (parameterized; transient per detail open)
 *   - kpt.feature.loans.ui.EditLoanViewModel     (parameterized; transient per edit open)
 *   - cmp.navigation.AppViewModel                                          (chrome; outside destinations)
 *   - cmp.navigation.rootnav.RootNavViewModel                              (chrome; outside destinations)
 *   - cmp.navigation.splash.SplashViewModel                                (chrome; outside destinations)
 *   - cmp.navigation.authenticatednavbar.AuthenticatedNavbarNavigationViewModel (chrome; the tab shell itself)
 *   - every other feature VM not listed above    (default until a screen opts in)
 *
 * ### D14 fence
 *
 * No `single<*ViewModel>` binding may appear in any feature Koin module. All
 * VMs above are `viewModel { }` factory bindings (verified today at
 * `feature/loans/src/commonMain/kotlin/kpt/feature/loans/di/LoansModule.kt:27`
 * and its peers). Static enforcement: `grep -rnE "single<[A-Za-z]*ViewModel>"`
 * over `feature/<module>/src/commonMain/kotlin` and
 * `cmp-navigation/src/commonMain/kotlin` expected `0 matches` at Acceptance G-3.
 *
 * ## Direct-import deviation from the 03-vm-scoping.md sub-plan
 *
 * The sub-plan expected feature screens to `import
 * cmp.navigation.viewmodel.retainedKoinViewModel` and consume this helper
 * directly. That resolution is architecturally impossible in the real module
 * graph: `:cmp-navigation` already depends on `:feature:home` and
 * `:feature:loans` (see `cmp-navigation/build.gradle.kts` `projects.feature.*`
 * lines), so a features → `:cmp-navigation` edge would be cyclic. The features
 * therefore consume the underlying symbol directly via
 *
 *   `import org.koin.compose.viewmodel.koinNavViewModel as retainedKoinViewModel`
 *
 * which preserves the sub-plan's call-site naming contract (`viewModel: X =
 * retainedKoinViewModel()`) without introducing the illegal edge. This helper
 * (a) compile-time-verifies that `koinNavViewModel` resolves on every KMP
 * target `:cmp-navigation` compiles, and (b) is the single canonical location
 * of the migration enumeration above.
 */
@Suppress("unused")
@Composable
inline fun <reified VM : ViewModel> retainedKoinViewModel(): VM = koinNavViewModel()
