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
@file:JvmName("Boxes")
package org.kryptonmc.krypton.physics

import org.kryptonmc.api.block.BoundingBox

fun String.toBoundingBox(): BoundingBox {
    if (isEmpty() || isBlank()) return BoundingBox.EMPTY
    val string = removePrefix("AABB")
    if (string.isEmpty() || string.isBlank()) return BoundingBox.EMPTY
    val (minima, maxima) = string.split(" -> ")
    val (minX, minY, minZ) = minima.removePrefix("[").removeSuffix("]").split(", ").map { it.toDouble() }
    val (maxX, maxY, maxZ) = maxima.removePrefix("[").removeSuffix("]").split(", ").map { it.toDouble() }
    // Use common constants to reduce allocations
    if (minX == 0.0 && minY == 0.0 && minZ == 0.0) {
        if (maxX == 0.0 && maxY == 0.0 && maxZ == 0.0) return BoundingBox.EMPTY
        if (maxX == 1.0 && maxY == 1.0 && maxZ == 1.0) return BoundingBox.BLOCK
    }
    return BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
}

fun String.toBoundingBoxList() = listOf(BoundingBox.BLOCK)
