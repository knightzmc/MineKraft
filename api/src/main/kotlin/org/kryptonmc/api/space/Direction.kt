/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.space

import org.kryptonmc.api.util.StringSerializable
import org.kryptonmc.api.util.asLong
import org.kryptonmc.api.util.floor
import org.spongepowered.math.vector.Vector3i
import java.util.function.Predicate
import kotlin.math.abs

/**
 * Represents a three-dimensional
 * [Cardinal direction](https://en.wikipedia.org/wiki/Cardinal_direction).
 *
 * @param serialized the name
 * @param axis the axis of this direction
 * @param axisDirection the direction of the axis of this direction
 * @param normal the normal of this direction
 */
enum class Direction(
    private val data3D: Int,
    private val oppositeIndex: Int,
    private val data2D: Int,
    override val serialized: String,
    val axis: Axis,
    val axisDirection: AxisDirection,
    val normal: Vector3i
) : StringSerializable {

    DOWN(0, 1, -1, "down", Axis.Y, AxisDirection.NEGATIVE, Vector3i(0, -1, 0)),
    UP(1, 0, -1, "up", Axis.Y, AxisDirection.POSITIVE, Vector3i(0, 1, 0)),
    NORTH(2, 3, 2, "north", Axis.Z, AxisDirection.NEGATIVE, Vector3i(0, 0, -1)),
    SOUTH(3, 2, 0, "south", Axis.Z, AxisDirection.POSITIVE, Vector3i(0, 0, 1)),
    WEST(4, 5, 1, "west", Axis.X, AxisDirection.NEGATIVE, Vector3i(-1, 0, 0)),
    EAST(5, 4, 3, "east", Axis.X, AxisDirection.POSITIVE, Vector3i(1, 0, 0));

    /**
     * The normal on the X axis.
     */
    val normalX = normal.x()

    /**
     * The normal on the Y axis.
     */
    val normalY = normal.y()

    /**
     * The normal on the Z axis.
     */
    val normalZ = normal.z()

    /**
     * This direction converted to a pitch value.
     */
    val pitch = ((data2D and 3) * 90).toFloat()

    /**
     * The opposite of this direction.
     *
     * Initialized lazily to avoid a circular dependency.
     */
    val opposite by lazy { from3D(oppositeIndex) }

    /**
     * Axes that a direction may be on.
     *
     * @param serialized the name
     */
    enum class Axis(override val serialized: String) : (Direction?) -> Boolean, Predicate<Direction?>, StringSerializable {

        X("x"),
        Y("y"),
        Z("z");

        /**
         * If this axis tiles vertically.
         */
        val isVertical: Boolean
            get() = this == Y

        /**
         * If this axis tiles horizontally.
         */
        val isHorizontal: Boolean
            get() = this == X || this == Z

        override fun invoke(direction: Direction?) = direction != null && direction.axis == this

        override fun test(t: Direction?) = invoke(t)
    }

    /**
     * The direction of an [Axis] on a plane.
     *
     * @param step the step
     * @param serialized the name
     */
    enum class AxisDirection(val step: Int, override val serialized: String) : StringSerializable {

        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");

        /**
         * The opposite direction.
         *
         * Initialized lazily to avoid circular dependencies.
         */
        val opposite by lazy { if (this == POSITIVE) NEGATIVE else POSITIVE }
    }

    companion object {

        private val BY_3D_DATA = values().sortedArrayWith { o1, o2 -> o1.data3D.compareTo(o2.data3D) }
        private val BY_2D_DATA: Array<Direction> = values().asSequence()
            .filter { it.axis.isHorizontal }
            .sortedWith { o1, o2 -> o1.data2D.compareTo(o2.data2D) }
            .toList()
            .toTypedArray()
        private val BY_OFFSET = values().associateBy { it.normal.asLong() }

        /**
         * Gets the [Direction] from the given [normal].
         *
         * @param normal the normal
         * @return the direction
         */
        fun fromNormal(normal: Vector3i) = BY_OFFSET[normal.asLong()]

        /**
         * Gets the [Direction] from the given [x], [y], and [z] normal coordinates.
         *
         * @param x the normal X
         * @param y the normal Y
         * @param z the normal Z
         * @return the direction
         */
        fun fromNormal(x: Int, y: Int, z: Int) = BY_OFFSET[asLong(x, y, z)]

        /**
         * Gets the [Direction] from the given [pitch].
         *
         * @param pitch the pitch
         * @return the direction
         */
        @Suppress("MagicNumber")
        fun fromPitch(pitch: Double) = from2D((pitch / 90.0 + 0.5).floor() and 3)

        private fun from2D(value: Int) = BY_2D_DATA[abs(value % BY_2D_DATA.size)]

        private fun from3D(value: Int) = BY_3D_DATA[abs(value % BY_3D_DATA.size)]
    }
}
