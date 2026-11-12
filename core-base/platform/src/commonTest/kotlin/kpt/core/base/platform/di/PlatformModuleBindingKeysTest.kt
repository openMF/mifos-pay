/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.platform.di

import kpt.core.base.platform.intent.IntentManager
import kpt.core.base.platform.share.ShareManager
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertTrue
import com.mobilebytelabs.kmptoolkit.intentlauncher.IntentManager as ToolkitIntentManager
import com.mobilebytelabs.kmptoolkit.share.ShareManager as ToolkitShareManager

/**
 * Locks [platformModule] against Koin key collisions between the template's managers and the
 * toolkit's same-named ones.
 *
 * Koin keys a binding by the class's simple name on JS and Wasm, so `kpt…IntentManager` and the
 * toolkit's `IntentManager` (and the two `ShareManager`s) share one key there unless one side is
 * qualified. Unqualified, the later binding replaced the earlier one: the Wasm app crashed at
 * startup with "Cannot cast instance of …kmptoolkit.intentlauncher.IntentManagerImpl to
 * kpt.core.base.platform.intent.IntentManager", and JS silently handed the template's local the
 * toolkit object. On the JVM the keys never collide, so this test bites on the js/wasmJs runs.
 *
 * Resolution is asserted by type only — the managers are not exercised, because launching an
 * intent or a share sheet is a side effect a unit test must not have.
 */
class PlatformModuleBindingKeysTest {

    private val koin = koinApplication { modules(platformModule) }.koin

    @Test
    fun templateManagersResolveToTemplateTypes() {
        assertTrue(koin.get<Any>(IntentManager::class) is IntentManager, "IntentManager key holds another type")
        assertTrue(koin.get<Any>(ShareManager::class) is ShareManager, "ShareManager key holds another type")
    }

    @Test
    fun toolkitManagersResolveUnderTheirQualifier() {
        assertTrue(
            koin.get<Any>(ToolkitIntentManager::class, ToolkitQualifier) is ToolkitIntentManager,
            "toolkit IntentManager not bound under ToolkitQualifier",
        )
        assertTrue(
            koin.get<Any>(ToolkitShareManager::class, ToolkitQualifier) is ToolkitShareManager,
            "toolkit ShareManager not bound under ToolkitQualifier",
        )
    }
}
