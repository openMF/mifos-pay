/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app.di

import org.koin.core.annotation.KoinInternalApi
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * On JS / Wasm Koin keys a binding by the type's SIMPLE name (`KClass.qualifiedName` is not
 * available there), so two bindings whose types share a simple name — the template's
 * `kpt.core.datastore.UserPreferencesRepository` and the app's
 * `kpt.core.datastore.UserPreferencesRepository` — collide: the later one replaces the
 * earlier, and a consumer of the other type gets the wrong object. The web app then dies at startup
 * (`this.preferencesRepository_1.get_observeDarkThemeConfig_… is not a function`) while Android,
 * iOS and desktop, which key by full name, work.
 *
 * This loads the same module set as `initKoin()` and fails when two bindings would share a key on
 * the web. A collision is fixed by giving one side a qualifier.
 */
class KoinSimpleNameCollisionTest {

    @OptIn(KoinInternalApi::class)
    @Test
    fun noTwoBindingsShareAWebKey() {
        val app = koinApplication(createEagerInstances = false) {
            allowOverride(true)
            // The same module set as cmp.shared.utils.initKoin(); the fork's arrive through
            // ForkFeatureRegistry.featureKoinModules.
            modules(cmp.navigation.di.KoinModules.allModules)
        }
        val webKeys = app.koin.instanceRegistry.instances.values
            .map { it.beanDefinition }
            .distinct()
            .flatMap { def ->
                (listOf(def.primaryType) + def.secondaryTypes).map { type ->
                    val webKey = "${type.simpleName}:${def.qualifier?.value.orEmpty()}:${def.scopeQualifier.value}"
                    webKey to (type.qualifiedName ?: type.toString())
                }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, names) -> names.toSet() }
            .filterValues { it.size > 1 }

        assertTrue(
            webKeys.isEmpty(),
            "These bindings share a Koin key on JS / Wasm (simple name + qualifier); qualify one side:\n" +
                webKeys.entries.joinToString("\n") { (key, names) -> "  $key -> $names" },
        )
        app.close()
    }
}
