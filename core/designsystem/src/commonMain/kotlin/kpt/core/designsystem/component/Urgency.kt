/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

/** Due-date urgency tier — informs the color of a leading dot on a list row. */
enum class Urgency {
    /** Due date has passed. */
    Overdue,

    /**
     * Due today. Its own band rather than part of `Upcoming`, because today is the only one that is still actionable
     * and not yet late.
     */
    Today,

    /** Due within the reminder window. */
    Upcoming,

    /** Beyond the reminder window — listed, but not highlighted. */
    Distant,
}
