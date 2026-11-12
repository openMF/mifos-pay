/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.common

/** Non-Android targets implementation of `Parcelable`. */
actual interface Parcelable

/** No-op on non-Android targets — nothing is parcelled, so nothing needs excluding. */
actual annotation class IgnoredOnParcel

/** No-op on non-Android targets: there is no process boundary to cross. */
actual annotation class Parcelize

/** Non-Android targets implementation of `Parceler`. */
actual interface Parceler<P> {
    /** `create` on Non-Android targets. */
    actual fun create(parcel: Parcel): P

    /** `P` on Non-Android targets. */
    actual fun P.write(parcel: Parcel, flags: Int)
}

/** No-op on non-Android targets. */
actual annotation class TypeParceler<T, P : Parceler<in T>>

/** Non-Android targets implementation of `Parcel`. */
actual class Parcel {
    /** `readString` on Non-Android targets. */
    actual fun readString(): String? = null

    /** `readByte` on Non-Android targets. */
    actual fun readByte(): Byte = 1

    /** `readInt` on Non-Android targets. */
    actual fun readInt(): Int = 1

    /** `readFloat` on Non-Android targets. */
    actual fun readFloat(): Float = 1f

    /** `readDouble` on Non-Android targets. */
    actual fun readDouble(): Double = 1.0

    /** `writeByte` on Non-Android targets. */
    actual fun writeByte(value: Byte) {
    }

    /** `writeInt` on Non-Android targets. */
    actual fun writeInt(value: Int) {
    }

    /** `writeFloat` on Non-Android targets. */
    actual fun writeFloat(value: Float) {
    }

    /** `writeDouble` on Non-Android targets. */
    actual fun writeDouble(value: Double) {
    }

    /** `writeString` on Non-Android targets. */
    actual fun writeString(value: String?) {
    }
}
