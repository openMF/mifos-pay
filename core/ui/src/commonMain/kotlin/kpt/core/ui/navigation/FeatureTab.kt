/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.navigation

/**
 * Marks a [NavigationItem] object as a bottom-navigation tab contributed by a feature.
 *
 * `:cmp-navigation:generateFeatureTabs` collects these into `GeneratedFeatureTabs`, which
 * `ForkTabRegistry.extraTabs` exposes — so a feature declares its own tab instead of the fork editing
 * `ForkTabRegistry` in a module the feature does not own.
 *
 * The annotated declaration MUST be an `object` implementing [NavigationItem]: the aggregate holds
 * them in a `List<NavigationItem>`, so a class would have nothing to instantiate from and a
 * non-[NavigationItem] would not compile into the list.
 *
 * TAB ORDER is the order features are collected (alphabetical by module, then by file), after the
 * backbone's Home and Profile tabs. A tab whose position matters is a layout decision, not a
 * feature-local one — put it in `ForkTabRegistry.tabs` by hand rather than encoding priority here.
 *
 * An inline tab (the default, [NavigationItem.inlineTab]) must ALSO register its top screen on the
 * inner NavHost via `ForkTabRegistry.extraInlineTabDestinations`; this annotation adds the tab to the
 * bar, not its destination.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class FeatureTab
