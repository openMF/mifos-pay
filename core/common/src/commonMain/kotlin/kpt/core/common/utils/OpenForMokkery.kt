/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.utils

/**
 * Annotation to mark a class as open for mocking during tests using Mokkery.
 *
 * When used with the Kotlin AllOpen plugin, any class annotated with this will be treated as `open`
 * during test compilation, allowing frameworks like Mokkery to create test doubles.
 *
 * ### Usage Example:
 * ```
 * @OpenForMokkery
 * class SomeService {
 *     fun doWork() = ...
 * }
 * ```
 *
 * In your build.gradle.kts:
 * ```
 * plugins {
 *     kotlin("plugin.allopen")
 * }
 *
 * allOpen {
 *     annotation("your.package.OpenForMokkery")
 * }
 * ```
 */
annotation class OpenForMokkery
