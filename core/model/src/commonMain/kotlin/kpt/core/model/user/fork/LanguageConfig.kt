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
    val localeName: String?,
    val text: String,
) {
    DEFAULT(
        localeName = null,
        text = "System Default",
    ),

    BENGALI(
        localeName = "bn",
        text = "বাংলা",
    ),

    SPANISH(
        localeName = "es",
        text = "Español",
    ),

    FRENCH(
        localeName = "fr",
        text = "Français",
    ),

    HINDI(
        localeName = "hi",
        text = "हिन्दी",
    ),

    INDONESIAN(
        localeName = "id",
        text = "Bahasa Indonesia",
    ),

    PORTUGUESE(
        localeName = "pt",
        text = "Português",
    ),

    SWAHILI(
        localeName = "sw",
        text = "Kiswahili",
    ),
}
