/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File

/**
 * Derives the two feature aggregates the app shell used to keep by hand.
 *
 * `ForkFeatureRegistry` carried a `listOf(...)` of every feature's Koin module and a lambda calling
 * every feature's nav graph. Adding a feature meant an import AND an entry in each — in a file the
 * feature does not own. Forgetting the Koin entry compiled cleanly and left that feature's
 * ViewModels unresolvable at runtime; forgetting the nav entry left a route that silently did not
 * exist. Both are derived here instead:
 *
 *  - [GENERATED_KOIN]  — every Kotlin file in a feature's `di` package declaring `val <X>Module = module`
 *  - [GENERATED_NAV]   — every `NavGraphBuilder` extension annotated `@FeatureDestination`
 *
 * ── WHY GRADLE AND NOT KSP ──────────────────────────────────────────────────────────────────────
 * The four shipped processors (`store-ksp`, `database-ksp`, `network-ksp`, `data-ksp`) each run
 * INSIDE the module that declares their annotations, so `getSymbolsWithAnnotation` — which is
 * source-only, scoped to the current compilation unit — is enough. These aggregates are different:
 * the inputs live in ~15 sibling modules and the output must land in one.
 *
 * KSP's classpath-reading API (`getDeclarationsFromPackage`) does cross module boundaries, but a
 * probe on 2026-09-13 measured it resolving 0 declarations during the commonMain METADATA pass and
 * 1 during `kspKotlinDesktop` — i.e. cross-module aggregation works ONLY per-target. The consumers
 * here (`ForkFeatureRegistry`, `KoinModules`, `AuthenticatedNavigation`) are all commonMain, so a KSP
 * aggregate would have forced an `expect`/`actual` across six targets to reach them. Gradle already
 * knows the feature list at configure time and writes straight into commonMain.
 *
 * ── WHY THE FQN IS READ FROM SOURCE ─────────────────────────────────────────────────────────────
 * Never inferred from the Gradle path. `:feature:crypto` declares `CryptoFeatureModule`, not
 * `CryptoModule`; a path convention would have dropped it from DI with a clean build.
 */
class FeatureAggregateConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val koinDir = layout.buildDirectory.dir("generated/featureKoin/commonMain/kotlin")
        val navDir = layout.buildDirectory.dir("generated/featureNav/commonMain/kotlin")
        val tabDir = layout.buildDirectory.dir("generated/featureTab/commonMain/kotlin")

        // Resolved at configuration time; the task bodies must not touch `project`.
        val featureRoots = rootProject.file("feature")
            .listFiles()
            ?.sortedBy { it.name }
            ?.map { File(it, SRC) }
            ?.filter { it.isDirectory }
            .orEmpty()

        val koinTask = tasks.register(TASK_KOIN) {
            group = GROUP
            description = "Derives $GENERATED_KOIN from every feature's di/ Koin module."
            inputs.files(featureRoots)
            outputs.dir(koinDir)
            doLast { writeKoinAggregate(featureRoots, koinDir, logger::lifecycle) }
        }

        val navTask = tasks.register(TASK_NAV) {
            group = GROUP
            description = "Derives $GENERATED_NAV from every @FeatureDestination entry."
            inputs.files(featureRoots)
            outputs.dir(navDir)
            doLast { writeNavAggregate(featureRoots, navDir, logger::lifecycle) }
        }

        val tabTask = tasks.register(TASK_TAB) {
            group = GROUP
            description = "Derives $GENERATED_TAB from every @FeatureTab NavigationItem."
            inputs.files(featureRoots)
            outputs.dir(tabDir)
            doLast { writeTabAggregate(featureRoots, tabDir, logger::lifecycle) }
        }

        extensions.configure(KotlinMultiplatformExtension::class.java) {
            sourceSets.named("commonMain") {
                kotlin.srcDir(koinDir)
                kotlin.srcDir(navDir)
                kotlin.srcDir(tabDir)
            }
        }

        // Everything that READS commonMain — including the per-target KSP tasks, which take the
        // generated dir as an input — must wait for both.
        tasks.matching { it.name.startsWith("compileKotlin") || it.name.startsWith("ksp") }
            .configureEach { dependsOn(koinTask, navTask, tabTask) }
    }

    private companion object {
        /** Gradle task group the three generators appear under. */
        const val GROUP = "feature aggregation"

        /**
         * Source set the generators scan. commonMain only — a feature's registrations are declared once, not per
         * platform.
         */
        const val SRC = "src/commonMain/kotlin"

        /** Package the aggregates are generated into. */
        const val PKG = "cmp.navigation.registry"

        /** [PKG] as a directory path, for writing the files. */
        const val PKG_PATH = "cmp/navigation/registry"

        /** Task name for the Koin-module aggregate. */
        const val TASK_KOIN = "generateFeatureKoinBindings"

        /** Task name for the nav-destination aggregate. */
        const val TASK_NAV = "generateFeatureDestinations"

        /** Task name for the bottom-tab aggregate. */
        const val TASK_TAB = "generateFeatureTabs"

        /** Generated object holding every feature's Koin module. */
        const val GENERATED_KOIN = "GeneratedFeatureKoinBindings"

        /** Generated object holding every `@FeatureDestination`. */
        const val GENERATED_NAV = "GeneratedFeatureDestinations"

        /** Generated object holding every `@FeatureTab`. */
        const val GENERATED_TAB = "GeneratedFeatureTabs"

        /**
         * Extracts a file's `package` line, so a generated reference can be fully qualified without resolving imports.
         */
        val PACKAGE_RX = Regex("""^package\s+([A-Za-z0-9_.]+)""", RegexOption.MULTILINE)

        /** `val FooModule = module {` — the optional `: Module` is declared on some, omitted on others. */
        val KOIN_MODULE_RX =
            Regex("""^\s*val\s+([A-Za-z0-9_]+Module)\s*(?::\s*Module\s*)?=\s*module\s*\{""", RegexOption.MULTILINE)

        /** `@FeatureDestination` immediately above a `NavGraphBuilder` extension. */
        val NAV_DEST_RX = Regex(
            """@FeatureDestination\s*\n\s*(?:public\s+)?fun\s+NavGraphBuilder\.([A-Za-z0-9_]+)\s*\(([^)]*)\)""",
            RegexOption.MULTILINE,
        )

        /** `@FeatureTab` on an `object` — a class would have nothing to put in the list. */
        val TAB_RX = Regex(
            """@FeatureTab\s*\n\s*(?:public\s+)?(object|class)\s+([A-Za-z0-9_]+)""",
            RegexOption.MULTILINE,
        )

        /** The ONE shape the generated aggregate can invoke. */
        val NAV_PARAM_RX = Regex("""^[A-Za-z0-9_]+\s*:\s*NavController$""")

        /** Every `.kt` under [roots] whose PARENT DIRECTORY is [dirName], with its package. */
        fun scan(roots: List<File>, dirName: String): List<Pair<String, String>> =
            roots.flatMap { root ->
                root.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" && it.parentFile.name == dirName }
                    .sortedBy { it.path }
                    .mapNotNull { f ->
                        val text = f.readText()
                        val pkg = PACKAGE_RX.find(text)?.groupValues?.get(1) ?: return@mapNotNull null
                        pkg to text
                    }
                    .toList()
            }

        /** Writes [content] to [fileName] under the generated package, creating the directory tree. */
        fun write(outDir: Provider<Directory>, fileName: String, content: String) {
            val dir = outDir.get().asFile.resolve(PKG_PATH)
            dir.mkdirs()
            dir.resolve(fileName).writeText(content)
        }

        /**
         * Scans every feature's `di` package and emits [GENERATED_KOIN].
         *
         * A feature with no `di/` contributes nothing — the directory filter covers it, so a nav-only feature needs no
         * special case.
         */
        fun writeKoinAggregate(roots: List<File>, outDir: Provider<Directory>, log: (String) -> Unit) {
            // A feature with no `di/` package (e.g. `:feature:showcase`, nav-only) simply contributes
            // nothing — the directory filter handles it, no special case required.
            val fqns = scan(roots, "di").mapNotNull { (pkg, text) ->
                KOIN_MODULE_RX.find(text)?.groupValues?.get(1)?.let { "$pkg.$it" }
            }.sorted()

            write(
                outDir,
                "$GENERATED_KOIN.kt",
                buildString {
                    appendLine("// GENERATED by :cmp-navigation:$TASK_KOIN — do not edit.")
                    appendLine("package $PKG")
                    appendLine()
                    appendLine("import org.koin.core.module.Module")
                    appendLine("import org.koin.dsl.module")
                    fqns.forEach { appendLine("import $it") }
                    appendLine()
                    appendLine("/** Every feature module's Koin module, derived from source. */")
                    appendLine("public val $GENERATED_KOIN: Module = module {")
                    if (fqns.isEmpty()) {
                        appendLine("    // No feature module declares a Koin module yet.")
                    } else {
                        fqns.forEach { appendLine("    includes(${it.substringAfterLast('.')})") }
                    }
                    appendLine("}")
                },
            )
            log("$TASK_KOIN: ${fqns.size} feature module(s)")
        }

        /** Scans for `@FeatureTab` declarations and emits [GENERATED_TAB]. */
        fun writeTabAggregate(roots: List<File>, outDir: Provider<Directory>, log: (String) -> Unit) {
            // Unlike Koin modules (`di`) and destinations (`navigation`), a tab object has no
            // conventional directory — scan every file and let the annotation be the filter.
            val fqns = mutableListOf<String>()
            val nonObject = mutableListOf<String>()
            roots.forEach { root ->
                root.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .sortedBy { it.path }
                    .forEach { f ->
                        val text = f.readText()
                        if (!text.contains("@FeatureTab")) return@forEach
                        val pkg = PACKAGE_RX.find(text)?.groupValues?.get(1) ?: return@forEach
                        TAB_RX.findAll(text).forEach { m ->
                            val kind = m.groupValues[1]
                            val name = m.groupValues[2]
                            if (kind == "object") fqns += "$pkg.$name" else nonObject += "$pkg.$name"
                        }
                    }
            }
            if (nonObject.isNotEmpty()) {
                throw GradleException(
                    "$TASK_TAB: @FeatureTab must annotate an `object` implementing NavigationItem — " +
                        "the aggregate holds instances, and a class gives it nothing to hold. " +
                        "Non-conforming: ${nonObject.joinToString("; ")}",
                )
            }
            fqns.sort()
            write(
                outDir,
                "$GENERATED_TAB.kt",
                buildString {
                    appendLine("// GENERATED by :cmp-navigation:$TASK_TAB — do not edit.")
                    appendLine("package $PKG")
                    appendLine()
                    appendLine("import kpt.core.ui.navigation.NavigationItem")
                    fqns.forEach { appendLine("import $it") }
                    appendLine()
                    appendLine("/** Every `@FeatureTab` contributed by a feature module. */")
                    appendLine("public val $GENERATED_TAB: List<NavigationItem> = listOf(")
                    fqns.forEach { appendLine("    ${it.substringAfterLast('.')},") }
                    appendLine(")")
                },
            )
            log("$TASK_TAB: ${fqns.size} tab(s)")
        }

        /**
         * Scans for `@FeatureDestination` declarations and emits [GENERATED_NAV], so adding a screen to the app graph
         * means annotating it rather than editing a registry.
         */
        fun writeNavAggregate(roots: List<File>, outDir: Provider<Directory>, log: (String) -> Unit) {
            val fqns = mutableListOf<String>()
            val nonConforming = mutableListOf<String>()

            scan(roots, "navigation").forEach { (pkg, text) ->
                NAV_DEST_RX.findAll(text).forEach { m ->
                    val name = m.groupValues[1]
                    val params = m.groupValues[2].trim()
                    if (NAV_PARAM_RX.matches(params)) fqns += "$pkg.$name" else nonConforming += "$pkg.$name($params)"
                }
            }

            // A destination with a bespoke signature cannot be invoked generically. Skipping it would
            // remove a SCREEN from the app with a clean build, so this fails instead.
            if (nonConforming.isNotEmpty()) {
                throw GradleException(
                    "$TASK_NAV: @FeatureDestination requires the signature " +
                        "(navController: NavController) so the aggregate can invoke it uniformly. " +
                        "Non-conforming: ${nonConforming.joinToString("; ")}",
                )
            }
            fqns.sort()

            write(
                outDir,
                "$GENERATED_NAV.kt",
                buildString {
                    appendLine("// GENERATED by :cmp-navigation:$TASK_NAV — do not edit.")
                    appendLine("package $PKG")
                    appendLine()
                    appendLine("import androidx.navigation.NavController")
                    appendLine("import androidx.navigation.NavGraphBuilder")
                    fqns.forEach { appendLine("import $it") }
                    appendLine()
                    appendLine("/** Every `@FeatureDestination` top-level entry, derived from source. */")
                    appendLine("public val $GENERATED_NAV: NavGraphBuilder.(NavController) -> Unit = { navController ->")
                    if (fqns.isEmpty()) {
                        appendLine("    // No feature declares a @FeatureDestination yet.")
                    } else {
                        fqns.forEach { appendLine("    ${it.substringAfterLast('.')}(navController)") }
                    }
                    appendLine("}")
                },
            )
            log("$TASK_NAV: ${fqns.size} destination(s)")
        }
    }
}
