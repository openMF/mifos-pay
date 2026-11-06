/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.user

import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
@Parcelize
class NewUser(
    val username: String,
    val firstname: String,
    val lastname: String,
    val email: String,
    val password: String,
) : Parcelable
