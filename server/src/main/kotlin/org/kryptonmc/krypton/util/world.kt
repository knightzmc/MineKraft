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
package org.kryptonmc.krypton.util

import org.kryptonmc.api.space.Position
import org.kryptonmc.api.util.floor
import org.kryptonmc.api.world.Location
import org.kryptonmc.krypton.entity.KryptonEntity
import org.kryptonmc.krypton.world.KryptonWorld
import org.kryptonmc.krypton.world.chunk.ChunkPosition
import kotlin.math.abs

val Position.isInSpawnableBounds: Boolean
    get() = !blockX.isOutsideSpawnableHeight && isInHorizontalWorldBounds

val Int.isOutsideSpawnableHeight: Boolean
    get() = this < -20000000 || this >= 20000000

val Position.isInHorizontalWorldBounds: Boolean
    get() = blockX >= -30000000 && blockZ >= -30000000 && blockX < 30000000 && blockZ < 30000000

fun KryptonWorld.forEachInRange(location: Location, viewDistance: Int, callback: (KryptonEntity) -> Unit) {
    val chunksInRange = location.chunksInRange(viewDistance)
    chunksInRange.forEach {
        val chunk = getChunkAt(it.chunkX, it.chunkZ) ?: return@forEach
        getEntitiesInChunk(chunk).forEach(callback)
    }
}

val Long.chunkX: Int
    get() = (this and 4294967295L).toInt()

val Long.chunkZ: Int
    get() = (this ushr 32 and 4294967295L).toInt()

private fun Location.chunksInRange(range: Int): LongArray {
    val visible = LongArray(range.toArea())
    var xDistance = 0
    var xDirection = 1
    var zDistance = 0
    var zDirection = -1
    var length = 1
    var corner = 0

    for (i in visible.indices) {
        val chunkX = (xDistance * 16 + x).toChunkCoordinate()
        val chunkZ = (zDistance * 16 + z).toChunkCoordinate()
        visible[i] = ChunkPosition.toLong(chunkX, chunkZ)

        if (corner % 2 == 0) {
            xDistance += xDirection
            if (abs(xDistance) == length) {
                corner++
                xDirection = -xDirection
            }
        } else {
            zDistance += zDirection
            if (abs(zDistance) == length) {
                corner++
                zDirection = -zDirection
                if (corner % 4 == 0) length++
            }
        }
    }
    return visible
}

private fun Double.toChunkCoordinate(): Int = Math.floorDiv(floor(), 16)
