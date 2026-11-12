/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/**
 * Provides an abstraction layer for accessing localized string resources.
 *
 * This interface is designed to decouple business logic (e.g., ViewModels) from platform-specific
 * implementations such as Android’s `Resources.getSystem()` or Compose’s `getString()` API.
 *
 * ## Why this abstraction exists:
 * Using `getString(StringResource)` directly in a ViewModel or business logic can break unit tests,
 * especially when running on the JVM, where Android platform APIs are not available or mocked.
 *
 * To avoid exceptions like:
 * ```
 * Method getSystem in android.content.res.Resources not mocked.
 * ```
 * the [StringProvider] allows injecting a test-safe mock/fake implementation during testing,
 * while still using the actual `getString()` logic in production via [DefaultStringProvider].
 *
 * ## Example (in ViewModel or shared logic):
 * ```
 * val message = stringProvider.get(Res.string.feature_auth_error_email_required)
 * ```
 */
interface StringProvider {

    /**
     * Resolves the given [StringResource] into a localized string.
     *
     * @param resource The string resource to retrieve.
     * @param formatArgs Optional arguments for formatting.
     * @return The final localized and formatted string.
     */
    suspend fun get(resource: StringResource, vararg formatArgs: Any = emptyArray()): String
}

/**
 * Default implementation of [StringProvider] that uses Compose Multiplatform’s [getString].
 *
 * This class is meant to be used in runtime environments where resource resolution is supported.
 * It should not be used in unit tests—use a mock or fake instead to avoid runtime exceptions
 * from accessing platform APIs like `Resources.getSystem`.
 */
class DefaultStringProvider : StringProvider {

    /**
     * Retrieves and formats a localized string from a [StringResource].
     *
     * @param resource The resource identifier to resolve.
     * @param formatArgs Optional format arguments.
     * @return The resolved localized string.
     */
    override suspend fun get(resource: StringResource, vararg formatArgs: Any): String {
        return getString(resource, *formatArgs)
    }
}
