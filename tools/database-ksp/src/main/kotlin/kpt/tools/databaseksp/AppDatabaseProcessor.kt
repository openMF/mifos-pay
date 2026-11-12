/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.tools.databaseksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSFile

/**
 * Derives the whole Room `@Database` class from `@DbEntity` / `@DbDao` / `@DbConverters`.
 *
 * ## Why the WHOLE file, not a bindings object
 * Every other generated surface in this repo is a separate object the hand-written code composes
 * (`GeneratedStoreBindings`, `GeneratedApiBindings`). Room cannot work that way: `entities = [...]`,
 * `@ColumnTypeConverters(...)` and the `abstract val` accessors must be compile-time literals INSIDE
 * the `@Database` class, and a Kotlin annotation argument cannot be read from a `val`. So the class
 * is emitted whole, and `AppDatabase.kt` stops being a source file at all.
 *
 * ## Why Room still sees it
 * The output lands in `build/generated/ksp/metadata/commonMain/kotlin`, which `core/database`
 * puts on commonMain's srcDir. It is therefore an ORDINARY source file for every per-target
 * compilation, and Room's own per-target processors read it exactly as they read the hand-written
 * one — no processor is asked to consume another's output mid-round.
 *
 * ## What is NOT derived from annotations
 * Framework infra tables live in `core-base/database`, a different module whose sources this
 * compilation cannot scan, and migrations/version are schema HISTORY that no class can carry.
 * Both arrive as KSP args from Gradle, which reads their existing owners:
 * `core-base/database/module-schema.yaml` (framework, template-owned) and
 * `app-profile/migration-ledger.yaml` (this fork's applied sequence). Keeping those two SoTs intact
 * is deliberate — a version number invented by scanning source is a stranded installed device.
 */
class AppDatabaseProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {
    private var emitted = false

    /** Header from the build's one source (`kpt.licenseHeader`, set by the Koin convention); none if unset. */
    private val licenseHeader: String =
        options["kpt.licenseHeader"]?.replace("\\n", "\n")?.takeIf(String::isNotBlank)?.let { "$it\n" }.orEmpty()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (emitted) return emptyList()
        emitted = true

        val entities = resolver.annotated(ANN_ENTITY).map { it.fqn() }.sorted()
        val converters = resolver.annotated(ANN_CONVERTERS).map { it.fqn() }.sorted()
        val daos = resolver.annotated(ANN_DAO)
            .map { decl ->
                val declared = decl.annotations
                    .firstOrNull { it.shortName.asString() == "DbDao" }
                    ?.arguments
                    ?.firstOrNull { it.name?.asString() == "accessor" }
                    ?.value as? String
                val accessor = declared?.takeIf { it.isNotBlank() }
                    ?: decl.simpleName.asString().replaceFirstChar { it.lowercaseChar() }
                accessor to decl.fqn()
            }
            .sortedBy { it.first }

        // Duplicate accessors would produce two `abstract val` of the same name — a compile error in
        // GENERATED code, where the reader has no line to look at. Name the annotated classes instead.
        daos.groupBy { it.first }.filterValues { it.size > 1 }.forEach { (name, rows) ->
            logger.error("database-ksp: duplicate @DbDao accessor '$name' on ${rows.joinToString { it.second }}")
        }

        val files = (resolver.annotated(ANN_ENTITY) + resolver.annotated(ANN_DAO) + resolver.annotated(ANN_CONVERTERS))
            .mapNotNull { it.containingFile }
            .distinct()

        val installs = resolver.annotated(ANN_CONVERTERS).mapNotNull { it.installSpec() }.sortedBy { it.first }

