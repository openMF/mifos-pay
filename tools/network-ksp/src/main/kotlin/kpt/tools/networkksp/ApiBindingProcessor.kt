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

        val bindings = annotated.mapNotNull { decl ->
            val id = decl.annotations
                .firstOrNull { it.shortName.asString() == "ApiBinding" }
                ?.arguments?.firstOrNull { it.name?.asString() == "accessPoint" }
                ?.value as? String
            if (id.isNullOrBlank()) {
                logger.error("network-ksp: @ApiBinding on ${decl.simpleName.asString()} has a blank accessPoint")
                return@mapNotNull null
            }
            val kind = kinds[id]
            if (kind == null) {
                logger.error(
                    "network-ksp: @ApiBinding(\"$id\") on ${decl.simpleName.asString()} names an access point " +
                        "that app-profile does not declare (declared: ${kinds.keys.joinToString(", ")})",
                )
                return@mapNotNull null
            }
            val fqn = decl.qualifiedName?.asString() ?: return@mapNotNull null

            // SUPABASE: bind the INTERFACE, not the concrete class.
            //
            // `supabaseApi<T>` is `single<T> { … }` with T inferred from the factory lambda, so
            // `{ ServerInstanceApiImpl(it) }` would register the IMPL type and `get<ServerInstanceApi>()` would
            // fail at runtime — after compiling cleanly, which is the worst place to find it. The
            // annotation stays on the impl (that is what gets constructed); the processor resolves
            // its single directly-implemented interface and binds that instead.
            //
            // Exactly one supertype interface is required: zero means there is no seam to bind
            // against (the old one-concrete-class shape — still valid, bind the class itself), and
            // more than one is ambiguous, which is an author error rather than something to guess at.
            val bound = if (kind == "supabase") {
                val ifaces = decl.superTypes
                    .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
                    .filter { it.classKind == ClassKind.INTERFACE }
                    .mapNotNull { it.qualifiedName?.asString() }
                    .toList()
                when (ifaces.size) {
                    0 -> fqn
                    1 -> ifaces.single()
                    else -> {
                        logger.error(
                            "network-ksp: @ApiBinding(\"$id\") on ${decl.simpleName.asString()} implements " +
                                "${ifaces.size} interfaces (${ifaces.joinToString()}) — cannot decide which to " +
                                "bind. Give the API type exactly one interface.",
                        )
                        return@mapNotNull null
                    }
                }
            } else {
                fqn
            }
            Triple(id, "$fqn|$bound", kind)
        }

        duplicateBindingErrors(bindings.map { (id, packed, _) -> id to packed.substringAfter('|') })
            .forEach(logger::error)

        // app-profile order, so the generated file reads like the declaration it mirrors — and is
        // stable, which KSP's own symbol order is not.
        val ordered = bindings.sortedBy { kinds.keys.indexOf(it.first) }
        write(render(ordered), annotated)
        logger.info("network-ksp: ${ordered.size} api binding(s)")
        return emptyList()
    }

    private fun render(bindings: List<Triple<String, String, String>>): String {
        val imports = sortedSetOf("org.koin.core.module.Module", "org.koin.dsl.module")
        val lines = StringBuilder()
        var rest = 0
        var supa = 0
        bindings.forEach { (id, packed, kind) ->
            val implFqn = packed.substringBefore('|')
            val boundFqn = packed.substringAfter('|')
            val fqn = implFqn
            val pkg = implFqn.substringBeforeLast('.')
            val simple = implFqn.substringAfterLast('.')
            if (kind == "supabase") {
                imports += implFqn
                imports += "kpt.core.base.network.supabaseApi"
                if (boundFqn != implFqn) {
                    // Explicit type argument — without it `single<T>` infers the impl and every
                    // `get<Interface>()` misses.
                    imports += boundFqn
                    val boundSimple = boundFqn.substringAfterLast('.')
                    lines.append("    supabaseApi<$boundSimple>(\"$id\") { $simple(it) }\n")
                } else {
                    lines.append("    supabaseApi(\"$id\") { $simple(it) }\n")
                }
                supa++
            } else {
                // Ktorfit emits `create<Simple>()` as a top-level extension beside the interface.
                imports += "$pkg.create$simple"
                imports += "kpt.core.base.network.restApi"
                lines.append("    restApi(\"$id\") { it.create$simple() }\n")
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
 * The one binding conflict that is real: the same bound TYPE registered twice.
 *
 * Every binding expands to an UNQUALIFIED `single<T>` (`restApi` / `supabaseApi` in
 * `core-base/network/NetworkDsl.kt`), so Koin keys it by type alone. Two different types on one access
 * point are two independent definitions — one point serves any number of APIs (a monolith backend binds
 * ~22 to a single point). The same type bound twice, on one point or on two, is one key defined twice:
 * the later definition silently wins for every injection site. This used to group by access point,
 * which rejected the valid case and missed the cross-point one.
 *
 * @param bindings (access point id, bound type FQN) per `@ApiBinding`.
 * @return one error message per type bound more than once; empty when the graph is sound.
 */
internal fun duplicateBindingErrors(bindings: List<Pair<String, String>>): List<String> =
    bindings.groupBy({ it.second }, { it.first })
        .filterValues { it.size > 1 }
        .map { (type, points) ->
            "network-ksp: $type is bound ${points.size} times (access points: ${points.joinToString()}) — " +
                "each binding is an unqualified single<$type>, so all but the last are silently lost. " +
                "Bind a type once; one access point may serve any number of different types."
        }

/**
 * KSP entry point — what the `META-INF/services` registration names, so Gradle can instantiate [ApiBindingProcessor].
 */
class ApiBindingProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        ApiBindingProcessor(environment.codeGenerator, environment.logger, environment.options)
}
