/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

/**
 * DevMenuEntry — a generic row in the Settings screen's "Developer" section.
 *
 * Any dev-only surface (showcase galleries, feature-flag toggles, diagnostics
 * screens, seed-data actions) contributes an entry via a registry that produces
 * `List<DevMenuEntry>` for the settings dev menu. The `feature/settings`
 * signature is agnostic — it iterates whatever list it's given, empty → hidden.
 *
 * Ownership: `owner: template` — the generic contract every fork inherits.
 *
 * Home note: this type lives in `feature/settings` (not `cmp-navigation/registry`)
 * because `cmp-navigation` already depends on `feature/settings`; homing it in
 * `cmp-navigation` would force `feature/settings -> cmp-navigation` and create a
 * circular module dependency. The dev-only registries in `cmp-navigation/registry`
 * (e.g. `ForkShowcaseRegistry`) import it from here.
 *
 * @property label Text shown for the entry in the dev menu.
 * @property onClick Invoked when the entry is tapped.
 */
data class DevMenuEntry(
    val label: String,
    val onClick: () -> Unit,
)
