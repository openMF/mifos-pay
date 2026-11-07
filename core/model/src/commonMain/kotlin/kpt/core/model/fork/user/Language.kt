/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.fork.user

import kotlinx.serialization.Serializable

/**
 * A language the user can choose in the app.
 *
 * @property localeName Locale code, or null for the system default.
 * @property text Name of the language shown in the UI.
 */
@Serializable
data class Language(
    val localeName: String?,
    val text: String,
) {
    /** Holds the default language. */
    companion object {
        /** Language entry that follows the system default. */
        val DEFAULT = LanguageConfig.DEFAULT.toLanguage()
    }
}

/** Converts this [LanguageConfig] into a [Language]. */
fun LanguageConfig.toLanguage(): Language {
    return Language(
        localeName = this.localeName,
        text = this.text,
    )
}
