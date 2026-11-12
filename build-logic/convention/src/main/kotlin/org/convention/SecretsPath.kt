/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.convention

import org.gradle.api.Project
import java.io.File
import java.io.StringReader
import java.util.Properties

/**
 * Resolve a secret's on-disk path for the Gradle build by READING the resolver-generated map
 * `gradle/secrets-paths.properties` — NEVER by shelling out.
 *
 * `deployment/_shared/lib/build_secrets.rb` (the one resolver over `secrets/LAYOUT.yaml`, SoT) stays the
 * single algorithm; its `export-paths` command emits `key=<repo-relative-path>` for every file-like secret
 * (`live`-wins-else-`sample`). That map is generated OUTSIDE Gradle — by the CI `pre_fastlane_script` /
 * Fastlane pre-step / `/secrets pull`, all of which run before the build and have Ruby — and Gradle merely
 * looks a key up in it. This is deliberate: a config-time subprocess (the old `build-secrets path <key>`
 * exec) is Windows-shebang-fragile, hostile to the configuration cache, and runs on EVERY build including
 * the Desktop/Web checks that never sign Android. A file read via [org.gradle.api.provider.ProviderFactory.fileContents]
 * is cross-platform, config-cache-correct, and free.
 *
 * Still NOT a parallel resolver (RULE-SECRETS-LAYOUT-001 / check-secrets-resolver.sh SR-7/SR-12): Gradle
 * never parses `secrets/LAYOUT.yaml` and never hardcodes a `secrets/live/…` path — it only consumes the
 * resolver's own output. When the map is absent (e.g. a PR-check build that never materializes secrets) or
 * lacks the key, it returns an under-`build/` sentinel so configuration of a non-signing build always
 * succeeds; a real Android release-signing task supplies the keystore via the `KEYSTORE_PATH` env override
 * (see cmp-android signingConfigs), so it never depends on this fallback. Returns an absolute [File].
 */
fun Project.resolveSecretPath(key: String): File {
    val repoRoot = rootProject.projectDir
    val mapText = providers.fileContents(
        rootProject.layout.projectDirectory.file("gradle/secrets-paths.properties"),
    ).asText.orNull
    if (mapText != null) {
        val props = Properties().apply { load(StringReader(mapText)) }
        props.getProperty(key)?.trim()?.takeIf { it.isNotEmpty() }?.let { return File(repoRoot, it) }
    }
    return File(repoRoot, "build/secrets-unresolved/$key")
}

/**
 * The trimmed text of a `kind: value` secret's file — [resolveSecretPath] (`secrets/live/` else the committed
 * `secrets/sample/` fake) — or null when it is absent or blank. For build-time client keys compiled into
 * BuildKonfig: the generated field reads the env var first (CI), then this. Read through
 * [org.gradle.api.provider.ProviderFactory.fileContents] so the configuration cache tracks the file.
 */
fun Project.readSecretValue(key: String): String? {
    val file = objects.fileProperty().fileValue(resolveSecretPath(key))
    return providers.fileContents(file).asText.orNull?.trim()?.takeIf { it.isNotEmpty() }
}
