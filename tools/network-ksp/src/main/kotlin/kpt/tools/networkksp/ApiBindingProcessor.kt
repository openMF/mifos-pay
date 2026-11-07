/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.tools.networkksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Derives `GeneratedApiBindings` from `@ApiBinding` on the API types.
 *
 * The endpoint TOPOLOGY stays in `app-profile/app.yaml#network.access_points` — base URL, kind,
 * owner, secrets — because that is per-fork deployment config and these API classes are
 * template-owned. Only the class↔point link moved here, off the point's old `api: <FQN>` string.
 *
 * The KIND still comes from the point (a class cannot know whether its endpoint is REST or Supabase
 * in a given fork), passed in as a ksp arg. That is also what makes an unknown `accessPoint` a build
 * error rather than a silently-dropped binding: an id with no declared kind is an id no point
 * declares, and a binding for a non-existent endpoint fails at Koin graph construction — at runtime,
 * on a device, far from the typo.
 */
class ApiBindingProcessor(
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

        // "id:rest|id:supabase|…" — the declared points, in app-profile order.
        val kinds = LinkedHashMap<String, String>()
        options[OPT_KINDS]?.split('|')?.forEach { row ->
            val id = row.substringBefore(':').trim()
            val kind = row.substringAfter(':', "").trim()
            if (id.isNotEmpty() && kind.isNotEmpty()) kinds[id] = kind
        }

        val annotated = resolver.getSymbolsWithAnnotation(ANN)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        // One binding per (type, access point). `@ApiBinding("main", "selfservice")` is ONE interface
        // served by several endpoints — a Fineract client API on the admin server and on its
        // self-service API, say. The FIRST id is the default (unqualified) binding; each further id is
        // qualified by its own id, so a consumer picks it with `@FromQualifier("<id>")`.
        val bindings = annotated.flatMap { decl ->
            val ids = decl.annotations
                .firstOrNull { it.shortName.asString() == "ApiBinding" }
                ?.arguments?.firstOrNull { it.name?.asString() == "accessPoints" }
                ?.value.let { v -> (v as? List<*>)?.filterIsInstance<String>() ?: listOfNotNull(v as? String) }
            if (ids.isEmpty() || ids.any(String::isBlank)) {
                logger.error("network-ksp: @ApiBinding on ${decl.simpleName.asString()} has a blank access point")
                return@flatMap emptyList()
            }
            if (ids.toSet().size != ids.size) {
                logger.error(
                    "network-ksp: @ApiBinding on ${decl.simpleName.asString()} lists an access point twice: $ids",
                )
                return@flatMap emptyList()
            }
            ids.mapIndexedNotNull { index, id -> bindingFor(decl, id, kinds, qualified = index > 0) }
        }

        duplicateBindingErrors(bindings.map { ApiBindingKey(it.id, it.bound, it.qualified) })
            .forEach(logger::error)

        // app-profile order, so the generated file reads like the declaration it mirrors — and is
        // stable, which KSP's own symbol order is not.
        val ordered = bindings.sortedWith(compareBy({ kinds.keys.indexOf(it.id) }, { it.impl }))
        write(render(ordered), annotated)
        logger.info("network-ksp: ${ordered.size} api binding(s)")
        return emptyList()
    }

    /** One generated binding: the API type [impl] (bound as [bound]) on access point [id]. */
    private data class Binding(
        val id: String,
        val impl: String,
        val bound: String,
        val kind: String,
        val qualified: Boolean,
    )

    /** The binding of [decl] on access point [id], or null (with an error logged) when it cannot be derived. */
    private fun bindingFor(
        decl: KSClassDeclaration,
        id: String,
        kinds: Map<String, String>,
        qualified: Boolean,
    ): Binding? {
        val kind = kinds[id]
        val fqn = decl.qualifiedName?.asString()
        if (kind == null || fqn == null) {
            if (kind == null) {
                logger.error(
                    "network-ksp: @ApiBinding(\"$id\") on ${decl.simpleName.asString()} names an access point " +
                        "that app-profile does not declare (declared: ${kinds.keys.joinToString(", ")})",
                )
            }
            return null
        }
        return boundTypeFor(decl, id, fqn, kind)
            ?.let { bound -> Binding(id = id, impl = fqn, bound = bound, kind = kind, qualified = qualified) }
    }

    /**
     * The type [decl] is bound as on a point of [kind]: itself for REST, its single interface for Supabase.
     *
     * SUPABASE binds the INTERFACE, not the concrete class. `supabaseApi<T>` is `single<T> { … }` with T
     * inferred from the factory lambda, so `{ ServerInstanceApiImpl(it) }` would register the IMPL type and
     * `get<ServerInstanceApi>()` would fail at runtime — after compiling cleanly, which is the worst place to
     * find it. The annotation stays on the impl (that is what gets constructed); this resolves its single
     * directly-implemented interface. Zero interfaces means there is no seam (bind the class itself); more
     * than one is ambiguous — an author error, logged, null.
     */
    private fun boundTypeFor(decl: KSClassDeclaration, id: String, fqn: String, kind: String): String? {
        if (kind != "supabase") return fqn
        val ifaces = decl.superTypes
            .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
            .filter { it.classKind == ClassKind.INTERFACE }
            .mapNotNull { it.qualifiedName?.asString() }
            .toList()
        return when (ifaces.size) {
            0 -> fqn
            1 -> ifaces.single()
            else -> {
                logger.error(
                    "network-ksp: @ApiBinding(\"$id\") on ${decl.simpleName.asString()} implements " +
                        "${ifaces.size} interfaces (${ifaces.joinToString()}) — cannot decide which to " +
                        "bind. Give the API type exactly one interface.",
                )
                null
            }
        }
    }

    private fun render(bindings: List<Binding>): String {
        val imports = sortedSetOf("org.koin.core.module.Module", "org.koin.dsl.module")
        val lines = StringBuilder()
        var rest = 0
        var supa = 0
        bindings.forEach { b ->
            val id = b.id
            val kind = b.kind
            val implFqn = b.impl
            val boundFqn = b.bound
            val pkg = implFqn.substringBeforeLast('.')
            val simple = implFqn.substringAfterLast('.')
            // A further access point of a multi-point binding is qualified by its own id.
            val point = if (b.qualified) {
                imports += "org.koin.core.qualifier.named"
                "\"$id\", qualifier = named(\"$id\")"
            } else {
                "\"$id\""
            }
            if (kind == "supabase") {
                imports += implFqn
                imports += "kpt.core.base.network.supabaseApi"
                if (boundFqn != implFqn) {
                    // Explicit type argument — without it `single<T>` infers the impl and every
                    // `get<Interface>()` misses.
                    imports += boundFqn
                    val boundSimple = boundFqn.substringAfterLast('.')
                    lines.append("    supabaseApi<$boundSimple>($point) { $simple(it) }\n")
                } else {
                    lines.append("    supabaseApi($point) { $simple(it) }\n")
                }
                supa++
            } else {
                // Ktorfit emits `create<Simple>()` as a top-level extension beside the interface.
                imports += "$pkg.create$simple"
                imports += "kpt.core.base.network.restApi"
                lines.append("    restApi($point) { it.create$simple() }\n")
                rest++
            }
        }
        return buildString {
            append(licenseHeader)
            append("package $PKG\n\n")
            imports.forEach { append("import ").append(it).append('\n') }
            append("\n/**\n")
            append(" * GENERATED from `@ApiBinding` — one Koin binding per bound access point. DO NOT HAND-EDIT.\n")
            append(" *\n")
            append(" * To add an API: declare the endpoint in `app-profile/app.yaml#network.access_points`\n")
            append(" * (its URL, kind and owner are deployment config, not a property of the class), then write\n")
            append(" * the API type and annotate it `@ApiBinding(\"<id>\")`. There is no wiring step.\n")
            append(" * One type served by several endpoints lists them all — `@ApiBinding(\"main\", \"self\")`:\n")
            append(" * the first is the default binding, the others are qualified by their id.\n")
            append(" *\n")
            append(" * $rest REST + $supa Supabase binding(s).\n")
            append(" *\n")
            append(" * Pulled in by `NetworkModule` via `includes(GeneratedApiBindings)`.\n")
            append(" */\n")
            append("val GeneratedApiBindings: Module = module {\n")
            append(lines)
            append("}\n")
        }
    }

    private fun write(text: String, decls: List<KSClassDeclaration>) {
        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true, *decls.mapNotNull { it.containingFile }.toTypedArray()),
            packageName = PKG,
            fileName = "GeneratedApiBindings",
        ).bufferedWriter().use { it.write(text) }
    }

    private companion object {
        const val PKG = "kpt.core.network.di"
        const val ANN = "kpt.core.base.network.annotation.ApiBinding"
        const val OPT_KINDS = "kpt.network.accessPointKinds"
    }
}

