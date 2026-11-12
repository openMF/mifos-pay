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

/** Generates Parcelable boilerplate for a class; maps to kotlinx.parcelize.Parcelize on Android. */
expect annotation class Parcelize()

/**
 * Marker for types that can be written to a [Parcel]; maps to android.os.Parcelable on Android and is a no-op
 * elsewhere.
 */
expect interface Parcelable

/** Excludes a property from parcelization; maps to kotlinx.parcelize.IgnoredOnParcel on Android. */
expect annotation class IgnoredOnParcel()

/** Custom parcel (de)serializer contract; maps to kotlinx.parcelize.Parceler on Android. */
expect interface Parceler<P> {
    /** Reads and returns an instance of [P] from [parcel]. */
    fun create(parcel: Parcel): P

    /** Writes this [P] instance into [parcel]. */
    fun P.write(parcel: Parcel, flags: Int)
}

/** Registers a custom [Parceler] for type [T]; maps to kotlinx.parcelize.TypeParceler on Android. */
expect annotation class TypeParceler<T, P : Parceler<in T>>()

/** Minimal cross-platform stand-in for android.os.Parcel; non-Android actuals are stubs. */
expect class Parcel {
    /** Reads a byte from the parcel. */
    fun readByte(): Byte

    /** Reads an int from the parcel. */
    fun readInt(): Int

    /** Reads a float from the parcel. */
    fun readFloat(): Float

    /** Reads a double from the parcel. */
    fun readDouble(): Double

    /** Reads a nullable string from the parcel. */
    fun readString(): String?

    /** Writes a byte to the parcel. */
    fun writeByte(value: Byte)

    /** Writes an int to the parcel. */
    fun writeInt(value: Int)

    /** Writes a float to the parcel. */
    fun writeFloat(value: Float)

    /** Writes a double to the parcel. */
    fun writeDouble(value: Double)

    /** Writes a nullable string to the parcel. */
    fun writeString(value: String?)
}
