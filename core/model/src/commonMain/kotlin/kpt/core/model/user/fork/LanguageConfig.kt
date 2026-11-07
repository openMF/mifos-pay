/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.user.fork

/**
 * Represents the languages supported by the app.
 */
enum class LanguageConfig(
    /** Locale code, or null for the system default. */
    val localeName: String?,
    /** Name of the language shown in the UI. */
    val text: String,
) {
    /** Follow the system language. */
    DEFAULT(
        localeName = null,
        text = "System Default",
    ),

    /** Bengali. */
    BENGALI(
        localeName = "bn",
        text = "বাংলা",
    ),

    /** Spanish. */
    SPANISH(
        localeName = "es",
        text = "Español",
    ),

    /** French. */
    FRENCH(
        localeName = "fr",
        text = "Français",
    ),

    /** Hindi. */
    HINDI(
        localeName = "hi",
        text = "हिन्दी",
    ),

    /** Indonesian. */
    INDONESIAN(
        localeName = "id",
        text = "Bahasa Indonesia",
    ),

    /** Portuguese. */
    PORTUGUESE(
        localeName = "pt",
        text = "Português",
    ),

    /** Swahili. */
    SWAHILI(
        localeName = "sw",
        text = "Kiswahili",
    ),
}
