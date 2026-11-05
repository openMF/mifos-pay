/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifospay.core.common

/**
 * Function type for parsing an error response body into a user-friendly message.
 * Repositories provide custom parsing logic (e.g. parsing MifosError) that the
 * ScreenState error mappers ([asScreenStateFlow], [MifosErrorMapper.mapErrorBody]) consume.
 */
typealias ErrorBodyParser = suspend (responseBody: String, statusCode: Int) -> String

/**
 * Default error body parser that returns the response body as-is or a generic message.
 */
val defaultErrorBodyParser: ErrorBodyParser = { body, _ ->
    body.ifBlank { "An error occurred" }
}
