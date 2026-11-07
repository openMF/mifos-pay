/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network

/**
 * Generic server response holding arbitrary key/value fields.
 *
 * @property responseFields Response fields keyed by field name.
 */
data class GenericResponse(
    var responseFields: HashMap<String, Any> = hashMapOf(),
)
