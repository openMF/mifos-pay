/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.core.model

/**
 * Created by dilpreet on 3/6/17.
 */
enum class LoanState {

    /** The application is a new loan being created. */
    CREATE,

    /** The application edits an existing loan. */
    UPDATE,
}
