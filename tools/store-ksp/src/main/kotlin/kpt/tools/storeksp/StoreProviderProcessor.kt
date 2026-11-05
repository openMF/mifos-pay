/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.tools.storeksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

private const val PROVIDER = "kpt.core.base.store.annotation.StoreProvider"
private const val BINDINGS_PKG = "kpt.core.store.di"
private const val CONFIG_PKG = "kpt.core.store.config"
private val TTL_RE = Regex("^(\\d+)(m|h|d)$")
private val PLACEHOLDER_RE = Regex("\\{([A-Za-z0-9_]+)\\}")

private data class KeySpec(val name: String, val fn: String, val key: String, val params: List<Pair<String, String>>)

private data class StoreSpec(
    val id: String,
    val qualifier: String,
    val ttl: String,
    val logout: Boolean,
    val pkg: String,
    val providerFqn: String,
    val providerName: String,
    val deps: List<String>,
    val keys: List<KeySpec>,
)

/**
 * Derives every Store5 wiring surface from `@StoreProvider`.
 *
 * Emits, per annotated function, a `<Qualifier>Keys` object into the provider's OWN package, and one
 * aggregated `GeneratedStoreBindings` carrying each binding plus the logout purge.
 *
 * Deriving both from a single annotation is the point: the binding and the purge used to be two
 * hand-kept lists that had to agree, and a store bound but never registered survives sign-out and
 * shows the previous user's cached rows to the next person on a shared device. They cannot disagree
 * if neither is written by hand.
 *
 * Validation is a BUILD ERROR, not a later gate: duplicate ids, duplicate key strings, a placeholder
 * with no matching param, a malformed ttl. A key collision matters because two streams sharing a key
 * share a fetched-at stamp — one screen's refresh marks the other fresh and it silently stops
 * refetching.
 */
class StoreProviderProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private var emitted = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Every exit is the same empty list — this processor never defers symbols — so the aborts
        // are a guard around the work, not distinct results. `&&` keeps the original order: a spec
        // count mismatch (error already logged by toSpec) short-circuits before validateGlobally.
        if (!emitted) {
            val fns = resolver.getSymbolsWithAnnotation(PROVIDER)
                .filterIsInstance<KSFunctionDeclaration>()
                .toList()
            val specs = fns.mapNotNull { toSpec(it) }
            if (fns.isNotEmpty() && specs.size == fns.size && validateGlobally(specs)) {
                // Sorted so the generated files are byte-stable across builds: KSP hands symbols
                // back in an order that depends on how the compiler walked the sources, which
                // would otherwise reshuffle the output on unrelated edits.
                val ordered = specs.sortedBy { it.qualifier }
                emitRegistry(ordered, fns)
                emitCacheKeys(ordered, fns)
                emitStoreIds(ordered, fns)
                emitBindings(ordered, fns)
                emitted = true
            }
        }
        return emptyList()
    }

    private fun ann(fn: KSFunctionDeclaration, short: String): List<KSAnnotation> =
        fn.annotations.filter { it.shortName.asString() == short }.toList()

    private fun KSAnnotation.str(name: String): String =
        arguments.firstOrNull { it.name?.asString() == name }?.value?.toString().orEmpty()

    private fun toSpec(fn: KSFunctionDeclaration): StoreSpec? {
        val a = ann(fn, "StoreProvider").firstOrNull()
        val id = a?.str("id").orEmpty()
        val ttl = a?.str("ttl").orEmpty()
        val idBad = a != null && id.isBlank()
        val ttlBad = a != null && ttl.isNotEmpty() && !TTL_RE.matches(ttl)

        // No `else`: exactly one branch runs, so the FIRST failing check is reported — the same
        // diagnostic the earlier log-then-return chain produced.
        when {
            idBad ->
                logger.error("@StoreProvider requires a non-blank id", fn)
            ttlBad ->
                logger.error("@StoreProvider(ttl = \"$ttl\") must look like 5m / 1h / 7d", fn)
        }
        // Rejected BEFORE any @CacheKey is parsed, exactly as the original early returns did, so a
        // provider with a bad id never also emits key diagnostics.
        if (a == null || idBad || ttlBad) return null

        val cacheKeyAnns = ann(fn, "CacheKey")
        val keys = cacheKeyAnns.mapNotNull { k -> toKeySpec(k, fn) }
        val qualifier = a.str("qualifier").ifEmpty { id.replaceFirstChar { it.uppercaseChar() } }
        val logout = a.arguments.firstOrNull { it.name?.asString() == "logout" }?.value as? Boolean ?: true

        // The payoff: dependencies are READ from the signature, never restated.
        val deps = fn.parameters.mapNotNull { it.name?.asString() }

        // Providers live in `<domain>.impl`; their keys belong beside the domain, not inside impl.
        val pkg = fn.packageName.asString().removeSuffix(".impl")
        // A short `keys` list means toKeySpec already logged the reason; reject without more noise.
        return if (keys.size != cacheKeyAnns.size) {
            null
        } else {
            StoreSpec(
                id = id, qualifier = qualifier, ttl = ttl, logout = logout, pkg = pkg,
                providerFqn = fn.qualifiedName?.asString().orEmpty(),
                providerName = fn.simpleName.asString(), deps = deps, keys = keys,
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun toKeySpec(k: KSAnnotation, fn: KSFunctionDeclaration): KeySpec? {
        val key = k.str("key")
        val name = k.str("name")
        val builder = k.str("fn")

        val keyBad = key.isBlank()
        val slotBad = name.isBlank() == builder.isBlank()

        // No `else`: the FIRST failing check is the one reported, as before.
        when {
            keyBad ->
                logger.error("@CacheKey requires a non-blank key", fn)
            slotBad ->
                logger.error("@CacheKey(key = \"$key\") needs exactly one of name= (constant) or fn= (builder)", fn)
        }
        if (keyBad || slotBad) return null

        val raw = (k.arguments.firstOrNull { it.name?.asString() == "params" }?.value as? List<*>).orEmpty()
        val params = raw.mapNotNull { p ->
            val t = p.toString()
            val i = t.indexOf(':')
            if (i <= 0) { logger.error("@CacheKey params entry '$t' must be \"name:Type\"", fn); null }
            else t.substring(0, i).trim() to t.substring(i + 1).trim()
        }
        val placeholders = PLACEHOLDER_RE.findAll(key).map { it.groupValues[1] }.toSet()
        val declared = params.map { it.first }.toSet()
        // These two loops report EVERY mismatched placeholder/param, so they stay loops rather
        // than folding into the single-branch `when` above.
        (placeholders - declared).forEach {
            logger.error("@CacheKey(key = \"$key\") has placeholder {$it} with no matching param", fn)
        }
        (declared - placeholders).forEach {
            logger.error("@CacheKey(key = \"$key\") declares param '$it' that the key never uses", fn)
        }
        val builderNoParams = builder.isNotBlank() && params.isEmpty()
        if (builderNoParams) {
            logger.error("@CacheKey(fn = \"$builder\") has no params — use name= for a constant", fn)
        }
        // params.size != raw.size means a malformed entry was already reported in the mapNotNull.
        return if (params.size != raw.size || placeholders != declared || builderNoParams) {
            null
        } else {
            KeySpec(name, builder, key, params)
        }
    }

    private fun validateGlobally(specs: List<StoreSpec>): Boolean {
        var ok = true
        specs.groupBy { it.id }.filterValues { it.size > 1 }.forEach { (id, dupes) ->
            logger.error("duplicate @StoreProvider id '$id' on ${dupes.joinToString { it.providerName }}"); ok = false
        }
        specs.groupBy { it.qualifier }.filterValues { it.size > 1 }.forEach { (q, dupes) ->
            logger.error("duplicate store qualifier '$q' on ${dupes.joinToString { it.providerName }}"); ok = false
        }
        specs.flatMap { s -> s.keys.map { it.key to s.providerName } }
            .groupBy({ it.first }, { it.second })
            .filterValues { it.size > 1 }
            .forEach { (key, owners) ->
                logger.error(
                    "duplicate cache key \"$key\" declared by ${owners.joinToString()} — two streams " +
                        "sharing a key share a fetched-at stamp, so one refresh silently marks the other fresh",
                )
                ok = false
            }
        return ok
    }

    /** `"loan:{id}"` -> `"loan:$id"`; braces only where the next char could continue the name. */
    private fun interpolate(key: String, names: List<String>): String {
        var out = key
        names.forEach { n ->
            val i = out.indexOf("{$n}")
            val after = out.getOrNull(i + n.length + 2)
            val braces = after != null && (after.isLetterOrDigit() || after == '_')
            out = out.replace("{$n}", if (braces) "\${$n}" else "$$n")
        }
        return out
    }

    /** `interestRateSeries` -> `INTEREST_RATE_SERIES`, the shape the store factories already read. */
    private fun screamingSnake(id: String): String =
        id.replace(Regex("([a-z0-9])([A-Z])"), "$1_$2").uppercase()

    private fun ttlExpr(ttl: String): String {
        val m = TTL_RE.find(ttl)!!
        return m.groupValues[1] + when (m.groupValues[2]) { "m" -> ".minutes"; "h" -> ".hours"; else -> ".days" }
    }

    private fun header(sb: StringBuilder) {
        sb.append("// GENERATED by store-ksp from @StoreProvider. DO NOT EDIT — this is a build\n")
        sb.append("// artifact, not committed source. Change the annotation on the provider instead.\n\n")
    }

    /**
     * `config/AppStoreRegistry.kt` — every store's Koin qualifier plus the TTL constants.
     *
     * Qualifiers are flat (`AppStoreRegistry.Loans`) because the processor already guarantees they
     * are globally unique. TTLs sit in a nested `Ttl` object keyed by the store id in
     * SCREAMING_SNAKE, which is the shape the store factories already read.
     */
    private fun emitRegistry(specs: List<StoreSpec>, fns: List<KSFunctionDeclaration>) {
        val withTtl = specs.filter { it.ttl.isNotEmpty() }
        val sb = StringBuilder()
        header(sb)
        sb.append("package $CONFIG_PKG\n\n")
        sb.append("import kpt.core.base.store.infra.StoreRegistry\n")
        if (withTtl.any { it.ttl.endsWith("d") }) sb.append("import kotlin.time.Duration.Companion.days\n")
        if (withTtl.any { it.ttl.endsWith("h") }) sb.append("import kotlin.time.Duration.Companion.hours\n")
        if (withTtl.any { it.ttl.endsWith("m") }) sb.append("import kotlin.time.Duration.Companion.minutes\n")
        sb.append("\n/**\n")
        sb.append(" * Every Store5 qualifier the app exposes, and the freshness window of each store that\n")
        sb.append(" * declares one. Derived from `@StoreProvider`.\n")
        sb.append(" */\n")
        sb.append("object AppStoreRegistry : StoreRegistry() {\n")
        specs.forEach { sb.append("    val ").append(it.qualifier).append(" = store(\"").append(it.id).append("\")\n") }
        if (withTtl.isNotEmpty()) {
            sb.append("\n    /** Freshness windows, declared as `@StoreProvider(ttl = …)`. */\n")
            sb.append("    object Ttl {\n")
            withTtl.forEach {
                sb.append("        val ").append(screamingSnake(it.id))
                    .append(" = ").append(ttlExpr(it.ttl)).append("\n")
            }
            sb.append("    }\n")
        }
        sb.append("}\n")
        write(sb.toString(), CONFIG_PKG, "AppStoreRegistry", fns)
    }

    /**
     * `config/AppStoreIds.kt` — the declared `@StoreProvider(id = …)` string as a `const val`.
     *
     * The cache-key side was already generated (`AppCacheKeys.Loans.LIST`), but the STORE ID stayed a
     * raw literal on both sides: `@StoreProvider(id = "loans")` in core/store and `@FromStore("loans")`
     * in core/data. The same string, typed twice, in two modules, with nothing linking them — a typo
     * surfaced only as an unresolved `AppStoreRegistry.Loanz` inside generated code.
     *
     * With this, a repository writes `@FromStore(AppStoreIds.Loans)` and the compiler resolves the
     * constant against the store's own declaration. `const val` is required: annotation arguments must
     * be compile-time constants, and core/data already has `api(projects.core.store)` so the symbol is
     * on its compile classpath.
     *
     * Names match the Koin qualifier (`qualifier` when set, else the id uppercased-first) so
     * `AppStoreIds.Loans` and `AppStoreRegistry.Loans` read as the pair they are.
     */
    private fun emitStoreIds(specs: List<StoreSpec>, fns: List<KSFunctionDeclaration>) {
        val sb = StringBuilder()
        sb.append("// GENERATED by tools/store-ksp — do not edit.\n")
        sb.append("package $CONFIG_PKG\n\n")
        sb.append("/**\n")
        sb.append(" * Every declared store id, as a compile-time constant.\n")
        sb.append(" *\n")
        sb.append(" * Use in `@FromStore(AppStoreIds.Loans)` instead of a raw \"loans\" literal, so the id a\n")
        sb.append(" * repository asks for is the id a store actually declared.\n")
        sb.append(" */\n")
        sb.append("object AppStoreIds {\n")
        if (specs.isEmpty()) {
            sb.append("    // No store declares an id yet.\n")
        }
        specs.forEach { spec ->
            sb.append("    const val ").append(spec.qualifier).append(" = \"").append(spec.id).append("\"\n")
        }
        sb.append("}\n")
        write(sb.toString(), CONFIG_PKG, "AppStoreIds", fns)
    }

    /**
     * `config/AppCacheKeys.kt` — every stream cache key, NESTED per store.
     *
     * Nested rather than flat because the keys are named by ROLE (`LIST`, `item`, `of`), which is
     * unique within a store but not across them — three stores each declaring `LIST` would collide in
     * a flat object. Nesting keeps the annotations as written and makes the collision impossible
     * rather than something the author has to avoid by hand.
     */
    private fun emitCacheKeys(specs: List<StoreSpec>, fns: List<KSFunctionDeclaration>) {
        val withKeys = specs.filter { it.keys.isNotEmpty() }
        val sb = StringBuilder()
        header(sb)
        sb.append("package $CONFIG_PKG\n\n")
        sb.append("/**\n")
        sb.append(" * Stream cache keys — the strings that key per-stream freshness tracking, grouped by store.\n")
        sb.append(" *\n")
        sb.append(" * A repository references `AppCacheKeys.Loans.LIST` or `AppCacheKeys.Loans.item(id)` and never\n")
        sb.append(" * spells the format at the call site. Derived from `@CacheKey`.\n")
        sb.append(" */\n")
        sb.append("object AppCacheKeys {\n")
        if (withKeys.isEmpty()) {
            sb.append("    // No store declares a @CacheKey yet.\n")
        }
        withKeys.forEachIndexed { i, spec ->
            if (i > 0) sb.append("\n")
            sb.append("    object ").append(spec.qualifier).append(" {\n")
            spec.keys.filter { it.name.isNotBlank() }.forEach { k ->
                sb.append("        const val ").append(k.name).append(" = \"").append(k.key).append("\"\n")
            }
            spec.keys.filter { it.fn.isNotBlank() }.forEach { k ->
                val sig = k.params.joinToString(", ") { "${it.first}: ${it.second}" }
                sb.append("        fun ").append(k.fn).append("(").append(sig).append("): String = \"")
                  .append(interpolate(k.key, k.params.map { it.first })).append("\"\n")
            }
            sb.append("    }\n")
        }
        sb.append("}\n")
        write(sb.toString(), CONFIG_PKG, "AppCacheKeys", fns)
    }

    private fun emitBindings(specs: List<StoreSpec>, fns: List<KSFunctionDeclaration>) {
        val purged = specs.filter { it.logout }
        val imports = buildList {
            add("kpt.core.base.store.infra.StoreCacheManager")
            add("kpt.core.base.store.infra.impl.StoreCacheManagerImpl")
            addAll(specs.map { it.providerFqn })
            add("$CONFIG_PKG.AppStoreRegistry")
            add("org.koin.core.module.Module")
            add("org.koin.dsl.module")
        }.filter { it.isNotBlank() }.distinct().sorted()

        val sb = StringBuilder()
        header(sb)
        sb.append("package $BINDINGS_PKG\n\n")
        imports.forEach { sb.append("import $it\n") }
        sb.append("\n/**\n")
        sb.append(" * Every `@StoreProvider`: its Koin binding AND its logout purge, from one annotation.\n")
        sb.append(" *\n")
        sb.append(" * `*Mutable` stores declare `logout = false` — Store5 5.1's MutableStore is not a `Store`\n")
        sb.append(" * subtype so `register` cannot take one. Their rows still go: each shares a table with its\n")
        sb.append(" * read store, whose deleteAll wipes it.\n")
        sb.append(" *\n")
        sb.append(" * Pulled in by `StoreModule` via `includes(GeneratedStoreBindings)`.\n")
        sb.append(" */\n")
        sb.append("val GeneratedStoreBindings: Module = module {\n")
        specs.forEach { s ->
            val args = s.deps.joinToString(", ") { "$it = get()" }
            sb.append("    single(AppStoreRegistry.${s.qualifier}) { ${s.providerName}($args) }\n")
        }
        if (purged.isNotEmpty()) {
            sb.append("\n    single(createdAtStart = true) {\n")
            sb.append("        val mgr = get<StoreCacheManager>() as StoreCacheManagerImpl\n")
            purged.forEach { sb.append("        mgr.register(get(AppStoreRegistry.${it.qualifier}))\n") }
            sb.append("    }\n")
        }
        sb.append("}\n")
        write(sb.toString(), BINDINGS_PKG, "GeneratedStoreBindings", fns)
        logger.info("store-ksp: ${specs.size} store(s), ${purged.size} purged on logout")
    }

    private fun write(text: String, pkg: String, name: String, fns: List<KSFunctionDeclaration>) {
        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true, *fns.mapNotNull { it.containingFile }.toTypedArray()),
            packageName = pkg,
            fileName = name,
        ).bufferedWriter().use { it.write(text) }
    }
}

/**
 * KSP entry point — what the `META-INF/services` registration names, so Gradle can instantiate
 * [StoreProviderProcessor].
 */
class StoreProviderProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        StoreProviderProcessor(environment.codeGenerator, environment.logger)
}
