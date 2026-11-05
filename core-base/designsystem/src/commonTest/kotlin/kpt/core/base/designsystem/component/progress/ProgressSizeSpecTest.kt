/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.component.progress

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pure-function tests for [ProgressSizeSpec]. The renderer reads these dp pairs at
 * render time — these tests guard the canonical (diameter, stroke) mapping.
 */
class ProgressSizeSpecTest {

    @Test
    fun xsMapsTo16and2() {
        assertEquals(16 to 2, ProgressSizeSpec.dpFor(ProgressSize.Xs))
    }

    @Test
    fun smMapsTo24and2() {
        assertEquals(24 to 2, ProgressSizeSpec.dpFor(ProgressSize.Sm))
    }

    @Test
    fun mdMapsTo36and3() {
        assertEquals(36 to 3, ProgressSizeSpec.dpFor(ProgressSize.Md))
    }

    @Test
    fun lgMapsTo48and4() {
        assertEquals(48 to 4, ProgressSizeSpec.dpFor(ProgressSize.Lg))
    }

    @Test
    fun diameterIsMonotonicAcrossSizes() {
        val sizes = ProgressSize.values().toList()
        val diameters = sizes.map { ProgressSizeSpec.dpFor(it).first }
        assertEquals(diameters.sorted(), diameters, "diameter must be monotonic non-decreasing")
    }

    @Test
    fun strokeIsMonotonicAcrossSizes() {
        val sizes = ProgressSize.values().toList()
        val strokes = sizes.map { ProgressSizeSpec.dpFor(it).second }
        assertEquals(strokes.sorted(), strokes, "stroke must be monotonic non-decreasing")
    }
}
