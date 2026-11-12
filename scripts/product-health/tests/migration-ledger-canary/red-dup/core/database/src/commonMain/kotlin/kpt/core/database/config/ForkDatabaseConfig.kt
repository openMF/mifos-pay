/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.config

/**
 * The fork's Room schema version. `owner: fork` — PRESERVED across `/kmp-project-template-sync`.
 *
 * GENERATED from `app-profile/migration-ledger.yaml#version` by `./gradlew syncForkConfig`; do not
 * hand-edit. Add a migration by adding a row to that ledger.
 *
 * This replaced `AppDatabase.TEMPLATE_BASE_VERSION + VERSION_OFFSET`. That scheme let the template
 * contribute to the fork's version number, which cannot work: Room's version is ONE monotonic
 * integer and migrations are edges between consecutive values, so a template bump SHIFTED the
 * fork's numbering out from under its installed devices. A fork at base 13 + offset 1 shipped v14;
 * the template bumped base to 14; the fork computed 15 while devices sat at 14, and Room found no
 * 14 -> 15 edge.
 *
 * The fork now owns the sequence outright. The template contributes `migration_units` — changes with
 * a stable id and NO version — which `syncForkConfig` appends to this fork's ledger at its next free
 * slot. Same unit, different version per fork, no collision.
 */
object ForkDatabaseConfig {
    const val VERSION = 13
}
