/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.convention

import org.gradle.api.Project
import java.util.Properties

/**
 * The ONE Gradle-side reader of `gradle/fork.properties`.
 *
 * `app-profile` (app.yaml + platforms) is the fork SoT; `scripts/white-label/derive.rb` materializes
 * it into the derived build-bridge `gradle/fork.properties`; everything downstream READS that bridge.
 * This object is the Gradle end of that contract — the direct counterpart of `fp_get()` in
 * `scripts/product-health/lib.sh`, whose header states the rule this file exists to extend:
 *
 * > "EVERY health check reads it through fp_get() so there is exactly one parser and one SoT —
 * >  a check never re-implements the read."
 *
 * That discipline held inside `scripts/product-health/` and nowhere else: six Gradle files each ran
 * their own `Properties().load()` with three different helper shapes and three different
 * missing-key defaults (`""`, a caller default, and a hardcoded `"App"`). Independent parsers are how
 * the resolution order silently diverges — the 2026-09-06 signing split, where `deployment/Appfile`
 * preferred the FILE and `_shared/project_config.rb` preferred the ENV for the same
 * `apple.team.id`, was exactly that failure at the Ruby layer.
 *
 * ## Semantics
 * - An ABSENT file is not an error. On a fresh clone (`fork.properties` is gitignored — it carries
 *   per-fork filled-in values) every key resolves to its caller-supplied default. Callers must always
 *   pass a default that is safe for an unbranded build.
 * - A key present but blank is treated as ABSENT. `derive.rb` omits placeholder values rather than
 *   writing them, so "" and "missing" mean the same thing: not authored yet.
 * - Reads are cached per [Project] build — the file is parsed once, not once per call site.
 *
 * ## What this is NOT
 * It does not consult environment variables. `SyncForkConfigPlugin` owns the full override chain —
 * `ENV > app-profile > fork.properties > libs.versions.toml > ""` (SyncForkConfigPlugin.kt:91) —
 * because it WRITES the derived surfaces. A consumer reading the already-derived bridge must not
 * re-litigate that order, or the two disagree again.
 */
object KptAppProfileProperties {

    private const val CACHE_KEY = "org.convention.forkProperties.cache"

    /** Parse (once per build) the derived bridge. Absent file → empty, never an error. */
    private fun load(project: Project): Properties {
        val root = project.rootProject
        @Suppress("UNCHECKED_CAST")
        (root.extensions.extraProperties.takeIf { it.has(CACHE_KEY) }?.get(CACHE_KEY) as? Properties)
            ?.let { return it }

        val props = Properties()
        val file = root.file("gradle/fork.properties")
        if (file.exists()) file.inputStream().use { props.load(it) }
        root.extensions.extraProperties.set(CACHE_KEY, props)
        return props
    }

    /**
     * Value for [key], or [default] when the key is absent OR blank.
     *
     * @param default MUST be safe for an unbranded build — never the template's own org identity.
     *   `fork-identity.sh` fails CI on a fork still carrying the template's brand, so a default that
     *   leaks it would turn a missing-config bug into a branding bug.
     */
    fun get(project: Project, key: String, default: String = ""): String =
        load(project).getProperty(key)?.trim()?.takeIf { it.isNotEmpty() } ?: default

    /**
     * First non-blank value across [keys], else [default].
     *
     * For a key that was renamed: list the current name first and the legacy name after it, so a fork
     * that has not re-derived its bridge yet still resolves.
     */
    fun getFirst(project: Project, vararg keys: String, default: String = ""): String {
        val props = load(project)
        for (k in keys) {
            val v = props.getProperty(k)?.trim()
            if (!v.isNullOrEmpty()) return v
        }
        return default
    }
}

/** Convenience receiver form — `forkProp("app.display.name", "App")`. */
fun Project.forkProp(key: String, default: String = ""): String =
    KptAppProfileProperties.get(this, key, default)

/** Convenience receiver form — `forkPropFirst("store.title", "app.display.name", default = "App")`. */
fun Project.forkPropFirst(vararg keys: String, default: String = ""): String =
    KptAppProfileProperties.getFirst(this, *keys, default = default)
