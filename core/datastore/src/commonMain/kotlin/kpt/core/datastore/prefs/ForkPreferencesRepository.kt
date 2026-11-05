/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.prefs

/**
 * THE FORK'S preferences. Extends the framework's — this is yours to fill.
 *
 * ## The split
 * [UserPreferencesRepository] and its impl are `owner: template`: the framework DESIGNS and MANAGES
 * those preferences (theme, language, auth, passcode, biometrics, onboarding) and keeps improving
 * them. They FULL-COPY on a sync, so a fork must never edit them — an accessor added there
 * disappears on the next adopt while its value stays on disk.
 *
 * This file and [ForkPreferencesRepositoryImpl] are `owner: fork` and are never copied. Extending
 * rather than paralleling means a fork gets all framework preferences for free and adds its own
 * beside them, through ONE injected type.
 *
 * ## What you get by default
 * Every framework preference, inherited. Inject [ForkPreferencesRepository] and
 * `userData`, `observeLanguage`, `setDarkThemeConfig`… are all there — the impl delegates them to
 * the template's, so the template can change how any of them works and this file never notices.
 *
 * ## Adding your own
 * ```
 * interface ForkPreferencesRepository : UserPreferencesRepository {
 *     val observeMyFlag: Flow<Boolean>
 *     suspend fun setMyFlag(enabled: Boolean)
 * }
 * ```
 *
 * ## Altering a framework one
 * Override it in [ForkPreferencesRepositoryImpl] — delegation supplies the default, an explicit
 * `override` wins. The template's own implementation stays untouched and keeps evolving:
 * ```
 * override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
 *     analytics.log("theme_changed")
 *     delegate.setDarkThemeConfig(darkThemeConfig)
 * }
 * ```
 *
 * Framework preferences cannot be REMOVED — the framework reads them. Inheritance makes that
 * structural rather than a rule: drop one and this interface no longer satisfies its supertype.
 */
interface ForkPreferencesRepository : UserPreferencesRepository
