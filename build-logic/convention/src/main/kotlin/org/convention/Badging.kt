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

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.google.common.truth.Truth.assertWithMessage
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.process.ExecOperations
import java.util.Locale
import javax.inject.Inject

/**
 * Generates the badging information of the APK.
 * This task is cacheable, meaning that if the inputs and outputs have not changed,
 * the task will be considered up-to-date and will not run.
 * This task is also incremental, meaning that if the inputs have not changed,
 *
 */
@CacheableTask
abstract class GenerateBadgingTask : DefaultTask() {

    /** Where the dumped badging is written — the task's only output, which is what makes it cacheable. */
    @get:OutputFile
    abstract val badging: RegularFileProperty

    /** The APK to inspect. `PathSensitivity.NONE` because only the bytes matter, not where the file sits. */
    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val apk: RegularFileProperty

    /**
     * The `aapt2` binary from the build tools. Declared as an input so an SDK upgrade that changes aapt2's output
     * invalidates the cached result.
     */
    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val aapt2Executable: RegularFileProperty

    /**
     * Gradle's process launcher, injected rather than calling `Runtime.exec` so the task stays configuration-cache
     * compatible.
     */
    @get:Inject
    abstract val execOperations: ExecOperations

    /** Runs `aapt2 dump badging` and writes stdout to [badging]. */
    @TaskAction
    fun taskAction() {
        execOperations.exec {
            commandLine(
                aapt2Executable.get().asFile.absolutePath,
                "dump",
                "badging",
                apk.get().asFile.absolutePath,
            )
            standardOutput = badging.asFile.get().outputStream()
        }
    }
}

/**
 * Fails the build when an APK's manifest surface drifts from the committed golden file.
 *
 * The guard against a dependency silently adding a permission or an exported component: the diff shows up here rather
 * than in a store review.
 */
@CacheableTask
abstract class CheckBadgingTask : DefaultTask() {

    // In order for the task to be up-to-date when the inputs have not changed,
    // the task must declare an output, even if it's not used. Tasks with no
    // output are always run regardless of whether the inputs changed
    /**
     * An unused output directory.
     *
     * Gradle treats a task with no declared output as never up-to-date, so a pure verification task must declare one
     * to stay incremental. Nothing is written here.
     */
    @get:OutputDirectory
    abstract val output: DirectoryProperty

    /** The committed reference badging — the manifest surface a reviewer has approved. */
    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val goldenBadging: RegularFileProperty

    /** The badging produced from the APK under test. */
    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val generatedBadging: RegularFileProperty

    /**
     * Name of the task that refreshes the golden file, quoted verbatim in the failure message so the fix is copy-
     * pasteable.
     */
    @get:Input
    abstract val updateBadgingTaskName: Property<String>

    override fun getGroup(): String = LifecycleBasePlugin.VERIFICATION_GROUP

    /** Runs `aapt2 dump badging` and writes stdout to [badging]. */
    @TaskAction
    fun taskAction() {
        assertWithMessage(
            "Generated badging is different from golden badging! " +
                "If this change is intended, run ./gradlew ${updateBadgingTaskName.get()}",
        )
            .that(generatedBadging.get().asFile.readText())
            .isEqualTo(goldenBadging.get().asFile.readText())
    }
}

/**
 * Wires a generate + check task pair for one Android variant.
 *
 * @param baseExtension the Android extension, for the aapt2 location.
 * @param componentsExtension the variant API, to attach one task pair per variant.
 */
fun Project.configureBadgingTasks(
    componentsExtension: ApplicationAndroidComponentsExtension,
) {
    // Registers a callback to be called, when a new variant is configured
    componentsExtension.onVariants { variant ->
        // Registers a new task to verify the app bundle.
        val capitalizedVariantName = variant.name.let {
            if (it.isEmpty()) {
                it
            } else {
                it[0].titlecase(
                    Locale.getDefault(),
                ) + it.substring(1)
            }
        }
        val generateBadgingTaskName = "generate${capitalizedVariantName}Badging"
        val generateBadging =
            tasks.register<GenerateBadgingTask>(generateBadgingTaskName) {
                apk.set(
                    variant.artifacts.get(SingleArtifact.APK_FROM_BUNDLE),
                )
                aapt2Executable.set(
                    componentsExtension.sdkComponents.aapt2.flatMap { it.executable },
                )

                badging.set(
                    project.layout.buildDirectory.file(
                        "outputs/apk_from_bundle/${variant.name}/${variant.name}-badging.txt",
                    ),
                )
            }

        val updateBadgingTaskName = "update${capitalizedVariantName}Badging"
        tasks.register<Copy>(updateBadgingTaskName) {
            from(generateBadging.get().badging)
            into(project.layout.projectDirectory)
        }

        val checkBadgingTaskName = "check${capitalizedVariantName}Badging"
        tasks.register<CheckBadgingTask>(checkBadgingTaskName) {
            goldenBadging.set(
                project.layout.projectDirectory.file("${variant.name}-badging.txt"),
            )
            generatedBadging.set(
                generateBadging.get().badging,
            )
            this.updateBadgingTaskName.set(updateBadgingTaskName)

            output.set(
                project.layout.buildDirectory.dir("intermediates/$checkBadgingTaskName"),
            )
        }
    }
}