/**
 * One Koin key a generated binding defines: the bound [type] on access point [accessPoint], [qualified] by
 * that point's id when it is not the type's first (default) point.
 */
internal data class ApiBindingKey(val accessPoint: String, val type: String, val qualified: Boolean)

/**
 * The one binding conflict that is real: the same Koin key defined twice.
 *
 * A type's first access point binds it UNQUALIFIED (`single<T>`), so Koin keys it by type alone; each
 * further point of a multi-point `@ApiBinding` is qualified by its id and is a key of its own. Two
 * different types on one point are independent — one point serves any number of APIs (a monolith binds
 * ~22 to one point). The same type bound UNQUALIFIED twice — by two annotations, on one point or on two —
 * is one key defined twice: the later definition silently wins for every injection site.
 *
 * @param bindings one key per generated binding.
 * @return one error message per type bound unqualified more than once; empty when the graph is sound.
 */
internal fun duplicateBindingErrors(bindings: List<ApiBindingKey>): List<String> =
    bindings.filterNot { it.qualified }
        .groupBy({ it.type }, { it.accessPoint })
        .filterValues { it.size > 1 }
        .map { (type, points) ->
            "network-ksp: $type is bound ${points.size} times (access points: ${points.joinToString()}) — " +
                "each is an unqualified single<$type>, so all but the last are silently lost. " +
                "Bind a type once; to serve it from several endpoints list them in ONE " +
                "@ApiBinding(\"a\", \"b\")."
        }

/**
 * KSP entry point — what the `META-INF/services` registration names, so Gradle can instantiate [ApiBindingProcessor].
 */
class ApiBindingProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        ApiBindingProcessor(environment.codeGenerator, environment.logger, environment.options)
}
