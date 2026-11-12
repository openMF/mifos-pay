/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pins [ensureMetadataParent]: a fresh clone has the platform `metadata/` root but not the
 * gitignored `review_information/` / `changelogs/` sub-dirs, and those files must still be written.
 */
class MetadataParentTest {

    private fun tmp(): File = Files.createTempDirectory("meta").toFile()

    @Test
    fun missingSubDirUnderExistingMetadataRootIsCreated() {
        val root = tmp()
        File(root, "deployment/ios/appstore/metadata").mkdirs()
        val target = File(root, "deployment/ios/appstore/metadata/review_information/notes.txt")
        assertTrue(ensureMetadataParent(target))
        assertTrue(target.parentFile.isDirectory)
    }

    @Test
    fun nestedLocaleSubDirIsCreated() {
        val root = tmp()
        File(root, "deployment/android/metadata").mkdirs()
        val target = File(root, "deployment/android/metadata/en-US/changelogs/default.txt")
        assertTrue(ensureMetadataParent(target))
        assertTrue(target.parentFile.isDirectory)
    }

    @Test
    fun platformWithoutMetadataRootIsSkipped() {
        val root = tmp()
        val target = File(root, "deployment/tvos/metadata/review_information/notes.txt")
        assertFalse(ensureMetadataParent(target))
        assertFalse(File(root, "deployment/tvos").exists())
    }

    @Test
    fun existingParentIsWritable() {
        val root = tmp()
        val dir = File(root, "cmp-ios/Configuration").apply { mkdirs() }
        assertTrue(ensureMetadataParent(File(dir, "Config.xcconfig")))
    }
}
