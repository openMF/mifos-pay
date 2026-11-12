/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
import org.gradle.api.Project

/**
 * Reflective loader for the per-consumer flavor extension at
 * `build-logic/convention/src/main/kotlin/local/LocalFlavors.kt`.
 *
 * Each downstream consumer app (mifos-mobile, mifos-pay, etc.) may create a
 * `local.LocalFlavors` object with a `@JvmStatic fun apply(ext, project)`
 * method to add their own flavors / dimensions / overrides on top of the
 * synced base defined by [KMPFlavorsConventionPlugin].
 *
 * The `local/` directory is excluded from `sync-dirs.sh` — anything inside
 * it survives every sync from the upstream `kmp-project-template`.
 *
 * Example consumer file (`build-logic/convention/src/main/kotlin/local/LocalFlavors.kt`):
 *
 * ```kotlin
 * package local
 *
 * import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
 * import org.gradle.api.Project
 *
 * object LocalFlavors {
 *     @JvmStatic
 *     fun apply(ext: KmpFlavorExtension, project: Project) {
 *         ext.flavorDimensions.register("bank") { priority.set(1) }
 *         ext.flavors.register("bankA") {
 *             dimension.set("bank")
 *             isDefault.set(true)
 *             applicationIdSuffix.set(".banka")
 *             buildConfigField("String", "BANK_URL", "\"https://api.banka.com\"")
 *         }
 *         ext.flavors.register("bankB") {
 *             dimension.set("bank")
 *             applicationIdSuffix.set(".bankb")
 *             buildConfigField("String", "BANK_URL", "\"https://api.bankb.com\"")
 *         }
 *     }
 * }
 * ```
 *
 * See `docs/FLAVORS_EXTENSION.md` for more recipes (overrides, language
 * dimensions, signing configs).
 *
 * DO NOT EDIT — this file is SYNCED. To extend, create `local/LocalFlavors.kt`.
 */
internal object LocalFlavorsLoader {

    private const val LOCAL_FLAVORS_FQN = "local.LocalFlavors"
    private const val LOCAL_FLAVORS_METHOD = "apply"

    fun applyIfPresent(ext: KmpFlavorExtension, project: Project) {
        val outcome = runCatching {
            val cls = Class.forName(LOCAL_FLAVORS_FQN)
            val method = cls.getDeclaredMethod(
                LOCAL_FLAVORS_METHOD,
                KmpFlavorExtension::class.java,
                Project::class.java,
            )
            // Top-level Kotlin `object` exposes a static `INSTANCE` and a
            // companion `@JvmStatic` makes the function directly callable.
            method.invoke(null, ext, project)
            true
        }
        when {
            outcome.isSuccess -> {
                project.logger.lifecycle(
                    "[KMPFlavors] Applied local.LocalFlavors extensions",
                )
            }
            outcome.exceptionOrNull() is ClassNotFoundException -> {
                project.logger.info(
                    "[KMPFlavors] No local.LocalFlavors override — using base demo/prod only.",
                )
            }
            outcome.exceptionOrNull() is NoSuchMethodException -> {
                project.logger.warn(
                    "[KMPFlavors] Found `local.LocalFlavors` but it does not expose " +
                        "`@JvmStatic fun apply(ext: KmpFlavorExtension, project: Project)`. " +
                        "Skipping local extension.",
                )
            }
            else -> {
                project.logger.warn(
                    "[KMPFlavors] Failed to apply local.LocalFlavors: " +
                        "${outcome.exceptionOrNull()?.message}",
                )
            }
        }
    }
}
