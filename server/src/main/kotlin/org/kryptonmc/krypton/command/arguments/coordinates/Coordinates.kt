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
package org.kryptonmc.krypton.command.arguments.coordinates

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.kyori.adventure.text.Component
import org.kryptonmc.api.adventure.toMessage
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.space.Vector
import org.spongepowered.math.vector.Vector2d

sealed interface Coordinates {

    val relativeX: Boolean get() = true

    val relativeY: Boolean get() = true

    val relativeZ: Boolean get() = true

    fun position(player: Player): Vector

    fun rotation(player: Player): Vector2d
}

val ERROR_EXPECTED_DOUBLE = SimpleCommandExceptionType(Component.translatable("argument.pos.missing.double").toMessage())
val ERROR_EXPECTED_INTEGER = SimpleCommandExceptionType(Component.translatable("argument.pos.missing.int").toMessage())
