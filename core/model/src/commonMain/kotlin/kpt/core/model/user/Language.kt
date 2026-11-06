/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.user

import kotlinx.serialization.Serializable
import kpt.core.model.user.fork.LanguageConfig

@Serializable
data class Language(
    val localeName: String?,
    val text: String,
) {
    companion object {
        val DEFAULT = LanguageConfig.DEFAULT.toLanguage()
    }
}

fun LanguageConfig.toLanguage(): Language {
    return Language(
        localeName = this.localeName,
        text = this.text,
    )
}
