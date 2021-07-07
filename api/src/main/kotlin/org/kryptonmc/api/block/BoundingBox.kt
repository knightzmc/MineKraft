/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.block

/**
 * Represents a bounding box. This defines the box for any entity or block that may be collided with.
 * A bounding box is a three-dimensional space, with lower X, Y and Z coordinates and higher X, Y and Z coordinates.
 *
 * @param minimumX the minimum X coordinate
 * @param minimumY the minimum Y coordinate
 * @param minimumZ the minimum Z coordinate
 * @param maximumX the maximum X coordinate
 * @param maximumY the maximum Y coordinate
 * @param maximumZ the maximum Z coordinate
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
data class BoundingBox(
    val minimumX: Double,
    val minimumY: Double,
    val minimumZ: Double,
    val maximumX: Double,
    val maximumY: Double,
    val maximumZ: Double
) : Cloneable {

    /**
     * The size of this box in the X direction
     */
    val sizeX = maximumX - minimumX

    /**
     * The size of this box in the Y direction
     */
    val sizeY = maximumY - minimumY

    /**
     * The size of this box in the Z direction
     */
    val sizeZ = maximumZ - minimumZ

    /**
     * The volume of this bounding box
     */
    val volume = sizeX * sizeY * sizeZ

    /**
     * The centre position in the X direction
     */
    val centerX = minimumX + sizeX * 0.5

    /**
     * The centre position in the Y direction
     */
    val centerY = minimumY + sizeY * 0.5

    /**
     * The centre position in the Z direction
     */
    val centerZ = minimumZ + sizeZ * 0.5

    companion object {

        /**
         * Constant for an empty bounding box.
         */
        @JvmField
        val EMPTY = BoundingBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        /**
         * Constant for a full block's bounding box.
         */
        @JvmField
        val BLOCK = BoundingBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
    }
}
