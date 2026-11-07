/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app

import org.koin.core.annotation.KoinInternalApi
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Every repository binding in the app graph can actually be constructed.
 *
 * The repositories bind themselves through `@RepositoryBinding` / `@DataProvider`, and data-ksp writes
 * the Koin lines. A compile proves those lines type-check; it does not prove each `get()` finds a
 * binding — a dropped qualifier or a parameter the generator skipped only fails when the screen asks.
 * This resolves them all on the JVM, with the same module set the app starts.
 */
class RepositoryGraphResolvesTest {

    @OptIn(KoinInternalApi::class)
    @Test
    fun everyRepositoryResolves() {
        val app = koinApplication(createEagerInstances = false) {
            allowOverride(true)
            modules(cmp.navigation.di.KoinModules.allModules)
        }
        val failures = app.koin.instanceRegistry.instances.values
            .map { it.beanDefinition }
            .distinct()
            .filter { it.primaryType.simpleName.orEmpty().endsWith("Repository") }
            .mapNotNull { def ->
                runCatching { app.koin.get<Any>(def.primaryType, def.qualifier) }
                    .exceptionOrNull()
                    ?.let { "${def.primaryType.qualifiedName} [${def.qualifier?.value.orEmpty()}]: ${it.message}" }
            }
        app.close()
        assertTrue(failures.isEmpty(), "Repositories the graph cannot build:\n" + failures.joinToString("\n"))
    }
}
