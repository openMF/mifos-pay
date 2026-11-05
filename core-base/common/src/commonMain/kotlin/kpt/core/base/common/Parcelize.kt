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

/**
 * Marks a class as parcelable so it can cross an Android process/configuration boundary.
 *
 * On Android this aliases `kotlinx.parcelize.Parcelize`; on every other target it is a no-op marker,
 * which is what lets a shared model carry the annotation without fragmenting per platform.
 */
expect annotation class Parcelize()

/**
 * The platform's parcelable contract — `android.os.Parcelable` on Android, an empty marker elsewhere.
 *
 * Implement it on a shared model together with [Parcelize]; do not hand-write the read/write pair.
 */
expect interface Parcelable

/**
 * Excludes one property from parcelling, for values that are derived or cannot cross a process
 * boundary (a lambda, a coroutine scope, a cached bitmap).
 *
 * The property must have a default, since it is reconstructed rather than restored.
 */
expect annotation class IgnoredOnParcel()

/**
 * Custom parcelling for a type the platform cannot serialise on its own — a value class, a
 * third-party type, anything needing a narrower wire form than its fields.
 *
 * Pair it with [TypeParceler] at the use site. Reads must consume fields in exactly the order the
 * writes produced them: [Parcel] is positional, so a mismatch corrupts every field after it rather
 * than failing at the one that moved.
 */
expect interface Parceler<P> {
    /** Reconstructs a [P] from [parcel]. Must read fields in exactly the order [write] produced them. */
    fun create(parcel: Parcel): P

    /** Writes this [P] into [parcel]. The read side depends on this order — see [create]. */
    fun P.write(parcel: Parcel, flags: Int)
}

/**
 * Binds a [Parceler] to type [T] for one property or file, so a shared model can carry a type the
 * platform does not know how to parcel.
 */
expect annotation class TypeParceler<T, P : Parceler<in T>>()

/**
 * The platform write buffer a [Parceler] reads from and writes to.
 *
 * Strictly POSITIONAL — there are no field names on the wire, so reads must mirror the write order
 * exactly. Only the primitives below are exposed; anything richer is composed from them by a
 * [Parceler], which keeps the shared surface identical on every target.
 */
expect class Parcel {
    /** Reads the next byte. */
    fun readByte(): Byte

    /** Reads the next int. */
    fun readInt(): Int

    /** Reads the next float. */
    fun readFloat(): Float

    /** Reads the next double. */
    fun readDouble(): Double

    /** Reads the next string, which may be null. */
    fun readString(): String?

    /**
     * Writes a byte. Read back with the matching `readByte` — order is the format, so reads must mirror writes
     * exactly.
     */
    fun writeByte(value: Byte)

    /** Writes a 32-bit int. */
    fun writeInt(value: Int)

    /** Writes a 32-bit float. */
    fun writeFloat(value: Float)

    /** Writes a 64-bit double. */
    fun writeDouble(value: Double)

    /** Writes a string, which may be null. */
    fun writeString(value: String?)
}
