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

import org.kryptonmc.krypton.world.chunk.ChunkPosition
import kotlin.math.sqrt

fun Double.floor(): Int {
    val result = toInt()
    return if (this < result.toFloat()) result - 1 else result
}

/**
 * Calculates a chunk position from a given [id] in a spiral pattern.
 *
 * This algorithm was previously part of Krypton (https://github.com/KryptonMC/Krypton), however it was removed after
 * it was no longer used.
 *
 * **Algorithm:**
 *
 * Given n, an index in the squared spiral
 * p, the sum of a point in the inner square
 * and a, the position on the current square
 *
 * n = p + a
 *
 * Credit for this algorithm goes to
 * [davidonet](https://stackoverflow.com/users/1068670/davidonet) (for the original algorithm),
 * and [Esophose](https://github.com/Esophose) (for the Kotlin conversion and modifications)
 *
 * See [here](https://stackoverflow.com/questions/398299/looping-in-a-spiral) for original
 *
 * @param id the id in the spiral
 * @param xOffset an optional X offset
 * @param zOffset an optional Z offset
 * @return a [ChunkPosition] containing the calculated position in the spiral.
 */
fun chunkInSpiral(id: Int, xOffset: Int = 0, zOffset: Int = 0): ChunkPosition {
    // if the id is 0 then we know we're in the centre
    if (id == 0) return ChunkPosition(0 + xOffset, 0 + zOffset)

    val index = id - 1

    // compute radius (inverse arithmetic sum of 8 + 16 + 24 + ...)
    val radius = ((sqrt(index + 1.0) - 1) / 2).floor() + 1

    // compute total point on radius -1 (arithmetic sum of 8 + 16 + 24 + ...)
    val p = 8 * radius * (radius - 1) / 2

    // points by face
    val en = radius * 2

    // compute de position and shift it so the first is (-r, -r) but (-r + 1, -r)
    // so the square can connect
    val a = (1 + index - p) % (radius * 8)

    return when (a / (radius * 2)) {
        // find the face (0 = top, 1 = right, 2 = bottom, 3 = left)
        0 -> ChunkPosition(a - radius + xOffset, -radius + zOffset)
        1 -> ChunkPosition(radius + xOffset, a % en - radius + zOffset)
        2 -> ChunkPosition(radius - a % en + xOffset, radius + zOffset)
        3 -> ChunkPosition(-radius + xOffset, radius - a % en + zOffset)
        else -> ChunkPosition.ZERO
    }
}

fun Int.clamp(low: Int, high: Int) = if (this < low) low else if (this > high) high else this
