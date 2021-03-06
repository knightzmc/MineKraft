/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.world

import org.jetbrains.annotations.Contract
import org.kryptonmc.api.space.AbstractPosition
import org.kryptonmc.api.space.Position.Companion.EPSILON
import org.kryptonmc.api.space.Vector
import kotlin.math.abs

/**
 * A [Location] is a three-dimensional space in a world. That is, it possesses
 * an x, y and z coordinate, as well as angles of rotation in [pitch] and [yaw].
 *
 * As opposed to a [Vector], a [Location] is bound to a world.
 *
 * @see [Vector]
 */
class Location @JvmOverloads constructor(
    x: Double,
    y: Double,
    z: Double,
    val yaw: Float = 0F,
    val pitch: Float = 0F
) : AbstractPosition(x, y, z) {

    /**
     * Convert this [Location] to a [Vector].
     *
     * @return the new vector from this location
     */
    @Contract("_ -> new", pure = true)
    @Suppress("unused")
    fun toVector() = Vector(x, y, z)

    override fun equals(other: Any?) = other is Location &&
            abs(x - other.x) < EPSILON &&
            abs(y - other.y) < EPSILON &&
            abs(z - other.z) < EPSILON &&
            abs(yaw - other.yaw) < EPSILON &&
            abs(pitch - other.pitch) < EPSILON

    override fun hashCode(): Int {
        var hash = 57
        hash *= 19 + (x.toRawBits() xor (x.toRawBits() shr 32)).toInt()
        hash *= 19 + (y.toRawBits() xor (y.toRawBits() shr 32)).toInt()
        hash *= 19 + (z.toRawBits() xor (z.toRawBits() shr 32)).toInt()
        hash *= 19 + yaw.toRawBits()
        hash *= 19 + pitch.toRawBits()
        return hash
    }

    override fun toString() = "Location(x=$x, y=$y, z=$z, yaw=$yaw, pitch=$pitch)"

    override fun copy(x: Double, y: Double, z: Double) = Location(x, y, z, yaw, pitch)

    /**
     * Create a copy of this location with the specified values applied to it.
     */
    fun copy(
        x: Double = this.x,
        y: Double = this.y,
        z: Double = this.z,
        yaw: Float = this.yaw,
        pitch: Float = this.pitch
    ) = Location(x, y, z, yaw, pitch)

    companion object {

        /**
         * A constant for the location at the centre of the world with 0 yaw and 0 pitch.
         */
        val ZERO = Location(0.0, 0.0, 0.0, 0F, 0F)
    }
}
