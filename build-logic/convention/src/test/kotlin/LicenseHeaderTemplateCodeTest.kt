/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import org.convention.LicenseHeader
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Spotless finds the pattern in the file content (`Matcher.find`), so `true` means "stamp the header". */
class LicenseHeaderTemplateCodeTest {
    private fun stamps(content: String) = Regex(LicenseHeader.NOT_TEMPLATE_CODE).containsMatchIn(content)

    @Test
    fun templateCodeKeepsItsHeader() {
        assertFalse(stamps("/*\n * Copyright 2025 Mifos Initiative\n */\npackage kpt.core.ui\n"))
    }

    @Test
    fun forkCodeIsStamped() {
        assertTrue(stamps("/*\n * Copyright 2025 MobileByteSensei\n */\npackage com.sensei.social\n"))
    }

    @Test
    fun fileWithoutHeaderIsStamped() {
        assertTrue(stamps("package com.sensei.social\n"))
    }
}