        write(render(entities, daos, converters), files, "AppDatabase")
        write(renderDaoBindings(daos), files, "GeneratedDaoBindings", BINDINGS_PKG)
        write(renderConverterBindings(installs), files, "GeneratedConverterBindings", BINDINGS_PKG)
        logger.info(
            "database-ksp: AppDatabase with ${entities.size} entity, ${daos.size} dao, " +
                "${converters.size} converter declaration(s)",
        )
        return emptyList()
    }

    /**
     * The framework's own DAOs — `BookkeeperDao`, `ConflictDao`, `DraftDao` — declared in
     * `core-base/database` and supplied by Gradle rather than by `@DbDao`, because a template-owned
     * module cannot carry a per-fork annotation.
     *
     * Shared by BOTH renderers on purpose. It used to be parsed inside `render` alone, so the
     * accessors reached `AppDatabase` while their Koin bindings did not — and the gap was filled by
     * hand in two different modules, which then drifted: `bookkeeperDao` ended up bound in
     * `DatabaseModule` AND `RepositoryModule`, both of which reach the same container. One
     * annotation-shaped fact, two hand-written copies — the defect `@DbDao` exists to remove.
     */
    private fun infraDaos(): List<Pair<String, String>> =
        opt(OPT_INFRA_DAOS).mapNotNull {
            val (n, t) = it.split(':', limit = 2).let { p -> p.getOrNull(0) to p.getOrNull(1) }
            if (n.isNullOrBlank() || t.isNullOrBlank()) null else n to t
        }

    // ── inputs Gradle supplies (see the class KDoc for why these are not annotations) ───────────
    private fun opt(key: String): List<String> =
        options[key]?.split('|')?.map(String::trim)?.filter(String::isNotEmpty).orEmpty()

    private fun render(
        entities: List<String>,
        daos: List<Pair<String, String>>,
        converters: List<String>,
    ): String {
        val infraEntities = opt(OPT_INFRA_ENTITIES)
        val migrations = opt(OPT_MIGRATIONS)

        return buildString {
            append(licenseHeader)
            append("package $PKG\n\n")
            append("import androidx.room3.AutoMigration\n")
            if (converters.isNotEmpty()) append("import androidx.room3.ColumnTypeConverters\n")
            append("import androidx.room3.ConstructedBy\n")
            append("import androidx.room3.Database\n")
            append("import androidx.room3.RoomDatabase\n")
            append("import androidx.room3.RoomDatabaseConstructor\n")
            append("import kpt.core.database.config.DatabaseConfig\n")
            append("import kpt.core.database.config.ForkDatabaseConfig\n\n")
            append(HEADER_KDOC)
            append("expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {\n")
            append("    override fun initialize(): AppDatabase\n")
            append("}\n\n")
            append(CLASS_KDOC)
            append("@Database(\n    entities = [\n")
            infraEntities.forEach { append("        $it::class,\n") }
            entities.forEach { append("        $it::class,\n") }
            append("    ],\n")
            append("    version = AppDatabase.VERSION,\n")
            append("    exportSchema = true,\n")
            append("    autoMigrations = [\n")
            migrations.forEach { append("        $it,\n") }
            append("    ],\n)\n")
            if (converters.isNotEmpty()) {
                append("@ColumnTypeConverters(\n")
                converters.forEach { append("    $it::class,\n") }
                append(")\n")
            }
            append("@ConstructedBy(AppDatabaseConstructor::class)\n")
            append("abstract class AppDatabase : RoomDatabase() {\n\n")
            val infra = infraDaos()
            infra.forEach { (n, t) -> append("    abstract val $n: $t\n") }
            if (infra.isNotEmpty() && daos.isNotEmpty()) append("\n")
            daos.forEach { (n, t) -> append("    abstract val $n: $t\n") }
            append(COMPANION)
            append("}\n")
        }
    }

    /**
     * The Koin binding for every `@DbDao`, from the SAME annotation that puts the accessor on
     * [AppDatabase]. One declaration produces both because they cannot be allowed to disagree: an
     * accessor with no binding is invisible to injection, and a binding for a missing accessor does
     * not compile.
     */
    private fun renderDaoBindings(daos: List<Pair<String, String>>): String = buildString {
        append(licenseHeader)
        append("package $BINDINGS_PKG\n\n")
        append("import $PKG.AppDatabase\n")
        append("import org.koin.core.module.Module\n")
        append("import org.koin.dsl.module\n\n")
        append("/**\n")
        append(" * GENERATED — one Koin binding per DAO, framework and fork alike. DO NOT HAND-EDIT.\n")
        append(" *\n")
        append(" * To add a DAO: annotate it `@DbDao`. That is the whole wiring step — the accessor on\n")
        append(" * `AppDatabase` and this binding both come from that one annotation.\n")
        append(" *\n")
        append(" * Pulled in by `DatabaseModule` via `includes(GeneratedDaoBindings)`.\n")
        append(" */\n")
        append("val GeneratedDaoBindings: Module = module {\n")
        // Framework DAOs FIRST, then the fork's. Both now come from the same source as their
        // AppDatabase accessor, so a binding can no longer be missing for a DAO the database
        // exposes — which is what forced the hand-written copies this replaces.
        infraDaos().forEach { (accessor, _) -> append("    single { get<AppDatabase>().$accessor }\n") }
        daos.forEach { (accessor, _) -> append("    single { get<AppDatabase>().$accessor }\n") }
        append("}\n")
    }

    /**
     * A `@DbConverters` class whose companion declares `install(<one dep>)`, as
     * `simpleName to (converterFqn to paramTypeFqn)` — flattened to a triple-ish pair for sorting.
     *
     * Room instantiates converters through a NO-ARG constructor, so a converter that needs a
     * collaborator (an encryptor, a clock) cannot receive it by construction; it takes it through a
     * static `install` that must run before the first database access. That is a real dependency,
     * and leaving it to be hand-written meant a converter could be registered on the database while
     * its encryptor was never installed — the column then silently round-trips as plaintext.
     */
    private fun KSClassDeclaration.installSpec(): Pair<String, Pair<String, String>>? {
        val companion = declarations
            .filterIsInstance<KSClassDeclaration>()
            .firstOrNull { it.isCompanionObject }
        val fn = companion?.declarations
            ?.filterIsInstance<KSFunctionDeclaration>()
            ?.firstOrNull { it.simpleName.asString() == "install" && it.parameters.size == 1 }
        val param = fn?.parameters?.single()?.type?.resolve()?.declaration?.qualifiedName?.asString()
        return param?.let { simpleName.asString() to (fqn() to it) }
    }

    /**
     * One-shot converter installs, from the SAME `@DbConverters` annotation that registers the
     * converter on the database. `createdAtStart = true` runs it at graph construction, before any
     * DAO can be resolved; the marker object exists only to give each `single` a distinct type.
     */
    private fun renderConverterBindings(
        installs: List<Pair<String, Pair<String, String>>>,
    ): String = buildString {
        append(licenseHeader)
        append("package $BINDINGS_PKG\n\n")
        append("import org.koin.core.module.Module\n")
        append("import org.koin.dsl.module\n\n")
        append("/**\n")
        append(" * GENERATED from `@DbConverters` — the one-shot install for every converter whose\n")
        append(" * companion declares `install(<dep>)`. DO NOT HAND-EDIT.\n")
        append(" *\n")
        append(" * Room builds converters with a no-arg constructor, so a converter needing a collaborator\n")
        append(" * takes it post-construction and MUST get it before the first database access. Deriving\n")
        append(" * this from the annotation is what stops a converter being registered on the database\n")
        append(" * while its dependency was never installed — an encrypted column would then round-trip\n")
        append(" * as plaintext, silently and only in production.\n")
        append(" *\n")
        append(" * Pulled in by `DatabaseModule` via `includes(GeneratedConverterBindings)`.\n")
        append(" */\n")
        installs.forEach { (name, _) -> append("private object ${name}Installed\n") }
        if (installs.isNotEmpty()) append("\n")
        append("val GeneratedConverterBindings: Module = module {\n")
        installs.forEach { (name, spec) ->
            val (converter, param) = spec
            append("    single(createdAtStart = true) {\n")
            append("        $converter.install(get<$param>())\n")
            append("        ${name}Installed\n")
            append("    }\n")
        }
        append("}\n")
    }

    private fun write(text: String, files: List<KSFile>, name: String, pkg: String = PKG) {
        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true, *files.toTypedArray()),
            packageName = pkg,
            fileName = name,
        ).bufferedWriter().use { it.write(text) }
    }

    private fun Resolver.annotated(fqn: String): List<KSClassDeclaration> =
        getSymbolsWithAnnotation(fqn).filterIsInstance<KSClassDeclaration>().toList()

    private fun KSClassDeclaration.fqn(): String = qualifiedName?.asString() ?: simpleName.asString()

    private companion object {
        const val PKG = "kpt.core.database"
        const val BINDINGS_PKG = "kpt.core.database.di"
        const val ANN = "kpt.core.base.database.annotation"
        const val ANN_ENTITY = "$ANN.DbEntity"
        const val ANN_DAO = "$ANN.DbDao"
        const val ANN_CONVERTERS = "$ANN.DbConverters"
        const val OPT_INFRA_ENTITIES = "kpt.database.infraEntities"
        const val OPT_INFRA_DAOS = "kpt.database.infraDaos"
        const val OPT_MIGRATIONS = "kpt.database.migrations"

        val HEADER_KDOC = """
            |/**
            | * KSP-generated constructor bridge for [AppDatabase].
            | *
            | * Room 3 requires an `expect object` annotated with `@ConstructedBy` so its compiler plugin can
            | * generate the platform-specific `actual object` holding the `AppDatabase_Impl` instantiation.
            | */
            |
        """.trimMargin()

        val CLASS_KDOC = """
            |/**
            | * GENERATED — do not hand-edit, and do not look for this file in `src/`.
            | *
            | * Emitted by `tools/database-ksp` from the `@DbEntity` / `@DbDao` / `@DbConverters`
            | * annotations on the tables themselves, plus the framework infra tables
            | * (`core-base/database/module-schema.yaml`) and this fork's applied migration sequence
            | * (`app-profile/migration-ledger.yaml`), both passed in as KSP args.
            | *
            | * ## Why this file is generated whole
            | * It was the LAST hand-merged file under `core/`: Room needs ONE compile-time
            | * `entities = [...]` literal, so a fork's tables could not live in a separate file the way
            | * every other seam works, and a template sync had to 3-way merge it. Deriving the literal
            | * from the annotations removes the merge rather than arbitrating it — there is no longer a
            | * committed file for two sides to disagree about.
            | *
            | * ## Adding a table
            | * Write the `@Entity` and annotate it `@DbEntity`; write the `@Dao` and annotate it `@DbDao`.
            | * That is the whole declaration — no YAML row, no accessor to add here, no Koin binding to
            | * write (`GeneratedDaoBindings` comes from the same `@DbDao`). Schema HISTORY is the one
            | * thing you still record by hand, in the migration ledger, because no class can carry it and
            | * a version invented by scanning source strands every installed device.
            | */
            |
        """.trimMargin()

        val COMPANION = """
            |
            |    companion object {
            |        /**
            |         * Effective Room schema version — owned OUTRIGHT by the fork ([ForkDatabaseConfig],
            |         * generated from `app-profile/migration-ledger.yaml#version`). The template does not
            |         * contribute to it; it ships `migration_units` the fork's ledger places at its own
            |         * next free version.
            |         */
            |        const val VERSION = ForkDatabaseConfig.VERSION
            |
            |        /**
            |         * Fork-unique on-disk DB filename — single source of truth is [DatabaseConfig.NAME]
            |         * (appId-derived, regenerated by `syncForkConfig`). All platform builders read this.
            |         */
            |        const val DATABASE_NAME = DatabaseConfig.NAME
            |    }
            |
        """.trimMargin()
    }
}

/**
 * KSP entry point — what the `META-INF/services` registration names, so Gradle can instantiate [AppDatabaseProcessor].
 */
class AppDatabaseProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        AppDatabaseProcessor(environment.codeGenerator, environment.logger, environment.options)
}
