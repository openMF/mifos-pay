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

import java.io.File

/**
 * The project's license header — ONE source, every file.
 *
 * `spotless/license-header.txt` holds the header as plain lines (no comment syntax). Two tokens:
 * `$YEAR` (Spotless's year token, kept for it) and `$ORG_NAME` — the copyright holder, resolved from
 * `app-profile/app.yaml#org.name`, the same company name every other surface already reads. It is
 * **fork-owned** (`customization-surface.yaml`), so a fork changes its header wording in that one file,
 * its company name in app-profile, and a template sync overwrites neither. Everything that writes a
 * header renders it from here:
 *
 * - Spotless (`Spotless.kt`) — every `.kt`, `.kts` and `.xml` in the build, via `spotlessApply`;
 * - `syncForkConfig` — the source files it generates and commits;
 * - the KSP processors (`tools/<name>-ksp`) — via the [KSP_OPTION] set in `KMPKoinConventionPlugin`.
 *
 * It replaced three per-language copies (`spotless/copyright.{kt,kts,xml}`) whose text could — and
 * did — drift: a `See See` typo in the `.kt` copy was stamped into 1,147 files while the other two
 * were correct, and the "Mifos Initiative" holder could not be changed by a fork without editing
 * three template-owned files that the next sync overwrote.
 */
object LicenseHeader {
    /** Repo-relative path of the one header source; fork-owned, never overwritten by a template sync. */
    const val SOURCE = "spotless/license-header.txt"

    /** The template's copyright holder — the header TEMPLATE code carries in every fork. */
    const val TEMPLATE_HOLDER = "Mifos Initiative"

    /**
     * Spotless `onlyIfContentMatches` pattern: matches a file that does NOT already carry the template's
     * copyright. A fork's header is applied to every file, and a template-owned file (`Kpt*`, `core-base/`,
     * module build scripts) re-stamped with the fork's holder stops being byte-identical to the template —
     * the next sync overwrites it back and the ownership audit flags it as fork-modified (13 core/ui files on
     * mbs/reels-downloader after one `spotlessApply`). Template code keeps its own header; in the template
     * itself the holder is the same, so nothing changes there.
     */
    const val NOT_TEMPLATE_CODE = "\\A(?![\\s\\S]*Copyright \\d{4} $TEMPLATE_HOLDER)"

    /** KSP option carrying the rendered Kotlin header to `tools/<name>-ksp` (newlines escaped as `\n`). */
    const val KSP_OPTION = "kpt.licenseHeader"

    /**
     * The header lines with `$ORG_NAME` resolved, trailing blank lines dropped. Missing file → no header
     * (never a hardcoded one): nothing writes one, and Spotless leaves existing headers alone.
     */
    fun lines(root: File): List<String> {
        val org = orgName(root)
        return File(root, SOURCE).takeIf(File::isFile)?.readLines()?.dropLastWhile(String::isBlank).orEmpty()
            .map { it.replace("\$ORG_NAME", org) }
    }

    /**
     * `app-profile/app.yaml#org.name` — the company name. Read with a line scan, not a YAML library:
     * this runs while Gradle configures Spotless, and `org.name` is a plain top-level scalar. Missing →
     * "the project authors", so a header is never written with a dangling token.
     */
    fun orgName(root: File): String {
        val yaml = File(root, "app-profile/app.yaml").takeIf(File::isFile)?.readLines().orEmpty()
        var inOrg = false
        for (line in yaml) {
            if (line.isNotEmpty() && !line[0].isWhitespace()) inOrg = line.trimEnd().startsWith("org:")
            if (inOrg) {
                Regex("""^ {2}name:\s*(.+?)\s*(#.*)?$""").find(line)?.let { m ->
                    return m.groupValues[1].trim().removeSurrounding("\"").removeSurrounding("'")
                }
            }
        }
        return "the project authors"
    }

    /** `/* … */` block for Kotlin / KTS. [year] replaces `$YEAR` (keep the token for Spotless). */
    fun kotlin(root: File, year: String = "\$YEAR"): String {
        val body = lines(root)
        if (body.isEmpty()) return ""
        return buildString {
            append("/*\n")
            body.forEach { append(if (it.isBlank()) " *\n" else " * $it\n") }
            append(" */")
        }.replace("\$YEAR", year)
    }

    /** XML declaration + `<!-- … -->` block, matching the declaration Spotless's XML step keeps first. */
    fun xml(root: File, year: String = "\$YEAR"): String {
        val body = lines(root)
        if (body.isEmpty()) return ""
        return buildString {
            append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<!--\n")
            body.forEach { append(if (it.isBlank()) "\n" else "    $it\n") }
            append("-->")
        }.replace("\$YEAR", year)
    }

    /** The year already stamped in [file]'s header, so regenerating a file never churns it. */
    fun existingYear(file: File): String? =
        file.takeIf(File::isFile)?.useLines { seq ->
            seq.take(4).firstNotNullOfOrNull { Regex("""Copyright (\d{4})""").find(it)?.groupValues?.get(1) }
        }
}
