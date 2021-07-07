/*
 * This file is part of the Krypton project, licensed under the GNU General Public License v3.0
 *
 * Copyright (C) 2021 KryptonMC and the contributors of the Krypton project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.kryptonmc.krypton.world.chunk

import org.kryptonmc.api.space.Position
import org.spongepowered.math.GenericMath
import org.spongepowered.math.vector.Vector3i
import kotlin.math.floor

/**
 * Holds a pair of chunk coordinates (x and z)
 */
data class ChunkPosition(val x: Int, val z: Int) {

    /**
     * Region coordinates for this chunk coordinate pair
     */
    val regionX = x shr 5
    val regionZ = z shr 5

    /**
     * Region local values are the coordinates relative to the region they are in
     */
    val regionLocalX = x and 0x1F
    val regionLocalZ = z and 0x1F

    operator fun contains(location: Position) = floor(location.x / 16.0).toInt() == x && floor(location.z / 16.0).toInt() == z

    operator fun contains(position: Vector3i) = GenericMath.floor(position.x() / 16.0) == x && GenericMath.floor(position.z() / 16.0) == z

    fun toLong() = toLong(x, z)

    companion object {

        val ZERO = ChunkPosition(0, 0)

        fun toLong(x: Int, z: Int) = x.toLong() and 4294967295L or (z.toLong() and 4294967295L shl 32)
    }
}
