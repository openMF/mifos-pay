/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

/**
 * Marker for types that can be written to a [Parcel]; maps to android.os.Parcelable on Android and is a no-op
 * elsewhere.
 */
actual interface Parcelable

/** Excludes a property from parcelization; maps to kotlinx.parcelize.IgnoredOnParcel on Android. */
actual annotation class IgnoredOnParcel

/** Generates Parcelable boilerplate for a class; maps to kotlinx.parcelize.Parcelize on Android. */
actual annotation class Parcelize

/** Custom parcel (de)serializer contract; maps to kotlinx.parcelize.Parceler on Android. */
actual interface Parceler<P> {
    /** Reads and returns an instance of [P] from [parcel]. */
    actual fun create(parcel: Parcel): P

    /** Writes this [P] instance into [parcel]. */
    actual fun P.write(parcel: Parcel, flags: Int)
}

/** Registers a custom [Parceler] for type [T]; maps to kotlinx.parcelize.TypeParceler on Android. */
actual annotation class TypeParceler<T, P : Parceler<in T>>

/** Minimal cross-platform stand-in for android.os.Parcel; non-Android actuals are stubs. */
actual class Parcel {
    /** Reads a nullable string from the parcel. */
    actual fun readString(): String? = null

    /** Reads a byte from the parcel. */
    actual fun readByte(): Byte = 1

    /** Reads an int from the parcel. */
    actual fun readInt(): Int = 1

    /** Reads a float from the parcel. */
    actual fun readFloat(): Float = 1f

    /** Reads a double from the parcel. */
    actual fun readDouble(): Double = 1.0

    /** Writes a byte to the parcel. */
    actual fun writeByte(value: Byte) {
    }

    /** Writes an int to the parcel. */
    actual fun writeInt(value: Int) {
    }

    /** Writes a float to the parcel. */
    actual fun writeFloat(value: Float) {
    }

    /** Writes a double to the parcel. */
    actual fun writeDouble(value: Double) {
    }

    /** Writes a nullable string to the parcel. */
    actual fun writeString(value: String?) {
    }
}
